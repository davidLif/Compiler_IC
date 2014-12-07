package IC.SymTables;

public class LocalVariableSymbol extends VariableSymbol{

	public LocalVariableSymbol(String id) {
		super(id);
		
	}

	@Override
	protected String getVarKind() {
		
		return "Local variable";
	}
	

}
