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

import IC.SymTables.VariableSymbolTable;
import IC.SymTables.Symbols.ClassSymbol;
import IC.SymTables.Symbols.FieldSymbol;

import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;






public class SymbolSetter implements Visitor{
	
	TypeTable         typeTable;            
	GlobalSymbolTable globalSymbolTable;
	Program           prog;
	
	public SymbolSetter(Program prog, TypeTable table, GlobalSymbolTable globalSymbolTable){
		typeTable = table;
		this.globalSymbolTable = globalSymbolTable;
		this.prog = prog;
	}
	
	
	public void setSymbolTypes() throws SemanticError
	{
		prog.accept(this);
	}

	@Override
	public Type visit(Program program) throws SemanticError {

		
		// first, set build all class types on first iteration
		for (ICClass program_class : program.getClasses()){

			//get class symbol out of GlobalSymbolTable
			ClassSymbol class_symbol = globalSymbolTable.getClassSymbol(program_class.getName());
			//calc class type
			Type class_type = typeTable.getClassType(program_class);
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
		ClassSymbolTable classSymbolTable = null;
		
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

		
		//set type for class AST node
		icClass.setNodeType(typeTable.getClassType(icClass));
		return icClass.getNodeType();
	}

	@Override
	public Type visit(Field field) throws SemanticError {
		
		field.setNodeType((Type) field.getType().accept(this));
		return  field.getNodeType();
	}

	@Override
	public Type visit(VirtualMethod method) throws SemanticError {
		method_visit(method);
		return method.getNodeType();
	}

	@Override
	public Type visit(StaticMethod method) throws SemanticError {

		method_visit(method);
		return method.getNodeType();
	}



	@Override
	public Type visit(LibraryMethod method) throws SemanticError {
		
		method_visit(method);
		return method.getNodeType();
	}

	@Override
	public Type visit(Formal formal) throws SemanticError {
		formal.setNodeType((Type) formal.getType().accept(this));
		return  formal.getNodeType(); 
	}

	@Override
	public Type visit(PrimitiveType type) throws SemanticError {
		
		IC.Types.Type nodeType= typeTable.getType(type); /* get type object from type table */
		type.setNodeType(nodeType);
		return nodeType;
	}
	
	
	

	@Override
	public Type visit(UserType type) throws SemanticError {
		Type class_type = typeTable.getType(type);     /* get type object from type table */
		type.setNodeType(class_type);
		return class_type;
	}

	@Override
	public Type visit(Assignment assignment) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(CallStatement callStatement) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(Return returnStatement) throws SemanticError {
		return null;
	}

	@Override
	public Type visit(If ifStatement) throws SemanticError {
		
		//visit into if block. 
		ifStatement.getOperation().accept(this);
		//visit into else block -if else  exist 
		if (ifStatement.hasElse()){
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
		Symbol localVariable_symbol = ((VariableSymbolTable)localVariable.enclosingScope()).getVariableLocally(localVariable.getName());
		//set type to symbol
		localVariable_symbol.setType(localVariable_type);
		localVariable.setNodeType(localVariable_type);
		return localVariable_type;
	}

	@Override
	public Type visit(VariableLocation location) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(ArrayLocation location) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(StaticCall call) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(VirtualCall call) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(This thisExpression) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(NewClass newClass) throws SemanticError {
		
		return null;
	}

	@Override
	public Type visit(NewArray newArray) throws SemanticError {
		
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
		
		
		return null;
	}
	
	
	private void method_visit(Method method) throws SemanticError {
		

		
		formal_list_visit(method);
		
		//visit all statements
		for (Statement stmt : method.getStatements()){
			stmt.accept(this);
		}
		
		method.setNodeType(typeTable.getMethodType(method));
	}

	private void formal_list_visit(Method method)
			throws SemanticError {
		
		//visit all the arguments
		for (Formal arg : method.getFormals()){
			//get argument type
			Type arg_type = (Type) arg.accept(this);
			//get argument symbol
			Symbol arg_symbol =  ((MethodSymbolTable)arg.enclosingScope()).getVariableLocally(arg.getName());
			//set type to symbol
			arg_symbol.setType(arg_type);
		}
	}

}
