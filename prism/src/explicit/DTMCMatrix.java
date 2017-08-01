package explicit;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import explicit.cex.util.Transition;
import prism.PrismException;

public class DTMCMatrix
{

	/** Probabilities for each transition (array of size numTransitions) */
	protected double nonZeros[];
	/** Column (destination) indices for each transition (array of size numTransitions) */
	protected int cols[];
	/** Indices into nonZeros/cols giving the start of the transitions for each state
	 * array is of size numStates + 1, where the last entry is equal to numTransitions */
	protected int stateStarts[];

	/** Entry buffer for quick access of transition iterator */
	protected List<List<Entry<Integer,Double>>> entryBuffer = new ArrayList<>();

	public DTMCMatrix(StateToDistributionMap model) {
		this(model, false);
	}

	public DTMCMatrix(StateToDistributionMap model, boolean reverse) {
		List<Distribution> dists = new ArrayList<>();
		if (!reverse) {
			for (int i = 0; i < model.getNumStates(); i++) {
				dists.add(model.getTransitions(i));
			}
		} else {
			for (int i = 0; i < model.getNumStates(); i++) {
				dists.add(new Distribution());
			}
			for (int i = 0; i < model.getNumStates(); i++) {
				for (Iterator<Entry<Integer,Double>> it = model.getTransitions(i).iterator(); it.hasNext(); ) {
					Entry<Integer,Double> e = it.next();
					dists.get(e.getKey()).set(i, e.getValue());
				}
			}
		}

		initFromDists(model, dists);
	}

	private void initFromDists(StateToDistributionMap model, List<Distribution> dists) {
		int numStates = model.getNumStates();
		int numTransitions = model.getNumTransitions();

		nonZeros = new double[numTransitions];
		cols = new int[numTransitions];
		stateStarts = new int[numStates+1];
		int index = 0;

		for (int i = 0; i < numStates; i++) {
			stateStarts[i] = index;

			for (Iterator<Entry<Integer,Double>> it = dists.get(i).iterator(); it.hasNext();) {
				Entry<Integer,Double> entry = it.next();
				cols[index] = entry.getKey();
				nonZeros[index] = entry.getValue();
				index++;
			}
		}
		stateStarts[numStates] = numTransitions;

		// Fill entry buffer
		for (int i = 0; i < numStates; i++) {
			entryBuffer.add(new ArrayList<Entry<Integer,Double>>());
		}

		for (int i = 0; i < numStates; i++) {
			for (Iterator<Entry<Integer,Double>> it = dists.get(i).iterator(); it.hasNext();) {
				//entryBuffer.get(i).add(new DTMCMatrixEntry<Integer, Double>(it.next()));
				entryBuffer.get(i).add(it.next());
			}
		}
	}

	public int getTargetOfTransition(int t) {
		return cols[t];
	}

	public double getProbOfTransition(int t) {
		return nonZeros[t];
	}

	public int getFirstIndexForState(int s) {
		return stateStarts[s];
	}

	public int getLastIndexForState(int s) {
		return stateStarts[s+1]-1;
	}

	public int getNumTransitions(int s)
	{
		return stateStarts[s+1] - stateStarts[s];
	}

	public Iterator<Entry<Integer, Double>> getTransitionsIterator(int s)
	{
		return entryBuffer.get(s).iterator();
	}

	public Iterator<Integer> getSuccessorsIterator(int s)
	{
		// TODO This could also be done more efficiently
		List<Integer> result = new LinkedList<>();
		for (int i = stateStarts[s]; i < stateStarts[s+1]; i++) {
			result.add(cols[i]);
		}
		return result.iterator();
	}

	public boolean someSuccessorsInSet(int s, BitSet set)
	{
		for (int i = stateStarts[s]; i < stateStarts[s+1]; i++) {
			if (set.get(cols[i])) return true;
		}
		return false;
	}

	public boolean someTransitionsNotInSet(int s, BitSet set)
	{
		for (int i = stateStarts[s]; i < stateStarts[s+1]; i++) {
			if (!set.get(i)) return true;
		}
		return false;
	}

	public boolean allSuccessorsInSet(int s, BitSet set)
	{
		for (int i = stateStarts[s]; i < stateStarts[s+1]; i++) {
			if (!set.get(cols[i])) return false;
		}
		return true;
	}
	
	public int getNumTransitionsIn(int s, BitSet set)
	{
		int result = 0;
		for (int i = stateStarts[s]; i < stateStarts[s+1]; i++) {
			if (set.get(i)) result++;
		}
		return result;
	}

	public boolean isSuccessor(int s1, int s2)
	{
		for (int i = stateStarts[s1]; i < stateStarts[s1+1]; i++) {
			if (cols[i] == s2) return true;
		}
		return false;
	}

	public int getTransitionId(int s1, int s2)
	{
		for (int i = stateStarts[s1]; i < stateStarts[s1+1]; i++) {
			if (cols[i] == s2) return i;
		}
		throw new RuntimeException();
	}

	public double getProbOfTransition(int s1, int s2)
	{
		for (int i = stateStarts[s1]; i < stateStarts[s1+1]; i++) {
			if (cols[i] == s2) return nonZeros[i];
		}
		return 0;
	}

	public void doForEachTransition(int src, TransitionConsumer f) throws PrismException {
		for (int i = stateStarts[src]; i < stateStarts[src+1]; i++) {
			f.accept(cols[i], nonZeros[i]);
		}
	}

	public void doForEachTransition(int src, TransitionConsumer f, BitSet except) throws PrismException {
		for (int i = stateStarts[src]; i < stateStarts[src+1]; i++) {
			if (!except.get(i)) f.accept(cols[i], nonZeros[i]);
		}
	}
	
	public void doForEachSuccessor(int src, SuccessorConsumer f) throws PrismException {
		for (int i = stateStarts[src]; i < stateStarts[src+1]; i++) {
			f.accept(cols[i]);
		}
	}
	
	public void doForEachSuccessor(int src, SuccessorConsumer f, BitSet except) throws PrismException {
		for (int i = stateStarts[src]; i < stateStarts[src+1]; i++) {
			if (!except.get(i)) f.accept(cols[i]);
		}
	}

	public final String transitionSetToString(BitSet set, int s) {
		StringBuilder result = new StringBuilder();

		for (int i = set.nextSetBit(s >= 0 ? stateStarts[s] : 0); i != -1 && (s >= s ? i < stateStarts[s+1] : true); i = set.nextSetBit(i+1)) {
			result.append(new Transition(getSource(i), cols[i], nonZeros[i]) + System.getProperty("line.separator"));
		}
	
		return result.toString();
	}

	private int getSource(int i)
	{
		int s = 0;
		while (stateStarts[s] <= i) s++;
		return s-1;
	}

}
