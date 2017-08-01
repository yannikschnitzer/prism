package explicit.cex.tests;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import explicit.DTMCSimple;

public class ProbabilisticGraph {

	private static final int NOT_STRONGLY_CONNECTED = -1;
	private static final int CONNECTIVITY_UNKNOWN = 0;
	private static final int STRONGLY_CONNECTED = 1;

	// A graph's state set is an interval of the natural numbers, derivable via numStates and offset
	private int numStates = 0;
	private int offset = 0;

	private List<Integer> inputStates = new LinkedList<>();
	private List<Integer> outputStates = new LinkedList<>();

	private Map<Integer, List<Tran>> transitionsByState = new TreeMap<>();

	private BitSet targetStates = new BitSet();

	private int connectivity = CONNECTIVITY_UNKNOWN;

	public ProbabilisticGraph() {

	}

	public ProbabilisticGraph(int numStates)
	{
		this.numStates = numStates;
	}

	public ProbabilisticGraph(ProbabilisticGraph other) {
		this(other, 0);
	}

	public ProbabilisticGraph(ProbabilisticGraph other, int offset) {
		numStates = other.numStates;
		this.offset = other.offset + offset;

		for (int input : other.inputStates) {
			inputStates.add(input + offset);
		}
		for (int output : other.outputStates) {
			outputStates.add(output + offset);
		}

		for (Entry<Integer,List<Tran>> entry : other.transitionsByState.entrySet()) {
			List<Tran> newTranisitions = new ArrayList<>();
			for (Tran t : entry.getValue()) {
				newTranisitions.add(new Tran(t, offset));
			}
			transitionsByState.put(entry.getKey()+offset, newTranisitions);
		}

		for (int i = other.targetStates.nextSetBit(0); i != -1; i = other.targetStates.nextSetBit(i+1)) {
			targetStates.set(i + offset);
		}

		connectivity = other.connectivity;
	}

	public int getNumStates()
	{
		return numStates;
	}

