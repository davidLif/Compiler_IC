package IC.AST;

import java.util.List;

import IC.SemanticChecks.SemanticError;

/**
 * Static method call AST node.
 * 
 * @author Tovi Almozlino
 */
public class StaticCall extends Call {

	private String className;

	public Object accept(Visitor visitor) throws SemanticError{
		return visitor.visit(this);
	}
	
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}

	/**
	 * Constructs a new static method call node.
	 * 
	 * @param line
	 *            Line number of call.
	 * @param className
	 *            Name of method's class.
	 * @param name
	 *            Name of method.
	 * @param arguments
	 *            List of all method arguments.
	 */
	public StaticCall(int line, String className, String name,
			List<Expression> arguments) {
		super(line, name, arguments);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

}
