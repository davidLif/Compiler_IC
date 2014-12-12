package IC.Types;

public class StringType extends Type{

	@Override
	protected String getTypeRep() {
		return "string";
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		return this == t;
	}

}
