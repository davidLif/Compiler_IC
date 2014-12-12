package IC.AST;

import java.util.ArrayList;
import java.util.List;

import IC.SemanticChecks.SemanticError;

/**
 * Library method declaration AST node.
 * 
 * @author Tovi Almozlino
 */
public class LibraryMethod extends Method {

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}

	/**
	 * Constructs a new library method declaration node.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 */
	public LibraryMethod(Type type, String name, List<Formal> formals) {
		super(type, name, formals, new ArrayList<Statement>());
	}

	@Override
	public boolean isStatic() {
		
		return true;
	}
	
	
}