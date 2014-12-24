package IC.lir.lirAST;

public class LoadArrayNode extends MoveArrayNode {

	/**
	 * target register to load to
	 */
	private Reg targetRegister; 
	
	LoadArrayNode(Reg arrayReg, LirNode index, Reg targetReg) {
		super(arrayReg, index);
		
		this.targetRegister = targetReg;
		
		
	}

}
