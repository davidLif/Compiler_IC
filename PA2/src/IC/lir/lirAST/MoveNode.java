package IC.lir.lirAST;


/**
 * represents a move instruction
 * @author Denis
 *
 */
public class MoveNode extends LirNode{
	
	LirNode op1; /* immediate, reg, memory */
	LirNode op2; /* reg, memory */
	
	
	public MoveNode(LirNode op1, LirNode op2)
	{
		this.op1 = op1;
		this.op2 = op2;
	}


	@Override
	public String emit() {
		return "Move "+op1.emit() +","+op2.emit()+"\n";
	}
}
