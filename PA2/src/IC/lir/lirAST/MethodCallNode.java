package IC.lir.lirAST;

import java.util.List;

public abstract class MethodCallNode extends LirNode{

	
	/**
	 * list of parameters that are passed to the class
	 * parameters are any of the following:
	 * 		1. register (Reg)
	 * 		2. variable (Memory)
	 * 		3. constant (Immediate)
	 * 		4. string label (Label)
	 * 
	 * in other words, anything that can store a value
	 */
	protected List<LirNode> params;
	
	
	/**
	 * target register to store the result at
	 */
	protected Reg targetRegister;
	
	public MethodCallNode(List<LirNode> params, Reg targetRegister)
	{
		this.params = params;
		this.targetRegister = targetRegister;
	}
	
}
