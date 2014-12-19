package IC.AST;

import IC.SemanticChecks.SemanticError;
import IC.SymTables.SymbolTable;
import IC.SymTables.GlobalSymbolTable;
import IC.Types.MethodType;

/**
 * Pretty printing visitor - travels along the AST and prints info about each
 * node, in an easy-to-comprehend format.
 * 
 * @author Tovi Almozlino
 */
public class PrettyPrinter implements Visitor {

	private int depth = 0; // depth of indentation

	private String ICFilePath;

	/**
	 * Constructs a new pretty printer visitor.
	 * 
	 * @param ICFilePath
	 *            The path + name of the IC file being compiled.
	 */
	public PrettyPrinter(String ICFilePath) {
		this.ICFilePath = ICFilePath;
	}

	private void indent(StringBuffer output, ASTNode node) {
		output.append("\n");
		for (int i = 0; i < depth*2; ++i)
			output.append(" ");
		if (node != null)
			output.append(node.getLine() + ": ");
	}

	private void indent(StringBuffer output) {
		indent(output, null);
	}
	
	private void print_Type_and_Scope(StringBuffer output,IC.Types.Type node_t,SymbolTable node_scopes){
		output.append(", Type: " + node_t);
		//problem global symbol table id isn't "Global"
		if (node_scopes instanceof GlobalSymbolTable){
			output.append(", Symbol table: " + "Global");
		}
		else {
			output.append(", Symbol table: " + node_scopes.getId());
		}
	}

	public Object visit(Program program) throws SemanticError {
		StringBuffer output = new StringBuffer();
		
		indent(output);
		output.append("Abstract Syntax Tree: " + ICFilePath + "\n");
		for (ICClass icClass : program.getClasses())
			output.append(icClass.accept(this));
		return output.toString();
	}

	public Object visit(ICClass icClass) throws SemanticError {
		StringBuffer output = new StringBuffer();
		
		indent(output, icClass);
		output.append("Declaration of class: " + icClass.getName());
		if (icClass.hasSuperClass())
			output.append(", subclass of " + icClass.getSuperClassName());
		print_Type_and_Scope(output,icClass.getNodeType(),icClass.enclosingScope());
		depth += 2;
		for (Field field : icClass.getFields())
			output.append(field.accept(this));
		for (Method method : icClass.getMethods())
			output.append(method.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(PrimitiveType type) throws SemanticError{
		StringBuffer output = new StringBuffer();

		indent(output, type);
		output.append("Primitive data type: ");
		if (type.getDimension() > 0)
			output.append(type.getDimension() + "-dimensional array of ");
		output.append(type.getName());
		return output.toString();
	}

	public Object visit(UserType type) throws SemanticError{
		StringBuffer output = new StringBuffer();

		indent(output, type);
		output.append("User-defined data type: ");
		if (type.getDimension() > 0)
			output.append(type.getDimension() + "-dimensional array of ");
		output.append(type.getName());
		return output.toString();
	}

	public Object visit(Field field) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, field);
		output.append("Declaration of field: " + field.getName());
		print_Type_and_Scope(output,field.getNodeType(),field.enclosingScope());
		return output.toString();
	}

