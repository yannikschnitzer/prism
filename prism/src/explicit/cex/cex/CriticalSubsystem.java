package explicit.cex.cex;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import parser.State;
import parser.Values;
import parser.VarList;
import prism.ModelType;
import prism.Pair;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLog;
import explicit.DTMC;
import explicit.DTMCMatrix;
import explicit.DTMCModelChecker;
import explicit.Distribution;
import explicit.Model;
import explicit.ModelCheckerResult;
import explicit.PredecessorRelation;
import explicit.StateValues;
import explicit.TransitionConsumer;
import explicit.cex.BidirectionalDTMCWrapper;
import explicit.cex.NormalizedDTMC;
import explicit.cex.gens.ShortestPathFinder;
import explicit.cex.util.CexParams;
import explicit.cex.util.CexStatistics;
import explicit.cex.util.CexSubsystemStats;
import explicit.cex.util.PathEntry;
import explicit.cex.util.Transition;
import explicit.cex.util.ValuationSet;
import explicit.exporters.ExportType;
import explicit.exporters.ModelExporter;
import explicit.exporters.StateExporter;
import explicit.rewards.MCRewards;

/**
 * A critical subsystem implementation for the local search. (The latter is relevant, see methods for initial and target states)
 * 
 * TODO: This is one of the first times I miss multiple inheritance in Java. It would be nice to just derive this from NormalizedDTMC -- that'd avoid a whole lot of code duplication. Maybe I should turn ProbabilisticCounterexample into an interface
 * TODO: This does not appear to work with tight probability bounds, although it should
 */
public class CriticalSubsystem extends ProbabilisticCounterexample implements BidirectionalDTMCWrapper, DTMC
{

	/** The dtmc that this is a subsystem of */
	private final NormalizedDTMC dtmc;
	/** Transition matrix of the dtmc for direct access */
	private final DTMCMatrix matrix;
	/** The states already present in the subsystem */
	private final BitSet statesInSubsystem = new BitSet();
	private final TreeSet<Integer> explicitStatesInSubsystem = new TreeSet<>();
	
	/** Candidate nodes for Dijkstra's algorithm, i.e. those states in the subsystem which still have outgoing candidate transitions */
	private final BitSet dijkstraCandidates = new BitSet();
	private final TreeSet<Integer> explicitDijkstraCandidates = new TreeSet<>();

	private final int numStates;

	private int numTransitions = 0;
	private int numPathFragments = 0;

	private final DTMCMatrix reverseMatrix;
	private final BitSet processedTrans = new BitSet();
	private final BitSet unprocessedTrans = new BitSet();

	/** Field for caching the probability mass of the subsystem, as determined by model-checking */
	private double probMass = 0;
	private boolean probMassOutdated = false;
	private boolean closureRecomputationTriggered = true; // Initially compute at least once
	private double[] reachabilityProbs = null;

	private final List<Integer> deadlocks = new LinkedList<>();
	private boolean deadlocksOutdated = true;

	// Some cached objects to avoid frequent recomputation (to be filled by constructor)
	private final List<Integer> normalizedInitialStates = new LinkedList<>();
	private final BitSet normalizedTargetStates = new BitSet();
	private final int dummySinkState;

	private final PrismLog log;

	private CriticalSubsystem(CexParams params, NormalizedDTMC dtmc, PrismLog log) {
		super(params);
		this.dtmc = dtmc;
		this.matrix = dtmc.getTransitionMatrix();
		this.log = log;
		
		numStates = dtmc.getNumStates() + 1; // Plus sink
		dummySinkState = dtmc.getNumStates();

		assert (dtmc.getNumInitialStates() == 1);
		normalizedInitialStates.add(CexParams.getNormalizedInitialState(getParams(), dtmc));
		normalizedTargetStates.set(dtmc.getTargetState());
		reverseMatrix = new DTMCMatrix(dtmc, true);
		unprocessedTrans.set(0, dtmc.getNumTransitions());

		updateDeadlocks();
	}
	
	public CriticalSubsystem(CexParams params, NormalizedDTMC dtmc, PrismLog log, ProbabilisticPath firstPath)
	{
		this(params, dtmc, log);
		addPathFragment(firstPath);
	}

	public CriticalSubsystem(CexParams params, NormalizedDTMC dtmc, PrismLog log, ArrayList<Integer> statesInSubsystem)
	{
		this(params, dtmc, log);
		for (int s : statesInSubsystem)
			addState(s);

		probMassOutdated = true;
	}

