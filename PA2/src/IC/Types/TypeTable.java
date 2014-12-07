package IC.Types;

import java.util.HashMap;
import java.util.Map;

import IC.DataTypes;

public class TypeTable {

	
	private Map<Integer, Type> type_map;
	
	/**
	 * how many unique types have we found so far
	 */
	private static int typeCounter = 0;
	
	/**
	 * this method allocates a new id for a type, and advances the counter
	 * @return type id
	 */
	public static int getAndAdvanceID()
	{
		++TypeTable.typeCounter ;
		return TypeTable.typeCounter - 1;
	}
	
	public TypeTable()
	{
		type_map = new HashMap<Integer, Type>();
		/* put primitive types to collection */
		addPrimitiveTypes();
		
	}
	
	private void addPrimitiveTypes()
	{
		Type integerType = new PrimitiveType(DataTypes.INT);
		Type booleanType = new PrimitiveType(DataTypes.BOOLEAN);
		Type nullType = new PrimitiveType(null);
		Type stringType = new PrimitiveType(DataTypes.STRING);
		Type voidType = new PrimitiveType(DataTypes.VOID);
		
		type_map.put(integerType.getTableId(), integerType);
		type_map.put(booleanType.getTableId(), booleanType);
		type_map.put(nullType.getTableId(), nullType);
		type_map.put(stringType.getTableId(), stringType);
		type_map.put(voidType.getTableId(), voidType);
		
	}
	
	public void addType()
	{
		
	}
	
	
	
}
