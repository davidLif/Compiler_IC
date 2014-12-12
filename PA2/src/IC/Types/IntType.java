package IC.Types;



public class IntType extends Type{

	@Override
	protected String getTypeRep() {
		
		return "int";
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		return this == t;
	}

	
	
}
