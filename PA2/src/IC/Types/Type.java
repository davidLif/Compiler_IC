package IC.Types;

public abstract class Type {

	/**
	 * numeric id of the type, represents id in the type table
	 */
	private int table_id;
	
	public Type()
	{
		// fetch id for type table
		this.table_id = TypeTable.getAndAdvanceID();
		
	}
	
	public int getTableId()
	{
		return this.table_id;
	}
	
	/**
	 * 
	 * @return method returns a textual representation for the type table
	 *         for example:
	 *         		"1: Primitive type: int"
	 *         or
	 *         		 "15: Method type: {string[] -> void}"
	 *       
	 */
	
	public String getTypeTableRep()
	{
		return String.format("%d: %s", this.table_id, this.getTypeRep());
	}
	
	/**
	 * 
	 * @return a string representing the Type node for the symbol table
	 *         for example, for a primitive type will hold
	 *         "Primitive type: int"
	 *         for class node it may hold
	 *         "Class: B, Superclass ID: 8"
	 */
	protected abstract String getTypeRep();
	
}
