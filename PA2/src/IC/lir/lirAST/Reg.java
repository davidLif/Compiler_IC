package IC.lir.lirAST;


/**
 * represent a LIR register
 *
 */

public class Reg extends LirNode{
	
	
	/**
	 * register id
	 * example: id == 5    =>  this register represents R5
	 */
	private int index;
	
	
	public Reg(int index)
	{
		this.index = index;
	}

	public int getIndex()
	{
		return this.index;
	}

	@Override
	public String emit() {
		
		return "R" + index;
	}

}
