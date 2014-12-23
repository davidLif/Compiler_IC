package IC.lir.lirAST;

import java.util.List;

import IC.lir.Label;
import IC.lir.LabelGenerator;


public class MethodNode extends LirNode{

	
	/**
	 * label of the method 
	 */
	
	private Label        methodLabel;
	
	
	/**
	 * list of lir instructions
	 */
	
	private List<LirNode> instructions;
	
	
	public MethodNode(Label methodLabel, List<LirNode> instructions)
	{
		this.methodLabel = methodLabel;
		this.instructions = instructions;
	}
	
	@Override
	public String emit() {
		StringBuilder sb = new StringBuilder(this.methodLabel + ":\n");
		for(LirNode instruct : this.instructions)
		{
			sb.append(instruct.emit() + "\n");
		}
		return sb.toString();
	}

	
	
}
