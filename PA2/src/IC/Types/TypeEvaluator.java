package IC.Types;

import java.util.ArrayList;
import java.util.List;

import IC.BinaryOps;
import IC.DataTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.Call;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.Expression;
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
import IC.AST.Visitor;
import IC.AST.While;
import IC.SemanticChecks.SemanticError;
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.GlobalSymbolTable;
import IC.SymTables.VariableSymbolTable;
import IC.SymTables.Symbols.ClassSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;

public class TypeEvaluator implements Visitor {
	
	TypeTable all_pos_types;
	GlobalSymbolTable globalSymbolTable;
	
	public TypeEvaluator(TypeTable table,GlobalSymbolTable globalSymbolTable){
		all_pos_types = table;
		this.globalSymbolTable = globalSymbolTable;
	}

	@Override
	public Type visit(Program program) throws SemanticError {
		//visit into each class
		for (ICClass program_class : program.getClasses()){
			program_class.accept(this);
		}
		return null;
	}

	@Override
	public Type visit(ICClass icClass) throws SemanticError {
		//no need to visit fields- they cann't be initialized in IC in Field AST node;
		
		//move all over methods
		for (Method class_method : icClass.getMethods()){
			class_method.accept(this);
		}
		return null;
	}

	@Override
	public Type visit(Field field) throws SemanticError {
		//we should not get here- no type errors in this AST node
		return null;
	}

	@Override
	public Type visit(VirtualMethod method) throws SemanticError {
		return method_visit(method);
	}

	@Override
	public Type visit(StaticMethod method) throws SemanticError {
		return method_visit(method);
	}

	@Override
	public Type visit(LibraryMethod method) throws SemanticError {
		//nothing to evaluate in LibraryMethod
		return null;
	}

	@Override
	public Type visit(Formal formal) throws SemanticError {
		//we should not get here- no type errors in this AST node
		return null;
	}

	@Override
	public Type visit(PrimitiveType type) throws SemanticError {
		//should be calculated already -find in one of the maps
		if (type.getDimension() == 0 && type.getDataTypes()!= null){
			type.setNodeType(all_pos_types.type_map_primitive.get(type.getDataTypes()));
		}
		else if (type.getDataTypes()!= null){
			type.setNodeType(all_pos_types.type_map_arrays_primitive.get(all_pos_types.type_map_primitive.get(type.getDataTypes())).get(type.getDimension()));
		}
		if(type.getNodeType() == null){
			throw new SemanticError(type.getLine(),"Such type was never defined");
		}
		return type.getNodeType();
	}

	@Override
	public Type visit(UserType type) throws SemanticError {
		//should be calculated already -find in one of the maps
		if (type.getDimension() == 0 && type.getName()!= null){
			type.setNodeType(all_pos_types.type_map_primitive.get(type.getName()));
		}
		else if (type.getName()!= null){
			type.setNodeType(all_pos_types.type_map_arrays_primitive.get(all_pos_types.type_map_primitive.get(type.getName())).get(type.getDimension()));
		}
		if(type.getNodeType() == null){
			throw new SemanticError(type.getLine(),"Such type was never defined");
		}
		return type.getNodeType();
	}

	@Override
	public Type visit(Assignment assignment) throws SemanticError {
		//get variable type
		Type left_side_type = (Type) assignment.getVariable().accept(this);
		//get expression type
		Type right_side_type = (Type) assignment.getAssignment().accept(this);
		
		//compare the sides types
		if (!right_side_type.subTypeOf(left_side_type)){
			throw new SemanticError(assignment.getLine(),"rigth side of assignment is not sub type of left side");
		}
		return null;
	}

	@Override
	public Type visit(CallStatement callStatement) throws SemanticError {
		//visit and check call
		Type call_type =(Type) callStatement.getCall().accept(this);
		//set callSatement type
		callStatement.setNodeType(call_type);
		return call_type;
	}

	@Override
	public Type visit(Return returnStatement) throws SemanticError {
		Type return_stmt_type =  (Type) returnStatement.getValue().accept(this);
		//TODO-check against $ret
		return null;
	}

	@Override
	public Type visit(If ifStatement) throws SemanticError {
		//check condition to be boolean
		Type cond_type = (Type) ifStatement.getCondition().accept(this);
		if (!cond_type.subTypeOf(all_pos_types.type_map_primitive.get(DataTypes.BOOLEAN))){
			throw new SemanticError(ifStatement.getLine(),"if condition isn't boolean");
		}
		//visit into if stmt and else stmt
		ifStatement.getOperation().accept(this);
		if (ifStatement.getElseOperation() != null){
			ifStatement.getElseOperation().accept(this);
		}
		return null;
	}

