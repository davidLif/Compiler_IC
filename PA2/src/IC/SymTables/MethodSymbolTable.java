package IC.SymTables;

import java.util.HashMap;
import java.util.Map;

import IC.SymTables.Symbols.LocalVariableSymbol;
import IC.SymTables.Symbols.ParameterSymbol;
import IC.SymTables.Symbols.VariableSymbol;

public class MethodSymbolTable extends VariableSymbolTable{
	
	
	/* maps for paramaters and local variables */
	private Map<String, ParameterSymbol> params = new HashMap<String, ParameterSymbol>();
	
	public MethodSymbolTable(String id) {
		super(id);
	
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Method Symbol Table: %s", this.id);
	}

	@Override
	public boolean containsLocally(String id) {
		
		/* check local variables */
		if(this.localVariables.containsKey(id))
			return true;
		
		/* check params */
		
		if(this.params.containsKey(id))
			return true;
		
		return false;
	}
	

	@Override
	public VariableSymbol getVariable(String id) {
		
		if(params.containsKey(id))
			return this.params.get(id);
		
		if(this.localVariables.containsKey(id))
			return this.localVariables.get(id);
		
		return this.parentSymbolTable.getVariable(id);
	}

	/** 
	 * add method parameter symbol
	 * @param sym
	 */
	public void addParameter(ParameterSymbol sym)
	{
		this.params.put(sym.getId(), sym);
	}
	
	
	

}
