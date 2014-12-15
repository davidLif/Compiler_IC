package IC.Types;

public class NullType extends Type{

	@Override
	protected String getTypeRep() {
		
		return this.getTableId()+": Primitive type: null";
	}

	@Override
	public String toString()
	{
		return "null";
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		// subtype of everything
		return true;
	}

	
	
}
