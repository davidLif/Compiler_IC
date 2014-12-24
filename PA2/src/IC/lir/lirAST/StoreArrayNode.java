package IC.lir.lirAST;

public class StoreArrayNode extends MoveArrayNode{
	
	
	/**
	 * source register/ immediate to store at array
	 */
	
	private LirNode source;
	
	StoreArrayNode(Reg arrayReg, LirNode index, LirNode source) {
		super(arrayReg, index);
		this.source = source;
		
	}

	

}
