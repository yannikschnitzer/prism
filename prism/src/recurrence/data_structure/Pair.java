package recurrence.data_structure;

/**
 * 	This class stores the values as key-value pairs
 * 	@class Decimal	
 */
public class Pair<P, Q>
{
	P firstItem;
	Q secondItem;

	/**
	 * Constructor that constructs the pair of first and second item
	 * @param firstItem
	 * @param secondItem
	 */
	public Pair(P firstItem, Q secondItem)
	{
		this.firstItem = firstItem;
		this.secondItem = secondItem;
	}

	/**
	 * @return the first item of the pair
	 */
	public P first()
	{
		return this.firstItem;
	}

	/**
	 * @param firstItem first item of the pair
	 */
	public void setFirst(P firstItem)
	{
		this.firstItem = firstItem;
	}

	/**
	 * @return the second item of the pair
	 */
	public Q second()
	{
		return secondItem;
	}

	/**
	 * @param secondItem second item of the pair
	 */
	public void setSecond(Q secondItem)
	{
		this.secondItem = secondItem;
	}
}
