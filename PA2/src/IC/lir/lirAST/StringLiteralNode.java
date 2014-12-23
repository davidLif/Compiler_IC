package IC.lir.lirAST;

import IC.lir.Label;
import IC.lir.LabelGenerator;


public class StringLiteralNode extends LirNode{

	
	private String stringLiteral; 
	private Label stringLabel;
	
	public StringLiteralNode(String stringLiteral, LabelGenerator labelGen)
	{
		this.stringLabel = labelGen.getStringLabel(stringLiteral);
		this.stringLiteral = stringLiteral;
	}
	
	@Override
	public String emit() {
		
		return String.format("%s: %s", stringLabel, stringLiteral);
	}

	
	
}
