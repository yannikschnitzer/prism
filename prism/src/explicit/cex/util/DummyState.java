package explicit.cex.util;

import parser.State;

public class DummyState extends State
{

	private final int uniqueIndex;

	public DummyState(int uniqueIndex)
	{
		super(0);
		this.uniqueIndex = uniqueIndex;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DummyState))
			return false;
		else
			return ((DummyState)o).uniqueIndex == uniqueIndex;
	}

}
