package IC.Types;

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
import IC.AST.Visitor;
import IC.AST.While;
import IC.SemanticChecks.SemanticError;
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.GlobalSymbolTable;
import IC.SymTables.MethodSymbolTable;
import IC.SymTables.SymbolTable;
import IC.SymTables.VariableSymbolTable;
import IC.SymTables.Symbols.ClassSymbol;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.LocalVariableSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;




//TODO: probably its better to not return anything, and not pass the symbol table
// use ASTNODE.getEnclosing..
// set the type at the first chance you have
// when you see the method, set its type (already when visiting the class)

public class SymbolSetter implements Visitor{
	
	TypeTable all_pos_types;
	GlobalSymbolTable globalSymbolTable;
	
	public SymbolSetter(TypeTable table,GlobalSymbolTable globalSymbolTable){
		all_pos_types = table;
		this.globalSymbolTable = globalSymbolTable;
	}

	@Override
	public Type visit(Program program) throws SemanticError {

		
		// first, set build all class types on first iteration
		for (ICClass program_class : program.getClasses()){

			//get class symbol out of GlobalSymbolTable
			ClassSymbol class_symbol = globalSymbolTable.getClassSymbol(program_class.getName());
			//calc class type
			Type class_type = all_pos_types.add_class_type(program_class);
			//set class type
			class_symbol.setType(class_type);
		}
		
		//travel into classes
		for (ICClass program_class : program.getClasses()){
			program_class.accept(this);
		}
		return null;
	}

	@Override
	public Type visit(ICClass icClass) throws SemanticError {
		//get class symbol table from field enclosingTable or method enclosing table
		ClassSymbolTable classSymbolTable =null;
		
		if (icClass.getFields().size() >0){
			classSymbolTable= (ClassSymbolTable)icClass.getFields().get(0).enclosingScope();
		}
		else {
			if(icClass.getMethods().size() > 0  ){
				classSymbolTable= (ClassSymbolTable)icClass.getMethods().get(0).enclosingScope();
			}
		}
		
		//move over all fields and set their type.
		for (Field class_field : icClass.getFields()) {
			//calc type for field
			Type field_type  = (Type) class_field.accept(this);
			//get field symbol
			FieldSymbol field_symbol = classSymbolTable.getField(class_field.getName());
			//set type
			field_symbol.setType(field_type);
		}
		
		
		//now travel into each method
		for (Method class_method : icClass.getMethods()) {
			//calc type for method
			Type method_type = (Type) class_method.accept(this); 
			//get method symbol - can be virtual or static
			MethodSymbol method_symbol = classSymbolTable.getMethod(class_method.getName(), class_method.isStatic());
			//set to symbol
			method_symbol.setType(method_type);
		}

		//in case of error add_class_type will both return null and throw error
		return all_pos_types.type_map_class.get(icClass.getName());
	}

	@Override
	public Type visit(Field field) throws SemanticError {
		return (Type) field.getType().accept(this);
	}

	@Override
	public Type visit(VirtualMethod method) throws SemanticError {
		method_visit(method);
		return all_pos_types.add_method_type(method);
	}

	@Override
	public Type visit(StaticMethod method) throws SemanticError {

		method_visit(method);
		return all_pos_types.add_method_type(method);
	}



	@Override
	public Type visit(LibraryMethod method) throws SemanticError {
		
		formal_list_visit(method);
		
		//in library method there are no statements
		
		return all_pos_types.add_method_type(method);
	}

	@Override
	public Type visit(Formal formal) throws SemanticError {
		return (Type) formal.getType().accept(this);//formals type is defined by its "type" field
	}

	@Override
	public Type visit(PrimitiveType type) throws SemanticError {
		IC.Types.Type nodeType= null;
		if (type.getDimension() == 0){
			//we already entered into the table all primitive types - just get it
			nodeType= all_pos_types.type_map_primitive.get(type.getDataTypes());
		}
		else{
			//add new type of array if needed.
			nodeType = all_pos_types.addArrayType_primitive(type.getDataTypes(),type.getDimension());
		}
		return nodeType;
	}

	@Override
	public Type visit(UserType type) throws SemanticError {
		// when first visiting program, we created all class possible types
		ClassType class_type = all_pos_types.type_map_class.get(type.getName());
		//so if we didn't find the class, it isn't defined in the program
		if (class_type ==null){
			throw new SemanticError(type.getLine(),type.getName()+": no such class is defined");
		}
		return class_type;
	}

