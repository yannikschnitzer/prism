package recurrence.data_structure;

public class Pair<P, Q>
{
	P firstItem;
	Q secondItem;

	public Pair(P firstItem, Q secondItem)
	{
		this.firstItem = firstItem;
		this.secondItem = secondItem;
	}

	public P first()
	{
		return this.firstItem;
	}

	public void setFirst(P firstItem)
	{
		this.firstItem = firstItem;
	}

	public Q second()
	{
		return secondItem;
	}

	public void setSecond(Q secondItem)
	{
		this.secondItem = secondItem;
	}
}
