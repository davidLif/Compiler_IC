package IC.Types;
import java.util.ArrayList;
import java.util.List;

public class MethodType extends Type{

	
	private List<Type> arguments;
	private Type returnType;
	
	/**
	 * 
	 * @param args - list of types (method argument types)
	 * @param returnType - return type of method
	 */
	
	public MethodType(List<Type> args, Type returnType)
	{
		this.arguments = args;
		this.returnType = returnType;
	}
	
	/**
	 * this constructor is used when there are no arguments
	 * @param returnType
	 */
	public MethodType(Type returnType)
	{
		this.arguments = new ArrayList<Type>();
		this.returnType = returnType;
	}
	
	@Override
	protected String getTypeRep() {
		return "Method type: " + this.toString();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(Type arg : arguments)
		{
			sb.append(arg.toString());
		}
		sb.append(" -> " + returnType.toString());
		return sb.toString();
	}

	public boolean formals_compare(List<Type> to_compare){
		if (to_compare.size() != arguments.size()){
			return false;
		}
		for (int i=0;i< arguments.size();i++){
			if (!type_compare(to_compare.get(i),arguments.get(i))) return false;
		}
		return true;
	}

	@Override
	public boolean subTypeOf(Type t) {
		
		
		//TODO maybe needs a change
		return this == t;
	}

}
