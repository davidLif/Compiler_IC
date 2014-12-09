package IC.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.DataTypes;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.Program;

public class TypeTable {

	public Map<String ,ClassType> type_map_class;
	public Map<Type, List <MethodType>> type_map_method;//change Int later
	public Map<DataTypes, PrimitiveType> type_map_primitive;
	public Map<DataTypes, Map <Integer, ArrayType>> type_map_arrays_primitive;
	public Map<String, Map <Integer, ArrayType>> type_map_arrays_class;
	
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
	
	/* ASSUMPTION - there are no "recursive type definitions*/
	public TypeTable(Program prog,Program lib)
	{
		type_map_class = new HashMap<String, ClassType>();
		type_map_method = new HashMap<Type, List <MethodType>>();
		type_map_primitive = new HashMap<DataTypes, PrimitiveType>();
		type_map_arrays_primitive = new HashMap<DataTypes, Map <Integer, ArrayType>>();
		type_map_arrays_class = new HashMap<String, Map <Integer, ArrayType>>();
		/* put primitive types to collection */
		addPrimitiveTypes();
		
		//TODO-should add library class and methods here to the Type table.
		
		/* We  should calculate all possible ClassTypes first, for a method may has as return type
		 *  a class parameter, which may be defined later in the code.*/
		addClassTypes(prog);
		//Calculate the method types- WARNING: this require part types evaluation of the tree 
		addMethodTypes(prog);
		//travel over the rest of the tree and evaluate- add array types
		
	}
	
	//This function adds new array - only for primitive type based.
	// WARNING- this function is used while traveling over the tree- caution is advised.
	// or should we travel all over the tree and gather them' and then travel normally again?
	protected ArrayType addArrayType_primitive(DataTypes type,int dimension){
		
		//check if array of such primitive type existed before
		if (type_map_arrays_primitive.get(type) == null){
			//if didn't - add it (and proper map)
			Map <Integer, ArrayType> dimensionMap = new HashMap<Integer, ArrayType>();
			
			ArrayType newArrayType = new  ArrayType(type_map_primitive.get(type),dimension);
			dimensionMap.put(dimension, newArrayType);
			type_map_arrays_primitive.put(type, dimensionMap);
			return newArrayType;
		}
		else {
			//if did -check for dimension
			Map <Integer, ArrayType> dimensionMap = type_map_arrays_primitive.get(type);
			
			if(dimensionMap.get(dimension) != null){
				//this type all ready exist- return it
				return (ArrayType) dimensionMap.get(dimension);
			}
			else {
				//if didn't- create
				ArrayType newArrayType = new  ArrayType(type_map_primitive.get(type),dimension);
				dimensionMap.put(dimension, newArrayType);
				return newArrayType;
			}
		}
		
	}
	
	//This function adds new array - only for class type based.
	// WARNING- this function is used while traveling over the tree- caution is advised.
	// or should we travel all over the tree and gather them' and then travel normally again?
	protected ArrayType addArrayType_class(String name,int dimension){
		
		//check if array of such class type existed before
		if (type_map_arrays_class.get(name) == null){
			//if didn't - add it (and proper map)
			Map <Integer, ArrayType> dimensionMap = new HashMap<Integer, ArrayType>();
			
			ArrayType newArrayType = new  ArrayType(type_map_primitive.get(name),dimension);
			dimensionMap.put(dimension, newArrayType);
			type_map_arrays_class.put(name, dimensionMap);
			return newArrayType;
		}
		else {
			//if did -check for dimension
			Map <Integer, ArrayType> dimensionMap = type_map_arrays_class.get(name);
			
			if(dimensionMap.get(dimension) != null){
				//this type all ready exist- return it
				return (ArrayType) dimensionMap.get(dimension);
			}
			else {
				//if didn't- create
				ArrayType newArrayType = new  ArrayType(type_map_primitive.get(name),dimension);
				dimensionMap.put(dimension, newArrayType);
				return newArrayType;
			}
		}	
	}

	private void addPrimitiveTypes()
	{
		//doesn't this add +1 to there numbers?
		PrimitiveType integerType = new PrimitiveType(DataTypes.INT);
		PrimitiveType booleanType = new PrimitiveType(DataTypes.BOOLEAN);
		PrimitiveType nullType = new PrimitiveType(null);
		PrimitiveType stringType = new PrimitiveType(DataTypes.STRING);
		PrimitiveType voidType = new PrimitiveType(DataTypes.VOID);
		
		type_map_primitive.put(DataTypes.INT, integerType);
		type_map_primitive.put(DataTypes.BOOLEAN, booleanType);
		type_map_primitive.put(null, nullType);
		type_map_primitive.put(DataTypes.STRING, stringType);
		type_map_primitive.put(DataTypes.VOID, voidType);
		
	}
	
