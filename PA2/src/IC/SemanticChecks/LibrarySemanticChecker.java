package IC.SemanticChecks;

import java.util.List;

import IC.AST.ASTNode;
import IC.AST.ICClass;
import IC.AST.Program;

public class LibrarySemanticChecker {

	
	public Boolean check(Program program) throws SemanticError
	{
		List<ICClass> classList = program.getClasses();
		
		if (!classList.get(0).getName().equals("Library")) /* denis - is this the way to know th elib name?? */
		{
			String err_msg = "Library class name is not Library!";
			throw new SemanticError(classList.get(0).getLine(), err_msg);
		}
		return true;
	}
	
}
