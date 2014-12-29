package IC.lir.lirAST;

import java.util.ArrayList;
import java.util.List;

public class StringConcatinetionCall extends LibraryCallNode {

	/**
	 * this library call should be available even of the user didn't give us the library to compile with th
	 * program, so I use this class instead of LibraryCallNode
	 */
	
	//this two will save the two labels of the strings to concatenate
	LirNode head;
	LirNode tail;
	
	//return value register
	Reg destination;
	
	public StringConcatinetionCall(LirNode head,LirNode tail,Reg destination){
		super(new Label ("__stringCat"),new ArrayList<LirNode>(), destination);
		this.params.add(head);
		this.params.add(tail);
		this.head = head;
		this.tail = tail;
		this.destination = destination;
	}
	@Override
	public String emit() {
		return "__stringCat("+head.emit()+","+tail.emit()+"),"+destination.emit()+"\n";
	}

}
