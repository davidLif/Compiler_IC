package IC.lir;

import java.util.ArrayList;
import java.util.List;


import IC.lir.lirAST.ArrayLengthNode;
import IC.lir.lirAST.CompareNode;
import IC.lir.lirAST.Immediate;
import IC.lir.lirAST.JumpFalse;
import IC.lir.lirAST.JumpL;
import IC.lir.lirAST.JumpNode;
import IC.lir.lirAST.Label;
import IC.lir.lirAST.LabelNode;
import IC.lir.lirAST.LibraryCallNode;
import IC.lir.lirAST.LirNode;
import IC.lir.lirAST.Memory;
import IC.lir.lirAST.MethodNode;
import IC.lir.lirAST.Reg;
import IC.lir.lirAST.ReturnNode;
import IC.lir.lirAST.SpaceNode;
import IC.lir.lirAST.StaticCallNode;
import IC.lir.lirAST.StringLiteralNode;
import IC.lir.lirAST.Memory.MemoryKind;

public class RuntimeChecks {


	/**
	 * used for generating labels, specifically our runtime check method labels in this case
	 */
	private LabelGenerator labelGen;
	
	
	/**
	 * label to jump to in order to exit the program
	 */
	
	private Label exitLabel;
	
	private List<StringLiteralNode> stringDefinitions;
	
	public RuntimeChecks(LabelGenerator labelGen, Label exitLabel,List<StringLiteralNode> stringDefinitions)
	{
		
		this.labelGen = labelGen;
		this.exitLabel = exitLabel;
		this.stringDefinitions = stringDefinitions;
	}
	

	public List<MethodNode> getImplementation()
	{
		
		List<MethodNode> methods = new ArrayList<MethodNode>();
		
		// add all methods
		methods.add(getNullRefImplementation());
		methods.add(getArrayAccessImplementation());
		methods.add(getSizeImplementation());
		methods.add(getZeroImplementation());
		
		return methods;

		
	}
	
	/**
	 * 
	 * the following methods provide implementation for the runtime checks
	 * 
	 */
	
	
	
	private MethodNode getNullRefImplementation()
	{
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkNullRef");
		
		Memory param = new Memory("a", MemoryKind.PARAM);
		
		Label correct_null_ref = labelGen.createLabel();
		instructions.add(new CompareNode(new Immediate(0),param));
		instructions.add(new JumpFalse(correct_null_ref));
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_null = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode("Runtime Error: Null pointer dereference!", labelGen));
		param_check_null.add(labelGen.getStringLabel("Runtime Error: Null pointer dereference!"));
		//print and exit
		instructions.add(new LibraryCallNode(new Label("__println"),param_check_null,new Reg(0)));
		instructions.add(new JumpNode(new Label("exit")));//quit program
		instructions.add(new LabelNode(correct_null_ref));
		instructions.add(new ReturnNode(new Immediate(1)));

		
		return new MethodNode(methodLabel, instructions);	
	}
	
	private MethodNode getArrayAccessImplementation()
	{
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkArrayAccess");
		
		Memory arrayParam = new Memory("a", MemoryKind.PARAM);
		Memory indexParam = new Memory("i", MemoryKind.PARAM);
		
		
		Label correct_array_access = labelGen.createLabel();
		instructions.add(new ArrayLengthNode(arrayParam,new Reg(0)));
		instructions.add(new CompareNode(indexParam,new Reg(0)));
		instructions.add(new JumpL(correct_array_access));
		
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_array_access = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode("Runtime Error: Array index out of bounds!", labelGen));
		param_check_array_access.add(labelGen.getStringLabel("Runtime Error: Array index out of bounds!"));
		//print and exit
		instructions.add(new LibraryCallNode(new Label("__println"),param_check_array_access,new Reg(0)));
		instructions.add(new JumpNode(exitLabel));//quit program
		
		instructions.add(new LabelNode(correct_array_access));
		instructions.add(new ReturnNode(new Immediate(1)));
		
		return new MethodNode(methodLabel,instructions);
	}
	
	
	private MethodNode getSizeImplementation()
	{
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkSize");
		
		Memory numItems = new Memory("n", MemoryKind.PARAM);
		
		
		Label correct_size = labelGen.createLabel();
		instructions.add(new CompareNode(new Immediate(0), numItems));
		instructions.add(new JumpL(correct_size));
		
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_size = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode("Runtime Error: Division by zero!", labelGen));
		param_check_size.add(labelGen.getStringLabel("Runtime Error: Division by zero!"));
		//print and exit
		instructions.add(new LibraryCallNode(new Label("__println"),param_check_size,new Reg(0)));
		instructions.add(new JumpNode(exitLabel));//quit program
		
		instructions.add(new LabelNode(correct_size));
		instructions.add(new ReturnNode(new Immediate(1)));
		
		return new MethodNode(methodLabel,instructions);
		
	}
	
	
	
	private MethodNode getZeroImplementation()
	{
		List<LirNode> instructions = new ArrayList<LirNode>();
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkZero");
		
		Memory devisor = new Memory("b", MemoryKind.PARAM);
		
		
		Label correct_zero = labelGen.createLabel();
		//inject_checks.add(new LabelNode(this.labelGenerator.get_runTime_checks_Label("checkZero", null))));
		instructions.add(new CompareNode(new Immediate(0),devisor));
		instructions.add(new JumpFalse(correct_zero));
		
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_zero = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode("Runtime Error: Array index out of bounds!", labelGen));
		param_check_zero.add(labelGen.getStringLabel("Runtime Error: Array index out of bounds!"));
		//print and exit
		instructions.add(new LibraryCallNode(new Label("__println"),param_check_zero,new Reg(0)));
		instructions.add(new JumpNode(exitLabel));//quit program
		
		instructions.add(new LabelNode(correct_zero));
		instructions.add(new ReturnNode(new Immediate(1)));
		
		return new MethodNode(methodLabel,instructions);
		
	}
	
	/**
	 * method returns a NullRefCheck
	 * @param value      - the value to pass to the method
	 * @param targetReg  - the register to store the value at
	 * @return
	 */
	
	public StaticCallNode getNullRefCheck(LirNode value, Reg targetReg)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkNullRef");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("a", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(value);
		
		return new StaticCallNode(methodLabel, memVars, values, targetReg);
		
		
		
	}
	

	/**
	 * method returns an ArrayAccessCheck
	 * @param value      - the value to pass to the method
	 * @param targetReg  - the register to store the value at
	 * @return
	 */
	
	public StaticCallNode getArrayAccessCheck(LirNode value, Reg targetReg)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkArrayAccess");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("a", MemoryKind.PARAM));
		memVars.add(new Memory("i", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(value);
		
		return new StaticCallNode(methodLabel, memVars, values, targetReg);
		
		
		
	}

	/**
	 * method returns a checkSize call
	 * @param value      - the value to pass to the method
	 * @param targetReg  - the register to store the value at
	 * @return
	 */
	
	public StaticCallNode getSizeCheck(LirNode value, Reg targetReg)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkSize");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("n", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(value);
		
		return new StaticCallNode(methodLabel, memVars, values, targetReg);
		
		
	}
	
	/**
	 * method returns a checkZero call
	 * @param value      - the value to pass to the method
	 * @param targetReg  - the register to store the value at
	 * @return
	 */
	
	public StaticCallNode getCheckZero(LirNode value, Reg targetReg)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkZero");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("b", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(value);
		
		return new StaticCallNode(methodLabel, memVars, values, targetReg);
		
		
	}


}
