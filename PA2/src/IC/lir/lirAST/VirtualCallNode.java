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

}