	@Override
	public Type visit(While whileStatement) throws SemanticError {
		//check condition to be boolean
		Type cond_type = (Type) whileStatement.getCondition().accept(this);
		if (!cond_type.subTypeOf(all_pos_types.type_map_primitive.get(DataTypes.BOOLEAN))){
			throw new SemanticError(whileStatement.getLine(),"if condition isn't boolean");
		}
		//visit into if stmt
		whileStatement.getOperation().accept(this);
		return null;
	}

	@Override
	public Type visit(Break breakStatement) throws SemanticError {
		return null;// no type for break
	}

	@Override
	public Type visit(Continue continueStatement) throws SemanticError {
		return null;// no type for continue
	}

	@Override
	public Type visit(StatementsBlock statementsBlock) throws SemanticError {
		//visit each stmt
		for (Statement s : statementsBlock.getStatements()){
			s.accept(this);
		}
		return null;
	}

	@Override
	public Type visit(LocalVariable localVariable) throws SemanticError {
		//already calculated type. now check that init Value type is corresponding.
		if (localVariable.getInitValue() != null){
			Type init_type = (Type) localVariable.getInitValue().accept(this);
			if (!init_type.subTypeOf(localVariable.getNodeType())){
				throw new SemanticError(localVariable.getLine(),"init Value does not correspond to vraiable type");
			}
			return init_type;
		}
		return localVariable.getNodeType();

	}

	@Override
	public Type visit(VariableLocation location) throws SemanticError {
		//get variable symbol to extract type (set in symbolSetter)
		Symbol var_symbol;
		if (location.getLocation() != null){
			//get type of expr out of expr.id
			Type location_type = (Type) location.getLocation().accept(this);
			
			//if location.getLocation() isn't a class, than there is a error ( (3+4).x )
			if (location_type instanceof ClassType){
				//get class symbol table
				ClassSymbolTable class_symbol_table = get_class_table(((ClassType) location_type).getName());
				//get id symbol from class table
				var_symbol = class_symbol_table.getField(location.getName());
			}
			else {
				throw new SemanticError(location.getLine(),"Type of "+location.getLocation()+" isn't ClassType -  it doesn't have methods or fields");
			}
		}
		else {
			//if no expr was given, this is a variable from this scope
			var_symbol = ((VariableSymbolTable)location.enclosingScope()).getVariable(location.getName());
		}
		//get type from var symbol 
		Type var_type = var_symbol.getType();
		location.setNodeType(var_type);
		return var_type;
	}

	@Override
	public Type visit(ArrayLocation location) throws SemanticError {
		//check index to be integer
		Type index_type = (Type) location.getIndex().accept(this);
		if (!index_type.subTypeOf(all_pos_types.type_map_primitive.get(DataTypes.INT))){
			throw new SemanticError(location.getLine(),"The index to this array isn't an integer");
		}
		
		//check array expression to really be an array
		Type expr_type = (Type) location.getArray().accept(this);
		if (!(expr_type instanceof ArrayType)){
			throw new SemanticError(location.getLine(),"left expression does not yield array type");
		}
		
		ArrayType array_type = (ArrayType)expr_type;
		Type arrayLocationType;
		if (array_type.getDimensions() == 1){
			location.setNodeType(array_type.getBasicType());
			return array_type.getBasicType();
		}
		//if Dimensions bigger than 1- get arrayType with Dimension-1
		if (array_type.getBasicType() instanceof ClassType ){
			arrayLocationType = (Type) all_pos_types.type_map_arrays_class.get(((ClassType)array_type.getBasicType()).getName()).get(array_type.getDimensions()-1);
		}
		else{
			//must be primitive type
			arrayLocationType = (Type) all_pos_types.type_map_arrays_primitive.get(array_type.getBasicType()).get(array_type.getDimensions()-1);
		}
		location.setNodeType(arrayLocationType);
		return arrayLocationType;
	}

	@Override
	public Type visit(StaticCall call) throws SemanticError {
		//get class symbol table
		ClassSymbolTable class_symbolTable = get_class_table(call.getClassName());
		//get method symbol
		MethodSymbol method_symbol = class_symbolTable.getStaticMethod(call.getName());
		//get method type
		MethodType method_type = get_method_type_for_call(call, method_symbol);
		
		//check argument types
		checkArgsValidity(call, method_type);
		
		call.setNodeType(method_type.getReturnType());
		return method_type.getReturnType();
	}

