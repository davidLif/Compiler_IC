package IC.lir.lirAST;

public class ArrayLengthNode extends LirNode{

	/**
	 * memory or register that stores the array pointer
	 */
	
	private LirNode array; 
	
	
	/**
	 * register to store the length
	 */
	private Reg targetRegister;
	
	
	public ArrayLengthNode(LirNode array, Reg targetRegister)
	{
		
		this.array = array;
		this.targetRegister = targetRegister;
	}


	@Override
	public String emit() {
		return "ArrayLength"+" "+array.emit()+","+targetRegister.emit()+"\n";
	}
	
	
	
}