	@Override
	public Type visit(Assignment assignment) throws SemanticError {
		//travel into left side of assignment to set for name type
		assignment.getVariable().accept(this);
		
		return null;
	}

	@Override
	public Type visit(CallStatement callStatement) throws SemanticError {
		// CallStatement holds only a call and doesn't have a symbol
		
		return null;
	}

	@Override
	public Type visit(Return returnStatement) throws SemanticError {
		return null;//Return statement will be evaluated only in TypeBuilder
	}

	@Override
	public Type visit(If ifStatement) throws SemanticError {
		
		//visit into if block. 
		ifStatement.getOperation().accept(this);
		//visit into else block -if else  exist 
		if (ifStatement.getElseOperation() != null){
			//visit into else block
			ifStatement.getElseOperation().accept(this);
		}
		return null;
	}

	@Override
	public Type visit(While whileStatement) throws SemanticError {
		
		//visit into while block
		whileStatement.getOperation().accept(this);
		return null;
	}

	@Override
	public Type visit(Break breakStatement) throws SemanticError {
		//AST leaf without type
		return null;
	}

	@Override
	public Type visit(Continue continueStatement) throws SemanticError {
		//AST leaf without type
		return null;
	}

	//context is the appropriate symbol table for the statement block
	@Override
	public Type visit(StatementsBlock statementsBlock) throws SemanticError {
		//visit into each stmt
		for (Statement stmt : statementsBlock.getStatements()){
			stmt.accept(this);
		}
		return null;
	}

	@Override
	public Type visit(LocalVariable localVariable) throws SemanticError {
		//get localVariable type
		Type localVariable_type = (Type) localVariable.getType().accept(this);
		//get localVariable symbol
		Symbol localVariable_symbol = localVariable.enclosingScope().getVariable(localVariable.getName());
		//set type to symbol
		localVariable_symbol.setType(localVariable_type);
		
		return localVariable_type;
	}

	@Override
	public Type visit(VariableLocation location) throws SemanticError {
		//Warning-May be a problem
		return null;
	}

	@Override
	public Type visit(ArrayLocation location) throws SemanticError {
		//Warning-May be a problem
		return null;
	}

	@Override
	public Type visit(StaticCall call) throws SemanticError {
		//Warning-May be a problem
		return null;
	}

	@Override
	public Type visit(VirtualCall call) throws SemanticError {
		//Warning-May be a problem, same
		return null;
	}

	@Override
	public Type visit(This thisExpression) throws SemanticError {
		//calc this only in TypeBuilder
		return null;
	}

	@Override
	public Type visit(NewClass newClass) throws SemanticError {
		//calc this only in TypeBuilder
		return null;
	}

	@Override
	public Type visit(NewArray newArray) throws SemanticError {
		//calc this only in TypeBuilder
		return null;
	}

	@Override
	public Type visit(Length length) throws SemanticError {
		return null;
	}

	@Override
	public Type visit(MathBinaryOp binaryOp) throws SemanticError {
		return null;
	}

	@Override
	public Type visit(LogicalBinaryOp binaryOp) throws SemanticError {
		return null;
	}

	@Override
	public Type visit(MathUnaryOp unaryOp) throws SemanticError {
		return null;
	}

	@Override
	public Type visit(LogicalUnaryOp unaryOp) throws SemanticError {
		return null;
	}

	@Override
	public Type visit(Literal literal) throws SemanticError {
		return null;
	}

	@Override
	public Type visit(ExpressionBlock expressionBlock) throws SemanticError {
		
		//calc this only in TypeBuilder
		return null;
	}
	
	
	private void method_visit(Method method) throws SemanticError {
		
		//return type and argument type calculation for clalc method type are handled in add_mathod_type
		
		formal_list_visit(method);
		
		//visit all statements
		for (Statement stmt : method.getStatements()){
			stmt.accept(this);
		}
	}

	private void formal_list_visit(Method method)
			throws SemanticError {
		//visit all the arguments
		for (Formal arg : method.getFormals()){
			//get argument type
			Type arg_type = (Type) arg.accept(this);
			//get argument symbol
			Symbol arg_symbol =  ((MethodSymbolTable)arg.enclosingScope()).getVariable(arg.getName());
			//set type to symbol
			arg_symbol.setType(arg_type);
		}
	}

}