	public void triggerClosureRecomputation()
	{
		this.closureRecomputationTriggered = true;
	}

	private void updateDeadlocks()
	{
		// Filter out states not in the model
		for (int dl : dtmc.getDeadlockStates()) {
			if (statesInSubsystem.get(dl))
				deadlocks.add(dl);
		}
		deadlocksOutdated = false;
	}

	public void addPathFragment(ProbabilisticPath path)
	{
		if (explicitStatesInSubsystem.isEmpty()) {
			// This is the very first path, so we need to add the initial state to the model as well
			addState(path.getFirst().getSource());
		}

		// All transitions on the path fragment are required to be new. Thus we can just set the corresponding value in the distribution
		Iterator<Transition> it = path.getTransitionIterator();
		while (it.hasNext()) {
			Transition t = it.next();
			log.println("Adding new transition " + t, PrismLog.VL_ALL);

			// Set probability for new transition (in both directions)
			assert (t.getProbability() > 0.0d);
			addTransIfNotThere(t.getSource(), t.getTarget());

			// The target state is now part of the subsystem as well
			addState(t.getTarget());
		}

		numPathFragments++;
		probMassOutdated = true;

		//log.println("Subsystem looks like this: " + this, PrismLog.VL_ALL);
	}

	private void addState(final int state)
	{
		statesInSubsystem.set(state);
		explicitStatesInSubsystem.add(state);
		dijkstraCandidates.set(state);
		explicitDijkstraCandidates.add(state);
		
		try {
			reverseMatrix.doForEachTransition(state, new TransitionConsumer()
			{
				@Override
				public void accept(int pred, double prob) throws PrismException
				{
					if (statesInSubsystem.get(pred)) {
						addTransIfNotThere(pred, state);
						
						if (!hasCandidateTransitions(pred)) {
							//log.println("Will remove " + pred + " from Dijkstra sources", PrismLog.VL_ALL);
							dijkstraCandidates.clear(pred);
							explicitDijkstraCandidates.remove(pred);
						} else {
							//log.println("Will keep " + pred + " in Dijkstra sources", PrismLog.VL_ALL);
						}
					}
				}
			});
			
			matrix.doForEachTransition(state, new TransitionConsumer()
			{
				@Override
				public void accept(int succ, double prob) throws PrismException
				{
					if (statesInSubsystem.get(succ)) {
						addTransIfNotThere(state, succ);
					}
				}
			});
			
		} catch (PrismException e) {
			// Can't occur
			e.printStackTrace();
		}
	}

	private void addTransIfNotThere(int pred, int succ)
	{
		int id = matrix.getTransitionId(pred,succ);
		if (!processedTrans.get(id)) {
			//log.println("Adding new transition from " + pred + " to " + succ, PrismLog.VL_ALL);
			processedTrans.set(id);
			unprocessedTrans.clear(id);
			numTransitions++;
		} else {
			//log.println("Transition from " + pred + " to " + succ + " already there", PrismLog.VL_ALL);
		}
	}

	/**
	 * Returns the set of all states currently in the subsystem. 
	 * This has O(1) performance..
	 * @return Set of states in subsystem
	 */
	public Set<Integer> getStatesInSubsystem()
	{
		return explicitStatesInSubsystem;
	}

	@Override
	public double getProbabilityMass()
	{
		if (probMassOutdated && closureRecomputationTriggered) {
			computeProbReach();
			probMassOutdated = false;
		}
		return probMass;
	}

	private void computeProbReach()
	{
		try {
			int init = CexParams.getNormalizedInitialState(getParams(), dtmc);
			log.println("Will compute new prob mass from initial state " + init + "...", PrismLog.VL_ALL);
			DTMCModelChecker mc = new DTMCModelChecker(null);

			// Perform modelchecking, restricted to the states that are currently in the subsystem
			ModelCheckerResult res = mc.computeReachProbs(this, statesInSubsystem, normalizedTargetStates, null, null);//mc.computeReachProbs(this, normalizedTargetStates);
			probMass = res.soln[init];
			reachabilityProbs = res.soln;

			log.println("Got back prob mass from modelchecking: " + probMass, PrismLog.VL_ALL);

			/*log.println("----------------------------------------", PrismLog.VL_ALL);
			for (int i : getStatesInSubsystem()) {
				log.println(i + " : " + res.soln[i], PrismLog.VL_ALL);
			}
			log.println("----------------------------------------", PrismLog.VL_ALL);*/

		} catch (PrismException e) {
			log.println("Modelchecking to compute new prob mass failed");
			e.printStackTrace();
		}
		closureRecomputationTriggered = false;
		probMassOutdated = false;
	}

