package IC.SymTables.Symbols;


public class FieldSymbol extends VariableSymbol{

	public FieldSymbol(String id) {
		super(id);
		
	}

	@Override
	protected String getVarKind() {
		return "Field";
	}
	

}