package IC.AST;

/**
 * Abstract AST node base class.
 * 
 * @author Tovi Almozlino
 */
public abstract class ASTNode {

	private int line;
	private Object enclosingScope;

	/**
	 * Double dispatch method, to allow a visitor to visit a specific subclass.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return A value propagated by the visitor.
	 */
	public abstract Object accept(Visitor visitor);

	/**
	 * Constructs an AST node corresponding to a line number in the original
	 * code. Used by subclasses.
	 * 
	 * @param line
	 *            The line number.
	 */
	protected ASTNode(int line) {
		this.line = line;
	}
	
	/**
	 * 
	 * @return returns the line of the AST node in the program
	 */

	public int getLine() {
		return line;
	}
	
	
	/**
	 * double dispatch method, to allow a visitor to visit a specific subclass.
	 * @param v
	 * @param context
	 * @return
	 */
	public abstract <D,U> U accept(PropagatingVisitor<D,U> v, D context);
	
	
	/* change to SymbolTable */
	public Object enclosingScope()
	{
		return this.enclosingScope;
	}

}
