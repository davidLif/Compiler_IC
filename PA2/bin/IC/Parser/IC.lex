package IC.Parser;

import java_cup.runtime.*;

%%
%cup
%class Lexer
%line
%column
%scanerror LexicalError
%public


/* 
	----	NOTE:    
	a lexical error will be thrown if a given numeric value is not an integer.
	if a 2^31 is precedent by a minus, it will count as a valid integer.
	
*/

%{

	/* flag that indicates whether the last token is a minus */
	private boolean isUnMinus=false;
	/* 
		return new token of type id with given value
	*/
	private Token getToken(int id, Object value)
	{
		Token interior = new Token(id, value, yyline + 1, yycolumn + 1);
		if(id != sym.MINUS){
			isUnMinus=false;
		}
		else{
			isUnMinus=true;
		}
		return new Token(id, interior, yyline + 1, yycolumn + 1);
	}
	
	/* return new token without value */
	private Token getToken(int id)
	{
		if(id != sym.MINUS){
			isUnMinus=false;
		}
		else{
			isUnMinus=true;
		}
		return new Token(id, yyline+1, yyline + 1, yycolumn + 1);
	}
	
	
	/* this method builds a new lexical error object */
	private LexicalError getLexicalError(String msg)
	{
		return new LexicalError(msg, yyline + 1, yycolumn + 1);
	}

%}

%eofval{
	return getToken(sym.EOF);
%eofval}


/* define Integer literal */
PosInt = [1-9][0-9]*
Integer = ({PosInt} | 0+)

/* define identifiers */
Letter_  = [a-zA-Z_]
ClassID = [A-Z] ({Letter_} | [0-9])*
OtherID   = [a-z] ({Letter_} | [0-9])*

/* define inline comment */
ContentChar = [^\r\n]
LineSeperator = \n | \r | \r\n
// inline comment may end with an EOF 
InlineComment = "//" {ContentChar}* {LineSeperator}?

/* define white spaces */
WhiteSpace = [ \t] | {LineSeperator}

/* define all the characters that can appear in a string
   all the characters between 32 (space) up to 126(~) except double quote "(34) and \ (42)
*/
StringChar = [ !#-Z] | "[" | "]" | "^" |[_-~]
String = \" ({StringChar} | "\\"n | "\\"t | \\\" | \\\\ )* \"


/* state for comment (not inline) parsing */
%state COMMENT


%%
/* process reserved keywords */
<YYINITIAL> "extends" { return getToken(sym.EXTENDS); }
<YYINITIAL> "static"  { return getToken(sym.STATIC); }
<YYINITIAL> "class"   { return getToken(sym.CLASS); }
<YYINITIAL> "void"   { return getToken(sym.VOID); }
<YYINITIAL> "int"   { return getToken(sym.INT); }
<YYINITIAL> "boolean"   { return getToken(sym.BOOL); }
<YYINITIAL> "return"   { return getToken(sym.RETURN); }
<YYINITIAL> "if"   { return getToken(sym.IF); }
<YYINITIAL> "else"   { return getToken(sym.ELSE); }
<YYINITIAL> "while"   { return getToken(sym.WHILE); }
<YYINITIAL> "break"   { return getToken(sym.BREAK); }
<YYINITIAL> "continue"   { return getToken(sym.CONTINUE); }
<YYINITIAL> "this"   { return getToken(sym.THIS); }
<YYINITIAL> "new"   { return getToken(sym.NEW); }
<YYINITIAL> "length"   { return getToken(sym.LENGTH); }
<YYINITIAL> "true"   { return getToken(sym.TRUE); }
<YYINITIAL> "false"   { return getToken(sym.FALSE); }
<YYINITIAL> "null"   { return getToken(sym.NULL); }
<YYINITIAL> "string"   { return getToken(sym.STRING); }

/* process identifies */
<YYINITIAL> {ClassID}  {   return getToken(sym.CLASSID, yytext()); }
<YYINITIAL> {OtherID}  {   return getToken(sym.ID, yytext());         }

/* process integers */
<YYINITIAL> {Integer}  {   
							try {
									// try to parse the number to Integer (signed int has the same range as IC spec)
									Object result=null;
									if(isUnMinus==true){
										/* minus appeared before this token */
										result = (-1) * Integer.parseInt("-" + yytext());
									}
									else{
										result = Integer.parseInt(yytext());
									}
									
									return getToken(sym.INTEGER, result);  
									
							} catch (NumberFormatException e) {
							
								// we know for sure that this numeric expression cannot be an integer
								throw getLexicalError(String.format("%s is not an Integer", yytext()));

							}
							   
						}
						
/* process string literal*/
<YYINITIAL> {String}    {
							
							return getToken(sym.STRING_LITERAL, yytext());
						}
						
/* punctuation */	
<YYINITIAL> "("       { return getToken(sym.LP); }
<YYINITIAL> ")"       { return getToken(sym.RP); }
<YYINITIAL> "}"       { return getToken(sym.CRP); }
<YYINITIAL> "{"       { return getToken(sym.CLP); }
<YYINITIAL> "]"       { return getToken(sym.SQUARERB); }
<YYINITIAL> "["       { return getToken(sym.SQUARELB); }
<YYINITIAL> "."       { return getToken(sym.DOT); }
<YYINITIAL> ";"       { return getToken(sym.SEMCOL); }
<YYINITIAL> ","       { return getToken(sym.COMMA); }

/* operators */
<YYINITIAL> "+"       { return getToken(sym.ADD); }
<YYINITIAL> "-"       { return getToken(sym.MINUS); }
<YYINITIAL> "*"       { return getToken(sym.MULT); }
<YYINITIAL> "/"       { return getToken(sym.DIV); }
<YYINITIAL> "%"       { return getToken(sym.MOD); }
<YYINITIAL> "<"       { return getToken(sym.LESSTHAN); }
<YYINITIAL> "<="       { return getToken(sym.LESSTHANEQ); }
<YYINITIAL> ">"        { return getToken(sym.GREATERTHAN); }
<YYINITIAL> ">="       { return getToken(sym.GREATERTHANEQ); }
<YYINITIAL> "=="       { return getToken(sym.EQ); }
<YYINITIAL> "!="       { return getToken(sym.NOTEQ); }
<YYINITIAL> "&&"       { return getToken(sym.AND); }
<YYINITIAL> "||"       { return getToken(sym.OR); }
<YYINITIAL> "!"       { return getToken(sym.NOT); }
<YYINITIAL> "="       { return getToken(sym.ASSIGN); }


/* process comment (not inline) */
<YYINITIAL> "/*"                 { 
									yybegin(COMMENT); }

/* process inline comment */
<YYINITIAL> {InlineComment}      { /* skip */}
					  
/* process white spaces */
<YYINITIAL> {WhiteSpace}         { /* skip */ }


<COMMENT> {
	"*/"						{ 	/*  comment closed */
									yybegin(YYINITIAL);  } 
	[^]							{   /* ignore  */  }
	<<EOF>>                     {   /* comment was not closed */
									throw getLexicalError("missing */");
								}
}



[^]                             { throw getLexicalError("invalid char: " + yytext()); }
