package IC.AST;

import IC.SemanticChecks.SemanticError;

public interface PropagatingVisitor<D, U> {

	public U visit(Program program, D context) throws SemanticError;

	public U visit(ICClass icClass, D context) throws SemanticError;

	public U visit(Field field, D context)     throws SemanticError;

	public U visit(VirtualMethod method, D context)  throws SemanticError;

	public U visit(StaticMethod method, D context)   throws SemanticError;

	public U visit(LibraryMethod method, D context)  throws SemanticError;

	public U visit(Formal formal, D context)         throws SemanticError;

	public U visit(PrimitiveType type, D context)    throws SemanticError;

	public U visit(UserType type, D context)          throws SemanticError;

	public U visit(Assignment assignment, D context)    throws SemanticError;

	public U visit(CallStatement callStatement, D context) throws SemanticError;

	public U visit(Return returnStatement, D context) throws SemanticError;

	public U visit(If ifStatement, D context) throws SemanticError;

	public U visit(While whileStatement, D context) throws SemanticError;

	public U visit(Break breakStatement, D context) throws SemanticError;

	public U visit(Continue continueStatement, D context) throws SemanticError;

	public U visit(StatementsBlock statementsBlock, D context) throws SemanticError;

	public U visit(LocalVariable localVariable, D context) throws SemanticError;

	public U visit(VariableLocation location, D context) throws SemanticError;

	public U visit(ArrayLocation location, D context) throws SemanticError;

	public U visit(StaticCall call, D context) throws SemanticError;

	public U visit(VirtualCall call, D context) throws SemanticError;

	public U visit(This thisExpression, D context) throws SemanticError;

	public U visit(NewClass newClass, D context) throws SemanticError;

	public U visit(NewArray newArray, D context) throws SemanticError;

	public U visit(Length length, D context) throws SemanticError;

	public U visit(MathBinaryOp binaryOp, D context) throws SemanticError;

	public U visit(LogicalBinaryOp binaryOp, D context) throws SemanticError;

	public U visit(MathUnaryOp unaryOp, D context) throws SemanticError;

	public U visit(LogicalUnaryOp unaryOp, D context) throws SemanticError;

	public U visit(Literal literal, D context) throws SemanticError;

	public U visit(ExpressionBlock expressionBlock, D context) throws SemanticError;
}
