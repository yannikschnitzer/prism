package explicit.cex.tests;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map.Entry;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import explicit.DTMCSimple;
import explicit.cex.NormalizedDTMC;
import explicit.cex.util.CexParams;

@RunWith(JUnit4.class)
public class TestNormalizedDTMC
{

	private static final DTMCwithTargets[] testDTMCs = new DTMCwithTargets[] { ExampleDTMCs.setOfPaths(1, 10, 1), ExampleDTMCs.setOfPaths(2, 10, 1),
			ExampleDTMCs.setOfPaths(2, 10, 2), ExampleDTMCs.binaryTree(3, 3, 0.5), ExampleDTMCs.grid(5, 3, 0.5), ExampleDTMCs.reverseBinaryTreeWithSink(3, 0.1) };

	@Test
	public void initialStatesNormalization()
	{

		for (DTMCwithTargets triple : testDTMCs) {
			DTMCSimple originalDTMC = triple.dtmc;
			//NormalizedDTMC normalized = new NormalizedDTMC(originalDTMC, "", triple.targets);
			NormalizedDTMC normalized = new NormalizedDTMC(originalDTMC, CexParams.makeFromDTMCAndTargets(originalDTMC, "", triple.targets));

			// We must have a unique initial state now
			assertEquals(triple.description + ": Wrong number of initial states", 1, normalized.getNumInitialStates());

			if (originalDTMC.getNumInitialStates() == 1) {
				// Make sure we only add an initial state if there isn't already a unique one
				assertEquals(triple.description + ": Already unique initial state, but still changed", originalDTMC.getFirstInitialState(),
						normalized.getFirstInitialState());
			} else {
				// If there were multiple initial states, they should now be uniformly distributed
				assertEquals(triple.description + ": Wrong probability of reaching old initial states", 1d / triple.dtmc.getNumInitialStates(),
						normalized.getCandidateProbability(normalized.getFirstInitialState(), originalDTMC.getFirstInitialState()), CexTestSuite.EPS);

				// And there should be a corresponding transition distribution available
				Iterator<Entry<Integer, Double>> transitions = normalized.getTransitionsIterator(normalized.getFirstInitialState());
				assertTrue("No outgoing transitions from new initial state", transitions.hasNext());
				while (transitions.hasNext()) {
					assertTrue("New initial state has transitions to non-initial state", originalDTMC.isInitialState(transitions.next().getKey()));
				}
			}
		}
	}

	@Test
	public void targetStatesNormalization()
	{
		for (DTMCwithTargets triple : testDTMCs) {
			DTMCSimple originalDTMC = triple.dtmc;
			BitSet originalTargets = triple.targets;

			NormalizedDTMC normalized = new NormalizedDTMC(originalDTMC, CexParams.makeFromDTMCAndTargets(originalDTMC, "", triple.targets));

			if (originalTargets.cardinality() > 1) {
				// Multiple targets initially, check that we have a unique one now,
				// to which all the original targets are connected
				int target = normalized.getTargetState();

				for (int i = originalTargets.nextSetBit(0); i >= 0; i = originalTargets.nextSetBit(i + 1)) {
					assertTrue(triple.description + ": Original target " + i + " doesn't have new target " + target + " as successor",
							normalized.isSuccessor(i, target));

					Iterator<Integer> succs = normalized.getSuccessorsIterator(i);
					assertTrue(triple.description + ": Original target " + i + " has empty successor iterator", succs.hasNext());

					while (succs.hasNext()) {
						assertEquals(triple.description + ": Original target " + i + " has wrong successor", target, succs.next().intValue());
					}
				}
			} else {
				assertEquals(triple.description + ": Already unique target, but still changes", originalTargets.nextSetBit(0), normalized.getTargetState());
			}
		}
	}

	@Test
	public void predecessorsMirrorSuccessors()
	{
		for (DTMCwithTargets triple : testDTMCs) {
			DTMCSimple originalDTMC = triple.dtmc;
			NormalizedDTMC normalized = new NormalizedDTMC(originalDTMC, CexParams.makeFromDTMCAndTargets(originalDTMC, "", triple.targets));

			for (int src = 0; src < normalized.getNumStates(); src++) {
				for (int trg = 0; trg < normalized.getNumStates(); trg++) {
					assertEquals("", normalized.isSuccessor(src, trg), normalized.isPredecessor(trg, src));
				}

			}
		}
	}
}
