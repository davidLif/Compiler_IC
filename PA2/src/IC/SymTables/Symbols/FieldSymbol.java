package IC.SymTables.Symbols;


public class FieldSymbol extends Symbol{

	public FieldSymbol(String id) {
		super(id);
		this.kind = SymbolKind.FIELD;
	}

	
	@Override
	public String toString()
	{
		return String.format("Field: %s %s", (this.type == null ? "NONE" : this.type), this.id);
	}

}
