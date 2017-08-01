package explicit.cex.util;

public class CexPathSetStats extends CexStatistics
{

	private int size;
	private double probOfLastPath;

	public CexPathSetStats(CexParams initialSetup, int size, double prob, double probOfLastPath, long computationTimeInMs)
	{
		this(initialSetup, size, prob, probOfLastPath, FailReason.NONE, computationTimeInMs);
	}

	public CexPathSetStats(CexParams initialSetup, int size, double prob, double probOfLastPath, FailReason reason, long computationTimeInMs)
	{
		super(initialSetup, reason, prob, computationTimeInMs);
		this.size = size;
		this.probOfLastPath = probOfLastPath;
	}

	@Override
	public String toString()
	{
		double threshold = params.getThreshold();
		double remainingProb = threshold - prob;

		StringBuilder result = new StringBuilder();

		result.append(super.toString());

		result.append(": Set of " + size + " paths with total probability " + String.format("%.4f", prob));
		result.append("; remaining prob. " + String.format("%.4f", remainingProb));
		result.append((threshold - prob) > 1e-16 ? " (min. " + (int) (remainingProb / probOfLastPath) + " more path(s))" : "");
		result.append(" [time passed: " + computationTimeInMs + "ms]");
		return result.toString();
	}

}
