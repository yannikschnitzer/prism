package explicit.cex.util;

import prism.PrismLog;

public abstract class CexStatistics
{

	public enum FailReason {
		NONE, TIME_OUT, NO_MORE_PATHS, NOT_STARTED, EXECUTION_ERROR
	}

	protected final CexParams params;
	protected final FailReason failReason;
	protected final double prob;
	protected final long computationTimeInMs;

	public CexStatistics(CexParams initialSetup, FailReason failReason, double prob, long computationTimeInMs)
	{
		this.failReason = failReason;
		this.params = initialSetup;
		this.prob = prob;
		this.computationTimeInMs = computationTimeInMs;
	}

	public void print(PrismLog log)
	{
		log.println(toString());
	}
	
	@Override
	public String toString() {
		switch (failReason) {
		case NONE:
			return (params.getThreshold() - prob) <= 1e-16 ? "COMPLETE COUNTEREXAMPLE" : "PARTIAL COUNTEREXAMPLE";
		case TIME_OUT:
			return "TIMEOUT AFTER";
		case NO_MORE_PATHS:
			return "ALL PATHS (NO C'EX EXISTS)";
		case NOT_STARTED:
			return "";
		case EXECUTION_ERROR:
			return "ERROR AFTER";
		default:
			assert(false);
			return "";
		}
	}

}
