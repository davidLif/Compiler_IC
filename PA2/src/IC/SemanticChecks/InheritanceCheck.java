package IC.SemanticChecks;


import java.util.List;

import IC.AST.Field;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.Program;
import IC.SymTables.Symbols.*;
import IC.SymTables.*;
import IC.Types.*;
/**
 * 
 * @author Denis
 *
 *
 * this class checks that identifier are not defined multiple times in parent
 * scopes, thus, it also checks that inheritance is valid
 * 
 * 
 */

public class InheritanceCheck {

	
	public static void check(Program program, GlobalSymbolTable globSymTable) throws SemanticError
	{
		List<ICClass> classList = program.getClasses();
		
		for(ICClass icClass : classList)
		{
			if(icClass.hasSuperClass())
			{
				checkScopeCollisions(icClass, globSymTable);
			}
		}
		
	}
	
	
	/**
	 * 
	 * this method checks that baseClass sucecssfully extends its superclass
	 * meaning that:
	 * 		1. no field in baseClass hides fields or virtual methods in some superclass
	 * 		2. no virtual method in baseClass hides fields in some some class
	 * 		3. if a virtual method of same name exists in some superclass, it must override it correctly
	 *      4. if a static method of same name exists in some superclass, it must override it correctly
	 * 
	 * @param baseClass - ASTNode of base class, that has a superclass
	 * @param globSymTable - the global symbol table (contains all the class symbols)
	 * @throws SemanticError
	 */
	
	private static void checkScopeCollisions(ICClass baseClass, GlobalSymbolTable globSymTable) throws SemanticError
	{
		String err;
		
		ClassSymbol baseClassSym = globSymTable.getClassSymbol(baseClass.getName());
		
		/* get current class's scope */
		ClassSymbolTable baseClassSymTable = baseClassSym.getClassSymbolTable();
		
		/* get super class scope */
		ClassSymbolTable superScope = (ClassSymbolTable) baseClassSymTable.getParentSymbolTable();
		
		
		/* check field redefinitions */
		List<Field> fieldNodes = baseClass.getFields();
		
		for(Field field : fieldNodes)
		{
			if(superScope.resolveField(field.getName()) || superScope.resolveMethod(field.getName(), false))
			{
				/* same field/method was already defined in super class*/
				err = String.format("identifer of field %s already defined in a superclass", field.getName());
				throw new SemanticError(field.getLine(), err);
					
			}
			
		}
		
		/* check method redefinitions */
		
		
		List<Method> methodNodes = baseClass.getMethods();
		for(Method method : methodNodes)
		{
			if(!method.isStatic())
			{
				
				// virtual method
				
				if(superScope.resolveField(method.getName()))
				{
					// this method shadows a field that was defined in a superclass
					err = String.format("method %s shadows a field with same id defined in a superclass", method.getName());
					throw new SemanticError(method.getLine(), err);
				}
				
				MethodSymbol superMethodSym = superScope.getMethod(method.getName(), false);
				if(superMethodSym != null)
				{
					
					// virtual method is found in superclass
					// check valid overriding
					MethodSymbol currMethodSym = baseClassSymTable.getVirtualMethod(method.getName());
					if(!isValidOverriding(currMethodSym, superMethodSym))
					{
						err = String.format("method %s invalid overriding", method.getName());
						throw new SemanticError(method.getLine(), err);
					}
				}
			}
			else
			{
				// static method
				MethodSymbol superMethodSym = superScope.getMethod(method.getName(), true);
				if(superMethodSym != null)
				{
					// static method is found in superclass
					MethodSymbol currMethodSym = baseClassSymTable.getStaticMethod(method.getName());
					if(!isValidOverriding(currMethodSym, superMethodSym))
					{
						err = String.format("static method %s invalid overriding", method.getName());
						throw new SemanticError(method.getLine(), err);
					}
				}
			}
		}
		
	
	}
	
	/**
	 * this method checks that baseMethodSym represents a method that successfully overrides superMethodSym
	 * @param baseMethodSym - symbol of method that overrides
	 * @param superMethodSym - symbol of method to be overridden
	 * @return true iff overriding is legal
	 */
	
	public static boolean isValidOverriding(MethodSymbol baseMethodSym, MethodSymbol superMethodSym)
	{
		
		Type baseMethodType = baseMethodSym.getType();
		Type superMethodType = superMethodSym.getType();
		
		return baseMethodType.subTypeOf(superMethodType);
	}
	
	
}
