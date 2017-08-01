package explicit.cex.util;

public class PathEntry implements Comparable<PathEntry>
{

	public int targetState;
	public int predState;
	public PathEntry predEntry;
	public double totalPathProb;
	public int k;
	public int stepsFromSource;

	public PathEntry(int state, int pred, PathEntry predEntry, double prob, int k, int distanceFromSource)
	{
		this.targetState = state;
		this.predState = pred;
		this.predEntry = predEntry;
		this.totalPathProb = prob;
		this.k = k;
		this.stepsFromSource = distanceFromSource;
	}

	@Override
	public int compareTo(PathEntry arg0)
	{
		// We want to extract entries with the highest probability first, so we return the negated result of comparting the doubles  
		return -(new Double(totalPathProb).compareTo(arg0.totalPathProb));
	}

	@Override
	public boolean equals(Object o)
	{
		// We regard two path entries as equal if they belong to the same state
		// This way the {@link decreaseKey} operation of the priority queue is sure to find the correct entry 
		if (o instanceof PathEntry) {
			return ((PathEntry) o).targetState == targetState;
		} else {
			return false;
		}
	}

	@Override
	public String toString()
	{
		return predState + " -[" + totalPathProb + "]-> " + targetState 
				+ " (k=" + k + (stepsFromSource != CexParams.UNBOUNDED ? ", h=" + stepsFromSource : "") + ")";
	}
}
