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
import IC.SymTables.Symbols.VariableSymbol;



//TODO: probably its better to not return anything, and not pass the symbol table
// use ASTNODE.getEnclosing..
// set the type at the first chance you have
// when you see the method, set its type (already when visiting the class)
// too many nulls are passed as arguments for no reason

public class SymbolSetter implements PropagatingVisitor<SymbolTable, Type>{
	
	TypeTable all_pos_types;
	
	public SymbolSetter(TypeTable table){
		all_pos_types = table;
	}

	@Override
	public Type visit(Program program, SymbolTable context) throws SemanticError {
		GlobalSymbolTable globalSymbolTable = (GlobalSymbolTable)context;
		
		// first, set build all class types on first iteration
		
		
		for (ICClass program_class : program.getClasses()){
			
			//get class symbol out of GlobalSymbolTable
			ClassSymbol class_symbol = globalSymbolTable.getClassSymbol(program_class.getName());
			//calc class type
			Type class_type = program_class.accept(this, context.getChildSymbolTableById(program_class.getName()));
			//set class type
			class_symbol.setType(class_type);
			
		}
		return null;
	}

	@Override
	public Type visit(ICClass icClass, SymbolTable context) throws SemanticError {
		
		ClassSymbolTable classSymbolTable = (ClassSymbolTable)context;
		//move over all fields and set their type.
		
		for (Field class_field : icClass.getFields()) {
			//calc type for field
			Type field_type  = class_field.accept(this, null);
			//get field symbol
			FieldSymbol field_symbol = classSymbolTable.getField(class_field.getName());
			//set type
			field_symbol.setType(field_type);
		}
		
		//now travel into each method
		for (Method class_method : icClass.getMethods()) {
			//calc type for method
			Type method_type = class_method.accept(this, classSymbolTable.getChildSymbolTableById(class_method.getName())); 
			//get method symbol - can be virtual or static
			MethodSymbol method_symbol = classSymbolTable.getMethod(class_method.getName());
			//set to symbol
			method_symbol.setType(method_type);
		}

		//in case of error add_class_type will both return null and throw error
		return all_pos_types.add_class_type(icClass);
	}

	@Override
	public Type visit(Field field, SymbolTable context) throws SemanticError {
		return field.getType().accept(this, null);
	}

	@Override
	public Type visit(VirtualMethod method, SymbolTable context) throws SemanticError {
		//no need to visit method return type- there is no symbol for it (??)
		
		MethodSymbolTable symTable = (MethodSymbolTable)context;
		
		//TODO: where do you set the return type ?
		
		//visit all the arguments
		for (Formal arg : method.getFormals()){
			//get argument type
			Type arg_type = arg.accept(this, null);
			//get argument symbol
			VariableSymbol arg_symbol =  symTable.getVariable(arg.getName());
			//set type to symbol
			arg_symbol.setType(arg_type);
		}
		
		//visit all statements
		for (Statement stmt : method.getStatements()){
			SymbolTable stmt_table = getStmtContext(symTable, stmt); 
			
			stmt.accept(this, stmt_table);
		}
		
		return all_pos_types.add_mathod_type(method);
	}

	@Override
	public Type visit(StaticMethod method, SymbolTable context) throws SemanticError {
		//no need to visit method return type- there is no symbol for it
		
		MethodSymbolTable symTable = (MethodSymbolTable)context;
		
		//TODO: where do you set the return type ?
		
		//visit all the arguments
		for (Formal arg : method.getFormals()){
			//get argument type
			Type arg_type = arg.accept(this, null);
			//get argument symbol
			VariableSymbol arg_symbol =  symTable.getVariable(arg.getName());
			//set type to symbol
			arg_symbol.setType(arg_type);
		}
		
		//visit all statements.statements does not have symbols, so just visit.
		for (Statement stmt : method.getStatements()){
			SymbolTable stmt_table = getStmtContext(symTable, stmt); 
			
			stmt.accept(this, stmt_table);
		}
		
		return all_pos_types.add_mathod_type(method);
	}

