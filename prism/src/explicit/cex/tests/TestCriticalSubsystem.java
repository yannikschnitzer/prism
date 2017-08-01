package explicit.cex.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import prism.PrismLog;
import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.CriticalSubsystem;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.gens.ShortestPathFinder;
import explicit.cex.util.CexParams;

@RunWith(JUnit4.class)
public class TestCriticalSubsystem
{

	private static final DTMCwithTargets[] singleSourcetestDTMCs = new DTMCwithTargets[] { ExampleDTMCs.setOfPaths(1, 10, 1),
			ExampleDTMCs.pathWithSingleLoop(10, 5, 0.5), ExampleDTMCs.binaryTree(3, 4, 0.5) };
	private static final double[][] pathProbs = new double[][] { { 1 }, { 1 }, { 0.125, 0.25, 0.375, 0.5 } };
	private static final int VERBOSITY = CexTestSuite.applyGlobalLoggingRestrictions(PrismLog.VL_ALL);

	@Test
	public void singleSourceDTMCs()
	{
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);
		
		assertEquals("Malformed test data", singleSourcetestDTMCs.length, pathProbs.length);

		for (int i = 0; i < singleSourcetestDTMCs.length; i++) {
			DTMCwithTargets triple = singleSourcetestDTMCs[i];
			CexParams params = new CexParams(1, CexParams.UNBOUNDED, triple.dtmc.getFirstInitialState(), "", triple.targets);
			NormalizedDTMC dtmc = new NormalizedDTMC(triple.dtmc, params);

			// Find first path via usual shortest path search
			ShortestPathFinder spf = new ShortestPathFinder(dtmc, dtmc.getFirstInitialState(), CexTestSuite.testLog, false);

			assertTrue(triple.description + ": Can't find initial path fragment for subsystem", spf.hasNext());
			ProbabilisticPath path = spf.next();
			System.out.println(path);
			assertTrue(triple.description + ": Initial path does not begin with an initial state", dtmc.isInitialState(path.getFirst().getSource()));
			assertTrue(triple.description + ": Initial path does not end in a target state", dtmc.isCandidateTargetState(path.getLast().getTarget()));

			System.out.println(path);

			CriticalSubsystem cs = new CriticalSubsystem(params, dtmc, CexTestSuite.testLog, path);

			System.out.println(dtmc);
			System.out.println("----------------------");
			System.out.println(cs);
			assertEquals(triple.description + ": Wrong probability of initial path", pathProbs[i][0], cs.getProbabilityMass(), CexTestSuite.EPS);
			// TODO: Fix this?
//			assertEquals(triple.description + ": Wrong number of initial states", path.getLength() + 1, cs.getNumCandidateInitialStates());
//			int numInit = 0;
//			for (@SuppressWarnings("unused")
//			int init : cs.getCandidateInitialStates())
//				numInit++;
//			assertEquals(triple.description + ": Wrong number of initial states", numInit, path.getLength() + 1);

			// Find remaining paths, if applicable, and then that there aren't any more
			for (int j = 1; j <= pathProbs[i].length; j++) {

				double bestProb = 0;
				ProbabilisticPath bestPath = null;

				for (ShortestPathFinder csSpf : cs.makeShortestPathFinders()) {
					System.out.println("Processing " + csSpf.getSource() + "... ");
					if (csSpf.hasNext()) {
						ProbabilisticPath candidate = csSpf.next();
						if (candidate.getProbability() > bestProb) {
							bestProb = candidate.getProbability();
							bestPath = candidate;
						}
						System.out.println("Found " + candidate + " (prob " + candidate.getProbability() + ")");
					}
				}

				if (j < pathProbs[i].length) {
					assertNotNull(triple.description + ": Didn't find another path", bestPath);

					System.out.println("Best candidate: " + bestPath + " (prob " + bestPath.getProbability() + ")");
					cs.addPathFragment(bestPath);
					cs.triggerClosureRecomputation();
					assertEquals(triple.description + ": Wrong probability of critical subsystem #" + j, pathProbs[i][j], cs.getProbabilityMass(),
							CexTestSuite.EPS);
				} else {
					assertNull(triple.description + ": Did find another path, but shouldn't", bestPath);
				}
			}

		}

	}

}
