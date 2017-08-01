package explicit.cex.gens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import prism.PrismException;
import prism.PrismLog;
import explicit.TransitionConsumer;
import explicit.cex.BidirectionalDTMCWrapper;
import explicit.cex.BidirectionalDTMCWrapper.StateType;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.util.PathEntry;
import explicit.cex.util.PriorityQueueWithDecreasableKeys;
import explicit.cex.util.Transition;

/**
 * Class for computing and accessing the most probable paths in a DTMC.
 * This implements the iterator interface for access to the paths in order of descending probability.
 * 
 * TODO: Generalize shortest path finder to directly support MDPs?!
 * TODO: Make constructors private and add factory methods for the different counterexample generation methods
 */
public class ShortestPathFinder implements Iterator<ProbabilisticPath>
{

	public static final int EXPAND_ALL_TARGETS = Integer.MAX_VALUE;
	public static final int SEARCH_FROM_ALL_INITIAL_STATES = -42;

	private static final int NO_PREDECESSOR = -1;

	private Comparator<PathEntry> pathComparator = new Comparator<PathEntry>()
	{

		@Override
		public int compare(PathEntry arg0, PathEntry arg1)
		{
			return new Double(arg0.totalPathProb).compareTo(arg1.totalPathProb);
		}

	};

	/** DTMC to search paths in */
	private BidirectionalDTMCWrapper model;
	/** The unique initial state for the shortest path search */
	private final int initialState;
	/** Prune paths down to original DTMC states, i.e. remove states introduced through normalization */
	private final boolean performPruning;
	/** Abort Dijkstra's algorithm after expanding this many targets. This makes sense in the local search, where we don't need to perform a full Dijkstra */
	private int numTargetsToExpand = EXPAND_ALL_TARGETS;

	/** Predecessor list for Dijkstra's algorithm */
	private ArrayList<ArrayList<PathEntry>> predList = null;
	/** Candidate predecessor list for recursive enumeration algorithm */
	private ArrayList<PriorityQueue<PathEntry>> candidates = null;
	/** The index of the next path to return by the iterator */
	private int k = 0;
	/** Buffer for the next path, used to make iterator implementation efficient
	 * @see {@link #hasNext}, {@link #next}
	 */
	private ProbabilisticPath nextPath = null;

	private PrismLog log;
	
	private ArrayList<PathEntry> expandedTargets = new ArrayList<>();

	/**
	 * 
	 * @param model
	 * @param initialState
	 * @param targetLabel
	 * @param targetStates
	 * @param log
	 */
	public ShortestPathFinder(BidirectionalDTMCWrapper model, int initialState, PrismLog log)
	{
		this(model, initialState, log, true, EXPAND_ALL_TARGETS, null);
	}

	public ShortestPathFinder(BidirectionalDTMCWrapper model, int initialState, PrismLog log, boolean performPruning)
	{
		this(model, initialState, log, performPruning, EXPAND_ALL_TARGETS, null);
	}

	/**
	 * 
	 * @param model
	 * @param initialState
	 * @param log
	 * @param performPruning Prune paths down to original DTMC states, i.e. remove states introduced through normalization
	 * @param pathComparator
	 */
	public ShortestPathFinder(BidirectionalDTMCWrapper model, int initialState, PrismLog log, boolean performPruning, int numTargetsToExpand,
			Comparator<PathEntry> pathComparator)
	{
		assert (model != null);
		this.model = model;
		this.initialState = initialState;
		this.log = log;
		this.performPruning = performPruning;
		assert (numTargetsToExpand > 0);
		this.numTargetsToExpand = numTargetsToExpand;
		if (pathComparator != null)
			this.pathComparator = pathComparator;

		init();
	}

	public int getSource()
	{
		return initialState;
	}

	private void init()
	{
		// Prepare data structure for Dijkstra's algorithm 
		predList = new ArrayList<>(model.getMaxPossibleStateIndex() + 1);
		for (int i = 0; i <= model.getMaxPossibleStateIndex(); i++) {
			predList.add(new ArrayList<PathEntry>());
		}

		// Prepare data structure for recursive enumeration algorithm
		candidates = new ArrayList<>(model.getMaxPossibleStateIndex() + 1);
		for (int i = 0; i <= model.getMaxPossibleStateIndex(); i++) {
			candidates.add(new PriorityQueue<PathEntry>());
		}

		//log.println("Initialized shortest path finder that will start from " + initialState, PrismLog.VL_ALL);
	}

