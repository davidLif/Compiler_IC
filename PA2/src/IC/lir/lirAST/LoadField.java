package IC.lir.lirAST;

public class LoadField extends MoveFieldNode{

	/**
	 * target register to load to
	 */
	private Reg targetRegister;
	
	
	public LoadField(Reg instanceRegister, LirNode offset, Reg targetRegister) {
		super(instanceRegister, offset);
		
		this.targetRegister = targetRegister;
		
	}


	@Override
	public String emit() {
		return "MoveField "+instanceRegister.emit()+"."+offset.emit()+","+targetRegister.emit()+"\n";
	}

	
	

}
