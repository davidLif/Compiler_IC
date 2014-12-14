package IC.AST;

import IC.SemanticChecks.SemanticError;
import IC.SymTables.*;
import IC.SymTables.Symbols.Symbol;

/**
 * Variable reference AST node.
 * 
 * @author Tovi Almozlino
 */
public class VariableLocation extends Location {

	private Expression location = null;
	
	
	/**
	 * this field saves a reference to the defining scope of this variable, in case it is not external
	 * this solves the problem of later definition in enclosing scope, that was added after the variable 
	 * was resolved. this scope is the scope that resolved the variable
	 * note that, it doesn't have to be this.enclosingScope;
	 */
	private SymbolTable definingScope = null; 
	

	private String name;

	public Object accept(Visitor visitor) throws SemanticError{
		return visitor.visit(this);
	}

	/**
	 * Constructs a new variable reference node.
	 * 
	 * @param line
	 *            Line number of reference.
	 * @param name
	 *            Name of variable.
	 */
	public VariableLocation(int line, String name) {
		super(line);
		this.name = name;
	}

	/**
	 * Constructs a new variable reference node, for an external location.
	 * 
	 * @param line
	 *            Line number of reference.
	 * @param location
	 *            Location of variable.
	 * @param name
	 *            Name of variable.
	 */
	public VariableLocation(int line, Expression location, String name) {
		this(line, name);
		this.location = location;
	}

	public boolean isExternal() {
		return (location != null);
	}

	public Expression getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}
	
	
	/**
	 *  set the scope in which this local variable was defined (NOT EXTERNAL)
	 */
	public void setDefiningScope(SymbolTable scope)
	{
		this.setDefiningScope(scope);
	}
	
	public SymbolTable getDefiningScope()
	{
		return this.definingScope;
	}
	
	/**
	 * this method retrieves the definition symbol of the variable
	 * @return
	 */
	
	public Symbol getDefiningSymbol()
	{
		if(this.definingScope instanceof ClassSymbolTable)
		{
			return ((ClassSymbolTable) definingScope).getField(this.name);
		}
		
		return ((VariableSymbolTable) definingScope).getVariableLocally(this.name);
	}

}
