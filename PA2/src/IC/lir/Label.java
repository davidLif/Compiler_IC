package IC.lir;


/**
 * 
 * @author Denis
 *
 *
 *	this class represents a lir label
 *  could be any of the following:
 *  
 *  - string label
 *  - method label
 *  - class dispatch table label
 *  - other (like if, else)
 *  
 */

public class Label{

	
	
	
	private String labelName; 
	
	public Label(String labelName)
	{
		this.labelName = labelName;
	}
	
	
	public String toString()
	{
		return this.labelName;
	}
	
	/* to be added: address */
	
}
