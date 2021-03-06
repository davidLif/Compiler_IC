package IC.lir.lirAST;

public class ReturnNode extends LirNode{

	/**
	 * could be any parameters (Reg, Label, Memory, Immediate)
	 */
	
	private LirNode returnParam;
	
	
	public ReturnNode(LirNode returnParam)
	{
		this.returnParam = returnParam;
	}
	
	@Override
	public String emit() {
		
		if(returnParam == null)
		{
			// empty return
			return "Return 9999\n";
		}
		
		return "Return "+returnParam.emit()+"\n";
	}

}
