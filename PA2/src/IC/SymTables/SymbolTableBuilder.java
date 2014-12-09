package IC.SymTables;
import java.util.List;

import IC.AST.*;
import IC.SymTables.Symbols.ClassSymbol;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.LocalVariableSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.ParameterSymbol;
import IC.SymTables.Symbols.StaticMethodSymbol;
import IC.SymTables.Symbols.VirtualMethodSymbol;
import IC.SemanticChecks.SemanticError;



/**
 * 
 * @author Denis
 *
 *
 * This visitor propagates the enclosing SymbolTable (enclosing scope), each visit returns the new Symbol table that was built
 * 
 * Use createGlobalSymbolTable to create a symbol table for an AST.
 * Note that, this visitor does not fill in types of symbols.
 * 
 * 
 * This visitor will also perform the following semantic checks along the way:
 * 
 * 1. checks that an id was not defined more than once in any scope (including variable redefinitions)
 * 2. checks that local variables were defined before use
 * 3. checks that the class hierarchy is a tree
 * 
 * 
 */

public class SymbolTableBuilder implements  PropagatingVisitor<SymbolTable, SymbolTable>{

	
	
	
	/**
	 * 
	 * This method creates a SymbolTable (GlobalSymbolTable) for program. note that the table will not be annotated with types.
	 * must be done in a later phase
	 * 
	 * @param program - AST root
	 * @param fileName - name of program
	 * @return global SymbolTable
	 * @throws SemanticError 
	 */
	public SymbolTable createGlobalSymbolTable(Program program, String fileName) throws SemanticError
	{
		
		GlobalSymbolTable symTable = new GlobalSymbolTable(fileName);
		return program.accept(this, symTable);
		
	}
	
	@Override
	public SymbolTable visit(Program program, SymbolTable globalSymTable) throws SemanticError {
		
		
		/* iterate over the classes in the program */
		List<ICClass> classList = program.getClasses();
		for(ICClass curr_class : classList)
		{
			
			/* a class symbol will be added to globalSymTable, and a classSymbolTable will be returned */
			ClassSymbolTable classSymTable = (ClassSymbolTable) curr_class.accept(this, globalSymTable);
			
			if(curr_class.hasSuperClass())
			{
				String superClassName = curr_class.getSuperClassName();
				
				if(curr_class.getName() == superClassName)
				{
					/* a class cannot extend it self */
					String err_msg = String.format("class %s cannot extends itself", curr_class.getName());
					throw new SemanticError(curr_class.getLine(), err_msg);
				}
				
				if(!globalSymTable.containsLocally(superClassName))
				{
					/* extends to a class that we have yet to see */
					
					String err_msg = String.format("class %s extends %s, a class that was yet to be defined", curr_class.getName(), superClassName );
					throw new SemanticError(curr_class.getLine(), err_msg);
					
				}
				else
				{
					ClassSymbol super_symbol = (ClassSymbol) globalSymTable.getSymbol(superClassName);
					// fetch super class symbol table
				    SymbolTable super_symbol_table = super_symbol.getClassSymbolTable();
				    
				    // add classSymTable as a child of superclass's symbol table
				    super_symbol_table.addChildTable(classSymTable);
				    // set enclosing scope
				    classSymTable.setParentSymbolTable(super_symbol_table);
					
				}
				
			}
			else
			{
				// has no super class, enclosing scope is global scope
				globalSymTable.addChildTable(classSymTable);
				classSymTable.setParentSymbolTable(globalSymTable);
			}
			
		}
		
	
		
		return globalSymTable;
	}



	
	@Override
	public SymbolTable visit(ICClass icClass, SymbolTable globalSymbolTable) throws SemanticError {
		
		// check if the class is defined for the first time 
		if(globalSymbolTable.containsLocally(icClass.getName()))
		{
			
			String err_msg = String.format("class %s was already defined ", icClass.getName());
			throw new SemanticError(icClass.getLine(), err_msg);
		}
		
		// create the class symbol table
		ClassSymbolTable classSymTable = new ClassSymbolTable(icClass.getName());
		
		// add class entry to global symbol table
		ClassSymbol classSym = new ClassSymbol(icClass.getName(), classSymTable);
		globalSymbolTable.addSymbol(classSym);
		
		// link AST node to globalSymbolTable
		icClass.setEnclosingScope(globalSymbolTable);
		
		
		/* we need to fill current class's symbol table that we built */
		
		// first, fill all the fields
		List<Field> fields = icClass.getFields();
		for(Field field : fields)
		{
			// link scope to AST node
			field.setEnclosingScope(classSymTable);
			
			// check that the field was not defined in current scope
			if(classSymTable.containsLocally(field.getName()))
			{
				String err_msg = String.format("field %s was already defined in class %s",field.getName(), icClass.getName());
				throw new SemanticError(field.getLine(), err_msg);
			}
			
			// otherwise, add field as symbol to table
			field.accept(this, classSymTable);
		}
		
		// now, fill all the methods
		List<Method> methods = icClass.getMethods();
		
		for(Method method : methods)
		{
			// link scope to AST node
			method.setEnclosingScope(classSymTable);
			
			// check that method was not defined before (in current scope)
			if(classSymTable.containsLocally(method.getName()))
			{
				String err_msg = String.format("field/method with id %s was already defined in class %s", method.getName(), icClass.getName());
				throw new SemanticError(method.getLine(), err_msg);
			}
			
			// fill the method table
			SymbolTable methodSymTable =  method.accept(this, classSymTable);
			
			// link parent/child
			methodSymTable.setParentSymbolTable(classSymTable);
			classSymTable.addChildTable(methodSymTable);
			
		}
		
		
		// all done
		return classSymTable;
	}

