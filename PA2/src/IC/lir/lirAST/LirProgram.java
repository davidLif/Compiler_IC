package IC.lir.lirAST;

import java.util.List;

import IC.lir.LirTranslator;

public class LirProgram extends LirNode {

	/**
	 * list of string literal definitions
	 */
	
	private List<StringLiteralNode> strings;
	
	
	
	/**
	 * list of class dispatch table definitions
	 */
	
	private List<DispatchTableNode> dispatchTables;
	
	/**
	 * list of lir methods
	 */
	
	private List<MethodNode> methods;
	
	
	
	/**
	 *  used for termination
	 */
	
	private Label exitLabel; 
	
	
	/**
	 * entrance label
	 */
	
	private Label mainMethodLabel;
	
	
	public LirProgram(List<StringLiteralNode> strings, List<DispatchTableNode> dispatchTables, List<MethodNode> methods, Label exitLabel, Label mainMethodLabel)
	{
		this.strings = strings;
		this.dispatchTables = dispatchTables;
		this.methods = methods;
		this.mainMethodLabel = mainMethodLabel;
		this.exitLabel = exitLabel;
		
	}
	
	

	@Override
	public String emit() {
		
		StringBuilder sb= new StringBuilder();
		for(StringLiteralNode stringLiteralDef : strings)
		{
			sb.append(stringLiteralDef.emit() + "\n");
			
		}
		sb.append("\n");
		
		for(DispatchTableNode dispatchTable : dispatchTables)
		{
			sb.append(dispatchTable.emit() + "\n\n");
		}
		
		String mainMethod = null;
		for(MethodNode method : methods)
		{
			if (method.methodLabel == this.mainMethodLabel){
				mainMethod = method.emit() + "\n";
				continue;
			}
			sb.append(method.emit() + "\n");
		}
		// add main method last
		sb.append(mainMethod + "\n");
		// add exit label
		sb.append(new LabelNode(this.exitLabel).emit());	
		return sb.toString();
	}
}
