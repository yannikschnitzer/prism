package parser.type;

import java.util.HashMap;
import java.util.Map;

public class TypeAssignment extends Type
{
	private static Map<Type, TypeAssignment> singletons;
	
	static
	{
		singletons = new HashMap<Type, TypeAssignment>();
	}
	
	private Type subType;
	
	public TypeAssignment(Type subType)
	{
		this.subType = subType;
	}

	public Type getSubType() 
	{
		return subType;
	}

	public void setSubType(Type subType) 
	{
		this.subType = subType;
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof TypeAssignment)
		{
			TypeAssignment oa = (TypeAssignment)o;
			return (subType.equals(oa.getSubType()));
		}
		
		return false;
	}
	
	@Override
	public String getTypeString()
	{
		return "assignment to " + subType.getTypeString();
	}
	
	public static TypeAssignment getInstance(Type subType)
	{
		if (!singletons.containsKey(subType))
			singletons.put(subType, new TypeAssignment(subType));
			
		return singletons.get(subType);
	}
}
