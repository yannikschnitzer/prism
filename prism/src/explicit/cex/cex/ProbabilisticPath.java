package explicit.cex.cex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import parser.State;
import simulator.PathFullInfo;
import explicit.Model;
import explicit.cex.util.Transition;
import explicit.cex.util.ValuationSet;
import explicit.exporters.StateExporter;

/**
 * This class represents a single probabilistic path.
 * 
 * It provides O(1) operations for adding and removing transitions at the front/back of the path
 * to allow for efficient incremental construction of the path.
 * While this is not enforced, the idea is that the finished path connects an initial to a target state.
 */
public class ProbabilisticPath implements PathFullInfo
{
	public static final double UNINITIALIZED = -1;

	/** The transitions on this path
	 * TODO: This contains some redundant info, since each transition contains both source and target. If memory becomes an issue, this is one area for cleanup
	 */
	protected ArrayDeque<Transition> transitions = new ArrayDeque<>();

	/** If the initial states are distributed randomly, this will hold the probability of the initial state of this path */
	protected double probabilityOfInitialState = 1;

	/** Total probability of the path (cached for efficiency reasons; invalidated when the path is changed */
	protected double probability = UNINITIALIZED;

	/** The state info for the states of the underlying model. This has to be set if one wants to make use of (certain parts of) the {@link PathFullInfo} interface.
	 * Otherwise, it may be left null */
	protected List<State> states = null;

	public void setStateInfo(List<State> states) {
		this.states = states;
	}

	public int getLength()
	{
		return transitions.size();
	}

	public void addTransitionAtFront(Transition t)
	{
		transitions.addFirst(t);
		probability = UNINITIALIZED;
	}

	public void addTransitionAtBack(Transition t)
	{
		transitions.addLast(t);
		probability = UNINITIALIZED;
	}

	public void setInitialProbability(double prob)
	{
		probabilityOfInitialState = prob;
	}

	public Transition getFirst()
	{
		return transitions.getFirst();
	}

	public Transition getLast()
	{
		return transitions.getLast();
	}

	public void removeFirst()
	{
		transitions.removeFirst();
		probability = UNINITIALIZED;
	}

	public void removeLast()
	{
		transitions.removeLast();
		probability = UNINITIALIZED;
	}

	/**
	 * Returns the total probability of the path.
	 * Unless the path is changed, this will only be computed once and cached, so this is O(1) from the second call onwards. 
	 * @return Total probability of the path.
	 */
	public double getProbability()
	{
		if (probability == UNINITIALIZED) {
			probability = probabilityOfInitialState;
			for (Transition t : transitions) {
				probability *= t.getProbability();
			}
		}

		return probability;
	}

	/**
	 * Returns an iterator for traversing the path in the correct order from initial to target state.
	 * @return Iterator for traversing the path
	 */
	public Iterator<Transition> getTransitionIterator()
	{
		return transitions.iterator();
	}

	public Iterable<Transition> getTransitions()
	{
		return transitions;
	}

	public Iterable<Integer> getStates() {
		List<Integer> states = new ArrayList<>();
		states.add(getFirst().getSource());
		for (Transition t : transitions) {
			states.add(t.getTarget());
		}
		return states;
	}

	public String toStringWithDelim(String delim, StateExporter exp) {
		StringBuilder result = new StringBuilder();
		if (!transitions.isEmpty()) {
			result.append(exp.stateToString(getFirst().getSource()));
			Iterator<Transition> it = getTransitionIterator();
			while (it.hasNext()) {
				result.append(delim);
				int trg = it.next().getTarget();
				result.append(exp.stateToString(trg));
			}
		} else {
			result.append("empty path");
		}

		return result.toString();
	}

	public void gatherValuations(ValuationSet vs) {
		for (int i : getStates()) {
			vs.addValuationForStateIndex(i);
		}
	}

	@Override
	public String toString()
	{
		return "{" + probabilityOfInitialState + "} " + toStringWithDelim("->", new StateExporter());
	}
	
	public Transition getIthTransition(int i) {
		// TODO: Must change underlying implementation away from deque to support this efficiently
		Iterator<Transition> it = transitions.iterator();
		for (int j = 1; j <= i; j++)
			it.next();
		return it.next();
	}

	/*
	 * Implementation of path full info interface
	 */

	@Override
	public long size()
	{
		return transitions.size();
	}

	@Override
	public State getState(int step)
	{
		if (states == null)
			throw new IllegalStateException("Trying to access state info, but list of state info is null");
		
		int resultIndex = (step == size()) ? getLast().getTarget() : getIthTransition(step).getSource();
		
		return states.get(resultIndex);
	}

	@Override
	public double getStateReward(int step, int rsi)
	{
		// No reward info is stored
		return 0;
	}

	@Override
	public double getCumulativeTime(int step)
	{
		// No time info is stored
		return 0;
	}

	@Override
	public double getCumulativeReward(int step, int rsi)
	{
		// No reward info is stored
		return 0;
	}

	@Override
	public double getTime(int step)
	{
		// No reward info is stored
		return 0;
	}

	@Override
	public int getChoice(int step)
	{
		// No choice info is stored (note that this is overridden in {@link NondetPath}
		return 0;
	}

	@Override
	public int getModuleOrActionIndex(int step)
	{
		// No action info is stored
		return 0;
	}

	@Override
	public String getModuleOrAction(int step)
	{
		// No action info is stored
		return null;
	}

	@Override
	public double getTransitionReward(int step, int rsi)
	{
		// No reward info is stored
		return 0;
	}

	@Override
	public boolean isLooping()
	{
		// No loop info is stored
		return false;
	}

	@Override
	public long loopStart()
	{
		// No loop info is stored
		return 0;
	}

	@Override
	public long loopEnd()
	{
		// No loop info is stored
		return 0;
	}

	@Override
	public boolean hasRewardInfo()
	{
		return false;
	}

	@Override
	public boolean hasChoiceInfo()
	{
		return false;
	}

	@Override
	public boolean hasActionInfo()
	{
		return false;
	}

	@Override
	public boolean hasTimeInfo()
	{
		return false;
	}

	@Override
	public boolean hasLoopInfo()
	{
		return false;
	}

	public ProbabilisticPath restrictTo(Model underlyingModel)
	{
		ProbabilisticPath result = new ProbabilisticPath();
		result.probabilityOfInitialState = probabilityOfInitialState;
		result.states = states;
		for (Transition t : transitions) {
			if (t.getSource() < underlyingModel.getNumStates() && t.getTarget() < underlyingModel.getNumStates()) {
				result.addTransitionAtBack(t);
			}
		}
		return result;
	}

}
