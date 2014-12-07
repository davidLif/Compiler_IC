package IC.SymTables;

public class ClassSymbol extends Symbol {

	/**
	 * the scope that is enclosed in this class
	 */
	private ClassSymbolTable symbolTable;
	
	public ClassSymbol(String id, ClassSymbolTable symTable) {
		super(id);
		this.symbolTable = symTable;
		
	}
	
	
	public SymbolTable getClassSymbolTable()
	{
		return this.symbolTable;
	}
	
	@Override
	public String toString()
	{
		return String.format("Class : %s", this.id);
	}
}
