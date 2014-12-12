package IC.Types;

import IC.DataTypes;


/**
 * 
 *  class represents a primitive type: int, boolean, null, string, void
 */

public class PrimitiveType extends Type {
	
	private DataTypes type;
	
	/**
	 * constructs a primitive type
	 * argument may receive null, to indicate that this is the null type.
	 */
	public PrimitiveType(DataTypes type)
	{
		this.type = type;
	}
	
	
	/**
	 * method returns true if this type represents the null type
	 */
	public boolean isNull()
	{
		return this.type == null;
	}
	
	/** 
	 * returns basic textual type representation
	 * null -> "null"
	 * boolean -> "boolean"
	 * string -> "string"
	 * int -> "int"
	 * void -> "void"
	 */
	@Override 
	public String toString()
	{
		
		if(type == null)
		{
			return "null";
		}
		return this.type.getDescription();
	}


	@Override
	protected String getTypeRep() {
		
		return "Primitive type: " + this.toString();
	}
	
	
	@Override
	public boolean subTypeOf(Type t) {
		//of the primitive types, only NullType can be others sub type
		if (t instanceof NullType){
			return true;
		}
		return false;
	}
	
	
}
