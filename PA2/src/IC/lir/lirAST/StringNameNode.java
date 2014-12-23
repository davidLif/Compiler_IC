package IC.lir.lirAST;

import IC.lir.Label;
import IC.lir.LabelGenerator;

public class StringNameNode extends Parameter{
	
	private Label stringLabel;
	
	public StringNameNode(LabelGenerator labelGen, String name)
	{
		this.stringLabel = labelGen.getStringLabel(name);
	}

}
