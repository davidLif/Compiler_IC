package IC.lir.lirAST;

public abstract class MoveFieldNode extends LirNode{

	/**
	 * the register that holds the class object reference
	 */
	private Reg instanceRegister; 
	
	
	/**
	 * the offset inside the class object that represents the field
	 */
	
	private LirNode offset;
	
	
	public MoveFieldNode(Reg instanceRegister, LirNode offset)
	{
		
		this.instanceRegister = instanceRegister;
		this.offset = offset;
		
	}
}
