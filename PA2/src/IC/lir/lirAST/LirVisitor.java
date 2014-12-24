package IC.lir.lirAST;



/**
 * 
 * 
 * @author Denis
 *
 *
 *	represents a visitor that visits all the Lir AST 
 *
 *	the entry point is the LirProgram node
 *
 */

public interface LirVisitor {


	public Object visit(LirProgram program);

	public Object visit(ArrayLengthNode arrayLength) ;

	public Object visit(BinaryInstructionNode binaryInstruct) ;

	public Object visit(Immediate imm) ;

	public Object visit(JumpFalse jump) ;

	public Object visit(JumpG jump) ;

	public Object visit(JumpGE jump) ;

	public Object visit(JumpL jump) ;

	public Object visit(JumpLE jump) ;

	public Object visit(JumpNode jump) ;

	public Object visit(JumpTrueNode jump) ;

	public Object visit(Label label ) ;

	public Object visit(LibraryCallNode libCall) ;

	public Object visit(LoadArrayNode loadArray) ;

	public Object visit(LoadField loadField) ;

	public Object visit(Memory mem) ;

	public Object visit(MethodNode statementsBlock) ;

	public Object visit(MoveNode move) ;

	public Object visit(Reg reg) ;

	public Object visit(ReturnNode ret) ;

	public Object visit(StaticCallNode call) ;

	public Object visit(StoreArrayNode storeArray) ;

	public Object visit(StoreField storeField) ;

	public Object visit(StringLiteralNode stringLiteral) ;

	public Object visit(UnaryInstructionNode instruction) ;

	public Object visit(VirtualCallNode call) ;

	
	
	
}
