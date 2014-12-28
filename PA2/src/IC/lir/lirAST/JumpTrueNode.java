package IC.lir.lirAST;


public class JumpTrueNode extends JumpNode{

	public JumpTrueNode(Label jumpLabel) {
		super(jumpLabel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String emit() {
		return "JumpTrue "+jumpLabel.emit()+"\n";
	}
}
