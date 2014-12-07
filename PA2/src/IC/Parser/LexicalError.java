package IC.Parser;

@SuppressWarnings("serial")
public class LexicalError extends Exception{
	
	/* location of lexical error */
	private int line;
	private int column;
	private boolean token_error = false;
	
	public LexicalError(String errorDescription, int line, int column){
		
		super(errorDescription);
		this.line = line;
		this.column = column;
		this.token_error = true;
	}
	
	// default constructor
	public LexicalError(String message)
	{
		super(message);
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
	
	public String getErrorDescription()
	{
		return this.getMessage();
	}
	
	public boolean isTokenError()
	{
		return this.token_error;
	}
}

