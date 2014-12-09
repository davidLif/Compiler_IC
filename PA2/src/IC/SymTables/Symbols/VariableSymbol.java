package IC.SymTables.Symbols;


public abstract class VariableSymbol extends Symbol{

	public VariableSymbol(String id) {
		super(id);
		
	}
	
	@Override
	public String toString()
	{
		return String.format("%s: %s %s", this.getVarKind(), this.type.toString(), this.id);
	}
	
	protected abstract String getVarKind();
	

}
