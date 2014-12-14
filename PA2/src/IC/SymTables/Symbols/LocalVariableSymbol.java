package IC.SymTables.Symbols;

public class LocalVariableSymbol extends Symbol{

	
	
	public LocalVariableSymbol(String id) {
		super(id);
		this.kind = SymbolKind.LOCALVAR;
		
	}
	
	@Override
	public String toString()
	{
		return String.format("Local variable: %s %s", (this.type == null ? "NONE" : this.type), this.id);
	}

	
	
	
	



}
