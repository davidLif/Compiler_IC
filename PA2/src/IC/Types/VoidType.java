package IC.Types;

public class VoidType extends Type{

	@Override
	public boolean subTypeOf(Type t) {
		if (this == t) return true;//void is sub type of void
		return false;
	}

	@Override
	protected String getTypeRep() {
		
		return "Primitive type: void";
	}
	
	@Override
	public String toString()
	{
		return "void";
	}

}
