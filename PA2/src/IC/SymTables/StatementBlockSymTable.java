package IC.SymTables;

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






}
