package explicit.cex.gens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import prism.PrismException;
import prism.PrismLog;
import explicit.cex.BidirectionalDTMCWrapper;
import explicit.cex.BidirectionalDTMCWrapper.StateType;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.util.CexParams;
import explicit.cex.util.PathEntry;
import explicit.cex.util.Transition;

public class BoundedPathFinder implements Iterator<ProbabilisticPath>
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
	
	/*private class UnboundedCandidateSet {
		
		private final ArrayList<PriorityQueue<PathEntry>> candidates;
		
		public UnboundedCandidateSet(int maxStateIndex) {
			
			candidates = new ArrayList<>(maxStateIndex+1);
			for (int j = 0; j <= maxStateIndex; j++) {
				candidates.add(new PriorityQueue<PathEntry>());
			}
			
		}
		
		public void addCandidate(int target, PathEntry candidate) {
			candidates.get(target).add(candidate);
		}
		
		public int numCandidates(int target) {
			return candidates.get(target).size();
		}

		public boolean hasNoCandidatesFor(int target)
		{
			return candidates.get(target).isEmpty();
		}

		public PriorityQueue<PathEntry> get(int target)
		{
			return candidates.get(target);
		}
	}*/
	
	private class PathSet {
		// Indices: h, s, k
		private final ArrayList<ArrayList<ArrayList<PathEntry>>> predList;
		
		public PathSet(int maxHops, int maxStateIndex) {
			log.println("Initializing path set for <= " + maxHops + " hops and states 0.." + maxStateIndex, PrismLog.VL_HIGH);
			predList = new ArrayList<>(maxHops+1);
			for (int i = 0; i <= maxHops; i++) {
			
				ArrayList<ArrayList<PathEntry>> pathsForH = new ArrayList<>(maxStateIndex+1);
				for (int j = 0; j <= maxStateIndex; j++) {
					pathsForH.add(new ArrayList<PathEntry>());
				}
				predList.add(pathsForH);
			
			}
		}
		
		public void setEntry(int s, int h, int k, PathEntry entry) {
			try {
				ArrayList<PathEntry> list = predList.get(h).get(s);
				// Make sure the list has the right capacity
				for (int j = list.size(); j <= k; j++)
					list.add(null);
				// Now we can safely set the corresponding entry
				list.set(k, entry);

				log.println("Added " + k + "th entry with <= " + h + " hops for state " + s + ": " + entry, PrismLog.VL_HIGH);
			} catch (Exception e) {
				throw new IllegalStateException(k + "th candidate for " + s + " (h = " + h + ") cannot be set. Uninitialized array list?");
			}
		}
		
		public PathEntry getEntry(int s, int h, int k) {
			try {
				return predList.get(h).get(s).get(k);
			} catch (Exception e) {
				return null; // That's fine, the path need not exist (yet)
			}
		}
		
		public int highestKnownK(int s, int h) {
			assert (predList.get(h).get(s) != null);
			return predList.get(h).get(s).size();
		}
	}
	
	private class CandidateSet {
		
		private final ArrayList<ArrayList<PriorityQueue<PathEntry>>> candidates;
		
		public CandidateSet(int maxHops, int maxStateIndex) {
			
			candidates = new ArrayList<>(maxHops+1);
			for (int i = 0; i <= maxHops; i++) {
			
				ArrayList<PriorityQueue<PathEntry>> candidatesForH = new ArrayList<>(maxStateIndex+1);
				for (int j = 0; j <= maxStateIndex; j++) {
					candidatesForH.add(new PriorityQueue<PathEntry>());
				}
				candidates.add(candidatesForH);
			
			}
		}
		
		public void addCandidate(int target, int h, PathEntry candidate) {
			candidates.get(h).get(target).add(candidate);
		}
		
		public int numCandidates(int target, int h) {
			return candidates.get(h).get(target).size();
		}

		public boolean hasNoCandidatesFor(int target, int h)
		{
			return candidates.get(h).get(target).isEmpty();
		}

		public PriorityQueue<PathEntry> get(int target, int h)
		{
			return candidates.get(h).get(target);
		}
	}

	/** DTMC to search paths in */
	private BidirectionalDTMCWrapper model;
	/** The unique initial state for the shortest path search */
	private final int initialState;
	/** Prune paths down to original DTMC states, i.e. remove states introduced through normalization */
	private final boolean performPruning;
	/** Abort Dijkstra's algorithm after expanding this many targets. This makes sense in the local search, where we don't need to perform a full Dijkstra */
	@SuppressWarnings("unused")
	private int numTargetsToExpand = EXPAND_ALL_TARGETS;
	/** Maximum number of hops in a path */
	private int stepBound = CexParams.UNBOUNDED;

	/** Paths for k and h */
	private PathSet pathSet = null;
	/** Candidate predecessor list for recursive enumeration algorithm */
	private CandidateSet candidates = null;
	
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
	public BoundedPathFinder(BidirectionalDTMCWrapper model, int stepBound, int initialState, PrismLog log)
	{
		this(model, stepBound, initialState, log, true, EXPAND_ALL_TARGETS, null);
	}

	public BoundedPathFinder(BidirectionalDTMCWrapper model, int stepBound, int initialState, PrismLog log, boolean performPruning)
	{
		this(model, stepBound, initialState, log, performPruning, EXPAND_ALL_TARGETS, null);
	}

	/**
	 * 
	 * @param model
	 * @param stepBound Max. number of steps (to perform in the original model, before init/target normalization)
	 * @param initialState
	 * @param log
	 * @param performPruning
	 * @param numTargetsToExpand
	 * @param pathComparator
	 */
	public BoundedPathFinder(BidirectionalDTMCWrapper model, int stepBound, int initialState, PrismLog log, boolean performPruning, int numTargetsToExpand,
			Comparator<PathEntry> pathComparator)
	{
		assert (model != null);
		this.model = model;
		// Normalization leads to additional steps as opposed to the original model. Have to modify step bound accordingly
		this.stepBound = stepBound + (model.hasNormalizedInitialState() ? 1 : 0) + (model.hasNormalizedTargetState() ? 1 : 0);
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
		// Prepare data structures for BF/REA algorithm 
		pathSet = new PathSet(stepBound, model.getMaxPossibleStateIndex());
		candidates = new CandidateSet(stepBound, model.getMaxPossibleStateIndex());
	}

	private void hopConstrainedBellmanFord() throws PrismException
	{
		assert (pathSet != null);

		log.println("Starting Bellman-Ford algorithm with step bound " + stepBound);

		if (initialState == SEARCH_FROM_ALL_INITIAL_STATES) {
			log.println("Will search from " + model.getNumCandidateInitialStates() + " sources", PrismLog.VL_ALL);
		} else {
			log.println("Will search from state " + initialState, PrismLog.VL_ALL);
		}
		
		// Actual computation
		for (int h = 0; h <= stepBound; h++) {
			log.println("Bellman-Ford algorithm for h <= " + h, PrismLog.VL_ALL);
			bellmanFord(h);
		}
	}
	
	private boolean isInitialState(int s) {
		if (initialState == SEARCH_FROM_ALL_INITIAL_STATES) {
			return model.isCandidateInitialState(s);
		} else {
			return s == initialState;
		}
	}
	
	private void bellmanFord(int h) {
		for (int s = 0; s <= model.getMaxPossibleStateIndex(); s++) {
		
			if (h == 0) {
				// Base case: Paths of length 0, i.e. source to source
				if (isInitialState(s)) {
					pathSet.setEntry(s, h, 0, new PathEntry(s, NO_PREDECESSOR, null, 1, 0, 0));
				} else {
					// No path of length 0
				}
			} else {
				// Inductive case: Paths of length h>0, created by extending paths of length h-1

				// Path of h-1 hops is also a candidate, we're looking for paths with length *at most* h
				PathEntry prevPath = pathSet.getEntry(s, h-1, 0);
				
				PathEntry bestEntry = prevPath;
				double bestProb = prevPath != null ? prevPath.totalPathProb : 0;
				
				Iterator<Integer> preds = model.getCandidatePredecessorsIterator(s);
				while (preds.hasNext()) {
					int pred = preds.next();
					PathEntry predEntry = pathSet.getEntry(pred, h-1, 0);
					if (predEntry != null) {
						double altProb = predEntry.totalPathProb * model.getCandidateProbability(pred, s);
						if (altProb > bestProb) {
							bestProb = altProb;
							bestEntry = new PathEntry(s, predEntry.targetState, predEntry, altProb, 0, h);
						}
					}
				}
				
				if (bestEntry != null) {
					pathSet.setEntry(s, h, 0, bestEntry);
				}
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

	private ProbabilisticPath getKthShortestPath(int h, int k) throws PrismException
	{
		expandedTargets.clear();
		if (k == 0) {
			hopConstrainedBellmanFord();
		} else {
			assert (model.getNumCandidateTargetStates() == 1);
			recursiveEnumeration(model.getFirstCandidateTargetState(), h, k);
		}
		
		if (expandedTargets.isEmpty()) {
			// REA
			PathEntry candidate = pathSet.getEntry(model.getFirstCandidateTargetState(), stepBound, k);
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
			nextPath = getKthShortestPath(stepBound, k);
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
				result = getKthShortestPath(stepBound, k);
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

	private void recursiveEnumeration(int targetState, int h, int k) throws PrismException
	{
		// See "Computing the K Shortest Paths: A New Algorithm and an Experimental Comparison"
		// by V́ıctor M. Jiḿenez and Andŕes Marzal. The comments B.x correspond to their algorithm listing
		// This is the hop-constrained variant presented in  "Counterexamples in Probabilistic Model Checking"
		// by Tingting Han and Joost-Pieter Katoen

		assert (k >= 1);

		log.println("Computing: recursiveEnumeration(" + targetState + "," + h + "," + k + ")", PrismLog.VL_ALL);

		if (h >= 1) {
			// We can only construct new candidates if we're allowed to perform at least 1 hop 
			
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
					PathEntry shortestPath = pathSet.getEntry(targetState, h, 0);
					if (shortestPath != null && sourceState == shortestPath.predState) {
						log.println("Edge used in very shortest path -> skip", PrismLog.VL_ALL);
						continue;
					}
	
					log.println("Will add edge as candidate.", PrismLog.VL_ALL);
	
					PathEntry srcEntry = pathSet.getEntry(sourceState, h-1, 0);
					
					if (srcEntry == null) {
						// If it's not possible to get to the source in h-1 hops,
						// then it's not possible to get to the target in h hops => skip
						continue;
					}
					
					double prob = srcEntry.totalPathProb * model.getCandidateProbability(sourceState, targetState);
					PathEntry candidateEntry = new PathEntry(targetState, sourceState, srcEntry, prob, 1, srcEntry.stepsFromSource + 1);
					candidates.addCandidate(targetState, h, candidateEntry);
					edgeCount++;
	
					log.println(targetState + ": B1: Added 1st shortest path (or 2nd edge) from " + sourceState + ", prob: " + candidateEntry.totalPathProb,
							PrismLog.VL_ALL);
				}
	
				assert (edgeCount == candidates.numCandidates(targetState, h));
				if (edgeCount > 0) {
					log.println(targetState + ": B1: Computed " + edgeCount + " candidates for k=1", PrismLog.VL_ALL);
				} else
					log.println(targetState + ": B1: Recursion stops, no candidates found for k=1", PrismLog.VL_ALL);
			}
	
			// B.2 [the very shortest path to the input node has length 1, so we skip B.3-B.5 if the condition holds]
			// TODO: Do we need this condition
			if (!(k == 1 && targetState == initialState)) {
				log.println(targetState + ": B2: Conditions hold, execute B3 to B5", PrismLog.VL_ALL);
				// If pred is the predecessor of targetState in the (k-1)-th shortest path,
				// then the whole (k-1)-th path consists of the k'-th shortest path to pred (for some k' <= k-1)
				// and the edge from pred to targetState
				// Thus, if the (k'+1)-th path to pred exists, then this together with the pred-targetState-edge
				// is a candidate for the k-th shortest path to the targetState
				// In the following, we find this candidate and leave all other candidates unchanged
	
				// B.3: Get the path, if it exists
				PathEntry entryKminus1 = pathSet.getEntry(targetState, h, k-1);
				if (entryKminus1 != null && entryKminus1.predEntry != null) {
					int pred = entryKminus1.predState;
					int kprime = entryKminus1.predEntry.k;
		
					log.println(targetState + ": B3: pred = " + pred + ", k' = " + kprime, PrismLog.VL_ALL);
		
					// B.4: Compute the next shortest path to pred if necessary
					if (pathSet.highestKnownK(pred, h-1) <= kprime || pathSet.getEntry(pred, h-1, kprime + 1) == null) {
						log.println(targetState + ": B4: have to compute " + (kprime + 1) + "-th shortest path for pred", PrismLog.VL_ALL);
						recursiveEnumeration(pred, h-1, kprime + 1);
					} else {
						log.println(targetState + ": B4: path for u (k = " + (kprime + 1) + ") already known: " + pathSet.getEntry(pred, h-1, kprime + 1),
								PrismLog.VL_ALL);
					}
		
					//B.5: If that path exists, it has been found by the recursive call and we have a new candidate
					if (pathSet.highestKnownK(pred, h-1) >= kprime + 1 && pathSet.getEntry(pred, h-1, kprime + 1) != null) {
						PathEntry predEntry = pathSet.getEntry(pred, h-1, kprime + 1);
						double candidateProb = model.getCandidateProbability(pred, targetState) * predEntry.totalPathProb;
						PathEntry newEntry = new PathEntry(targetState, pred, predEntry, candidateProb, k, predEntry.stepsFromSource + 1);
						candidates.addCandidate(targetState, h, newEntry);
		
						log.println(targetState + ": B5: Path exists, added candidate has prob: " + candidateProb, PrismLog.VL_ALL);
					} else {
						log.println(targetState + ": B5: no more paths from pred exist, no new candidate", PrismLog.VL_ALL);
					}
				}
			}
		}

		//B.6: If any candidates remain, take the best
		if (candidates.hasNoCandidatesFor(targetState, h)) {
			log.println(targetState + ": B6: No more paths for target node", PrismLog.VL_ALL);
		} else {
			PathEntry bestCandidate = candidates.get(targetState, h).poll();
			PathEntry kthShortestPathEntry = new PathEntry(bestCandidate.targetState, bestCandidate.predState, bestCandidate.predEntry,
					bestCandidate.totalPathProb, k, bestCandidate.stepsFromSource);

			pathSet.setEntry(targetState, h, k, kthShortestPathEntry);
			log.println(targetState + ": B6: have found kth shortest path: " + kthShortestPathEntry, PrismLog.VL_ALL);
		}
	}
	
}
