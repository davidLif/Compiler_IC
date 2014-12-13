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
	
	public Symbol getVariable(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	


	
	
}
