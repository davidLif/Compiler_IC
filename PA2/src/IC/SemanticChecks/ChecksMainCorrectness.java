package IC.SemanticChecks;
import java.util.List;

import IC.DataTypes;
import IC.AST.*;


import IC.SemanticChecks.SemanticError;

import IC.Types.TypeTable;
;


/**
 * 
 * This class checks the following structural checks:
 * 
 * 		1. exactly one main method is found with the correct signature
 * 
 * This class also adds the types:
 		string[]
 		{ string[] -> void }
 		in that order to the type table

 */

public class ChecksMainCorrectness implements  Visitor  {

	
	private boolean found_main;
	private TypeTable typeTable;
	private Program prog;
	private int lineOfDef;  /* line where we found main method */
	
	
	public ChecksMainCorrectness(Program program, TypeTable typeTable)
	{
		this.prog = program;
		this.typeTable = typeTable;
		this.found_main = false;
	}
	
	
	public void check() throws SemanticError
	{
		 this.prog.accept(this);	
		 
		 /* checking that the class has exactly one main function. Since we don't allow more than one 
		  * main, if found_min == false then no main exists.
		  */
		 if ( found_main == false)
		 {
				String err_msg = "No main method found";
				throw new SemanticError(prog.getLine(), err_msg);
		
		 }
	}
	
	
	@Override
	public Object visit(Program program) throws SemanticError {
		
		/* iterate over the classes in the program */
		List<ICClass> classList = program.getClasses();
		
		for(ICClass curr_class : classList)
		{
			curr_class.accept(this );
			
		}
		
		return null;
	}

	@Override
	public Object visit(ICClass icClass) throws SemanticError {
		List<Method> methods = icClass.getMethods();
		
		/* visit each method */
		for(Method method : methods)
		{
			method.accept(this);
			
		}
		
		return null;
	}

	@Override
	public Object visit(Field field) throws SemanticError {
		// wont visit fields
		return null;
	}

	@Override
	public Object visit(VirtualMethod method)
			throws SemanticError {
		
	
		
		/* checking that it's not a method named 'main' */
		if ( method.getName().equals("main") && found_main ) 
		{	
			String err_msg = "Only one method named 'main' can be defined (and it should be a static method)";
			throw new SemanticError(method.getLine(), err_msg);
		}
		else if( method.getName().equals("main"))
		{
			String err_msg = "main method should be defined as a static method, not a virtual method";
			throw new SemanticError(method.getLine(), err_msg);
		}
		
		
		return null;
		
	}
	


	@Override
	public Object visit(StaticMethod method)
			throws SemanticError {

		String methodName = method.getName();
		
		
		if ( methodName.equals("main"))
		{
			
			
			if(found_main ) /* we have already seen a main function */
			{
				String err_msg = "A main method was already defined before in the program, at line: " + this.lineOfDef;
				throw new SemanticError(method.getLine(), err_msg);
			}
			
			/* checking that it's in a correct form of a main function */
	
			boolean error = false;
			IC.AST.Type methodReturnType = method.getType();
			if(methodReturnType instanceof PrimitiveType)
			{
				/* check that void return type */
				PrimitiveType retPrimitiveType = (PrimitiveType)methodReturnType;
				if(retPrimitiveType.getDataTypes() != DataTypes.VOID)
				{
					// invalid return type
					error = true;
					
				}
			}
			else
			{
				// invalid return type
				error = true;
				
			}
			
			if(error)
			{
				String err = String.format("invalid main return type, should be void");
				throw new SemanticError(method.getLine(), err);
				
			}
			
			List<Formal> formals = method.getFormals();
			
			if(formals.size() != 1)
			{
				String err = String.format("main should receive only one argument of type string[], instead receives %d arguments", formals.size());
				throw new SemanticError(method.getLine(), err);
			}
			
			IC.AST.Type formalType = formals.get(0).getType();
			if(formalType instanceof UserType)
			{
				error = true;
			}
			else {
				
				PrimitiveType primitiveFormal = (PrimitiveType)formalType;
				if(primitiveFormal.getDataTypes() != DataTypes.STRING || primitiveFormal.getDimension() != 1)
					error = true;
				
			}
			
			
			if(error)
			{
				String err = String.format("main should receive only one argument of type string[], instead receives argument of type %s", formalType);
				throw new SemanticError(method.getLine(), err);
			}
			
			found_main = true;
			this.lineOfDef = method.getLine();
			
			
			/* add the types */
			/* first add string[] */
			typeTable.getType(formalType);
			/* add method type */
			typeTable.getMethodType(method);
			 
		}
		
	
		return null;
	}

	@Override
	public Object visit(LibraryMethod method)
			throws SemanticError {
		
		/* library method cannot contain a method named main, according to the forum */
		if(method.getName().equals("main"))
		{
			String err_msg = "Library class cannot contain a method named 'main'";
			throw new SemanticError(method.getLine(), err_msg);
		}
		
		
		return null;
	}

	@Override
	public Object visit(Formal formal) throws SemanticError {
		return true;
	}

	@Override
	public Object visit(PrimitiveType type)
			throws SemanticError {
		return true;
	}

	@Override
	public Object visit(UserType type) throws SemanticError {
		return true;
	}

	@Override
	public Object visit(Assignment assignment)
			throws SemanticError {
			
	
		
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement)
			throws SemanticError {
		
	
		return null;
	}

	@Override
	public Object visit(Return returnStatement)
			throws SemanticError {
		
	
		return null;
	}

	@Override
	public Object visit(If ifStatement) throws SemanticError {
		
		
	

		return null;
	}

	@Override
	public Object visit(While whileStatement)
			throws SemanticError {
			
	
		return null;
	}

	@Override
	public Object visit(Break breakStatement)
			throws SemanticError {

		return null;
		
	}

	@Override
	public Object visit(Continue continueStatement)
			throws SemanticError {
		
		
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock)
			throws SemanticError {
		 
		 
		 return null;
	}

	@Override
	public Object visit(LocalVariable localVariable)
			throws SemanticError {
			
		return null;
	}

	@Override
	public Object visit(VariableLocation location)
			throws SemanticError {
		
		return null;
	}

	@Override
	public Object visit(ArrayLocation location)
			throws SemanticError {
		
		return null;
	}

	@Override
	public Object visit(StaticCall call) throws SemanticError {
		

		
		
		return null;
	}

	@Override
	public Object visit(VirtualCall call) throws SemanticError {
		

		return null;
	}

	@Override
	public Object visit(This thisExpression)
			throws SemanticError {
		

		
		return null;
		
	
	}

	@Override
	public Object visit(NewClass newClass)
			throws SemanticError {

		return null;
	}

	@Override
	public Object visit(NewArray newArray)
			throws SemanticError {
		
		
		return null;
	}

	@Override
	public Object visit(Length length) throws SemanticError {
		
		
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp)
			throws SemanticError {
		
	
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp)
			throws SemanticError {
		
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp)
			throws SemanticError {
		
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp)
			throws SemanticError {
		
		return null;
	}

	@Override
	public Object visit(Literal literal) throws SemanticError {
		
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock)
			throws SemanticError {
		
		
		return null;
	}

}