	@Override
	public SymbolTable visit(Field field, SymbolTable classSymbolTable) {
		
		/* simply add the created symbol to the given symbol table */
		
		FieldSymbol fieldSym = new FieldSymbol(field.getName());
		classSymbolTable.addSymbol(fieldSym);

		// no symbol table is constructed for field
		return null;
	}
	
	
	/*
	 * this method adds all formals from given method as symbols in methodSymTable
	 */
	
	private void addFormalsToMethodSymTable(Method method, SymbolTable methodSymTable) throws SemanticError
	{
		// go over argument list, and add them to symbol table
		List<Formal> formals = method.getFormals();
		for(Formal formal : formals)
		{
				// check that formal name was not defined before
				if(methodSymTable.containsLocally(formal.getName()))
				{
					String err_msg = String.format("formal %s was already defined in method %s", formal.getName(), method.getName());
					throw new SemanticError(formal.getLine(), err_msg);
				}
					
				// link AST
				formal.setEnclosingScope(methodSymTable);
				// add proper symbol to scope
				formal.accept(this, methodSymTable);
		}
				
	}
	
	/*
	 * this method goes over the given list of statements and adds them to the given symbol table 
	 */
	
	private void addStatementsToSymTable(List<Statement> statements, SymbolTable symTable) throws SemanticError
	{
		
		for(Statement statement : statements)
		{
			/* link AST to method scope */
			statement.setEnclosingScope(symTable);
			
			/* maybe get another sub scope */
			SymbolTable statementSymTable = statement.accept(this, symTable);
			if(statementSymTable != null)
			{
				/* in case of statement block */
				symTable.addChildTable(statementSymTable);
				statementSymTable.setParentSymbolTable(symTable);
			}
		}
	}
	
	
	/**
	 * 
	 * this method handles all methods visits
	 * 
	 * @param method  -   The method AST node
	 * @param methodSym - method symbol
	 * @param classSymbolTable - the symbol table of enclosing class
	 * @return proper method symbol table
	 * @throws SemanticError 
	 */
	
