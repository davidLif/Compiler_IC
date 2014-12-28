package IC.lir.lirAST;

public class StoreArrayNode extends MoveArrayNode{
	
	
	/**
	 * source register/ immediate to store at array
	 */
	
	private LirNode source;
	
	public StoreArrayNode(RegWithIndex regIndex, LirNode source) {
		super(regIndex);
		this.source = source;
		
	}

	@Override
	public String emit() {
		
		
		return String.format("MoveArray %s,%s\n", source.emit(), this.regIndex.emit());
	}

	

}
