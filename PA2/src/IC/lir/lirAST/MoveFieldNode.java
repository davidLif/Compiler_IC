package IC.lir.lirAST;

public abstract class MoveFieldNode extends LirNode{

	/**
	 * the register that holds the class object reference
	 */
	protected Reg instanceRegister; 
	
	
	/**
	 * the offset inside the class object that represents the field
	 */
	
	protected LirNode offset;
	
	
	public MoveFieldNode(Reg instanceRegister, LirNode offset)
	{
		
		this.instanceRegister = instanceRegister;
		this.offset = offset;
		
	}
}
