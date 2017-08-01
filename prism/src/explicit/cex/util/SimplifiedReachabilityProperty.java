package explicit.cex.util;

public class SimplifiedReachabilityProperty
{

	public double upperProbBound;
	public boolean normalizeProb;
	public boolean expectCounterexample;

	public SimplifiedReachabilityProperty(double upperProbBound, boolean normalizeProb, boolean expectCounterexample)
	{
		this.upperProbBound = upperProbBound;
		this.normalizeProb = normalizeProb;
		this.expectCounterexample = expectCounterexample;
	}

}
