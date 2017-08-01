package explicit.cex.gens;

import prism.PrismException;
import prism.PrismLog;
import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.CexSetOfPaths;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.util.CexParams;
import explicit.cex.util.CexStatistics.FailReason;

/**
 * This class generates "raw" counterexamples to a given set of target states, 
 * i.e. sets of paths whose cumulative probability exceeds a given threshold. 
 */
public class PathSetCexGenerator extends DTMCCexGenerator
{

	/** Number of paths to generate between successive polling for interrupts */
	private static final int CHECK_INTERRUPT_AFTER_PATHS = 100;
	/** Number of paths to generate between successive generation of stats */
	private static final int GENERATE_STATS_AFTER_PATHS = 5000;

	/**
	 * Generate counterexample for the given DTMC and expression, based on the given reachability probabilities and target set
	 * @param dtmc
	 * @param solution
	 * @return
	 * @throws PrismException Thrown if the set of all paths actually doesn't constitute a counterexample
	 */
	public PathSetCexGenerator(NormalizedDTMC dtmc, CexParams params, PrismLog log)
	{
		super(dtmc, params, log);
	}

	@Override
	public ProbabilisticCounterexample call() throws Exception
	{
		log.println("Thread for " + params + " has been started", PrismLog.VL_ALL);
		startTimer();
		
		CexSetOfPaths cex = new CexSetOfPaths(params, dtmc.getOriginalDTMC());
		partialResult = cex;

		try {
			// Depending on the params, we have a unique initial state or an initial distribution
			// In the case of an initial distribution, we should have a unique normalized initial state at this point
			ShortestPathFinder finder = new ShortestPathFinder(dtmc, CexParams.getNormalizedInitialState(params, dtmc), log);

			int pathCount = 0;
			
			while (!cex.probabilityMassExceedsThreshold() && finder.hasNext()) {
				// We don't have a counterexample yet & there is at least one more path --> add it
				ProbabilisticPath path = finder.next();
				log.println("Next path: " + path, PrismLog.VL_ALL);
				cex.add(path);
				pathCount++;

				if (shouldGenerateStats() && pathCount % GENERATE_STATS_AFTER_PATHS == 0) {
					setComputationTime(cex);
					setNewStats(cex);
				}

				if (pathCount % CHECK_INTERRUPT_AFTER_PATHS == 0) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
				}
				
			}

			if (!cex.probabilityMassExceedsThreshold()) {
				log.println("Search failed for " + params + ": No more paths", PrismLog.VL_HIGH);
				cex.setFailReason(FailReason.NO_MORE_PATHS);
			}

		} catch (InterruptedException e) {
			// That's fine, probably we've timed out. Just make sure that the partial result is accessible
			processInterrupt(cex, e);
		}
		setComputationTime(cex);
		
		if (cex.probabilityMassExceedsThreshold()) {
			log.println("Have computed complete counterexample of probability " + cex.getProbabilityMass());
		}
		
		return cex.probabilityMassExceedsThreshold() ? cex : null;
	}

	@Override
	public String toString() {
		return "k-Most-Probable Path Generator";
	}
	
}
