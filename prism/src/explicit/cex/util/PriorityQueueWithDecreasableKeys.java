package explicit.cex.util;

import java.util.PriorityQueue;

/**
 * Wrapper for java.util.PriorityQueue which suppports a decreaseKey operation.
 *
 * @param <E> Element type
 */
public class PriorityQueueWithDecreasableKeys<E>
{

	private PriorityQueue<E> queue = new PriorityQueue<>();

	public PriorityQueueWithDecreasableKeys()
	{

	}

	public void add(E elem)
	{
		assert (!queue.contains(elem));
		queue.add(elem);
	}

	public void decreaseKey(E elem)
	{
		queue.remove(elem);
		queue.add(elem);
	}

	public E poll()
	{
		return queue.poll();
	}

	public int size()
	{
		return queue.size();
	}

	public boolean isEmpty()
	{
		return queue.isEmpty();
	}

}
