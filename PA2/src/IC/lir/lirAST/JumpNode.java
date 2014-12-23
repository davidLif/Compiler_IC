package IC.lir.lirAST;

import IC.lir.Label;

public class JumpNode extends LirNode{

	
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
