package IC.SymTables;

import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.VariableSymbol;



/**
 * 
 * @author Denis
 *
 * this abstract class represents a symbol table that contains only variable symbols such as parameters and local vars
 * requests to receive method symbols or field methods will be passed to the parent table
 *
 */
public abstract class VariableSymbolTable  extends SymbolTable{

	
	
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
	public VariableSymbol getVariable(String id) {
		
		if(this.containsLocally(id))
			return (VariableSymbol)this.entries.get(id);
		
		return this.parentSymbolTable.getVariable(id);
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
	

}
