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
	 * used for add_run_time_checks_implementation function
	 */
	LirTranslator for_inject;
	
	public LirProgram(List<StringLiteralNode> strings, List<DispatchTableNode> dispatchTables, List<MethodNode> methods,LirTranslator for_inject)
	{
		this.strings = strings;
		this.dispatchTables = dispatchTables;
		this.methods = methods;
		this.for_inject = for_inject;
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
		//inject run time checks
		for(LirNode injectRow : for_inject.add_run_time_checks_implementation())
		{
			sb.append(injectRow.emit());
		}
		sb.append("\n");
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
				sb.append(method.emit());
			}
		}
		sb.append(new LabelNode(new Label("exit")).emit());
		
		
		return sb.toString();
	}
}
