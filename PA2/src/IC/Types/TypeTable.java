package IC.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import IC.DataTypes;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.UserType;
import IC.SemanticChecks.SemanticError;
import IC.SymTables.GlobalSymbolTable;

public class TypeTable {

	
	
	/**
	 * maps class name to corresponding class types
	 */
	public Map<String ,ClassType>        type_map_class;
	
	/**
	 * maps return types to list of method types
	 */
	public Map<Type, List <MethodType>> type_map_method;
	
	/**
	 * maps DataTypes to primitive types 
	 * only for NullType, use null key
	 */
	
	public Map<DataTypes, Type> type_map_primitive;
	
	
	/**
	 * maps from type to another map, that maps array dimensions to the proper type
	 * in this case the type represents a primitive type (including null type)
	 */
	public Map<Type, Map <Integer, ArrayType>> type_map_arrays_primitive;
	
	/**
	 * same as above, but maps class types
	 */
	
	public Map<String, Map <Integer, ArrayType>> type_map_arrays_class;
	
	
	/**
	 * saves program file name, for printing
	 */
	private String prog_location;
	
	
	/**
	 * how many unique types have we found so far
	 */
	private static int typeCounter = 1;
	
	
	
	private GlobalSymbolTable globSymTable;
	
	/**
	 * this method allocates a new id for a type, and advances the counter
	 * @return type id
	 */
	public static int getAndAdvanceID()
	{
		++TypeTable.typeCounter ;
		return TypeTable.typeCounter - 1;
		
	}
	

	public TypeTable(String prog_location) throws SemanticError
	{
		this.prog_location = prog_location;
		
		type_map_class = new LinkedHashMap<String, ClassType>();
		type_map_method = new LinkedHashMap<Type, List <MethodType>>();
		type_map_primitive = new LinkedHashMap<DataTypes, Type>();
		type_map_arrays_primitive = new LinkedHashMap<Type, Map <Integer, ArrayType>>();
		type_map_arrays_class = new LinkedHashMap<String, Map <Integer, ArrayType>>();
		
		this.globSymTable = null;
		
		/* put primitive types to collection */
		addPrimitiveTypes();
	
	
		
	}
	
	
	/**
	 * set the programs global symbol table, used to check if a class exists in the program or not
	 * @param symTable
	 */
	public void SetSymbolTable(GlobalSymbolTable symTable)
	{
		this.globSymTable = symTable; 
	}
	
	/**
	 * this method initializes the basic types in the type table
	 */
	private void addPrimitiveTypes()
	{
		
		Type intType = new IntType();
		Type boolType = new BoolType();
		Type nullType = new NullType();
		Type stringType = new StringType();
		Type voidType = new VoidType();
		
		type_map_primitive.put(DataTypes.INT, intType);
		type_map_primitive.put(DataTypes.BOOLEAN, boolType);
		type_map_primitive.put(null, nullType);
		type_map_primitive.put(DataTypes.STRING, stringType);
		type_map_primitive.put(DataTypes.VOID, voidType);
		
	}
	

	/**
	 * simple get methods, return the primitive types
	 * @return
	 */
	
	public Type getNullType()
	{
		return this.type_map_primitive.get(null);
	}
	public Type getBooleanType()
	{
		return this.type_map_primitive.get(DataTypes.BOOLEAN);
	}
	public Type getIntType()
	{
		return this.type_map_primitive.get(DataTypes.INT);
	}
	public Type getStringType()
	{
		return this.type_map_primitive.get(DataTypes.STRING);
	}
	
	
	/**
	 * this methods creates a new ClassType from given AST class node and returns it
	 * if such type already exists simply returns it
	 * @param iC_class
	 * @return added ClassType
	 */
	public Type getClassType(ICClass iC_class)  {
		
		/* this check is important, since we may actually found it before! */
		if(!type_map_class.containsKey(iC_class.getName())){
			ClassType userClassType = new ClassType(iC_class.getName());
			type_map_class.put(iC_class.getName(), userClassType);
			
		}
		
		return type_map_class.get(iC_class.getName());
		
		
	}
	
	/**
	 * this methods returns a class type by given name, returns null if no such class exists
	 * @param name of class
	 * @return class type or null if does not exist
	 */
	public ClassType getClassType(String name)  {
		
		/* need to check first that this is a valid class */
		
		if(this.globSymTable.getClassSymbol(name) == null)
			return null;
		
		/* check if class map already contains the class type , if so return it */
		if(this.type_map_class.containsKey(name))
			return this.type_map_class.get(name);
		
		/* doesn't exist yet, create it */
		ClassType userClassType = new ClassType(name);
		type_map_class.put(name, userClassType);
		return userClassType;
		
	}
	

	
	
	/**
	 * method creates a new array type with basic class type of given name, with given number of dimensions
	 * type will be stored in the type table
	 * 
	 * if such a type already exists, it will be returned
	 * @param name        - name of the class, must exist as a class type in the table
	 * @param dimension   - number of array dimensions
	 * @return
	 */
	
