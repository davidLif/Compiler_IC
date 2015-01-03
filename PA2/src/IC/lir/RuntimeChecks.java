package IC.lir;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import IC.lir.lirAST.ArrayLengthNode;
import IC.lir.lirAST.CompareNode;
import IC.lir.lirAST.Immediate;
import IC.lir.lirAST.JumpFalse;
import IC.lir.lirAST.JumpG;
import IC.lir.lirAST.JumpL;
import IC.lir.lirAST.JumpNode;
import IC.lir.lirAST.Label;
import IC.lir.lirAST.LabelNode;
import IC.lir.lirAST.LibraryCallNode;
import IC.lir.lirAST.LirNode;
import IC.lir.lirAST.Memory;
import IC.lir.lirAST.MethodNode;
import IC.lir.lirAST.MoveNode;
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
	
	public RuntimeChecks(LabelGenerator labelGen, Label exitLabel,List<StringLiteralNode> stringDefinitions, Set<String> foundStrings)
	{
		
		this.labelGen = labelGen;
		this.exitLabel = exitLabel;
		this.stringDefinitions = stringDefinitions;
		
		foundStrings.add(nullRefMsg);
		foundStrings.add(indexBoundsMsg);
		foundStrings.add(allocationMsg);
		foundStrings.add(devZeroMsg);
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
	
	private final String nullRefMsg = "\"Runtime Error: Null pointer dereference!\"";
	private final String indexBoundsMsg = "\"Runtime Error: Array index out of bounds!\"";
	private final String allocationMsg = "\"Runtime Error: Array allocation with negative array size!\"";
	private final String devZeroMsg = "\"Runtime Error: Division by zero!\"";
	
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
		instructions.add(new MoveNode(param,new Reg(0)));
		
		Label correct_null_ref = labelGen.createLabel();
		instructions.add(new CompareNode(new Immediate(0),new Reg(0)));
		instructions.add(new JumpFalse(correct_null_ref));
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_null = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode(nullRefMsg, labelGen));
		param_check_null.add(labelGen.getStringLabel(nullRefMsg));
		//print and exit
		Label printlnLabel = this.labelGen.getLibraryMethodLabel("println");
		instructions.add(new LibraryCallNode(printlnLabel, param_check_null, new Reg(0)));
		instructions.add(new JumpNode(exitLabel));//quit program
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
		instructions.add(new JumpG(correct_array_access));
		
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_array_access = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode(indexBoundsMsg, labelGen));
		param_check_array_access.add(labelGen.getStringLabel(indexBoundsMsg));
		//print and exit
		Label printlnLabel = this.labelGen.getLibraryMethodLabel("println");
		instructions.add(new LibraryCallNode(printlnLabel,param_check_array_access,new Reg(0)));
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
		instructions.add(new MoveNode(numItems,new Reg(0)));
		
		Label correct_size = labelGen.createLabel();
		instructions.add(new CompareNode(new Immediate(0), new Reg(0)));
		instructions.add(new JumpG(correct_size));
		
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_size = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode(allocationMsg, labelGen));
		param_check_size.add(labelGen.getStringLabel(allocationMsg));
		//print and exit
		Label printlnLabel = this.labelGen.getLibraryMethodLabel("println");
		instructions.add(new LibraryCallNode(printlnLabel, param_check_size,new Reg(0)));
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
		instructions.add(new MoveNode(devisor,new Reg(0)));
		
		Label correct_zero = labelGen.createLabel();
		//inject_checks.add(new LabelNode(this.labelGenerator.get_runTime_checks_Label("checkZero", null))));
		instructions.add(new CompareNode(new Immediate(0),new Reg(0)));
		instructions.add(new JumpFalse(correct_zero));
		
		//if not null jump to return 1. else print and out
		List<LirNode> param_check_zero = new ArrayList<LirNode>();
		//add String to param
		stringDefinitions.add(new StringLiteralNode(devZeroMsg,labelGen));
		param_check_zero.add(labelGen.getStringLabel(devZeroMsg));
		//print and exit
		Label printlnLabel = this.labelGen.getLibraryMethodLabel("println");
		instructions.add(new LibraryCallNode(printlnLabel, param_check_zero,new Reg(0)));
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
	
	public StaticCallNode getNullRefCheck(LirNode value)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkNullRef");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("a", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(value);
		
		return new StaticCallNode(methodLabel, memVars, values, new Reg(-1));
		
		
		
	}
	

	/**
	 * method returns an ArrayAccessCheck
	 * @param value      - array register
	 * @param targetReg  - the register to store the value at
	 * @return
	 */
	
	public StaticCallNode getArrayAccessCheck(LirNode arr, LirNode index)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkArrayAccess");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("a", MemoryKind.PARAM));
		memVars.add(new Memory("i", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(arr);
		values.add(index);
		
		return new StaticCallNode(methodLabel, memVars, values, new Reg(-1));
		
		
		
	}

	/**
	 * method returns a checkSize call
	 * @param value      - the value to pass to the method
	 * @param targetReg  - the register to store the value at
	 * @return
	 */
	
	public StaticCallNode getSizeCheck(LirNode value)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkSize");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("n", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(value);
		
		return new StaticCallNode(methodLabel, memVars, values, new Reg(-1));
		
		
	}
	
	/**
	 * method returns a checkZero call
	 * @param value      - the value to pass to the method
	 * @param targetReg  - the register to store the value at
	 * @return
	 */
	
	public StaticCallNode getCheckZero(LirNode value)
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkZero");

		List<Memory> memVars = new ArrayList<Memory>();
		
		memVars.add(new Memory("b", MemoryKind.PARAM));
		
		List<LirNode> values = new ArrayList<LirNode>();
		values.add(value);
		
		return new StaticCallNode(methodLabel, memVars, values, new Reg(-1));
		
		
	}


}
