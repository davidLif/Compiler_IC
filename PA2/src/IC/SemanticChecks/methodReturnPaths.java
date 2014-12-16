package IC.SemanticChecks;

import java.util.List;

import IC.DataTypes;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;
import IC.SymTables.ClassSymbolTable;
import IC.SymTables.Symbols.MethodSymbol;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

/**
 * 
 * @author Denis
 *
 * this class checks that each path in a method returns a value (if return type is not void)
 *
 */


public class methodReturnPaths implements Visitor{

	
	/*
	 * type table of current program
	 */
	private TypeTable typeTable;
	
	/*
	 * ast root
	 */
	private Program prog;
	
	
	
	/**
	 * use this method to perform the checks, throws semantic error if such error is found
	 * @throws SemanticError
	 */
	
	public void check() throws SemanticError
	{
		this.prog.accept(this);
	}
	
	
	@Override
	public Object visit(Program program) throws SemanticError {
		
		// visit all the classes
		List<ICClass> classList = program.getClasses();
		
		for(ICClass icClass : classList)
		{
			icClass.accept(this);
		}
		
		
		return null;
	}
	
	
	
	public methodReturnPaths (Program prog, TypeTable typeTable)
	{
		this.prog = prog;
		this.typeTable = typeTable; 
	}
	

	

	@Override
	public Object visit(ICClass icClass) throws SemanticError {
		
		
		// visit all methods
		List<Method> methodList = icClass.getMethods();
		
		for(Method method : methodList)
		{
			
			if(method instanceof LibraryMethod)
			{
				// methods contain only definition
				continue;
			}
			
			/* get the method symbol */
			MethodSymbol methodSym = ((ClassSymbolTable)method.enclosingScope()).getMethod(method.getName(), method.isStatic());
			/* get return type */
			Type retType = ((MethodType) methodSym.getType()).getReturnType();
			
			if(retType == typeTable.type_map_primitive.get(DataTypes.VOID))
			{
				/* automatically valid, no need to return on each path */
				continue;
			}
			
			if(method.getStatements().size() == 0)
			{
				// empty list, obviously an error
				String error = String.format("method %s does not return a value", method.getName());
				throw new SemanticError(method.getLine(), error);
			}
			
			if((Boolean)method.accept(this) == false)
			{
				String error = String.format("not all paths in method %s return a value", method.getName());
				throw new SemanticError(method.getLine(), error);
			}
		}
		
		return null;
	}

	@Override
	public Object visit(Field field) throws SemanticError {
		
		// wont visit
		return null;
	}
	
	
	
	/**
	 * this method checks that all the return paths on given statement block returns a value
	 * 
	 * this method returns true iff all possible paths return a value
	 * 
	 * statementList represents either a statementsBlock or a method
	 * 
	 * @param statementList  - A non empty (ordered) list of statements
	 * 
	 */
	
	private boolean checkStatements(List<Statement> statementList) throws SemanticError
	{
		
		// scan the statements from start to finish
		
		
		boolean statementReturns = false;  // this flag holds whether we found a statement that returns at all paths
		
		for(int curr_index = 0 ; curr_index < statementList.size() ; ++curr_index)
		{
			
			Statement stmt = statementList.get(curr_index);
			
			// if we find a return statement, finished, ALL paths return.
			
			if(stmt instanceof Return)
			{
				/* check passed */
				statementReturns = true;
				break;
			}
			
			else if(stmt instanceof While)
			{
				/* check cannot pass, we have a path that finishes the while */
			
				continue;
			}
			
			else if(stmt instanceof If)
			{
				/* gotta check both action and else */
				
				Statement action = ((If)stmt).getOperation();
				if(action instanceof StatementsBlock)
				{
					// visit the block, must return at all paths
					if((Boolean)action.accept(this)  == false)
					{
						// check failed
						continue;
					}
				}
				else if (action instanceof Return)
				{
					// action returns a value, valid
				}
				else
				{
					// action does not return a value
					// check failed, no return
					continue;
				}
				
				// at this point, if action returns a value in all possible paths
				
				If ifStmt = (If)stmt;
				if( ifStmt.hasElse())
				{
					Statement elseStmt = ifStmt.getElseOperation();
					
					// else must return a value
					if(elseStmt instanceof StatementsBlock)
					{
						// visit the block, must return at all paths
						if((Boolean)elseStmt.accept(this) == false){
							
							// check failed
							continue;
						}
					}
					else if (elseStmt instanceof Return)
					{
						// else action returns a value, valid
						
					}
					else
					{
						// else action does not return a value
						continue;
					}
				}
				else
				{
					
					// if there is no else statement, there is always a path that does not take the 
					// if action, statically speaking
					continue;
				}
				
				// if returns at all paths
				statementReturns = true;
				break;
					
				
			}
			
			else if (stmt instanceof StatementsBlock)
			{
				// visit the block, all paths must return a value
				if((Boolean)stmt.accept(this) == false)
					continue;
				
				// statement block returns at all paths
				statementReturns = true;
				break;
			}
			
			// else, keep looking
				
		
			
		}
		
		if(statementReturns)
		{
			// test succeeded
			return true;
		}
		
		// otherwise, some path does not return a value
		return false;
		
	
		
		
		
		
	}
	

	@Override
	public Object visit(VirtualMethod method) throws SemanticError {
		
		/* check all the statements */
		return checkStatements(method.getStatements());

		
	}

	@Override
	public Object visit(StaticMethod method) throws SemanticError {
		/* check all the statements */
		return checkStatements(method.getStatements());
		
		
	}

	@Override
	public Object visit(LibraryMethod method) throws SemanticError {
		
		// automatically valid, no statements
		return true;
	}

	@Override
	public Object visit(Formal formal) throws SemanticError {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(UserType type) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Assignment assignment) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(While whileStatement) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Break breakStatement) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) throws SemanticError {
		
		
		// statement block must return at all paths, same as a method
		return checkStatements(statementsBlock.getStatements());
		
	
	}

	@Override
	public Object visit(LocalVariable localVariable) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) throws SemanticError {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
