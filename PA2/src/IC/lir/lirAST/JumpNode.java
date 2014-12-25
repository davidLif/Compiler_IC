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
	protected Label jumpLabel;
	
	
	public JumpNode(Label jumpLabel)
	{
		this.jumpLabel = jumpLabel;
	}
	
	@Override
	public String emit() {
		return "Jump "+jumpLabel.emit()+"\n";
	}

}
