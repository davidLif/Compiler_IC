package IC.SymTables.Symbols;

public class VirtualMethodSymbol extends MethodSymbol{

	public VirtualMethodSymbol(String id, String className) {
		super(id, className);
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
