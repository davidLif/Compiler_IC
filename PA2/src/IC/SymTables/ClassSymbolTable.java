package IC.SymTables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import IC.AST.Method;
import IC.AST.VariableLocation;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.StaticMethodSymbol;
import IC.SymTables.Symbols.Symbol;

import IC.SymTables.Symbols.VirtualMethodSymbol;

public class ClassSymbolTable extends SymbolTable{

	
	
	/* maps (ordered) of possible symbols */
	private Map<String, FieldSymbol>         fieldSymbolsList;   /* fields */
	private Map<String, StaticMethodSymbol>  staticSymbolList;   /* static methods */
	private Map<String, VirtualMethodSymbol> virtualSymbolList;  /* virtual methods */
	
	public ClassSymbolTable(String id) {
		super(id);
		
		/* init lists */
	
		this.initMaps();
	}

	public ClassSymbolTable(String id, SymbolTable parentSymTable) {
		super(id, parentSymTable);
		
		/* init lists */
	
		this.initMaps();
		
	}
	
	/**
	 * initialize data structures
	 */
	

	private void initMaps()
	{
		this.fieldSymbolsList = new LinkedHashMap<String, FieldSymbol>();
		this.staticSymbolList = new LinkedHashMap<String ,StaticMethodSymbol>();
		this.virtualSymbolList = new LinkedHashMap<String, VirtualMethodSymbol>();
	}
	
	
	
	/**
	 * various setters for methods and fields
	 */
	
	public void addStaticMethod(StaticMethodSymbol method)
	{
		
		this.staticSymbolList.put(method.getId(), method);
	}
	
	public void addVirtualMethod(VirtualMethodSymbol method)
	{
		
		this.virtualSymbolList.put(method.getId(), method);
	}
	
	public void addField(FieldSymbol field)
	{
		
		this.fieldSymbolsList.put(field.getId(), field);
	}



	
	/**
	 * Method tries to find given field, returns true iff found in current class scope or superclass scope
	 * @param varLoc - field to search
	 * @return true iff the field is found in enclosing scopes
	 *         
	 * NOTE: 
	 *       This method will set the defining scope of varLoc if found in current scope
	 *       >> use this method only when building the symbol tables <<
	 */

	public boolean resolveField(VariableLocation varLoc) {

		if(this.fieldSymbolsList.containsKey(varLoc.getName()))
		{
			varLoc.setDefiningScope(this);
			return true;
		}
		
		if(this.parentSymbolTable instanceof ClassSymbolTable)
		{
			return ((ClassSymbolTable)parentSymbolTable).resolveField(varLoc);
		}
		
		/* field not found */
		return false;
	}
	
	
	/**
	 * method returns true iff this scope or super scope contain a field with given id
	 * @param id - if of method to search
	 * @return true if found, false o/w
	 */
	public boolean resolveField(String id)
	{
		return this.getField(id) != null;
	}

	
	/**
	 * Method tries to find given field, returns the field if found, o/w returns null
	 * method will also search parent scopes
	 * 
	 * Note: if id is "this", a field will be returned, containing the type of current class
	 * 
	 * @param id
	 * @return FieldSymbol if found, null otherwise
	 */
	public FieldSymbol getField(String id) {
		
		
		if(id.equals("this"))
		{
			/* in case of "this", return a field representing the type of class */
			
			GlobalSymbolTable globSymTable = getGlobalSymTable();
			FieldSymbol thisSym = new FieldSymbol(id);
			thisSym.setType(globSymTable.getClassSymbol(this.id).getType());
			return thisSym;
		}
		
		if(this.fieldSymbolsList.containsKey(id))
		{
			return fieldSymbolsList.get(id);
		}
		
		/* fetch from parent table */
		
		if(this.parentSymbolTable instanceof ClassSymbolTable)
			return ((ClassSymbolTable)this.parentSymbolTable).getField(id);
		
		/* field not found */
		return null;
	}
	
	
	/**
	 * this method retrieves the global symbol table
	 * @return
	 */
	private GlobalSymbolTable getGlobalSymTable()
	{
		SymbolTable scope = this.parentSymbolTable;
		while(scope instanceof ClassSymbolTable)
			scope = scope.getParentSymbolTable();
		return (GlobalSymbolTable)scope;
	}

	@Override
	public boolean containsLocally(String id) {
		
		/* check that one of the lists have the id */

		return this.containsLocallyStatic(id) || this.containsLocallyVirtual(id);
	}
	
	
	/**
	 * 
	 * @param id - name of method
	 * @return true iff current LOCAL static scope already contains a method with given id
	 */
	public boolean containsLocallyStatic(String id)
	{
		return getStaticMethod(id) != null;
	}
	
	/**
	 * 
	 * @param id - name of method
	 * @return true iff current virtual scope already contains a method with given id
	 */
	
	public boolean containsLocallyVirtual(String id)
	{
		
		return getFieldSymById(id) != null || getVirtualMethod(id) != null;
		
		
	}



	@Override
	public void printTable() {
		
		/* print title */
		System.out.print(String.format("Class Symbol Table: %s\n", this.id));
		
		/* print body */
		
		for(FieldSymbol field : this.fieldSymbolsList.values())
		{
			System.out.print("\t" + field.toString() + "\n");
		}
		
		for(StaticMethodSymbol staticMethod : this.staticSymbolList.values())
		{
			System.out.print("\t" + staticMethod.toString() + "\n");
		}
		
		for(VirtualMethodSymbol virtualMethod : this.virtualSymbolList.values())
		{
			System.out.print("\t" + virtualMethod.toString() + "\n");
		}
		
		this.printChildernTables();
		
	}

	
	/**
	 * 
	 * @param name
	 * @param isStatic
	 * @return
	 */
	
	public boolean resolveMethod(String name, boolean isStatic) {
		
		return this.getMethod(name,  isStatic) != null;
	}

	/**
	 * 
	 * @param name
	 * @param isStatic
	 * @return
	 */
	
	public MethodSymbol getMethod(String name, boolean isStatic) {
		
		/* search the method in local scopes */
		MethodSymbol res = null; 
		if(isStatic)
		{
			res = getStaticMethod(name);
		}
		else
		{
			res = getVirtualMethod(name);
			
		}
		
		/* check if found in local scope */
		if(res != null)
			return res;
		
		/* try parent */
		
		if(this.parentSymbolTable instanceof ClassSymbolTable)
		{
			return ((ClassSymbolTable)this.parentSymbolTable).getMethod(name, isStatic);
		}
		
		
		return null;
	}


	
	/**
	 * 
	 * this method returns field symbol with given id, only from local scope (not from parents)
	 * 
	 * @param id - name of field symbol
	 * @return the symbol if found, null o/w.
	 */
	public FieldSymbol getFieldSymById(String id)
	{
		if(this.fieldSymbolsList.containsKey(id))
			return this.fieldSymbolsList.get(id);
		return null;
	}
	
	/**
	 * method returns virtual method with given id from local scope, returns null if not found
	 * @param id
	 * @return
	 */
	
	public MethodSymbol getVirtualMethod(String id)
	{
		if( this.virtualSymbolList.containsKey(id))
			return this.virtualSymbolList.get(id);
		
		return null;
	}
	
	/**
	 * method returns static method symbol with given id from local scope, returns null if not found
	 * @param id
	 * @return
	 */
	
	public MethodSymbol getStaticMethod(String id)
	{
		if( this.staticSymbolList.containsKey(id))
			return this.staticSymbolList.get(id);
		return null;
	}
	
	
	

	

}
