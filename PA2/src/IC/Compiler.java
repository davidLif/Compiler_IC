package IC;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.Parser.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;


import java_cup.runtime.Symbol;
import IC.SemanticChecks.ChecksMainCorrectness;
import IC.SemanticChecks.InitBeforeUse;
import IC.SemanticChecks.StructuralChecks;
import IC.SemanticChecks.methodReturnPaths;
import IC.SymTables.*;
import IC.Types.SymbolSetter;
import IC.Types.TypeEvaluator;
import IC.Types.TypeTable;
import IC.lir.LirTranslator;
import IC.lir.lirAST.LirProgram;

/**
 * @team pooyae <pooyae@mail.tau.ac.il>
 *  Team member 206107740
 *  Team member 320882988
 *  Team member 305686297
 *
 */

public class Compiler {


	/* list of syntax errors that occured during parsing */
	private static List<SyntaxError> syntax_errors = null; 
	
	

	public static void main(String[] args) {
	   
	    /* check given arguments */
		
		if(args.length == 0 || args.length > 4)
		{
			System.err.println("Invalid number of arguments");
			return;
			
		}
		
		/* program flags */
		String library_name = null;
		boolean dumpSymTable = false;
		boolean printAST = false;
		boolean makeLir = false;
		
		for(int i = 1 ; i < args.length; ++i)
		{
		
		
			// argument was provided
			String temp = args[i];
			if(temp.substring(0, 2).equals("-L"))
			{
				// library argument was provided
				library_name = temp.substring(2);
			}
			else if(temp.equals("-print-ast"))
			{
				printAST = true;
			}
			else if(temp.equals("-dump-symtab"))
			{
				dumpSymTable = true;
			}
			else if(temp.equals("-print-lir"))
			{
				makeLir = true;
			}
			else
			{
				System.err.println("invalid argument: " + temp);
				return;
			}
		
		
		}
		
		try {
			// parse the program first
			FileReader programFile = new FileReader(args[0]);
			Lexer scanner = new Lexer(programFile);
			Parser prog_parser = new Parser(scanner);
			
			try {
				
				/* parse the lib file first */
				
				LibParser lib_parser = null;
				Program lib_prog=      null;
				if(library_name != null)
				{
					FileReader libFile = new FileReader(library_name);
					scanner = new Lexer(libFile);
					lib_parser = new LibParser(scanner);
					
					Symbol  lib_root = lib_parser.parse();
					if(syntax_errors != null)
					{
						// parsing failed
						throw syntax_errors.get(0);
						
					}
					
					lib_prog = (Program)lib_root.value;
					System.out.println(String.format("Parsed %s successfully!", library_name));
					
					
				}
				
				/* parse the program */
				
				Symbol root = prog_parser.parse();
				
				if(syntax_errors != null)
				{
					// parsing, recovered successfully, however errors ocurred.
					throw syntax_errors.get(0);
				}
				
				// otherwise, save the progarm's AST tree
				Program prog = (Program)root.value;
				
				if(library_name != null)
				{
					/* add library class to AST */
					prog.addLibClass(lib_prog.getClasses().get(0));
				}
				
				/* otherwise, everything went smooth, print success message */
				System.out.println(String.format("Parsed %s successfully!", args[0]));
				
				
				/* create type table */
				TypeTable typeTable = new TypeTable(args[0]);
				
				
				/* check main and add main method types */
				ChecksMainCorrectness mainChecker = new ChecksMainCorrectness(prog, typeTable);
				mainChecker.check();
				
				/* create global symbol table */
				SymbolTableBuilder symTableBuilder = new SymbolTableBuilder();
				GlobalSymbolTable globalSymbolTable = (GlobalSymbolTable) symTableBuilder.createGlobalSymbolTable(prog, args[0]);
				
				
				/* initialize the symbols' types and fill the type table */
				SymbolSetter typeSetter = new SymbolSetter(prog, typeTable, globalSymbolTable );
				typeSetter.setSymbolTypes();
				
				/* finish checking scope rules and proper inheritance */
				IC.SemanticChecks.InheritanceCheck.check(prog, globalSymbolTable);
				
				
				/* check that program structure is valid */
				StructuralChecks structChecks = new StructuralChecks();
				structChecks.check(prog);
				
				/* perform typing checks */
				TypeEvaluator evaluate = new TypeEvaluator(typeTable, globalSymbolTable);
				prog.accept(evaluate);
				
				
				/* check that variables are initialized before use */
				InitBeforeUse initBeforeUseTest = new InitBeforeUse(prog);
				initBeforeUseTest.check();
				
				/* all paths return value */
				methodReturnPaths methodPaths = new methodReturnPaths(prog, typeTable);
				methodPaths.check();
				
				
				if(dumpSymTable)
				{
					/* print the symbol tables */
					globalSymbolTable.printTable();
				
					/* print the type tables */
					System.out.print(typeTable + "\n");
				}
				
				if(printAST)
				{
					/* if we want to print the AST, we gotta skip the library class first */
					if(library_name != null)
					{
						prog.removeLibClass();
					}
					PrettyPrinter prog_printer = new PrettyPrinter(args[0]);
					String str_prog = (String) prog.accept(prog_printer);
					System.out.println(str_prog);
					
					/* add lib class back */
					if(library_name != null)
					{
						prog.addLibClass(lib_prog.getClasses().get(0));
					}
				}
				
				
				
				// moving to lir
				LirTranslator translator = new LirTranslator(prog, globalSymbolTable);
				LirProgram result = translator.translate();
				
				if(makeLir)
				{
				
					
				
					// output to file
					String newFileName;
					if(args[0].endsWith(".ic"))
					{
						newFileName = args[0].substring(0, args[0].length() - 3);
					}
					else if(args[0].endsWith(".txt"))
					{
						newFileName = args[0].substring(0, args[0].length() - 4);
					}
					else
					{
						// don't know, keep it this way
						newFileName = args[0];
					}
				
					PrintWriter writer = new PrintWriter(newFileName + ".lir");
					writer.print(result.emit());
					writer.close();
				
				}
				
				
			} 
			catch (SyntaxError e)
			{
				/* print all error list, e was only used to get to this point*/
				
				PrintSyntaxErrors();
			}
			
			catch (LexicalError e)
			{
				String error = "";
				if(e.isTokenError())
					error = String.format("%d:%d : lexical error; %s", e.getLine(), e.getColumn(), e.getErrorDescription());
				else
					// error of different type, possibly IO
					error = e.getMessage();
				System.err.println(error);
			}
			
			catch (Exception e) {
				
				if(syntax_errors != null)
				{
					/* syntax errors found */
					PrintSyntaxErrors();
				}
				else{
					System.err.println( e.getMessage());
					//System.err.println( e.getStackTrace());
				}
				
			}
			
		} catch (FileNotFoundException e) {
			
			System.err.println(e.getMessage());
		}
			
	}
	
	/* this method should be called by the parser to set a syntax error list */
	public static void SetSyntaxErrors(List<SyntaxError> errorLst)
	{
		Compiler.syntax_errors = errorLst;
		
	}
	
	private static void PrintSyntaxErrors()
	{
		for(SyntaxError err : syntax_errors){
				
				String error = String.format("%d:%d : syntax error; %s", err.getLine(), err.getColumn(), err.getErrorDescription());
				System.err.println(error);
		}
	}
	

	
	


}