	private void dijkstra() throws PrismException
	{
		assert (predList != null);
		assert (predList.size() == model.getMaxPossibleStateIndex() + 1);

		log.println("Starting " + (numTargetsToExpand == EXPAND_ALL_TARGETS ? "full " : "") + "Dijkstra's algorithm"
				+ (numTargetsToExpand != EXPAND_ALL_TARGETS ? ". Will perform at most " + numTargetsToExpand + " target expansions" : ""), PrismLog.VL_ALL);

		PriorityQueueWithDecreasableKeys<PathEntry> queue = new PriorityQueueWithDecreasableKeys<PathEntry>();

		if (initialState == SEARCH_FROM_ALL_INITIAL_STATES) {
			log.println("Will search from " + model.getNumCandidateInitialStates() + " sources", PrismLog.VL_ALL);
			
			/*Iterable<Integer> it = model.getCandidateInitialStates();
			for (int s : it) log.print(s + " ");
			log.println();*/
			
			setupDijkstraFromAllSources(queue);
		} else {
			log.println("Will search from state " + initialState, PrismLog.VL_ALL);
			setupDijkstraFromSingleSource(queue, initialState);
		}

		dijkstra(queue);
	}

	private void setupDijkstraFromAllSources(PriorityQueueWithDecreasableKeys<PathEntry> queue) throws PrismException
	{
		for (int initialState : model.getCandidateInitialStates()) {
			setupDijkstraFromSingleSource(queue, initialState);
		}
	}

	private void setupDijkstraFromSingleSource(final PriorityQueueWithDecreasableKeys<PathEntry> queue, final int initialState) throws PrismException
	{
		// If there is a new initial state, then its distance from the source is initialized with -1, since step bounds apply to the original model
		int distanceFromSource = model.hasNormalizedInitialState() ? -1 : 0;

		if (!model.isCandidateTargetState(initialState)) {
			// Usual setup for Dijkstra's algorithm, since the initial state is no target we won't have problems with 0 length paths
			// The initial state has a reachability probability of 1, no predecessors, is the 0-th shortest path to get there
			PathEntry initEntry = new PathEntry(initialState, NO_PREDECESSOR, null, 1, 0, distanceFromSource);
			setKthEntry(predList, initialState, 0, initEntry);
			queue.add(initEntry);
		} else {
			// The initial state is a target as well. Thus the best path is always of length 0.
			// To avoid that, we add the successors of the initial state to the queue, rather than the initial state itself
			
			// This entry is special, because we won't enqueue it to allow for a self-loop at the initial state
			final PathEntry specialInitEntry = new PathEntry(initialState, NO_PREDECESSOR, null, 1, 0, distanceFromSource);
			
			model.doForEachCandidateTransition(initialState, new TransitionConsumer()
			{
				@Override
				public void accept(int target, double prob) throws PrismException
				{
					// TODO Auto-generated method stub
					enqueueTarget(queue, initialState, specialInitEntry, target, prob);
				}
			});
		}

		//log.println("Have enqueued initial state " + initialState + ". Will start Dijkstra's algorithm now.", PrismLog.VL_ALL);
	}

	private void dijkstra(final PriorityQueueWithDecreasableKeys<PathEntry> queue) throws PrismException
	{
		int numExpandedTargets = 0;

		while (!queue.isEmpty() && numExpandedTargets < numTargetsToExpand) {
			final PathEntry srcEntry = queue.poll();
			final int src = srcEntry.targetState;
			if (model.isCandidateTargetState(src)) {
				numExpandedTargets++;
				expandedTargets.add(srcEntry);
				log.println("Expanded target " + src, PrismLog.VL_ALL);
			}
			
			model.doForEachCandidateTransition(src, new TransitionConsumer()
			{
				
				@Override
				public void accept(int target, double prob) throws PrismException
				{
					// Self-loops are possible, but need never be considered in Dijkstra's algorithm
					// Unless the state is both an initial and a target state)
					if (target == src && (!model.isCandidateTargetState(target) || !model.isCandidateInitialState(src))) {
						return;
					}

					// The (candidate) new probability for the target is the probability of reaching
					// the source multiplied by the probability of the transition
					double altTrgProb = srcEntry.totalPathProb * prob;

					// If we're already at a target state, we can stop the search.
					// Otherwise, we have to continue the search from the target
					if (!model.isCandidateTargetState(src)) {
						//log.println("Enqueuing " + trg, PrismLog.VL_ALL);
						enqueueTarget(queue, src, srcEntry, target, altTrgProb);
					}
				}
			});
		}

	}

