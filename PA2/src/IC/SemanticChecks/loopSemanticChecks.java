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



public class loopSemanticChecks implements  PropagatingVisitor<boolean, Object>  throws SemanticError {


//	boolean inside_for_or_while_loop = false;
	
	public boolean check(Program program) throws SemanticError
	{
		return program.accept(this);	
	}
	
	@Override
	public boolean visit(Program program, Object object) throws SemanticError 
	{	
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
	public boolean visit(ICClass icClass, Object object) throws SemanticError 
	{
		
		List<Method> methods = icClass.getMethods();
		boolean rc;
		for(Method method : methods)
		{
			rc = SymbolTable methodSymTable =  method.accept(this, null);
			if ( rc == false)
				return false;
		}
		
		return true;
	}

	@Override
	public boolean visit(Field field,Object object) throws SemanticError  {
		
		
		return true;
	}
	
	@Override
	public boolean visit(VirtualMethod method, Object object) throws SemanticError {
		
		return true;
	}

	@Override
	public boolean visit(StaticMethod method, Object object) throws SemanticError {
		
		return true;
	}

	@Override
	public boolean visit(LibraryMethod method, Object object) throws SemanticError {
		
		return true;
	}

	@Override
	public boolean visit(Formal formal, Object object){
		
		return true;
	}

	@Override
	public boolean visit(PrimitiveType type,Object object) {
		
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(UserType type, Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(Assignment assignment, Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(CallStatement callStatement, Object object) {
		// nothing to do
		// call statements will be handled in later phases
		return true;
	}

	@Override
	public boolean visit(Return returnStatement,Object object){
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(If ifStatement, Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(While whileStatement, Object object) {
		/* we are in a while loop - thus the visit of a brek statement is valid */
		return true;
	}

	@Override
	public boolean visit(Break breakStatement, Object object){
		/*
		if( inside_for_or_while_loop == false)
		{
			String err_msg = "Found a break not inside a loop statement";
			throw new SemanticError(breakStatement.getLine(), err_msg);
			return false;
		}
		else
		{
			return true;
		}
		*/
		/* we couldn't arrive here from a while loop becuase we cut the recursion there */
		String err_msg = "Found a break not inside a loop statement";
		throw new SemanticError(breakStatement.getLine(), err_msg);
		return false;
	}

	@Override
	public boolean visit(Continue continueStatement, Object object) {
		
		
		/*if( inside_for_or_while_loop == false)
		{
			String err_msg = "Found a break not inside a loop statement";
			throw new SemanticError(continueStatement.getLine(), err_msg);
			return false;
		}
		else
		{
			return true;
		}
		*/
		String err_msg = "Found a break not inside a loop statement";
		throw new SemanticError(continueStatement.getLine(), err_msg);
		return false;
	}

	@Override
	public boolean visit(StatementsBlock statementsBlock, Object object) throws SemanticError {
		
		 List<Statement> stList = statementsBlock.getStatements();
		 for ( Statement st: stList)
		 {
			 st.accept(this, null);
		 }
		
		return true;
	}

	@Override
	public boolean visit(LocalVariable localVariable, Object object) throws SemanticError {
		
		
		return true;
	}

	@Override
	public boolean visit(VariableLocation location, Object object) throws SemanticError {
	
		return true;;
	}

	@Override
	public boolean visit(ArrayLocation location,Object object) {
		
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(StaticCall call, Object object) {
		// calls are handled in later phases
		return true;
	}

	@Override
	public boolean visit(VirtualCall call, Object object) {
		// calls are handled in later phases
		return true;
	}

	@Override
	public boolean visit(This thisExpression,Object object){
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(NewClass newClass, Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(NewArray newArray, Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(Length length, Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(MathBinaryOp binaryOp, Object object){
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(LogicalBinaryOp binaryOp,Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(MathUnaryOp unaryOp, Object object) {
		// nothing to doub
		return true;
	}

	@Override
	public boolean visit(LogicalUnaryOp unaryOp, Object object) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visit(Literal literal, Object object) {
		return true;
	}

	@Override
	public boolean visit(ExpressionBlock expressionBlock,
			Object object)) {
		// nothing to do
		return true;
	}
	
	

}
