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
import IC.SemanticChecks.SemanticError;
import IC.SymTables.GlobalSymbolTable;
import IC.SymTables.SymbolTable;

public class TypeTable {

	public Map<String ,ClassType> type_map_class;
	public Map<Type, List <MethodType>> type_map_method;
	public Map<DataTypes, Type>type_map_primitive;
	public Map<Type, Map <Integer, ArrayType>> type_map_arrays_primitive;
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
	public TypeTable(Program prog,Program lib,GlobalSymbolTable symbol_table)
	{
		type_map_class = new HashMap<String, ClassType>();
		type_map_method = new HashMap<Type, List <MethodType>>();
		type_map_primitive = new HashMap<DataTypes, Type>();
		type_map_arrays_primitive = new HashMap<Type, Map <Integer, ArrayType>>();
		type_map_arrays_class = new HashMap<String, Map <Integer, ArrayType>>();
		/* put primitive types to collection */
		addPrimitiveTypes();
		
		SymbolSetter test = new SymbolSetter(this,symbol_table);
		try {
			prog.accept(test);
		} catch (SemanticError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TypeEvaluator evaluate = new TypeEvaluator(this,symbol_table);
		try {
			prog.accept(evaluate);
		} catch (SemanticError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO-should add library class and methods here to the Type table.
		
	}
	
	//This function adds new array - only for primitive type based.
	// WARNING- this function is used while traveling over the tree- caution is advised.
	// or should we travel all over the tree and gather them' and then travel normally again?
	protected ArrayType addArrayType_primitive(DataTypes type,int dimension){
		
		//check if array of such primitive type existed before
		if (type_map_arrays_primitive.get(type_map_primitive.get(type)) == null){
			//if didn't - add it (and proper map)
			Map <Integer, ArrayType> dimensionMap = new HashMap<Integer, ArrayType>();
			
			ArrayType newArrayType = new  ArrayType(type_map_primitive.get(type),dimension);
			dimensionMap.put(dimension, newArrayType);
			type_map_arrays_primitive.put(type_map_primitive.get(type), dimensionMap);
			return newArrayType;
		}
		else {
			//if did -check for dimension
			Map <Integer, ArrayType> dimensionMap = type_map_arrays_primitive.get(type_map_primitive.get(type));
			
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
			
			ArrayType newArrayType = new  ArrayType(type_map_class.get(name),dimension);
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
				ArrayType newArrayType = new  ArrayType(type_map_class.get(name),dimension);
				dimensionMap.put(dimension, newArrayType);
				return newArrayType;
			}
		}	
	}

	private void addPrimitiveTypes()
	{
		//doesn't this add +1 to there numbers?
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
	
	//this method adds new class type (with all proper ClassType parameters)
	public Type add_class_type(ICClass iC_class) throws SemanticError {
		ClassType userClassType = null;
		if(iC_class.hasSuperClass()){
			//get ClassType of super class
			ClassType superClass= type_map_class.get(iC_class.getSuperClassName());
			//If super class does not exist - this is a semantic error
			if(superClass ==null){
				throw new SemanticError(iC_class.getLine(), "hasSuperClass() return true but superClass == null");
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
			throw new SemanticError(iC_class.getLine(), "the class "+iC_class.getName()+" is defined twice");
		}
		else{
			type_map_class.put(iC_class.getName(), userClassType);
			return userClassType;
		}
		
	}

	//This method adds new method type (with all proper MethodType parameters)
	//This method can be used only after  return statement type evaluation was done!
	public MethodType add_method_type(Method class_method) throws SemanticError {
		
		//calc type of return method
		Type returnType = getTypeFromASTType(class_method.getType());
		
		//This list holds all the type of the method arguments
		//if there is no formals than formals_compare will return true to all MethodTypes with no parameters
		List<Type> formals_type_list = get_arguments_type_list(class_method);

		
		if (type_map_method.get(returnType) == null){
			//if didn't - make list and add new method type
			List<MethodType> return_type_list = new ArrayList<MethodType>();

			MethodType new_method_type = new MethodType(formals_type_list,returnType);
			return_type_list.add(new_method_type);
			type_map_method.put(returnType, return_type_list);
			return new_method_type;
		}
		else {
			//if did -get list of all the methods with the same return type
			List<MethodType> return_type_list = type_map_method.get(returnType);
			
			//check list to find if this method type (all parameter types correspond) is already in the list
			for (MethodType method_same_return_type : return_type_list){
				if (method_same_return_type.formals_compare(formals_type_list)) return method_same_return_type; 
			}
			
			//if we came till here, no such type exist in the list.Add it
			MethodType new_method_type = new MethodType(formals_type_list,returnType);
			return_type_list.add(new_method_type);
			type_map_method.put(returnType, return_type_list);
			return new_method_type;
		}
	}

	//Given List<Formal> calc list of there "table types"
	private List<Type> getTypesOfFormals(List<Formal> formals) throws SemanticError {
		List<Type> formals_type_list = new ArrayList<Type>(); 
		for(Formal class_formal : formals){
			Type formalType = (Type) class_formal.accept(new SymbolSetter(this,null)); //in finding a type of formal, no context should be used
			formals_type_list.add(formalType);
		}
		return formals_type_list;
	}
	
	//Given AST node type calc type for table type
	private Type getTypeFromASTType(IC.AST.Type type) throws SemanticError {
		return (Type) type.accept(new SymbolSetter(this,null));//in finding a type of type, no context should be used
	}
	
	public MethodType getMethodTypeFromMap(Method class_method) throws SemanticError{
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
	
	private List<Type> get_arguments_type_list(Method class_method) throws SemanticError {
		List<Type> formals_type_list = new ArrayList<Type>();
		
		//calc parameters type (if any) and 
		if(class_method.getFormals()!=null && class_method.getFormals().size() > 0){//check which one I need
			//get Types Of Formals
			formals_type_list =getTypesOfFormals(class_method.getFormals());
		}
		return formals_type_list;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("primitive: " + "\n");
		sb.append(type_map_primitive.values()+ "\n");
		sb.append("class: " + "\n");
		sb.append(type_map_class.values()+ "\n");
		sb.append("methods: " + "\n");
		sb.append(type_map_method.values()+ "\n");
		return sb.toString() ;
	}
	
	//TODO-get_main_methodType
}
