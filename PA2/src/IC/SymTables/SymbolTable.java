/**
 * 
 */
package IC.SymTables;

import java.util.Map;

/**
 * @author Denis
 *
 */
public abstract class SymbolTable {

	/**
	 * maps from identifiers to corresponding table entries
	 */
	private Map<String, Symbol> entires;  
	/**
	 * name of the scope (name of class, method, .. )
	 */
	private String id;
	
	/**
	 * a referece to the enclosing symbol table
	 */
	private SymbolTable parentSymbolTable;
	
	public SymbolTable(String id)
	{
		this.id = id;
		entries = new HashMap<String, Symbol>();
	}
	
}
