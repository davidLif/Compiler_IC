package IC.SymTables;

public class GlobalSymbolTable extends SymbolTable{

	public GlobalSymbolTable(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Global Symbol Table: %s", this.id);
	}

	
	
	

	
}
