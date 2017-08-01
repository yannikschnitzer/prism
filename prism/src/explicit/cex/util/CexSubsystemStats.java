package explicit.cex.util;

public class CexSubsystemStats extends CexStatistics
{

	private int numStates;
	private int numTransitions;
	private int numPathFragments;

	public CexSubsystemStats(CexParams initialSetup, int numStates, int numTransitions, int numPathFragments, double prob, long computationTimeInMs) {
		this(initialSetup, numStates, numTransitions, numPathFragments, prob, FailReason.NONE, computationTimeInMs);
	}
	
	public CexSubsystemStats(CexParams initialSetup, int numStates, int numTransitions, int numPathFragments, double prob, FailReason failReason, long computationTimeInMs)
	{
		super(initialSetup, failReason, prob, computationTimeInMs);
		this.numStates = numStates;
		this.numTransitions = numTransitions;
		this.numPathFragments = numPathFragments;
	}
	
	@Override
	public String toString()
	{
		double threshold = params.getThreshold();
		double remainingProb = threshold - prob;

		StringBuilder result = new StringBuilder();
		result.append(super.toString());

		result.append(": Subsystem of " + numStates + " states / " + numTransitions + " t's ");
		if (numPathFragments > 0) result.append("(" + numPathFragments + " path fragments)");
		result.append(" with closure probability " + String.format("%.4f", prob));
		result.append("; remaining prob. " + String.format("%.4f", remainingProb));
		result.append(" [time passed: " + computationTimeInMs + "ms]");
		return result.toString();
	}

}
