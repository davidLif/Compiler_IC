package IC.lir;

import java.util.HashMap;
import java.util.Map;

import IC.SymTables.Symbols.Symbol;
import IC.SymTables.Symbols.SymbolKind;




/**
 * 
 * @author Denis
 *
 *
 *	This class allocates names for local variables and parameters
 *	and handles variable shadowing in inner blocks (a unique name will be given to different symbols)
 *  a new instance should be created for each METHOD to prevent name allocation overflow
 *  use getVariableName() to fetch the variable name by symbol
 *
 */

public class VariableNameGenerator {


	private Map<Symbol, Integer> symbolIndex; /* maps each symbol to its variable name index         */
	private Map<String, Integer> varCounters; /* holds the next index of the variable with same name */
	
	
	/**
	 * a new instance should be created for each method to prevent name allocation overflow
	 */
	
	public VariableNameGenerator()
	{
		symbolIndex = new HashMap<Symbol, Integer>();
		varCounters = new HashMap<String, Integer>();
	}
	
	/**
	 * method returns the name of a given variable symbol or parameter symbol
	 * if the given symbol is not one the above, null will be returned (check null in case of field!)
	 * @param varSymbol
	 * @return name of the variable
	 */
	
	public String getVariableName(Symbol varSymbol)
	{
		/* check if not a local variable or parameter, either error or a field */
		if(varSymbol.getKind() != SymbolKind.LOCALVAR && varSymbol.getKind() != SymbolKind.PARAM)
			return null;
		
		if(symbolIndex.containsKey(varSymbol))
		{
			// this variable was named beforehand
			int index = symbolIndex.get(varSymbol);
			if(index == 1)
				return varSymbol.getId();
			return String.format("%s%d", varSymbol.getId(), index);
		}
		else
		{
			// new variable 
			// get current index
			Integer index = varCounters.get(varSymbol.getId());
			if(index == null)
			{
				// new name 
				varCounters.put(varSymbol.getId(), 2);
				symbolIndex.put(varSymbol, 1);
				// return the variable name without an id
				return varSymbol.getId(); 
			}
				
			// else, such a name already exists
			symbolIndex.put(varSymbol, index);
			// update index for variables with same name
			++index;
			varCounters.put(varSymbol.getId(), index);
			return String.format("%s%d", varSymbol.getId(), index - 1);
				
		}
		
	
	}
}
