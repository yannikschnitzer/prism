package explicit.cex.gens;

import java.util.Comparator;
import java.util.List;

import prism.PrismException;
import prism.PrismLog;
import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.CriticalSubsystem;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.util.CexParams;
import explicit.cex.util.PathEntry;
import explicit.cex.util.CexStatistics.FailReason;

public class CritSysCexGenerator extends DTMCCexGenerator
{

	/** Number of paths to generate between regenerating the closure prob via model checking */
	private static final int DEFAULT_MC_INTERVAL = 5;
	/** Default (maximum) number of targets to expand per Dijkstra invocation */
	private static final int DEFAULT_NUM_TARGETS_TO_EXPAND = 5;

	/** Number of path fragments to generate between successive polling for interrupts */
	private static final int CHECK_INTERRUPT_AFTER_FRAGS = 10;
	/** Number of path fragments to generate between successive generation of stats */
	private static final int GENERATE_STATS_AFTER_FRAGS = 50;

	/** Number of path fragments to compute between closure computation (model-checking invocations) */
	private final int modelCheckingInterval;
	/** (Maximum) number of targets to expand per Dijkstra invocation */ 
	private final int numTargetsToExpand;
	

	/**
	 * Generate counterexample for the given DTMC and expression, based on the given reachability probabilities and target set
	 * @param dtmc
	 * @param solution
	 * @return
	 * @throws PrismException Thrown if the set of all paths actually doesn't constitute a counterexample
	 */
	public CritSysCexGenerator(NormalizedDTMC dtmc, CexParams params, PrismLog log)
	{
		this(dtmc, params, log, DEFAULT_MC_INTERVAL, DEFAULT_NUM_TARGETS_TO_EXPAND);
	}
	
	public CritSysCexGenerator(NormalizedDTMC dtmc, CexParams params, PrismLog log, int modelCheckingInterval)
	{
		this(dtmc, params, log, modelCheckingInterval, DEFAULT_NUM_TARGETS_TO_EXPAND);
	}
	
	public CritSysCexGenerator(NormalizedDTMC dtmc, CexParams params, PrismLog log, int modelCheckingInterval, int numTargetsToExpand)
	{
		super(dtmc, params, log);
		this.modelCheckingInterval = modelCheckingInterval;
		this.numTargetsToExpand = numTargetsToExpand;
	}

	@Override
	public ProbabilisticCounterexample call() throws Exception
	{
		CriticalSubsystem cs = null;
		startTimer();
		
		try {
			// The initial and target states change with each new path fragment, so we will make a new ShortestPathFinder in every iteration
			
			// To initialize the subsystem, we have to find the most probable path in the original (normalized) model
			ShortestPathFinder finder = new ShortestPathFinder(dtmc, CexParams.getNormalizedInitialState(params, dtmc), log, false, 1, null);
			
			// The first path must of course exist (since we assume there is a reachable target),
			// so we can now initialize the critical subsystem
			ProbabilisticPath firstPath = finder.next();
			log.println("First path: " + firstPath);
			cs = new CriticalSubsystem(params, this.dtmc, log, firstPath);
			partialResult = cs;
			int pathCount = 1;
			int dijkstraCount = 1;
			
			boolean isCex = cs.probabilityMassExceedsThreshold();
			log.println("Finished generation of first closure", PrismLog.VL_ALL);
			log.flush();
			boolean noMorePaths = false;
			
			// Now we can start the actual path fragment search
			while (!isCex && !noMorePaths) {
				
				if (dijkstraCount % modelCheckingInterval == 0) {
					cs.triggerClosureRecomputation();
				}

				if (shouldGenerateStats() && (dijkstraCount * numTargetsToExpand) % GENERATE_STATS_AFTER_FRAGS == 0) {
					setNewStats(cs);
				}

				if (dijkstraCount % CHECK_INTERRUPT_AFTER_FRAGS == 0) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
				}
				
				//double bestProb = 0;
				//ProbabilisticPath bestPath = null;
				
				long dijkstaStartTime = System.currentTimeMillis();
				
				// Get a new comparator based on previous model checking results
				@SuppressWarnings("unused")
				Comparator<PathEntry> pathComparator = cs.makeComparatorFromReachabilityProbs();
				
				/*ShortestPathFinder csSpf = new ShortestPathFinder(cs, ShortestPathFinder.SEARCH_FROM_ALL_INITIAL_STATES, log, false, numTargetsToExpand, null);
				if (csSpf.hasNext()) {
					ProbabilisticPath candidate = csSpf.next();
					if (candidate.getProbability() > bestProb) {
						bestProb = candidate.getProbability();
						bestPath = candidate;
					}
				}*/
				
				ShortestPathFinder csSpf = new ShortestPathFinder(cs, ShortestPathFinder.SEARCH_FROM_ALL_INITIAL_STATES, log, false, numTargetsToExpand, null);
				List<ProbabilisticPath> nextFragments = csSpf.getPathsToAllExpandedTargets();
				dijkstraCount++;
				long dijkstraEndTime = System.currentTimeMillis() - dijkstaStartTime;
				log.println("Finished Dijkstra's algorithm for " + cs.getNumCandidateInitialStates() + " initial states, returning " + nextFragments.size() + " fragments in " + ((double)dijkstraEndTime)/1000.0 + " sec", PrismLog.VL_ALL);
				
				if (nextFragments.isEmpty()) {
					noMorePaths = true;
				} else {
					for (ProbabilisticPath frag : nextFragments) {
						cs.addPathFragment(frag);
						log.println("Next path fragment: (no. " + pathCount + "): " + frag, PrismLog.VL_ALL);
						pathCount++;
					}
					isCex = (dijkstraCount % modelCheckingInterval == 0) ? cs.probabilityMassExceedsThreshold() : false;
					
				}
				
				//log.println(cs, PrismLog.VL_ALL);
			}

			if (!cs.probabilityMassExceedsThreshold()) {
				cs.setFailReason(FailReason.NO_MORE_PATHS);
				log.println("There are no more path fragments, will return");
			}

		} catch (InterruptedException e) {
			processInterrupt(cs, e);
		}
		
		setComputationTime(cs);
		
		return (cs != null && cs.probabilityMassExceedsThreshold()) ? cs : null;
	}
	
	@Override
	public String toString() {
		return "Local Search for Critical Subsystems";
	}

}
