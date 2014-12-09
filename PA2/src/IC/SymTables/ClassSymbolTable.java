package IC.SymTables;

import java.util.HashMap;
import java.util.Map;

public class ClassSymbolTable extends SymbolTable{

	
	private Map<String, FieldSymbol> fieldSymbols;
	private Map<String, StaticMethodSymbol> staticMethodSymbols;
	private Map<String, VirtualMethodSymbol> virtualMethodSymbols;
	
	public ClassSymbolTable(String id) {
		super(id);
		fieldSymbols = new HashMap<String, FieldSymbol>();
		staticMethodSymbols = new HashMap<String, StaticMethodSymbol>();
		virtualMethodSymbols = new HashMap<String, VirtualMethodSymbol>();
	}

	public ClassSymbolTable(String id, SymbolTable parentSymTable) {
		super(id, parentSymTable);
	}

	@Override
	protected String getSymbolTableHeader() {
		
		return String.format("Class Symbol Table: %s", this.id);
	}
	
	@Override
	public void addSymbol(Symbol sym)
	{
		super.addSymbol(sym); 
		
		/* add it to a more specific list, for fast lookup and static/instance scoping */
		if(sym instanceof FieldSymbol)
		{
			fieldSymbols.put(sym.getId(), (FieldSymbol) sym);
		}
		else if (sym instanceof StaticMethodSymbol)
		{
			staticMethodSymbols.put(sym.getId(), (StaticMethodSymbol) sym);
		}
		else
		{
			virtualMethodSymbols.put(sym.getId(), (VirtualMethodSymbol) sym);
		}
	}

	//@Override
	public boolean resolveSymbol(Symbol sym) {
		
		/* we need to decide if sym is a variable or method symbol (virtual or static) */
		
		if(sym instanceof VariableSymbol)
		{
			// in our case, check if sym is a field
			if(fieldSymbols.containsKey(sym.getId()))
				return true;
				
		}
		
		// else, sym is of instance method
		if(sym instanceof VirtualMethodSymbol)
		{
			if(virtualMethodSymbols.containsKey(sym.getId()))
				return true;
			
		}
		else
		{
			// static method
			if(staticMethodSymbols.containsKey(sym.getId()))
				return true;
		}
		
		// try parent
		/*if(this.parentSymbolTable.resolveSymbol(sym))
			return true;*/
		
		return false;
		
	}

	
	

}
