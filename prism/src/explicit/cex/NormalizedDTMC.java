package explicit.cex;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import parser.State;
import parser.Values;
import prism.ModelType;
import prism.PrismException;
import explicit.DTMC;
import explicit.DTMCExplicit;
import explicit.DTMCMatrix;
import explicit.Distribution;
import explicit.StateValues;
import explicit.TransitionConsumer;
import explicit.cex.util.CexParams;
import explicit.cex.util.DummyState;
import explicit.rewards.MCRewards;

/**
 * Given a DTMC and a set of target states, this class provides access to a modified graph with
 * only a single initial and target state.
 * 
 * This is achieved by adding two new nodes, one initial and one target node,
 * adding transitions with probability 1 from all original target nodes to the new target node,
 * which itself will have a probability one self loop.
 * TODO: We may also need to offer normalization for initial states even if there is just one: Some methods forbid incoming arcs on initial states
 * TODO: Test deadlock methods & model checking methods
 * TODO: We have a lot of code duplication, because we essentially maintain an exact copy of the model-checking methods from DTMCSimple. Should think of a way to avoid that
 * TODO: What about the mv mult methods?
 */
public class NormalizedDTMC extends DTMCExplicit implements DTMC, BidirectionalDTMCWrapper
{
	private static final int UNINITIALIZED = -1;

	/** Underlying original DTMC model */
	private final DTMC dtmc;
	/** Label associated with the target set */
	private final String targetLabel;
	/** Target states of the original reachability probability */
	private final BitSet originalTargets;
	/** Distribution of the initial states to be used */
	private final Distribution initialDistribution;

	/** Number of states of the new DTMC */
	private int numTransitions;

	/** Matrix representation of the normalized DTMC */
	private DTMCMatrix matrix;

	/** The reverse adjacency list for O(1) access to predecessors */
	private ArrayList<ArrayList<Integer>> predecessors;

	/** Have we normalized the initial states or kept the original ones? */
	private final boolean normalizeInitialStates;
	/** The new initial states, if applicable. Remains uninitialized if not applicable. */
	private int initialState = UNINITIALIZED;
	/** List with the unique initial state. Cached for efficiency reasons */
	private final List<Integer> initialStates = new LinkedList<>();
	/** Successors of the unique initial state (i.e. the original initial states.
	 * Cached for efficiency reasons */
	private final List<Integer> initialStateSuccesors = new LinkedList<>();

	/** Have we normalized the target states?
	 * This will be the case iff there is more than one target in the given target bitset */
	private final boolean normalizeTargetStates;
	/** The new unique target state or the old unique target state. This will always be initialized by the constructor */
	private int targetState = UNINITIALIZED;
	/** List with the unique target state. Cached for efficiency reasons */
	private final List<Integer> targetStates = new LinkedList<>();
	/** List with the new successors of the old targets (cached for efficiency reasons) */
	private final List<Integer> newTargetSuccesors = new LinkedList<>();
	/** Successor distribution for the old & new targets (cached for efficiency reasons) */
	private final Distribution targetStateDistribution = new Distribution();

	private List<Integer> deadlocks = new LinkedList<>();
	private int numDeadlocks = 0;

	/**
	 * Finds the state type for the state of the given index
	 * @param s Index of state
	 * @return Corresponding state type
	 */
	public StateType getStateType(int s)
	{
		if (s >= 0 && s < dtmc.getNumStates()) {
			if (normalizeTargetStates && originalTargets.get(s)) {
				return StateType.MODIFIED_OLD_TARGET;
			} else {
				return StateType.ORIGINAL;
			}
		} else if (normalizeTargetStates && s == targetState) {
			return StateType.NEW_TARGET;
		} else if (normalizeInitialStates && s == initialState) {
			return StateType.NEW_INITIAL;
		} else {
			assert (false);
			return StateType.ERROR;
		}
	}

