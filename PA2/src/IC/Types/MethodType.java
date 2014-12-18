package IC.Types;
import java.util.ArrayList;
import java.util.Comparator;
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
		return this.getTableId()+": Method type: " + this.toString();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		for(int i = 0; i < arguments.size(); ++i)
		{
			
			sb.append(arguments.get(i).toString());
			
			if(i < arguments.size() - 1)
			{
				// not last
				sb.append(", ");
				
		}
		}
		
		sb.append(" -> " + returnType.toString());
		sb.append("}");
		return sb.toString();
	}

	/**
	 * method returns true iff the list of formals to_compare is the same
	 * @param to_compare
	 * @return
	 */

	public boolean formals_compare(List<Type> to_compare){
		if (to_compare.size() != arguments.size()){
			return false;
		}
		for (int i=0;i< arguments.size();i++){
			if (!type_compare(to_compare.get(i),arguments.get(i))) return false;
		}
		return true;
	}

	
	/**
	 * this method type is a subtype of t if and only if the following holds:
	 * 		[0. t is a MethodType]
	 * 		1. same number of arguments
	 *      2. exactly same type of arguments
	 *      3. return type is subtype of t's return type
	 *      
	 * 
	 */
	
	@Override
	public boolean subTypeOf(Type t) {
		
		if(t == this)
			return true;
		
		if(!(t instanceof MethodType))
			return false;
		
		MethodType other = (MethodType)t; 
		
		
		/* return type check */
		if(!this.returnType.subTypeOf(other.getReturnType()))
			return false;
		
		/* formals check */
		if(!this.formals_compare(other.arguments))
				return false;
	
	
		return true;
	}
	
	public Type getReturnType(){
		return returnType;
	}
	
	public List<Type> getArgstypes(){
		return arguments;
	}

	public int compareTo(MethodType comparemethod) {
		return this.getTableId() - comparemethod.getTableId();
}
	
	public static Comparator<MethodType> Comparator_methods  = new Comparator<MethodType>() {

		public int compare(MethodType meth_1, MethodType meth_2) {
			return meth_1.compareTo(meth_2);
		}
	};
}


