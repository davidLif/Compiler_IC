package IC.SemanticChecks;
import java.util.HashSet;
import java.util.List;

import IC.AST.*;
import IC.SymTables.VariableSymbolTable;
import IC.SymTables.Symbols.LocalVariableSymbol;
import IC.SymTables.Symbols.Symbol;
import IC.SymTables.Symbols.SymbolKind;


/**
 * 
 * @author Denis
 *
 *
 * This class implements the following check:
 * local variables must be initialized before use
 * note that method parameters and fields are automatically initialized
 * (fields on the heap, method parameters receive their values from method call )
 *
 */

public class InitBeforeUse implements Visitor{

	private Program progRoot;
	
	/**
	 * this set will store all the initialized local variables
	 * in current method
	 * 
	 */
	private HashSet<LocalVariableSymbol> initializedSet;
	
	public InitBeforeUse(Program program)
	{
		this.progRoot = program;
		this.initializedSet = new HashSet<LocalVariableSymbol>();
	}
	
	/**
	 * call this method to run this test
	 * 
	 * @throws SemanticError if error found
	 */
	
	public void check() throws SemanticError
	{
		this.progRoot.accept(this);
	}
	
	
	@Override
	public Object visit(Program program) throws SemanticError {
		
		for(ICClass iClass : program.getClasses())
		{
			iClass.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(ICClass icClass) throws SemanticError {
		
		/* visit all the methods */
		for(Method method : icClass.getMethods())
		{
			/* clear the set for the new method */
			initializedSet.clear();
			method.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(Field field) throws SemanticError {
		/* wont reach this part */
		
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) throws SemanticError {
		
		/* visit all the statements in the method */
		for(Statement stmt : method.getStatements())
		{
			stmt.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(StaticMethod method) throws SemanticError {
		/* visit all the statements in the method */
		for(Statement stmt : method.getStatements())
		{
			stmt.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) throws SemanticError {
		/* nothing to do */
		return null;
	}

	@Override
	public Object visit(Formal formal) throws SemanticError {
		
		/* wont reach this part */
		
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) throws SemanticError {

		/* nothing to do, no sub expressions */
		return null;
	}

	@Override
	public Object visit(UserType type) throws SemanticError {
		/* nothing to do, no sub expressions */
		return null;
	}

	@Override
	public Object visit(Assignment assignment) throws SemanticError {
		
		Expression rhs = assignment.getAssignment();
		
		/* rhs is evaluated first */
		rhs.accept(this);
		
		/* lhs */
		
		Location loc = assignment.getVariable();
		if(loc instanceof VariableLocation)
		{
			/* we're setting the value of a variable location */
			VariableLocation varLoc = (VariableLocation)loc;
			Symbol definingSymbol = varLoc.getDefiningSymbol();
			
			/* we must check that the defining symbol is really a local variable */
			if(definingSymbol.getKind() == SymbolKind.LOCALVAR)
			{
				LocalVariableSymbol localVarSym = (LocalVariableSymbol)definingSymbol;
				/* mark local variable as initialized */
				this.initializedSet.add(localVarSym);
				
			}
		}
		else
		{
			// location is not a variable, visit it, may contain variable uses
			loc.accept(this);
		}
		
		
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) throws SemanticError {

		callStatement.getCall().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Return returnStatement) throws SemanticError {
		
		if(returnStatement.hasValue())
			returnStatement.getValue().accept(this);
		
		return null;
	}

	@Override
	public Object visit(If ifStatement) throws SemanticError {
		
		ifStatement.getCondition().accept(this);
		if( ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);
		
		return null;
	}

	@Override
	public Object visit(While whileStatement) throws SemanticError {
		
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Break breakStatement) throws SemanticError {
		// nothing to do
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) throws SemanticError {
		// nothing to do
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) throws SemanticError {
		
		for(Statement stmt : statementsBlock.getStatements())
		{
			stmt.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) throws SemanticError {
		
		/* in this case, we're visiting a localVariable
		 * we may be initialized it now 
		 */
		
		if(localVariable.hasInitValue())
		{
			/* first rhs first */
			localVariable.getInitValue().accept(this);
			
			/* above check succeeded, mark variable as initialized */
			/* get enclosing scope */
			VariableSymbolTable scope = (VariableSymbolTable) localVariable.enclosingScope();
			
			/* find the variable symbol */
			LocalVariableSymbol localVarSym = (LocalVariableSymbol) scope.getVariableLocally(localVariable.getName());
			
			this.initializedSet.add(localVarSym);
			
			
		}
		
		return null;
	}

	@Override
	public Object visit(VariableLocation location) throws SemanticError {

		/* in this case we're using a local variable, we must check that it was initialized before use */
		/* if it refers to a field or method param, ignore, it was already set */
		
		if(location.isExternal())
		{
			// refers to a field, automatically set on the heap
			// only need to visit the expression
			location.getLocation().accept(this);
			return null;
		}
		
		// not external, but check if really refers to a local variable
		if(location.getDefiningSymbol().getKind() == SymbolKind.LOCALVAR)
		{
			/* check if was initailized */
			LocalVariableSymbol locVarSym = (LocalVariableSymbol) location.getDefiningSymbol();
			if(!this.initializedSet.contains(locVarSym))
			{
				String err = String.format("local variable %s used before was initialized", location.getName());
				throw new SemanticError(location.getLine(), err);
			}
		}
		
		
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) throws SemanticError {
		
		location.getArray().accept(this);
		location.getIndex().accept(this);
		
		return null;
	}

	@Override
	public Object visit(StaticCall call) throws SemanticError {
		
		List<Expression> args = call.getArguments();
		/* visit each argument */
		for(Expression arg : args)
		{
			arg.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(VirtualCall call) throws SemanticError {

		
		if(call.isExternal())
			call.getLocation().accept(this);
		
	
		
		List<Expression> args = call.getArguments();
		/* visit each argument */
		for(Expression arg : args)
		{
			arg.accept(this);
		}
		
		
		return null;
	}

	@Override
	public Object visit(This thisExpression) throws SemanticError {
		// nothing to do here
		return null;
	}

	@Override
	public Object visit(NewClass newClass) throws SemanticError {
		// nothing to do here, type cannot affect
		return null;
	}

	@Override
	public Object visit(NewArray newArray) throws SemanticError {
		newArray.getSize().accept(this);
		return null;
	}

	@Override
	public Object visit(Length length) throws SemanticError {
		length.getArray().accept(this);
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) throws SemanticError {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) throws SemanticError {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) throws SemanticError {
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) throws SemanticError {
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(Literal literal) throws SemanticError {
		// nothing to do
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) throws SemanticError {
		expressionBlock.getExpression().accept(this);
		return null;
	}

}
