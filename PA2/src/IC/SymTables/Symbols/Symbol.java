package IC.SymTables.Symbols;

import IC.Types.Type;

public abstract class Symbol {
	
	/** 
	 * name/id of the symbol (for example, name of method)
	 */
	protected String id; 
	
	/**
	 * type of symbol
	 * 
	 */
	protected Type type;
	
	public Symbol(String id, Type type)
	{
		this.id = id;
		this.type = type;
	}
	
	public Symbol(String id )
	{
		this.id = id;
		this.type = null;
	}
	
	
	
	/**
	 * 
	 * @param type - type of symbol
	 */
	public void setType(Type type)
	{
		this.type = type;
	}
	
	
	public String getId()
	{
		return this.id;
	}
	
}
