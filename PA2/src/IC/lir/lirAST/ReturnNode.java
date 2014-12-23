package IC.lir.lirAST;

public class ReturnNode extends LirNode{

	private Parameter returnParam;
	
	
	public ReturnNode(Parameter returnParam)
	{
		this.returnParam = returnParam;
	}
	
	@Override
	public String emit() {
		// TODO Auto-generated method stub
		return null;
	}

}
