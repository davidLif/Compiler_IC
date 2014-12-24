package IC.lir.lirAST;

import java.util.List;


public class StaticCallNode extends MethodCallNode{
	
	/**
	 * list of method parameters
	 * should match exactly to those defined in the method
	 */
	private List<Memory> memoryVars;
	
	
	/**
	 * method label
	 */
	
	private Label methodLabel;

	public StaticCallNode(Label methodLabel, List<Memory> memVars,  List<LirNode> params, Reg targetRegister) {
		super(params, targetRegister);
		
		this.memoryVars = memVars;
		this.methodLabel = methodLabel;
		
	}
	

}
