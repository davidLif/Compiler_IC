package IC.lir.lirAST;

import java.util.List;

public class VirtualCallNode extends MethodCallNode {

	/**
	 * the method parameters of the method
	 */
	
	private List<Memory> memoryVars;
	
	/**
	 * the register that points to the class object
	 */
	private Reg objectRegister;
	
	/**
	 * the offset of the method inside the class object
	 */

	private Immediate offset;
	
	public VirtualCallNode(Reg objectRegister, Immediate offset, List<Memory> memVars,  List<LirNode> params, Reg targetRegister) {
		super(params, targetRegister);
		
		this.objectRegister = objectRegister;
		this.offset = offset;
		this.memoryVars = memVars;
		
	}

	@Override
	public String emit() {
		
		StringBuilder result = new StringBuilder(String.format("VirtualCall %s.%s(", this.objectRegister, this.offset));
		for(int i = 0; i < this.memoryVars.size(); ++i)
		{
			
			result.append(String.format("%s=%s", memoryVars.get(i), params.get(i)));
			
		}
		result.append(")");
		
		return result.toString();
		
	}

}
