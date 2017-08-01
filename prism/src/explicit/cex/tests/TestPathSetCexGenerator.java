package explicit.cex.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import prism.PrismLog;
import explicit.cex.CexGenRunner;
import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.gens.CexGenerator;
import explicit.cex.gens.PathSetCexGenerator;
import explicit.cex.util.CexParams;

// TODO: Reduce code duplication

@RunWith(JUnit4.class)
public class TestPathSetCexGenerator
{

	private static final int VERBOSITY = CexTestSuite.applyGlobalLoggingRestrictions(PrismLog.VL_HIGH);
	// 1 second should suffice for all the simple examples unless we log everything
	private static final long TIMEOUT = VERBOSITY == PrismLog.VL_ALL ? 10000 : 1000; 

	private static final DTMCwithTargets[] testDTMCsSingle = new DTMCwithTargets[] { ExampleDTMCs.setOfPaths(1, 10, 1), ExampleDTMCs.binaryTree(3, 4, 0.5),
			ExampleDTMCs.binaryTree(3, 4, 0.5), ExampleDTMCs.pathWithSingleLoop(10, 5, 0.5), ExampleDTMCs.grid(7, 1, 0.75), ExampleDTMCs.grid(8, 1, 0.75) };

	private static final double[] thresholdsSingle = new double[] { 1, 4 * Math.pow(0.5, 3), 5 * Math.pow(0.5, 3), 0.99, 1, 1 };
	private static final boolean[] expectedResultsSingle = new boolean[] { true, true, false, true, true, true };

	private static final DTMCwithTargets[] testDTMCsMultiple = new DTMCwithTargets[] { ExampleDTMCs.setOfPaths(2, 1000, 2),
			ExampleDTMCs.setOfPaths(2, 1000, 1), ExampleDTMCs.setOfPaths(5, 10000, 3) };

	private static final double[] thresholdsMultiple = new double[] { 1, 1, 1 };
	private static final boolean[] expectedResultsMultiple = new boolean[] { true, true, true };

	@Test
	public void testWithSingleInitials()
	{
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);
		
		assertEquals("Different length test data arrays", testDTMCsSingle.length, thresholdsSingle.length);
		assertEquals("Different length test data arrays", testDTMCsSingle.length, expectedResultsSingle.length);

		CexGenRunner runner = new CexGenRunner(CexTestSuite.testLog);

		for (int i = 0; i < testDTMCsSingle.length; i++) {
			DTMCwithTargets triple = testDTMCsSingle[i];
			CexParams params = new CexParams(thresholdsSingle[i], CexParams.UNBOUNDED, triple.dtmc.getFirstInitialState(), "", triple.targets);
			CexGenerator gen = new PathSetCexGenerator(new NormalizedDTMC(triple.dtmc, params), params, CexTestSuite.testLog);
			ProbabilisticCounterexample cex = runner.generateCex(gen, TIMEOUT, true);
			assertNotNull(triple.description + ": No (partial) counter example returned", cex);
			System.out.println(triple.description + ": " + cex.generateStats());
			assertEquals(triple.description + ": Unexpected result of counter example generation", expectedResultsSingle[i],
					cex.probabilityMassExceedsThreshold());
		}
	}

	@Test
	public void testWithMultipleInitials()
	{
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);
		
		assertEquals("Different length test data arrays", testDTMCsMultiple.length, thresholdsMultiple.length);
		assertEquals("Different length test data arrays", testDTMCsMultiple.length, expectedResultsMultiple.length);

		CexGenRunner runner = new CexGenRunner(CexTestSuite.testLog);

		for (int i = 0; i < testDTMCsMultiple.length; i++) {
			DTMCwithTargets triple = testDTMCsMultiple[i];

			ArrayList<CexGenerator> gens = new ArrayList<>();
			for (int initial : triple.dtmc.getInitialStates()) {
				System.out.println("Setting up counterexample generator for initial state " + initial);
				CexParams params = new CexParams(thresholdsMultiple[i], CexParams.UNBOUNDED, initial, "", triple.targets);
				CexGenerator gen = new PathSetCexGenerator(new NormalizedDTMC(triple.dtmc, params), params, CexTestSuite.testLog);
				gens.add(gen);
			}

			ProbabilisticCounterexample cex = runner.generateAnyCex(gens, TIMEOUT, true);
			assertNotNull(triple.description + ": No (partial) counter example returned", cex);
			System.out.println(triple.description + ": " + cex.generateStats());
			assertEquals(triple.description + ": Unexpected result of counter example generation", expectedResultsMultiple[i],
					cex.probabilityMassExceedsThreshold());
		}

		// Now let's test if the parallel computation works....
		ArrayList<CexGenerator> gens = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			DTMCwithTargets triple = ExampleDTMCs.grid(7, 1, 0.75);
			CexParams params = new CexParams(1, CexParams.UNBOUNDED, triple.dtmc.getFirstInitialState(), "", triple.targets);
			CexGenerator gen = new PathSetCexGenerator(new NormalizedDTMC(triple.dtmc, params), params, CexTestSuite.testLog);
			gens.add(gen);
		}

		ProbabilisticCounterexample cex = runner.generateAnyCex(gens, 5 * TIMEOUT, true);
		assertNotNull("Parallel grids: No (partial) counter example returned", cex);
		System.out.println("Result: " + cex.generateStats());
		assertTrue("Parallel grids: Unexpected result of counter example generation", cex.probabilityMassExceedsThreshold());
	}

}
