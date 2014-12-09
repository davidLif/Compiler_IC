package IC.Types;

import IC.DataTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.PropagatingVisitor;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.While;
import IC.SymTables.ClassSymbol;
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.FieldSymbol;
import IC.SymTables.SymbolTable;
import IC.SymTables.VirtualMethodSymbol;


public class TypeBuilder implements PropagatingVisitor<SymbolTable, Type> {
	
	//This TypeTable will be used to fetch the Type that should be given to the symbols
	public TypeTable pastSeenTypes;

	public TypeBuilder(TypeTable typeTable) {
		pastSeenTypes = typeTable;
	}

	//ASSUMPTION - context should be a GlobalSymbolTable instance
	@Override
	public Type visit(Program program, SymbolTable context) {
		//move over all classes and give there corresponding tables.
		for (ICClass program_class : program.getClasses()){
			//Let's Hope this will work- may cause troble
			
			//get class symbol out of GlobalSymbolTable
			ClassSymbol class_symbol = (ClassSymbol)context.getSymbol(program_class.getName());
			//get SymbolTable from class symbol
			ClassSymbolTable class_context = (ClassSymbolTable) class_symbol.getClassSymbolTable();
			//calc class type
			Type class_type = program_class.accept(this, class_context);
			//set class type
			class_symbol.setType(class_type);
			
		}
		return null;//there is no type for Program
	}

	@Override
	public Type visit(ICClass icClass, SymbolTable context) {
		//Symbol Table for class should be ClassSymbolTable
		ClassSymbolTable class_context = (ClassSymbolTable) context;
		
		//move over all fields and calculate.
		for (Field class_field : icClass.getFields()) {
			//calc type for field
			Type field_type  = class_field.accept(this, null);
			//get field symbol
			FieldSymbol field_symbol = (FieldSymbol) class_context.getSymbol(class_field.getName());
			//set type
			field_symbol.setType(field_type);
		}
		
		//move over all methods and give there corresponding tables.
		//add method types to symbol table now- in order to do lookup in case of future reference(like a() before we saw def)
		for (Method class_method : icClass.getMethods()) {
			//calc type for field
			Type method_type  = pastSeenTypes.getMethodTypeFromMap(class_method);
			//get field symbol
			FieldSymbol field_symbol = (FieldSymbol) class_context.getSymbol(class_method.getName());
			//set type
			field_symbol.setType(method_type);
		}
		
		//now travel into each method
		for (Method class_method : icClass.getMethods()) {
			//TODO- get proper SymbolTable for each method 
			class_method.accept(this, null);//THIS WILL NOT WORK ON PORPES 
		}
		
		return pastSeenTypes.type_map_class.get(icClass.getName());
	}

	@Override
	public Type visit(Field field, SymbolTable context) {
		return field.getType().accept(this, null);
	}

	//ASSUMPTION - context should be according to table. Might do junk if it is not so.
	@Override
	public Type visit(VirtualMethod method, SymbolTable context) {
		return static_or_virtual_visit(method, context);
	}

	//ASSUMPTION - context should be according to table. Might do junk if it is not so.
	@Override
	public Type visit(StaticMethod method, SymbolTable context) {
		return static_or_virtual_visit(method, context);
	}

	@Override
	public Type visit(LibraryMethod method, SymbolTable context) {
		//library method should not contain anything
		return pastSeenTypes.getMethodTypeFromMap(method);
	}

	@Override
	public Type visit(Formal formal, SymbolTable context) {
		return formal.getType().accept(this,context);//formals type is according to it's definition
	}

	@Override
	public Type visit(PrimitiveType type, SymbolTable context) {
		IC.Types.Type nodeType= null;
		if (type.getDimension() == 0){
			//we already entered into the table all primitive types
			nodeType= pastSeenTypes.type_map_primitive.get(type.getDataTypes());
		}
		else{
			//add new type of array if needed. Get anyway the Type
			nodeType = pastSeenTypes.addArrayType_primitive(type.getDataTypes(),type.getDimension());
		}
		return nodeType;
	}

