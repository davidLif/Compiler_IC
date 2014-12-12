package IC.SymTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.Method;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.StaticMethodSymbol;
import IC.SymTables.Symbols.Symbol;

import IC.SymTables.Symbols.VirtualMethodSymbol;

public class ClassSymbolTable extends SymbolTable{

	
	
	/* lists of possible symbols */
	private List<FieldSymbol>         fieldSymbolsList;
	private List<StaticMethodSymbol>  staticSymbolList;
	private List<VirtualMethodSymbol> virtualSymbolList;
	
	public ClassSymbolTable(String id) {
		super(id);
		
		/* init lists */
	
		this.initLists();
	}

	public ClassSymbolTable(String id, SymbolTable parentSymTable) {
		super(id, parentSymTable);
		
		/* init lists */
	
		this.initLists();
		
	}
	
	/**
	 * initialize data structures
	 */
	

	private void initLists()
	{
		this.fieldSymbolsList = new ArrayList<FieldSymbol>();
		this.staticSymbolList = new ArrayList<StaticMethodSymbol>();
		this.virtualSymbolList = new ArrayList<VirtualMethodSymbol>();
	}
	
	
	
	/**
	 * various setters for methods and fields
	 */
	
	public void addStaticMethod(StaticMethodSymbol method)
	{
		
		this.staticSymbolList.add(method);
	}
	
	public void addVirtualMethod(VirtualMethodSymbol method)
	{
		
		this.virtualSymbolList.add(method);
	}
	
	public void addField(FieldSymbol field)
	{
		
		this.fieldSymbolsList.add(field);
	}



	@Override
	public boolean resolveVariable(String id) {
		
		/* it may only be a field */
		for(Symbol sym : this.fieldSymbolsList)
		{
			if(sym.getId().equals(id))
				return true;
		}
		
		/* try parent symbol table */
		return this.parentSymbolTable.resolveVariable(id);

	}

	

	@Override
	public Symbol getVariable(String id) {
		/* may be only a field */
		
		return this.getField(id);
		
	}

	

	@Override
	public boolean resolveField(String id) {
		/* variable in a class may be only a field */
		return this.resolveVariable(id);
	}

	@Override
	public FieldSymbol getField(String id) {
		
		for(FieldSymbol sym : this.fieldSymbolsList)
		{
			if(sym.getId().equals(id))
				return sym;
		}
		
		/* fetch from parent table */
		return this.parentSymbolTable.getField(id);
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
		for(MethodSymbol sym : this.staticSymbolList)
		{
			if(sym.getId().equals(id))
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param id - name of method
	 * @return true iff current virtual scope already contains a method with given id
	 */
	
	public boolean containsLocallyVirtual(String id)
	{
		
		for(FieldSymbol sym : this.fieldSymbolsList)
		{
			if(sym.getId().equals(id))
				return true;
		}
			
		for(MethodSymbol sym : this.virtualSymbolList)
		{
			if(sym.getId().equals(id))
				return true;
		}
		
		return false;
		
	}



	@Override
	public void printTable() {
		
		/* print title */
		System.out.println(String.format("Class Symbol Table: %s", this.id));
		
		/* print body */
		
		for(FieldSymbol field : this.fieldSymbolsList)
		{
			System.out.println("\t" + field.toString());
		}
		
		for(StaticMethodSymbol staticMethod : this.staticSymbolList)
		{
			System.out.println("\t" + staticMethod.toString());
		}
		
		for(VirtualMethodSymbol virtualMethod : this.virtualSymbolList)
		{
			System.out.println("\t" + virtualMethod.toString());
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
		
		if(isStatic)
		{
			if(this.containsLocallyStatic(name))
			{
				return true;
			}
		}
		else
		{
			if(this.containsLocallyVirtual(name))
			{
				return true;
			}
		}
		
		if(this.parentSymbolTable instanceof ClassSymbolTable)
		{
			return ((ClassSymbolTable)this.parentSymbolTable).resolveMethod(name, isStatic);
		}
		
		/* parent is global scope, method was not found */
		
		return false;
	}

	/**
	 * 
	 * @param name
	 * @param isStatic
	 * @return
	 */
	
	public MethodSymbol getMethod(String name, boolean isStatic) {
		
		/* search the method in local scopes */
		
		if(isStatic)
		{
			for(MethodSymbol sym : this.staticSymbolList)
			{
				if(sym.getId().equals(name))
					return sym;
			}
		}
		else
		{
			for(MethodSymbol sym : this.virtualSymbolList)
			{
				if(sym.getId().equals(name))
					return sym;
			}
		}
		
		/* try parent */
		
		if(this.parentSymbolTable instanceof ClassSymbolTable)
		{
			return ((ClassSymbolTable)this.parentSymbolTable).getMethod(name, isStatic);
		}
		
		
		return null;
	}

	
	

	

}
