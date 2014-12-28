package IC.lir.lirAST;

public class StringConcatinetionCall extends LirNode {

	/**
	 * this library call should be available even of the user didn't give us the library to compile with th
	 * program, so I use this class instead of LibraryCallNode
	 */
	
	//this two will save the two labels of the strings to concatenate
	LirNode head;
	LirNode tail;
	
	//return value register
	LirNode destination;
	
	public StringConcatinetionCall(LirNode head,LirNode tail,LirNode destination){
		super();
		this.head = head;
		this.tail = tail;
		this.destination = destination;
	}
	@Override
	public String emit() {
		return "__stringCat("+head.emit()+","+tail.emit()+"),"+destination.emit()+"\n";
	}

}
