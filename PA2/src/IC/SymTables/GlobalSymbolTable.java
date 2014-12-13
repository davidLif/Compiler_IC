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

import IC.SymTables.Symbols.ClassSymbol;


public class GlobalSymbolTable extends SymbolTable{

	/**
	 * this list holds all the class entries
	 */
	private List<ClassSymbol> classList = new ArrayList<ClassSymbol>();
	
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
		
		this.classList.add(sym);
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
		for(ClassSymbol sym : this.classList)
		{
			if(sym.getId().equals(id))
				return sym;
		}
		
		return null;
	}

	

	@Override
	public void printTable() {
		
		System.out.println(String.format("Global Symbol Table: %s", this.id));
		
		/* print body */
		
		for(ClassSymbol classSym : this.classList)
		{
			System.out.println("\t" + classSym.toString());
		}
		
		this.printChildernTables();
		
	}


	

	
}
