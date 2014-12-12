package IC.SymTables;

import java.util.HashMap;
import java.util.Map;

import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.StaticMethodSymbol;
import IC.SymTables.Symbols.Symbol;
import IC.SymTables.Symbols.VariableSymbol;
import IC.SymTables.Symbols.VirtualMethodSymbol;

public class ClassSymbolTable extends SymbolTable{

	/* more specific maps, allows us to handle static and instance scopes differently */
	private Map<String, FieldSymbol> fieldSymbols;
	private Map<String, StaticMethodSymbol> staticMethodSymbols;
	private Map<String, VirtualMethodSymbol> virtualMethodSymbols;
	
	public ClassSymbolTable(String id) {
		super(id);
		/* init maps */
		this.initMaps();
	}

	public ClassSymbolTable(String id, SymbolTable parentSymTable) {
		super(id, parentSymTable);
		/* init maps */
		this.initMaps();
		
	}
	
	/**
	 * initialize data structures
	 */
	private void initMaps()
	{
		fieldSymbols = new HashMap<String, FieldSymbol>();
		staticMethodSymbols = new HashMap<String, StaticMethodSymbol>();
		virtualMethodSymbols = new HashMap<String, VirtualMethodSymbol>();
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Class Symbol Table: %s", this.id);
	}
	
//	@Override
//	public void addSymbol(Symbol sym)
//	{
//		
//		/* add it to a more specific list, for fast lookup and static/instance scoping */
//		if(sym instanceof FieldSymbol)
//		{
//			fieldSymbols.put(sym.getId(), (FieldSymbol) sym);
//		}
//		else if (sym instanceof StaticMethodSymbol)
//		{
//			staticMethodSymbols.put(sym.getId(), (StaticMethodSymbol) sym);
//		}
//		else
//		{
//			virtualMethodSymbols.put(sym.getId(), (VirtualMethodSymbol) sym);
//		}
//	}
//	
	
	/**
	 * various setters for methods and fields
	 */
	
	public void addStaticMethod(StaticMethodSymbol method)
	{
		this.staticMethodSymbols.put(method.getId(), method);
	}
	
	public void addVirtualMethod(VirtualMethodSymbol method)
	{
		this.virtualMethodSymbols.put(method.getId(), method);
	}
	
	public void addField(FieldSymbol field)
	{
		this.fieldSymbols.put(field.getId(), field);
	}



	@Override
	public boolean resolveVariable(String id) {
		
		/* it may only be a field */
		if(this.fieldSymbols.containsKey(id))
			return true;
		
		/* try parent symbol table */
		return this.parentSymbolTable.resolveVariable(id);

	}

	@Override
	public boolean resolveMethod(String id, boolean virtualMethod) {
		
		if(virtualMethod)
		{
			if(this.virtualMethodSymbols.containsKey(id))
				return true;
		}
		
		if( this.staticMethodSymbols.containsKey(id))
			return true;
		
		/* otherwise, try parent symbol table */
		return this.parentSymbolTable.resolveMethod(id, virtualMethod);
		
	}

	@Override
	public VariableSymbol getVariable(String id) {
		/* may be only a field */
	
		
		return this.getField(id);
		
		
	}

	@Override
	public MethodSymbol getMethod(String id) {
		
		if(this.staticMethodSymbols.containsKey(id))
			return staticMethodSymbols.get(id);
		if(this.virtualMethodSymbols.containsKey(id))
			return virtualMethodSymbols.get(id);
		
		return this.parentSymbolTable.getMethod(id);
	}

	@Override
	public boolean resolveField(String id) {
		/* variable in a class may be only a field */
		return this.resolveVariable(id);
	}

	@Override
	public FieldSymbol getField(String id) {
		
		if(this.fieldSymbols.containsKey(id))
			return fieldSymbols.get(id);
		
		/* fetch from parent table */
		return this.parentSymbolTable.getField(id);
	}

	@Override
	public boolean containsLocally(String id) {
		
		/* check that one of the maps have the id */
		
		if(this.fieldSymbols.containsKey(id))
			return true;
		if(this.virtualMethodSymbols.containsKey(id))
			return true;
		if(this.staticMethodSymbols.containsKey(id))
			return true;
		return false;
	}

	//@Override
	//public Symbol getLocalSymbol(String id) {
//		
		/* check local scope only */
	//	if(this.fieldSymbols.containsKey(id))
	//		return this.fieldSymbols.get(id);
	//	
	//	if(this.virtualMethodSymbols.containsKey(id))
	//		return this.virtualMethodSymbols.get(id);
		
	//	if(this.staticMethodSymbols.containsKey(id))
	//		return this.staticMethodSymbols.get(id);
	//	
	//	return null;
	//}

	
	

}
