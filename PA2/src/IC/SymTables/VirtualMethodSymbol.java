package IC.SymTables;

public class VirtualMethodSymbol extends MethodSymbol{

	public VirtualMethodSymbol(String id) {
		super(id);
		
	}

	@Override
	protected String getMethodKind() {
		return "Virtual";
	}

	

}
