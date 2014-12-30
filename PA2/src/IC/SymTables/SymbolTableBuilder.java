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

	
	private GlobalSymbolTable globalSymTable;
	
	
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
		this.globalSymTable = symTable;
		return program.accept(this, symTable);
		
	}
	
	@Override
	public SymbolTable visit(Program program, SymbolTable globalSymTable) throws SemanticError {
		

		
		/* iterate over the classes in the program */
		List<ICClass> classList = program.getClasses();
		for(ICClass curr_class : classList)
		{
			
			
			// check if the class is defined for the first time 
			if(globalSymTable.containsLocally(curr_class.getName()))
			{
				
				String err_msg = String.format("class %s was already defined ", curr_class.getName());
				throw new SemanticError(curr_class.getLine(), err_msg);
			}
			
			if(curr_class.hasSuperClass())
			{
				String superClassName = curr_class.getSuperClassName();
				
				if(curr_class.getName().equals( superClassName))
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
					ClassSymbol super_symbol =  ((GlobalSymbolTable)globalSymTable).getClassSymbol(superClassName);
					// fetch super class symbol table
				    SymbolTable super_symbol_table = super_symbol.getClassSymbolTable();
				    
					/* a class symbol will be added to globalSymTable, and a classSymbolTable will be returned */
					ClassSymbolTable classSymTable = (ClassSymbolTable) curr_class.accept(this, super_symbol_table);
				   
				    // add classSymTable as a child of superclass's symbol table
				    super_symbol_table.addChildTable(classSymTable);
				    	    
					
				}
				
			}
			else
			{
				// has no super class, enclosing scope is global scope
				ClassSymbolTable classSymTable = (ClassSymbolTable) curr_class.accept(this, globalSymTable);
				
				globalSymTable.addChildTable(classSymTable);
			}
			
		}
		
	
		
		return globalSymTable;
	}



	
	@Override
	public SymbolTable visit(ICClass icClass, SymbolTable parentSymTable) throws SemanticError {
	
		
		// create the class symbol table
		ClassSymbolTable classSymTable = new ClassSymbolTable(icClass.getName());
		
		// add class entry to global symbol table
		ClassSymbol classSym = new ClassSymbol(icClass.getName(), classSymTable);
		((GlobalSymbolTable)globalSymTable).addClassSymbol(classSym);
		
		// link AST node to globalSymbolTable
		icClass.setEnclosingScope(parentSymTable);
		classSymTable.setParentSymbolTable(parentSymTable);
		
		
		/* we need to fill current class's symbol table that we built */
		
		// first, fill all the fields
		List<Field> fields = icClass.getFields();
		for(Field field : fields)
		{
			
			// check that the field was not defined in current scope
			if(classSymTable.containsLocally(field.getName()))
			{
				String err_msg = String.format("identifer %s was already defined in class %s (instance scope)",field.getName(), icClass.getName());
				throw new SemanticError(field.getLine(), err_msg);
			}
			
			// otherwise, add field as symbol to table
			field.accept(this, classSymTable);
		}
		
		// now, fill all the methods
		List<Method> methods = icClass.getMethods();
		
		for(Method method : methods)
		{
			
			// check that method was not defined before (in current scope)
			if(classSymTable.containsLocallyStatic(method.getName()))
			{
				if(method.isStatic())
				{
					String err_msg = String.format("static method %s was already defined in class %s", method.getName(), icClass.getName());
					throw new SemanticError(method.getLine(), err_msg);
				}
			}
			else if(classSymTable.containsLocallyVirtual(method.getName()))
			{
				if(!method.isStatic())
				{
					String err_msg = String.format("id %s was already defined in class %s (instance scope)", method.getName(), icClass.getName());
					throw new SemanticError(method.getLine(), err_msg);
				}
			}
			
			
			// fill the method table
			SymbolTable methodSymTable =  method.accept(this, classSymTable);
			
			// link parent/child
			classSymTable.addChildTable(methodSymTable);
			
			
			
		}
		
		
		// all done
		return classSymTable;
	}

	@Override
	public SymbolTable visit(Field field, SymbolTable classSymbolTable) {
		
		// link scope to AST node
		field.setEnclosingScope(classSymbolTable);
		
		/* simply add the created symbol to the given symbol table */
		
		FieldSymbol fieldSym = new FieldSymbol(field.getName());
		((ClassSymbolTable)classSymbolTable).addField(fieldSym);

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
			
			 statement.accept(this, symTable);
		}
	}
	
	
	/**
	 * 
	 * this method handles all methods visits
	 * 
	 * @param method  -   The method AST node
	 * 
	 * @param classSymbolTable - the symbol table of enclosing class
	 * @return proper method symbol table
	 * @throws SemanticError 
	 */
	
	private  SymbolTable commonMethodVisit(Method method, ClassSymbolTable classSymbolTable, boolean isStatic) throws SemanticError
	{
        // link method to classSymbolTable
		method.setEnclosingScope(classSymbolTable);
		
		// create method symbol table
		MethodSymbolTable methodSymTable = new MethodSymbolTable(method.getName(), isStatic);
		methodSymTable.setParentSymbolTable(classSymbolTable);
		
		// go over argument list, and add them to symbol table
		addFormalsToMethodSymTable(method, methodSymTable);
		
		// now , go over statements
		List<Statement> statements = method.getStatements();
		
		addStatementsToSymTable(statements, methodSymTable);
		
		return methodSymTable;
	}
	
	
	@Override
	public SymbolTable visit(VirtualMethod method, SymbolTable classSymbolTable) throws SemanticError {
		
		ClassSymbolTable classSymTable = (ClassSymbolTable)classSymbolTable;
		VirtualMethodSymbol methodSym =  new VirtualMethodSymbol(method.getName(), classSymTable.getId());
		
		/* add symbol */
		classSymTable.addVirtualMethod(methodSym);
		
		
		return commonMethodVisit(method, classSymTable, false);
	}

	@Override
	public SymbolTable visit(StaticMethod method, SymbolTable classSymbolTable) throws SemanticError {
		
		ClassSymbolTable classSymTable = (ClassSymbolTable)classSymbolTable;
		StaticMethodSymbol methodSym =  new StaticMethodSymbol(method.getName(), classSymTable.getId());
		
		/* add symbol */
		classSymTable.addStaticMethod(methodSym);
		
		return commonMethodVisit(method, classSymTable, true);
	}

	@Override
	public SymbolTable visit(LibraryMethod method, SymbolTable classSymbolTable) throws SemanticError {
		
		/* same as static method */
		
		ClassSymbolTable classSymTable = (ClassSymbolTable)classSymbolTable;
		StaticMethodSymbol methodSym =  new StaticMethodSymbol(method.getName(), classSymTable.getId());
	
		
		/* add symbol */
		classSymTable.addStaticMethod(methodSym);
		
		return commonMethodVisit(method, classSymTable, true);
	}

	@Override
	public SymbolTable visit(Formal formal, SymbolTable scope) throws SemanticError {
		
		/* link AST to scope */
		formal.setEnclosingScope(scope);
		
		/* simply add new symbol to given scope */
		ParameterSymbol param = new ParameterSymbol(formal.getName());
		MethodSymbolTable methodTable =  (MethodSymbolTable)scope;
		
		methodTable.addParameter(param);
		
		formal.getType().accept(this, scope);
		
		return null;
	}

	@Override
	public SymbolTable visit(PrimitiveType type, SymbolTable context) {
		
		/* link AST to scope */
		type.setEnclosingScope(context);
		
		return null;
	}

	@Override
	public SymbolTable visit(UserType type, SymbolTable context) {
		/* link AST to scope */
		type.setEnclosingScope(context);
		
		return null;
	}

	@Override
	public SymbolTable visit(Assignment assignment, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		assignment.setEnclosingScope(context);
		
		/* continue visiting sub expressions */
		assignment.getVariable().accept(this, context);
		assignment.getAssignment().accept(this, context);
		
		return null;
	}

	@Override
	public SymbolTable visit(CallStatement callStatement, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		callStatement.setEnclosingScope(context);
		
		callStatement.getCall().accept(this, context);
		
		return null;
	}

	@Override
	public SymbolTable visit(Return returnStatement, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		returnStatement.setEnclosingScope(context);
		
		if(returnStatement.hasValue())
			returnStatement.getValue().accept(this, context);

		return null;
	}

	@Override
	public SymbolTable visit(If ifStatement, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		ifStatement.setEnclosingScope(context);
		
		/* visit condition expression */
		ifStatement.getCondition().accept(this, context);
		/* visit action statement */
		ifStatement.getOperation().accept(this, context);
		
		/* visit else statement */
		if(ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this, context);
		
		return null;
	}

	@Override
	public SymbolTable visit(While whileStatement, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		whileStatement.setEnclosingScope(context);
		
		/* visit condition */
		whileStatement.getCondition().accept(this, context);
		/* visit statement */
		whileStatement.getOperation().accept(this, context);
		
		return null;
	}

	@Override
	public SymbolTable visit(Break breakStatement, SymbolTable context) {
		/* link AST to scope */
		breakStatement.setEnclosingScope(context);
		
		return null;
	}

	@Override
	public SymbolTable visit(Continue continueStatement, SymbolTable context) {
		
		/* link AST to scope */
		continueStatement.setEnclosingScope(context);
		
		return null;
	}

	@Override
	public SymbolTable visit(StatementsBlock statementsBlock, SymbolTable enclosingScope) throws SemanticError {
		
		/* link AST to scope */
		statementsBlock.setEnclosingScope(enclosingScope);
		
		/* create statementblock sym table and add parent scope */
		StatementBlockSymTable symTable = new StatementBlockSymTable(enclosingScope);
		enclosingScope.addChildTable(symTable);
		
		addStatementsToSymTable(statementsBlock.getStatements(), symTable);
		
		return symTable;
	}

	@Override
	public SymbolTable visit(LocalVariable localVariable, SymbolTable scope) throws SemanticError {
		
		/* link AST to scope */
		localVariable.setEnclosingScope(scope);
		
		String name = localVariable.getName();
		
		// check redefinition
		if(scope.containsLocally(name))
		{
			String err_msg = String.format("variable with same id, %s, was already defined in current scope: %s", name, scope.getId());
			throw new SemanticError(localVariable.getLine(), err_msg);
		}
		
		LocalVariableSymbol varSym = new LocalVariableSymbol(name);
		/* scope is either method scope, or statement block */
		((VariableSymbolTable)scope).addLocalVariable(varSym);
		
		// visit init expression
		
		if(localVariable.hasInitValue())
			localVariable.getInitValue().accept(this, scope);
		
		// no symbol table is constructed
		return null;
	}

	@Override
	public SymbolTable visit(VariableLocation location, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		location.setEnclosingScope(context);
		
		
		if(location.isExternal())
		{
			location.getLocation().accept(this, context);
		}
		
		else
		{
			// variable needs to be resolved now
			if(!((VariableSymbolTable)context).resolveVariable(location))
			{
				String err_msg = String.format("%s could not be resolved to a variable", location.getName());
				throw new SemanticError(location.getLine(), err_msg);
			}
			
		}
		
		/* no scope is created */
		return null;
	}

	@Override
	public SymbolTable visit(ArrayLocation location, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		location.setEnclosingScope(context);
	
		Expression arrExp = location.getArray();
		Expression indexExp = location.getIndex();
		
		arrExp.accept(this, context);
		indexExp.accept(this, context);
		
		
		return null;
	}

	@Override
	public SymbolTable visit(StaticCall call, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		call.setEnclosingScope(context);
		
		List<Expression> args = call.getArguments();
		/* visit each argument */
		for(Expression arg : args)
		{
			arg.accept(this, context);
		}
		
		
		return null;
	}

	@Override
	public SymbolTable visit(VirtualCall call, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		call.setEnclosingScope(context);
	
		if(call.isExternal())
		{
			/* visit location expression */
			call.getLocation().accept(this, context);
		}
		
		List<Expression> args = call.getArguments();
		/* visit each argument */
		for(Expression arg : args)
		{
			arg.accept(this, context);
		}
		
		return null;
	}

	@Override
	public SymbolTable visit(This thisExpression, SymbolTable context) {
		
		/* link AST to scope */
		thisExpression.setEnclosingScope(context);
		
		return null;
	}

	@Override
	public SymbolTable visit(NewClass newClass, SymbolTable context) {
		
		/* link AST to scope */
		newClass.setEnclosingScope(context);
		
		
		return null;
	}

	@Override
	public SymbolTable visit(NewArray newArray, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		newArray.setEnclosingScope(context);
		
		/* visit the size expression */
		newArray.getSize().accept(this, context);
		
		/* visit type */
		newArray.getType().accept(this, context);
		
		return null;
	}

	@Override
	public SymbolTable visit(Length length, SymbolTable context) throws SemanticError {
		/* link AST to scope */
		length.setEnclosingScope(context);
		
		Expression array = length.getArray();
		
		/* visit the array expression */
		array.accept(this, context);
		
		return null;
	}

	@Override
	public SymbolTable visit(MathBinaryOp binaryOp, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		binaryOp.setEnclosingScope(context);
		
		binaryOp.getFirstOperand().accept(this, context);
		binaryOp.getSecondOperand().accept(this, context);
		
		return null;
	}

	@Override
	public SymbolTable visit(LogicalBinaryOp binaryOp, SymbolTable context) throws SemanticError {
		/* link AST to scope */
		binaryOp.setEnclosingScope(context);
		
		binaryOp.getFirstOperand().accept(this, context);
		binaryOp.getSecondOperand().accept(this, context);
		return null;
	}

	@Override
	public SymbolTable visit(MathUnaryOp unaryOp, SymbolTable context) throws SemanticError {
		
		
		/* link AST to scope */
		unaryOp.setEnclosingScope(context);
		
		unaryOp.getOperand().accept(this, context);
		return null;
	}

	@Override
	public SymbolTable visit(LogicalUnaryOp unaryOp, SymbolTable context) throws SemanticError {
		
		/* link AST to scope */
		unaryOp.setEnclosingScope(context);
		
		unaryOp.getOperand().accept(this, context);
		return null;
	}

	@Override
	public SymbolTable visit(Literal literal, SymbolTable context) {
		/* link AST to scope */
		literal.setEnclosingScope(context);
	
		
		return null;
	}

	@Override
	public SymbolTable visit(ExpressionBlock expressionBlock,
			SymbolTable context) throws SemanticError {
		/* link AST to scope */
		expressionBlock.setEnclosingScope(context);
		
		expressionBlock.getExpression().accept(this, context);
		return null;
	}
	
	

}
