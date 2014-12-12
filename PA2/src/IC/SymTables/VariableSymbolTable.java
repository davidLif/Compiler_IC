package IC.SymTables;

import java.util.HashMap;
import java.util.Map;

import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;

import IC.SymTables.Symbols.LocalVariableSymbol;;


/**
 * 
 * @author Denis
 *
 * this abstract class represents a symbol table that contains variable symbols and does not contain fields and methods
 * 
 *
 */
public abstract class VariableSymbolTable  extends SymbolTable{

	/**
	 * maps string ids to local variables
	 */
	
	protected Map<String, LocalVariableSymbol> localVariables = new HashMap<String, LocalVariableSymbol>();
	
	/**
	 * 
	 * @param id of scope
	 */
	
	public VariableSymbolTable(String id) {
		
		super(id);
		
	}
	
	public VariableSymbolTable(String id, SymbolTable parentSymbolTable) {
		
		super(id, parentSymbolTable);
		
	}
	
	

	@Override
	public MethodSymbol getMethod(String id) {
		
		return this.parentSymbolTable.getMethod(id);
	}

	@Override
	public boolean resolveField(String id) {
		
		return this.parentSymbolTable.resolveField(id);
	}

	@Override
	public FieldSymbol getField(String id) {
		
		return this.parentSymbolTable.getField(id);
	}
	
	@Override
	public boolean resolveVariable(String id) {
		/* may be a local variable or a parameter or a field */
		
		if(this.containsLocally(id))
				return true; 
		
		/* otherwise, try parent */
		return this.parentSymbolTable.resolveVariable(id);
	}

	@Override
	public boolean resolveMethod(String id, boolean virtualMethod) {
		
		/* only parent symbol table may contain methods */
		return this.parentSymbolTable.resolveMethod(id, virtualMethod);
	}
	
	@Override
	public boolean containsLocally(String id) {
		
		return this.localVariables.containsKey(id);
		
	}


	@Override
	public Symbol getVariable(String id) {
		
		if(this.localVariables.containsKey(id))
			return this.localVariables.get(id);
		
		/* try parent */
		return this.parentSymbolTable.getVariable(id);
	}
	
	
	/**
	 * method adds new local variable
	 * @param sym
	 */
	public void addLocalVariable(LocalVariableSymbol sym)
	{
		this.localVariables.put(sym.getId(), sym);
	}
	

}
