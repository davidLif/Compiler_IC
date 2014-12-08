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

	@Override
	public boolean resolveSymbol(Symbol sym) {
		// TODO Auto-generated method stub
		return false;
	}

	
	
	

	
}
