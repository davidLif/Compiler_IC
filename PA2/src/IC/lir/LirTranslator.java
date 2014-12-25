package IC.lir;

import java.util.ArrayList;
import java.util.List;

import IC.LiteralTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
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
import IC.AST.While;
import IC.SemanticChecks.SemanticError;
import IC.SymTables.VariableSymbolTable;
import IC.SymTables.Symbols.Symbol;
import IC.Types.Type;
import IC.lir.lirAST.ArrayLengthNode;
import IC.lir.lirAST.BinaryInstructionNode;
import IC.lir.lirAST.CompareNode;
import IC.lir.lirAST.DispatchTableNode;
import IC.lir.lirAST.Immediate;
import IC.lir.lirAST.JumpFalse;
import IC.lir.lirAST.JumpNode;
import IC.lir.lirAST.Label;
import IC.lir.lirAST.LabelNode;
import IC.lir.lirAST.LirNode;
import IC.lir.lirAST.LirProgram;
import IC.lir.lirAST.Memory;
import IC.lir.lirAST.Memory.MemoryKind;
import IC.lir.lirAST.MethodNode;
import IC.lir.lirAST.MoveNode;
import IC.lir.lirAST.Reg;
import IC.lir.lirAST.ReturnNode;
import IC.lir.lirAST.StringLiteralNode;
import IC.lir.lirAST.UnaryInstructionNode;
import IC.lir.lirAST.lirBinaryOp;
import IC.lir.lirAST.lirUnaryOp;

public class LirTranslator implements IC.AST.Visitor {


	/**
	 * labelGenerator used to generate all labels in the program (except variable names)
	 */
	
	private LabelGenerator labelGenerator;
	
	
	/**
	 * used to build the class layout objects and fetch dispatch tables
	 */
	
	private ClassLayoutManager classManager;
	
	
	
	/**
	 * collects all the string literal definitions (Lir nodes) that we created
	 */
	private List<StringLiteralNode> stringDefinitions;
	
	
	/**
	 * used to generate all the variable names in the program
	 * should be allocated each time a method is visited
	 */
	private VariableNameGenerator  varNameGen;
	
	
	/**
	 * the IC program root
	 */
	private Program   icProgram;
	
	
	/**
	 * holds the next register index to be allocated
	 * thus, if currentRegister > 0 then currentRegister - 1 is the current maximum register index
	 */
	
	private int currentRegister = 0 ;
	
	
	/**
	 * current class name that we're visiting
	 */
	private String currentClassName = null; 
	
	/**
	 * this to labels are used so break and continue jumps will konow which labels should use
	 */
	private Label head_loop_label = null;
	private Label tail_loop_label = null;
	
	private boolean debug = true;
	
	
	public LirTranslator(Program program)
	{
		
		/* init data structures and fields */
		
		this.icProgram = program;
		
		labelGenerator = new LabelGenerator();
		
		/* this creates the class layouts and dispatch tables */
		
		classManager   = new ClassLayoutManager(icProgram);
		
		/* saves all the string definitions we've seen, note that the type of the list is a LIR node */
		
		stringDefinitions = new ArrayList<StringLiteralNode>();
		
	}
	
	
	public LirProgram translate()
	{
		
		LirProgram lirProg = null;
		
		try {
			lirProg = (LirProgram) this.icProgram.accept(this);
			
			
		} catch (SemanticError e) {

			// this cannot happen
			// the visitor does not throw any errors, but we must catch it because
			// the accept itself throws an error
		}
		
		
		return lirProg;
		
		
		
	}
	
	@Override
	public LirProgram visit(Program program) throws SemanticError {

		
		List<MethodNode> programMethods = new ArrayList<MethodNode>();   // list of lir methods
		
		for(ICClass currClass : program.getClasses())
		{
			// go over each class and get all its lir methods
			
			if(currClass.getName().equals("Library"))
				continue;
			
			programMethods.addAll(this.visit(currClass));
		}
		
		List<DispatchTableNode> dispatchTables = classManager.getAllDispatchTables(labelGenerator);
		
		// use all the data we gathered: dispatch tables, string literals and methods
		
		return new LirProgram(stringDefinitions, dispatchTables, programMethods);
		
	}

