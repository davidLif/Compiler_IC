package IC.SymTables;

import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;


/**
 * 
 * @author Denis
 *
 *	represents Statement block scope
 */

public class StatementBlockSymTable extends VariableSymbolTable{

	

	
	/**
	 * 
	 * constructor that infers the name of the statement block from the parent symbol table
	 * @param parentSymbolTable
	 */
	public StatementBlockSymTable( SymbolTable parentSymbolTable) {
		super("statement block in " + parentSymbolTable.getId(), parentSymbolTable);
		
	}
	



	@Override
	public void printTable() {
		System.out.println(String.format("Statement Block Symbol Table ( located in %s )", this.parentSymbolTable.getId()));
		
		for(Symbol sym : this.localVarsList)
		{
			System.out.println("\t" + sym.toString());
		}
		
		this.printChildernTables();
		
	}
	
	@Override
	public Symbol getVariable(String id) {
		
		/* may be a local variable or a parameter or a field */
		
		for(Symbol sym : this.localVarsList)
		{
			if(sym.getId().equals(id))
				return sym;
		}
		
		
		/* have not reached parent class yet, may be a local var or param or field */
		return ((VariableSymbolTable)this.parentSymbolTable).getVariable(id);
	}

	
	@Override
	public  MethodSymbol getMethod(String name) {
		
		/* we know for certain we need to continue to the enclosing scope
		 * we also know that we cannot determine yet if the scope is static or not
		 */
		
		return ((VariableSymbolTable)this.parentSymbolTable).getMethod(name);
	}





}