	@Override
	public Type visit(VirtualCall call) throws SemanticError {
		/*calculate to get method type*/
		Type locationType = (Type) call.getLocation().accept(this);
		if (!(locationType instanceof ClassType )){
			throw new SemanticError(call.getLine(),"the call's expr does not have methods - isn't class type");
		}
		//get class name
		String class_name = ((ClassType)locationType).getName();
		//get class symbol table
		ClassSymbolTable class_symbolTable = get_class_table(class_name);
		//get method symbol
		MethodSymbol method_symbol = class_symbolTable.getVirtualMethod(call.getName());
		//get method type
		MethodType method_type = get_method_type_for_call(call, method_symbol);
		
		//check argument types
		checkArgsValidity(call, method_type);
		
		call.setNodeType(method_type.getReturnType());
		return method_type.getReturnType();
	}

	@Override
	public Type visit(This thisExpression) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(NewClass newClass) throws SemanticError {
		ClassType class_type = all_pos_types.type_map_class.get(newClass.getName());
		newClass.setNodeType(class_type);
		return class_type;
	}

	@Override
	public Type visit(NewArray newArray) throws SemanticError {
		//check size to be int
		Type size_type = (Type) newArray.getSize().accept(this);
		if (!(size_type instanceof IntType)){
			throw new SemanticError(newArray.getLine(),"array size should be integer");
		}
		//calc array type
		Type array_type = null;
		Type basic_type = (Type) newArray.getType().accept(this);
		if (basic_type instanceof ClassType){
			array_type = (Type) all_pos_types.type_map_arrays_class.get(((ClassType)basic_type).getName()).get(newArray.getSize());
		}
		else if (!(basic_type instanceof ArrayType)){
			array_type = (Type) all_pos_types.type_map_arrays_primitive.get(basic_type).get(newArray.getSize());
		}
		else{
			throw new SemanticError(newArray.getLine(),"basic type for new array can't be another array");
		}
		newArray.setNodeType(array_type);
		return array_type;
	}

	@Override
	public Type visit(Length length) throws SemanticError {
		//check expr to be array type
		Type expr_type = (Type) length.getArray().accept(this);
		if (!(expr_type instanceof ArrayType)){
			throw new SemanticError(length.getLine(),"no atribute length for objects which aren't arrays");
		}
		length.setNodeType(all_pos_types.type_map_primitive.get(DataTypes.INT));
		return length.getNodeType();
	}

	@Override
	public Type visit(MathBinaryOp binaryOp) throws SemanticError {
		Type side_1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type side_2 = (Type) binaryOp.getSecondOperand().accept(this);
		Type op_type;
		
		if ((side_1 instanceof StringType) && (side_2 instanceof StringType)){
			//check for concatenation
			if (binaryOp.getOperator() == BinaryOps.PLUS){
				op_type = all_pos_types.type_map_primitive.get(DataTypes.STRING);
			}
			else{
				throw new SemanticError(binaryOp.getLine(),"bad binary math operation with strings");
			}
		}
		else if ((side_1 instanceof IntType) && (side_2 instanceof IntType)){
			switch(binaryOp.getOperator()){
			case PLUS: case MINUS: case MULTIPLY:
			case DIVIDE: case MOD: 
				{
					op_type = all_pos_types.type_map_primitive.get(DataTypes.INT);
					break;
				}
			case LAND: case LOR: case LT:
			case LTE: case GT: case GTE:
			case EQUAL: case NEQUAL:
				{
					op_type = all_pos_types.type_map_primitive.get(DataTypes.BOOLEAN);
					break;
				}
			default:
				throw new SemanticError(binaryOp.getLine(),"bad binary math operation with ints");
			}
		}
		else{
			throw new SemanticError(binaryOp.getLine(),"bad binary math operation");
		}
		binaryOp.setNodeType(op_type);
		return op_type;
	}

