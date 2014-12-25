package IC.lir.lirAST;


public class JumpFalse extends JumpNode {

	public JumpFalse(Label jumpLabel) {
		super(jumpLabel);
	}
	
	@Override
	public String emit() {
		return "JumpFalse "+jumpLabel.emit()+"\n";
	}

}
