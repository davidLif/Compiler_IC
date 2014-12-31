package IC.lir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import IC.AST.UnaryOp;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.While;
import IC.SemanticChecks.SemanticError;
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.GlobalSymbolTable;
import IC.SymTables.VariableSymbolTable;
import IC.SymTables.Symbols.ClassSymbol;
import IC.SymTables.Symbols.FieldSymbol;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;
import IC.Types.ClassType;
import IC.Types.IntType;
import IC.Types.MethodType;
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
import IC.lir.lirAST.SpaceNode;
import IC.lir.lirAST.StaticCallNode;
import IC.lir.lirAST.StoreArrayNode;
import IC.lir.lirAST.StoreField;
import IC.lir.lirAST.StringConcatinetionCall;
import IC.lir.lirAST.StringLiteralNode;
import IC.lir.lirAST.ThisNode;
import IC.lir.lirAST.UnaryInstructionNode;
import IC.lir.lirAST.VirtualCallNode;
import IC.lir.lirAST.lirBinaryOp;
import IC.lir.lirAST.lirUnaryOp;
import IC.Types.VoidType;

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
	 * this two labels are used so break and continue jumps will know which labels should use
	 */
	private Label head_loop_label = null;
	private Label tail_loop_label = null;
	
	
	
	
	/**  
	 * class name -> method name -> Method object
	 */
	
	private Map<String, Map<String, Method>> methodMap ; 
	
	/**
	 * class name -> static method name -> Method object
	 */
	private Map<String, Map<String, Method>> staticMethodMap ; 
	
	
	/**
	 * global symbol table, in our case, used to resolve method definitions [call statements]
	 */
	
	private GlobalSymbolTable globSymTable;
	
	
	/**
	 * run time check manager, use this object to generate implementation and generate calls
	 */
	private RuntimeChecks runTimeChecks;
	
	
	
	
	public LirTranslator(Program program, GlobalSymbolTable globSymTable)
	{
		
		/* init data structures and fields */
		this.globSymTable = globSymTable;
		this.icProgram = program;
		
		labelGenerator = new LabelGenerator();
		
		/* this creates the class layouts and dispatch tables */
		
		classManager   = new ClassLayoutManager(icProgram);
		
		/* saves all the string definitions we've seen, note that the type of the list is a LIR node */
		
		stringDefinitions = new ArrayList<StringLiteralNode>();
		
		
		/* init method map, used for method calls */
		
		this.initMethodMap(program);
		
		
		
	}
	
	
	/**
	 * Fill the maps: class name -> method name -> method object
	 * for both static and virtual methods
	 * 
	 */
	
	private void initMethodMap(Program program )
	{
		
		/* maps and sets for all types of methods */
		this.methodMap = new HashMap<String, Map<String,Method>>();
		this.staticMethodMap = new HashMap<String, Map<String, Method>>();
		
		for(ICClass icClass : program.getClasses())
		{
			
			if(icClass.getName().equals("Library"))
			{
				continue;
				
			}
			
			Map<String, Method> namesToMethods = new HashMap<String, Method>();
			Map<String, Method> staticNamesToMethods = new HashMap<String, Method>();
 			
			for(Method method: icClass.getMethods())
			{
				if(method.isStatic())
				{
					staticNamesToMethods.put(method.getName(), method);
				}
				else
				{
					namesToMethods.put(method.getName(), method);
				}
			}
				
			
			this.methodMap.put(icClass.getName(), namesToMethods);
			this.staticMethodMap.put(icClass.getName(), staticNamesToMethods);
		}
		
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
		
		/* get the exit label, main method label */
		
		Label programExitLabel = labelGenerator.getExitLabel();
		Label mainMethodLabel = labelGenerator.mainMethodLabel();
		
		this.runTimeChecks =  new RuntimeChecks(this.labelGenerator, programExitLabel,stringDefinitions);
		
		for(ICClass currClass : program.getClasses())
		{
			// go over each class and get all its lir methods
			
			if(currClass.getName().equals("Library"))
				continue;
			
			programMethods.addAll(this.visit(currClass));
		}
		
		List<DispatchTableNode> dispatchTables = classManager.getAllDispatchTables(labelGenerator);
		
		
		
		/* insert the run time check implementation */
		
		programMethods.addAll(this.runTimeChecks.getImplementation());
		
		// use all the data we gathered: dispatch tables, string literals and methods
		
		return new LirProgram(stringDefinitions, dispatchTables, programMethods, programExitLabel, mainMethodLabel);
		
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
	
	
	
	/**
	 * list saves the current instructions generated for current method we're visiting
	 */
	
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
		
		MethodType methodType = (MethodType) method.getNodeType();
		if(methodType.getReturnType() instanceof VoidType && !(methodLabel.emit().equals("_ic_main")))
		{
			// need to add dummy return
			currentMethodInstructions.add(new ReturnNode(new Immediate(9999)));
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
		
		// need to save assignment register
		++currentRegister;

		
		//case 1: assignment to variable location (local var or field (this or external)
		if(assignment.getVariable() instanceof VariableLocation ){
			
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
			
		}
		
		
		// case 2 : assignment to array
		
		else
		{
			
			RegWithIndex arrayLoc = ArrayLocationCommonVisit((ArrayLocation) assignment.getVariable());
			
			// store the assignment in the array
			
			this.currentMethodInstructions.add(new StoreArrayNode(arrayLoc, assignmentResult));	
			
		}
		// no longer need assignment register
		--currentRegister;
		
		return null;
	
	}



	//TODO handle RDummy

	@Override
	public Object visit(CallStatement callStatement) throws SemanticError  {
		
		callStatement.getCall().accept(this);
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
		LirNode cond_exp = (LirNode) ifStatement.getCondition().accept(this);
		
		Reg b=subExp_into_reg(cond_exp);
		
		//compare and jump to else if exist and out of if if doesn't
		this.currentMethodInstructions.add(new CompareNode(new Immediate(1),b));
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
		LirNode cond_exp = (LirNode) whileStatement.getCondition().accept(this);
				
		Reg b = subExp_into_reg(cond_exp);
		
		//compare and jump if condition false
		this.currentMethodInstructions.add(new CompareNode(new Immediate(1),b));
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


	private Reg subExp_into_reg(LirNode cond_exp) {
		Reg b;
				
		if(cond_exp instanceof Reg)
		{
			
			b = (Reg)cond_exp;
			
		}
		else {
			
			// must load into a register
			b = new Reg(currentRegister);
			this.currentMethodInstructions.add(new MoveNode(cond_exp, b));
			
			
		}
		return b;
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
				
			//runtime check prefix != null
			List<LirNode> params = new ArrayList<LirNode>();
			params.add(classObject);
			//this.currentMethodInstructions.add(new StaticCall(new Label("__checkNullRef"),params,new Reg(currentRegister)));
			
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
		
		/*//runtime check array != null
		List<LirNode> params = new ArrayList<LirNode>();
		params.add(arrayObject);
		this.currentMethodInstructions.add(new LibraryCallNode(new Label("__checkNullRef"),params,new Reg(currentRegister)));
		*/
		
		
		// generate code for index
		
		LirNode indexNode;
		
		// optimization in case of single literal
		
		if(location.getIndex() instanceof Literal)
		{
			
			indexNode = (LirNode) location.getIndex().accept(this);
			/*//runtime check and arraySize < i-1
			params.add(indexNode);
			this.currentMethodInstructions.add(new LibraryCallNode(new Label("__checkArrayAccess"),params,new Reg(currentRegister)));*/
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
		
		/*//runtime check and arraySize < i-1
		params.add(indexNode);
		this.currentMethodInstructions.add(new LibraryCallNode(new Label("__checkArrayAccess"),params,new Reg(currentRegister)));
		*/
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

	
	public Reg libraryCallVisit(StaticCall call) throws SemanticError
	{
		
		// get method label
		Label methodLabel = this.labelGenerator.getLibraryMethodLabel(call.getName());
		
		int temp = currentRegister;
		// get values for arguments
		List<LirNode> values = getParameterValues(call);
		
		currentRegister = temp;
		Reg targetReg = new Reg(currentRegister);
		
		LibraryCallNode libCall = new LibraryCallNode(methodLabel, values, targetReg);
		
		this.currentMethodInstructions.add(libCall);
		return targetReg;
		
	}
	
	
	@Override
	public Object visit(StaticCall call) throws SemanticError  {
		
		// need to find out the name of the class that defines the method
		// might not actually be call.getClassName(), because, inheritance
		String className = call.getClassName();
		
		if(className.equals("Library"))
		{
			return this.libraryCallVisit(call);
		}
		
		ClassSymbol classSymobl = this.globSymTable.getClassSymbol(className);
		ClassSymbolTable classSymbolTable = classSymobl.getClassSymbolTable();
		
		// retrieve the method symbol table from the symbol table [ true stands for static ]
		MethodSymbol methodSym = classSymbolTable.getMethod(call.getName(), true);
		
		String definingClassName = methodSym.getClassName();
		
		
		StaticCallNode staticCallNode = staticCallCommonVisit(definingClassName, call);
		this.currentMethodInstructions.add(staticCallNode);
		return staticCallNode.getTargetReg();
	}




	@Override
	public Object visit(VirtualCall call) throws SemanticError {
		
		if(call.isExternal())
		{
			// check if external 
			
			ClassType objectClassType = (ClassType) call.getLocation().getNodeType();
			
			String staticTypeClassName = objectClassType.getName();
			
			// get the actual class that defines the method (overrides it last)
			// in other words, which method really holds the definition for this method call
			
			String definingClassName = classManager.getClassLayout(staticTypeClassName).getMethodClassName(call.getName());
			
			// generate code for the location
			LirNode classObj = (LirNode) call.getLocation().accept(this);
			Reg classObjReg;
			
			if(classObj instanceof Memory)
			{
				// load to register
				classObjReg = new Reg(currentRegister);
				
				// move to register
				this.currentMethodInstructions.add(new MoveNode(classObj, classObjReg));
			}
			else
			{
				// already in register
				classObjReg = (Reg)classObj;
			}
			
			/*//runtime check array != null
			currentRegister++;
			List<LirNode> params = new ArrayList<LirNode>();
			params.add(classObjReg);
			this.currentMethodInstructions.add(new LibraryCallNode(new Label("__checkNullRef"),params,new Reg(currentRegister)));
			currentRegister--;*/
			
			/* generate the call code, note that that method handles saving classObjReg */
			VirtualCallNode virtualCallNode = virtualCallCommonVisit(classObjReg, call, definingClassName);
			this.currentMethodInstructions.add(virtualCallNode);
			return virtualCallNode.getTargetReg();
			
			
		}
		else	
		{
			
			// may be a static call, may be a virtual call, get the symbol that defines this method
			MethodSymbol methodSym = ((VariableSymbolTable)call.enclosingScope()).getMethod(call.getName());
			
			// get the class that defines the method
			String className = methodSym.getClassName();
			
			if(methodSym.isStatic())
			{
				// same as static call
				StaticCallNode staticCallNode = staticCallCommonVisit(className, call);
				this.currentMethodInstructions.add(staticCallNode);
				return staticCallNode.getTargetReg();
			}
			
			// else, virtual method, object register should be loaded with this
			ThisNode thisNode = new ThisNode();
			Reg classObjReg = new Reg(currentRegister);
			this.currentMethodInstructions.add(new MoveNode(thisNode, classObjReg ));
			VirtualCallNode virtualCallNode = virtualCallCommonVisit(classObjReg, call, className);
			this.currentMethodInstructions.add(virtualCallNode);
			return virtualCallNode.getTargetReg();
			
		}
		
	
	}
	
	
	private List<Memory> generateParameterList(Method method)
	{
		
		// generate the list of parameters according to the given names of formals
		List<Memory> params = new ArrayList<Memory>();
							
		for(Formal formal : method.getFormals())
		{
							
			params.add(new Memory(formal.getName(), MemoryKind.PARAM));
							
		}
		
		return params;
		
	}
	
	
	
	private List<LirNode> getParameterValues(Call call) throws SemanticError
	{
		
		List<LirNode> values = new ArrayList<LirNode>();
		
		for(Expression exp : call.getArguments())
		{
			// visit each expression
			LirNode expressionResult = (LirNode) exp.accept(this);
						
			if(expressionResult instanceof Reg)
			{
				// need to backup register
				++this.currentRegister;
			}
						
			values.add(expressionResult);
						
		}
		return values;
		
	}
	
	
	/**
	 * this method handles common virtual visit. the method generates the proper instructions
	 * for loading parameters and generating the call node
	 * @param classRegister - the register that holds the class object
	 * @param call          - AST node call object (virtual call)
	 * @param definingClassName - the class name that actually defines the method
	 * @return
	 * @throws SemanticError
	 */
	
	private VirtualCallNode virtualCallCommonVisit(Reg classRegister, VirtualCall call, String definingClassName) throws SemanticError
	{
		
		// get the virtual method object			
		Method method = this.methodMap.get(definingClassName).get(call.getName());
					
					
		// generate the list of parameters according to the given names of formals
		List<Memory> params = generateParameterList(method);
			
		// remember the register we started with
		int temp = this.currentRegister;
					
		// save classRegister
		++this.currentRegister;
		
	    // get method offset
					
		Immediate methodOffset = new Immediate(this.classManager.getMethodOffset(definingClassName, call.getName()));
					
		List<LirNode> values = getParameterValues(call);
					
		// create a target register, may reuse registers
					
		this.currentRegister = temp;
		return new VirtualCallNode(classRegister, methodOffset, params, values, new Reg(currentRegister));
		
		
	}
	
	/**
	 * this method handles common static call visit. the method generates the proper instructions for 
	 * loading parameters and generating the call node
	 * @param className - the class that defines the given method
	 * @param call      - AST call node
	 * @return
	 * @throws SemanticError
	 */
	
	
	private StaticCallNode staticCallCommonVisit(String className, Call call) throws SemanticError
	{
		
		// generate method label
		Label methodLabel = this.labelGenerator.getStaticMethodLabel(call.getName(), className);
		
		// get the method object
		Method method = this.staticMethodMap.get(className).get(call.getName());
		
		// generate list of parameters according to the method
		List<Memory> params = generateParameterList(method);
		
		// back up initial counter
		int temp = currentRegister;
		
		// get value for each parameter
		List<LirNode> paramValues = getParameterValues(call);
		
		this.currentRegister = temp;
		
		return new StaticCallNode(methodLabel, params, paramValues, new Reg(currentRegister));
	}



	@Override
	public Object visit(This thisExpression)  {
		return new ThisNode();
	}

	@Override
	public Object visit(NewClass newClass) {
		//allocate object and return pointer reg
		LirNode class_size = new Immediate(classManager.getClassSize(newClass.getName())*4);
		Reg allocatedReg = allocate(class_size,"allocateObject");
		this.currentMethodInstructions.add(new StoreField(new RegWithOffset(allocatedReg, new Immediate(0)), this.labelGenerator.getClassDVLabel(newClass.getName())));
		return allocatedReg;
	}

	@Override
	public Object visit(NewArray newArray) throws SemanticError  {
		//allocate array and return pointer reg
		LirNode objectNum = (LirNode) newArray.getSize().accept(this);
		Reg objectNum_reg = subExp_into_reg(objectNum);
		this.currentMethodInstructions.add(new BinaryInstructionNode(lirBinaryOp.MUL,new Immediate(4),objectNum_reg));
		
		/*//so size check won't override
		currentRegister++;
		//runtime check array size > 0
		List<LirNode> params = new ArrayList<LirNode>();
		params.add(objectNum_reg);
		this.currentMethodInstructions.add(new LibraryCallNode(new Label("__checkSize"),params,new Reg(currentRegister)));
		//size check return is uninteresting
		currentRegister--;*/
		
		return allocate(objectNum_reg,"allocateArray");
	}
	
	private Reg allocate(LirNode allocated_size,String alloc_kind){
		List<LirNode> params = new ArrayList<LirNode>();
		//the only parameter will be array size
		params.add(allocated_size);
		//make target reg
		Reg allocation_reg = new Reg(currentRegister);
		//allocate array space
		this.currentMethodInstructions.add(new LibraryCallNode(this.labelGenerator.getLibraryMethodLabel(alloc_kind),params,allocation_reg));
		return allocation_reg;//return the reg to which allocated array
	}

	public Object visit(Length length) throws SemanticError  {
		
		// get array object
		LirNode array = (LirNode) length.getArray().accept(this);
		
		/*//runtime check array != null
		List<LirNode> params = new ArrayList<LirNode>();
		params.add(array);
		this.currentMethodInstructions.add(new LibraryCallNode(new Label("__checkNullRef"),params,new Reg(currentRegister)));
		*/
		// array may be a register or a local var (memory allowed)
		// currentRegister will hold the result
			
		this.currentMethodInstructions.add(new ArrayLengthNode(array , new Reg(currentRegister)));
		return new Reg(currentRegister);
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
			math_exp = concatenate_strings(binaryOp);
		}
		return math_exp;
	}
	
	
	private LirNode simple_binary_op(BinaryOp binaryOp, lirBinaryOp op) throws SemanticError {
		
		// OP a,b means b: = b OP a
		// b must be a register
		// a however, may be anything [immediate, memory, reg] !
		
		
		//calc FirstOperand (b), b will serve as a accumulator register
		LirNode right_exp = (LirNode) binaryOp.getFirstOperand().accept(this);
		
		Reg b=subExp_into_reg(right_exp);
		// backup register
		++this.currentRegister;
		
		
		// get secondOperand (a), may be anything!
		LirNode a = (LirNode) binaryOp.getSecondOperand().accept(this);
	
		// no longer need b register
		
		--this.currentRegister;
		
		//check zero div
		if (op == lirBinaryOp.DIV){
			/*//so zero check won't override
			currentRegister++;
			//runtime check array size > 0
			List<LirNode> params = new ArrayList<LirNode>();
			params.add(a);
			this.currentMethodInstructions.add(new LibraryCallNode(new Label("__checkZero"),params,new Reg(currentRegister)));
			//size check return is uninteresting
			currentRegister--;*/
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
			simple_binary_op(binaryOp,op);
			
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
			return new Reg(currentRegister);
			
		}
		else{//LAND or LOR
			//this label will be used if by calculating only the first op we will know the answer
			Label op_label = labelGenerator.createLabel();
			
			//calc FirstOperand into currentRegister
			LirNode first_exp = (LirNode) binaryOp.getFirstOperand().accept(this);
			
			Reg b=subExp_into_reg(first_exp);
			
			//save register
			currentRegister++;
			
			//test for "critical result"
			this.currentMethodInstructions.add(new CompareNode(new Immediate(0),b));
			if (op == lirBinaryOp.AND){
				this.currentMethodInstructions.add(new JumpTrueNode(op_label));
			}
			else {//op == lirBinaryOp.OR
				this.currentMethodInstructions.add(new JumpFalse(op_label));
			}
			
			//check second operand (if no "critical result") to currentRegister+1
			LirNode second_exp = (LirNode) binaryOp.getSecondOperand().accept(this);
			
			//op labels points of the op calculation. we "jumped" over calculating the second operation
			this.currentMethodInstructions.add(new LabelNode(op_label));
			//save op to currentRegister
			this.currentMethodInstructions.add(new BinaryInstructionNode(op,second_exp,b));
			
			// no longer need b register - if needed caller should handle
			--this.currentRegister;
			
			return b;
		}
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) throws SemanticError  {
		return unary_exp_calc(unaryOp,lirUnaryOp.NEG);
	}


	private Reg unary_exp_calc(UnaryOp unaryOp,lirUnaryOp op) throws SemanticError {
		//evaluate inner expression
		LirNode exp = (LirNode) unaryOp.getOperand().accept(this);
		
		Reg b=subExp_into_reg(exp);
		
		//only one unary math op - minus. calc it
		this.currentMethodInstructions.add(new UnaryInstructionNode(b,op));
		return b;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) throws SemanticError  {
		return unary_exp_calc(unaryOp,lirUnaryOp.NOT);
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

	@Override
	public Object visit(ExpressionBlock expressionBlock) throws SemanticError  {
		return expressionBlock.getExpression().accept(this);//return the reg/mem of the expression 
	}
	
	private LirNode concatenate_strings(MathBinaryOp binaryOp) throws SemanticError {
		//calc head string
		LirNode head = (LirNode) binaryOp.getFirstOperand().accept(this);
		Reg b;//in this register we will save the answer in the end
		
		if(head instanceof Reg)
		{
			b = (Reg)head;
		}
		else {
			// must load into a register
			b = new Reg(currentRegister);
		}
		//save reg from override
		currentRegister++;
		
		//calc tail string
		LirNode tail = (LirNode) binaryOp.getSecondOperand().accept(this);
		Label concatMethodLabel = this.labelGenerator.getLibraryMethodLabel("stringCat");
		this.currentMethodInstructions.add(new StringConcatinetionCall(concatMethodLabel, head,tail,b));
		
		//free saved register
		currentRegister--;
		
		return b;
	}

}
