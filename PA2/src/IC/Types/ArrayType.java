package IC.Types;

public class ArrayType extends Type{

	/**
	 * the basic type of the array.
	 * for example, basic type of A[][] is A
	 */
	
	private Type basicType;
	
	/**
	 * this holds the number of dimensions of the array
	 */
	private int dimensions = 0;
	
	
	public ArrayType(Type basicType, int dimensions)
	{
		this.basicType = basicType;
		this.dimensions = dimensions;
	}
	
	


	public Type getBasicType() {
		return basicType;
	}


	public int getDimensions() {
		return dimensions;
	}

	/**
	 * returns string representation for array, for example:
	 *      int[][]
	 *      or
	 *      A[]
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.basicType.toString());
		for(int i = 0; i < this.dimensions; ++i)
		{
			sb.append("[]");
		}
		return sb.toString();
	}
	
	@Override
	protected String getTypeRep() {
		
		return this.getTableId()+": Array type: " + this.toString();
	}




	@Override
	public boolean subTypeOf(Type t) {
		
		return this == t;
	}




	
	
	
		
	
	
}
