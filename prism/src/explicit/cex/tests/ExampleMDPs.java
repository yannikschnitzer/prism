package explicit.cex.tests;

import java.util.BitSet;

import explicit.Distribution;
import explicit.MDPSimple;

public class ExampleMDPs
{

	public static MDPwithTargets initialChoicePathSet(int numPaths, int statesPerPath, int numTargets)
	{
		int numStates = numPaths * statesPerPath + 1;
		MDPSimple mdp = new MDPSimple(numStates);
		BitSet targets = new BitSet(numStates);

		// A single initial state...
		int init = 0;
		mdp.addInitialState(init);
		
		// ...with choices leading to each path
		for (int pathNr = 0; pathNr < numPaths; pathNr++) {
			int firstStateOnPath = pathNr * statesPerPath + 1;
			addChoice(mdp, init, new Tran(firstStateOnPath, 1));
		}
		
		for (int pathNr = 0; pathNr < numPaths; pathNr++) {
			int firstStateOnPath = pathNr * statesPerPath + 1;
			int lastStateOnPath = (pathNr + 1) * statesPerPath;

			// Edges from initial state to target state
			for (int stateNr = firstStateOnPath; stateNr < lastStateOnPath; stateNr++) {
				addChoice(mdp, stateNr, new Tran(stateNr + 1, 1));
			}

			// Self-loop on last state
			addChoice(mdp, lastStateOnPath, new Tran(lastStateOnPath, 1));

			// Target state if applicable
			if (pathNr < numTargets)
				targets.set(lastStateOnPath);
		}

		return new MDPwithTargets(mdp, targets, numPaths + " nondet. selected " + statesPerPath + "-Paths (" + numTargets + " targets)");
	}
	
	public static MDPwithTargets binaryChoiceTree(int depth, int numTargets)
	{
		int numStates = (2 << depth) - 1;
		int firstLeaf = (2 << (depth - 1)) - 1;
		MDPSimple mdp = new MDPSimple(numStates);
		BitSet targets = new BitSet(numStates);

		mdp.addInitialState(0);

		// Add edges to children
		for (int state = 0; state < firstLeaf; state++) {
			addChoice(mdp, state, new Tran(2 * state + 1, 1));
			addChoice(mdp, state, new Tran(2 * state + 2, 1));
		}

		// Self-loop on leaves
		for (int state = firstLeaf; state < numStates; state++) {
			addChoice(mdp, state, new Tran(state, 1));
		}

		// Set targets
		for (int state = firstLeaf; state < firstLeaf + numTargets; state++) {
			targets.set(state);
		}

		return new MDPwithTargets(mdp, targets, "Binary Choice Tree w/ depth " + depth + ", " + numTargets + " targets");
	}
	
	private static void addChoice(MDPSimple mdp, int init, Tran... ts)
	{
		mdp.addChoice(init, distFromTrans(ts));
	}

	private static Distribution distFromTrans(Tran... ts) {
		Distribution distr = new Distribution();
		for (Tran t : ts) {
			distr.add(t.trg, t.prob);
		}
		return distr;
	}
	
}
