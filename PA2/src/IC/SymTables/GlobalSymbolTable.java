package IC.SymTables;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



import IC.AST.VariableLocation;
import IC.SymTables.Symbols.ClassSymbol;
import IC.SymTables.Symbols.Symbol;


public class GlobalSymbolTable extends SymbolTable{

	/**
	 * this list holds all the class entries
	 */
	private Map<String, ClassSymbol> classList = new LinkedHashMap<String, ClassSymbol>();
	

	
	
	/**
	 * 
	 * @param id - name of file program
	 */
	public GlobalSymbolTable(String id) {
		super(id);
		
	}



	@Override
	public boolean containsLocally(String id) {
		
		if(this.getClassSymbol(id) == null)
			return false;
		return true;
	}


	
	/**
	 * add new class symbol to the table
	 * @param sym to add
	 */
	
	public void addClassSymbol(ClassSymbol sym)
	{
		
		this.classList.put(sym.getId(), sym);
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
		if(this.classList.containsKey(id))
			
			return this.classList.get(id);
		return null;
	}

	

	@Override
	public void printTable() {
		
		System.out.println(String.format("\nGlobal Symbol Table: %s", this.id));
		
		/* print body */
		
		for(ClassSymbol classSym : this.classList.values())
		{
			System.out.println("\t" + classSym.toString());
		}
		
		this.printChildernTables();
		
	}
	
	/**
	 * method returns an ordered collections of class symbols (ordered by insertion order )
	 * @return
	 */
	
	public Collection<ClassSymbol> getClassList()
	{
		return this.classList.values();
	}


	
	
	

	
}
