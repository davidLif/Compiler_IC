package IC.SymTables;

public class ParameterSymbol extends VariableSymbol{

	public ParameterSymbol(String id) {
		super(id);
		
	}

	@Override
	protected String getVarKind() {
		
		return "Parameter";
	}
	

	
}
