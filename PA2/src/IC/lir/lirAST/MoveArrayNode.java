package IC.lir.lirAST;

public abstract class MoveArrayNode extends LirNode{

	
	/**
	 * the register that holds the array address
	 */
	
	protected Reg arrayReg;   
	
	/**
	 * the register/immediate that holds the array index
	 */
	protected LirNode index;  
	 
			
	MoveArrayNode(Reg arrayReg, LirNode index)
	{
		this.arrayReg = arrayReg;
		this.index = index;
		
	}
	
	
	
	
}
