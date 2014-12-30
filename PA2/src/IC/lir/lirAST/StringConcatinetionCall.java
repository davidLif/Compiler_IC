package IC.lir.lirAST;

import java.util.ArrayList;
import java.util.List;

public class StringConcatinetionCall extends LibraryCallNode {

	/**
	 * this library call should be available even of the user didn't give us the library to compile with the
	 * program, so I use this class instead of LibraryCallNode
	 */
	
	//this two will save the two labels of the strings to concatenate
	protected LirNode head;
	protected LirNode tail;
	
	//return value register
	protected Reg destination;
	
	// actual method label
	protected Label methodLabel;
	
	public StringConcatinetionCall(Label callLabel, LirNode head, LirNode tail,Reg destination){
		super(callLabel,new ArrayList<LirNode>(), destination);
		this.params.add(head);
		this.params.add(tail);
		this.head = head;
		this.tail = tail;
		this.methodLabel = callLabel;
		this.destination = destination;
	}
	

}
