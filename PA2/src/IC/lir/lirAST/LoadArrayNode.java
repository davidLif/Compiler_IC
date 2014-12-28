package IC.lir.lirAST;

public class LoadArrayNode extends MoveArrayNode {

	/**
	 * target register to load to
	 */
	private Reg targetRegister; 
	
	public LoadArrayNode(RegWithIndex regIndex, Reg targetReg) {
		super(regIndex);
		
		this.targetRegister = targetReg;
		
		
	}

	@Override
	public String emit() {
		
		return "MoveArray " + this.regIndex.emit() + "," + this.targetRegister.emit() + "\n";
	}

}
