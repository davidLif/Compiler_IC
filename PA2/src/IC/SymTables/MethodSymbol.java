package IC.SymTables;

public abstract class MethodSymbol extends Symbol{
	
	public MethodSymbol(String id) {
		super(id);
	}

	
	@Override
	public String toString()
	{
		return String.format("%s method: %s %s",this.getMethodKind() ,this.id, this.type.toString());
	}
	
	protected abstract String getMethodKind();
	
	

}
