package IC.lir.lirAST;

public class Memory extends LirNode{

	
	/**
	 * holds PARAM or LOCAL depending if memory node is a method parameter
	 * or local variable
	 */
	private MemoryKind kind; 
	
	/**
	 * name of the variable
	 */
	
	private String name; 
	
	
	public Memory(String name, MemoryKind kind)
	{
		this.name = name;
		this.kind = kind;
	}
	
	
	@Override
	public String emit() {
		if(kind == MemoryKind.PARAM){
			//TODO- I think just name
			return null;
		}
		else{
			return name;
		}
	}

	
	/**
	 * enum representing the kind of the variable
	 * could be a local variable or method parameter
	 *
	 */
	
	public static enum MemoryKind
	{
		PARAM, LOCAL;
	}
	
	
}