	@Override
	public List<MethodNode> visit(ICClass icClass) throws SemanticError {
		
		
		// visit each method, we need to find all the statements
		
		List<MethodNode> lirMethods = new ArrayList<MethodNode>();
		
		for(Method method : icClass.getMethods())
		{
			
			
			// create a new variable generator instance
			varNameGen = new VariableNameGenerator();
			
			// update current class
			this.currentClassName = icClass.getName();
			
			// build the lir method
			MethodNode currentLirMethod = (MethodNode)method.accept(this);
			
			// add it to the list
			lirMethods.add(currentLirMethod);
			
		}
		
		return lirMethods;
	}

	@Override
	public Object visit(Field field)  {
		
		// should not be reached
		
		return null;
	}
	
	
	private MethodNode common_method_visit( Method method) throws SemanticError
	{
		
		// get method label
		Label methodLabel;
		
		if(method.isStatic())
			methodLabel  = this.labelGenerator.getStaticMethodLabel(method.getName(), this.currentClassName);
		else
			methodLabel = this.labelGenerator.getVirtualMethodLabel(method.getName(), this.currentClassName);
		
		// create lir instructions from statements
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		
		
		// debug
		/*if(debug)
			return new MethodNode(methodLabel, instructions);*/
		//
		
		for(Statement stmt : method.getStatements())
		{
			// generate instructions from statement

			@SuppressWarnings("unchecked")
			List<LirNode> statementInstructions = (List<LirNode>)stmt.accept(this);
			
			// add all new instructions.some statements like "int x;" may not generate instructions 
			if(statementInstructions != null){
				instructions.addAll(statementInstructions);
			}
			
		}
		
		
		return new MethodNode(methodLabel, instructions);
		
	}
	

	@Override
	public MethodNode visit(VirtualMethod method) throws SemanticError  {
		
		return common_method_visit(method);
		
		
		
	}

	@Override
	public MethodNode visit(StaticMethod method) throws SemanticError  {
		
		return common_method_visit(method);
	}

	@Override
	public Object visit(LibraryMethod method)  {
		// this node wont be visited
		// infact all library class wont be visited
		return null;
	}
	
	/*
	 * Return type of all visit of all expressions will be the return temp name
	*/

	@Override
	public Object visit(Formal formal)  {
		// no lir instructions generated for formals, wont be visited
		return null;
	}

	@Override
	public Object visit(PrimitiveType type)  {
		// no lir instructions generated for "int" or such
		return null;
	}