	@Override
	public Type visit(LibraryMethod method, SymbolTable context) throws SemanticError {
		//no need to visit method return type- there is no symbol for it
		
		MethodSymbolTable symTable = (MethodSymbolTable)context;
		
		//TODO: where do you set the return type ?
		//TODO: too much code duplication, take out common implementation
		
		//visit all the arguments
		for (Formal arg : method.getFormals()){
			//get argument type
			Type arg_type = arg.accept(this, null);
			//get argument symbol
			VariableSymbol arg_symbol =  symTable.getVariable(arg.getName());
			//set type to symbol
			arg_symbol.setType(arg_type);
		}
		
		//in library method there are no statements
		
		//TODO: TYPO : mathod -> method
		
		return all_pos_types.add_mathod_type(method);
	}

	@Override
	public Type visit(Formal formal, SymbolTable context) throws SemanticError {
		return formal.getType().accept(this, null);//formals type is defined by its "type" field
	}

	@Override
	public Type visit(PrimitiveType type, SymbolTable context) throws SemanticError {
		IC.Types.Type nodeType = null;
		if (type.getDimension() == 0){
			//we already entered into the table all primitive types - just get it
			nodeType = all_pos_types.type_map_primitive.get(type.getDataTypes());
		}
		else{
			//add new type of array if needed.
			nodeType = all_pos_types.addArrayType_primitive(type.getDataTypes(),type.getDimension());
		}
		return nodeType;
	}

	@Override
	public Type visit(UserType type, SymbolTable context) throws SemanticError {
		// there is no forward reference for classes
		
		//TODO: this is wrong in this case, you visit classes one by one, a good chance that
		//you have not created this a class type yet
		
		return all_pos_types.type_map_class.get(type.getName());
	}

	@Override
	public Type visit(Assignment assignment, SymbolTable context) throws SemanticError {
		//travel into left side of assignment to set for name type
		assignment.getVariable().accept(this, context);
		
		//TODO : what about the other side ? you must visit ALL expressions
		//assignment.getAssignment(); 
		
		return null;
	}

	@Override
	public Type visit(CallStatement callStatement, SymbolTable context) throws SemanticError {
		// CallStatement holds only a call and doesn't have a symbol
		//TODO: Nop, also has expressions, may create new types
		
		return null;
	}

	@Override
	public Type visit(Return returnStatement, SymbolTable context)throws SemanticError {
		
		//TODO gotta visit expression
		
		return null;//Return statement will be evaluated only in TypeBuilder
	}

	@Override
	public Type visit(If ifStatement, SymbolTable context) throws SemanticError {
		
		//TODO: probably better to just visit each statement
		
		//get other symbol table if .getOperation returns Statement block
		SymbolTable if_operation_symbol_table = getStmtContext(context,ifStatement.getOperation());
		//visit into if block
		ifStatement.getOperation().accept(this, if_operation_symbol_table);
		//visit into else block -if else  exist 
		if (ifStatement.getElseOperation() != null){
			//get other symbol table if .getElseOperation returns Statement block
			SymbolTable else_operation_symbol_table = getStmtContext(context,ifStatement.getElseOperation());
			//visit into else block
			ifStatement.getElseOperation().accept(this, else_operation_symbol_table);
		}
		return null;
	}

	@Override
	public Type visit(While whileStatement, SymbolTable context) throws SemanticError {
		//TODO: same as if
		
		//get other symbol table if .getOperation returns Statement block
		SymbolTable while_operation_symbol_table = getStmtContext(context,whileStatement.getOperation());
		//visit into while block
		whileStatement.getOperation().accept(this, while_operation_symbol_table);
		return null;
	}

	@Override
	public Type visit(Break breakStatement, SymbolTable context) throws SemanticError {
		//AST leaf without type
		return null;
	}