	public Object visit(LibraryMethod method) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of library method: " + method.getName());
		print_Type_and_Scope(output, method.getNodeType(), method.enclosingScope());
		depth += 2;
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Formal formal) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, formal);
		output.append("Parameter: " + formal.getName());
		print_Type_and_Scope(output, formal.getNodeType(), formal.enclosingScope());
		return output.toString();
	}

	public Object visit(VirtualMethod method) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of virtual method: " + method.getName());
		print_Type_and_Scope(output, method.getNodeType(), method.enclosingScope());
		depth += 2;
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(StaticMethod method) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of static method: " + method.getName());
		print_Type_and_Scope(output, method.getNodeType(), method.enclosingScope());
		depth += 2;
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Assignment assignment) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, assignment);
		output.append("Assignment statement");
		output.append(", Symbol table: " + assignment.enclosingScope().getId());
		depth += 2;
		output.append(assignment.getVariable().accept(this));
		output.append(assignment.getAssignment().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(CallStatement callStatement) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, callStatement);
		output.append("Method call statement");
		print_Type_and_Scope(output, callStatement.getNodeType(), callStatement.enclosingScope());
		++depth;
		output.append(callStatement.getCall().accept(this));
		--depth;
		return output.toString();
	}

	public Object visit(Return returnStatement) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, returnStatement);
		output.append("Return statement");
		if (returnStatement.hasValue())
			output.append(", with return value");
		output.append(", Symbol table: " + returnStatement.enclosingScope().getId());
		if (returnStatement.hasValue()) {
			++depth;
			++depth;
			output.append(returnStatement.getValue().accept(this));
			--depth;
			--depth;
		}
		return output.toString();
	}

	public Object visit(If ifStatement) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, ifStatement);
		output.append("If statement");
		if (ifStatement.hasElse())
			output.append(", with Else operation");
		output.append(", Symbol table: " + ifStatement.enclosingScope().getId());
		depth += 2;
		output.append(ifStatement.getCondition().accept(this));
		output.append(ifStatement.getOperation().accept(this));
		if (ifStatement.hasElse())
			output.append(ifStatement.getElseOperation().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(While whileStatement) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, whileStatement);
		output.append("While statement");
		output.append(", Symbol table: " + whileStatement.enclosingScope().getId());
		depth += 2;
		output.append(whileStatement.getCondition().accept(this));
		output.append(whileStatement.getOperation().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Break breakStatement) {
		StringBuffer output = new StringBuffer();

		indent(output, breakStatement);
		output.append("Break statement");
		output.append(", Symbol table: " + breakStatement.enclosingScope().getId());
		return output.toString();
	}

	public Object visit(Continue continueStatement) throws SemanticError{
		StringBuffer output = new StringBuffer();

		indent(output, continueStatement);
		output.append("Continue statement");
		output.append(", Symbol table: " + continueStatement.enclosingScope().getId());
		return output.toString();
	}

	public Object visit(StatementsBlock statementsBlock) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, statementsBlock);
		output.append("Block of statements");
		output.append(", Symbol table: " + statementsBlock.enclosingScope().getId());
		depth += 2;
		for (Statement statement : statementsBlock.getStatements())
			output.append(statement.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(LocalVariable localVariable) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, localVariable);
		output.append("Declaration of local variable: "
				+ localVariable.getName());
		if (localVariable.hasInitValue()) {
			output.append(", with initial value");
			++depth;
		}
		++depth;
		print_Type_and_Scope(output, localVariable.getNodeType(), localVariable.enclosingScope());
		if (localVariable.hasInitValue()) {
			output.append(localVariable.getInitValue().accept(this));
			--depth;
		}
		--depth;
		return output.toString();
	}

	public Object visit(VariableLocation location) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to variable: " + location.getName());
		if (location.isExternal())
			output.append(", in external scope");
		print_Type_and_Scope(output, location.getNodeType(), location.enclosingScope());
		if (location.isExternal()) {
			++depth;
			++depth;
			output.append(location.getLocation().accept(this));
			--depth;
			--depth;
		}
		return output.toString();
	}

	public Object visit(ArrayLocation location) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to array");
		print_Type_and_Scope(output, location.getNodeType(), location.enclosingScope());
		depth += 2;
		output.append(location.getArray().accept(this));
		output.append(location.getIndex().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(StaticCall call) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, call);
		output.append("Call to static method: " + call.getName()
				+ ", in class " + call.getClassName());
		print_Type_and_Scope(output, call.getNodeType(), call.enclosingScope());
		depth += 2;
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(VirtualCall call) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, call);
		output.append("Call to virtual method: " + call.getName());
		if (call.isExternal())
			output.append(", in external scope");
		print_Type_and_Scope(output,call.getNodeType(), call.enclosingScope());
		depth += 2;
		if (call.isExternal())
			output.append(call.getLocation().accept(this));
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(This thisExpression) throws SemanticError{
		StringBuffer output = new StringBuffer();

		indent(output, thisExpression);
		output.append("Reference to 'this' instance");
		print_Type_and_Scope(output, thisExpression.getNodeType(), thisExpression.enclosingScope());
		return output.toString();
	}

	public Object visit(NewClass newClass) throws SemanticError{
		StringBuffer output = new StringBuffer();

		indent(output, newClass);
		output.append("Instantiation of class: " + newClass.getName());
		print_Type_and_Scope(output, newClass.getNodeType(), newClass.enclosingScope());
		return output.toString();
	}

	public Object visit(NewArray newArray) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, newArray);
		output.append("Array allocation");
		print_Type_and_Scope(output, newArray.getNodeType(), newArray.enclosingScope());
		depth += 2;
		output.append(newArray.getSize().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(Length length) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, length);
		output.append("Reference to array length");
		print_Type_and_Scope(output, length.getNodeType(), length.enclosingScope());
		++depth;
		++depth;
		output.append(length.getArray().accept(this));
		--depth;
		--depth;
		return output.toString();
	}

	public Object visit(MathBinaryOp binaryOp) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, binaryOp);
		output.append("Mathematical binary operation: "
				+ binaryOp.getOperator().getDescription());
		print_Type_and_Scope(output, binaryOp.getNodeType(), binaryOp.enclosingScope());
		depth += 2;
		output.append(binaryOp.getFirstOperand().accept(this));
		output.append(binaryOp.getSecondOperand().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(LogicalBinaryOp binaryOp) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, binaryOp);
		output.append("Logical binary operation: "
				+ binaryOp.getOperator().getDescription());
		print_Type_and_Scope(output, binaryOp.getNodeType(), binaryOp.enclosingScope());
		depth += 2;
		output.append(binaryOp.getFirstOperand().accept(this));
		output.append(binaryOp.getSecondOperand().accept(this));
		depth -= 2;
		return output.toString();
	}

	public Object visit(MathUnaryOp unaryOp) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, unaryOp);
		output.append("Mathematical unary operation: "
				+ unaryOp.getOperator().getDescription());
		print_Type_and_Scope(output, unaryOp.getNodeType(), unaryOp.enclosingScope());
		++depth;
		++depth;
		output.append(unaryOp.getOperand().accept(this));
		--depth;
		--depth;
		return output.toString();
	}

	public Object visit(LogicalUnaryOp unaryOp) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, unaryOp);
		output.append("Logical unary operation: "
				+ unaryOp.getOperator().getDescription());
		print_Type_and_Scope(output, unaryOp.getNodeType(), unaryOp.enclosingScope());
		depth +=2;
		output.append(unaryOp.getOperand().accept(this));
		depth -=2;
		return output.toString();
	}

	public Object visit(Literal literal) throws SemanticError{
		StringBuffer output = new StringBuffer();

		indent(output, literal);
		output.append(literal.getType().getDescription() + ": "
				+ literal.getType().toFormattedString(literal.getValue()));
		print_Type_and_Scope(output, literal.getNodeType(), literal.enclosingScope());
		return output.toString();
	}

	public Object visit(ExpressionBlock expressionBlock) throws SemanticError {
		StringBuffer output = new StringBuffer();

		indent(output, expressionBlock);
		output.append("Parenthesized expression");
		print_Type_and_Scope(output, expressionBlock.getNodeType(), expressionBlock.enclosingScope());
		depth +=2;
		output.append(expressionBlock.getExpression().accept(this));
		depth -=2;
		return output.toString();
	}
}