package IC.SymTables.Symbols;

public class VirtualMethodSymbol extends MethodSymbol{

	public VirtualMethodSymbol(String id) {
		super(id);
		this.kind = SymbolKind.VIRTUAL_METHOD;
		
	}

	@Override
	protected String getMethodKind() {
		return "Virtual";
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	

}
