package IC.lir.lirAST;

public abstract class MoveArrayNode extends LirNode{

	
	/**
	 * register that holds the array [ immediate or register of index ]
	 */
	
	protected RegWithIndex regIndex;   
	
	 
	 
			
	public MoveArrayNode(RegWithIndex regIndex)
	{
		this.regIndex = regIndex;
		
	}
	
	
	
	
}
