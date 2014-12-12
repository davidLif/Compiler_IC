package IC.Types;

public class NullType extends Type{

	@Override
	protected String getTypeRep() {
		
		return "null";
	}

	@Override
	public boolean subTypeOf(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

	
	
}
