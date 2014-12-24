package IC.lir.lirAST;

public abstract class MoveArrayNode {

	
	/**
	 * the register that holds the array address
	 */
	
	private Reg arrayReg;   
	
	/**
	 * the register/immediate that holds the array index
	 */
	private LirNode index;  
	 
			
	MoveArrayNode(Reg arrayReg, LirNode index)
	{
		this.arrayReg = arrayReg;
		this.index = index;
		
	}
	
	
	
	
}
