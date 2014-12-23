package IC.lir.lirAST;

import java.util.List;

import IC.lir.Label;

public class StaticCallNode extends MethodCallNode{
	
	
	private List<Memory> memoryVars;
	
	private Label methodLabel;

	public StaticCallNode(Label methodLabel, List<Memory> memVars,  List<Parameter> params, Reg targetRegister) {
		super(params, targetRegister);
		
		this.memoryVars = memVars;
		this.methodLabel = methodLabel;
		
	}
	

}
