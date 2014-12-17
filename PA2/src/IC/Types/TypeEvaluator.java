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
	
	TypeTable typeTable;
	GlobalSymbolTable globalSymbolTable;
	
	public TypeEvaluator(TypeTable table, GlobalSymbolTable globalSymbolTable){
		typeTable = table;
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
		//no need to visit fields- they can't be initialized in IC in Field AST node;
		
		//move over all methods
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
		
		type.setNodeType(typeTable.getType(type));
		return type.getNodeType();
	}

	@Override
	public Type visit(UserType type) throws SemanticError {
		
		type.setNodeType(typeTable.getType(type));
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
			String err = String.format("type of right hand side of assignment %s is not sub type of left hand side %s", right_side_type, left_side_type);
			throw new SemanticError(assignment.getLine(), err);
		}
		
		return null;
	}

	@Override
	public Type visit(CallStatement callStatement) throws SemanticError {
		//visit call
		callStatement.getCall().accept(this);
		
		return null;
	}

	@Override
	public Type visit(Return returnStatement) throws SemanticError {
		
		Type return_stmt_type;
		if (returnStatement.getValue() == null){
			//this case covers "return ;"
			return_stmt_type =  null;
		}
		else{
			return_stmt_type =  (Type) returnStatement.getValue().accept(this);
		}
		
		Type method_return_type = ((VariableSymbolTable)returnStatement.enclosingScope()).getReturnType();
		if (return_stmt_type == null){
			if (!(method_return_type instanceof VoidType))
			{
				throw new SemanticError(returnStatement.getLine(),"method return type isn't void - return command must return a value of type " + method_return_type);
			}
		}
		else if (method_return_type instanceof VoidType && return_stmt_type != null)
		{
			String err = String.format("method return type is void, can not return a value");
			throw new SemanticError(returnStatement.getLine(), err);
		}
		else if (!return_stmt_type.subTypeOf(method_return_type)){
			String err = String.format("returned value's type (%s) is not a subtype of method's return type (%s)", return_stmt_type, method_return_type);
			throw new SemanticError(returnStatement.getLine(), err);
			
		}
		
		return null;
	}

	@Override
	public Type visit(If ifStatement) throws SemanticError {
		//check condition to be boolean
		Type cond_type = (Type) ifStatement.getCondition().accept(this);
		if (cond_type != typeTable.getPrimitiveType(DataTypes.BOOLEAN)){
			throw new SemanticError(ifStatement.getLine(),"if condition isn't of type boolean");
		}
		//visit into if stmt and else stmt
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse()){
			ifStatement.getElseOperation().accept(this);
		}
		return null;
	}

	@Override
	public Type visit(While whileStatement) throws SemanticError {
		//check condition to be boolean
		Type cond_type = (Type) whileStatement.getCondition().accept(this);
		if (cond_type != typeTable.getPrimitiveType(DataTypes.BOOLEAN)){
			
			throw new SemanticError(whileStatement.getLine(),"while condition isn't of type boolean");
		}
		//visit into operation
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
		//already calculated left hand side type. now check that init Value type is corresponding.
		
		if (localVariable.hasInitValue()){
			Type init_type = (Type) localVariable.getInitValue().accept(this);
			if (!init_type.subTypeOf(localVariable.getNodeType())){
				
				String err = String.format("init value type (%s) is not a subtype of declaration type (%s)", init_type, localVariable.getNodeType());
				throw new SemanticError(localVariable.getLine(), err);
			}
			
		}
		return null;

	}

	@Override
	public Type visit(VariableLocation location) throws SemanticError {
		//get variable symbol to extract type (set in symbolSetter)
		Symbol var_symbol;
		if (location.isExternal()){
			
			//get type of expr out of expr.id
			Type location_type = (Type) location.getLocation().accept(this);
			
			//if location.getLocation() isn't a class, than there is a error ( (3+4).x )
			if (location_type instanceof ClassType){
				//get class symbol table
				String className = ((ClassType) location_type).getName();
				ClassSymbolTable class_symbol_table = get_class_table(className);
				//get id symbol from class table
				var_symbol = class_symbol_table.getField(location.getName());
				
				if(var_symbol == null)
				{
					// no such field exists
					throw new SemanticError(location.getLine(), String.format("Field %s does not exist in class %s or its superclasses", location.getName(),className));
				}
				
			}
			else {
				String err = String.format("type of location (prefix before .) is not a class type (but rather %s)", location_type);
				throw new SemanticError(location.getLine(),err);
			}
		}
		else {
			//if no expr was given, this is a variable from this scope
			var_symbol =  location.getDefiningSymbol();
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
		if (index_type != typeTable.getPrimitiveType(DataTypes.INT)){
			throw new SemanticError(location.getLine(),"index of array is not an integer, but rather of type " + index_type);
		}
		
		Type array_type = (Type) location.getArray().accept(this);
		if(!(array_type instanceof ArrayType))
		{
			String err = String.format("location of array is not of array type, but rather of type %s", array_type);
			throw new SemanticError(location.getLine(), err);
		}
		
		location.setNodeType(typeTable.getArrayTypeItemType((ArrayType)array_type));
		
		return location.getNodeType();
		
	}

	@Override
	public Type visit(StaticCall call) throws SemanticError {
		//get class symbol table, class exists as determined in previous phase
		ClassSymbolTable classSymbolTable = get_class_table(call.getClassName());
		
		//get static method symbol
		MethodSymbol method_symbol = classSymbolTable.getMethod(call.getName(), true);
		
		if(method_symbol == null)
		{
			throw new SemanticError(call.getLine(), "class " + call.getClassName() + " (or its superclasses) does not contain static method " + call.getName());
		}
		
		//get method type
		MethodType method_type = (MethodType) method_symbol.getType();
		
		//check argument types
		checkArgsValidity(call, method_type);
		
		call.setNodeType(method_type.getReturnType());
		return method_type.getReturnType();
	}

	@Override
	public Type visit(VirtualCall call) throws SemanticError {

		MethodSymbol methodSym;
		
		if(call.isExternal())
		{
			Type locationType = (Type) call.getLocation().accept(this);
			if (!(locationType instanceof ClassType )){
				throw new SemanticError(call.getLine(), "location type is not of class type, but rather of type " + locationType);
			}
			//get class name
			String class_name = ((ClassType)locationType).getName();
			
			//get class symbol table
			ClassSymbolTable class_symbolTable = get_class_table(class_name);
			
			/* note: class_symbolTable is not null , class must be defined since it has a type*/
			
			//get virtual method symbol
			
			methodSym  = class_symbolTable.getMethod(call.getName(), false);
			
			if(methodSym == null)
			{
				throw new SemanticError(call.getLine(), "class " +class_symbolTable.getId() + " (or its superclasses) does not contain virtual method " + call.getName());
			}
				
		}
		else
		{
			
			
			//get method symbol
			methodSym = ((VariableSymbolTable)call.enclosingScope()).getMethod(call.getName());
			
			if(methodSym == null)
			{
				String error = String.format("method %s could not be resolved in current scope", call.getName());
				throw new SemanticError(call.getLine(), error);
			}
			
		}
	
	
		//get method type
		MethodType method_type = (MethodType) methodSym.getType();
		
		//check argument types
		checkArgsValidity(call, method_type);
		
		call.setNodeType(method_type.getReturnType());
		return method_type.getReturnType();
	}

	@Override
	public Type visit(This thisExpression) throws SemanticError {
		
		thisExpression.setNodeType(((VariableSymbolTable)thisExpression.enclosingScope()).getThisType());
		return thisExpression.getNodeType();
	}

	@Override
	public Type visit(NewClass newClass) throws SemanticError {
		
		/* class should exist, determined in previous phase */
		
		return newClass.getNodeType();
	}

	@Override
	public Type visit(NewArray newArray) throws SemanticError {
		
		//check size to be int
		Type size_type = (Type) newArray.getSize().accept(this);
		if (!(size_type instanceof IntType)){
			throw new SemanticError(newArray.getLine(),"array size should be of type integer, currently it is of type " + size_type);
		}
		
		// new array type was set in previous phase
		return newArray.getNodeType();
	}

	

	@Override
	public Type visit(Length length) throws SemanticError {
		//check expr to be array type
		Type expr_type = (Type) length.getArray().accept(this);
		if (!(expr_type instanceof ArrayType)){
			throw new SemanticError(length.getLine(),"length operator can only be used on expression of array type, instead found " + expr_type);
		}
		
		length.setNodeType(typeTable.getIntType());
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
				op_type = typeTable.getStringType();
			}
			else{
				throw new SemanticError(binaryOp.getLine(),"invalid operator: " + binaryOp.getOperator() + " , used on two string operands");
			}
		}
		else if ((side_1 instanceof IntType) && (side_2 instanceof IntType)){
			
			op_type =  typeTable.getIntType();
		}
		else{
			String err = String.format("invalid operand types: %s %s used with binary math operator: %s", side_1, side_2, binaryOp.getOperator());
			throw new SemanticError(binaryOp.getLine(), err);
		}
		
		binaryOp.setNodeType(op_type);
		return op_type;
	}

	@Override
	public Type visit(LogicalBinaryOp binaryOp) throws SemanticError {
		
		Type side_1 = (Type) binaryOp.getFirstOperand().accept(this);
		Type side_2 = (Type) binaryOp.getSecondOperand().accept(this);
		Type op_type = null;
		
		
		switch(binaryOp.getOperator())
		{
		
		case EQUAL: case NEQUAL:
		{
			if(side_1.subTypeOf(side_2)  || side_2.subTypeOf(side_1))
			{
				op_type =  typeTable.getBooleanType();
			}
			else
			{
				String err = String.format("invalid comparsion, lhs: %s is not sub type of rhs: %s or vice versa", side_1, side_2);
				throw new SemanticError(binaryOp.getLine(), err);
			}
			break;
		}
		case LT: case LTE : case GT : case GTE:
		{
			if(side_1 == typeTable.getIntType() && side_2 == typeTable.getIntType())
			{
				op_type =  typeTable.getBooleanType();
			}
			else
			{
				String err = String.format("both operands must be of type integer when using %s operator, instead, lhs: %s, rhs: %s", binaryOp.getOperator(), side_1, side_2);
				throw new SemanticError(binaryOp.getLine(), err);
			}
			break;
		}
		case LAND: case LOR :
		{
			if(side_1 == typeTable.getBooleanType() && side_2 == typeTable.getBooleanType())
			{
				op_type =  typeTable.getBooleanType();
			}
			else
			{
				String err = String.format("both operands must be of type boolean when using %s operator, instead, lhs: %s, rhs: %s", binaryOp.getOperator(), side_1, side_2);
				throw new SemanticError(binaryOp.getLine(), err);
			}
			break;
		}
		default: 
			// can't happen
		}
		
		binaryOp.setNodeType(op_type);
		return op_type;
	}

	@Override
	public Type visit(MathUnaryOp unaryOp) throws SemanticError {
		Type expr_type = (Type) unaryOp.getOperand().accept(this);
		if ((expr_type instanceof IntType)){
			unaryOp.setNodeType(typeTable.getIntType());
			return unaryOp.getNodeType();
		}
		else{
			throw new SemanticError(unaryOp.getLine(),"unary minus used on non integer operand, operand of type " + expr_type + " was given");
		}
	}

	@Override
	public Type visit(LogicalUnaryOp unaryOp) throws SemanticError {
		Type expr_type = (Type) unaryOp.getOperand().accept(this);
		if ((expr_type instanceof BoolType)){
			unaryOp.setNodeType( typeTable.getBooleanType());
			return unaryOp.getNodeType();
		}
		else{
			throw new SemanticError(unaryOp.getLine(),"! operator used on non boolean operand, operand of type " + expr_type + " was given");
		}
	}

	@Override
	public Type visit(Literal literal) throws SemanticError {
		
		switch(literal.getType()){
		case INTEGER:
			literal.setNodeType( typeTable.getIntType() );
			break;
		case STRING:
			literal.setNodeType(  typeTable.getStringType() );
			break;
		case FALSE:
			literal.setNodeType( typeTable.getBooleanType() );
			break;
		case NULL:
			literal.setNodeType(  typeTable.getNullType());
			break;
		case TRUE:
			literal.setNodeType( typeTable.getBooleanType());
			break;
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
		
		//no need to visit formals - already calculated type (no type errors in Formals AST node)
		
		for (Statement stmt : method.getStatements()){
			//visit into each statement
			stmt.accept(this);
		}
		
		return method_type;
	}
	

	/*
	 * get class symbol table by given class name
	 * returns null if no such class exists
	 */
	private ClassSymbolTable get_class_table(String class_name) {
		//get class table 
		ClassSymbol class_symbol = globalSymbolTable.getClassSymbol(class_name);
		if(class_symbol == null) /* class may not exist */
			return null;
		
		ClassSymbolTable class_symbolTable = class_symbol.getClassSymbolTable();
		return class_symbolTable;
	}
	

	
	/*
	 * this method checks that the types of arguments given to the call are valid
	 * meaning that, exactly same number of arguments and values' types are subtypes of the formals
	 */
	private void checkArgsValidity(Call call, MethodType method_type) throws SemanticError {
		//visit all the call arguments
		
		List<Type> argumentsTypes = new ArrayList<Type>();
		for (Expression exp : call.getArguments()){
			argumentsTypes.add((Type) exp.accept(this));
		}
		
		//check that all the arguments are correct
		if (argumentsTypes.size() != method_type.getArgstypes().size()){
			throw new SemanticError(call.getLine(),"incorrect number of arguments for"+call.getName()+"call, should be " + method_type.getArgstypes().size());
		}
		for (int i=0; i< argumentsTypes.size(); i++){
			if (!argumentsTypes.get(i).subTypeOf(method_type.getArgstypes().get(i))){
				throw new SemanticError(call.getLine(),"incorrect argument type for argument number "+i+ " in call of "+call.getName()+", arg type " + argumentsTypes.get(i) + " is not a subtype of " + method_type.getArgstypes().get(i));
			}
		}
	}


}
