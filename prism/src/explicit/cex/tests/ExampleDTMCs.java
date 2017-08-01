package explicit.cex.tests;

import java.util.BitSet;

import explicit.DTMCSimple;

/**
 * This class provides (static) access to several simple families of DTMCs.
 * 
 * These DTMCs are meant to be used both in manual and automatic tests.
 */
public class ExampleDTMCs
{

	/**
	 * DTMC that is a set of disjoint path, each of which starts with an initial state
	 * and the first numTargets end in a target state with self-loop.
	 * This is useful for simple tests of the different reachability property semantics:
	 * Either a single initial state has to violate the property, or all of them given an initial distribution.
	 * It can also be used as the basis for more difficult DTMCs 
	 * @param numPaths number of disjoint paths
	 * @param statesPerPath number of nodes connected by prob. 1 edges for each path
	 * @param numTargets number of paths that end in a target
	 * @return
	 */
	public static DTMCwithTargets setOfPaths(int numPaths, int statesPerPath, int numTargets)
	{
		int numStates = numPaths * statesPerPath;
		DTMCSimple dtmc = new DTMCSimple(numStates);
		BitSet targets = new BitSet(numStates);

		for (int pathNr = 0; pathNr < numPaths; pathNr++) {
			int firstStateOnPath = pathNr * statesPerPath;
			int lastStateOnPath = (pathNr + 1) * statesPerPath - 1;

			// Initial state on path
			dtmc.addInitialState(firstStateOnPath);

			// Edges from initial state to target state
			for (int stateNr = firstStateOnPath; stateNr < lastStateOnPath; stateNr++) {
				dtmc.setProbability(stateNr, stateNr + 1, 1);
			}

			// Self-loop on last state
			dtmc.setProbability(lastStateOnPath, lastStateOnPath, 1);

			// Target state if applicable
			if (pathNr < numTargets)
				targets.set(lastStateOnPath);
		}

		return new DTMCwithTargets(dtmc, targets, numPaths + " Disjoint " + statesPerPath + "-Paths (" + numTargets + " targets)");
	}

	/**
	 * This returns a complete binary tree of the given depth, the given number of
	 * target leaves and probabilities of leftProb and (1-leftProb) to go to the children. 
	 * @param depth Depth of the tree
	 * @param numTargets The first numTargets leaves will be target states
	 * @param leftProb Probability of going to the left child
	 * @return
	 */
	public static DTMCwithTargets binaryTree(int depth, int numTargets, double leftProb)
	{
		int numStates = (2 << depth) - 1;
		int firstLeaf = (2 << (depth - 1)) - 1;
		DTMCSimple dtmc = new DTMCSimple(numStates);
		BitSet targets = new BitSet(numStates);

		dtmc.addInitialState(0);

		// Add edges to children
		for (int state = 0; state < firstLeaf; state++) {
			dtmc.setProbability(state, 2 * state + 1, leftProb);
			dtmc.setProbability(state, 2 * state + 2, 1 - leftProb);
		}

		// Self-loop on leaves
		for (int state = firstLeaf; state < numStates; state++) {
			dtmc.setProbability(state, state, 1);
		}

		// Set targets
		for (int state = firstLeaf; state < firstLeaf + numTargets; state++) {
			targets.set(state);
		}

		return new DTMCwithTargets(dtmc, targets, "Binary Tree w/ depth " + depth + ", " + numTargets + " targets, left prob " + leftProb);
	}

