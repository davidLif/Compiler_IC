package IC.SymTables;

import IC.Types.Type;;

public abstract class Symbol {
	
	/** 
	 * name/id of the symbol (for example, name of method)
	 */
	private String id; 
	
	/**
	 * type of symbol
	 * 
	 */
	private Type type;
	
	
	/**
	 * symbol kind
	 */
	private Kind kind;
}