	public void setNumStates(int numStates)
	{
		this.numStates = numStates;
		connectivity = CONNECTIVITY_UNKNOWN;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public List<Integer> getInputStates()
	{
		return inputStates;
	}

	public void setInputStates(List<Integer> inputStates)
	{
		this.inputStates = inputStates;
	}

	public List<Integer> getOutputStates()
	{
		return outputStates;
	}

	public void setOutputStates(List<Integer> outputStates)
	{
		this.outputStates = outputStates;
	}

	public void addInitialState(int i)
	{
		inputStates.add(i);
	}

	public void addOutputState(int i)
	{
		outputStates.add(i);
	}

	public Map<Integer, List<Tran>> getTransitionsByState()
	{
		return transitionsByState;
	}

	public BitSet getTargetStates()
	{
		return targetStates;
	}

	public void setTargetStates(BitSet targetStates)
	{
		this.targetStates = targetStates;
	}

	public void unionWith(ProbabilisticGraph g, boolean shiftStates)
	{
		int offset = shiftStates ? numStates - g.getOffset() : 0;
		ProbabilisticGraph sg = new ProbabilisticGraph(g, offset);

		numStates += sg.numStates;
		inputStates.addAll(sg.inputStates);
		outputStates.addAll(sg.outputStates);
		targetStates.or(sg.targetStates);
		transitionsByState.putAll(sg.transitionsByState);

		connectivity = CONNECTIVITY_UNKNOWN;
	}

	public List<Integer> getStates() {
		List<Integer> states = new ArrayList<>();
		for (int i = offset; i < numStates + offset; i++)
			states.add(i);
		return states;
	}

	public List<Tran> getTransitions(int from)
	{
		if (transitionsByState.get(from) != null)
			return transitionsByState.get(from);
		else
			return new LinkedList<Tran>();
	}

	public boolean hasTransition(int src, int trg) {
		for (Tran t : getTransitions(src)) {
			if (t.trg == trg) return true;
		}
		return false;
	}

	public void addTransition(int src, int trg, double prob) {
		addTransition(src, trg, prob, false);
	}

	public void addTransition(int src, int trg, double prob, boolean removeFromInputOutput)
	{
		if (transitionsByState.get(src) == null)
			transitionsByState.put(src, new ArrayList<Tran>());

		transitionsByState.get(src).add(new Tran(trg,prob));

		if (removeFromInputOutput) {
			outputStates.remove(new Integer(src));
			inputStates.remove(new Integer(trg));
		}

		if (connectivity != STRONGLY_CONNECTED) connectivity = CONNECTIVITY_UNKNOWN;
	}

	/**
	 * Changes the probability of the given transition. Assumes the transition is present!
	 * @param src
	 * @param t
	 * @param prob
	 */
	public void changeProbability(int src, Tran t, double prob) {
		assert(transitionsByState.get(src) != null);
		boolean contained = transitionsByState.get(src).remove(t);
		assert(contained);
		transitionsByState.get(src).add(new Tran(t.trg, prob));
	}

	/**
	 * Scales probabilities of outgoing transitions of each state to 1
	 */
	public void normalizeProbabilities() {
		for (int i : transitionsByState.keySet()) {
			List<Tran> origTs = transitionsByState.get(i);
			List<Tran> ts = new ArrayList<>(origTs.size());
			for (Tran t : origTs) ts.add(t);

			double sum = 0;
			for (Tran t : ts) sum += t.prob;
			for (Tran t : ts) changeProbability(i, t, t.prob / sum);
		}
	}

	/**
	 * Merge two graphs using the given list of outputs/inputs to determine the connections
	 * rather than automatically via mergeAll
	 * @param g1 First of the two graphs to merge
	 * @param g2 Second of the two graphs to merge
	 * @param g1OutputsToMergeOn Outputs of the first graph to connect to the second graph
	 * @param g2InputsToMergeOn Inputs of the second graph to be connected to
	 * @return
	 */
	public static ProbabilisticGraph merge(ProbabilisticGraph g1, ProbabilisticGraph g2, List<Integer> g1OutputsToMergeOn, List<Integer> g2InputsToMergeOn) {

		if (g1OutputsToMergeOn.size() != g2InputsToMergeOn.size()) {
			throw new IllegalArgumentException("Input/Output merge lists are of different length");
		}

		// Start from g1...
		ProbabilisticGraph result = new ProbabilisticGraph(g1);
		// ...add a shifted copy of g2...
		result.unionWith(g2, true);

		// ...then add new transitions to connect outputs of g1 to inputs of g2
		for (int i = 0; i < g1OutputsToMergeOn.size(); i++) {
			result.addTransition(g1OutputsToMergeOn.get(i), g2InputsToMergeOn.get(i), 1.0, true);
		}

		result.getOutputStates().removeAll(g1OutputsToMergeOn);
		result.getInputStates().removeAll(g2InputsToMergeOn);

		return result;
	}

	/**
	 * Merges multiple graphs together by connecting the output (or input, depending on first argument) nodes of
	 * the first graph to the input (output) nodes of the other graphs.
	 * For this to be possible, the first graph needs sufficiently many output/input nodes; otherwise, null is returned
	 * @param useOutputsOfFirst Should the output or input nodes of the first graph be used for merging?
	 * @param graphs Graphs to merge, where the first graph is the special input or output graph of the merge
	 * @return A new graph that is the result of merging deep copies of the arguments, or null if merging is impossible
	 */
	public static ProbabilisticGraph mergeAll(boolean useOutputsOfFirst, ProbabilisticGraph... graphs) {
		assert(graphs.length >= 2); // Need something to merge...

		ProbabilisticGraph first = graphs[0];
		ProbabilisticGraph result = new ProbabilisticGraph(first);

		List<Integer> nodesForConnections = useOutputsOfFirst ? first.outputStates : first.inputStates;
		List<Integer> remainingNodes = new ArrayList<Integer>(nodesForConnections.size());
		for (Integer i : nodesForConnections) remainingNodes.add(i);

		for (int i = 1; i < graphs.length; i++) {
			ProbabilisticGraph g = graphs[i];
			result.unionWith(g, true);

			// Find the two lists of nodes to connect depending on first argument
			List<Integer> sources;
			List<Integer> targets;
			try {
				if (useOutputsOfFirst) {
					targets = g.inputStates;
					sources = remainingNodes.subList(0, targets.size());
				} else {
					sources = g.outputStates;
					targets = remainingNodes.subList(0, sources.size());
				}
				remainingNodes = remainingNodes.subList(targets.size(), remainingNodes.size());
			}
			catch (IndexOutOfBoundsException e) {
				return null;
			}

			// Add the connections
			assert(sources.size() == targets.size());
			for (int j = 0; j < sources.size(); j++) {
				result.addTransition(sources.get(j), targets.get(j), 1.0, true);
			}

		}		

		return result;
	}

	public static DTMCSimple toDTMC(ProbabilisticGraph graph, String labelForOutput) {
		DTMCSimple dtmc = new DTMCSimple(graph.numStates);

		for (Entry<Integer, List<Tran>> entry : graph.transitionsByState.entrySet()) {
			int src = entry.getKey();
			for (Tran t : entry.getValue()) {
				dtmc.setProbability(src, t.trg, t.prob);				
			}
		}

		for (int init : graph.inputStates) {
			dtmc.addInitialState(init);
		}

		dtmc.addLabel(labelForOutput, graph.targetStates);

		return dtmc;
	}

	@Deprecated
	public static void addToDTMC(DTMCSimple dtmc, List<Integer> outputsOfGraph, ProbabilisticGraph newPart) {
		assert(outputsOfGraph.size() == newPart.inputStates.size());

		int offset = dtmc.getNumStates();
		dtmc.addStates(newPart.numStates);

		// Inner transitions
		for (Entry<Integer, List<Tran>> entry : newPart.transitionsByState.entrySet()) {
			int src = entry.getKey() + offset;
			for (Tran t : entry.getValue()) {
				int trg = t.trg + offset;
				dtmc.setProbability(src, trg, t.prob);				
			}
		}

		// Connections
		for (int i = 0; i < outputsOfGraph.size(); i++) {
			int output = outputsOfGraph.get(i);
			dtmc.setProbability(output, offset + newPart.inputStates.get(i), 1);
		}
	}

	public boolean isStronglyConnected() {
		if (connectivity == CONNECTIVITY_UNKNOWN ) {
			if (computeConnectivity())
				connectivity = STRONGLY_CONNECTED;
			else
				connectivity = NOT_STRONGLY_CONNECTED;
		}

		return connectivity == STRONGLY_CONNECTED;
	}

	private boolean computeConnectivity()
	{
		if (!transitionsByState.keySet().iterator().hasNext()) {
			return numStates <= 1;
		}

		int startNode = transitionsByState.keySet().iterator().next();
		int index = 0;
		Map<Integer, Integer> nodeToIndex = new TreeMap<>();
		Map<Integer, Integer> nodeToLowlink = new TreeMap<>();

		exploreFrom(startNode, index, nodeToIndex, nodeToLowlink);

		if (nodeToLowlink.size() < numStates) {
			return false; // Unreachable states
		}

		for (int lowlink : nodeToLowlink.values()) {
			if (lowlink != 0) {
				// Start node is not reachable, not strongly connected
				return false;
			}
		}
		return true;
	}

	private int exploreFrom(int startNode, int index, Map<Integer, Integer> nodeToIndex, Map<Integer, Integer> nodeToLowlink)
	{
		nodeToIndex.put(startNode, index);
		nodeToLowlink.put(startNode, index);
		index++;

		for (Tran t : getTransitions(startNode)) {
			if (!nodeToIndex.containsKey(t.trg)) {
				// Not yet visited, recurse
				index = exploreFrom(t.trg, index, nodeToIndex, nodeToLowlink);
			} 
			// We can reach everything from startNode that we can reach from the target
			nodeToLowlink.put(startNode, Math.min(nodeToLowlink.get(startNode), nodeToLowlink.get(t.trg)));
		}

		return index;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int node : transitionsByState.keySet()) {
			result.append(node);
			if (inputStates.contains(node)) result.append(" [input]");
			if (outputStates.contains(node)) result.append(" [output]");

			result.append(" -> ");

			for (Tran t : getTransitions(node)) {
				result.append(t + ", ");
			}
			result.append(System.getProperty("line.separator"));
		}

		for (int node : inputStates) {
			if (!transitionsByState.keySet().contains(node)) {
				result.append(node + " [input, no outgoing transitions]" + System.getProperty("line.separator"));
			}
		}
		for (int node : outputStates) {
			if (!transitionsByState.keySet().contains(node)) {
				result.append(node + " [output, no outgoing transitions]" + System.getProperty("line.separator"));
			}
		}

		return result.toString();
	}

