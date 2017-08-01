package explicit.cex.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import explicit.cex.BidirectionalDTMCWrapper;
import explicit.cex.NormalizedDTMC;
import explicit.cex.gens.BoundedPathFinder;
import explicit.cex.util.CexParams;
import prism.PrismLog;

@RunWith(JUnit4.class)
public class TestBoundedPathFinder
{

	private static final int VERBOSITY = CexTestSuite.applyGlobalLoggingRestrictions(PrismLog.VL_HIGH);

	@Test
	public void noPathsForSmallH()
	{
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);

		DTMCwithTargets triple = ExampleDTMCs.setOfPaths(1, 10, 1);
		int searchFrom = triple.dtmc.getFirstInitialState();
		for (int h = 0; h <= 12; h++) {
			System.out.println("----------------");
			System.out.println("NEW TEST: h = " + h);
			System.out.println("----------------");
			BidirectionalDTMCWrapper wrapper = new NormalizedDTMC(triple.dtmc, new CexParams(0d, h, searchFrom, "", triple.targets));
			BoundedPathFinder bpf = new BoundedPathFinder(wrapper, h, searchFrom, CexTestSuite.testLog);
	
			if (h < 9) {
				assertFalse(triple.description + ": Path from " + searchFrom + " with <= " + h + " hops was found", bpf.hasNext());	
			} else {
				assertTrue(triple.description + ": Path from " + searchFrom + " with <= " + h + " hops was *not* found", bpf.hasNext());
				bpf.next();
				assertFalse(triple.description + ": Found two paths", bpf.hasNext());
			}
			
			System.out.println(); System.out.println();
		}
	}
	
	@Test
	public void shorterPathsForHigherH() {
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);
		
		DTMCwithTargets triple = ExampleDTMCs.highProbabilityHighLength(5);
		double[] probs = new double[]{0, 0, 0.04, 0.08, 0.12, 0.16, 0.6};
		
		int searchFrom = triple.dtmc.getFirstInitialState();
		
		for (int h = 2; h <= 6; h++) {
			System.out.println("----------------");
			System.out.println("NEW TEST: h = " + h);
			System.out.println("----------------");
			BidirectionalDTMCWrapper wrapper = new NormalizedDTMC(triple.dtmc, new CexParams(0d, h, searchFrom, "", triple.targets));
			BoundedPathFinder bpf = new BoundedPathFinder(wrapper, h, searchFrom, CexTestSuite.testLog);
	
			assertTrue("There should be a path for h=" + h + ", but there isn't", bpf.hasNext());
			assertEquals(triple.description + ": Path from " + searchFrom + " with <= " + h + " hops has wrong probability ", probs[h], bpf.next().getProbability(), CexTestSuite.EPS);
			
			System.out.println(); System.out.println();
		}
	}

	@Test
	public void multipleInitialStates() {
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);
		
		DTMCwithTargets triple = ExampleDTMCs.reverseBinaryTreeWithSink(3, 0);
		
		for (int h = 1; h <= 4; h++) {
			System.out.println("----------------");
			System.out.println("NEW TEST: h = " + h);
			System.out.println("----------------");
			BidirectionalDTMCWrapper wrapper = new NormalizedDTMC(triple.dtmc, CexParams.makeFromDTMCAndTargets(triple.dtmc, "", triple.targets, h));
			BoundedPathFinder bpf = new BoundedPathFinder(wrapper, h, BoundedPathFinder.SEARCH_FROM_ALL_INITIAL_STATES, CexTestSuite.testLog);
			
			if (h < 3) {
				assertFalse(triple.description + ": Path with <= " + h + " hops was found", bpf.hasNext());	
			} else {
				assertTrue(triple.description + ": Path with <= " + h + " hops was *not* found", bpf.hasNext());
				bpf.next();
				for (int k = 1; k <= 7; k++) {
					assertTrue(triple.description + ": Path with <= " + h + " hops for k = " + k + " was *not* found", bpf.hasNext());
					bpf.next();
				}
				
				assertFalse(triple.description + ": Found too many paths", bpf.hasNext());
			}
			
			System.out.println(); System.out.println();
		}
	}
	
}
