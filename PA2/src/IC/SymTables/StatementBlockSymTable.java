package IC.SymTables;


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
	protected String getSymbolTableHeader() {
		return String.format("Statement Block Symbol Table ( located in %s )", this.parentSymbolTable.getId());
	}






}
