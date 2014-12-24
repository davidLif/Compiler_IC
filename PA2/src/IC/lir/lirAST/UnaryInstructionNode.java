package IC.lir.lirAST;

public class UnaryInstructionNode {

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
	
	
	
}
