package IC.lir.lirAST;


public class LabelNode extends LirNode{

	private Label label;
	
	public LabelNode(Label label)
	{
		this.label = label;
	}

	@Override
	public String emit() {
		return label.emit()+":"+"\n";
	}
}
