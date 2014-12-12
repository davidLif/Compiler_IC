package IC.SymTables;

import java.util.HashMap;
import java.util.Map;

import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;

import IC.SymTables.Symbols.ClassSymbol;


public class GlobalSymbolTable extends SymbolTable{

	private Map<String, ClassSymbol> classMap = new HashMap<String, ClassSymbol>();
	
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
	public Symbol getVariable(String id) {
	
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

	@Override
	public boolean containsLocally(String id) {
		
		if(this.classMap.containsKey(id))
			return true;
		return false;
	}

	//@Override
	//public Symbol getLocalSymbol(String id) {
	//	
	//	return null;
	//}

//	@Override
	//public void addSymbol(ClassSymbol sym) {
	//	
	//	this.classMap.put(sym.getId(), sym);
	//	
	//}
	
	/**
	 * add new class symbol to the table
	 * @param sym to add
	 */
	
	public void addClassSymbol(ClassSymbol sym)
	{
		this.classMap.put(sym.getId(), sym);
	}

	
	/**
	 * 
	 * return class symbol by class id
	 * 
	 * @param id
	 * @return class symbol with given id, or null if not found
	 */
	public ClassSymbol getClassSymbol(String id)	
	{
		if(this.containsLocally(id))
			return this.classMap.get(id);
		
		return null;
	}
	

	
}
