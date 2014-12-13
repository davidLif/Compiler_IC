package IC.AST;

import IC.BinaryOps;
import IC.SemanticChecks.SemanticError;

/**
 * Logical binary operation AST node.
 * 
 * @author Tovi Almozlino
 */
public class LogicalBinaryOp extends BinaryOp {

	public Object accept(Visitor visitor) throws SemanticError {
		return visitor.visit(this);
	}
	
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}

	/**
	 * Constructs a new logical binary operation node.
	 * 
	 * @param operand1
	 *            The first operand.
	 * @param operator
	 *            The operator.
	 * @param operand2
	 *            The second operand.
	 */
	public LogicalBinaryOp(Expression operand1, BinaryOps operator,
			Expression operand2) {
		super(operand1, operator, operand2);
	}

}