	private ArrayType addArrayType_class(String name , int dimension){
		
		//check if array of such class type existed before
		if (type_map_arrays_class.get(name) == null){
			// first array of such type
			Map <Integer, ArrayType> dimensionMap = new LinkedHashMap<Integer, ArrayType>();
			
			ArrayType newArrayType = new  ArrayType(type_map_class.get(name), dimension);
			dimensionMap.put(dimension, newArrayType);
			type_map_arrays_class.put(name, dimensionMap);
			return newArrayType;
		}
		else {
			//an array of this type already exists
			Map <Integer, ArrayType> dimensionMap = type_map_arrays_class.get(name);
			
			if(dimensionMap.get(dimension) != null){
				//this type all ready exist- return it
				return (ArrayType) dimensionMap.get(dimension);
			}
			else {
				//if not - create and store
				ArrayType newArrayType = new  ArrayType(type_map_class.get(name),dimension);
				dimensionMap.put(dimension, newArrayType);
				return newArrayType;
			}
		}	
	}

	
	

	
	
	/**
	 * 
	 * This method returns method type by given method ast node
	 * If type is a valid but does not exist, it is created, added and returned
	 * if type already exists, it is simply returned
	 * if type is invalid (a class that does not exist is used as a formal) a semantic error is thrown
	 * 
	 * @param class_method - AST node
	 * @return the proper method type
	 * @throws SemanticError
	 */
	
	public MethodType getMethodType(Method class_method) throws SemanticError{
		
		// get method return type
		Type returnType = this.getType(class_method.getType());
				
		// get formal type list
		List<Type> formals_type_list = get_arguments_type_list(class_method);
		
		
		if (type_map_method.get(returnType) == null){

			// first method type with such return type
			List<MethodType> return_type_list = new ArrayList<MethodType>();

			MethodType new_method_type = new MethodType(formals_type_list, returnType);
			return_type_list.add(new_method_type);
			type_map_method.put(returnType, return_type_list);
			return new_method_type;
		}
		
		// get possible formals' lists by return type
		List<MethodType> return_type_list = type_map_method.get(returnType);
		
		
		//check list to find if this method type (all parameter types correspond) is already in the list
		for (MethodType method_same_return_type : return_type_list){
			if (method_same_return_type.formals_compare(formals_type_list)) 
				return method_same_return_type; 
		}
		
		
		//if we came till here, no such type exist in the list. Add it
		MethodType new_method_type = new MethodType(formals_type_list, returnType);
		return_type_list.add(new_method_type);
		return new_method_type;
		
		
		
	}
	
	
	/**
	 * 	Method returns list of types of corresponding formals list
	 * 
	 * @param formals
	 * @return
	 * @throws SemanticError if an invalid type is met
	 */
	
	private List<Type> get_arguments_type_list(Method class_method) throws SemanticError {
		List<Type> formals_type_list = new ArrayList<Type>();
		
		for(Formal class_formal : class_method.getFormals()){
			Type formalType = this.getType(class_formal.getType());
			formals_type_list.add(formalType);
		}
		return formals_type_list;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nType Table: ");
		sb.append(prog_location+ "\n");
		for (Type t : type_map_primitive.values()){
			sb.append("\t" +t.getTypeRep() + "\n");
		}
		for (ClassType t : type_map_class.values()){
			sb.append("\t" +t.getTypeRep() + "\n");
		}
		for (Map<Integer, ArrayType> sub_map :type_map_arrays_primitive.values()){
			for (ArrayType t : sub_map.values()){
				sb.append("\t" +t.getTypeRep() + "\n");
			} 
		}
		for (Map<Integer, ArrayType> sub_map :type_map_arrays_class.values()){
			for (ArrayType t : sub_map.values()){
				sb.append("\t" +t.getTypeRep() + "\n");
			} 
		}
		//in order to print correctly, must sort by id
		List<MethodType> printing_list =new ArrayList<MethodType>();
		for (List<MethodType> t_list : type_map_method.values()){
			for (MethodType t : t_list){
				printing_list.add(t);
			}
			}
		Collections.sort(printing_list,MethodType.Comparator_methods);
		for(int i =0 ; i < printing_list.size() ; ++i)
		{
			if( i < printing_list.size() - 1)
			{
				sb.append("\t" +printing_list.get(i).getTypeRep() + "\n");
			}
			else
			{
				sb.append("\t" +printing_list.get(i).getTypeRep());
			}
		}
		
		
		return sb.toString() ;
	}
	
	/**
	 * method creates a new array type with basic primitive type of given type, with given number of dimensions
	 * type will be stored in the type table. if such array type already exists, it will be simpley returned.
	 * @param basicType - basic type of array, must be primitive
	 * @param dimension
	 * @return
	 */