	@Override
	public CexStatistics generateStats()
	{
		return new CexSubsystemStats(getParams(), getNumStatesInSubsystem(), numTransitions, numPathFragments, getProbabilityMass(), computationTimeInMs);
	}

	@Override
	public int getNumStates()
	{
		return numStates;
	}

	public int getNumStatesInSubsystem()
	{
		return explicitStatesInSubsystem.size();
	}

	@Override
	public int getMaxPossibleStateIndex()
	{
		return dummySinkState;
	}

	@Override
	public int getNumCandidateInitialStates()
	{
		// All states in the subsystem that have not been processed completely are initial states in the local search
		return explicitDijkstraCandidates.size();
		//return getNumStatesInSubsystem();
	}

	@Override
	public Iterable<Integer> getCandidateInitialStates()
	{
		// All states in the subsystem that have not been processed completely are initial states in the local search
		return explicitDijkstraCandidates;
		//return explicitStatesInSubsystem;
	}

	@Override
	public int getFirstCandidateInitialState()
	{
		// All states in the subsystem that have not been processed completely are initial states in the local search
		return explicitDijkstraCandidates.first();
		//return explicitStatesInSubsystem.first();
	}

	@Override
	public boolean isCandidateInitialState(int i)
	{
		// All states in the subsystem are initial states in the local search
		return dijkstraCandidates.get(i);
		// return statesInSubsystem.get(i);
	}

	@Override
	public int getNumCandidateTargetStates()
	{
		// All states in the subsystem are target states in the local search
		return getNumStatesInSubsystem();
	}

	@Override
	public Iterable<Integer> getCandidateTargetStates()
	{
		// All states in the subsystem are target states in the local search
		return explicitStatesInSubsystem;
	}

	@Override
	public boolean isCandidateTargetState(int trg)
	{
		// All states in the subsystem are target states in the local search
		return statesInSubsystem.get(trg);
	}

	@Override
	public int getFirstCandidateTargetState()
	{
		// All states in the subsystem are target states in the local search
		return explicitStatesInSubsystem.first();
	}

	@Override
	public int getNumTransitions()
	{
		// Note: This is *very* inefficient, but calls to this should be infrequent,
		// since we internally use numTransitions directly when generating stats etc
		// Fixing the number of transitions is especially important in export, e.g. to .tra
		int transitionsToSink = 0;
		for (int s : explicitStatesInSubsystem) {
			if (isSuccessor(s, dummySinkState)) transitionsToSink++;
		}
		
		return numTransitions + transitionsToSink;
	}

	@Override
	public Iterator<Integer> getSuccessorsIterator(int s)
	{
		return matrix.getSuccessorsIterator(s);
	}

	@Override
	public boolean isSuccessor(int s1, int s2)
	{
		return matrix.isSuccessor(s1, s2);
	}

	@Override
	public Iterator<Entry<Integer, Double>> getTransitionsIterator(int s)
	{
		if (statesInSubsystem.get(s))
			return getTransitions(s).iterator();
		else
			return new Distribution().iterator();
	}
	
	@Override
	public void doForEachCandidateTransition(int src, TransitionConsumer f) throws PrismException {
		matrix.doForEachTransition(src, f, processedTrans);
	}
	
	public void doForEachTransition(int src, TransitionConsumer f) throws PrismException {
		matrix.doForEachTransition(src, f, unprocessedTrans);
	}
	
	private boolean hasCandidateTransitions(int s) {
		return matrix.someTransitionsNotInSet(s, processedTrans);
	}

	@Override
	public double getCandidateProbability(int s1, int s2)
	{
		//assert (!processedTrans.get(matrix.getTransitionId(s1,s2))); // Otherwise this is not actually a candidate!
		return matrix.getProbOfTransition(s1, s2);
	}

	@Override
	public Iterator<Integer> getCandidatePredecessorsIterator(int targetNode)
	{
		return reverseMatrix.getSuccessorsIterator(targetNode);
	}

	public boolean isPredecessor(int s1, int s2)
	{
		return reverseMatrix.isSuccessor(s1, s2);
	}

	@Override
	public boolean hasNormalizedInitialState()
	{
		return dtmc.hasNormalizedInitialState();
	}

	@Override
	public boolean hasNormalizedTargetState()
	{
		return dtmc.hasNormalizedTargetState();
	}

