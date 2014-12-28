package IC.lir.lirAST;

public class LoadField extends MoveFieldNode{

	/**
	 * target register to load to
	 */
	private Reg targetRegister;
	
	
	public LoadField(RegWithOffset regWithOffset, Reg targetRegister) {
		super(regWithOffset);
		
		this.targetRegister = targetRegister;
		
	}


	@Override
	public String emit() {
		return "MoveField "+regWithOffset.emit()+","+targetRegister.emit()+"\n";
	}

	
	

}
