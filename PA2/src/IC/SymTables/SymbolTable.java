/**
 * 
 */
package IC.SymTables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;


/**
 * @author Denis
 *
 */
public abstract class SymbolTable {

	
	/**
	 * maps from identifiers to corresponding table entries.
	 * a string key is sufficient, since all symbols must have different ids in this symbol table
	 */
	//protected Map<String, Symbol> entries;  
	
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
		childrenTables = new ArrayList<SymbolTable>();
		this.parentSymbolTable = null;
		//entries = new HashMap<String, Symbol>();
	}
	
	public SymbolTable(String id, SymbolTable parentSymbolTable)
	{
		this(id);
		this.parentSymbolTable = parentSymbolTable;
		//entries = new HashMap<String, Symbol>();
	}
	
	
	/**
	 * method returns the list of childern symbol tables of current symbol table
	 * @return
	 */
	public List<SymbolTable> getChildrenTables()
	{
		return childrenTables;
	}
	
	
	/**
	 *  this method returns a child symbol table with given id
	 *  if no such child is found, null is returned.
	 */
	public SymbolTable getChildSymbolTableById(String id)
	{
		for(int i = 0; i < this.childrenTables.size(); i ++)
		{
			if ( childrenTables.get(i).getId().equals(id) )
				return childrenTables.get(i);
		}
		
		return null;
	}
	

	
	/**
	 * this method searches the CURRENT scope for the given identifier
	 * 
	 * @param id - identifier of symbol
	 * @return true iff symbol is in current scope (current symbol table)
	 */
	public abstract boolean containsLocally(String id);


	
	
	
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
		sb.append(this.getSymbolTableHeader() + "\n");
		/* add body */
		//for(Symbol sym : this.entries.values())
		//{
		//	sb.append("\t" + sym.toString() + "\n");
		//}
		/* add footer */
		sb.append("Children tables: \n");
		
		for(int i = 0; i < this.childrenTables.size(); ++i)
		{
			sb.append(childrenTables.get(i).getId());
			// not last child
			if( i  < childrenTables.size() - 1)
			{
				sb.append(",");
			}
		}
		
		for(int i = 0; i < this.childrenTables.size(); ++i)
		{
			sb.append("\n");
			sb.append(childrenTables.get(i).toString());
			// not last child
			if( i  < childrenTables.size() - 1)
			{
				sb.append("\n");
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
	
	
	/**
	 * method returns true iff the symbol table or its parents contain a VariableSymbol named id (may be a field, parameter or local variable)
	 * (variable may be a field, a local variable or a method parameter ), useful for low level scopes
	 * @param id - id of variable to resolve
	 * @return
	 */
	public abstract boolean resolveVariable(String id);
	
	/**
	 * Return variable with given id, according to the specific logic of the current symbol table
	 * (variable may be a field, a local variable or a method parameter ), useful for low level scopes
	 * @param id - variable name
	 * @return the desired variable, or null if not found
	 */
	
	
	
	public abstract Symbol getVariable(String id);
	
	
	/**
	 * method returns true iff the symbol table or its parents contain a MethodSymbol with given id, and its scope matched virtualMethod
	 * @param id - id of the method you're looking for
	 * @param virtualMethod - set true if you're looking for a virtual method, otherwise, set false
	 * @return
	 */
	
	public abstract boolean resolveMethod(String id, boolean virtualMethod);
	
	/**
	 * Return method with given id, according to the specific logic of the current symbol table
	 * @param id - method name
	 * @return the desired method, or null if not found
	 */
	
	public abstract MethodSymbol getMethod(String id);
	
	/**
	 * 
	 * @param id - name of field
	 * @return true iff the table symbol or its parents contain a field named id
	 */
	public abstract boolean resolveField(String id);
	
	/**
	 * Return field with given id, according to the specific logic of the current symbol table
	 * @param id - field name
	 * @return the desired field, or null if not found
	 */
	
	public abstract FieldSymbol getField(String id);

	
}
