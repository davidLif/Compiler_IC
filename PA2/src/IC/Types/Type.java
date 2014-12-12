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
	 * return true iff current type is subset of given type
	 * @param t
	 * @return
	 */
	public abstract boolean subTypeOf(Type t);
	
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
	
	protected static boolean type_compare(Type type_1,Type type_2){
		if(type_1.table_id != type_2.table_id){
			return false;
		}
		else {
			return true;
		}
	}
	
}
