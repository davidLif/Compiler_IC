package IC.lir.lirAST;


/**
 * 
 * @author Denis
 *
 *
 *	represents the following node:
 *
 *	Reg[Reg] OR Reg[Immediate]
 *
 *  used to represent array location, used to store to and load from
 *
 */

public class RegWithIndex extends LirNode{
	
	private Reg arrayReg;
	
	private LirNode index; /* immediate or reg */
	
	public RegWithIndex(Reg arrayReg, LirNode index)
	{
		
		this.arrayReg = arrayReg;
		this.index = index;
		
	}
	
	
	public Reg getArrayReg()
	{
		return this.arrayReg;
		
		
	
	}
	
	
	public LirNode getIndex()
	{
		
		return this.index;
	}


	@Override
	public String emit() {
		
		return arrayReg.emit() + "[" + index.emit() + "]";
	}

}
