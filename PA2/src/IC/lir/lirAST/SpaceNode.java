package IC.lir.lirAST;

/**
 * build for one purpose - printing space between functions
 */
public class SpaceNode extends LirNode {

	@Override
	public String emit() {
		return "\n";
	}

}
