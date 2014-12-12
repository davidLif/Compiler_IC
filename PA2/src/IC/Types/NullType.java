package IC.Types;

public class NullType extends Type{

	@Override
	protected String getTypeRep() {
		
		return "null";
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		// subtype of everything
		return true;
	}

	
	
}
