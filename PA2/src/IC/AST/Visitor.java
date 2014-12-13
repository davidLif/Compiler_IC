package IC.AST;

import IC.SemanticChecks.SemanticError;

/**
 * AST visitor interface. Declares methods for visiting each type of AST node.
 * 
 * @author Tovi Almozlino
 */
public interface Visitor {

	public Object visit(Program program) throws SemanticError;

	public Object visit(ICClass icClass) throws SemanticError;

	public Object visit(Field field) throws SemanticError;

	public Object visit(VirtualMethod method) throws SemanticError;

	public Object visit(StaticMethod method) throws SemanticError;

	public Object visit(LibraryMethod method) throws SemanticError;

	public Object visit(Formal formal) throws SemanticError;

	public Object visit(PrimitiveType type) throws SemanticError;

	public Object visit(UserType type) throws SemanticError;

	public Object visit(Assignment assignment) throws SemanticError;

	public Object visit(CallStatement callStatement) throws SemanticError;

	public Object visit(Return returnStatement) throws SemanticError;

	public Object visit(If ifStatement) throws SemanticError;

	public Object visit(While whileStatement) throws SemanticError;

	public Object visit(Break breakStatement) throws SemanticError;

	public Object visit(Continue continueStatement) throws SemanticError;

	public Object visit(StatementsBlock statementsBlock) throws SemanticError;

	public Object visit(LocalVariable localVariable) throws SemanticError;

	public Object visit(VariableLocation location) throws SemanticError;

	public Object visit(ArrayLocation location) throws SemanticError;

	public Object visit(StaticCall call) throws SemanticError;

	public Object visit(VirtualCall call) throws SemanticError;

	public Object visit(This thisExpression) throws SemanticError;

	public Object visit(NewClass newClass) throws SemanticError;

	public Object visit(NewArray newArray) throws SemanticError;

	public Object visit(Length length) throws SemanticError;

	public Object visit(MathBinaryOp binaryOp) throws SemanticError;

	public Object visit(LogicalBinaryOp binaryOp) throws SemanticError;

	public Object visit(MathUnaryOp unaryOp) throws SemanticError;

	public Object visit(LogicalUnaryOp unaryOp) throws SemanticError;

	public Object visit(Literal literal) throws SemanticError;

	public Object visit(ExpressionBlock expressionBlock) throws SemanticError;
}
