package IC.AST;

import IC.SemanticChecks.SemanticError;
import IC.SymTables.SymbolTable;


/**
 * Abstract AST node base class.
 * 
 * @author Tovi Almozlino
 */
public abstract class ASTNode {

	private int line;
	private SymbolTable enclosingScope;
	private IC.Types.Type type;

	/**
	 * Double dispatch method, to allow a visitor to visit a specific subclass.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return A value propagated by the visitor.
	 * @throws SemanticError 
	 */
	public abstract Object accept(Visitor visitor) throws SemanticError;

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
	public abstract <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError;
	
	
	/**
	 * 
	 * @return enclosing scope of current ast node
	 */
	public SymbolTable enclosingScope()
	{
		return this.enclosingScope;
	}
	
	public void setEnclosingScope(SymbolTable scope)
	{
		this.enclosingScope = scope;
	}
	
	/**
	 * this method sets the type of the AST node
	 * @param type - type of node
	 */
	public void setNodeType(IC.Types.Type type)
	{
		this.type = type;
	}
	
	public IC.Types.Type getNodeType()
	{
		return this.type;
	}

}
