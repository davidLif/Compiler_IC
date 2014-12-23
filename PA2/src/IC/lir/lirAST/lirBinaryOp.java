package IC.lir.lirAST;

public enum lirBinaryOp {

	ADD, SUB, MUL, DIV, MOD, /* math */
	AND, OR, XOR, COMPARE;   /* logical */
	
	public boolean isMathematical()
	{
		return this == ADD || this == SUB || this == MUL || this == DIV || this == MOD;
		
	}
	
	public boolean isLogical()
	{
		return !this.isMathematical();
	}
	
	/**
	 * get string representation of this enum instance
	 * @return
	 */
	
	public String getRepresentation()
	{
		switch(this)
		{
		case ADD:
			return "Add";
		case SUB:
			return "Sub";
		case MUL:
			return "Mul";
		case DIV:
			return "Div";
		case AND:
			return "And";
		case OR:
			return "Or";
		case XOR:
			return "Xor";
		case COMPARE:
			return "Compare";
		case MOD:
			return "Mod";
		default:
			return null;
		
		}
	}
	
}
