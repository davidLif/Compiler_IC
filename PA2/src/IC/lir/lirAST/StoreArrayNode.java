package IC.lir.lirAST;

public class StoreArrayNode extends MoveArrayNode{
	
	
	/**
	 * source register/ immediate to store at array
	 */
	
	private LirNode source;
	
	public StoreArrayNode(Reg arrayReg, LirNode index, LirNode source) {
		super(arrayReg, index);
		this.source = source;
		
	}

	@Override
	public String emit() {
		
		
		return String.format("MoveArray %s,%s[%s]", source.emit(), this.arrayReg.emit(), this.index.emit());
	}

	

}
