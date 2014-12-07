/**
 * 
 */
package IC.SymTables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denis
 *
 */
public abstract class SymbolTable {

	/**
	 * maps from identifiers to corresponding table entries
	 */
	protected Map<String, Symbol> entries;  
	/**
	 * name of the scope (name of class, method, .. )
	 */
	protected String id;
	
	/**
	 * a reference to the enclosing symbol table
	 */
	protected SymbolTable parentSymbolTable;
	
	/**
	 * list of children types (sub scopes)
	 */
	protected List<SymbolTable> childrenTables;
	
	public SymbolTable(String id)
	{
		this.id = id;
		entries = new HashMap<String, Symbol>();
		childrenTables = new ArrayList<SymbolTable>();
		this.parentSymbolTable = null;
	}
	
	public SymbolTable(String id, SymbolTable parentSymbolTable)
	{
		this(id);
		this.parentSymbolTable = parentSymbolTable;
	}
	
	/**
	 * method adds given symbol to the table
	 * @param sym
	 */
	public void addSymbol(Symbol sym)
	{
		entries.put(sym.id, sym);
	}
	
	/**
	 * this method searches the CURRENT scope for the given identifier
	 * 
	 * @param id - identifier of symbol
	 * @return true iff symbol is in current scope (current symbol table)
	 */
	public boolean containsLocally(String id)
	{
		return this.entries.containsKey(id);
	}
	
	/**
	 * this method returns a symbol from the SymbolTable, or its parents, whatever is found first
	 * @param id - id of the symbol
	 * @return symbol
	 */
	
	public Symbol getSymbol(String id)
	{
		if(this.containsLocally(id))
		{
			return this.entries.get(id);
		}
		// not found locally, fetch from parent scopes
		return this.parentSymbolTable.getSymbol(id);
	}
	
	/**
	 * 
	 * @return true iff this symbol table is a sub-scope of another scope
	 */
	public boolean hasParent()
	{
		return parentSymbolTable != null;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		/* add title */
		sb.append(this.getSymbolTableHeader());
		/* add body */
		for(Symbol sym : this.entries.values())
		{
			sb.append("\t" + sym.toString());
		}
		/* add footer */
		sb.append("Children tables: ");
		
		for(int i = 0; i < this.childrenTables.size(); ++i)
		{
			sb.append(childrenTables.get(i).getId());
			// not last child
			if( i  < childrenTables.size() - 1)
			{
				sb.append(",");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @return symbol table header(title) representation
	 * for example: Class Symbol Table: A
	 *              Statement Block Symbol Table ( located in sfunc )
	 */
	
	protected abstract String getSymbolTableHeader();

	
	
	/**
	 * 
	 * @return sym table's string id, for example: Library, print, ..
	 */
	
	public String getId()
	{
		return this.id;
	}
	
	/**
	 * 
	 * @param child symbol table to add to the children list of this symbol table
	 */
	public void addChildTable(SymbolTable child)
	{
		this.childrenTables.add(child);
	}
	
	/**
	 * set enclosing scope
	 * @param parent symbol table
	 */
	
	
	public void setParentSymbolTable(SymbolTable parent)
	{
		this.parentSymbolTable = parent;
	}
}
