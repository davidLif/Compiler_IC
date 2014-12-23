package IC.lir.lirAST;

import java.util.List;

import IC.lir.Label;

public class LibraryCallNode extends MethodCallNode{

	private Label methodLabel;
	
	public LibraryCallNode(Label methodLabel, List<Parameter> params, Reg targetRegister) {
		super(params, targetRegister);
		
		
		this.methodLabel = methodLabel;
	}

}
