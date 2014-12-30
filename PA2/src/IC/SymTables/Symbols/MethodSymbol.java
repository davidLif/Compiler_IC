package IC.SymTables.Symbols;

public abstract class MethodSymbol extends Symbol{
	
	
	 /**
	  * name of the class that defines this method
	  */
	private String className;
	
	public MethodSymbol(String id, String className) {
		super(id);
		this.className = className;
		
	}

	
	@Override
	public String toString()
	{
		return String.format("%s method: %s %s",this.getMethodKind() ,this.id, (this.type == null ? "NONE" : this.type.toString()));
	}
	
	protected abstract String getMethodKind();
	
	public abstract boolean isStatic();

	
	public String getClassName()
	{
		
		return this.className;
	}
}
