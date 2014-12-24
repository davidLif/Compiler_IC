package IC.lir.lirAST;

public class Immediate extends LirNode{

	
	/**
	 * 32 bit (signed) value
	 */
	int value;
	
	public Immediate(int value)
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return this.value;
	}

	@Override
	public String emit() {
		
		return ((Integer)value).toString();
	}
}