	/**
	 * Creates a normalized DTMC assuming the given distribution over initial states.
	 * Pass null as initial distribution if no normalization of initial states is desired.
	 * @param dtmc Original DTMC model
	 * @param params Params for counterexample generation
	 */
	public NormalizedDTMC(DTMC dtmc, CexParams params)
	{
		super(dtmc.getNumStates());
		this.targetLabel = params.getTargetLabel();
		this.originalTargets = params.getTargetSet();
		
		assert (originalTargets.cardinality() >= 1);
		assert (dtmc.getNumInitialStates() >= 1);

		this.dtmc = dtmc;
		this.initialDistribution = params.hasExplicitInitialState() ? null : params.getInitialDistribution();

		normalizeInitialStates = initialDistribution != null && dtmc.getNumInitialStates() > 1;
		normalizeTargetStates = originalTargets.cardinality() > 1;

		numTransitions = dtmc.getNumTransitions();

		if (normalizeTargetStates) {
			addUniqueTargetState();
		} else {
			targetState = originalTargets.nextSetBit(0);
		}
		targetStates.add(targetState);

		if (normalizeInitialStates) {
			addUniqueInitialState();
		}

		assert (numStates == dtmc.getNumStates() + (normalizeInitialStates ? 1 : 0) + (normalizeTargetStates ? 1 : 0));

		matrix = new DTMCMatrix(this);

		populatePredecessorLists();

		computeDeadlocks();
	}
	
	public DTMCMatrix getTransitionMatrix() {
		return matrix;
	}

	private void computeDeadlocks()
	{
		for (Integer s : dtmc.getDeadlockStates()) {
			// Original targets that used to be deadlocks are no deadlocks anymore,
			// because of their transition to the new target
			if (!originalTargets.get(s)) {
				deadlocks.add(s);
				numDeadlocks++;
			}
		}
	}

	@Override
	public boolean hasNormalizedInitialState()
	{
		return normalizeInitialStates;
	}

	/**
	 * Checks if normalization of target states was performed in this DTMC
	 * @return True iff a new target state was generated
	 */
	public boolean hasNormalizedTargetState()
	{
		return normalizeTargetStates;
	}

	/**
	 * Creates and fills the predecessors list
	 */
	private void populatePredecessorLists()
	{
		predecessors = new ArrayList<>(numStates);
		for (int i = 0; i < numStates; i++) {
			predecessors.add(new ArrayList<Integer>());
		}

		for (int src = 0; src < numStates; src++) {
			Iterator<Integer> succs = getSuccessorsIterator(src);
			while (succs.hasNext()) {
				predecessors.get(succs.next()).add(src);
			}
		}
	}

	/**
	 * Adds a unique target state to the model, increasing the size of state space by 1
	 */
	private void addUniqueTargetState()
	{
		numStates += 1;
		targetState = numStates - 1;

		int numOldTargets = originalTargets.cardinality();

		numTransitions += numOldTargets; // Transitions to new target state
		numTransitions += 1; // Self-loop at target

		// Now we need to subtract all the old transitions from targets
		for (int oldTarget = originalTargets.nextSetBit(0); oldTarget != -1; oldTarget = originalTargets.nextSetBit(oldTarget + 1)) {
			numTransitions -= dtmc.getNumTransitions(oldTarget);
		}

		newTargetSuccesors.add(targetState);
		targetStateDistribution.set(targetState, 1);
	}

	/**
	 * Adds a unique initial state to the model, increasing the size of state space by 1
	 */
	private void addUniqueInitialState()
	{
		numStates += 1;
		initialState = numStates - 1;
		initialStates.add(initialState);

		int numOldInits = dtmc.getNumInitialStates();
		numTransitions += numOldInits; // Transitions from new initial state

		for (int init : dtmc.getInitialStates()) {
			initialStateSuccesors.add(init);
		}
	}

	/**
	 * Returns the unique target state
	 * All Normalized DTMCs must normalize multiple target states into a single one. 
	 * @return The unique target state
	 */
	public int getTargetState()
	{
		return targetState;
	}

	@Override
	public int getFirstCandidateTargetState()
	{
		return targetState;
	}

	@Override
	public boolean isCandidateTargetState(int trg)
	{
		return trg == targetState;
	}


	@Override
	public double getCandidateProbability(int s1, int s2)
	{
		return matrix.getProbOfTransition(s1, s2);
	}

	@Override
	public ModelType getModelType()
	{
		return dtmc.getModelType();
	}

	@Override
	public int getNumStates()
	{

		return numStates;
	}

	@Override
	public int getNumInitialStates()
	{
		return normalizeInitialStates ? 1 : dtmc.getNumInitialStates();
	}

	@Override
	public int getNumCandidateTargetStates()
	{
		return 1;
	}

	@Override
	public Iterable<Integer> getInitialStates()
	{
		assert(!normalizeInitialStates || !initialStates.isEmpty());
		return normalizeInitialStates ? initialStates : dtmc.getInitialStates();
	}

	@Override
	public Iterable<Integer> getCandidateTargetStates() {
		assert(!targetStates.isEmpty());
		return targetStates;
	}