	@Override
	public Object visit(UserType type)  {
		// no lir instructions generated for "A a" or such
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(Assignment assignment) throws SemanticError {
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		//optimization 1: Move R0,x - in opt 1 case no need to enter assignment.getVariable()
		if(assignment.getVariable() instanceof VariableLocation 
				&& ((VariableLocation)assignment.getVariable()).isExternal()==false){
			Symbol var_symbol = ((VariableLocation)assignment.getVariable()).getDefiningSymbol();
			Memory var= new Memory(varNameGen.getVariableName(var_symbol),MemoryKind.LOCAL);
			return local_var_assgiment(var,assignment.getAssignment());
		}
		
		instructions.addAll((List<LirNode>)assignment.getVariable().accept(this));
		currentRegister++;//return register is currentRegister
		
		//return register is currentRegister+1 (or currentRegister if condition true
		instructions.addAll((List<LirNode>)assignment.getAssignment().accept(this));
		
		//MOVE currentRegister+1,currentRegister
		instructions.add(new MoveNode(new Reg(currentRegister),new Reg(currentRegister-1)));
		currentRegister--;//return register is currentRegister
			
		//TODO: move currentRegister into the variable - do this by creating "saving backwards" method, that will do this with assignment.getVariable() 
		
		return instructions;
	}


	@SuppressWarnings("unchecked")
	private List<LirNode> local_var_assgiment(Memory var,Expression assignment) throws SemanticError{
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		//return register is currentRegister+1 (or currentRegister if condition true
		instructions.addAll((List<LirNode>)assignment.accept(this));
		//Move R0,x (or y or any such)
		instructions.add(new MoveNode(new Reg(currentRegister),var));
		
		return instructions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(CallStatement callStatement) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//push all arguments into registers
		for (Expression arg:callStatement.getCall().getArguments()){
			//calc arg i and save arg i in currentRegister+i
			instructions.addAll((List<LirNode>)arg.accept(this));
			currentRegister++;
		}
		
		//make call and save to register currentRegister + num_of_parameters
		instructions.addAll((List<LirNode>)callStatement.accept(this));
		
		//after call, arguments are no longer needed. free all the registers which we used to save them
		for (int i=0; i<callStatement.getCall().getArguments().size();i++){
			currentRegister--;
		}
		return instructions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(Return returnStatement) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		if (returnStatement.getValue() != null){
			//evaluate return expression into currentRegister
			instructions.addAll((List<LirNode>)returnStatement.getValue().accept(this));
		}
		
		//Return currentRegister - in case of return void return junk
		instructions.add(new ReturnNode(new Reg(currentRegister)));
		return instructions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(If ifStatement) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//make labels
		Label else_label = null;
		if(ifStatement.hasElse()){
			else_label = labelGenerator.createLabel();
		}
		Label end_if_else_label = labelGenerator.createLabel();
		
		//evaluate condition expression into currentRegister
		instructions.addAll((List<LirNode>)ifStatement.getCondition().accept(this));
		
		//compare and jump to else if exist and out of if if doesn't
		instructions.add(new CompareNode(new Immediate(0),new Reg(currentRegister)));
		if(ifStatement.hasElse()){
			instructions.add(new JumpFalse(else_label));
		}
		else{
			instructions.add(new JumpFalse(end_if_else_label));
		}
		
		//if we enter if
		instructions.addAll((List<LirNode>)ifStatement.getOperation().accept(this));
		
		
		if(ifStatement.hasElse()){
			//jump to avoid else - if else exist
			instructions.add(new JumpNode(end_if_else_label));
			
			//add else part - else label and code
			instructions.add(new LabelNode(else_label));
			instructions.addAll((List<LirNode>)ifStatement.getElseOperation().accept(this));
		}
		
		//set end of if label
		instructions.add(new LabelNode(end_if_else_label));
		
		return instructions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(While whileStatement) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//make labels
		Label head_loop_label = labelGenerator.createLabel();
		Label tail_loop_label = labelGenerator.createLabel();
		//set labels loop global values for break and continue;
		this.head_loop_label = head_loop_label;
		this.tail_loop_label = tail_loop_label;
		
		//here shall begin each loop head
		instructions.add(new LabelNode(head_loop_label));
		
		//evaluate condition expression into currentRegister
		instructions.addAll((List<LirNode>)whileStatement.getCondition().accept(this));
		
		//compare and jump if condition false
		instructions.add(new CompareNode(new Immediate(0),new Reg(currentRegister)));
		instructions.add(new JumpFalse(tail_loop_label));
		
		//set loop code and jump to head
		instructions.addAll((List<LirNode>)whileStatement.getOperation().accept(this));
		instructions.add(new JumpNode(head_loop_label));
		//set labels loop global values for break and continue - in case inner loop made us "forget"
		this.head_loop_label = head_loop_label;
		this.tail_loop_label = tail_loop_label;
		
		//set tail
		instructions.add(new LabelNode(tail_loop_label));
		
		return instructions;
	}

	@Override
	public Object visit(Break breakStatement)  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//add jump to the end of the loop we are currently in
		instructions.add(new JumpNode(tail_loop_label));
		return instructions;
	}

