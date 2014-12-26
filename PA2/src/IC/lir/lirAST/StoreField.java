package IC.lir.lirAST;

public class StoreField extends MoveFieldNode{

	/**
	 * what to store in the field
	 */
	private LirNode source;
	
	
	public StoreField(Reg instanceRegister, LirNode offset, LirNode source) {
		super(instanceRegister, offset);
		this.source = source;
	}


	@Override
	public String emit() {
		return "MoveField "+source.emit()+","+instanceRegister.emit()+"."+offset.emit()+"\n";
	}

}