	@Override
	public StateType getStateType(int source)
	{
		return dtmc.getStateType(source);
	}

	@Override
	public int getNumTransitions(int s)
	{
		return matrix.getNumTransitionsIn(s, processedTrans);
	}

	@Override
	public ModelType getModelType()
	{
		return ModelType.DTMC;
	}

	@Override
	public int getNumDeadlockStates()
	{
		if (deadlocksOutdated)
			updateDeadlocks();
		return deadlocks.size();
	}

	@Override
	public Iterable<Integer> getDeadlockStates()
	{
		if (deadlocksOutdated)
			updateDeadlocks();
		return deadlocks;
	}

	@Override
	public StateValues getDeadlockStatesList()
	{
		if (deadlocksOutdated)
			updateDeadlocks();

		// TODO: This is code duplication, see NormalizedDTMC
		BitSet bs = new BitSet();
		for (int dl : deadlocks) {
			bs.set(dl);
		}

		return StateValues.createFromBitSet(bs, this);
	}

	@Override
	public int getFirstDeadlockState()
	{
		if (deadlocksOutdated)
			updateDeadlocks();
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
		// TODO: Filter on subsystem states?! That would destroy indexing, though
		// System.out.println("Numstates: " + dtmc.getNumStates() + ", list size: " + dtmc.getStatesList().size());
		return dtmc.getStatesList();
	}

	@Override
	public Values getConstantValues()
	{
		return dtmc.getConstantValues();
	}

