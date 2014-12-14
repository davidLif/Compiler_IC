package IC.SymTables.Symbols;

import IC.SymTables.ClassSymbolTable;
import IC.SymTables.SymbolTable;

public class ClassSymbol extends Symbol {

	/**
	 * the scope that is enclosed in this class
	 */
	private ClassSymbolTable symbolTable;
	
	public ClassSymbol(String id, ClassSymbolTable symTable) {
		super(id);
		this.symbolTable = symTable;
		this.kind = SymbolKind.CLASS;
	}
	
	
	public ClassSymbolTable getClassSymbolTable()
	{
		return this.symbolTable;
	}
	
	@Override
	public String toString()
	{
		return String.format("Class : %s", this.id);
	}
	
}
