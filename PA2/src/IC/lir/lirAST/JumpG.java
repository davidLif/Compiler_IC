package IC.lir.lirAST;


public class JumpG extends JumpNode{

	public JumpG(Label jumpLabel) {
		super(jumpLabel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String emit() {
		return "JumpG "+jumpLabel.emit()+"\n";
	}
}
