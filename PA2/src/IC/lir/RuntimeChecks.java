package IC.lir;

import java.util.ArrayList;
import java.util.List;


import IC.lir.lirAST.Label;
import IC.lir.lirAST.LirNode;
import IC.lir.lirAST.Memory;
import IC.lir.lirAST.MethodNode;
import IC.lir.lirAST.Reg;
import IC.lir.lirAST.StaticCallNode;
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
	
	public RuntimeChecks(LabelGenerator labelGen, Label exitLabel)
	{
		
		this.labelGen = labelGen;
		this.exitLabel = exitLabel;
		
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
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkNullRef");
		
		Memory param = new Memory("a", MemoryKind.PARAM);
		
		//...
		
		
		
		
		
		return null;
		
		
	}
	
	private MethodNode getArrayAccessImplementation()
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkArrayAccess");
		
		Memory arrayParam = new Memory("a", MemoryKind.PARAM);
		Memory indexParam = new Memory("i", MemoryKind.PARAM);
		
		
		//...
		
		return null;
	}
	
	
	private MethodNode getSizeImplementation()
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkSize");
		
		Memory numItems = new Memory("n", MemoryKind.PARAM);
		
		
		//...
		
		return null;
		
	}
	
	
	
	private MethodNode getZeroImplementation()
	{
		
		Label methodLabel = labelGen.getRuntimeCheckLabel("checkZero");
		
		Memory devisor = new Memory("b", MemoryKind.PARAM);
		
		
		//...
		
		return null;
		
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