	//ASSAMPTION-User type can only be class or array of class
	@Override
	public Type visit(UserType type, SymbolTable context) {
		IC.Types.Type nodeType= null;
		if (type.getDimension() == 0){
			//we already entered into the table all primitive types
			nodeType= pastSeenTypes.type_map_class.get(type.getName());
		}
		else{
			//add new type of array if needed. Get anyway the Type
			nodeType = pastSeenTypes.addArrayType_class(type.getName(),type.getDimension());
		}
		return nodeType;
	}

	@Override
	public Type visit(Assignment assignment, SymbolTable context) {
		//get right side type
		Type exp_type = assignment.getAssignment().accept(this, context);
		//get left side type
		Type var_rype = assignment.getVariable().accept(this, context);
		
		//check that left side type == right side type 
		if (!IC.Types.Type.type_compare(exp_type, var_rype)){
			//TODO-throw error
		}
		return null;//assignment should not have type
	}

	@Override
	public Type visit(CallStatement callStatement, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Return returnStatement, SymbolTable context) {
		return returnStatement.getValue().accept(this, context);//return stmt type is the type which he returns
	}

	@Override
	public Type visit(If ifStatement, SymbolTable context) {
		//get condition type
		Type cond=ifStatement.getCondition().accept(null, context);
		//condition type must be boolean
		if (IC.Types.Type.type_compare(cond, pastSeenTypes.type_map_primitive.get(DataTypes.BOOLEAN))){
			//TODO -throw error
		}
		//else - all is fine
		return null;//if has no type
	}

	@Override
	public Type visit(While whileStatement, SymbolTable context) {
		//get condition type
		Type cond=whileStatement.getCondition().accept(null, context);
		//condition type must be boolean
		if (IC.Types.Type.type_compare(cond, pastSeenTypes.type_map_primitive.get(DataTypes.BOOLEAN))){
			//TODO -throw error
		}
		//else - all is fine
		return null;//while has no type
	}

	@Override
	public Type visit(Break breakStatement, SymbolTable context) {
		return null;//break statement has no type or sons
	}

	@Override
	public Type visit(Continue continueStatement, SymbolTable context) {
		return null;//continue statement has no type or sons
	}

	@Override
	public Type visit(StatementsBlock statementsBlock, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(LocalVariable localVariable, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(VariableLocation location, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ArrayLocation location, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(StaticCall call, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(VirtualCall call, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(This thisExpression, SymbolTable context) {
		return null;
	}

	@Override
	public Type visit(NewClass newClass, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(NewArray newArray, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Length length, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(MathBinaryOp binaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(LogicalBinaryOp binaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(MathUnaryOp unaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(LogicalUnaryOp unaryOp, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Literal literal, SymbolTable context) {
		//for literal always return string
		return pastSeenTypes.type_map_primitive.get(DataTypes.STRING);
	}

	@Override
	public Type visit(ExpressionBlock expressionBlock, SymbolTable context) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//both virtual and static visits act the same.Only the context differ.
	private Type static_or_virtual_visit(Method method,SymbolTable context) {
		boolean is_there_return = false;
		//calc all formal types
		for (Formal method_argument :method.getFormals()){
			method_argument.accept(this, null);
		}
		
		//get return type
		Type ret_type= method.getType().accept(this, context);
		//travel on all statements and check return type
		for (Statement method_statement : method.getStatements()){
			Type stmt_type = method_statement.accept(this, context);
			
			//check that all return statements type is according to method return type
			if (method_statement instanceof Return){
				is_there_return = true;
				if (!IC.Types.Type.type_compare(ret_type, stmt_type)) {
					//TODO-throw error
				}
			}
		}
		
		//if not void return type - return stmt must be
		if (!is_there_return 
			&& IC.Types.Type.type_compare(ret_type, pastSeenTypes.type_map_primitive.get(DataTypes.VOID)))
		{
			//TODO-throw error
		}
		
		return pastSeenTypes.getMethodTypeFromMap(method);
	}
	

}
