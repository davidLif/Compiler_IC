package IC.SymTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import IC.AST.VariableLocation;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.ParameterSymbol;
import IC.SymTables.Symbols.Symbol;
import IC.Types.ClassType;
import IC.Types.MethodType;
import IC.Types.Type;



public class MethodSymbolTable extends VariableSymbolTable{
	
	/**
	 * flags holds whether this method scope is static or virtual
	 */
	
	private boolean isStatic;

	/**
	 * map contains all the parameters of the method
	 */
	
	private Map<String, ParameterSymbol> paramsList = new LinkedHashMap<String, ParameterSymbol>();
	
	
	
	
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
		if( paramsList.containsKey(id))
			return true;
		
		return false;
	}
	


	
	@Override
	public Symbol getVariableLocally(String id) {
		
		
		/* check params */
		if(paramsList.containsKey(id))
		{
			return paramsList.get(id);
		}
		
		/* check local vars  */
		if(super.containsLocally(id))
			return super.getVariableLocally(id);
		
		return null;
		
	
	}
	
	@Override
	public boolean resolveVariable(VariableLocation varLocation)
	{
		
		
		/* check if local scope contains the variable */
		if(paramsList.containsKey(varLocation.getName()) || this.localVarsList.containsKey(varLocation.getName()))
		{
			// if so, return true, but also set defining scope
			varLocation.setDefiningScope(this);
			return true;
		}
		
		/* try enclosing classes, if virtual method */
		
		if(this.isStatic)
			/* no static fields */
			return false;
		
		return ((ClassSymbolTable)this.parentSymbolTable).resolveField(varLocation);
		
		
	}
	

	/** 
	 * add method parameter symbol
	 * @param sym
	 */
	public void addParameter(ParameterSymbol sym)
	{
		this.paramsList.put(sym.getId(), sym);
	}

	

	@Override
	public void printTable() {

		System.out.print(String.format("Method Symbol Table: %s\n", this.id));
		
		for(Symbol sym : this.paramsList.values())
		{
			System.out.print("\t" + sym.toString() + "\n");
		}
		
		for(Symbol sym : this.localVarsList.values())
		{
			System.out.print("\t" + sym.toString() + "\n");
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
		
		if(isStatic) /* look only for a static method */
			return enclosingClass.getMethod(name, isStatic);
		
		/* otherwise, look for the static method first */
		MethodSymbol staticMethod = enclosingClass.getMethod(name, true);
		if(staticMethod == null)
		{
			/* static not found, try to find a virtual */
			MethodSymbol virtualMethod = enclosingClass.getMethod(name, false);
			return virtualMethod;
		}
		return staticMethod;
	}




	@Override
	public ClassType getThisType() {
		
		ClassSymbolTable enclosingClass = (ClassSymbolTable)parentSymbolTable;
		/* retrieve information from parent class */
		return (ClassType) enclosingClass.getField("this").getType();
	}




	@Override
	public Type getReturnType() {
		
		ClassSymbolTable enclosingClass = (ClassSymbolTable)parentSymbolTable;
		/* get current method symbol */
		MethodSymbol currMethodSym = enclosingClass.getMethod(this.id, isStatic);
		
		return ((MethodType) currMethodSym.getType()).getReturnType();
	}
	


	
	

}
