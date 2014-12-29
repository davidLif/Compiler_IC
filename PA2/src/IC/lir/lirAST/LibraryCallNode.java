package IC.lir.lirAST;

import java.util.List;


public class LibraryCallNode extends MethodCallNode{

	/**
	 * label of the method ("func-name" in spec file )
	 */
	
	private Label methodLabel;
	
	public LibraryCallNode(Label methodLabel, List<LirNode> params, Reg targetRegister) {
		super(params, targetRegister);
		
		this.methodLabel = methodLabel;
	}

	public String emit() {
		StringBuilder s =new StringBuilder();
		s.append("Library "+methodLabel.emit()+"(");
		for (LirNode arg:params){
			s.append(arg.emit());
		}
		s.append("),"+targetRegister.emit()+"\n");
		return s.toString();
	}

}