	public void expandNodeIntoGraph(int node, ProbabilisticGraph g)
	{
		if (g.getInputStates().size() != 1 || g.getOutputStates().size() != 1) {
			throw new IllegalArgumentException("The argument graph doesn't have unique input / output states, expansion undefined.");
		}

		unionWith(g, true);

		System.out.println("After union: ");
		System.out.println(this);
		System.out.println("--------------------------");

		// Change transitions of node
		// Outgoing:
		int output = outputStates.get(outputStates.size()-1);
		for (Tran t : getTransitions(node)) {
			addTransition(output, t.trg, t.prob);
		}
		transitionsByState.remove(node);

		// Incoming: Very inefficient, but still doable for the kind of small graphs we're generating...
		int input = inputStates.get(inputStates.size()-1);
		for (int src : transitionsByState.keySet()) {
			if (src == node) continue; // Ignore self-loop

			Tran remove = null;
			for (Tran t : getTransitions(src)) {
				if (t.trg == node) {
					remove = t;
					break;
				}
			}
			if (remove != null) {
				getTransitions(src).remove(remove);
				addTransition(src, input, remove.prob);
			}
		}

		if (inputStates.contains(node)) {
			inputStates.remove(new Integer(node));
		} else {
			inputStates.remove(new Integer(input));
		}
		if (outputStates.contains(node)) {
			outputStates.remove(new Integer(node));
		} else {
			outputStates.remove(new Integer(output));
		}
	}

}