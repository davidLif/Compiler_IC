package IC.SymTables;

public class ClassSymbolTable extends SymbolTable{

	public ClassSymbolTable(String id) {
		super(id);
		
	}

	public ClassSymbolTable(String id, SymbolTable parentSymTable) {
		super(id, parentSymTable);
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Class Symbol Table: %s", this.id);
	}

	
	

}
