package IC.lir.lirAST;

public class StoreField extends MoveFieldNode{

	/**
	 * what to store in the field
	 */
	private LirNode source;
	
	
	public StoreField(RegWithOffset regWithOffset, LirNode source) {
		super(regWithOffset);
		this.source = source;
	}


	@Override
	public String emit() {
		return "MoveField "+source.emit()+","+regWithOffset.emit()+"\n";
	}

}
