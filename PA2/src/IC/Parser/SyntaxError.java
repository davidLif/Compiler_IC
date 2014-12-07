package IC.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class SyntaxError extends Exception {

	/* a syntax error, can be thrown by any parser */
	/* this consturctor also creates an informative error description */
	public SyntaxError(Token foundToken, List<Integer> expected_token_ids)
	{
		
		// set location of syntax error
		this.line = foundToken.getLine();
		this.column = foundToken.getColumn();
		
		StringBuilder sb = new StringBuilder("expected ");
		// get tokens' string representations
		List<String> expected_tokens = getExpectedTokens(expected_token_ids);
		
		// build the error description string
		boolean first = true;
		for(String tok : expected_tokens){
			if(!first)
			{
				sb.append(" or ");
			}
			else
				first = false;
			
			sb.append(String.format("'%s'", tok));
		}
		sb.append(String.format(",  but found '%s'", Token.getTokenRepById(foundToken.sym)));
		
		this.errorDescription = sb.toString();
	}
	
	
	private int line;                 /* location of problematic token */
	private int column; 
	private String errorDescription;  /* this field contains an error description: expected .., but found .. */
	
	
	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public String getErrorDescription() {
		return errorDescription;
	}
	
	public void setErrorDescription(String error)
	{
		this.errorDescription = error;
	}
	
	/*
	 * this method builds a sorted list of the given tokens' visual representations
	 */
	
	private List<String> getExpectedTokens(List<Integer> token_ids)
	{
		
		List<String> result = new ArrayList<String>();
		
		for(int i = 0; i< token_ids.size(); ++i){
			/* skip expected error tokens, not very informative */
			if(token_ids.get(i) == sym.error){
				continue;
			}
			result.add(Token.getTokenRepById( token_ids.get(i) ));
			
		}
		
		// sort the list lexicographically
		Collections.sort(result);
		return result;
		
	}

}
