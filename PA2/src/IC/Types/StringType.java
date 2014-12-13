package IC.Types;

public class StringType extends Type{

	@Override
	protected String getTypeRep() {
		return "Primitive type: string";
	}
	
	
	@Override
	public String toString()
	{
		return "string";
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		return this == t;
	}

}