	/**
	 * 
	 * @param queue
	 * @param src
	 * @param srcEntry
	 * @param trg
	 * @param altTrgProb
	 * @throws PrismException 
	 * @see {@link dijkstra}
	 */
	private void enqueueTarget(PriorityQueueWithDecreasableKeys<PathEntry> queue, int src, PathEntry srcEntry, int trg, double altTrgProb)
			throws PrismException
	{
		assert (predList.get(trg) != null);

		if (predList.get(trg).isEmpty()) {
			// No PathEntry there yet => Create a new one and add to the queue
			PathEntry trgEntry = new PathEntry(trg, src, srcEntry, altTrgProb, 0, srcEntry.stepsFromSource + 1);
			setKthEntry(predList, trg, 0, trgEntry);

			queue.add(trgEntry);
		} else {
			// Already an entry there => Decrease key if applicable
			// If not, do nothing (will be processed as before)
			PathEntry trgEntry = predList.get(trg).get(0);
			if (altTrgProb > trgEntry.totalPathProb) {
				trgEntry.totalPathProb = altTrgProb;
				trgEntry.predState = src;
				trgEntry.predEntry = srcEntry;
				queue.decreaseKey(trgEntry);
			}
		}
	}

	private static ProbabilisticPath pathEntryToPath(PathEntry targetEntry, BidirectionalDTMCWrapper model)
			throws PrismException
	{
		assert (targetEntry.predState != NO_PREDECESSOR); // Path must have length at least 1
		int target = targetEntry.targetState;

		ProbabilisticPath result = new ProbabilisticPath();

		while (targetEntry.predState != NO_PREDECESSOR) {
			double prob = model.getCandidateProbability(targetEntry.predState, target);
			assert (prob != 0); // The transition must actually be there
			//System.out.println("New transition from " + targetEntry.predState + " to " + target + " with prob " + prob + " (total: " + result.getProbability() + ")");
			result.addTransitionAtFront(new Transition(targetEntry.predState, target, prob));

			target = targetEntry.predState;
			targetEntry = targetEntry.predEntry;
		}

		//System.out.println("New path with prob " + result.getProbability());

		return result;
	}

	private ProbabilisticPath getKthShortestPath(int k) throws PrismException
	{
		expandedTargets.clear();
		if (k == 0) {
			dijkstra();
		} else {
			assert (model.getNumCandidateTargetStates() == 1);
			recursiveEnumeration(model.getFirstCandidateTargetState(), k);
		}
		
		if (expandedTargets.isEmpty()) {
			// REA
			PathEntry candidate = getKthEntry(predList, model.getFirstCandidateTargetState(), k);
			if (candidate != null) {
				expandedTargets.add(candidate);
			}
		}

		PathEntry bestTargetEntry = null;
		for (PathEntry candidate : expandedTargets) {
			int trg = candidate.targetState;
			
			if (bestTargetEntry == null) {
				bestTargetEntry = candidate;
				continue;
			}

			if (pathComparator.compare(candidate, bestTargetEntry) > 0) {
				bestTargetEntry = candidate;
				log.println("Found entry for " + trg + ": " + candidate + " (NEW BEST CANDIDATE)", PrismLog.VL_ALL);
			} else {
				log.println("Found entry for " + trg + ": " + candidate, PrismLog.VL_ALL);
			}
		}

		if (bestTargetEntry == null) {
			return null;
		} else {
			return pathEntryToPath(bestTargetEntry, model);
		}
	}

	@Override
	public boolean hasNext()
	{
		//log.println("Trying to find path for k=" + k + "...", PrismLog.VL_ALL);
		if (nextPath != null)
			return true;

		try {
			nextPath = getKthShortestPath(k);
		} catch (PrismException e) {
			e.printStackTrace();
			nextPath = null;
		}

		return nextPath != null;
	}

