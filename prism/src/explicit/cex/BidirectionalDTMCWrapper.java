package explicit.cex;

import java.util.Iterator;

import prism.PrismException;
import explicit.TransitionConsumer;
import explicit.cex.cex.CriticalSubsystem;

/**
 * A wrapper for bidirectional access to DTMCs (sucessor and predecessor view) with a built-in notion of target states 
 * as well as optional normalization of initial and target states.
 * This serves as the common interface for both {@link NormalizedDTMC} and {@link CriticalSubsystem},
 * so that both can be passed to the shortest path algorithms.
 */
public interface BidirectionalDTMCWrapper
{

	/**
	 * Enum for distinguishing between types of states in the normalized model.
	 * This can be used for control flow abstraction together with {@link BidirectionalDTMCWrapper#getStateType(int)}.
	 */
	public enum StateType {
		ORIGINAL, MODIFIED_OLD_TARGET, NEW_TARGET, NEW_INITIAL, ERROR
	}

	/**
	 * Returns the probability to get from the first to the second state
	 * @param s1 Source of the transition
	 * @param s2 Target of the transition
	 * @return
	 */
	public double getCandidateProbability(int s1, int s2);

	public int getMaxPossibleStateIndex();
	
	public int getNumCandidateInitialStates();

	public Iterable<Integer> getCandidateInitialStates();

	public int getFirstCandidateInitialState();

	public boolean isCandidateInitialState(int i);

	public int getNumCandidateTargetStates();

	public Iterable<Integer> getCandidateTargetStates();
	
	public boolean isCandidateTargetState(int trg);

	public int getFirstCandidateTargetState();

	// TODO: Should have a getCandidatePredecessorsIterator method instead, but that doesn't matter right now, since the REA is only applied to normalized DTMCs, where there is no distinction. Should possibly change this at some point 
	public Iterator<Integer> getCandidatePredecessorsIterator(int targetNode);
	
	public void doForEachCandidateTransition(int src, TransitionConsumer f) throws PrismException;

	/**
	 * Checks if normalization of initial states was performed in this DTMC Wrapper
	 * @return True iff a new initial state was added to the model
	 */
	public boolean hasNormalizedInitialState();

	public boolean hasNormalizedTargetState();

	public StateType getStateType(int source);

}
