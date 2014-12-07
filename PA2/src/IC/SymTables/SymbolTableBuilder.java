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
			
			// check if the class is defined for the first time 
			if(globalSymTable.containsLocally(curr_class.getName()))
			{
				//TODO throw error
			}
			
			// create the class symbol table
			ClassSymbolTable classSymTable = new ClassSymbolTable(curr_class.getName());
			
			// actually fill classSymTable
			curr_class.accept(this, classSymTable);
				
			// add class entry to global symbol table
			
			ClassSymbol classSym = new ClassSymbol(curr_class.getName(), classSymTable);
			globalSymTable.addSymbol(classSym);
			

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
	public SymbolTable visit(ICClass icClass, SymbolTable classSymbolTable) {
		/* we need to fill current class's symbol table that was given to us */
		
		// first, fill all the fields
		List<Field> fields = icClass.getFields();
		for(Field field : fields)
		{
			FieldSymbol fieldSym = new FieldSymbol(field.getName());
			
			//if()
			
			classSymbolTable.addSymbol(fieldSym);
		}
		
		
		//icClass.getMethods()
		return null;
	}

	@Override
	public SymbolTable visit(Field field, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(VirtualMethod method, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(StaticMethod method, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(LibraryMethod method, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(Formal formal, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(PrimitiveType type, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(UserType type, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(Assignment assignment, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(CallStatement callStatement, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(Return returnStatement, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(If ifStatement, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(While whileStatement, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(Break breakStatement, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(Continue continueStatement, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(StatementsBlock statementsBlock,
			SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(LocalVariable localVariable, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SymbolTable visit(VariableLocation location, SymbolTable context) {
		// TODO Auto-generated method stub
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