	@Override
	public Type visit(Continue continueStatement, SymbolTable context) throws SemanticError {
		//AST leaf without type
		return null;
	}

	//context is the appropriate symbol table for the statement block
	@Override
	public Type visit(StatementsBlock statementsBlock, SymbolTable context) throws SemanticError {
		//visit into each stmt
		for (Statement stmt : statementsBlock.getStatements()){
			stmt.accept(this, getStmtContext(context,stmt));
		}
		return null;
	}

	@Override
	public Type visit(LocalVariable localVariable, SymbolTable context) throws SemanticError {
		//get localVariable type
		Type localVariable_type = localVariable.getType().accept(this, null);
		//get localVariable symbol
		VariableSymbol localVariable_symbol = ((VariableSymbolTable)context).getVariable(localVariable.getName());
		//set type to symbol
		localVariable_symbol.setType(localVariable_type);
		
		//TODO : visit localVariable.getInitValue() (if not null) ; 
		return localVariable_type;
	}

	@Override
	public Type visit(VariableLocation location, SymbolTable context) throws SemanticError {
		//Warning-May be a problem
		//TODO: yep, gotta visit sub experssions
		return null;
	}

	@Override
	public Type visit(ArrayLocation location, SymbolTable context) throws SemanticError {
		//Warning-May be a problem
		//TODO: yep, gotta visit sub experssions
		return null;
	}

	@Override
	public Type visit(StaticCall call, SymbolTable context) throws SemanticError {
		//Warning-May be a problem
		//TODO: yep, gotta visit sub experssions
	//	System.err.println("should no be here - StaticCall"); (will visit this node)
		return null;
	}

	@Override
	public Type visit(VirtualCall call, SymbolTable context) throws SemanticError {
		//Warning-May be a problem, same
		//System.err.println("should no be here - VirtualCall");
		return null;
	}

	@Override
	public Type visit(This thisExpression, SymbolTable context)throws SemanticError {
		//calc this only in TypeBuilder
		return null;
	}

	@Override
	public Type visit(NewClass newClass, SymbolTable context) throws SemanticError {
		//calc this only in TypeBuilder
		return null;
	}

	@Override
	public Type visit(NewArray newArray, SymbolTable context) throws SemanticError {
		//calc this only in TypeBuilder
		//TODO: sub expressions
		return null;
	}

	@Override
	public Type visit(Length length, SymbolTable context) throws SemanticError {
		//TODO: sub expressions
		return null;
	}

	@Override
	public Type visit(MathBinaryOp binaryOp, SymbolTable context)
			throws SemanticError {
		//TODO: sub expressions
		return null;
	}

	@Override
	public Type visit(LogicalBinaryOp binaryOp, SymbolTable context)
			throws SemanticError {
		//TODO sub expressions
		return null;
	}

	@Override
	public Type visit(MathUnaryOp unaryOp, SymbolTable context)
			throws SemanticError {
		//TODO sub expressions
		return null;
	}

	@Override
	public Type visit(LogicalUnaryOp unaryOp, SymbolTable context)
			throws SemanticError {
		//TODO sub expressions
		return null;
	}

	@Override
	public Type visit(Literal literal, SymbolTable context)
			throws SemanticError {
		return null;
	}

	@Override
	public Type visit(ExpressionBlock expressionBlock, SymbolTable context)
			throws SemanticError {
		
		//calc this only in TypeBuilder
		//System.err.println("should no be here - ExpressionBlock");
		//TODO sub expressions
		return null;
	}
	
	//every type we travel into a statement' it may be StatementsBlock- so get the right SymbolTable
	private SymbolTable getStmtContext(SymbolTable context, Statement stmt) {
		//if this is a , that we should give it another symbol table
		SymbolTable stmt_table = context;
		int stmt_block_counter =0;
		if (stmt instanceof StatementsBlock ){
			stmt_table = context.getChildrenTables().get(stmt_block_counter);
		}
		return stmt_table;
		
		//TODO: you probably dont need this method
	}

}
