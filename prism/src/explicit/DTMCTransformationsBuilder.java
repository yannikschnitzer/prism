package explicit;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import parser.State;
import prism.PrismException;
import prism.PrismLog;

/**
 * Provides methods for preprocessing DTMC models.
 * The preprocessing is specified by calling one or more of the methods 
 */
public class DTMCTransformationsBuilder
{

	/** List of the transformations to be executed */
	private List<DTMCTransformation> transformations = new ArrayList<>();

	/**
	 * Initiate a new DTMC transformations process, beginning with an empty list of transformations.
	 * Once transformations have been added, a callable DTMCTransformations object can be received
	 * via {@link #getDTMCTransformations()
	 */
	public DTMCTransformationsBuilder() {

	}

	/**
	 * Start a new build process, i.e. forgets all previously added transformations.
	 */
	public void startNewBuild() {
		transformations.clear();
	}

	/**
	 * Adds a new unique target state. All of the original target states will lead into this new
	 * state with probability 1.
	 */
	public void addUniqueTarget(final BitSet targets) {
		transformations.add(new DTMCTransformation()
		{
			@Override
			public void apply(DTMCSimple dtmc, PrismLog log)
			{
				int newTarget = dtmc.addState();
				for (int trg = targets.nextSetBit(0); trg != -1; trg = targets.nextSetBit(trg+1)) {
					dtmc.clearState(trg);
					dtmc.addToProbability(trg, newTarget, 1);
				}
				dtmc.addToProbability(newTarget, newTarget, 1);
			}

			@Override
			public String toString() {
				return "Add unique target state";
			}
		});
	}

	/**
	 * Adds a new initial state to the DTMC with uniformly distributed transitions to all the original initial states. 
	 */
	public void makeUniformDistributionOverInitStates() {
		transformations.add(new DTMCTransformation()
		{
			@Override
			public void apply(DTMCSimple dtmc, PrismLog log)
			{
				if (dtmc.getNumInitialStates() <= 1) {
					log.println("Warning: The original DTMC already has " + dtmc.getNumInitialStates() + " initial states, will be returned unmodified");
				} else {
					int newInit = dtmc.addState();
					int numInits = dtmc.getNumInitialStates();

					for (int init : dtmc.getInitialStates()) {
						dtmc.setProbability(newInit, init, 1d / numInits);
						dtmc.removeFromInitialStates(init);
					}

					dtmc.addInitialState(newInit);
				}
			}

			@Override
			public String toString() {
				return "Add unique initial state via uniform distribution";
			}
		});
	}

