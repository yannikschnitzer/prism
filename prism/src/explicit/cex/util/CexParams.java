package explicit.cex.util;

import java.util.BitSet;

import explicit.DTMC;
import explicit.Distribution;
import explicit.Model;
import explicit.cex.NormalizedDTMC;

/**
 * Represents the set of parameters that guides counterexample computation for a P <= p (F<=s l),
 * i.e. where to search from, where to search to (states satisfying l),
 * which step bound to respect (s, optional), and which threshold to exceed (p).
 * Note that we do not support the full generality of the until operator here,
 * since we can always reduce properties to this form via DTMC transformations available through
 * {@link explicit.DTMCTransformationsBuilder}.
 */
public class CexParams
{
	/** Constant indicating that no initial state has been provided */
	public static final int UNINITIALIZED_STATE = -1;
	/** Constant indicating that no step bounds are desired */
	public static final int UNBOUNDED = Integer.MAX_VALUE;

	/** Probability threshold to exceed -- we only consider upper bounds for the reachability properties */
	private final double threshold;

	/** Maximum number of steps in each path or {@link #UNBOUNDED} */
	private final int stepBound;
	
	/** Unique initial state for the counterexample search, if any */
	private int initialState = UNINITIALIZED_STATE;
	/** Probability distribution over the initial states, if there is no unique state */
	private Distribution initialDistribution = null;

	/** Label of the target set */
	private final String targetLabel;
	/** Bitset indicating which states are targets */
	private final BitSet targetSet;
	
	@Override
	public String toString() {
		return "threshold=" + threshold + ", initial=" + (initialState != UNINITIALIZED_STATE ? initialState : initialDistribution.toString());
	}

	private CexParams(double threshold, int stepBound, Distribution initialDistribution, int initialState, String targetLabel, BitSet targetSet) {
		this.threshold = threshold;
		this.stepBound = stepBound;
		this.initialDistribution = initialDistribution;
		this.initialState = initialState;
		this.targetLabel = targetLabel;
		this.targetSet = targetSet;
	}
	
	public CexParams(double threshold, int stepBound, Distribution initialDistribution, String targetLabel, BitSet targetSet)
	{
		this(threshold, stepBound, initialDistribution, UNINITIALIZED_STATE, targetLabel, targetSet);
	}

	public CexParams(double threshold, int stepBound, int initialState, String targetLabel, BitSet targetSet)
	{
		this(threshold, stepBound, null, initialState, targetLabel, targetSet);
	}
	
	public CexParams(double threshold, int stepBound, Model model, String targetLabel, BitSet targetSet)
	{
		this(threshold, stepBound, model.getNumInitialStates() > 1 ? makeUniformDistribution(model) : null, model.getNumInitialStates() == 1 ? model.getFirstInitialState() : UNINITIALIZED_STATE, targetLabel, targetSet);
	}
	
	public double getThreshold()
	{
		return threshold;
	}
	
	public boolean isBounded() {
		return stepBound != UNBOUNDED;
	}
	
	public double getStepBound() {
		return stepBound;
	}

	public String getTargetLabel()
	{
		return targetLabel;
	}

	public BitSet getTargetSet()
	{
		return targetSet;
	}
	
	/**
	 * Returns unique initial state, if it has been set.
	 * Should only be called if {@link #hasExplicitInitialState()} returns true.
	 * @return Index of unique initial state as set during construction
	 */
	public int getInitialState()
	{
		assert (initialState != UNINITIALIZED_STATE);
		return initialState;
	}

	/**
	 * Returns distribution over initial states, if it has been set.
	 * Should only be called if {@link #hasInitialDistribution()} returns true.
	 * @return Distribution over initial state as set during construction
	 */
	public Distribution getInitialDistribution()
	{
		assert (initialDistribution != null);
		return initialDistribution;
	}

	/**
	 * Is there a unique initial state that was explicitly provided, rather than a probability distribution over initial states?
	 * @return True iff an initial state was explicitly set during construction.
	 */
	public boolean hasExplicitInitialState()
	{
		return initialState != UNINITIALIZED_STATE;
	}

	/**
	 * Was there a non-null initial distriubution provided during construction_
	 * @return True iff a non-null distributoin was explicitly set during construction.
	 */
	public boolean hasInitialDistribution()
	{
		return initialDistribution != null;
	}

	/**
	 * Returns a uniform distribution over the initial states of the given model
	 * @param model Model with >=1 initial state(s)
	 * @return Uniform distribution over initial states
	 */
	public static Distribution makeUniformDistribution(Model model)
	{
		int numInits = model.getNumInitialStates();
		Distribution initialStateDistribution = new Distribution();
		for (int init : model.getInitialStates()) {
			initialStateDistribution.set(init, 1d / numInits);
		}
		return initialStateDistribution;
	}
	
	/**
	 * Returns a sensible default initial state for the given combination of parameters and model.
	 */
	public static int getNormalizedInitialState(CexParams params, NormalizedDTMC dtmc) {
		assert (params.hasExplicitInitialState() || dtmc.getNumInitialStates() == 1);
		return params.hasExplicitInitialState() ? params.getInitialState() : dtmc.getFirstInitialState();
	}

	/**
	 * Creates a parameter object for the given model and unbounded properties.
	 * Note that the result will have a probability threshold of 0, so this is useful only for testing,
	 * not for setting up actual counterexample computation.
	 */
	public static CexParams makeFromDTMCAndTargets(DTMC dtmc, String targetLabel, BitSet targets)
	{
		return makeFromDTMCAndTargets(dtmc, targetLabel, targets, UNBOUNDED);
	}

	/**
	 * Creates a parameter object for the given model and bounded properties with step bound of h.
	 * Note that the result will have a probability threshold of 0, so this is useful only for testing,
	 * not for setting up actual counterexample computation.
	 */
	public static CexParams makeFromDTMCAndTargets(DTMC dtmc, String targetLabel, BitSet targets, int h)
	{
		if (dtmc.getNumInitialStates() == 1)
			return new CexParams(0d, h, dtmc.getFirstInitialState(), targetLabel, targets);
		else
			return new CexParams(0d, h, makeUniformDistribution(dtmc), targetLabel, targets);
	}

}
