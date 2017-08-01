package explicit.cex.tests;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import prism.PrismLog;
import prism.PrismPrintStreamLog;
import explicit.cex.BidirectionalDTMCWrapper;
import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.gens.ShortestPathFinder;
import explicit.cex.util.CexParams;
import explicit.cex.util.PathEntry;
import explicit.cex.util.PriorityQueueWithDecreasableKeys;
import explicit.cex.util.Transition;

@RunWith(JUnit4.class)
public class TestShortestPathFinder
{

	private static final DTMCwithTargets[] dijkstraTestDTMCs = new DTMCwithTargets[] { ExampleDTMCs.setOfPaths(1, 10, 1), ExampleDTMCs.setOfPaths(2, 10, 1),
			ExampleDTMCs.binaryTree(3, 1, 0.5), ExampleDTMCs.binaryTree(3, 4, 0.5), ExampleDTMCs.pathWithSingleLoop(10, 5, 0.5),
			ExampleDTMCs.reverseBinaryTreeWithSink(3, 0.1), ExampleDTMCs.grid(11, 1, 0.75) };
	private static final double[] dijkstraFromSingleStateResults = new double[] { 1, 1, Math.pow(0.5, 3), Math.pow(0.5, 3), 0.5, Math.pow(0.9, 3),
			Math.pow(0.75, 10) };
	private static final double[] dijkstraFromUniformDistResults = new double[] { 1, 0.5, Math.pow(0.5, 3), Math.pow(0.5, 3), 0.5,
			Math.pow(0.5, 3) * Math.pow(0.9, 3), Math.pow(0.75, 10) };
	
	private static final int VERBOSITY = CexTestSuite.applyGlobalLoggingRestrictions(PrismLog.VL_HIGH);

	/**
	 * Only the target state and the probability are relevant for the behavior of the queue
	 * @param trgState
	 * @param prob
	 * @return
	 */
	private static PathEntry mkPathEntry(int state, double prob)
	{
		return new PathEntry(state, -1, null, prob, 0, 0);
	}

	@Test
	public void priorityQueueOnPathEntries()
	{
		PriorityQueueWithDecreasableKeys<PathEntry> queue = new PriorityQueueWithDecreasableKeys<PathEntry>();
		int i = 0;
		queue.add(mkPathEntry(i++, 0.5));
		queue.add(mkPathEntry(i++, 0.6));
		PathEntry toBeDecreased = mkPathEntry(i++, 0.4);
		queue.add(toBeDecreased);
		PathEntry toBeDecreased2 = mkPathEntry(i++, 0.3);
		queue.add(toBeDecreased2);

		assertEquals("Did not remove item with highest probability. ", 0.6, queue.poll().totalPathProb, CexTestSuite.EPS);
		assertEquals("Poll did not remove item", i - 1, queue.size());

		queue.add(mkPathEntry(i++, 0.7));
		toBeDecreased.totalPathProb = 0.9;
		queue.decreaseKey(toBeDecreased);
		queue.add(mkPathEntry(i++, 0.6));
		toBeDecreased2.totalPathProb = 0.8;
		queue.decreaseKey(toBeDecreased2);

		for (int j = 9; j >= 5; j--) {
			assertEquals("Did not remove item with highest probability (decrease key buggy?) ", j / 10d, queue.poll().totalPathProb, CexTestSuite.EPS);
		}
	}

	@Test
	public void dijkstraFromSingleState()
	{
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);

		assertEquals("Messed up test case definition...", dijkstraTestDTMCs.length, dijkstraFromSingleStateResults.length);

