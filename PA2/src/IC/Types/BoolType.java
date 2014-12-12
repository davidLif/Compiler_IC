package IC.Types;

public class BoolType extends Type{

	@Override
	protected String getTypeRep() {
		return "boolean type";
		
	}

	@Override
	public boolean subTypeOf(Type t) {
		// TODO Auto-generated method stub
		return false;
	}

}
