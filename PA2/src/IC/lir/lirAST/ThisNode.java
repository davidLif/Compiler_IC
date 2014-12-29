package IC.lir.lirAST;

public class ThisNode extends Memory{

	public ThisNode() {
		
		super("this", MemoryKind.PARAM);
		
	}

	@Override
	public String emit() {
		
		return "this";
	}
	
	
	
}