	@Override
	public BitSet getLabelStates(String name)
	{
		// TODO: Filter on subsystem states?! That would destroy indexing, though
		return dtmc.getLabelStates(name);
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

	@Override
	public void findDeadlocks(boolean fix) throws PrismException
	{
		// There are no deadlocks in this model, since deadlock states are never contained in a critical subsystem
	}

	@Override
	public void checkForDeadlocks() throws PrismException
	{
		// There are no deadlocks in this model, since deadlock states are never contained in a critical subsystem
	}

	@Override
	public void checkForDeadlocks(BitSet except) throws PrismException
	{
		// There are no deadlocks in this model, since deadlock states are never contained in a critical subsystem
	}

	@Override
	public void exportStates(int exportType, VarList varList, PrismLog log) throws PrismException
	{
		assert (false);
	}

	@Override
	public String infoString()
	{
		assert (false);
		return null;
	}

	@Override
	public String infoStringTable()
	{
		assert (false);
		return null;
	}

	@Override
	public Distribution getTransitions(int s)
	{
		// TODO: If this still needs to be called often (which shouldn't be the case), it might be worthwhile to only recompute this when necessary	
		
		final Distribution res = new Distribution();
		if (s < dummySinkState) {
			try {
				doForEachTransition(s, new TransitionConsumer(){

					@Override
					public void accept(int target, double prob)
					{
						res.add(target, prob);
					}

				});
			} catch (PrismException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// To make this a valid DTMC, we send the remaining probability mass into a dummy sink state with self-loop
		if (res.sum() < 1.0d)
			res.add(dummySinkState, 1.0d - res.sum());
		return res;
	}
	
	private double probOfSink(int s)
	{
		if (s == dummySinkState) return 1;
		double res = 0;
		for (int j = matrix.getFirstIndexForState(s); j <= matrix.getLastIndexForState(s); j++) {
			if (!processedTrans.get(j)) res += matrix.getProbOfTransition(j); 
		}
		return res;
	}

	@Override
	public void prob0step(BitSet subset, BitSet u, BitSet result)
	{
		int i;
		for (i = 0; i < numStates; i++) {
			if (subset.get(i)) {
				for (int j = matrix.getFirstIndexForState(i); j <= matrix.getLastIndexForState(i); j++) {
					if (!processedTrans.get(j)) continue; // Only consider transitions actually in the subsystem
					if (u.get(matrix.getTargetOfTransition(j))) result.set(i);
				}
				
			}
		}
	}

	@Override
	public void prob1step(BitSet subset, BitSet u, BitSet v, BitSet result)
	{
		int i;
		boolean contains = false;
		boolean isSubset = true;
		
		for (i = 0; i < numStates; i++) {
			if (subset.get(i)) {
				for (int j = matrix.getFirstIndexForState(i); j <= matrix.getLastIndexForState(i); j++) {
					if (!processedTrans.get(j)) continue; // Only consider transitions actually in the subsystem
					if (v.get(matrix.getTargetOfTransition(j))) contains = true;
					if (!u.get(matrix.getTargetOfTransition(j))) isSubset = false;
				}
				
				// Transition to sink
				if (probOfSink(i) > 0) {
					if (v.get(dummySinkState)) contains = true;
					if (!u.get(dummySinkState)) isSubset = false;
				}
				
				result.set(i, contains && isSubset);
			}
		}
	}

	@Override
	public double mvMultSingle(int s, double vect[])
	{
		/*int k;
		double d, prob;
		Distribution distr;

		distr = getTransitions(s);
		d = 0.0;
		for (Map.Entry<Integer, Double> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue();
			d += prob * vect[k];
		}

		return d;*/
		
		// TODO: This is untested
		int k;
		double d, prob;
		
		d = 0.0;
		for (int j = matrix.getFirstIndexForState(s); j <= matrix.getLastIndexForState(s); j++) {
			if (!processedTrans.get(j)) continue; // Only consider transitions actually in the subsystem
			
			k = matrix.getTargetOfTransition(j);
			prob = matrix.getProbOfTransition(j);
			d += prob * vect[k];
		}
		
		// Need not deal with sink here, since vect[sink] == 0 anyway?

		return d;
	}

	@Override
	public double mvMultJacSingle(int s, double vect[])
	{
		/*int k;
		double diag, d, prob;
		Distribution distr;

		distr = getTransitions(s);
		if (!(Math.abs(1.0d - distr.sum()) < 1e-8)) {
			log.println("Invalid distr for " + s + " sum: " + distr.sum() + ", transitions: " + distr, PrismLog.VL_HIGH);
			assert (false);
		}
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

		return d;*/
		
		int k;
		double diag, d, prob;

		diag = 1.0;
		d = 0.0;
		for (int j = matrix.getFirstIndexForState(s); j <= matrix.getLastIndexForState(s); j++) {
			if (!processedTrans.get(j)) continue; // Only consider transitions actually in the subsystem
			k = matrix.getTargetOfTransition(j);
			prob = matrix.getProbOfTransition(j);
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
		log.println("Warning: Inefficient implementation of CriticalSubsystem.mvMultRewSingle");
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
		log.println("Warning: Inefficient implementation of CriticalSubsystem.vmMult");
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
	public void mvMult(double vect[], double result[], BitSet subset, boolean complement)
	{
		int s;
		// Loop depends on subset/complement arguments
		if (subset == null) {
			for (s = 0; s < numStates; s++)
				result[s] = mvMultSingle(s, vect);
		} else if (complement) {
			for (s = subset.nextClearBit(0); s < numStates; s = subset.nextClearBit(s + 1))
				result[s] = mvMultSingle(s, vect);
		} else {
			for (s = subset.nextSetBit(0); s >= 0; s = subset.nextSetBit(s + 1))
				result[s] = mvMultSingle(s, vect);
		}
	}

	@Override
	public double mvMultGS(double vect[], BitSet subset, boolean complement, boolean absolute)
	{
		int s;
		double d, diff, maxDiff = 0.0;
		// Loop depends on subset/complement arguments
		if (subset == null) {
			//log.println("GS Case 1", PrismLog.VL_ALL);
			for (s = 0; s < numStates; s++) {
				d = mvMultJacSingle(s, vect);
				diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
				maxDiff = diff > maxDiff ? diff : maxDiff;
				vect[s] = d;
			}
		} else if (complement) {
			//log.println("GS Case 2", PrismLog.VL_ALL);
			for (s = subset.nextClearBit(0); s < numStates; s = subset.nextClearBit(s + 1)) {
				d = mvMultJacSingle(s, vect);
				diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
				maxDiff = diff > maxDiff ? diff : maxDiff;
				vect[s] = d;
			}
		} else {
			//log.println("GS Case 3", PrismLog.VL_ALL);
			for (s = subset.nextSetBit(0); s >= 0; s = subset.nextSetBit(s + 1)) {
				d = mvMultJacSingle(s, vect);
				diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
				maxDiff = diff > maxDiff ? diff : maxDiff;
				vect[s] = d;
			}
			// Use this code instead for backwards Gauss-Seidel
			/*for (s = numStates - 1; s >= 0; s--) {
				if (subset.get(s)) {
					d = mvMultJacSingle(s, vect);
					diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
					maxDiff = diff > maxDiff ? diff : maxDiff;
					vect[s] = d;
				}
			}*/
		}
		return maxDiff;
	}

	@Override
	public void mvMultRew(double vect[], MCRewards mcRewards, double result[], BitSet subset, boolean complement)
	{
		int s;
		// Loop depends on subset/complement arguments
		if (subset == null) {
			for (s = 0; s < numStates; s++)
				result[s] = mvMultRewSingle(s, vect, mcRewards);
		} else if (complement) {
			for (s = subset.nextClearBit(0); s < numStates; s = subset.nextClearBit(s + 1))
				result[s] = mvMultRewSingle(s, vect, mcRewards);
		} else {
			for (s = subset.nextSetBit(0); s >= 0; s = subset.nextSetBit(s + 1))
				result[s] = mvMultRewSingle(s, vect, mcRewards);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < getNumStates(); i++) {
			Distribution distr = getTransitions(i);

			if (distr.get(dummySinkState) < 1.0 || i == dummySinkState) {
				// Actually have a transition
				result.append(i + " to ");

				Iterator<Entry<Integer, Double>> it = distr.iterator();
				while (it.hasNext()) {
					Entry<Integer, Double> entry = it.next();
					result.append(entry.getKey() + " : " + entry.getValue());
					if (it.hasNext())
						result.append(", ");
				}

				result.append(System.getProperty("line.separator"));
			} else {
				//result.append(i + " no outgoing transitions in subsystem " + System.getProperty("line.separator"));
			}
		}
		return result.toString();
	}

	public Iterable<ShortestPathFinder> makeShortestPathFinders()
	{
		List<ShortestPathFinder> spfs = new LinkedList<>();
		for (int init : getCandidateInitialStates()) {
			// Since we're only interested in reachability, we must not search for path fragments from the target we're trying to reach  
			if (init == dtmc.getTargetState())
				continue;

			spfs.add(new ShortestPathFinder(this, init, log, false));
		}
		return spfs;
	}

	@Override
	public int getNumInitialStates()
	{
		return dtmc.getNumInitialStates();
	}

	@Override
	public Iterable<Integer> getInitialStates()
	{
		return dtmc.getInitialStates();
	}

	@Override
	public int getFirstInitialState()
	{
		return dtmc.getFirstInitialState();
	}

	@Override
	public boolean isInitialState(int i)
	{
		return dtmc.isInitialState(i);
	}

	public Comparator<PathEntry> makeComparatorFromReachabilityProbs()
	{
		if (probMassOutdated && closureRecomputationTriggered) {
			computeProbReach();
			probMassOutdated = false;
		}

		if (reachabilityProbs == null) {
			// This actually shouldn't be the case, but since the shortest path finder can deal
			// with a null comparator, we won't raise an exception
			assert (reachabilityProbs != null);
			return null;
		} else {
			return new Comparator<PathEntry>()
			{
				@Override
				public int compare(PathEntry arg0, PathEntry arg1)
				{
					int result = new Double(arg0.totalPathProb * reachabilityProbs[arg0.targetState]).compareTo(arg1.totalPathProb
							* reachabilityProbs[arg1.targetState]);
					return result;
				}
			};
		}
	}

	@Override
	public void export(PrismLog out, ExportType modelExportType) throws PrismException
	{
		ModelExporter modelExporter = ModelExporter.makeExporter(this, modelExportType);
		if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
		modelExporter.export(out);
	}

	@Override
	public void export(PrismLog out, StateExporter exp) throws PrismException
	{
		ModelExporter modelExporter = ModelExporter.makeExporter(this, ExportType.EXPLICIT_TRA);
		if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
		modelExporter.setStateExporter(exp);
		modelExporter.export(out);
	}

	@Override
	public String getTypeString()
	{
		return "Critical DTMC Subsystem";
	}

	@Override
	public void fillValuationSet(ValuationSet vs) throws PrismException
	{
		for (int i : explicitStatesInSubsystem) {
			vs.addValuationForStateIndex(i);
		}
	}

	@Override
	public String getSummaryString()
	{
		return "Critical Subsystem of " + getNumStatesInSubsystem() + " states, " + getNumTransitions() + " transitions";
	}

	public Model getUnderlyingModel()
	{
		return dtmc;
	}

	@Override
	public Set<String> getLabels()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasStoredPredecessorRelation()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PredecessorRelation getPredecessorRelation(PrismComponent parent, boolean storeIfNew)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearPredecessorRelation()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Entry<Integer, Pair<Double, Object>>> getTransitionsAndActionsIterator(int s)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VarList getVarList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasLabel(String name)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
