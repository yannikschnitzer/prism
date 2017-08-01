package explicit.cex.cex;

import prism.PrismException;
import cex.Counterexample;
import explicit.cex.util.CexParams;
import explicit.cex.util.CexStatistics;
import explicit.cex.util.CexStatistics.FailReason;
import explicit.cex.util.ValuationSet;

/**
 * The base class for all probabilistic (partial) counterexamples.
 * Defines the common interface with respect to accessing parameters, probability information
 * and the failure reason (if this represents a failed attempt at computing a counterexample). 
 */
public abstract class ProbabilisticCounterexample implements Counterexample
{

	/** Precision to demand when checking whether the probability mass suffices */
	private static final double EPSILON = 1e-16;

	/** Parameters the counterexample computation was based on */
	private final CexParams params;
	
	/** Time taken for computing this counterexample in milliseconds */
	protected long computationTimeInMs = 0;
	
	/** Should exports include states introduced through preprocessing? */
	protected boolean fullExportModeEnabled = false;

	/** When counterexample generation finishes without success, this should be set to a meaningful value */
	protected FailReason failReason = FailReason.NONE;

	public ProbabilisticCounterexample(CexParams params)
	{
		this.params = params;
	}
	
	public CexParams getParams()
	{
		return params;
	}
	
	/**
	 * Configure whether exports include states introduced through preprocessing (default: false).
	 * Note: May be ignored by deriving classes if not applicable.
	 * @param setEnabled
	 */
	public void enableFullExportMode(boolean setEnabled) {
		this.fullExportModeEnabled = setEnabled;
	}
	
	public void setComputationTime(long computationTimeInMs)
	{
		this.computationTimeInMs = computationTimeInMs;
	}

	/**
	 * Sets the given fail reason, representing the reason why the computation was stopped prematurely
	 * @param failReason Reason this counterexample is a failure
	 */
	public void setFailReason(FailReason failReason)
	{
		this.failReason = failReason;
	}

	/**
	 * Returns the probability threshold that this counterexample is supposed to exceed
	 * @return Probability threshold to exceed
	 */
	public double getThreshold()
	{
		return params.getThreshold();
	}

	/**
	 * Returns the total probability mass of this counterexample
	 * @return Total probability mass
	 */
	public abstract double getProbabilityMass();

	/**
	 * Checks whether this (partial) counterexample contains enough probability mass to constitute a complete counterexample. 
	 * @return True iff this contains enough probability mass to be a counterexample 
	 */
	public boolean probabilityMassExceedsThreshold()
	{
		//System.out.println("Checking whether " + getProbabilityMass() + " > " + params.getThreshold());
		return getProbabilityMass() + EPSILON >= params.getThreshold();
	}

	/**
	 * Generates statistics for this counterexample
	 * @return Stats for counterexample
	 */
	public abstract CexStatistics generateStats();

	public abstract String getTypeString();

	/**
	 * Add all variable valuations occurring in the counterexample to the given valuation set 
	 * @param vs Set to add variable valuations to
	 * @throws PrismException
	 */
	public abstract void fillValuationSet(ValuationSet vs) throws PrismException;

}
