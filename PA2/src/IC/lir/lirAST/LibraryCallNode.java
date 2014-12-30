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

	@Override
	public String emit() {
		StringBuilder s =new StringBuilder();
		s.append("Library "+methodLabel.emit()+"(");
		
		for(int i = 0; i < params.size(); ++i)
		{
			if( i < params.size() - 1)
				s.append(params.get(i).emit() + ",");
			else
				s.append(params.get(i).emit() );
		}
		
		
		
		s.append("),"+targetRegister.emit()+"\n");
		return s.toString();
	}

}
