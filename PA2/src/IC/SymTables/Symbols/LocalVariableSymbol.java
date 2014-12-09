package IC.SymTables.Symbols;

public class LocalVariableSymbol extends VariableSymbol{

	public LocalVariableSymbol(String id) {
		super(id);
		
	}

	@Override
	protected String getVarKind() {
		
		return "Local variable";
	}
	

}
