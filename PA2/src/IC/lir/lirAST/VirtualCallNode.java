package IC.lir.lirAST;

import java.util.List;

public class VirtualCallNode extends MethodCallNode {

	private List<Memory> memoryVars;
	
	private Reg objectRegister;

	private Immediate offset;
	
	public VirtualCallNode(Reg objectRegister, Immediate offset, List<Memory> memVars,  List<Parameter> params, Reg targetRegister) {
		super(params, targetRegister);
		
		this.objectRegister = objectRegister;
		this.offset = offset;
		this.memoryVars = memVars;
		
	}

}
