package IC.lir;

import java.util.HashMap;
import java.util.Map;


public class LabelGenerator {

	
	/**
	 * number of string literals generated so far
	 * index of next string literal
	 */
	private int stringLiteralCounter = 0;
	
	/**
	 * maps each string literal it seen so far to a label object
	 */
	private Map<String, Label> stringLiteralLabels;
	
	
	/**
	 * maps each class we've seen so far to a label object
	 */
	private Map<String, Label> classDispatchTableLabels;
	
	
	/**
	 * maps class name -> method name -> label
	 */
	private Map<String, Map<String, Label>> methodLabels;
	
	
	private Label mainMethodLabel;
	
	/**
	 * method returns a label by given string literal
	 * if such a string was already added before, its label will returned
	 * otherwise, a new label will be allocated for the string literal
	 * @param stringLiteral
	 * @return label for the string
	 */
	
	public Label getStringLabel(String stringLiteral)
	{
		Integer index;
		if(stringLiteralLabels.containsKey(stringLiteral))
		{
			// seen this string before
			
		}
		else
		{
			// new string literal
			index = stringLiteralCounter;
			stringLiteralLabels.put(stringLiteral, new Label(generateStringLabel(index)));
			
			++stringLiteralCounter;
			
		}
		
		return stringLiteralLabels.get(stringLiteral);
	}
	
	
	
	/**
	 * returns the actual label for a string, by its id
	 * @param index - index of the string
	 * @return actual label
	 */
	private String generateStringLabel(int index)
	{
		return String.format("str%d", index);
	}
	
	/**
	 * returns a class DV label for the given class
	 * @param className
	 * @return DV class label
	 */
	
	public Label getClassDVLabel(String className)
	{
		Label label = this.classDispatchTableLabels.get(className);
		if(label == null)
		{
			label = this.classDispatchTableLabels.put(className, new Label(String.format("_DV_%s", className)));
		}
		
		
		return label;
	}
	
	/**
	 * returns a label for the given method
	 * @param methodName - name of the method
	 * @param className  - name of the class
	 * @return the label name for the method
	 */
	
	public Label getMethodLabel(String methodName, String className)
	{
		// special case
		if(methodName.equals("main"))
		{
			return this.mainMethodLabel;
		}
		
		
		Map<String, Label> methodsToLabels = this.methodLabels.get(className);
		if(methodsToLabels == null)
		{
			// first time we see this class name
			methodsToLabels = new HashMap<String, Label>();
			Label label = new Label( String.format("_%s_%s", className, methodName));
			methodsToLabels.put(methodName, label);
			this.methodLabels.put(className, methodsToLabels);
			
			return label;
		}
		
		Label methodLabel = methodsToLabels.get(methodName);
		if(methodLabel == null)
		{
			// no such label was added yet
			Label label = new Label( String.format("_%s_%s", className, methodName));
			methodsToLabels.put(methodName, label);
			return label;
		}
		
		// else, it exists
		return methodLabel;
		
		
	}
	
}
