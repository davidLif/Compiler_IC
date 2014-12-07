package IC.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.DataTypes;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.Program;

public class TypeTable {

	private Map<Integer, Type> type_map_class;
	private Map<Integer, Type> type_map_method;
	private Map<Integer, Type> type_map_primitive;
	
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
	public TypeTable(Program prog)
	{
		type_map_class = new HashMap<Integer, Type>();
		type_map_method = new HashMap<Integer, Type>();
		type_map_primitive = new HashMap<Integer, Type>();
		/* put primitive types to collection */
		addPrimitiveTypes();
		/* We  should calculate all possible ClassTypes first, for a method may has as return type
		 *  a class parameter' which may be defined later in the code.*/
		addClassTypes(prog);
		//Calculate the method types- WARNING: this require part types evaluation of the tree
		addMethodTypes(prog);
		//travel over the rest of the tree and evaluate
		//evaluateTypes(prog);
		
	}
	
	private void addPrimitiveTypes()
	{
		//doesn't this add +1 to there numbers?
		Type integerType = new PrimitiveType(DataTypes.INT);
		Type booleanType = new PrimitiveType(DataTypes.BOOLEAN);
		Type nullType = new PrimitiveType(null);
		Type stringType = new PrimitiveType(DataTypes.STRING);
		Type voidType = new PrimitiveType(DataTypes.VOID);
		
		type_map_primitive.put(integerType.getTableId(), integerType);
		type_map_primitive.put(booleanType.getTableId(), booleanType);
		type_map_primitive.put(nullType.getTableId(), nullType);
		type_map_primitive.put(stringType.getTableId(), stringType);
		type_map_primitive.put(voidType.getTableId(), voidType);
		
	}
	
	/*this method travels over part of the tree and gather all possible class types
	* ASSUMPTION - if A extends B than B came in the code before A (IC spec)*/
	private void addClassTypes(Program prog) {
		Map<String,ClassType> classTypesKnown = new HashMap<String,ClassType>();
		for (ICClass IC_class:prog.getClasses()){
			ClassType newClassType = add_class_type(IC_class,classTypesKnown);
			classTypesKnown.put(IC_class.getName(), newClassType);
		}
	}
	
	//this method adds new class type (with all proper ClassType parameters)
	private ClassType add_class_type(ICClass iC_class, Map<String,ClassType> classTypesKnown) {
		Type userClassType = null;
		if(iC_class.hasSuperClass()){
			//get ClassType of super class
			ClassType superClass= getClassTypeOfString(iC_class.getSuperClassName(),classTypesKnown);
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
		type_map_class.put(userClassType.getTableId(), userClassType);
		return (ClassType) userClassType;//this casting is always safe
	}
	
	private ClassType getClassTypeOfString(String superClassName, Map<String, ClassType> classTypesKnown) {
		return classTypesKnown.get(superClassName);
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
		Type userClassMethod;
		//calc type of return method
		Type returnType = getTypeFromASTType(class_method.getType());
		
		//calc parameters type (if any)
		if(class_method.getFormals()!=null && class_method.getFormals().size() > 0){//check which one I need
			List<Type> formals_type_list =getTypesOfFormals(class_method.getFormals());
			userClassMethod = new MethodType(formals_type_list,returnType);
		}
		else{
			userClassMethod = new MethodType(returnType);
	}
	
		type_map_method.put(userClassMethod.getTableId(),userClassMethod);
		
	}

	//Given List<Formal> calc list of there "table types"
	private List<Type> getTypesOfFormals(List<Formal> formals) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//Given AST node type calc type for table type
	private Type getTypeFromASTType(IC.AST.Type type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
