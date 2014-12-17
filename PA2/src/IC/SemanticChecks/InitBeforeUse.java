package IC.SemanticChecks;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class InitBeforeUse implements PropagatingVisitor<Set<LocalVariableSymbol>, Set<LocalVariableSymbol>>{

	private Program progRoot;
	
	
	
	public InitBeforeUse(Program program)
	{
		this.progRoot = program;
	}
	
	/**
	 * call this method to run this test
	 * 
	 * @throws SemanticError if error found
	 */
	
	public void check() throws SemanticError
	{
		this.progRoot.accept(this, new HashSet<LocalVariableSymbol>());
	}
	
	
	/**
	 * NOTE:
	 * definedSymbols are a set of LocalVariableSymbols that we know for sure were initialized up until a certain point in the program
	 * the test is conservative, we're taking into account the worst case possible
	 * 
	 */
	
	@Override
	public Set<LocalVariableSymbol> visit(Program program, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		for(ICClass iClass : program.getClasses())
		{
			iClass.accept(this, definedSymbols);
		}
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(ICClass icClass, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		/* visit all the methods */
		for(Method method : icClass.getMethods())
		{
			/* clear the set for the new method */
			definedSymbols.clear();
			method.accept(this, definedSymbols);
		}
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Field field, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		/* wont reach this part */
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(VirtualMethod method, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		/* visit all the statements in the method */
		for(Statement stmt : method.getStatements())
		{
			stmt.accept(this, definedSymbols);
		}
		return null;
	}

	@Override
	public  Set<LocalVariableSymbol> visit(StaticMethod method, Set<LocalVariableSymbol> definedSymbols ) throws SemanticError {
		/* visit all the statements in the method */
		for(Statement stmt : method.getStatements())
		{
			stmt.accept(this, definedSymbols);
		}
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(LibraryMethod method, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		/* nothing to do */
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Formal formal, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		/* wont reach this part */
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(PrimitiveType type, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {

		/* nothing to do, no sub expressions */
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(UserType type, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		/* nothing to do, no sub expressions */
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Assignment assignment, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		Expression rhs = assignment.getAssignment();
		
		/* rhs is evaluated first */
		rhs.accept(this, definedSymbols);
		
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
				definedSymbols.add(localVarSym);
				
			}
		}
		else
		{
			// location is not a variable, visit it, may contain variable uses
			loc.accept(this, definedSymbols);
		}
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(CallStatement callStatement, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {

		callStatement.getCall().accept(this, definedSymbols);
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Return returnStatement, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		if(returnStatement.hasValue())
			returnStatement.getValue().accept(this, definedSymbols);
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(If ifStatement,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		ifStatement.getCondition().accept(this, definedSymbols);
		
		/* make a copy of all known definition at this point */
		Set<LocalVariableSymbol> temp = new HashSet<LocalVariableSymbol>(definedSymbols);
		
		ifStatement.getOperation().accept(this, definedSymbols);
		
		/* definedSymbols now contains symbols that were defined in the if statement */
		
		
		if( ifStatement.hasElse())
		{
			
			ifStatement.getElseOperation().accept(this, temp);
			
			/* temp now contains also what was defined in the else statement */
			/* intersent the defintions */
			definedSymbols.retainAll(temp);
			
		}
		else
		{
			/* no else, if might not be taken so all new definitions inside if branch need to be discarded */
			definedSymbols.clear();
			definedSymbols.addAll(temp);
		}
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(While whileStatement, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		whileStatement.getCondition().accept(this, definedSymbols);
		/* make a copy of all known definition at this point */
		Set<LocalVariableSymbol> temp = new HashSet<LocalVariableSymbol>(definedSymbols);
		
		whileStatement.getOperation().accept(this, definedSymbols);
		
		/* while may not be taken */
		definedSymbols.clear();
		definedSymbols.addAll(temp);
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Break breakStatement, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		// nothing to do
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Continue continueStatement, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		// nothing to do
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(StatementsBlock statementsBlock, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		for(Statement stmt : statementsBlock.getStatements())
		{
			stmt.accept(this, definedSymbols);
		}
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(LocalVariable localVariable,Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		/* in this case, we're visiting a localVariable
		 * we may be initialized it now 
		 */
		
		if(localVariable.hasInitValue())
		{
			/* first rhs first */
			localVariable.getInitValue().accept(this, definedSymbols);
			
			/* above check succeeded, mark variable as initialized */
			/* get enclosing scope */
			VariableSymbolTable scope = (VariableSymbolTable) localVariable.enclosingScope();
			
			/* find the variable symbol */
			LocalVariableSymbol localVarSym = (LocalVariableSymbol) scope.getVariableLocally(localVariable.getName());
			
			definedSymbols.add(localVarSym);
			
			
		}
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(VariableLocation location, Set<LocalVariableSymbol> definedSymbols) throws SemanticError {

		/* in this case we're using a local variable, we must check that it was initialized before use */
		/* if it refers to a field or method param, ignore, it was already set */
		
		if(location.isExternal())
		{
			// refers to a field, automatically set on the heap
			// only need to visit the expression
			location.getLocation().accept(this, definedSymbols);
			return null;
		}
		
		// not external, but check if really refers to a local variable
		if(location.getDefiningSymbol().getKind() == SymbolKind.LOCALVAR)
		{
			/* check if was initailized */
			LocalVariableSymbol locVarSym = (LocalVariableSymbol) location.getDefiningSymbol();
			if(!definedSymbols.contains(locVarSym))
			{
				String err = String.format("local variable %s may be used before initialized", location.getName());
				throw new SemanticError(location.getLine(), err);
			}
		}
		
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(ArrayLocation location,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		location.getArray().accept(this, definedSymbols);
		location.getIndex().accept(this, definedSymbols);
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(StaticCall call,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		
		List<Expression> args = call.getArguments();
		/* visit each argument */
		for(Expression arg : args)
		{
			arg.accept(this, definedSymbols);
		}
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(VirtualCall call,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {

		
		if(call.isExternal())
			call.getLocation().accept(this, definedSymbols);
		
	
		
		List<Expression> args = call.getArguments();
		/* visit each argument */
		for(Expression arg : args)
		{
			arg.accept(this, definedSymbols);
		}
		
		
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(This thisExpression,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		// nothing to do here
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(NewClass newClass,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		// nothing to do here, type cannot affect
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(NewArray newArray,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		newArray.getSize().accept(this, definedSymbols);
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Length length,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		length.getArray().accept(this, definedSymbols);
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(MathBinaryOp binaryOp,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		binaryOp.getFirstOperand().accept(this, definedSymbols);
		binaryOp.getSecondOperand().accept(this, definedSymbols);
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(LogicalBinaryOp binaryOp,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		binaryOp.getFirstOperand().accept(this, definedSymbols);
		binaryOp.getSecondOperand().accept(this, definedSymbols);
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(MathUnaryOp unaryOp,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		unaryOp.getOperand().accept(this, definedSymbols);
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(LogicalUnaryOp unaryOp,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		unaryOp.getOperand().accept(this, definedSymbols);
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(Literal literal,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		// nothing to do
		return null;
	}

	@Override
	public Set<LocalVariableSymbol> visit(ExpressionBlock expressionBlock,  Set<LocalVariableSymbol> definedSymbols) throws SemanticError {
		expressionBlock.getExpression().accept(this, definedSymbols);
		return null;
	}



}
