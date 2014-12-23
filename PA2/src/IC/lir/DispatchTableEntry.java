package IC.lir;

public class DispatchTableEntry {

	
	/**
	 * the offset of the entry, same as the index
	 */
	private int offset; 
	
	/**
	 * the name of the method
	 */
	
	private String methodName;
	
	/**
	 * the name of the class that defines this method
	 */
	
	private String className;
	
	public DispatchTableEntry(int offset, String methodName, String className)
	{
		this.offset = offset;
		this.methodName = methodName;
		this.className = className;
	}
	
	public String getMethodName()
	{
		return this.methodName;
	}
	
	public String getClassName()
	{
		return this.className;
	}
	
	
}
