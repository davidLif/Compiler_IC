package IC.SymTables;

public class StatementBlockSymTable extends SymbolTable{

	
	/**
	 * id - the identifier of the wrapping method
	 */
	public StatementBlockSymTable(String id) {
		super("statement block in " + id);
		
	}

	@Override
	protected String getSymbolTableHeader() {
		return String.format("Statement Block Symbol Table ( located in %s )", this.parentSymbolTable.getId());
	}
	
	@Override
	public boolean resolveSymbol(Symbol sym) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