	/**
	 * Demand that self loops be added to deadlock states,
	 * so that infinite paths are always possible from any states.
	 */
	public void addSelfLoppsOnDeadlocks() {
		transformations.add(new DTMCTransformation()
		{
			@Override
			public void apply(DTMCSimple dtmc, PrismLog log)
			{
				try {
					dtmc.findDeadlocks(true);
				} catch (PrismException e) {
					// This should actually be impossible
					log.println("Error while trying to fix deadlocks: " + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public String toString() {
				return "Add Self-loops on Deadlocks";
			}
		});
	}

	/**
	 * Turns all states outside the "remain" bitset into deadlocks.
	 * In this way, once the "remain" set was left, one can never reenter it in the transformed model.
	 * A property P (remain U target) on the original model is equivalent to
	 * a P (F target) property for the model obtained through this transformation.  
	 * @param remain Set of states to remain in
	 */
	public void restrictModelTo(final BitSet remain) {
		transformations.add(new DTMCTransformation()
		{
			@Override
			public void apply(DTMCSimple dtmc, PrismLog log)
			{
				for (int s = 0; s < dtmc.getNumStates(); s++) {
					if (!remain.get(s)) {
						dtmc.clearState(s);
						dtmc.addToProbability(s, s, 1);
					}
				}
			}
		});
	}

	/**
	 * For properties P >= p (remain U target):
	 * Adds two new bitsets with the given labels such that P <= 1-p (newremain U newtarget)
	 * iff the original property is satisfied. (The new property corresponds to staying in
	 * (remain and not target) until we are somewhere where neither can ever hold again.
	 * See e.g. "Counterexamples in Probabilistic Modelchecking" by Han / Katoen") 
	 * This way, we can restrict our attention to <= properties.
	 */
	public void lowerboundToUpperbound(final BitSet remain, final String targetLabel, final BitSet target, final String newRemainLabel, final String newTargetLabel) {
		transformations.add(new DTMCTransformation()
		{
			@Override
			public void apply(DTMCSimple dtmc, PrismLog log) throws PrismException
			{ 
				BitSet newRemain = new BitSet();
				BitSet newTargets = new BitSet();

				for (int s = 0; s < dtmc.getNumStates(); s++) {
					// Remain in "remain and not target"...
					if (remain.get(s) && target.get(s)) {
						newRemain.set(s);
						// ...forever...
						newTargets.set(s);
					}
				}

				// ...or until we are in a bottom SCC that contains neither a remain nor a target state
				SCCConsumerStore sccStore = new SCCConsumerStore();
				SCCComputer sccComputer = SCCComputer.createSCCComputer(null, dtmc, sccStore);
				sccComputer.computeSCCs();
				List<BitSet> bsccs = sccStore.getBSCCs();
				for (BitSet bs : bsccs) {
					boolean noneRemainNoneTarget = true;
					for (int s = bs.nextSetBit(0); s >= 0; s = bs.nextSetBit(s+1)) {
						if (remain.get(s) || target.get(s)) noneRemainNoneTarget = false;
					}
					if (noneRemainNoneTarget) {
						// The bottom SCC contains neither remain nor target states wrt the original property
						// Thus all states in the BSCC are possible targets in the modified property
						newTargets.or(bs);
					}
				}

				// Now we can actually add the sets
				dtmc.addLabel(newRemainLabel, newRemain);
				dtmc.addLabel(newTargetLabel, newTargets);
			}
		});
	}

	/**
	 * Demand that bottom-SCCs be collapsed into single states
	 */
	public void collapseBSCCs() {
		transformations.add(new DTMCTransformation()
		{
			@Override
			public void apply(DTMCSimple dtmc, PrismLog log) throws PrismException
			{
				SCCConsumerStore sccStore = new SCCConsumerStore();
				SCCComputer sccComputer = SCCComputer.createSCCComputer(null, dtmc, sccStore);
				sccComputer.computeSCCs();
				List<BitSet> bsccs = sccStore.getBSCCs();
				BitSet notInBSCCs = sccStore.getNotInBSCCs();

				// Make a new DTMC with the reduced number of states
				DTMCSimple result = new DTMCSimple();
				result.setConstantValues(dtmc.getConstantValues());

				Map<Integer,Integer> indexMap = new TreeMap<>();

				// Create the new state space and save a mapping between state spaces
				for (int i = 0; i < dtmc.getNumStates(); i++) {
					if (notInBSCCs.get(i)) {
						int newState = result.addState();
						indexMap.put(i, newState);
					}
				}

				// Create one node with self-loop per BSCC
				// plus a mapping between SCCs and collapsed nodes
				Map<Integer,Integer> sccToIndexMap = new TreeMap<>();
				for (int i = 0; i < bsccs.size(); i++) {
					int newState = result.addState();
					sccToIndexMap.put(i, newState);
					// All nodes that were originally in the BSCC are mapped onto the new node
					for (int nodeInBscc = bsccs.get(i).nextSetBit(0); nodeInBscc != -1; nodeInBscc = bsccs.get(i).nextSetBit(nodeInBscc+1)) {
						indexMap.put(nodeInBscc, newState);
					}
					result.addToProbability(newState, newState, 1);
				}

				// Set up new transition relation for non-bscc nodes
				for (int oldSrc = notInBSCCs.nextSetBit(0); oldSrc != -1; oldSrc = notInBSCCs.nextSetBit(oldSrc+1)) {
					int newSrc = indexMap.get(oldSrc);
					Distribution oldDistr = dtmc.getTransitions(oldSrc);
					for (int oldSucc : oldDistr.getSupport()) {
						int newTrg = indexMap.get(oldSucc);
						result.setProbability(newSrc, newTrg, oldDistr.get(newTrg));
					}
				}

				// Set initial states & deadlocks
				for (int init : dtmc.getInitialStates()) {
					result.addInitialState(indexMap.get(init));
				}
				for (int deadlock : dtmc.getDeadlockStates()) {
					result.addDeadlockState(indexMap.get(deadlock));
				}

				// Populate & add new label sets
				for (String label : dtmc.getAssociatedLabels()) {
					BitSet oldLabelSet = dtmc.getLabelStates(label);
					BitSet newLabelSet = new BitSet();
					for (int old = oldLabelSet.nextSetBit(0); old != -1; old = oldLabelSet.nextSetBit(old+1)) {
						newLabelSet.set(indexMap.get(old));
					}
					result.addLabel(label, newLabelSet);
				}

				// Filter state list on new states
				Iterator<State> it = dtmc.getStatesList().iterator();
				List<State> newStateList = new ArrayList<>();
				for (int i = 0; i < dtmc.getNumStates(); i++) {
					State next = it.next();
					if (notInBSCCs.get(i)) {
						newStateList.add(next);
					}
				}
			}

			@Override
			public String toString() {
				return "Perform BSCC Elimination";
			}
		});
	}

	/**
	 * Builds an object with a single run() method that will call the configured DTMC transformations
	 * in sequence, without modifying the original DTMC.  
	 */
	public DTMCTransformations getDTMCTransformations() {
		return new DTMCTransformations(transformations);
	}

	/**
	 * A sequence of DTMC transformations that can be applied via the {@link #run(DTMC, PrismLog, String...)} method.
	 */
	public class DTMCTransformations {

		/** List of transformations to be executed */
		private final List<DTMCTransformation> transformations;

		private DTMCTransformations(List<DTMCTransformation> transformations) {
			this.transformations = transformations;
		}

		/**
		 * Runs the previously set-up DTMC transformations, returning a new DTMC, leaving the original unchanged.
		 * If provided with a sparse DTMC, the result will be a sparse DTMC as well. Otherwise, a simple DTMC is returned.
		 * @param dtmc Original DTMC
		 * @param log
		 * @param labelStrings The labels to keep in the copy
		 * @return New simple or sparse DTMC with the transformations applied
		 */
		public DTMC run(final DTMC dtmc, final PrismLog log, final String... labelStrings) throws PrismException {

			DTMCSimple newDtmc = new DTMCSimple(dtmc, labelStrings);

			if (transformations.isEmpty()) {
				log.println("Warning: Run method called for empty list of DTMC Transformations.");
			} else {
				log.println("Will perform the following DTMC transformation(s): ", PrismLog.VL_HIGH);
				for (DTMCTransformation f : transformations) {
					log.println(" * " + f);
				}	
			}

			for (DTMCTransformation f : transformations) {
				f.apply(newDtmc, log);
			}

			return transformToOriginalType(newDtmc, dtmc, log);
		}	
	}

	private static DTMC transformToOriginalType(DTMCSimple newDtmc, DTMC original, final PrismLog log)
	{
		if (original instanceof DTMCSimple) {
			return newDtmc;
		} else if (original instanceof DTMCSparse) {
			return new DTMCSparse(newDtmc);
		} else {
			log.println("Warning: Cannot return DTMC of original type " + original.getClass().getName()
					+ "; will return " + newDtmc.getClass().getName() + " instead");
			return newDtmc;
		}
	}

	/**
	 * A single executable DTMC transformation
	 */
	protected interface DTMCTransformation {
		public void apply(DTMCSimple dtmc, PrismLog log) throws PrismException;
	}

}
