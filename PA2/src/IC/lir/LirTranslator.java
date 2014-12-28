package IC.lir;

import java.util.ArrayList;
import java.util.List;

import IC.LiteralTypes;
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
import IC.lir.lirAST.LibraryCallNode;
import IC.lir.lirAST.LirNode;
import IC.lir.lirAST.LirProgram;
import IC.lir.lirAST.LoadField;
import IC.lir.lirAST.Memory;
import IC.lir.lirAST.Memory.MemoryKind;
import IC.lir.lirAST.MethodNode;
import IC.lir.lirAST.MoveFieldNode;
import IC.lir.lirAST.MoveNode;
import IC.lir.lirAST.Reg;
import IC.lir.lirAST.ReturnNode;
import IC.lir.lirAST.StaticCallNode;
import IC.lir.lirAST.StoreArrayNode;
import IC.lir.lirAST.StoreField;
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
	 * this to labels are used so break and continue jumps will know which labels should use
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
	
	
	
	private List<LirNode> currentMethodInstructions;
	
	private MethodNode common_method_visit( Method method) throws SemanticError
	{
		
		// get method label
		Label methodLabel;
		
		if(method.isStatic())
			methodLabel  = this.labelGenerator.getStaticMethodLabel(method.getName(), this.currentClassName);
		else
			methodLabel = this.labelGenerator.getVirtualMethodLabel(method.getName(), this.currentClassName);
		
		
		// need to save the formal names
		
		for(Formal formal : method.getFormals())
		{
			Symbol formalSym = ((VariableSymbolTable)formal.enclosingScope()).getVariableLocally(formal.getName());
			
			this.varNameGen.getVariableName(formalSym); /* ignore the result */
		}
		
		
		// create lir instructions from statements
		currentMethodInstructions = new ArrayList<LirNode>();
		
		
		for(Statement stmt : method.getStatements())
		{
			// generate instructions from statement
			// nullify the register counter
			
			this.currentRegister = 0;
			
			
			// visit statements
			stmt.accept(this);
			
			
		}
		
		
		return new MethodNode(methodLabel, currentMethodInstructions);
		
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
		// in fact all library class wont be visited
		return null;
	}
	
	

	@Override
	public Object visit(Formal formal)  {
		
		// will not be visited
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
		
		
		/*
		 * 	this method handles all store variants
		 *  store to array
		 *  store to local variable
		 *  store to field
		 *  
		 *  note that in all cases, we can store either a register or a immediate
		 *  if left hand side is a immediate we can optimize it immediately
		 */
		
		
		LirNode rightHand = (LirNode)assignment.getAssignment().accept(this);
		
		LirNode assignmentResult;
		
		if(rightHand instanceof Memory)
		{
			// need to move memory to register
			Reg temp = new Reg(currentRegister);
			// load memory to register
			this.currentMethodInstructions.add(new MoveNode(rightHand, temp));
			
			assignmentResult = temp;
			
		}
		else
			
		{
			assignmentResult = rightHand;
		}
		
		

		
		//case 1: assignment to local variable
		if(assignment.getVariable() instanceof VariableLocation 
				&& ((VariableLocation)assignment.getVariable()).isExternal()==false){
			
			Symbol var_symbol = ((VariableLocation)assignment.getVariable()).getDefiningSymbol();
			Memory var = new Memory(varNameGen.getVariableName(var_symbol), MemoryKind.LOCAL);
			
			// add move instruction
			this.currentMethodInstructions.add(new MoveNode(assignmentResult, var));
			
		}
		
		
		// case 2: assignment to field (external variable location )
		
		else if(assignment.getVariable() instanceof VariableLocation)
		{
			
			if(rightHand instanceof Memory)
			{
				// we stored our result in a register, we need to hold it
				++currentRegister;
			
			}
			save_to_field((VariableLocation) assignment.getVariable(), assignmentResult);
			
			if(rightHand instanceof Memory){
				
				// no longer need the register that held the result we want to store
				--currentRegister;
				
			}
			
		}
		
		// case 3 : assignment to array
		
		else
		{
			if(rightHand instanceof Memory)
			{
				// we stored our result in a register, we need to hold it
				++currentRegister;
			
			}
			save_to_array((ArrayLocation) assignment.getVariable(), assignmentResult);
			if(rightHand instanceof Memory){
				
				// no longer need the register that held the result we want to store
				--currentRegister;
				
			}
			
			
		}
		
		return null;
	
	}




	@SuppressWarnings("unchecked")
	@Override
	public Object visit(CallStatement callStatement) throws SemanticError  {
		//TODO
		return null;
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

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(LocalVariable localVariable) throws SemanticError  {
		
		// the name is important
		// we need to remember this name is reserved for this local variable
		
		Symbol localVariable_symbol = ((VariableSymbolTable)localVariable.enclosingScope()).getVariableLocally(localVariable.getName());
		
		Memory var = new Memory(varNameGen.getVariableName(localVariable_symbol),MemoryKind.LOCAL);
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		//if has initialization , just like assignment
		if(localVariable.getInitValue() != null){
			
			
			LirNode rightHand;
			
			if(localVariable.getInitValue() instanceof Literal)
			{
				rightHand = getImmediateFromLiteral((Literal)localVariable.getInitValue());
				
			}
			else
			{
				
				// generate code for assignment and store result in register
				instructions.addAll((List<LirNode>)localVariable.getInitValue().accept(this));
				
				// currentRegister is the result register at this point
				rightHand = new Reg(currentRegister);
				
				
			}
			
			// add move instruction
			instructions.add(new MoveNode(rightHand, var));
		}
		
		return instructions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(VariableLocation location) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		if(location.isExternal()){
			//in here we will only LOAD the object (the external). in order to save it we will use save_to_field
			
			//load external of external to currentRegister
			instructions.addAll((List<LirNode>)location.getLocation().accept(this));
			
			//calc offset of field
			int field_offset = classManager.getFieldOffset(currentClassName, location.getName());
			
			//load external to currentRegister. we no longer need external of external
			instructions.add(new LoadField(new Reg(currentRegister),new Immediate(field_offset),new Reg(currentRegister)));
			
		}
		else{
			Symbol var_symbol = location.getDefiningSymbol();
			Memory var= new Memory(varNameGen.getVariableName(var_symbol),MemoryKind.LOCAL);
			instructions.add(new MoveNode(var,new Reg(currentRegister)));
		}
		return instructions;
	}
	
	//this method should be used each time we want to calculate a field and save something in it
	
	private void save_to_field(VariableLocation location, LirNode source) throws SemanticError{
		
		//calc external of location to currentRegister
		LirNode locationNode = (LirNode)location.getLocation().accept(this);
		
		
		// todo, check if location is memory
		
		
		
		//calc offset [ need to fix this ]
		int field_offset = classManager.getFieldOffset(currentClassName, location.getName());
		
		/* this register holds the object , set by accept*/
		Reg objectRegister = new Reg(currentRegister);
		
		//make store instruction
		this.currentMethodInstructions.add(new StoreField(objectRegister, new Immediate(field_offset), source));
		
	}

	
	@SuppressWarnings("unchecked")
	private List<LirNode> save_to_array(ArrayLocation location, LirNode source) throws SemanticError{
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		// generate code for array location, result must be stored in register (currentRegister)
		instructions.addAll((List<LirNode>)location.getArray().accept(this));
		
		Reg arrayReg = new Reg(currentRegister);
		
		// generate code for index
		
		LirNode indexNode;
		
		// optimization in case of single literal
		
		if(location.getIndex() instanceof Literal)
		{
			indexNode = getImmediateFromLiteral((Literal)location.getIndex());
			
			instructions.add(new StoreArrayNode(arrayReg, indexNode, source));
			
			return instructions;
			
		
		}
		
		// otherwise, result must be stored in another register
		// backup arrayReg
		++currentRegister;
		
		instructions.addAll((List<LirNode>)location.getIndex().accept(this));
		
		/* this register holds the index result , set by accept*/
		Reg indexRegister = new Reg(currentRegister);
		
		//make store instruction
		instructions.add(new StoreArrayNode(arrayReg, indexRegister, source));
		
		// no longer need array reg
		--currentRegister;
		
		return instructions;
	}
	
	
	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(StaticCall call) throws SemanticError  {
		List<LirNode> instructions = new ArrayList<LirNode>();
		//this function push all arguments into registers
		push_args_to_regs(call, instructions);
		
		//if this is a call to Library function
		if(call.getClassName().equals("Library")){
			//get call label
			Label lib_call_label = labelGenerator.getLibraryMethodLabel(call.getName());
			//make Library call and 
			//TODO- CHECK THIS IS POSSIBLE-save in currentRegister- |parameters number| since we won't need the parameters after we will get the result
			instructions.addAll((List<LirNode>)new LibraryCallNode(lib_call_label,param_regs_maker(call),new Reg(currentRegister)));
		}
		else{
			//get call label
			Label static_call_label = labelGenerator.getStaticMethodLabel(call.getName(), call.getClassName());
			
			//make Library call and 
			//TODO- CHECK THIS IS POSSIBLE-save in currentRegister- |parameters number| since we won't need the parameters after we will get the result
			//instructions.addAll((List<LirNode>)new StaticCallNode(static_call_label,param_regs_maker(call),new Reg(currentRegister)));
		}
		
		free_parametrs_regs(call);
		
		return instructions;
	}

	//this function push all arguments into registers
	private void push_args_to_regs(StaticCall call, List<LirNode> instructions)
			throws SemanticError {
		
		for (Expression arg:call.getArguments()){
			//calc arg i and save arg i in currentRegister+i
			instructions.addAll((List<LirNode>)arg.accept(this));
			currentRegister++;
		}
	}


	private void free_parametrs_regs(StaticCall call) {
		//after call, arguments are no longer needed. free all the registers which we used to save them
		for (int i=0; i < call.getArguments().size()-1;i++){//-1 since the answer will be in (former currentRegister)- |parameters number|
			currentRegister--;
		}
	}

	//this function generates the register list where all the parameters lay
	@SuppressWarnings("unchecked")
	private List<LirNode> param_regs_maker(Call call) throws SemanticError {
		List<LirNode> regs = new ArrayList<LirNode>();
		
		for (int i=0; i < call.getArguments().size();i++){
			regs.add(new Reg(currentRegister-(call.getArguments().size()-i)));
		}
		return regs;
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
				im = new Immediate((Integer) literal.getValue());
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

	
	/**
	 * this method simply creates a Label node or Immediate node from the given literal
	 * @param literal
	 * @return
	 */
	
	
	public LirNode getImmediateFromLiteral(Literal literal)
	{
		
		if(literal.getType() == LiteralTypes.STRING){
			
			String value = literal.getValue().toString();
		
			// add new string definition to lir program

			stringDefinitions.add(new StringLiteralNode(value, labelGenerator));
			
			
			return labelGenerator.getStringLabel(value);
		}
		
		else {
			
			//all other literals translates to immediate
			
			Immediate im = null;
			
			if(literal.getType() == LiteralTypes.INTEGER){
				
				im = new Immediate((Integer) literal.getValue());
			}
			else if(literal.getType() == LiteralTypes.FALSE){
				//make new Immediate 0
				im = new Immediate(0);
			} 
			else if(literal.getType() == LiteralTypes.TRUE){
				//make new Immediate 1
				im = new Immediate(1);
			} 
			else{
				//literal.getType() == LiteralTypes.NULL
				//zero will represent NULL
				im = new Immediate(0);
			}
			
			return im;
		}
		
		
		
		
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
