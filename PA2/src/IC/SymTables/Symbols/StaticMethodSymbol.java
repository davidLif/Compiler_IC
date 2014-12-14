package IC.SymTables.Symbols;

public class StaticMethodSymbol extends MethodSymbol{

	public StaticMethodSymbol(String id) {
		super(id);
		this.kind = SymbolKind.STATIC_METHOD;
	}



	@Override
	protected String getMethodKind() {
		
		return "Static";
	}



	@Override
	public boolean isStatic() {
		
		return true;
	}
	

}
