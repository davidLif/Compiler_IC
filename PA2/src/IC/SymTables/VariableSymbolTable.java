package IC.SymTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import IC.AST.Method;
import IC.AST.VariableLocation;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;

import IC.SymTables.Symbols.LocalVariableSymbol;
import IC.Types.ClassType;
import IC.Types.MethodType;
import IC.Types.Type;


/**
 * 
 * @author Denis
 *
 * this abstract class represents a symbol table that contains variable symbols and does not contain fields and methods
 * for example : method symbol table, statement block symbol table
 * the parent table will always be a ClassSymbolTable or VariableSymbolTable
 *
 */
public abstract class VariableSymbolTable  extends SymbolTable{

	/**
	 * map of local variable symbols
	 */
	
	
	protected Map<String, LocalVariableSymbol> localVarsList = new LinkedHashMap<String, LocalVariableSymbol>();
	
	/**
	 * 
	 * @param id of scope
	 */
	
	public VariableSymbolTable(String id) {
		
		super(id);
		
	}
	
	/**
	 * 
	 * @param id of scope
	 * @param parentSymbolTable enclosing scope
	 */
	public VariableSymbolTable(String id, SymbolTable parentSymbolTable) {
		
		super(id, parentSymbolTable);
		
	}
	
	
	/**
	 * this method will resolve variable (local var, param or field )
	 * returns true iff found in this scope and enclosing scopes, otherwise false
	 * 
	 * IMPORTANT: if varLoc is resolved in current scope, defining scope of varLoc will be set to this
	 *            >> this method should be used ONLY when building the symbol tables  <<
	 * 
	 * @param varLoc - non external local variable location
	 * @return true iff found recursively, plus sets the defining scope
	 */
	public abstract boolean resolveVariable(VariableLocation varLoc); 
	
	
	@Override
	public boolean containsLocally(String id) {
		
		return this.localVarsList.containsKey(id);
		
	}

	/**
	 * 
	 * this method returns variable with given id FROM LOCAL SCOPE
	 * 
	 * 
	 * @param id - of variable to look for
	 * @return the symbol if found, null otherwise
	 */
	
	public Symbol getVariableLocally(String id)
	{
		if(this.localVarsList.containsKey(id))
		{
			return this.localVarsList.get(id);
		}
		return null;
		
	}
		

	
	/**
	 * method adds new local variable
	 * @param sym
	 */
	public void addLocalVariable(LocalVariableSymbol sym)
	{
		
		this.localVarsList.put(sym.getId(), sym);
	}
	
	
	/**
	 * method resolves a method with given id
	 * @param name - name of method
	 * @return true iff the method is found in the upper scopes
	 */
	
	public boolean resolveMethod(String name) {
		
		
		return getMethod(name) != null;
	}

	/**
	 * method retrieves the method from upper scopes corresponding to this name
	 * @param name
	 * @return method symbol if method is found, null o/w
	 */

	public abstract MethodSymbol getMethod(String name) ;
	
	
	
	/**
	 * this method returns the enclosing class type  ("this" expression type)
	 * @return method type of enclosing class 
	 */
	public abstract ClassType getThisType();


	/**
	 * this method returns the return type of enclosing method (static or virtual)
	 * @return method type
	 */
	
	public abstract Type getReturnType();
	
}
