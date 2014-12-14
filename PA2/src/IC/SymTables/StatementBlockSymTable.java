package IC.SymTables;

import IC.AST.VariableLocation;
import IC.SymTables.Symbols.MethodSymbol;
import IC.SymTables.Symbols.Symbol;
import IC.Types.ClassType;
import IC.Types.MethodType;


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
		
		for(Symbol sym : this.localVarsList.values())
		{
			System.out.println("\t" + sym.toString());
		}
		
		this.printChildernTables();
		
	}
	

	
	@Override
	public  MethodSymbol getMethod(String name) {
		
		/* we know for certain we need to continue to the enclosing scope
		 * we also know that we cannot determine yet if the scope is static or not
		 */
		
		return ((VariableSymbolTable)this.parentSymbolTable).getMethod(name);
	}




	@Override
	public boolean resolveVariable(VariableLocation varLoc) {
		/* may be a local variable or a parameter or a field */
		
		if(this.localVarsList.containsKey(varLoc.getName()))
		{
			varLoc.setDefiningScope(this);
			return true;
		}
		
		// try to resolve the variable in the parent ( can only be variableSymbolTable - method or block )
		return ((VariableSymbolTable)this.parentSymbolTable).resolveVariable(varLoc);
	}




	@Override
	public ClassType getThisType() {
		
		return ((VariableSymbolTable)this.parentSymbolTable).getThisType();
		
	}




	@Override
	public MethodType getReturnType() {
		
		return ((VariableSymbolTable)this.parentSymbolTable).getReturnType();
	}





}
