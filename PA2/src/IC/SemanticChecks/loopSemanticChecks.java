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



public class loopSemanticChecks implements  PropagatingVisitor<Object, Boolean>  {

	
	
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(StaticMethod method, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean visit(LibraryMethod method, Object context)
			throws SemanticError {
		// TODO Auto-generated method stub
		return null;
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
		/* we are in a while loop - thus the visit of a brek statement is valid */
		return true;
	}

	@Override
	public Boolean visit(Break breakStatement, Object context)
			throws SemanticError {
		/* we couldn't arrive here from a while loop becuase we cut the recursion there */
		String err_msg = "Found a break not inside a loop statement";
		throw new SemanticError(breakStatement.getLine(), err_msg);
		
	}

	@Override
	public Boolean visit(Continue continueStatement, Object context)
			throws SemanticError {
	
		String err_msg = "Found a break not inside a loop statement";
		throw new SemanticError(continueStatement.getLine(), err_msg);
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
		// TODO Auto-generated method stub
		return null;
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