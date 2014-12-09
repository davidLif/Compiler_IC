package IC.SymTables;



public class MethodSymbolTable extends VariableSymbolTable{

	public MethodSymbolTable(String id) {
		super(id);
	
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Method Symbol Table: %s", this.id);
	}

	



	
	
	

}