	private ArrayType addArrayType_primitive(Type basicType, int dimension) {
		
		//check if array of such primitive type existed before
		
		if (!type_map_arrays_primitive.containsKey(basicType)){
			
			//if didn't - add it (and proper map)
			Map <Integer, ArrayType> dimensionMap = new LinkedHashMap<Integer, ArrayType>();
			
			ArrayType newArrayType = new ArrayType(basicType, dimension);
			dimensionMap.put(dimension, newArrayType);
			type_map_arrays_primitive.put(basicType, dimensionMap);
			return newArrayType;
		}
		else {
			//if did -check for dimension
			Map <Integer, ArrayType> dimensionMap = type_map_arrays_primitive.get(basicType);
			
			if(dimensionMap.get(dimension) != null){
				//this type all ready exist- return it
				return (ArrayType) dimensionMap.get(dimension);
			}
			else {
				//if didn't- create
				ArrayType newArrayType = new  ArrayType(basicType, dimension);
				dimensionMap.put(dimension, newArrayType);
				return newArrayType;
			}
		}
	}
	
	/**
	 * method adds a new array type to the table, or returns an existing one if such an array exists
	 * @param basicType - DataType of the basic type (primitive type)
	 * @param dimension - number of dimensions
	 * @return
	 */
	
	private ArrayType addArrayType_primitive(DataTypes basicType, int dimension){
		return addArrayType_primitive(type_map_primitive.get(basicType), dimension);
	}
	
	
	/**
	 * this method builds/retrieves a type object from a AST type node
	 * note that if the node is of type UserType and such a class does not exist, an error will be thrown
	 * @param type - AST node
	 * @return type of AST node
	 * @throws SemanticError
	 */
	
	public Type getType(IC.AST.Type type) throws SemanticError
	{
		if(type instanceof PrimitiveType)
			return getPrimitiveType((PrimitiveType)type);
		return getUserType((UserType)type);
		
	}
	
	private Type getPrimitiveType(PrimitiveType type)
	{
		
		if(type.getDimension() == 0)
		{
			return  type_map_primitive.get(type.getDataTypes());
		}
		else{
			//add new type of array if needed. add all types of arrays with smaller dimensions
			for (int i=1;i < type.getDimension(); i++){
				  addArrayType_primitive(type.getDataTypes(), i);
			}
			return  addArrayType_primitive(type.getDataTypes(), type.getDimension());
		}
		
		
	}
	
	
	private Type getUserType(UserType type) throws SemanticError
	{
	
		
		
		// check if class  exists
		if(this.globSymTable.getClassSymbol(type.getName()) == null)
		{
			// such class was not defined in the entire program
			throw new SemanticError(type.getLine(), "class " + type.getName() + " is not defined");
		}
		
		if(!this.type_map_class.containsKey(type.getName()))
		{
			// need to add the class type first
			// first time we see this class type
			ClassType userClassType = new ClassType(type.getName());
			type_map_class.put(type.getName(), userClassType);
		}
		
		
		if (type.getDimension() == 0){
			
			return this.type_map_class.get(type.getName());
			
			
		}
		else{
			
			//add new type of array if needed.
			
			for (int i=1; i < type.getDimension();i++){
				 addArrayType_class(type.getName(), i);
			}
			return addArrayType_class(type.getName(), type.getDimension());
			
		}
		
	}
	
	public Type getPrimitiveType(DataTypes dataType)
	{
		return this.type_map_primitive.get(dataType);
	}
	
	/**
	 * returns an array type of same basic type, but with one dimension less (item type)
	 * @param arrayType
	 * @return
	 */
	
	public Type getArrayTypeItemType(ArrayType arrayType)
	{
		
		return this.getArrayType(arrayType.getBasicType(), arrayType.getDimensions() - 1);
	}
	
	
	/**
	 * This method returns an array type of given basic type and number of dimensions 
	 * (if no such type exists, it is created and stored in the table )
	 * 
	 * @param basicType - not of array type
	 * @param dimensions
	 * @return
	 */
	
	public Type getArrayType(Type basicType, int dimensions)
	{
		if(basicType instanceof ClassType)
		{
			ClassType classBasicType = (ClassType)basicType;
			if(dimensions == 0)
			{
				return this.type_map_class.get(classBasicType.getName());
			}
			return this.addArrayType_class(classBasicType.getName(), dimensions);
		}
		else
		{
			// must be primitive type

			DataTypes primitiveType;
			if(basicType == getBooleanType())
			{
				primitiveType = DataTypes.BOOLEAN;
			}
			else if(basicType == this.getIntType())
			{
				primitiveType = DataTypes.INT;
			}
			else // must be string
			{
				primitiveType = DataTypes.STRING;
			}
			
			if(dimensions == 0)
			{
				return this.type_map_primitive.get(primitiveType);
			}
			
			return this.addArrayType_primitive(primitiveType, dimensions);
		}
	}
	
	
}
