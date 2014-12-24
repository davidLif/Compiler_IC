package IC.lir.lirAST;

/**
 * 
 *
 *
 *	represents an unconditional jump node
 *
 */


public class JumpNode extends LirNode{

	/**
	 * jump label
	 */
	private Label jumpLabel;
	
	
	public JumpNode(Label jumpLabel)
	{
		this.jumpLabel = jumpLabel;
	}
	
	@Override
	public String emit() {
		// TODO Auto-generated method stub
		return null;
	}

}
