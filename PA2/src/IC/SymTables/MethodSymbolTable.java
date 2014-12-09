package IC.SymTables;

public class MethodSymbolTable extends SymbolTable{

	public MethodSymbolTable(String id) {
		super(id);
	
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Method Symbol Table: %s", this.id);
	}

	@Override
	public boolean resolveSymbol(Symbol sym) {
		// TODO Auto-generated method stub
		return false;
	}

	
	
	

}
