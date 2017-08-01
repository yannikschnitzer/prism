package explicit.cex.util;

/**
 * This class is a container for a single value shared between multiple threads.
 * 
 * By offering a method to check whether the value has changed since the last read access,
 * this class is especially suitable for applications where one thread regularly polls for
 * updated values from one or more other threads.
 * Since there is no buffer, this class is only suitable if missing an update is non-critical.
 *
 * @param <T> Type of data to hold
 */
public class SharedMutableVar<T>
{

	private T val;
	private boolean holdsNewData;

	/**
	 * Creates a new var with the given value.
	 * If null is passed, {@link #holdsNewData} will return false until the first non-null value is {@link #set(Object)}
	 * @param val Initial value or null
	 */
	public SharedMutableVar(T val)
	{
		this.val = val;
		holdsNewData = val != null;
	}

	/**
	 * Checks if the value of this var has changed since the last read ({@link #get}) access
	 * @return True iff value has changed
	 */
	public boolean holdsNewData()
	{
		return holdsNewData;
	}

	/**
	 * Gets the current value out of this var
	 * @return Current value
	 */
	public synchronized T get()
	{
		holdsNewData = false;
		return val;
	}

	/**
	 * Replace
	 * @param val
	 */
	public synchronized void set(T val)
	{
		this.val = val;
		holdsNewData = true;
	}

}