	/*this method travels over part of the tree and gather all possible class types
	* ASSUMPTION - if A extends B than B came in the code before A (IC spec)*/
	private void addClassTypes(Program prog) {
		for (ICClass IC_class:prog.getClasses()){
			add_class_type(IC_class);
		}
	}
	
	//this method adds new class type (with all proper ClassType parameters)
	private void add_class_type(ICClass iC_class) {
		ClassType userClassType = null;
		if(iC_class.hasSuperClass()){
			//get ClassType of super class
			ClassType superClass= type_map_class.get(iC_class.getSuperClassName());
			//If super class does not exist - this is a semantic error
			if(superClass ==null){
				//TODO- throw error
			}
			//get getSuperClassName_list so we will add her later to current class A<=B<=C ==> A<=C
			List<ClassType> super_super_list = superClass.getSuperClassName_list();
			//add direct supper class to superClassList
			super_super_list.add(superClass);
			
			userClassType =new ClassType(iC_class.getName(),superClass,super_super_list);
		}
		else{
			userClassType =new ClassType(iC_class.getName());
		}
		//check if class defined twice
		if (type_map_class.get(iC_class.getName()) != null){
			//TODO- throw error
		}
		else{
			type_map_class.put(iC_class.getName(), userClassType);
		}
		
	}
	
	//this method travels over part of the tree and gather all possible method types
	private void addMethodTypes(Program prog)
	{
		for (ICClass IC_class:prog.getClasses()){
			for(Method class_method : IC_class.getMethods()){
					add_mathod_type(class_method);
				}
		}
	}

	//This method adds new method type (with all proper MethodType parameters)
	//This method can be used only after  return statement type evaluation was done!
	private void add_mathod_type(Method class_method) {
		
		//calc type of return method
		Type returnType = getTypeFromASTType(class_method.getType());
		
		//This list holds all the type of the method arguments
		//if there is no formals than formals_compare will return true to all MethodTypes with no parameters
		List<Type> formals_type_list = get_arguments_type_list(class_method);

		
		if (type_map_method.get(returnType) == null){
			//if didn't - make list and add new method type
			List<MethodType> return_type_list = new ArrayList<MethodType>();

			return_type_list.add(new MethodType(formals_type_list,returnType));
			type_map_method.put(returnType, return_type_list);
		}
		else {
			//if did -get list of all the methods with the same return type
			List<MethodType> return_type_list = type_map_method.get(returnType);
			
			//check list to find if this method type (all parameter types correspond) is already in the list
			for (MethodType method_same_return_type : return_type_list){
				if (method_same_return_type.formals_compare(formals_type_list)) return; 
			}
			
			//if we came till here, no such type exist in the list.
			return_type_list.add(new MethodType(formals_type_list,returnType));
			type_map_method.put(returnType, return_type_list);
		}
	}

	//Given List<Formal> calc list of there "table types"
	private List<Type> getTypesOfFormals(List<Formal> formals) {
		List<Type> formals_type_list = new ArrayList<Type>(); 
		for(Formal class_formal : formals){
			Type formalType = class_formal.accept(new TypeBuilder(this),null); //in finding a type of formal, no context should be used
			formals_type_list.add(formalType);
		}
		return formals_type_list;
	}
	
	//Given AST node type calc type for table type
	private Type getTypeFromASTType(IC.AST.Type type) {
		return type.accept(new TypeBuilder(this), null);//in finding a type of type, no context should be used
	}
	
	public MethodType getMethodTypeFromMap(Method class_method){
		//calc type of return method
		Type returnType = getTypeFromASTType(class_method.getType());
				
		//if there is no formals than formals_compare will return true to all MethodTypes with no parameters
		List<Type> formals_type_list = get_arguments_type_list(class_method);
		
		//if did -get list of all the methods with the same return type
		List<MethodType> return_type_list = type_map_method.get(returnType);
		
		//check list to find if this method type (all parameter types correspond) is already in the list
		for (MethodType method_same_return_type : return_type_list){
			if (method_same_return_type.formals_compare(formals_type_list)) return method_same_return_type; 
		}
		return null;
	}
	
	private List<Type> get_arguments_type_list(Method class_method) {
		List<Type> formals_type_list = new ArrayList<Type>();
		
		//calc parameters type (if any) and 
		if(class_method.getFormals()!=null && class_method.getFormals().size() > 0){//check which one I need
			//get Types Of Formals
			formals_type_list =getTypesOfFormals(class_method.getFormals());
		}
		return formals_type_list;
	}
	
	
}
