package IC.AST;

import IC.LiteralTypes;
import IC.SemanticChecks.SemanticError;

/**
 * Literal value AST node.
 * 
 * @author Tovi Almozlino
 */
public class Literal extends Expression {

	private LiteralTypes type;

	private Object value;

	public Object accept(Visitor visitor) throws SemanticError{
		return visitor.visit(this);
	}
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}

	/**
	 * Constructs a new literal node.
	 * 
	 * @param line
	 *            Line number of the literal.
	 * @param type
	 *            Literal type.
	 */
	public Literal(int line, LiteralTypes type) {
		super(line);
		this.type = type;
		value = type.getValue();
	}

	/**
	 * Constructs a new literal node, with a value.
	 * 
	 * @param line
	 *            Line number of the literal.
	 * @param type
	 *            Literal type.
	 * @param value
	 *            Value of literal.
	 */
	public Literal(int line, LiteralTypes type, Object value) {
		this(line, type);
		this.value = value;
	}

	public LiteralTypes getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

}
