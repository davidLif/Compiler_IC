package IC.AST;

import IC.SemanticChecks.SemanticError;

/**
 * AST node for expression in parentheses.
 * 
 * @author Tovi Almozlino
 */
public class ExpressionBlock extends Expression {

	private Expression expression;

	public Object accept(Visitor visitor)  throws SemanticError{
		return visitor.visit(this);
	}
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}

	/**
	 * Constructs a new expression in parentheses node.
	 * 
	 * @param expression
	 *            The expression.
	 */
	public ExpressionBlock(Expression expression) {
		super(expression.getLine());
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

}
