package IC.Types;



public class IntType extends Type{

	@Override
	protected String getTypeRep() {
		
		return "Primitive type: int";
	}
	
	@Override
	public String toString()
	{
		return "int";
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		return this == t;
	}

	
	
}
