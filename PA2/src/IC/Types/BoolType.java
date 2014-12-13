package IC.Types;

public class BoolType extends Type{

	@Override
	protected String getTypeRep() {
		return "Primitive type: boolean";
		
	}
	
	@Override
	public String toString()
	{
		return "boolean";
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		return t == this;
	}

}
