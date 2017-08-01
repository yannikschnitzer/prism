package explicit.cex.util;

import java.util.ListIterator;

import explicit.cex.cex.ProbabilisticPath;

public interface ProbablePathIterator extends ListIterator<ProbabilisticPath>
{

	/**
	 * Reset to the first element of the iterator
	 */
	public void reset();

}
