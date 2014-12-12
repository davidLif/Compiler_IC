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
		
		if(super.containsLocally(id))
		{
			/* id is a local variable and not a parameter */
			return super.getVariable(id);
		}
		
		/* try parent */
		return this.parentSymbolTable.getVariable(id);
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



	@Override
	public boolean resolveMethod(String name)
	{
		
		ClassSymbolTable enclosingClass = (ClassSymbolTable)parentSymbolTable;
		return enclosingClass.resolveMethod(name, isStatic);
		
	}
	
	@Override
	public MethodSymbol getMethod(String name)
	{
		ClassSymbolTable enclosingClass = (ClassSymbolTable)parentSymbolTable;
		return enclosingClass.getMethod(name, isStatic);
	}
	
	@Override
	public boolean resolveVariable(String id) {
		/* may be a local variable or a parameter or a field */
		
		if(this.containsLocally(id))
				return true; 
		
		/* otherwise, try parent if not static ! */
		if(this.isStatic)
		{
			// no static fields
			return false;
		}
		
		return this.parentSymbolTable.resolveField(id);
	
		
	}

	
	

}
