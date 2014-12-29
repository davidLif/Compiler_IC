package IC.lir.lirAST;

public class JumpLE extends JumpNode{

	public JumpLE(Label jumpLabel) {
		super(jumpLabel);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String emit() {
		return "JumpLE "+jumpLabel.emit()+"\n";
	}
	
}