	@Override
	public ProbabilisticPath next()
	{
		ProbabilisticPath result = null;
		if (nextPath != null) {
			// Next path has already been computed by call to hasNext()
			result = nextPath;
			nextPath = null;
		} else {
			// hasNext() has not been called, have to perform the actual computation here
			try {
				result = getKthShortestPath(k);
			} catch (PrismException e) {
				e.printStackTrace();
			}
		}

		/*if (result != null) {
			log.println("k=" + k + ": New path " + result, PrismLog.VL_ALL);
		}*/
		
		if (result != null && performPruning) {
			prunePath(result);
		}

		k++;
		return result;
	}

	private void prunePath(ProbabilisticPath result)
	{
		// Prune nodes added in normalization from paths
		if (model.hasNormalizedInitialState()) {
			assert (model.getStateType(result.getFirst().getSource()) == StateType.NEW_INITIAL);
			result.setInitialProbability(result.getFirst().getProbability());
			result.removeFirst();
		}
		if (model.hasNormalizedTargetState()) {
			assert (model.getStateType(result.getLast().getTarget()) == StateType.NEW_TARGET);
			result.removeLast();
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	private static PathEntry getKthEntry(ArrayList<ArrayList<PathEntry>> predList, int state, int k)
	{
		try {
			return predList.get(state).get(k);
		} catch (Exception e) {
			return null; // That's fine, the path need not exist (yet)
		}
	}

	private static int highestKnownK(ArrayList<ArrayList<PathEntry>> predList, int state)
	{
		assert (predList.get(state) != null);
		return predList.get(state).size();
	}

	private static void setKthEntry(ArrayList<ArrayList<PathEntry>> predList, int state, int k, PathEntry entry) throws PrismException
	{
		try {
			ArrayList<PathEntry> list = predList.get(state);
			// Make sure the list has the right capacity
			for (int j = list.size(); j <= k; j++)
				list.add(null);
			// Now we can safely set the corresponding entry
			list.set(k, entry);

			//System.out.println("Added 0th entry for " + state + " with probability " + entry.totalPathProb);
		} catch (Exception e) {
			throw new PrismException(k + "th candidate for " + state + " cannot be set. Uninitialized array list?");
		}
	}

	private void recursiveEnumeration(int targetState, int k) throws PrismException
	{
		// See "Computing the K Shortest Paths: A New Algorithm and an Experimental Comparison"
		// by V́ıctor M. Jiḿenez and Andŕes Marzal. The comments B.x correspond to their algorithm listing

		assert (k >= 1);

		log.println("Computing: recursiveEnumeration(" + targetState + "," + k + ")", PrismLog.VL_ALL);

		if (k == 1) {
			log.println("k = 1 => do step B1", PrismLog.VL_ALL);
			// B.1: The shortest paths to the direct predecessors of the target node
			// concatenated with the edge to our target node are the candidates for
			// the next path (skipping the actual shortest path)

			Iterator<Integer> preds = model.getCandidatePredecessorsIterator(targetState);
			int edgeCount = 0;
			while (preds.hasNext()) {
				int sourceState = preds.next();

				log.println("Considering: edge from " + sourceState + " to " + targetState, PrismLog.VL_ALL);

				// Ignore loop on the global target state
				if (sourceState == targetState && targetState == model.getFirstCandidateTargetState()) {
					log.println("Loop on target state -> skip", PrismLog.VL_ALL);
					continue;
				}

				// Don't insert the very shortest path into the list of candidates,
				// We already used that for k = 0
				if (sourceState == getKthEntry(predList, targetState, 0).predState) {
					log.println("Edge used in very shortest path -> skip", PrismLog.VL_ALL);
					continue;
				}

				log.println("Will add edge as candidate.", PrismLog.VL_ALL);

				PathEntry srcEntry = getKthEntry(predList, sourceState, 0);
				
				if (srcEntry == null) {
					// This is only possible if the state has become unreachable due to target normalization, because there was a sequence of target states in the model.
					// As long as we're not interested in bounded reachability, we can just skip this
					continue;
				}
				
				double prob = srcEntry.totalPathProb * model.getCandidateProbability(sourceState, targetState);
				PathEntry candidateEntry = new PathEntry(targetState, sourceState, srcEntry, prob, 1, srcEntry.stepsFromSource + 1);
				candidates.get(targetState).add(candidateEntry);
				edgeCount++;

				log.println(targetState + ": B1: Added 1st shortest path (or 2nd edge) from " + sourceState + ", prob: " + candidateEntry.totalPathProb,
						PrismLog.VL_ALL);
			}

			assert (edgeCount == candidates.get(targetState).size());
			if (edgeCount > 0) {
				log.println(targetState + ": B1: Computed " + edgeCount + " candidates for k=1", PrismLog.VL_ALL);
			} else
				log.println(targetState + ": B1: Recursion stops, no candidates found for k=1", PrismLog.VL_ALL);
		}

		// B.2 [the very shortest path to the input node has length 1, so we skip B.3-B.5 if the condition holds]
		if (!(k == 1 && targetState == initialState)) {
			log.println(targetState + ": B2: Conditions hold, execute B3 to B5", PrismLog.VL_ALL);
			// If pred is the predecessor of targetState in the (k-1)-th shortest path,
			// then the whole (k-1)-th path consists of the k'-th shortest path to pred (for some k' <= k-1)
			// and the edge from pred to targetState
			// Thus, if the (k'+1)-th path to pred exists, then this together with the pred-targetState-edge
			// is a candidate for the k-th shortest path to the targetState
			// In the following, we find this candidate and leave all other candidates unchanged

			// B.3: Get the path
			PathEntry entryKminus1 = getKthEntry(predList, targetState, k - 1);
			int pred = entryKminus1.predState;
			int kprime = entryKminus1.predEntry.k;

			log.println(targetState + ": B3: pred = " + pred + ", k' = " + kprime, PrismLog.VL_ALL);

			// B.4: Compute the next shortest path to pred if necessary
			if (highestKnownK(predList, pred) <= kprime || getKthEntry(predList, pred, kprime + 1) == null) {
				log.println(targetState + ": B4: have to compute " + (kprime + 1) + "-th shortest path for pred", PrismLog.VL_ALL);
				recursiveEnumeration(pred, kprime + 1);
			} else {
				log.println(targetState + ": B4: path for u (k = " + (kprime + 1) + ") already known: " + getKthEntry(predList, pred, kprime + 1),
						PrismLog.VL_ALL);
			}

			//B.5: If that path exists, it has been found by the recursive call and we have a new candidate
			if (highestKnownK(predList, pred) >= kprime + 1 && getKthEntry(predList, pred, kprime + 1) != null) {
				PathEntry predEntry = getKthEntry(predList, pred, kprime + 1);
				double candidateProb = model.getCandidateProbability(pred, targetState) * predEntry.totalPathProb;
				PathEntry newEntry = new PathEntry(targetState, pred, predEntry, candidateProb, k, predEntry.stepsFromSource + 1);
				candidates.get(targetState).add(newEntry);

				log.println(targetState + ": B5: Path exists, added candidate has prob: " + candidateProb, PrismLog.VL_ALL);
			} else {
				log.println(targetState + ": B5: no more paths from pred exist, no new candidate", PrismLog.VL_ALL);
			}
		}

		//B.6: If any candidates remain, take the best
		if (candidates.get(targetState).isEmpty()) {
			log.println(targetState + ": B6: No more paths for target node", PrismLog.VL_ALL);
		} else {
			PathEntry bestCandidate = candidates.get(targetState).poll();
			PathEntry kthShortestPathEntry = new PathEntry(bestCandidate.targetState, bestCandidate.predState, bestCandidate.predEntry,
					bestCandidate.totalPathProb, k, bestCandidate.stepsFromSource);

			setKthEntry(predList, targetState, k, kthShortestPathEntry);
			log.println(targetState + ": B6: have found kth shortest path: " + kthShortestPathEntry, PrismLog.VL_ALL);
		}
	}

	public List<ProbabilisticPath> getPathsToAllExpandedTargets() throws PrismException
	{
		dijkstra();
		
		ArrayList<ProbabilisticPath> result = new ArrayList<ProbabilisticPath>();
		
		for (PathEntry entry : expandedTargets) {
			ProbabilisticPath path = pathEntryToPath(entry, model);
			if (performPruning) prunePath(path);
			result.add(path);
		}
		
		return result;
	}

}
