package IC.Types;

import java.util.ArrayList;
import java.util.List;

public class ClassType extends Type{
	
	
	/**
	 * super class type, optional
	 */
	private ClassType superClass  = null; /* direct superclass */
	
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
		
		String rep = this.getTableId()+": Class: " + this.toString();
		if(hasSuper())
		{
			rep += String.format(", Superclass ID: %d", this.superClass.getTableId());
		}
		return rep;
	}
	
	
	

	@Override
	public boolean subTypeOf(Type t) {
		
		if(this == t)
			return true;
		
		
		
		if(this.hasSuper()) {
			
			if( this.superClass == t)
				return true;
		
		if(this.superClass != null && this.superClass.hasSuper())
			{
				/* transitive property */
				return superClass.getSuper().subTypeOf(t);
			}
		}
		

		return false;
	}
	
	/**
	 * this method sets the super class of current class type
	 * @param superClass
	 */
	
	public void setSuperClassType(ClassType superClass)
	{
		this.superClass = superClass;
	}

}
