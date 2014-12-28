package IC.lir.lirAST;

public abstract class MoveFieldNode extends LirNode{

	
	/**
	 * represents the pair Register.Offset
	 * when the register holds the class object
	 */
	protected RegWithOffset regWithOffset;
	
	
	public MoveFieldNode(RegWithOffset regWithOffset)
	{
		
		this.regWithOffset = regWithOffset;
		
	}
}
