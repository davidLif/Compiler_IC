package IC.Types;

public class BoolType extends Type{

	@Override
	protected String getTypeRep() {
		return "boolean";
		
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		return t == this;
	}

}
