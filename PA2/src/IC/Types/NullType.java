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
		
		// subtype of all reference types (array type, string type, class type)
		
		if(t instanceof ClassType || t instanceof ArrayType || t instanceof StringType)
			return true;
		
		return false;
	}

	
	
}
