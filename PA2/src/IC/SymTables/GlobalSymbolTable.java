package IC.SymTables;

import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.VariableSymbol;


public class GlobalSymbolTable extends SymbolTable{

	public GlobalSymbolTable(String id) {
		super(id);
		
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Global Symbol Table: %s", this.id);
	}

	@Override
	public boolean resolveVariable(String id) {
		
		/* this symbol table does not contain variable symbols */
		return false;
	}

	@Override
	public boolean resolveMethod(String id, boolean virtualMethod) {
		/* this symbol table does not contain method symbols */
		return false;
	}

	@Override
	public VariableSymbol getVariable(String id) {
	
		return null;
	}

	@Override
	public MethodSymbol getMethod(String id) {
	
		return null;
	}

	@Override
	public boolean resolveField(String id) {
		
		return false;
	}

	@Override
	public FieldSymbol getField(String id) {
		
		return null;
	}


	
	
	

	
}