	@Override
	public Type visit(LogicalBinaryOp binaryOp) throws SemanticError {
		Type side_1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type side_2 = (Type) binaryOp.getSecondOperand().accept(this);
		Type op_type;
		if ((side_1.subTypeOf(side_2)) || (side_2.subTypeOf(side_1))){
			switch(binaryOp.getOperator()){
			case LAND: case LOR: case LT:
			case LTE: case GT: case GTE:
			case EQUAL: case NEQUAL:
				{
					op_type = all_pos_types.type_map_primitive.get(DataTypes.BOOLEAN);
					break;
				}
			default:
				throw new SemanticError(binaryOp.getLine(),"bad binary logical operation with booleans");
			}
		}
		else{
			throw new SemanticError(binaryOp.getLine(),"bad binary logical operation");
		}
		binaryOp.setNodeType(op_type);
		return op_type;
	}

	@Override
	public Type visit(MathUnaryOp unaryOp) throws SemanticError {
		Type expr_type = (Type) unaryOp.getOperand().accept(this);
		if ((expr_type instanceof IntType)){
			unaryOp.setNodeType(all_pos_types.type_map_primitive.get(DataTypes.INT));
			return unaryOp.getNodeType();
		}
		else{
			throw new SemanticError(unaryOp.getLine(),"bad unary math operation");
		}
	}

	@Override
	public Type visit(LogicalUnaryOp unaryOp) throws SemanticError {
		Type expr_type = (Type) unaryOp.getOperand().accept(this);
		if ((expr_type instanceof BoolType)){
			unaryOp.setNodeType(all_pos_types.type_map_primitive.get(DataTypes.BOOLEAN));
			return unaryOp.getNodeType();
		}
		else{
			throw new SemanticError(unaryOp.getLine(),"bad unary bollean operation");
		}
	}

	@Override
	public Type visit(Literal literal) throws SemanticError {
		//all literals are strings
		switch(literal.getType()){
		case INTEGER:
			literal.setNodeType(all_pos_types.type_map_primitive.get(DataTypes.INT));
			break;
		case STRING:
			literal.setNodeType(all_pos_types.type_map_primitive.get(DataTypes.STRING));
			break;
		case FALSE:
			literal.setNodeType(all_pos_types.type_map_primitive.get(DataTypes.BOOLEAN));
			break;
		case NULL:
			literal.setNodeType(all_pos_types.type_map_primitive.get(null));
			break;
		case TRUE:
			literal.setNodeType(all_pos_types.type_map_primitive.get(DataTypes.BOOLEAN));
			break;
		default:
			throw new SemanticError(literal.getLine(),"literal problem");
		}
		
		return literal.getNodeType();
	}

	@Override
	public Type visit(ExpressionBlock expressionBlock) throws SemanticError {
		expressionBlock.setNodeType((Type) expressionBlock.getExpression().accept(this));
		return expressionBlock.getNodeType();
	}
	
	private Type method_visit(Method method) throws SemanticError{
		
		//get return type.
		MethodType method_type = (MethodType) method.getNodeType();
		
		//for special symbol $ret set type to be return_type. check proper return type in Return AST
		
		//no need to visit formals - already calculated type (no type errors in Formals AST node)
		
		for (Statement stmt : method.getStatements()){
			//visit into each statement
			stmt.accept(this);
		}
		
		return method_type;
	}
	
	//get class symbol table for class name
	private ClassSymbolTable get_class_table(String class_name) {
		//get class table 
		ClassSymbol class_symbol = globalSymbolTable.getClassSymbol(class_name);
		ClassSymbolTable class_symbolTable = class_symbol.getClassSymbolTable();
		return class_symbolTable;
	}
	
	//given call, get method type
	private MethodType get_method_type_for_call(Call call,MethodSymbol method_symbol) throws SemanticError {
		if (method_symbol == null){
			throw new SemanticError(call.getLine(),call.getName()+" - no such static method exist");
		}
		MethodType method_type = (MethodType) method_symbol.getType();
		return method_type;
	}
	
	//this method checks arguments validity for call
	private void checkArgsValidity(Call call, MethodType method_type) throws SemanticError {
		//visit all the call arguments
		List<Type> argumentsTypes = new ArrayList<Type>();
		for (Expression exp : call.getArguments()){
			argumentsTypes.add((Type) exp.accept(this));
		}
		
		//check that all the arguments are correct
		if (argumentsTypes.size() != method_type.getArgstypes().size()){
			throw new SemanticError(call.getLine(),"incorrect numbers of arguments for"+call.getName()+"call");
		}
		for (int i=0; i<argumentsTypes.size();i++){
			if (!argumentsTypes.get(i).subTypeOf(method_type.getArgstypes().get(i))){
				throw new SemanticError(call.getLine(),"incorrect argument type for argument number "+i+ " in "+call.getName()+"call");
			}
		}
	}


}
