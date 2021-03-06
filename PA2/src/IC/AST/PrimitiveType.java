package IC.AST;

import IC.DataTypes;
import IC.SemanticChecks.SemanticError;

/**
 * Primitive data type AST node.
 * 
 * @author Tovi Almozlino
 */
public class PrimitiveType extends Type {

	private DataTypes type;

	public Object accept(Visitor visitor) throws SemanticError{
		return visitor.visit(this);
	}
	
	public <D,U> U accept(PropagatingVisitor<D,U> v, D context) throws SemanticError{
		
		return v.visit(this, context);
	}

	/**
	 * Constructs a new primitive data type node.
	 * 
	 * @param line
	 *            Line number of type declaration.
	 * @param type
	 *            Specific primitive data type.
	 */
	public PrimitiveType(int line, DataTypes type) {
		super(line);
		this.type = type;
	}

	public String getName() {
		return type.getDescription();
	}
	
	public DataTypes getDataTypes(){
		return type;
	}
	
	@Override
	public String toString()
	{
		String res = type.getDescription();
		for(int i = 0; i < this.getDimension() ; ++ i)
		{
			res += "[]";
		}
		return res;
	}
}