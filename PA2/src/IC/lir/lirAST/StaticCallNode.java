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

	@Override
	public String emit() {
		
		StringBuilder result = new StringBuilder(String.format("StaticCall %s(", this.methodLabel.emit()));
		for(int i = 0; i < this.memoryVars.size(); ++i)
		{
			if(i < this.memoryVars.size() - 1)
				result.append(String.format("%s=%s,", memoryVars.get(i).emit(), params.get(i).emit()));
			else
				result.append(String.format("%s=%s", memoryVars.get(i).emit(), params.get(i).emit()));
			
		}
		result.append(")," + this.targetRegister.emit() + "\n");
		
		return result.toString();
		
	}

	
}