	private  SymbolTable commonMethodVisit(Method method, MethodSymbol methodSym, SymbolTable classSymbolTable) throws SemanticError
	{

		// add the method symbol to class symbol table
		classSymbolTable.addSymbol(methodSym);
		
		// create method symbol table
		MethodSymbolTable methodSymTable = new MethodSymbolTable(method.getName());
		
		// go over argument list, and add them to symbol table
		addFormalsToMethodSymTable(method, methodSymTable);
		
		// now , go over statements
		List<Statement> statements = method.getStatements();
		
		addStatementsToSymTable(statements, methodSymTable);
		
		return methodSymTable;
	}
	
	
	@Override
	public SymbolTable visit(VirtualMethod method, SymbolTable classSymbolTable) throws SemanticError {
		
		MethodSymbol methodSym =  new VirtualMethodSymbol(method.getName());
		return commonMethodVisit(method, methodSym, classSymbolTable);
	}

	@Override
	public SymbolTable visit(StaticMethod method, SymbolTable classSymbolTable) throws SemanticError {
		
		MethodSymbol methodSym =  new StaticMethodSymbol(method.getName());
		return commonMethodVisit(method, methodSym, classSymbolTable);
	}

	@Override
	public SymbolTable visit(LibraryMethod method, SymbolTable classSymbolTable) throws SemanticError {
		
		MethodSymbol methodSym =  new StaticMethodSymbol(method.getName());
		return commonMethodVisit(method, methodSym, classSymbolTable);
	}

	@Override
	public SymbolTable visit(Formal formal, SymbolTable methodSymbolTable) {
		
		/* simply add new symbol to given scope */
		ParameterSymbol param = new ParameterSymbol(formal.getName());
		methodSymbolTable.addSymbol(param);
		return null;
	}

	@Override
	public SymbolTable visit(PrimitiveType type, SymbolTable context) {
		
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(UserType type, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(Assignment assignment, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(CallStatement callStatement, SymbolTable context) {
		// nothing to do
		// call statements will be handled in later phases
		return null;
	}

	@Override
	public SymbolTable visit(Return returnStatement, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(If ifStatement, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(While whileStatement, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(Break breakStatement, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(Continue continueStatement, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(StatementsBlock statementsBlock, SymbolTable enclosingScope) throws SemanticError {
		
		StatementBlockSymTable symTable = new StatementBlockSymTable(enclosingScope);
		addStatementsToSymTable(statementsBlock.getStatements(), symTable);

		// no need to create a symbol !
		
		return symTable;
	}

	@Override
	public SymbolTable visit(LocalVariable localVariable, SymbolTable scope) throws SemanticError {
		
		String name = localVariable.getName();
		// check redefinition
		if(scope.containsLocally(name))
		{
			String err_msg = String.format("variable with same id, %s, was already defined in current scope: %s", name, scope.getId());
			throw new SemanticError(localVariable.getLine(), err_msg);
		}
		
		LocalVariableSymbol varSym = new LocalVariableSymbol(name);
		scope.addSymbol(varSym);
		
		// no symbol table is constructed
		return null;
	}

	@Override
	public SymbolTable visit(VariableLocation location, SymbolTable context) throws SemanticError {
		
		if(!location.isExternal())
		{
			// variable needs to be resolved now
			if(!context.resolveVariable(location.getName()))
			{
				String err_msg = String.format("%s could not be resolved to a variable", location.getName());
				throw new SemanticError(location.getLine(), err_msg);
			}
			
		}
		
		/* no scope is created */
		return null;
	}

	@Override
	public SymbolTable visit(ArrayLocation location, SymbolTable context) {
		
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(StaticCall call, SymbolTable context) {
		// calls are handled in later phases
		return null;
	}

	@Override
	public SymbolTable visit(VirtualCall call, SymbolTable context) {
		// calls are handled in later phases
		return null;
	}

	@Override
	public SymbolTable visit(This thisExpression, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(NewClass newClass, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(NewArray newArray, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(Length length, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(MathBinaryOp binaryOp, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(LogicalBinaryOp binaryOp, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(MathUnaryOp unaryOp, SymbolTable context) {
		// nothing to doub
		return null;
	}

	@Override
	public SymbolTable visit(LogicalUnaryOp unaryOp, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(Literal literal, SymbolTable context) {
		// nothing to do
		return null;
	}

	@Override
	public SymbolTable visit(ExpressionBlock expressionBlock,
			SymbolTable context) {
		// nothing to do
		return null;
	}
	
	

}
