package IC.lir;

import java.util.ArrayList;
import java.util.List;

import IC.BinaryOps;
import IC.LiteralTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.BinaryOp;
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
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.VariableSymbolTable;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.Symbol;
import IC.Types.ClassType;
import IC.Types.IntType;
import IC.Types.Type;
import IC.lir.lirAST.ArrayLengthNode;
import IC.lir.lirAST.BinaryInstructionNode;
import IC.lir.lirAST.CompareNode;
import IC.lir.lirAST.DispatchTableNode;
import IC.lir.lirAST.Immediate;
import IC.lir.lirAST.JumpFalse;
import IC.lir.lirAST.JumpG;
import IC.lir.lirAST.JumpGE;
import IC.lir.lirAST.JumpL;
import IC.lir.lirAST.JumpLE;
import IC.lir.lirAST.JumpNode;
import IC.lir.lirAST.JumpTrueNode;
import IC.lir.lirAST.Label;
import IC.lir.lirAST.LabelNode;
import IC.lir.lirAST.LibraryCallNode;
import IC.lir.lirAST.LirNode;
import IC.lir.lirAST.LirProgram;
import IC.lir.lirAST.LoadArrayNode;
import IC.lir.lirAST.LoadField;
import IC.lir.lirAST.Memory;
import IC.lir.lirAST.Memory.MemoryKind;
import IC.lir.lirAST.MethodNode;
import IC.lir.lirAST.MoveFieldNode;
import IC.lir.lirAST.MoveNode;
import IC.lir.lirAST.Reg;
import IC.lir.lirAST.RegWithIndex;
import IC.lir.lirAST.RegWithOffset;
import IC.lir.lirAST.ReturnNode;
import IC.lir.lirAST.StaticCallNode;
import IC.lir.lirAST.StoreArrayNode;
import IC.lir.lirAST.StoreField;
import IC.lir.lirAST.StringLiteralNode;
import IC.lir.lirAST.ThisNode;
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
		
		

		
		//case 1: assignment to variable location (local var or field (this or external)
		if(assignment.getVariable() instanceof VariableLocation ){
			
			
			if(rightHand instanceof Memory)
			{
				// we stored our result in a register, we need to hold it
				++currentRegister;
			
			}
			
			
			// visit location, can be: local variable, a this field or an external field
			LirNode locationNode = VariableLocationCommonVisit((VariableLocation) assignment.getVariable());
			
			if(locationNode instanceof Memory)
			{
				
				// simple variable location
				// add move instruction
				this.currentMethodInstructions.add(new MoveNode(assignmentResult, locationNode));
			
			}
			else
			{
				// some field access
				RegWithOffset regWithOffset = (RegWithOffset)locationNode;
				
				//make store instruction
				this.currentMethodInstructions.add(new StoreField(regWithOffset, assignmentResult));
				
			}
			
			if(rightHand instanceof Memory)
			{
				// we stored our result in a register, we need to hold it
				--currentRegister;
			
			}
			
			
			
		}
		
		
		// case 2 : assignment to array
		
		else
		{
			if(rightHand instanceof Memory)
			{
				// we stored our result in a register, we need to hold it
				++currentRegister;
			
			}
			RegWithIndex arrayLoc = ArrayLocationCommonVisit((ArrayLocation) assignment.getVariable());
			
			// store the assignment in the array
			
			this.currentMethodInstructions.add(new StoreArrayNode(arrayLoc, assignmentResult));
			
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

	@Override
	public Object visit(Return returnStatement) throws SemanticError  {
		
		LirNode assignmentResult = null;
		
		if (returnStatement.getValue() != null){
			//evaluate return expression
			LirNode return_exp = (LirNode) returnStatement.getValue().accept(this);
			
			//set the return_exp(register or var or immediate) to be the result 
			assignmentResult = return_exp;
		}
		
		//Return currentRegister - in case of return void return junk
		this.currentMethodInstructions.add(new ReturnNode(assignmentResult));
		
		return null;
	}

	
	@Override
	public Object visit(If ifStatement) throws SemanticError  {

		//make labels
		Label else_label = null;
		
		if(ifStatement.hasElse()){
			else_label = labelGenerator.createLabel();
		}
		
		Label end_if_else_label = labelGenerator.createLabel();
		
		//evaluate condition expression into currentRegister
		ifStatement.getCondition().accept(this);
		
		//compare and jump to else if exist and out of if if doesn't
		this.currentMethodInstructions.add(new CompareNode(new Immediate(0),new Reg(currentRegister)));
		if(ifStatement.hasElse()){
			this.currentMethodInstructions.add(new JumpFalse(else_label));
		}
		else{
			this.currentMethodInstructions.add(new JumpFalse(end_if_else_label));
		}
		
		//if we enter if
		ifStatement.getOperation().accept(this);
		
		
		if(ifStatement.hasElse()){
			//jump to avoid else - if else exist
			this.currentMethodInstructions.add(new JumpNode(end_if_else_label));
			
			//add else part - else label and code
			this.currentMethodInstructions.add(new LabelNode(else_label));
			ifStatement.getElseOperation().accept(this);
		}
		
		//set end of if label
		this.currentMethodInstructions.add(new LabelNode(end_if_else_label));
		
		return null;
	}
	
	
	public Object visit(While whileStatement) throws SemanticError  {
		
		//make labels
		Label head_loop_label = labelGenerator.createLabel();
		Label tail_loop_label = labelGenerator.createLabel();
		
		//set labels loop global values for break and continue;
		this.head_loop_label = head_loop_label;
		this.tail_loop_label = tail_loop_label;
		
		//here shall begin each loop head
		this.currentMethodInstructions.add(new LabelNode(head_loop_label));
		
		//evaluate condition expression into currentRegister
		whileStatement.getCondition().accept(this);
		
		//compare and jump if condition false
		this.currentMethodInstructions.add(new CompareNode(new Immediate(0),new Reg(currentRegister)));
		this.currentMethodInstructions.add(new JumpFalse(tail_loop_label));
		
		//set loop code and jump to head
		whileStatement.getOperation().accept(this);
		this.currentMethodInstructions.add(new JumpNode(head_loop_label));
		
		//set labels loop global values for break and continue - in case inner loop made us "forget"
		this.head_loop_label = head_loop_label;
		this.tail_loop_label = tail_loop_label;
		
		//set tail
		this.currentMethodInstructions.add(new LabelNode(tail_loop_label));
		
		return null;
	}
	
	@Override
	public Object visit(Break breakStatement)  {
		//add jump to the end of the loop we are currently in
		this.currentMethodInstructions.add(new JumpNode(tail_loop_label));
		//statements return null
		return null;
	}

	@Override
	public Object visit(Continue continueStatement)  {
		//add jump to the end of the loop we are currently in
		this.currentMethodInstructions.add(new JumpNode(head_loop_label));
		return null;
	}

	public Object visit(StatementsBlock statementsBlock) throws SemanticError  {
		
		// run all over the statements and process them
		for(Statement stmt : statementsBlock.getStatements()){
			
			// may nullify register counter
			this.currentRegister = 0;
			stmt.accept(this);
			
			
		}
		return null;
	}
	
	@Override
	public Object visit(LocalVariable localVariable) throws SemanticError  {
		
		// the name is important
		// we need to remember this name is reserved for this local variable
		
		Symbol localVariable_symbol = ((VariableSymbolTable)localVariable.enclosingScope()).getVariableLocally(localVariable.getName());
		
		Memory var = new Memory(varNameGen.getVariableName(localVariable_symbol), MemoryKind.LOCAL);
		
		// if has initialization , just like assignment
		if(localVariable.hasInitValue()){
			
			LirNode rightHand = (LirNode) localVariable.getInitValue().accept(this);
			
			LirNode assignment;
			
			if(rightHand instanceof Memory)
			{
				// gotta load to a register
				assignment = new Reg(currentRegister);
				this.currentMethodInstructions.add(new MoveNode(rightHand, assignment));
				
			}
			else
			{
				assignment = rightHand; // register, string, immediate
			}
			
			
			// add move instruction
			this.currentMethodInstructions.add(new MoveNode(assignment, var));
		}
		
		return null;
		
	}
	
	
	
	
	private LirNode VariableLocationCommonVisit(VariableLocation location) throws SemanticError
	{
		
		
		if(location.isExternal()){
		
			// get class object, either a register, or memory
			LirNode locationNode = (LirNode)location.getLocation().accept(this);
				
			Reg classObject = null;
				
			
			if(locationNode instanceof Memory)
			{
				// in the first case, location is memory
				// meaning that location was a local variable or a parameter
				
				
				classObject = new Reg(currentRegister);
				// load the memory to register
				this.currentMethodInstructions.add(new MoveNode(locationNode, classObject));
			}
		
			else
			{
				// it is already in a register
				classObject = (Reg)locationNode;
				
			}
				
			// get class type
			
			String definingClassName = ((ClassType)location.getLocation().getNodeType()).getName();

				
			int field_offset = classManager.getFieldOffset(definingClassName, location.getName());
			
			
			/* returns a new lir object that holds a. register that holds the object b. an offset of the field */
			return new RegWithOffset(classObject, new Immediate(field_offset));
				
		}
		
		
		else{
			
			Symbol var_symbol = location.getDefiningSymbol();
			
			if(var_symbol instanceof FieldSymbol)
			{
				// need to generate code that loads this to a register
				Reg classObject = new Reg(currentRegister);
				ThisNode thisNode = new ThisNode();
				
				this.currentMethodInstructions.add(new MoveNode(thisNode, classObject));
				
				// get class type
				String definingClassName = ((VariableSymbolTable)location.enclosingScope()).getThisType().getName();
					
				//calc offset 
				int field_offset = classManager.getFieldOffset(definingClassName, location.getName());
				
				return new RegWithOffset(classObject, new Immediate(field_offset));
				
			}
			else
			{

				Memory var = new Memory(varNameGen.getVariableName(var_symbol),MemoryKind.LOCAL);
				return var;
			}
			
			
		}
		
		
	}
	

	
	@Override
	public Object visit(VariableLocation location) throws SemanticError  {
		
		
		LirNode resultNode = VariableLocationCommonVisit(location);
		
		if(resultNode instanceof RegWithOffset)
		{
			// field
			// need to load the pair register.offset to a register, may use same register
			
			Reg reg = new Reg(currentRegister);
			this.currentMethodInstructions.add(new LoadField((RegWithOffset)resultNode, reg));
			
			return reg;
			
		}
		else
		{
			// local variable/ parameter
			// result is memory, return as is, storing to register, if needed, should be handled by calling method
			return resultNode;
			
		}
			
			
		
	}
	
	
	/**
	 * helper method for arrayLocation visit
	 * returns a pair Reg[Immediate] or Reg[Reg]
	 * representing the array location
	 * 
	 * 
	 * @param location
	 * @return
	 * @throws SemanticError
	 */

	private RegWithIndex ArrayLocationCommonVisit(ArrayLocation location) throws SemanticError
	{
		
		// generate code for array location, result must be stored in register (currentRegister)
		LirNode arrayNode = (LirNode)location.getArray().accept(this);
		
		Reg arrayObject;
		
		if(arrayNode instanceof Memory)
		{
			arrayObject = new Reg(currentRegister);
			// load the memory to register
			this.currentMethodInstructions.add(new MoveNode(arrayNode, arrayObject));
		}
		else
		{
			
			arrayObject = (Reg)arrayNode;
		}
		
		
		// generate code for index
		
		LirNode indexNode;
		
		// optimization in case of single literal
		
		if(location.getIndex() instanceof Literal)
		{
			indexNode = (LirNode) location.getIndex().accept(this);
			
			return new RegWithIndex(arrayObject, indexNode);
			

			
		
		}
		
		// otherwise, result must be stored in another register
		// backup arrayReg
		++currentRegister;
		
		indexNode = (LirNode) location.getIndex().accept(this);
		
		/* this register holds the index result*/
		Reg indexRegister;
		
		if(indexNode instanceof Memory)
		{
			// we need to save the variable into a register first
			indexRegister = new Reg(currentRegister);
			this.currentMethodInstructions.add(new MoveNode(indexNode, indexRegister));
			
		}
		else
		{
			/* already a register */
			indexRegister = (Reg)indexNode;
		}
		
		// no longer need array register
		--currentRegister;
		
		return new RegWithIndex(arrayObject, indexRegister);
		
		
	}
	
	
	@Override
	public Object visit(ArrayLocation location) throws SemanticError {
		
		
		// get array[index] pair
		RegWithIndex result = ArrayLocationCommonVisit(location);
		
		// now need to store that pair in a register, may use current register
		
		Reg destReg = new Reg(currentRegister);
		
		this.currentMethodInstructions.add(new LoadArrayNode(result, destReg));
				
		
		return destReg;
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

	public Object visit(Length length) throws SemanticError  {
		
	
		
		// get array object
		LirNode array = (LirNode) length.getArray().accept(this);
		
		// array may be a register or a local var (memory allowed)
		// currentRegister will hold the result
			
		this.currentMethodInstructions.add(new ArrayLengthNode(array , new Reg(currentRegister)));
		return null;
	}
	
	@Override
	public Object visit(MathBinaryOp binaryOp) throws SemanticError  {
		
		LirNode math_exp = null;
		
		//check types of vars for op
		Type side_1 = binaryOp.getFirstOperand().getNodeType();
		Type side_2 = binaryOp.getSecondOperand().getNodeType();
		
		if ((side_1 instanceof IntType) && (side_2 instanceof IntType)){
			
			//get lirBinaryOp
			lirBinaryOp op = get_function_by_BinaryOps_math(binaryOp.getOperator());
			
			math_exp = simple_binary_op(binaryOp, op);
		}
		else {
			
			//if they aren't both int's, they are strings
			//math_exp = concatenate_strings(binaryOp);
		}
		return math_exp;
	}
	
	
	private LirNode simple_binary_op(BinaryOp binaryOp, lirBinaryOp op) throws SemanticError {
		
		// OP a,b means b: = b OP a
		// b must be a register
		// a however, may be anything [immediate, memory, reg] !
		
		
		//calc FirstOperand (b), b will serve as a accumulator register
		LirNode right_exp = (LirNode) binaryOp.getFirstOperand().accept(this);
		
		Reg b;
		
		if(right_exp instanceof Reg)
		{
			
			b = (Reg)right_exp;
			
		}
		else
		{
			
			// must load into a register
			b = new Reg(currentRegister);
			this.currentMethodInstructions.add(new MoveNode(right_exp, b));
			
			// backup register
			++this.currentRegister;
		}
		
		
		
		// get secondOperand (a), may be anything!
		LirNode a = (LirNode) binaryOp.getSecondOperand().accept(this);
	
		// no longer need b register
		
		if(!(right_exp instanceof Reg))
		{
			
			 // we used a register to store the first operand
			 // 
			--this.currentRegister;
		}
		
		//save op to currentRegister
		this.currentMethodInstructions.add(new BinaryInstructionNode(op, a, b));
		
		// the result is stored in b
		return b;
	}
	
	

	private lirBinaryOp get_function_by_BinaryOps_math (BinaryOps op){
		lirBinaryOp ret_op = null;
		switch(op){
		case PLUS:
			ret_op= lirBinaryOp.ADD;
			break;
		case MINUS:
			ret_op= lirBinaryOp.SUB;
			break;
		case MULTIPLY:
			ret_op= lirBinaryOp.MUL;
			break;
		case DIVIDE:
			ret_op= lirBinaryOp.DIV;
			break;
		case MOD:
			ret_op= lirBinaryOp.MOD;
			break;
		default:
			break;
		}
		return ret_op;
	}
	

	private JumpNode make_logical_jump(LogicalBinaryOp binaryOp, Label true_label) {
		JumpNode jump = null;
		switch(binaryOp.getOperator()){
		case EQUAL: 
			jump = new JumpTrueNode(true_label);
			break;
		case NEQUAL:
			jump = new JumpFalse(true_label);
			break;
		case GT: 
			jump = new JumpG(true_label);
			break;
		case GTE:
			jump = new JumpGE(true_label);
			break;
		case LT: 
			jump = new JumpL(true_label);
			break;
		case LTE:
			jump = new JumpLE(true_label);
			break;
		default:
			break;
		}
		return jump;
	}
	
	

	private lirBinaryOp get_function_by_BinaryOps_logical (BinaryOps op){
		
		lirBinaryOp ret_op= null;
		switch(op){
			case LAND:
				ret_op= lirBinaryOp.AND;
				break;
			case LOR:
				ret_op= lirBinaryOp.OR;
				break;
			default:
				break;
		}
		return ret_op;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Object visit(LogicalBinaryOp binaryOp) throws SemanticError {
		
		
		lirBinaryOp op = get_function_by_BinaryOps_logical(binaryOp.getOperator());
		
		if (op == null){ //LT or LTE or GT or GTE or EQUAL or NEQUAL
			Label exit = labelGenerator.createLabel();
			Label true_label = labelGenerator.createLabel();
			//no need to make false label- we continue from jump to false case
			
			//we will use sub to simulate these ops
			op = lirBinaryOp.SUB;
			//calculate sub between both
			//simple_binary_op(binaryOp,op,instructions);
			
			JumpNode jump = make_logical_jump(binaryOp, true_label);//make proper jump
			
			//take test
			this.currentMethodInstructions.add(new CompareNode(new Immediate(0),new Reg(currentRegister)));
			this.currentMethodInstructions.add(jump);
			
			//make false case
			this.currentMethodInstructions.add(new MoveNode(new Immediate(0),new Reg(currentRegister)));
			this.currentMethodInstructions.add(new JumpNode(exit));//unconditional jump to exit
			
			//make true case
			this.currentMethodInstructions.add(new LabelNode(true_label));
			this.currentMethodInstructions.add(new MoveNode(new Immediate(1),new Reg(currentRegister)));
			
			//exit here
			this.currentMethodInstructions.add(new LabelNode(exit));
			
		}
		else{//LAND or LOR
			//this label will be used if by calculating only the first op we will know the answer
			Label op_label = labelGenerator.createLabel();
			
			//calc FirstOperand into currentRegister
			this.currentMethodInstructions.addAll((List<LirNode>)binaryOp.getFirstOperand().accept(this));
			currentRegister++;
			
			//test for "critical result"
			this.currentMethodInstructions.add(new CompareNode(new Immediate(0),new Reg(currentRegister-1)));
			if (op == lirBinaryOp.AND){
				this.currentMethodInstructions.add(new JumpTrueNode(op_label));
			}
			else {//op == lirBinaryOp.OR
				this.currentMethodInstructions.add(new JumpFalse(op_label));
			}
			
			//check second operand (if no "critical result") to currentRegister+1
			this.currentMethodInstructions.addAll((List<LirNode>)binaryOp.getSecondOperand().accept(this));
			
			//op labels points of the op calculation. we "jumped" over calculating the second operation
			this.currentMethodInstructions.add(new LabelNode(op_label));
			//save op to currentRegister
			this.currentMethodInstructions.add(new BinaryInstructionNode(op,new Reg(currentRegister),new Reg(currentRegister-1)));
			
			//free second register
			currentRegister--;
		}
		return null; // TODO check this
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
