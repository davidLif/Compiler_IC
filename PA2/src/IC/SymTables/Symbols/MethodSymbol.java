package IC.SymTables.Symbols;

public abstract class MethodSymbol extends Symbol{
	
	public MethodSymbol(String id) {
		super(id);
	}

	
	@Override
	public String toString()
	{
		return String.format("%s method: %s %s",this.getMethodKind() ,this.id, (this.type == null ? "NONE" : this.type.toString()));
	}
	
	protected abstract String getMethodKind();
	
	public abstract boolean isStatic();

}
