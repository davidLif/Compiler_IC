package IC.lir.lirAST;

public enum lirUnaryOp {

	INC, DEC, NEG, NOT;
	
	
	/**
	 * get string representation of this enum instance
	 * @return
	 */
	
	public String getRepresentation()
	{
		switch(this)
		{
		case INC:
			return "Inc";
		case DEC:
			return "Dec";
		case NEG:
			return "Neg";
		case NOT:
			return "Not";
		
		default:
			return null;
		
		}
	}
}
