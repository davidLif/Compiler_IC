package IC.lir.lirAST;

public class BinaryInstructionNode extends LirNode{

	
	/** 
	 * the binary operation itself
	 */
	private lirBinaryOp binOp; 
	
	/**
	 * operand one (left side), could be Immediate, memory or register
	 */
	
	
	private LirNode op1;
	
	
	/**
	 * target register, where to store the result (also 2nd operand)
	 */
	
	private Reg targetRegister;
	
	
	public BinaryInstructionNode(lirBinaryOp binOp, LirNode op1, Reg targetRegister)
	{
		this.binOp = binOp;
		this.op1 = op1;
		this.targetRegister = targetRegister;
	}


	@Override
	public String emit() {
		return binOp.getRepresentation()+" "+op1.emit()+","+targetRegister.emit()+"\n" ;
	}
	
}
