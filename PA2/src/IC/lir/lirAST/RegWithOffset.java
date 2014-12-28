package IC.lir.lirAST;


/**
 * 
 * @author Denis
 *
 *	this class represents the pair
 *		objectRegister.offset
 *	used in external local variable, in both storing and loading.
 *
 */

public class RegWithOffset extends LirNode{
	
	private Reg objectRegister;
	
	
	private Immediate offset;
	
	public RegWithOffset(Reg objectRegister, Immediate offset)
	{
		
		this.objectRegister = objectRegister;
		this.offset = offset;
		
		
	}
	
	public Reg getObjectRegister()
	{
		return this.objectRegister;
		
	}
	
	
	public Immediate getOffset()
	{
		return this.offset;
		
	}

	@Override
	public String emit() {
		
		return objectRegister.emit() + "." + offset.emit();
	}

}
