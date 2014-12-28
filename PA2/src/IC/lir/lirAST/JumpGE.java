package IC.lir.lirAST;


public class JumpGE extends JumpNode{

	public JumpGE(Label jumpLabel) {
		super(jumpLabel);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String emit() {
		return "JumpGE "+jumpLabel.emit()+"\n";
	}

}
