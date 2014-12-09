package IC.SemanticChecks;

@SuppressWarnings("serial")
public class SemanticError extends Exception {

	private int line;
	
	/**
	 * constructs a new semantic error object
	 * @param line - line at which the error occurred
	 * @param msg -  informative message describing the error
	 */
	
	public SemanticError(int line, String msg)
	{
		super(msg);
		this.line = line;
	}
	
	public String getMessage()
	{
		return String.format("semantic error at line %d: %s", this.line, super.getMessage());
	}
	
}
