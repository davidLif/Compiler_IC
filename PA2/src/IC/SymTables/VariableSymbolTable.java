package IC.SymTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.Method;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;

import IC.SymTables.Symbols.LocalVariableSymbol;;


/**
 * 
 * @author Denis
 *
 * this abstract class represents a symbol table that contains variable symbols and does not contain fields and methods
 * for example : method symbol table, statement block symbol table
 *
 */
public abstract class VariableSymbolTable  extends SymbolTable{

	/**
	 * list saved local variable symbols
	 */
	
	
	protected List<LocalVariableSymbol> localVarsList = new ArrayList<LocalVariableSymbol>();
	
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
	public boolean containsLocally(String id) {
		
		for(Symbol sym : this.localVarsList)
		{
			if(sym.getId().equals(id))
				return true;
		}
		return false;
		
	}


	@Override
	public Symbol getVariable(String id) {
		
		for(Symbol sym : this.localVarsList)
		{
			if(sym.getId().equals(id))
				return sym;
		}
		
		/* try parent */
		return this.parentSymbolTable.getVariable(id);
	}
	
	
	/**
	 * method adds new local variable
	 * @param sym
	 */
	public void addLocalVariable(LocalVariableSymbol sym)
	{
		
		this.localVarsList.add(sym);
	}
	
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	
	public boolean resolveMethod(String name) {
		
		
		return ((VariableSymbolTable)this.parentSymbolTable).resolveMethod(name);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */

	public MethodSymbol getMethod(String name) {
		
		
		return ((VariableSymbolTable)this.parentSymbolTable).getMethod(name);
	}
	
	


}
