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
import IC.AST.Method;


/**
 * @author Denis
 *
 */
public abstract class SymbolTable {

	
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
		
	}
	
	public SymbolTable(String id, SymbolTable parentSymbolTable)
	{
		this(id);
		this.parentSymbolTable = parentSymbolTable;
		
	}
	
	
	/**
	 * method returns the list of children symbol tables of current symbol table
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
	
	
	/**
	 * this method prints the symbol table and its childern
	 */
	public abstract void printTable();
	
	/**
	*  this method prints the footer of the symbol table
	*/
	
	protected void printChildernTables()
	{
		
		if(this.childrenTables.isEmpty())
			return;
		
		System.out.print("Children tables: ");
		for(int i = 0; i < this.childrenTables.size(); ++i)
		{
			System.out.print(childrenTables.get(i).getId());
			
			// if not last child
			if( i  < childrenTables.size() - 1)
				System.out.print(", ");
			else
				System.out.print("\n");
		}
		
		/*  print child tables */
	
		for( int i = 0; i < this.childrenTables.size(); ++i)
		{
			System.out.print("\n");
			childrenTables.get(i).printTable();
		}
		
		
	}
	
	
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
