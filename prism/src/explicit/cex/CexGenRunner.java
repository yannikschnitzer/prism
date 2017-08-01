package explicit.cex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import prism.PrismLog;
import explicit.Model;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.gens.CexGenerator;
import explicit.cex.util.CexStatistics;
import explicit.cex.util.SharedMutableVar;

/**
 * This class executes counterexample generators.
 * 
 * Execution is performed in separate threads and (partial) results are returned once complete or upon timeout.
 * Several counterexample searches can be run in parallel, to return when the first one finishes successfully.
 * This is useful for parallel search from multiple initial states.
 */
public class CexGenRunner
{
	/** Max. number of threads to schedule in parallel */
	public static final int NTHREADS = 8;

	/** List of the state */
	private List<SharedMutableVar<CexStatistics>> statsList = new ArrayList<>();

	private PrismLog log;

	public CexGenRunner(PrismLog log)
	{
		this.log = log;
	}
	
	public ProbabilisticCounterexample generateCex(CexSettings settings, Model model) {
		return null;
	}

	/**
	 * Runs the given generator in a separate thread to try and generate a counter example
	 * @param gen Generator to execute
	 * @param timeout Timeout after which to interrupt the generation if it is not yet complete
	 * @param printProgress Should we log the progress to provide regular feedback to the user?
	 * @return Complete counterexamle or null
	 */
	public ProbabilisticCounterexample generateCex(CexGenerator gen, long timeout, boolean printProgress)
	{
		List<CexGenerator> gens = new LinkedList<>();
		gens.add(gen);
		return generateAnyCex(gens, timeout, printProgress, 1);
	}

	/**
	 * Tries to generate a counter example via any of the given generators, running the default number of threads in parallel
	 * @param gens List of generators to execute (e.g. one per initial state of a DTMC)
	 * @param timeout Timeout after which to interrupt each generator
	 * @param printProgress Should we log the progress to provide regular feedback to the user?
	 * @return Complete counterexamle or null
	 */
	public ProbabilisticCounterexample generateAnyCex(List<CexGenerator> gens, long timeout, boolean printProgress)
	{
		return generateAnyCex(gens, timeout, printProgress, NTHREADS);
	}

	/**
	 * Tries to generate a counter example via any of the given generators
	 * @param gens List of generators to execute (e.g. one per initial state of a DTMC)
	 * @param timeout Timeout after which to interrupt each generator
	 * @param printProgress Should we log the progress to provide regular feedback to the user?
	 * @param numParallelThreads Maximal number of threads to run in parallel; set to 1 if sequential computation is desired
	 * @result Complete counterexamle or best partial result
	 */
	public ProbabilisticCounterexample generateAnyCex(List<CexGenerator> gens, long timeout, boolean printProgress, int numParallelThreads)
	{
		log.println("Starting counterexample generation for " + gens.size() + " generators with timeout " + timeout + "ms");
		
		final long startTime = System.currentTimeMillis();
		final long deadline = startTime + timeout;

		final ExecutorService executor = Executors.newFixedThreadPool(numParallelThreads);
		List<Future<ProbabilisticCounterexample>> tasks = new ArrayList<>();

		ProbabilisticCounterexample result = null;

		try {
			startComputation(gens, printProgress, executor, tasks);

			do {
				// Try to get result -- will be assigned null if the computation is still ongoing
				result = collectResults(tasks);

				// Print out statistics if applicable
				printStatistics(printProgress);

				// Then yield, since it doesn't make sense to immediately poll again
				Thread.yield();
			} while (System.currentTimeMillis() < deadline && result == null && stillRunning(tasks));

			// Try to get result once more -- might be outdated at this point
			if (result == null) result = collectResults(tasks);
		} catch (InterruptedException | ExecutionException e) {
			log.println("Caught exception: " + e.getMessage(), PrismLog.VL_ALL);
			e.printStackTrace();
		} finally {
			cleanUp(executor, tasks);

			printStatistics(printProgress);

			if (result == null) {
				log.println("Couldn't find counterexample using timeout of " + (timeout/1000.0) + "sec per generator. Will return best partial result among:", PrismLog.VL_HIGH);
				result = getBestPartialResult(gens);
			}
		}

		return result;
	}

	private boolean stillRunning(List<Future<ProbabilisticCounterexample>> tasks)
	{
		for (Future<ProbabilisticCounterexample> f : tasks) {
			if (!f.isDone()) return true;
		}
		return false;
	}

