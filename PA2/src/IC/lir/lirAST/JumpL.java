package IC.lir.lirAST;


public class JumpL extends JumpNode{

	public JumpL(Label jumpLabel) {
		super(jumpLabel);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String emit() {
		return "JumpL "+jumpLabel.emit()+"\n";
	}
}
