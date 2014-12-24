package IC.lir.lirAST;

import java.util.List;



public class DispatchTableNode extends LirNode{

	
	/**
	 * list of class labels
	 */
	private List<Label> entries;
	
	/**
	 * DV label of given class
	 */
	private Label classLabel; 
	
	
	public DispatchTableNode(Label classLabel, List<Label> entries)
	{
		this.entries = entries;
		this.classLabel = classLabel;
	}
	
	@Override
	public String emit() {
		
		StringBuilder sb = new StringBuilder(classLabel + ":");
		
		if(this.entries.size() > 0)
		{
			sb.append(" [");
			boolean first = true;
			for(Label entry : entries)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append(entry);
			}
			sb.append("]");
		}
		return sb.toString();

		
	}

}
