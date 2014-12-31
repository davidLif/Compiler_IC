package IC.lir.lirAST;

import java.util.List;

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
	
	
	public LirProgram(List<StringLiteralNode> strings, List<DispatchTableNode> dispatchTables, List<MethodNode> methods)
	{
		this.strings = strings;
		this.dispatchTables = dispatchTables;
		this.methods = methods;
		
	}
	
	

	@Override
	public String emit() {
		
		StringBuilder sb= new StringBuilder();
		for(StringLiteralNode stringLiteralDef : strings)
		{
			sb.append(stringLiteralDef.emit() + "\n");
			
		}
		
		for(DispatchTableNode dispatchTable : dispatchTables)
		{
			sb.append(dispatchTable.emit() + "\n");
		}
		
		for(MethodNode method : methods)
		{
			if (method.methodLabel.emit().equals("_ic_main")){
				continue;
			}
			sb.append(method.emit() + "\n");
		}
		for(MethodNode method : methods)
		{
			if (method.methodLabel.emit().equals("_ic_main")){
				sb.append(method.emit() + "\n");
			}
		}
		
		
		return sb.toString();
	}
}
