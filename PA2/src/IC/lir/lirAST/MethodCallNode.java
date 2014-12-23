package IC.lir.lirAST;

import java.util.List;

public abstract class MethodCallNode {

	private List<Parameter> params;
	
	private Reg targetRegister;
	
	public MethodCallNode(List<Parameter> params, Reg targetRegister)
	{
		this.params = params;
		this.targetRegister = targetRegister;
	}
	
}
