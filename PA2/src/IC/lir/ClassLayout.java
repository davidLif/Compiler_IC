package IC.lir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import IC.AST.Field;
import IC.AST.ICClass;
import IC.AST.Method;


public class ClassLayout {

	
	private String className;
	
	/**
	 * this map maps a given virtual class name to its offset in the dispatch table
	 */
	
	private Map<String, Integer> methodToOffset = new HashMap<String, Integer>();
	
	
	/**
	 * this map maps a given method's offset in the dispatch table, to the class name that defines the method
	 * usage: we know that class B extends class A and overrides method f, thus, we check f's offset (using the map above)
	 * and update the  class name that contains method f (by using this map)
	 */
	
	private Map<Integer, String> methodOffsetToClassName = new HashMap<Integer, String>(); 
	
	
	/**
	 *  this map maps a given field name to its offset in the class object
	 */
	private Map<String, Integer> fieldToOffset = new HashMap<String, Integer>();
	
	
	private int currentMaxMethodOffset = 0;
	private int currentMaxFieldOffset = 0;
	
	public ClassLayout(ICClass classNode)
	{
		
		this.className = classNode.getName();
		/* fill our data structures */
		addAllVirtualSymbols(classNode);
	}
	
	
	public ClassLayout(ICClass classNode, ClassLayout parentClass)
	{
		
		this.className = classNode.getName();
		
		// add all method to offsets mapping (super class prefix)
		this.methodToOffset.putAll(parentClass.methodToOffset);
		this.currentMaxMethodOffset = parentClass.currentMaxFieldOffset;
		
		// add all field to offsets mapping (super class prefix)
		this.fieldToOffset.putAll(parentClass.fieldToOffset);
		this.currentMaxFieldOffset = parentClass.currentMaxFieldOffset;
		
		/* add our classes and fields, possible override */
		addAllVirtualSymbols(classNode);
		
	}
	
	
	private void addAllVirtualSymbols(ICClass classNode)
	{
		// add all virtual methods
		// we want to insert them in the same order they appeared in the class
		for(Method method : classNode.getMethods())
		{
			if(!method.isStatic())
			{
				this.addMethod(method.getName(), this.className);
			}
		}
		// add all the fields
		for(Field field : classNode.getFields())
		{
			this.addField(field.getName());
		}
	}
	
	
	/**
	 * this method receives a method, and adds it to the class layout
	 * if such a method already exists, it will override it (keeping the same offset, but updating the defining class)
	 * @param name      - name of method
	 * @param className - name of class that contains the method
	 */
	
	private void addMethod(String name, String className)
	{
		
		if(methodToOffset.containsKey(name))
		{
			/* get method offset inside dispatch table */
			int methodOffset = methodToOffset.get(name);
			
			/* set that offset to point to the new class */
			this.methodOffsetToClassName.put(methodOffset, className);
			
			return;
		}
		
		methodToOffset.put(name, this.currentMaxMethodOffset);
		methodOffsetToClassName.put(currentMaxMethodOffset, className);
		
		// update method offset
		++this.currentMaxMethodOffset;
		
	}
	
	/**
	 * method adds a new field to the class layout (fields cannot be overridden, luckily )
	 * @param name
	 */
	
	private void addField(String name)
	{
		this.fieldToOffset.put(name, this.currentMaxFieldOffset);
		++this.currentMaxFieldOffset;
		
	}
	
	
	public String getClassName()
	{
		return this.className;
	}
	
	
	public Integer getMethodOffset(String method)
	{
		return this.methodToOffset.get(method);
	}
	
	public Integer getFieldOffset(String field)
	{
		return this.fieldToOffset.get(field);
	}
	
	
	/**
	 * this method returns an ordered list (by method offset) of dispatch table entries
	 * each entry contains a method name and the name of the class that should call it
	 * @return the dispatch table
	 */
	
	public List<DispatchTableEntry> getDispatchTable()
	{
		
		// get the name of all classes that exist in this class (some methods may be inherited)
		Set<String> methodNames = this.methodToOffset.keySet();
		
		/* order the methods by their offset */
		List<String> methodNamesOrdered = new ArrayList<String>();
		
		// insert all the methods to the list
		for( String methodName : methodNames)
			methodNamesOrdered.add(methodName);
		
		Collections.sort(methodNamesOrdered, new Comparator<String>(){

			@Override
			public int compare(String arg0, String arg1) {
				
				return methodToOffset.get(arg0).compareTo(methodToOffset.get(arg1));
			}

		
			
		});
		
		
		List<DispatchTableEntry> dispatchTable = new ArrayList<DispatchTableEntry>();
		
		for(String methodName : methodNamesOrdered)
		{
			Integer methodOffset = this.methodToOffset.get(methodName);
			DispatchTableEntry entry = new DispatchTableEntry(methodOffset, methodName,
															  this.methodOffsetToClassName.get(methodOffset));
			dispatchTable.add(entry);
			
		}
		
		return dispatchTable;
				
				
		
	}
	
	
	
	
	
}
