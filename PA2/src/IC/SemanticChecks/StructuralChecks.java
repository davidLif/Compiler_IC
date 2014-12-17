package IC.SemanticChecks;
import java.util.List;

import IC.AST.*;
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.Symbols.MethodSymbol;

import IC.SemanticChecks.SemanticError;
import IC.Types.ArrayType;
import IC.Types.MethodType;
import IC.Types.StringType;
import IC.Types.Type;
import IC.Types.VoidType;


/**
 * 
 * This class checks the following structural checks:
 * 
 * 		1. exactly one main method is found with the correct signature
 * 		2. break, continue used only inside loops
 * 		3. this keyword used only inside virtual methods
 *
 */

public class StructuralChecks implements  Visitor  {

	
	boolean inside_loop = false;
	boolean inside_static_function = false;
	
	public void check(Program program) throws SemanticError
	{
		 program.accept(this);	

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
		
		List<Statement> stList = method.getStatements();
		
		for (Statement stm: stList)
		{
			/* statements may use this keyword */
			stm.accept(this);
		}
		
		return null;
		
	}
	


	@Override
	public Object visit(StaticMethod method)
			throws SemanticError {

		List<Statement> stList = method.getStatements();
	
		
		inside_static_function = true; /* mark that this cannot appear as an expression inside this method */
		for (Statement stm: stList)
		{
			stm.accept(this);
		}
		inside_static_function = false;
		
		return null;
	}

	@Override
	public Object visit(LibraryMethod method)
			throws SemanticError {
	
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
			
			
		assignment.getAssignment().accept(this);
		
		assignment.getVariable().accept(this); /* also visit the location expression, may contain this*/
		
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement)
			throws SemanticError {
		
		callStatement.getCall().accept(this);
		return null;
	}

	@Override
	public Object visit(Return returnStatement)
			throws SemanticError {
		
		if(returnStatement.hasValue()) /* return value is optional */
			returnStatement.getValue().accept(this);
		return null;
	}

	@Override
	public Object visit(If ifStatement) throws SemanticError {
		
		
		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if(ifStatement.hasElse()) /* else is optional, may contain null */
			ifStatement.getElseOperation().accept(this);

		return null;
	}

	@Override
	public Object visit(While whileStatement)
			throws SemanticError {
			
		/* visit the condition */
		whileStatement.getCondition().accept(this); 
		
		/* we are in a while loop - thus the visit of a break and continue statements is valid */
		Statement st = whileStatement.getOperation();
		
		if(inside_loop == true) /* we are already inside a loop */
		{
			st.accept(this);
		}
		else
		{
			inside_loop = true;
			st.accept(this);
			inside_loop = false;
		}
		
		return null;
	}

	@Override
	public Object visit(Break breakStatement)
			throws SemanticError {
		if( inside_loop == false)
		{
			String err_msg = "break can only be used when inside a while statement";
			throw new SemanticError(breakStatement.getLine(), err_msg);
		}


		return null;
		
	}

	@Override
	public Object visit(Continue continueStatement)
			throws SemanticError {
		
		if( inside_loop == false)
		{
			String err_msg = "break can only be used when inside a while statement";
			throw new SemanticError(continueStatement.getLine(), err_msg);
		}
		
		
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock)
			throws SemanticError {
		 List<Statement> stList = statementsBlock.getStatements();
		 
		 for ( Statement st: stList)
		 {
			 st.accept(this);
		 }
		 
		 return null;
	}

	@Override
	public Object visit(LocalVariable localVariable)
			throws SemanticError {
			
		if(localVariable.hasInitValue())
		{
			// may have initial expression
			localVariable.getInitValue().accept(this);
		}
		return null;
	}

	@Override
	public Object visit(VariableLocation location)
			throws SemanticError {
		
		if(location.isExternal())
		{
			/* may have location: expr.a for example */
			location.getLocation().accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(ArrayLocation location)
			throws SemanticError {
		
		location.getArray().accept(this);
		location.getIndex().accept(this);
		return null;
	}

	@Override
	public Object visit(StaticCall call) throws SemanticError {
		
		 List<Expression> args = call.getArguments();
		 for (Expression arg: args)
		 {
			 arg.accept(this);
		 }
		
		
		return null;
	}

	@Override
	public Object visit(VirtualCall call) throws SemanticError {
		
		 if(call.isExternal())
		 {
			 /* might have location:  expr.func() */
			 call.getLocation().accept(this);
		 }
		
		 List<Expression> arg = call.getArguments();
		 for (Expression a: arg)
		 {
			 a.accept(this);
		 }
		return null;
	}

	@Override
	public Object visit(This thisExpression)
			throws SemanticError {
		
		if( inside_static_function == true)
		{
			String err_msg = "this keyword can only be used inside a virtual method";
			throw new SemanticError(thisExpression .getLine(), err_msg);
		}
		
		
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
		
		newArray.getSize().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Length length) throws SemanticError {
		
		length.getArray().accept(this);
		
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp)
			throws SemanticError {
		
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
	
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp)
			throws SemanticError {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp)
			throws SemanticError {
		
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp)
			throws SemanticError {
		
		unaryOp.getOperand().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Literal literal) throws SemanticError {
		
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock)
			throws SemanticError {
		
		expressionBlock.getExpression().accept(this);
		
		
		return null;
	}

}