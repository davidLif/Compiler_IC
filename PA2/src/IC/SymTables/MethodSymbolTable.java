package IC.SymTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.Method;
import IC.SymTables.Symbols.LocalVariableSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.ParameterSymbol;
import IC.SymTables.Symbols.Symbol;


public class MethodSymbolTable extends VariableSymbolTable{
	
	/**
	 * flags holds whether this method scope is static or virtual
	 */
	
	private boolean isStatic;

	/**
	 * list contains all the parameters of the method
	 */
	
	private List<ParameterSymbol> paramsList = new ArrayList<ParameterSymbol>();
	
	public MethodSymbolTable(String id, boolean isStatic) {
		
		super(id);
		this.isStatic = isStatic;
		
	
	}
	
	
	

	@Override
	public boolean containsLocally(String id) {
		
		/* check local variables */
		if(super.containsLocally(id))
			return true;
		
		/* check params */
		
		for(Symbol sym : paramsList)
		{
			if(sym.getId().equals(id))
				return true;
		}
		
		return false;
	}
	

	@Override
	public Symbol getVariable(String id) {
		
		/* check params */
		for(Symbol sym : paramsList)
		{
			if(sym.getId().equals(id))
				return sym;
		}
		
		/* check local vars and parents */
		
		for(Symbol sym : this.localVarsList)
		{
			if(sym.getId().equals(id))
				return sym;
		}
		
		/* enclosing scope is a class, may be a field */
		if(this.isStatic)
			return null;
		
		return ((ClassSymbolTable)this.parentSymbolTable).getField(id);
		
	
	}

	/** 
	 * add method parameter symbol
	 * @param sym
	 */
	public void addParameter(ParameterSymbol sym)
	{
		this.paramsList.add(sym);
	}

	

	@Override
	public void printTable() {

		System.out.println(String.format("Method Symbol Table: %s", this.id));
		
		for(Symbol sym : this.paramsList)
		{
			System.out.println("\t" + sym.toString());
		}
		
		for(Symbol sym : this.localVarsList)
		{
			System.out.println("\t" + sym.toString());
		}
		
		this.printChildernTables();
	}


	/**
	 * in this case, we know if current scope is static or not, so we're looking for a method
	 * that must be isStatic like current scope
	 */

	
	@Override
	public MethodSymbol getMethod(String name)
	{
		ClassSymbolTable enclosingClass = (ClassSymbolTable)parentSymbolTable;
		return enclosingClass.getMethod(name, isStatic);
	}
	


	
	

}
