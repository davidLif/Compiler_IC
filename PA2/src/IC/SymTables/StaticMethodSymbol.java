package IC.SymTables;

public class StaticMethodSymbol extends MethodSymbol{

	public StaticMethodSymbol(String id) {
		super(id);
	}



	@Override
	protected String getMethodKind() {
		
		return "Static";
	}
	

}
