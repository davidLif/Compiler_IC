package IC.lir.lirAST;

public class UnaryInstructionNode extends LirNode{

	/**
	 * the single operand
	 */
	
	private Reg operand;
	
	/**
	 * the actual operation
	 */
	
	private lirUnaryOp op;
	
	
	public UnaryInstructionNode(Reg operand, lirUnaryOp operation)
	{
		this.operand  = operand;
		this.op = operation;
	}


	@Override
	public String emit() {
		return op.getRepresentation()+" "+operand.emit()+"\n";
	}
	
	
	
}
