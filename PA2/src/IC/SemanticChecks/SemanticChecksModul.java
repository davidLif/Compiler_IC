package IC.SemanticChecks;
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



public class SemanticChecksModul implements  PropagatingVisitor<Object, Boolean>  {

	
	boolean inside_for_or_while_loop = false;
	boolean inside_virtual_function = false;
	int how_many_main_functions_found = 0;
	
	public Boolean check(Program program) throws SemanticError
	{
		return program.accept(this, null);	
	}
	
	
	@Override
	public Boolean visit(Program program, Object context) throws SemanticError {
		/* iterate over the classes in the program */
		List<ICClass> classList = program.getClasses();
		boolean rc;
		for(ICClass curr_class : classList)
		{
			rc = curr_class.accept(this , null);
			if ( rc == false)
				return false;
		}
		
		return true;
	}

	@Override
	public Boolean visit(ICClass icClass, Object context) throws SemanticError {
		List<Method> methods = icClass.getMethods();
		boolean rc;
		for(Method method : methods)
		{
			rc = method.accept(this, null);
			if ( rc == false)
				return false;
		}
		
		return true;
	}

	@Override
	public Boolean visit(Field field, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(VirtualMethod method, Object context)
			throws SemanticError {
		
		/* checking that it's not a main function */
		if ( method.getName().equals("main"))
		{
			String err_msg = "Found a non static main method";
			throw new SemanticError(method.getLine(), err_msg);
		}
		
		inside_virtual_function = true;
		method.accept(this,null);
		inside_virtual_function = false;
		return null;
	}

	@Override
	public Boolean visit(StaticMethod method, Object context)
			throws SemanticError {

		String methodName = method.getName();
		List<Formal> params;
		params = method.getFormals();
		if ( methodName.equals("Main"))
		{
			/* checking that it's a real main function */
			boolean fail = false;
			how_many_main_functions_found++;
			if( how_many_main_functions_found > 1)
			{
				fail = true;
			}
			/*if ( method.getType() != "VOID" )  denis how should I check it! 
			{
				fail = true;
			}
		
			if( params.length != 1 && params.get(0).getType() = "string array")
			{
				fail = true;
			}
			*/
			
			if (fail == true)
			{
				String err_msg = "Main function is not in the corect signature";
				throw new SemanticError(method.getLine(), err_msg);
			}
			 
		}
		
		
		method.accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(LibraryMethod method, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		method.accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(Formal formal, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(PrimitiveType type, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(UserType type, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(Assignment assignment, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(CallStatement callStatement, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(Return returnStatement, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(If ifStatement, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(While whileStatement, Object context)
			throws SemanticError {
		/* we are in a while loop - thus the visit of a break statement is valid */
		Statement st = whileStatement.getOperation();
		inside_for_or_while_loop = true;
		st.accept(this, null);
		inside_for_or_while_loop = false;
		return true;
	}

	@Override
	public Boolean visit(Break breakStatement, Object context)
			throws SemanticError {
		if( inside_for_or_while_loop == false)
		{
			String err_msg = "Found a break not inside a loop statement";
			throw new SemanticError(breakStatement.getLine(), err_msg);
		}
		else
		{
			return true;
		}
		
	}

	@Override
	public Boolean visit(Continue continueStatement, Object context)
			throws SemanticError {
		if( inside_for_or_while_loop == false)
		{
			String err_msg = "Found a break not inside a loop statement";
			throw new SemanticError(continueStatement.getLine(), err_msg);
		}
		else
		{
			return true;
		}
	}

	@Override
	public Boolean visit(StatementsBlock statementsBlock, Object context)
			throws SemanticError {
		 List<Statement> stList = statementsBlock.getStatements();
		 for ( Statement st: stList)
		 {
			 st.accept(this, null);
		 }
		return true;
	}

	@Override
	public Boolean visit(LocalVariable localVariable, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(VariableLocation location, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(ArrayLocation location, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(StaticCall call, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(VirtualCall call, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(This thisExpression, Object context)
			throws SemanticError {
		if( inside_virtual_function == false)
		{
			String err_msg = "Found a this not inside a virtual method";
			throw new SemanticError(thisExpression .getLine(), err_msg);
		}
		else
		{
			return true;
		}
	
	}

	@Override
	public Boolean visit(NewClass newClass, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(NewArray newArray, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(Length length, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(MathBinaryOp binaryOp, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(LogicalBinaryOp binaryOp, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(MathUnaryOp unaryOp, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(LogicalUnaryOp unaryOp, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(Literal literal, Object context) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(ExpressionBlock expressionBlock, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

}