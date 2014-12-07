package IC.Parser;

import java_cup.runtime.Symbol;


public class Token extends Symbol{
	
	
	private int line;
	private int column;
	
	
	public int getId() { return super.sym; }
	public Object getValue() { return this.value; }
	public int getLine() { return this.line; }
	public int getColumn() { return this.column; }
	
	public Token(int id, Object value, int line, int column){
		
		super(id, value);
		this.line = line;
		this.column = column;

	}
	
	
	public static String getTokenRepById(int id)
	{
		String rep;
		
		switch(id)
		{
		case IC.Parser.sym.EXTENDS:
			rep = "extends";
			break;
		case IC.Parser.sym.STATIC:
			rep = "static";
			break;
		case IC.Parser.sym.VOID:
			rep = "void";
			break;
		case IC.Parser.sym.INT:
			rep = "int";
			break;
		case IC.Parser.sym.BOOL:
			rep ="boolean";
			break;
		case IC.Parser.sym.STRING:
			rep = "string";
			break;
		case IC.Parser.sym.RETURN:
			rep = "return";
			break;
		case IC.Parser.sym.IF:
			rep = "if";
			break;
		case IC.Parser.sym.ELSE:
			rep = "else";
			break;
		case IC.Parser.sym.WHILE:
			rep = "while";
			break;
		case IC.Parser.sym.BREAK:
			rep = "break";
			break;
		case IC.Parser.sym.CONTINUE:
			rep = "continue";
			break;
		case IC.Parser.sym.THIS:
			rep = "this";
			break;
		case IC.Parser.sym.NEW:
			rep = "new";
			break;
		case IC.Parser.sym.LENGTH:
			rep = "length";
			break;
		case IC.Parser.sym.CLASS:
			rep = "class";
			break;
		
		// problematic cases
		case IC.Parser.sym.ID:
			rep = "ID";
			break;
		case IC.Parser.sym.CLASSID:
			rep = "CLASS_ID";
			break;
		case IC.Parser.sym.INTEGER:
			rep = "INTEGER";
			break;
		case IC.Parser.sym.STRING_LITERAL:
			rep = "STRING";
			break;
		case IC.Parser.sym.EOF:
			rep = "EOF";
			break;
		//
		case IC.Parser.sym.FALSE:
			rep = "false";
			break;
		case IC.Parser.sym.NULL:
			rep ="null";
			break;
		case IC.Parser.sym.TRUE:
			rep = "true";
			break;
		case IC.Parser.sym.LP:
			rep = "(";
			break;
		case IC.Parser.sym.RP:
			rep = ")";
			break;
		case IC.Parser.sym.CRP:
			rep = "}";
			break;
		case IC.Parser.sym.CLP:
			rep = "{";
			break;
		case IC.Parser.sym.SQUARELB:
			rep = "[";
			break;
		case IC.Parser.sym.SQUARERB:
			rep = "]";
			break;
		case IC.Parser.sym.DOT:
			rep = ".";
			break;
		case IC.Parser.sym.SEMCOL:
			rep = ";";
			break;
		case IC.Parser.sym.COMMA:
			rep = ",";
			break;
		case IC.Parser.sym.ADD:
			rep ="+";
			break;
		case IC.Parser.sym.MINUS:
			rep = "-";
			break;
		case IC.Parser.sym.MULT:
			rep = "*";
			break;
		case IC.Parser.sym.DIV:
			rep = "/";
			break;
		case IC.Parser.sym.MOD:
			rep = "%";
			break;
		case IC.Parser.sym.LESSTHAN:
			rep = "<";
			break;
		case IC.Parser.sym.LESSTHANEQ:
			rep = "<=";
			break;
		case IC.Parser.sym.GREATERTHAN:
			rep = ">";
			break;
		case IC.Parser.sym.GREATERTHANEQ:
			rep = ">=";
			break;
		case IC.Parser.sym.EQ:
			rep = "==";
			break;
		case IC.Parser.sym.NOTEQ:
			rep = "!=";
			break;
		case IC.Parser.sym.AND:
			rep = "&&";
			break;
		case IC.Parser.sym.OR:
			rep = "||";
			break;
		case IC.Parser.sym.NOT:
			rep = "!";
			break;
		case IC.Parser.sym.ASSIGN:
			rep = "=";
			break;
		default:
			rep = "ERROR";
			break;
		}
		
		return rep;
	
	
	}
	
}
