package IC.Types;

public class ClassType extends Type{
	
	
	/**
	 * super class type, optional
	 */
	private ClassType superClass;
	
	/**
	 * name of the class
	 */
	private String className;
	
	
	/**
	 * 
	 * @param className - name of class
	 */
	public ClassType(String className)
	{
		this.className = className;
		this.superClass = null;
	}
	/**
	 * 
	 * @param className - name of class
	 * @param superClass - ClassType of superclass 
	 */
	public ClassType(String className, ClassType superClass)
	{
		
		this(className);
		this.superClass = superClass;
	}
	
	public boolean hasSuper()
	{
		return this.superClass != null;
	}
	
	public ClassType getSuper()
	{
		return this.superClass;
	}
	
	public String getName()
	{
		return this.className;
	}
	
	@Override
	public String toString()
	{
		return this.className;
	}
	
	@Override
	protected String getTypeRep() {
		
		String rep = "Class: " + this.toString();
		if(hasSuper())
		{
			rep += String.format(", Superclass ID: %d", this.superClass.getTableId());
		}
		return rep;
	}
}
