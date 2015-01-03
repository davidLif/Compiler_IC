package IC.lir.lirAST;

import IC.lir.LabelGenerator;


public class StringLiteralNode extends LirNode{

	
	/**
	 * the actual string  literal
	 */
	private String stringLiteral;
	
	
	/**
	 * the label generated for the string
	 */
	
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