	/**
	 * This returns a complete reverse binary tree oriented from the leaves to the root,
	 * where all leaves are initial nodes and the root is the single target,
	 * which optionally has an additionally sink, to which all nodes other than the target
	 * are connected with the given sinkProb 
	 * @param depth Depth of the tree
	 * @param sinkProb Probability of going to the sink instead of towards the root (may be 0)
	 * @return
	 */
	public static DTMCwithTargets reverseBinaryTreeWithSink(int depth, double sinkProb)
	{
		int nodesInTree = (2 << depth) - 1;
		int numStates = sinkProb > 0 ? nodesInTree + 1 : nodesInTree;

		int sink = sinkProb > 0 ? nodesInTree : -1;
		int target = 0;
		int firstInit = (2 << (depth - 1)) - 1;

		DTMCSimple dtmc = new DTMCSimple(numStates);

		BitSet targets = new BitSet(numStates);
		targets.set(target);

		for (int state = firstInit; state < nodesInTree; state++) {
			dtmc.addInitialState(state);
		}

		// Add edges to children
		for (int state = 0; state < firstInit; state++) {
			dtmc.setProbability(2 * state + 1, state, 1 - sinkProb);
			dtmc.setProbability(2 * state + 2, state, 1 - sinkProb);

			if (sinkProb > 0) {
				dtmc.setProbability(2 * state + 1, sink, sinkProb);
				dtmc.setProbability(2 * state + 2, sink, sinkProb);
			}
		}

		// Self-loop on target and sink
		dtmc.setProbability(target, target, 1);
		if (sinkProb > 0) {
			dtmc.setProbability(sink, sink, 1);
		}

		return new DTMCwithTargets(dtmc, targets, "Reverse Binary Tree w/ depth " + depth + ", sink prob " + sinkProb);
	}

	public static DTMCwithTargets pathWithSingleLoop(int nodes, int indexOfLoop, double loopProb)
	{
		DTMCwithTargets path = setOfPaths(1, nodes, 1);
		path.dtmc.setProbability(indexOfLoop, indexOfLoop + 1, 1 - loopProb);
		path.dtmc.setProbability(indexOfLoop, indexOfLoop, loopProb);
		return new DTMCwithTargets(path.dtmc, path.targets, path.description + " w/ loop at " + indexOfLoop);
	}

	public static DTMCwithTargets grid(int sideLength, int targetCorners, double upProb)
	{
		int numStates = sideLength * sideLength;

		DTMCSimple dtmc = new DTMCSimple(numStates);
		dtmc.addInitialState(0);

		BitSet targets = new BitSet(numStates);
		// There are a total of four corners, of which at most three are target states 
		targets.set(sideLength - 1, targetCorners >= 3);
		targets.set(numStates - sideLength, targetCorners >= 2);
		targets.set(numStates - 1);

		for (int state = 0; state < numStates; state++) {
			boolean up = state < numStates - sideLength;
			boolean right = state % sideLength != sideLength - 1;

			if (up) {
				dtmc.setProbability(state, state + sideLength, right ? upProb : 1);
			}
			if (right) {
				dtmc.setProbability(state, state + 1, up ? 1 - upProb : 1);
			}
		}
		dtmc.setProbability(numStates - 1, numStates - 1, 1);

		return new DTMCwithTargets(dtmc, targets, sideLength + "*" + sideLength + "-Grid w/ " + targetCorners + " targets, up prob " + upProb);
	}
	
	/**
	 * Returns a DAG that has several paths from the source to the target
	 * whose probability increases with the number of transitions / hops to the target
	 * (For testing bounded / hop-constrained algorithms) 
	 */
	public static DTMCwithTargets highProbabilityHighLength(int numPaths)
	{
		int numStates = (numPaths * (numPaths+1)) / 2 + 2;
		DTMCSimple dtmc = new DTMCSimple(numStates);
		
		dtmc.addInitialState(0);
		BitSet targets = new BitSet(numStates);
		int target = numStates-1;
		targets.set(target);
		// Self-loop on target
		dtmc.setProbability(target, target, 1);
		
		double baseProb = 1.0 / (double)(numPaths * numPaths);
		double probSum = 0;
		int firstStateOnPath = 1;
		
		for (int pathNr = 1; pathNr <= numPaths; pathNr++) {
			int lastStateBeforeTarget = firstStateOnPath + pathNr - 1;
			System.out.println(firstStateOnPath + " ... " + lastStateBeforeTarget);

			// First edge
			double prob = pathNr < numPaths ? pathNr * baseProb : 1 - probSum;
			probSum += prob;
			dtmc.setProbability(0, firstStateOnPath, prob);

			// Remaining edges to increase hop count
			for (int i = firstStateOnPath; i < lastStateBeforeTarget; i++) {
				dtmc.setProbability(i, i+1, 1);
			}
			
			// Edge to target
			dtmc.setProbability(lastStateBeforeTarget, target, 1);
			
			firstStateOnPath = lastStateBeforeTarget + 1;
		}

		System.out.println(dtmc);

		return new DTMCwithTargets(dtmc, targets, numPaths + " Disjoint paths of increasing length and prob");
	}

}
