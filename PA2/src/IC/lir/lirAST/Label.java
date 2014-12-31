package IC.lir.lirAST;


/**
 * 
 * @author Denis
 *
 *
 *	this class represents a lir label
 *  could be any of the following:
 *  
 *  - string label (string name)
 *  - method label
 *  - class dispatch table label
 *  - other (like if, else generated labels)
 *  
 *  
 *  labels should be generated only by LabelGenerator class !
 *  
 */

public class Label extends Memory{

	
	
	/**
	 * label name
	 */
	private String labelName; 
	
	
	
	public Label(String labelName)
	{
		super(labelName,MemoryKind.LOCAL);
		this.labelName = labelName;
	}
	
	
	public String toString()
	{
		return this.labelName;
	}


	@Override
	public String emit() {
		
		return this.toString();
	}
	
	/* to be added: address */
	
}
