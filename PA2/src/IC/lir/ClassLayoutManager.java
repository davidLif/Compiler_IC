package IC.lir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.ICClass;
import IC.AST.Program;
import IC.lir.lirAST.DispatchTableNode;
import IC.lir.lirAST.Label;

public class ClassLayoutManager {

	
	/**
	 * this map maps class names to their corresponding class layouts
	 */
	private Map<String, ClassLayout> classToClassLayout; 
	
	
	/**
	 * method constructs the class layouts for the whole program 
	 * @param programRoot
	 */
	
	public ClassLayoutManager(Program programRoot)
	{
		
		classToClassLayout = new HashMap<String, ClassLayout>();
		
		for(ICClass currClass : programRoot.getClasses())
		{
			
			if(currClass.getName().equals("Library"))
			{
				//TODO: verify this is correct
				continue;
			}
			
			/* iterate over classes, and construct their class layouts
			 * since we made sure that there are no forward references
			 * a class may only rely on class layouts of its parent, that was already constructed before
			 */
			
			
			ClassLayout currClassLayout ;
			
			if(currClass.hasSuperClass())
			{
				currClassLayout = new ClassLayout(currClass, classToClassLayout.get(currClass.getSuperClassName()));
			}
			else
			{
				/* no super class */
				currClassLayout = new ClassLayout(currClass);
			}
			
			/* add new mapping */
			classToClassLayout.put(currClass.getName(), currClassLayout);
		}
		
		
	}
	
	
	/** 
	 * this method returns the offset of a given method, in a given class
	 * @param className  - name of class [ static class type ] 
	 * @param methodName - name of method in class
	 */
	public Integer getMethodOffset(String className, String methodName)
	{
		return this.classToClassLayout.get(className).getMethodOffset(methodName);
	}
	
	/**
	 * this method returns the offset of a given field in class className
	 * @param className - name of the class
	 * @param fieldName - name of the field
	 * @return the offset of the field in the class
	 */
	
	public Integer getFieldOffset(String className, String fieldName)
	{
		//since DV pointer is in offset 0, all field move +1
		return this.classToClassLayout.get(className).getFieldOffset(fieldName)+1;
	}
	
	/**
	 * this method returns the class dispatch table 
	 * @param className - name of the class
	 * @return
	 */
	
	public DispatchTableNode getClassDispatchTable(String className, LabelGenerator labelGen)
	{
		ClassLayout layout = classToClassLayout.get(className);
		
		List<Label> methodLabels = new ArrayList<Label>();
		List<DispatchTableEntry> entries = layout.getDispatchTableEntries();
		
		/* transform the entries to actual method labels */
		
		for(DispatchTableEntry entry : entries)
		{
			methodLabels.add(labelGen.getVirtualMethodLabel(entry.getMethodName(), entry.getClassName()));
		}
		Label classLabel = labelGen.getClassDVLabel(className);
		
		return new DispatchTableNode(classLabel, methodLabels);
	}
	
	/**
	 * this method returns a collection of all dispatch tables
	 * @param labelGen - label generator
	 * @return
	 */
	
	public List<DispatchTableNode> getAllDispatchTables(LabelGenerator labelGen)
	{
		
		List<DispatchTableNode> result = new ArrayList<DispatchTableNode>();
		
		for(String className : this.classToClassLayout.keySet())
		{
			result.add(this.getClassDispatchTable(className, labelGen));
		}
		return result;
	}
	
	
	/**
	 * get class layout by class name
	 * @param className
	 * @return
	 */
	public ClassLayout getClassLayout(String className)
	{
		return classToClassLayout.get(className);
		
	}
	
	public Integer getClassSize(String className){
		ClassLayout layout = classToClassLayout.get(className);
		
		return layout.getDispatchTableSize();
	}
	
	
	
}