	@Override
	public Object visit(Continue continueStatement)  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//add jump to the end of the loop we are currently in
		instructions.add(new JumpNode(head_loop_label));
		return instructions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(StatementsBlock statementsBlock) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		// run all over the statements and add them
		for(Statement stmt:statementsBlock.getStatements()){
			List<LirNode> stmt_exe = (List<LirNode>)stmt.accept(this);
			if(stmt_exe != null){
				instructions.addAll(stmt_exe);
			}
		}
		return instructions;
	}

	@Override
	public Object visit(LocalVariable localVariable) throws SemanticError  {
		//if has initialization , just like assignment
		if(localVariable.getInitValue() != null){
			//get localVariable symbol
			Symbol localVariable_symbol = ((VariableSymbolTable)localVariable.enclosingScope()).getVariableLocally(localVariable.getName());
			//make Memory object for var
			Memory var= new Memory(varNameGen.getVariableName(localVariable_symbol),MemoryKind.LOCAL);
			return local_var_assgiment(var,localVariable.getInitValue());
		}
		//else no Lir generated
		return null;
	}

	@Override
	public Object visit(VariableLocation location)  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		if(location.isExternal()){
			//TODO
		}
		else{
			Symbol var_symbol = location.getDefiningSymbol();
			Memory var= new Memory(varNameGen.getVariableName(var_symbol),MemoryKind.LOCAL);
			instructions.add(new MoveNode(var,new Reg(currentRegister)));
		}
		return instructions;
	}

	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray)  {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(Length length) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//calc Array into currentRegister
		instructions.addAll((List<LirNode>)length.getArray().accept(this));
		//use special command
		instructions.add(new ArrayLengthNode(new Reg(currentRegister),new Reg(currentRegister)));
		return instructions;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(MathUnaryOp unaryOp) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//evaluate inner expression to currentRegister
		instructions.addAll((List<LirNode>)unaryOp.getOperand().accept(this));
		//only one unary math op - minus. calc it
		instructions.add(new UnaryInstructionNode(new Reg(currentRegister),lirUnaryOp.NEG));
		return instructions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(LogicalUnaryOp unaryOp) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//evaluate inner expression to currentRegister
		instructions.addAll((List<LirNode>)unaryOp.getOperand().accept(this));
		//only one unary math op - not. calc it
		instructions.add(new UnaryInstructionNode(new Reg(currentRegister),lirUnaryOp.NOT));
		return instructions;
	}

	@Override
	public Object visit(Literal literal)  {
		List<LirNode> generated_immidiate_in_list = new ArrayList<LirNode>();
		// use this
		
		if(literal.getType() == LiteralTypes.STRING){
		
			String value = literal.getValue().toString();
		
			// add new string definition to lir program

			stringDefinitions.add(new StringLiteralNode(value, labelGenerator));
		}
		else {//all other literals translates to immediate
			Immediate im = null;
			if(literal.getType() == LiteralTypes.INTEGER){
				im = new Immediate((int) literal.getValue());
			}
			else if(literal.getType() == LiteralTypes.FALSE){
				//make new Immediate 0
				im = new Immediate(0);
			} 
			else if(literal.getType() == LiteralTypes.TRUE){
				//make new Immediate 1
				im = new Immediate(1);
			} 
			else{//literal.getType() == LiteralTypes.NULL
				//zero will represent NULL
				im = new Immediate(0);
			}
			generated_immidiate_in_list.add(new MoveNode(im,new Reg(currentRegister)));
			return generated_immidiate_in_list;
		}
		
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(ExpressionBlock expressionBlock) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//call the inner expression to currentRegister
		instructions.addAll((List<LirNode>)expressionBlock.getExpression().accept(this));
		return instructions;//we return with expressionBlock evaluation in currentRegister 
	}

}
