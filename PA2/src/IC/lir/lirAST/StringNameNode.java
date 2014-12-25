package IC.lir.lirAST;

import IC.lir.LabelGenerator;

public class StringNameNode{
	
	private Label stringLabel;
	
	public StringNameNode(LabelGenerator labelGen, String name)
	{
		this.stringLabel = labelGen.getStringLabel(name);
	}

}