	@Override
	public int getFirstInitialState()
	{
		return normalizeInitialStates ? initialState : dtmc.getFirstInitialState();
	}

	@Override
	public boolean isInitialState(int i)
	{
		return normalizeInitialStates ? (i == initialState) : dtmc.isInitialState(i);
	}

	@Override
	public int getNumDeadlockStates()
	{
		return numDeadlocks;
	}

	@Override
	public Iterable<Integer> getDeadlockStates()
	{
		return deadlocks;
	}

	@Override
	public StateValues getDeadlockStatesList()
	{
		BitSet bs = new BitSet();
		for (int dl : deadlocks) {
			bs.set(dl);
		}

		return StateValues.createFromBitSet(bs, this);
	}

	@Override
	public int getFirstDeadlockState()
	{
		return deadlocks.isEmpty() ? -1 : deadlocks.get(0);
	}

	@Override
	public boolean isDeadlockState(int i)
	{
		return deadlocks.contains(i);
	}

	@Override
	public List<State> getStatesList()
	{
		assert(numStates >= dtmc.getNumStates());
		assert(dtmc.getNumStates() == dtmc.getStatesList().size());
		
		List<State> result = new ArrayList<State>(numStates);
		for (int i = 0; i < dtmc.getStatesList().size(); i++) result.add(null);
		Collections.copy(result, dtmc.getStatesList());
		for (int i = dtmc.getNumStates(); i < numStates; i++) {
			// Pad with dummy states
			result.add(new DummyState(i));
		}
		return result;
	}

	@Override
	public Values getConstantValues()
	{
		return dtmc.getConstantValues();
	}

	@Override
	public BitSet getLabelStates(String name)
	{
		if (name.equals(targetLabel) && normalizeTargetStates) {
			// We now have only a single state with that label, the new target state
			BitSet result = new BitSet();
			result.set(targetState);
			return result;
		} else {
			return dtmc.getLabelStates(name);
		}
	}

	@Override
	public int getNumTransitions()
	{
		return numTransitions;
	}

	@Override
	public Iterator<Integer> getSuccessorsIterator(int s)
	{
		return matrix.getSuccessorsIterator(s);
	}

	@Override
	public Iterator<Integer> getCandidatePredecessorsIterator(int targetNode)
	{
		return predecessors.get(targetNode).iterator();
	}

	@Override
	public boolean isSuccessor(int s1, int s2)
	{
		return matrix.isSuccessor(s1, s2);
	}

	public boolean isPredecessor(int s1, int s2)
	{
		return predecessors.get(s1).contains(s2);
	}

	@Override
	public boolean allSuccessorsInSet(int s, BitSet set)
	{
		return matrix.allSuccessorsInSet(s, set);
	}

	@Override
	public boolean someSuccessorsInSet(int s, BitSet set)
	{
		return matrix.someSuccessorsInSet(s, set);
	}

	public boolean someTransitionsNotInSet(int s, BitSet set) {
		return matrix.someTransitionsNotInSet(s, set);
	}

	@Override
	public void findDeadlocks(boolean fix) throws PrismException
	{
		// We just compute the deadlocks in the original model, since we won't introduce any new deadlocks
		// We may, however, *remove* deadlocks by normalizing target states, but this will be 
		dtmc.findDeadlocks(fix);
	}

	@Override
	public void checkForDeadlocks() throws PrismException
	{
		// Check for deadlocks in all states but the original targets, because those can't be deadlocks anymore
		dtmc.checkForDeadlocks(originalTargets);
	}

	@Override
	public void checkForDeadlocks(BitSet except) throws PrismException
	{
		// Check for deadlocks in all states but the original targets, because those can't be deadlocks anymore
		BitSet except2 = (BitSet) except.clone();
		except2.or(originalTargets);
		dtmc.checkForDeadlocks(except2);
	}

	@Override
	public int getNumTransitions(int s)
	{
		switch (getStateType(s)) {
		case ORIGINAL:
			return dtmc.getNumTransitions(s);
		case MODIFIED_OLD_TARGET:
			//$FALL-THROUGH$
		case NEW_TARGET:
			return 1;
		case NEW_INITIAL:
			dtmc.getNumInitialStates();
		default:
			assert (false);
			return 0;
		}
	}

