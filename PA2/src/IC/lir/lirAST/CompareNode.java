package IC.lir.lirAST;

public class CompareNode extends LirNode {
	
	LirNode op1; /* immediate, reg, memory */
	LirNode op2; /* reg */
	
	public CompareNode(LirNode op1, LirNode op2){
		this.op1 = op1;
		this.op2 = op2;
	}
	@Override
	public String emit() {
		return "Compare "+op1.emit() +","+op2.emit()+"\n";
	}

}
