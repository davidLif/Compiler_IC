package IC.SymTables.Symbols;


public class ParameterSymbol extends Symbol{

	public ParameterSymbol(String id) {
		super(id);
		this.kind = SymbolKind.PARAM;
		
	}

	@Override
	public String toString()
	{
		return String.format("Parameter: %s %s", (this.type == null ? "NONE" : this.type), this.id);
	}


	
	
}
