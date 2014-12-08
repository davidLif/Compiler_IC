package IC.SymTables;
import java.util.List;

import IC.AST.*;


public class SymbolTableBuilder implements  PropagatingVisitor<SymbolTable, SymbolTable>{

	
	public SymbolTable createGlobalSymbolTable(Program program, String fileName)
	{
		
		GlobalSymbolTable symTable = new GlobalSymbolTable(fileName);
		return program.accept(this, symTable);
		
	}
	
	@Override
	public SymbolTable visit(Program program, SymbolTable globalSymTable) {
		
		
		List<ICClass> classList = program.getClasses();
		for(ICClass curr_class : classList)
		{
			
			
			
			/* a class symbol will be added to globalSymTable, and a classSymbolTable will be returned */
			ClassSymbolTable classSymTable = (ClassSymbolTable) curr_class.accept(this, globalSymTable);
			

			if(curr_class.hasSuperClass())
			{
				String superClassName = curr_class.getSuperClassName();
				if(!globalSymTable.containsLocally(superClassName))
				{
					/* extends to a class that we have yet to see */
					//TODO : semantic error
					
				}
				else
				{
					
					// fetch super class symbol and its symbol table
			    	ClassSymbol super_symbol = (ClassSymbol) globalSymTable.getSymbol(superClassName);
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
	public SymbolTable visit(ICClass icClass, SymbolTable globalSymbolTable) {
		
		// check if the class is defined for the first time 
		if(globalSymbolTable.containsLocally(icClass.getName()))
		{
				//TODO throw error
		}
		
		// create the class symbol table
		ClassSymbolTable classSymTable = new ClassSymbolTable(icClass.getName());
		
		// add class entry to global symbol table
		ClassSymbol classSym = new ClassSymbol(icClass.getName(), classSymTable);
		globalSymbolTable.addSymbol(classSym);
		
		// link AST node to globalSymbolTable
		icClass.setEnclosingScope(globalSymbolTable);
		
		
		/* we need to fill current class's symbol table that was given to us */
		
		// first, fill all the fields
		List<Field> fields = icClass.getFields();
		for(Field field : fields)
		{
			// link scope to AST node
			field.setEnclosingScope(classSymTable);
			
			// check that the field was not defined in current scope
			if(classSymTable.containsLocally(field.getName()))
			{
				 //TODO throw error
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
				//TODO throw error
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
		
		
		FieldSymbol fieldSym = new FieldSymbol(field.getName());
		classSymbolTable.addSymbol(fieldSym);

		// no symbol table is constructed for field
		return null;
	}

	
	private void addFormalsToMethodSymTable(Method method, SymbolTable methodSymTable)
	{
		// go over argument list, and add them to symbol table
		List<Formal> formals = method.getFormals();
		for(Formal formal : formals)
		{
				// check that formal name was not defined before
				if(methodSymTable.containsLocally(formal.getName()))
				{
						//TODO throw error
				}
					
				// link AST
				formal.setEnclosingScope(methodSymTable);
				// add proper symbol to scope
				formal.accept(this, methodSymTable);
		}
				
	}
	
	private void addStatementsToSymTable(List<Statement> statements, SymbolTable methodSymTable)
	{
		
		for(Statement statement : statements)
		{
			/* link AST to method scope */
			statement.setEnclosingScope(methodSymTable);
			
			/* maybe get another sub scope */
			SymbolTable statementSymTable = statement.accept(this, methodSymTable);
			if(statementSymTable != null)
			{
				/* in case of statement block */
				methodSymTable.addChildTable(statementSymTable);
				statementSymTable.setParentSymbolTable(methodSymTable);
			}
		}
	}
	
	
	private  SymbolTable commonMethodVisit(Method method, MethodSymbol methodSym, SymbolTable classSymbolTable)
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
	public SymbolTable visit(VirtualMethod method, SymbolTable classSymbolTable) {
		
		MethodSymbol methodSym =  new VirtualMethodSymbol(method.getName());

		return commonMethodVisit(method, methodSym, classSymbolTable);
	}

	@Override
	public SymbolTable visit(StaticMethod method, SymbolTable classSymbolTable) {
		
		MethodSymbol methodSym =  new StaticMethodSymbol(method.getName());
		return commonMethodVisit(method, methodSym, classSymbolTable);
	}

	@Override
	public SymbolTable visit(LibraryMethod method, SymbolTable classSymbolTable) {
		
		MethodSymbol methodSym =  new StaticMethodSymbol(method.getName());
		return commonMethodVisit(method, methodSym, classSymbolTable);
	}

	@Override
	public SymbolTable visit(Formal formal, SymbolTable methodSymbolTable) {
		
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
	public SymbolTable visit(StatementsBlock statementsBlock,
			SymbolTable enclosingScope) {
		
		StatementBlockSymTable symTable = new StatementBlockSymTable(enclosingScope.getId());
		addStatementsToSymTable(statementsBlock.getStatements(), symTable);

		// no need to create a symbol
		return symTable;
	}

	@Override
	public SymbolTable visit(LocalVariable localVariable, SymbolTable scope) {
		
		String name = localVariable.getName();
		// check redefinition
		if(scope.containsLocally(name))
		{
			 //TODO throw error
		}
		
		LocalVariableSymbol varSym = new LocalVariableSymbol(name);
		scope.addSymbol(varSym);
		
		// no symbol table is constructed
		return null;
	}

	@Override
	public SymbolTable visit(VariableLocation location, SymbolTable context) {
		
		if(!location.isExternal())
		{
			// variable needs to be resolved now
			
			
		}
		return null;
	}

	@Override
	public SymbolTable visit(ArrayLocation location, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(StaticCall call, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(VirtualCall call, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(This thisExpression, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(NewClass newClass, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(NewArray newArray, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(Length length, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(MathBinaryOp binaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(LogicalBinaryOp binaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(MathUnaryOp unaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(LogicalUnaryOp unaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(Literal literal, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(ExpressionBlock expressionBlock,
			SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
