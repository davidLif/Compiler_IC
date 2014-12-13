package IC.SymTables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.Method;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;

import IC.SymTables.Symbols.LocalVariableSymbol;;


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
	 * list of local variable symbols
	 */
	
	
	protected List<LocalVariableSymbol> localVarsList = new ArrayList<LocalVariableSymbol>();
	
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
	 * this method will resolve variable with given id (local var, param or field )
	 * returns true iff found in this scope and enclosing scopes, otherwise false
	 * @param id - to look for
	 * @return true iff found recursively
	 */
	public boolean resolveVariable(String id) {
		
		/* may be a local variable or a parameter or a field */
		
		return this.getVariable(id) != null;
	}

	
	
	@Override
	public boolean containsLocally(String id) {
		
		for(Symbol sym : this.localVarsList)
		{
			if(sym.getId().equals(id))
				return true;
		}
		return false;
		
	}

	/**
	 * this method will resolve variable with given id (local var, param or field )
	 * returns the symbol iff found in this scope and enclosing scopes, otherwise null
	 * 
	 * NOTE: if current scope is static method scope, and id is not found locally, the 
	 * method will not search for fields in the enclosing scope, since its a different scope
	 * 
	 * @param id - of var to look for
	 * @return the symbol if found, null otherwise
	 */
	
	public abstract Symbol getVariable(String id);
		

	
	
	/**
	 * method adds new local variable
	 * @param sym
	 */
	public void addLocalVariable(LocalVariableSymbol sym)
	{
		
		this.localVarsList.add(sym);
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
	
	


}