		for (int i = 0; i < dijkstraTestDTMCs.length; i++) {
			DTMCwithTargets triple = dijkstraTestDTMCs[i];
			// Will search from specific initial state
			int searchFrom = triple.dtmc.getFirstInitialState();
			BidirectionalDTMCWrapper wrapper = new NormalizedDTMC(triple.dtmc, new CexParams(0d, CexParams.UNBOUNDED, searchFrom, "", triple.targets));
			ShortestPathFinder spf = new ShortestPathFinder(wrapper, searchFrom, CexTestSuite.testLog);

			assertTrue(triple.description + ": Shortest path from " + searchFrom + " to targets couldn't be found", spf.hasNext());
			ProbabilisticPath shortestPath = spf.next();
			//System.out.println(triple.description + ": Found shortest path of prob " + shortestPath.getProbability());
			assertNotNull(triple.description + ": Returned null as shortest path", shortestPath);
			assertEquals(triple.description + ": Wrong probability of shortest path", dijkstraFromSingleStateResults[i], shortestPath.getProbability(), CexTestSuite.EPS);
		}
	}

	@Test
	public void dijkstraFromDistribution()
	{
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);

		assertEquals("Messed up test case definition...", dijkstraTestDTMCs.length, dijkstraFromSingleStateResults.length);

		for (int i = 0; i < dijkstraTestDTMCs.length; i++) {
			DTMCwithTargets triple = dijkstraTestDTMCs[i];
			BidirectionalDTMCWrapper wrapper = new NormalizedDTMC(triple.dtmc, CexParams.makeFromDTMCAndTargets(triple.dtmc, "", triple.targets));
			ShortestPathFinder spf = new ShortestPathFinder(wrapper, wrapper.getFirstCandidateInitialState(), CexTestSuite.testLog);

			System.out.println(triple.description + "; first target " + triple.targets.nextSetBit(0));
			assertTrue(triple.description + ": Shortest path from " + triple.dtmc.getNumInitialStates()
					+ " uniformly distributed initial states to targets couldn't be found", spf.hasNext());
			ProbabilisticPath shortestPath = spf.next();
			//System.out.println(triple.description + ": Found shortest path of prob " + shortestPath.getProbability());
			assertNotNull(triple.description + ": Returned null as shortest path", shortestPath);
			assertEquals(triple.description + ": Wrong probability of shortest path", dijkstraFromUniformDistResults[i], shortestPath.getProbability(), CexTestSuite.EPS);
			checkLegal(triple, shortestPath);
		}
	}

	@Test
	public void recursiveEnumeration()
	{
		PrismLog log = new PrismPrintStreamLog(System.out);
		//log.setVerbosityLevel(PrismLog.VL_ALL);

		// For the single path, there is only one shortest path
		DTMCwithTargets triple = ExampleDTMCs.setOfPaths(1, 10, 1);
		System.out.println(triple.description);
		BidirectionalDTMCWrapper wrapper = new NormalizedDTMC(triple.dtmc, CexParams.makeFromDTMCAndTargets(triple.dtmc, "", triple.targets));
		ShortestPathFinder spf = new ShortestPathFinder(wrapper, wrapper.getFirstCandidateInitialState(), log);
		assertTrue(triple.description + ": Shortest path does not exist", spf.hasNext());
		spf.next();
		assertFalse(triple.description + ": 2nd shortest path exists, but shouldn't", spf.hasNext());

		// For the single path with a 0.5 loop, there are infinitely many paths, each halving in probability
		// We'll check the first 10
		triple = ExampleDTMCs.pathWithSingleLoop(10, 5, 0.5);
		System.out.println(triple.description);
		wrapper = new NormalizedDTMC(triple.dtmc, CexParams.makeFromDTMCAndTargets(triple.dtmc, "", triple.targets));
		spf = new ShortestPathFinder(wrapper, wrapper.getFirstCandidateInitialState(), log);
		for (int i = 1; i <= 10; i++) {
			assertTrue(triple.description + ": Path does not exist for k = " + (i - 1), spf.hasNext());
			ProbabilisticPath path = spf.next();
			assertEquals(triple.description + ": Path has wrong probability", Math.pow(2, -i), path.getProbability(), CexTestSuite.EPS);
		}

		// A grid with somewhat skewed probabilities
		// There should be exactly 70 paths through this grid. We'll check all of them
		double upProb = 0.75;
		triple = ExampleDTMCs.grid(5, 1, upProb);

		// Gather the expected probabilities
		PriorityQueue<Double> pathProbs = new PriorityQueue<>(70, new ProbComp());
		fillQueue(pathProbs, 5, 8, 1, 0, 0, upProb);
		assertEquals(triple.description + ": Unexpected number of paths through grid", 70, pathProbs.size());

		System.out.println(triple.description);
		wrapper = new NormalizedDTMC(triple.dtmc, CexParams.makeFromDTMCAndTargets(triple.dtmc, "", triple.targets));
		spf = new ShortestPathFinder(wrapper, wrapper.getFirstCandidateInitialState(), log);
		for (int i = 1; i <= 70; i++) {
			assertTrue(triple.description + ": Path does not exist for k = " + (i - 1), spf.hasNext());
			ProbabilisticPath path = spf.next();
			assertEquals(triple.description + ": Path for k = " + (i - 1) + " has wrong probability", pathProbs.poll().doubleValue(), path.getProbability(),
					CexTestSuite.EPS);
		}

		assertFalse(triple.description + ": Path for k=256 exists, but shouldn't", spf.hasNext());

	}

	private void fillQueue(PriorityQueue<Double> pathProbs, int sideLength, int remainingSteps, double accum, int upStepsSoFar, int rightStepsSoFar,
			double upProb)
	{
		if (remainingSteps == 0) {
			pathProbs.add(accum);
		} else {
			if (upStepsSoFar < sideLength - 1 && rightStepsSoFar < sideLength - 1) {
				fillQueue(pathProbs, sideLength, remainingSteps - 1, accum * upProb, upStepsSoFar + 1, rightStepsSoFar, upProb);
				fillQueue(pathProbs, sideLength, remainingSteps - 1, accum * (1 - upProb), upStepsSoFar, rightStepsSoFar + 1, upProb);
			} else if (upStepsSoFar == sideLength - 1)
				fillQueue(pathProbs, sideLength, remainingSteps - 1, accum, upStepsSoFar, rightStepsSoFar + 1, upProb);
			else
				fillQueue(pathProbs, sideLength, remainingSteps - 1, accum, upStepsSoFar + 1, rightStepsSoFar, upProb);
		}

	}

	private void checkLegal(DTMCwithTargets triple, ProbabilisticPath shortestPath)
	{
		assertTrue(triple.description + ": Path does not start in initial state", triple.dtmc.isInitialState(shortestPath.getFirst().getSource()));
		assertTrue(triple.description + ": Path does not end in target state", triple.targets.get(shortestPath.getLast().getTarget()));

		Iterator<Transition> it = shortestPath.getTransitionIterator();
		while (it.hasNext()) {
			Transition t = it.next();
			assertTrue(triple.description + ": Transition " + t + " does not exist", triple.dtmc.isSuccessor(t.getSource(), t.getTarget()));
		}
	}

	private class ProbComp implements Comparator<Double>
	{

		@Override
		public int compare(Double arg0, Double arg1)
		{
			return -(arg0.compareTo(arg1));
		}

	}

}
