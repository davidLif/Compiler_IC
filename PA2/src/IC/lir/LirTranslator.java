package IC.lir;

import java.util.ArrayList;
import java.util.List;

import IC.LiteralTypes;
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
import IC.AST.While;
import IC.SemanticChecks.SemanticError;
import IC.lir.lirAST.DispatchTableNode;
import IC.lir.lirAST.Label;
import IC.lir.lirAST.LirNode;
import IC.lir.lirAST.LirProgram;
import IC.lir.lirAST.MethodNode;
import IC.lir.lirAST.StringLiteralNode;

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
		if(debug)
			return new MethodNode(methodLabel, instructions);
		//
		
		for(Statement stmt : method.getStatements())
		{
			// generate instructions from statement

			@SuppressWarnings("unchecked")
			List<LirNode> statementInstructions = (List<LirNode>)stmt.accept(this);
			
			// add all new instructions
			instructions.addAll(statementInstructions);
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

	@Override
	public Object visit(Formal formal)  {
		// no lir instructions generated for formals, wont be visited
		return null;
	}

	@Override
	public Object visit(PrimitiveType type)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(UserType type)  {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object visit(Assignment assignment) throws SemanticError {
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		
		// generate instructions
		instructions.addAll((List<LirNode>)assignment.getAssignment().accept(this));
		
		instructions.addAll((List<LirNode>)assignment.getVariable().accept(this));
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(While whileStatement)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Break breakStatement)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location)  {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public Object visit(Length length)  {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public Object visit(MathUnaryOp unaryOp)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp)  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal)  {
		
		// use this
		
		if(literal.getType() == LiteralTypes.STRING){
		
			String value = literal.getValue().toString();
		
			// add new string definition to lir program

			stringDefinitions.add(new StringLiteralNode(value, labelGenerator));
		}
		
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock)  {
		// TODO Auto-generated method stub
		return null;
	}

}