	@Override
	public Iterator<Entry<Integer, Double>> getTransitionsIterator(int s)
	{
		return matrix.getTransitionsIterator(s);
	}
	@Override
	public void doForEachCandidateTransition(int src, TransitionConsumer f) throws PrismException {
		matrix.doForEachTransition(src, f);
	}

	public int getTransitionId(int s1, int s2) {
		return matrix.getTransitionId(s1, s2);
	}

	public void doForEachTransition(int src, TransitionConsumer f, BitSet except) throws PrismException {
		matrix.doForEachTransition(src, f, except);
	}
	
	public DTMC getOriginalDTMC() {
		return dtmc;
	}

	@Override
	public void buildFromPrismExplicit(String filename) throws PrismException
	{
		// We don't need this
		throw new PrismException("Not supported in wrapper class");
	}

	@Override
	public Distribution getTransitions(int s)
	{
		switch (getStateType(s)) {
		case ORIGINAL:
			return dtmc.getTransitions(s);
		case MODIFIED_OLD_TARGET:
			return targetStateDistribution;
		case NEW_TARGET:
			return targetStateDistribution;
		case NEW_INITIAL:
			return initialDistribution;
		default:
			assert (false);
			return null;
		}
	}

	@Override
	public void prob0step(BitSet subset, BitSet u, BitSet result)
	{
		int i;
		Distribution distr;
		for (i = 0; i < numStates; i++) {
			if (subset.get(i)) {
				distr = getTransitions(i);
				result.set(i, distr.containsOneOf(u));
			}
		}
	}

	@Override
	public void prob1step(BitSet subset, BitSet u, BitSet v, BitSet result)
	{
		int i;
		Distribution distr;
		for (i = 0; i < numStates; i++) {
			if (subset.get(i)) {
				distr = getTransitions(i);
				result.set(i, distr.containsOneOf(v) && distr.isSubsetOf(u));
			}
		}
	}

	@Override
	public double mvMultSingle(int s, double vect[])
	{
		int k;
		double d, prob;
		Distribution distr;

		distr = getTransitions(s);
		d = 0.0;
		for (Map.Entry<Integer, Double> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue();
			d += prob * vect[k];
		}

		return d;
	}

	@Override
	public double mvMultJacSingle(int s, double vect[])
	{
		int k;
		double diag, d, prob;
		Distribution distr;

		distr = getTransitions(s);
		diag = 1.0;
		d = 0.0;
		for (Map.Entry<Integer, Double> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue();
			if (k != s) {
				d += prob * vect[k];
			} else {
				diag -= prob;
			}
		}
		if (diag > 0)
			d /= diag;

		return d;
	}

	@Override
	public double mvMultRewSingle(int s, double vect[], MCRewards mcRewards)
	{
		int k;
		double d, prob;
		Distribution distr;

		distr = getTransitions(s);
		d = mcRewards.getStateReward(s);
		for (Map.Entry<Integer, Double> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue();
			d += prob * vect[k];
		}

		return d;
	}

	@Override
	public void vmMult(double vect[], double result[])
	{
		int i, j;
		double prob;
		Distribution distr;

		// Initialise result to 0
		for (j = 0; j < numStates; j++) {
			result[j] = 0;
		}
		// Go through matrix elements (by row)
		for (i = 0; i < numStates; i++) {
			distr = getTransitions(i);
			for (Map.Entry<Integer, Double> e : distr) {
				j = (Integer) e.getKey();
				prob = (Double) e.getValue();
				result[j] += prob * vect[i];
			}

		}
	}

	@Override
	public int getMaxPossibleStateIndex()
	{
		return numStates - 1;
	}

	@Override
	public int getNumCandidateInitialStates()
	{
		return getNumInitialStates();
	}

	@Override
	public Iterable<Integer> getCandidateInitialStates()
	{
		return getInitialStates();
	}

	@Override
	public int getFirstCandidateInitialState()
	{
		return getFirstInitialState();
	}

	@Override
	public boolean isCandidateInitialState(int i)
	{
		return isInitialState(i);
	}

	public Entry<Integer, Double> getEntry(int src, int trg)
	{
		return getTransitions(src).getEntry(trg);
	}

	@Override
	public String toString() {

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < getNumStates(); i++) {
			Distribution distr = getTransitions(i);

			result.append(i + " to ");

			Iterator<Entry<Integer, Double>> it = distr.iterator();
			while (it.hasNext()) {
				Entry<Integer, Double> entry = it.next();
				result.append(entry.getKey() + " : " + entry.getValue());
				if (it.hasNext())
					result.append(", ");
			}

			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}

}