	/**
	 * Checks whether any of the shared vars has been updated with new stats
	 * @return True iff there are new stats
	 */
	private boolean hasNewStats()
	{
		for (SharedMutableVar<CexStatistics> stats : statsList) {
			if (stats.holdsNewData())
				return true;
		}
		return false;
	}

	/**
	 * Iff the first param is true, this prints all new statistics, i.e. current stats that haven't been printed yet.
	 * @param printProgress Should we actually do any printing?
	 * @param startTime Start time of the whole computation (for computing elapsed time)
	 */
	private void printStatistics(boolean printProgress)
	{
		if (printProgress && hasNewStats()) {
			for (int i = 0; i < statsList.size(); i++) {
				SharedMutableVar<CexStatistics> stats = statsList.get(i);
				if (stats.holdsNewData()) {
					log.println((statsList.size() > 1 ? "Thread " + i + ": " : "") + stats.get());
					log.flush();
				}
			}
		}
	}

	/**
	 * This method returns the best approximation for a counterexample computed so far.
	 * This should be called when the counterexample generation is aborted, to at least provide the best approximation to the user.
	 * @param gens Generators from which to extract the best approximation
	 * @param timeout
	 * @return
	 */
	private ProbabilisticCounterexample getBestPartialResult(List<CexGenerator> gens)
	{
		double maxProb = 0;
		int maxProbIndex = -1;
		for (int i = 0; i < gens.size(); i++) {
			ProbabilisticCounterexample cex = gens.get(i).forceResult();
			if (cex.getProbabilityMass() >= maxProb) {
				maxProb = cex.getProbabilityMass();
				maxProbIndex = i;
			}
		}

		for (int i = 0; i < gens.size(); i++) {
			log.println("* " + gens.get(i).forceResult().generateStats().toString() + (i == maxProbIndex ? " (BEST PARTIAL RESULT)" : ""), PrismLog.VL_HIGH);
		}

		return gens.get(maxProbIndex).forceResult();
	}

	/**
	 * Tries to immediately (i.e. without blocking) get a result from any one of the passed list of futures
	 * @param tasks Tasks to get a result from
	 * @return Complete counterexample or null
	 */
	private ProbabilisticCounterexample collectResults(List<Future<ProbabilisticCounterexample>> tasks) throws InterruptedException, ExecutionException
	{
		for (Future<ProbabilisticCounterexample> f : tasks) {
			if (f.isDone()) {
				//log.println("We're done, will try to return counterexample...", PrismLog.VL_ALL);
				ProbabilisticCounterexample cex = f.get();
				if (cex != null) {
					log.println("", PrismLog.VL_ALL);
					return cex;
				} else {
					//log.println(" Is null, will continue", PrismLog.VL_ALL);
				}
			}
		}

		return null;
	}

	/**
	 * Starts the counterexample generation for the given list of generators
	 * @param gens Counterexample generators to execute
	 * @param printProgress Should we print the progress to the log?
	 * @param executor
	 * @param list
	 */
	private void startComputation(List<CexGenerator> gens, boolean printProgress, final ExecutorService executor, List<Future<ProbabilisticCounterexample>> list)
	{
		for (CexGenerator gen : gens) {
			// Register statistics call back
			if (printProgress) {
				SharedMutableVar<CexStatistics> cellForStats = new SharedMutableVar<>(null);
				statsList.add(cellForStats);
				gen.setCallBack(cellForStats);
			}

			// Submit for computation
			Future<ProbabilisticCounterexample> submit = executor.submit(gen);
			list.add(submit);

			// A context switch makes sense now to actually run the computation
			Thread.yield();
		}
	}

	/**
	 * Performs the necessary cleanup work after the generation is finished / has timed out
	 * @param executor Executor to shut down
	 * @param tasks Tasks to cancel
	 */
	private void cleanUp(final ExecutorService executor, List<Future<ProbabilisticCounterexample>> tasks)
	{
		for (Future<ProbabilisticCounterexample> f : tasks) {
			f.cancel(true);
		}		

		statsList.clear();

		executor.shutdown();
		
		// Wait for the tasks to actually be canceled
		// This guarantess that the generators perform clenaup work (like setting fail reasons) before (partial) results are returned to the user 
		while (!executor.isTerminated()) {
			Thread.yield();
		}
	}

}