package IC.AST;

import IC.SemanticChecks.SemanticError;

/**
 * Break statement AST node.
 * 
 * @author Tovi Almozlino
 */
public class Break extends Statement {

	public Object accept(Visitor visitor) throws SemanticError{
		return visitor.visit(this);
	}
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}

	/**
	 * Constructs a break statement node.
	 * 
	 * @param line
	 *            Line number of break statement.
	 */
	public Break(int line) {
		super(line);
	}

}
