package IC.SemanticChecks;
import java.util.List;

import IC.AST.*;
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.MethodSymbolTable;
import IC.SymTables.SymbolTable;
import IC.SymTables.Symbols.ClassSymbol;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.LocalVariableSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.ParameterSymbol;
import IC.SymTables.Symbols.StaticMethodSymbol;
import IC.SymTables.Symbols.VirtualMethodSymbol;
import IC.SemanticChecks.SemanticError;
import IC.Types.ArrayType;
import IC.Types.MethodType;
import IC.Types.StringType;
import IC.Types.Type;
import IC.Types.VoidType;



public class SemanticChecksModul implements  PropagatingVisitor<Object, Boolean>  {

	
	boolean inside_loop = false;
	boolean inside_static_function = false;
	boolean found_main = false;
	
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
		
		
		 List<Statement> stList = method.getStatements();
		
		/* checking that it's not a main function */
		if ( method.getName().equals("main") && found_main == true) /* DENIS is that necassetry? */
		{
			
			String err_msg = "Found more than one main functions";
			throw new SemanticError(method.getLine(), err_msg);
		}
		
		for (Statement stm: stList)
		{
			stm.accept(this,null);
		}
		
		return true;
	}
	


	@Override
	public Boolean visit(StaticMethod method, Object context)
			throws SemanticError {

		String methodName = method.getName();
		List<Statement> stList = method.getStatements();
		
		if ( methodName.equals("Main"))
		{
			/* checking that it's in a correct form of a main function */
			boolean fail = false;
			ClassSymbolTable scope = (ClassSymbolTable) method.enclosingScope();  // ??
			MethodSymbol SymbolTable =  scope.getMethod("Main", true );
			MethodType methodType = (MethodType) SymbolTable.getType();
			List<Type> arguments = methodType.getArguments();
			
			
			if(found_main == true) /* we have already seen a main function */
			{
				fail = true;
			}
			if ( ! (methodType.getReturnType() instanceof VoidType) ) /* the return type is not a void */
			{
				fail = true;
			}
		
			if( !( (arguments.size() == 1) && (arguments.get(0) instanceof ArrayType) /* params are string [] */
					&&   ((ArrayType) arguments.get(0)).getBasicType() instanceof StringType   
					&&    ((ArrayType) arguments.get(0)).getDimensions() != 1 ))
			{
				fail = true;
			}
			
			if (fail == true)
			{
				String err_msg = "Main function is not in the corect signature";
				throw new SemanticError(method.getLine(), err_msg);
			}
			found_main = true;
			 
		}
		
		inside_static_function = true;
		for (Statement stm: stList)
		{
			stm.accept(this,null);
		}
		inside_static_function = false;
		return true;
	}

	@Override
	public Boolean visit(LibraryMethod method, Object context)
			throws SemanticError {
		method.accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(Formal formal, Object context) throws SemanticError {
		return true;
	}

	@Override
	public Boolean visit(PrimitiveType type, Object context)
			throws SemanticError {
		return true;
	}

	@Override
	public Boolean visit(UserType type, Object context) throws SemanticError {
		return true;
	}

	@Override
	public Boolean visit(Assignment assignment, Object context)
			throws SemanticError {
		assignment.getAssignment().accept(this,null); 
		return true;
	}

	@Override
	public Boolean visit(CallStatement callStatement, Object context)
			throws SemanticError {
		callStatement.getCall().accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(Return returnStatement, Object context)
			throws SemanticError {
		returnStatement.getValue().accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(If ifStatement, Object context) throws SemanticError {
		
		
		ifStatement.getCondition().accept(this,null);
		ifStatement.getOperation().accept(this,null);
		ifStatement.getElseOperation().accept(this,null);

		return true;
	}

	@Override
	public Boolean visit(While whileStatement, Object context)
			throws SemanticError {
		/* we are in a while loop - thus the visit of a break statement is valid */
		Statement st = whileStatement.getOperation();
		
		if(inside_loop = true) /* we are already inside a loop */
		{
			st.accept(this, null);
			return true;
		}
		else
		{
			inside_loop = true;
			st.accept(this, null);
			inside_loop = false;
			return true;
		}
	}

	@Override
	public Boolean visit(Break breakStatement, Object context)
			throws SemanticError {
		if( inside_loop == false)
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
		if( inside_loop == false)
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
		return true;
	}

	@Override
	public Boolean visit(VariableLocation location, Object context)
			throws SemanticError {
		return true;
	}

	@Override
	public Boolean visit(ArrayLocation location, Object context)
			throws SemanticError {
		location.getArray().accept(this, null);
		location.getIndex().accept(this, null);
		return true;
	}

	@Override
	public Boolean visit(StaticCall call, Object context) throws SemanticError {
		 List<Expression> arg = call.getArguments();
		 for (Expression a: arg)
		 {
			 a.accept(this, null);
		 }
		
		
		return true;
	}

	@Override
	public Boolean visit(VirtualCall call, Object context) throws SemanticError {
		 List<Expression> arg = call.getArguments();
		 for (Expression a: arg)
		 {
			 a.accept(this, null);
		 }
		return true;
	}

	@Override
	public Boolean visit(This thisExpression, Object context)
			throws SemanticError {
		if( inside_static_function == true)
		{
			String err_msg = "Found this in use not in an instance method";
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

		return true;
	}

	@Override
	public Boolean visit(NewArray newArray, Object context)
			throws SemanticError {
		newArray.getSize().accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(Length length, Object context) throws SemanticError {
		length.getArray().accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(MathBinaryOp binaryOp, Object context)
			throws SemanticError {
		
		binaryOp.getFirstOperand().accept(this,null);
		binaryOp.getSecondOperand().accept(this,null);
	
		return true;
	}

	@Override
	public Boolean visit(LogicalBinaryOp binaryOp, Object context)
			throws SemanticError {
		binaryOp.getFirstOperand().accept(this,null);
		binaryOp.getSecondOperand().accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(MathUnaryOp unaryOp, Object context)
			throws SemanticError {
		unaryOp.getOperand().accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(LogicalUnaryOp unaryOp, Object context)
			throws SemanticError {
		unaryOp.getOperand().accept(this,null);
		return true;
	}

	@Override
	public Boolean visit(Literal literal, Object context) throws SemanticError {
		return true;
	}

	@Override
	public Boolean visit(ExpressionBlock expressionBlock, Object context)
			throws SemanticError {
		expressionBlock.getExpression().accept(this, null);
		
		
		return true;
	}

}