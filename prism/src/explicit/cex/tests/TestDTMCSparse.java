package explicit.cex.tests;

import static org.junit.Assert.*;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import explicit.DTMCSimple;
import explicit.DTMCSparse;

@RunWith(JUnit4.class)
public class TestDTMCSparse
{

	private static final DTMCwithTargets[] testDTMCs = new DTMCwithTargets[] { ExampleDTMCs.setOfPaths(1, 10, 1), ExampleDTMCs.setOfPaths(2, 10, 1),
		ExampleDTMCs.setOfPaths(2, 10, 2), ExampleDTMCs.binaryTree(3, 3, 0.5), ExampleDTMCs.grid(5, 3, 0.5), ExampleDTMCs.reverseBinaryTreeWithSink(3, 0.1) };

	@Test
	public void constructorAndAccessors()
	{
		for (DTMCwithTargets triple : testDTMCs) {
			DTMCSimple simple = triple.dtmc;
			DTMCSparse sparse = new DTMCSparse(simple);

			// General properties
			assertEquals("Model type changed", simple.getModelType(), sparse.getModelType());
			assertEquals("Constant values changed", simple.getConstantValues(), sparse.getConstantValues());

			assertEquals("Number of states changed", simple.getNumStates(), sparse.getNumStates());
			assertEquals("Number of transitions changed", simple.getNumTransitions(), sparse.getNumTransitions());

			// Successors / Transitions
			for (int i = 0; i < simple.getNumStates(); i++) {
				assertEquals("Number of transitions for state " + i + " changed", simple.getNumTransitions(i), sparse.getNumTransitions(i));

				Set<Integer> succSimple = new TreeSet<>();
				Set<Integer> succSparse = new TreeSet<>();
				for (Iterator<Integer> it = simple.getSuccessorsIterator(i); it.hasNext(); )
					succSimple.add(it.next());
				for (Iterator<Integer> it = sparse.getSuccessorsIterator(i); it.hasNext(); )
					succSparse.add(it.next());
				assertArrayEquals("Successors for state " + i + " changed", succSimple.toArray(), succSparse.toArray());

				BitSet positive = bitsetFromSet(succSimple);
				BitSet negative = getComplement(positive);
				assertTrue(sparse.allSuccessorsInSet(i, positive));
				assertTrue(sparse.someSuccessorsInSet(i, positive));
				assertFalse(sparse.someSuccessorsInSet(i, negative));
				assertFalse(sparse.allSuccessorsInSet(i, negative));

				Set<Entry<Integer,Double>> transSimple = new TreeSet<>(new EntryComparator<Integer,Double>());
				Set<Entry<Integer,Double>> transSparse = new TreeSet<>(new EntryComparator<Integer,Double>());
				for (Iterator<Entry<Integer,Double>> it = simple.getTransitionsIterator(i); it.hasNext(); )
					transSimple.add(it.next());
				for (Iterator<Entry<Integer,Double>> it = sparse.getTransitionsIterator(i); it.hasNext(); )
					transSparse.add(it.next());
				assertArrayEquals("Transitions for state " + i + " changed", transSimple.toArray(), transSparse.toArray());

				for (int j = 0; j < simple.getNumStates(); j++) {
					assertEquals("Different successors for states " + i + " / " + j, simple.isSuccessor(i, j), sparse.isSuccessor(i, j));
				}
			}

			// Initial states
			assertEquals("Num initial states changed", simple.getNumInitialStates(), sparse.getNumInitialStates());
			Set<Integer> initSimple = new TreeSet<>();
			Set<Integer> initSparse = new TreeSet<>();
			for (int i : simple.getInitialStates()) initSimple.add(i);
			for (int i : sparse.getInitialStates()) initSparse.add(i);
			assertArrayEquals("Initial states changed", initSimple.toArray(), initSparse.toArray());

			// Deadlocks
			assertEquals("Num deadlocks changed", simple.getNumDeadlockStates(), sparse.getNumDeadlockStates());
			Set<Integer> deadlocksSimple = new TreeSet<>();
			Set<Integer> deadlocksSparse = new TreeSet<>();
			for (int i : simple.getDeadlockStates()) deadlocksSimple.add(i);
			for (int i : sparse.getDeadlockStates()) deadlocksSparse.add(i);
			assertArrayEquals("Deadlock states changed", deadlocksSimple.toArray(), deadlocksSparse.toArray());
		}
	}

	private static BitSet bitsetFromSet(Set<Integer> set) {
		BitSet result = new BitSet();
		for (int s : set) result.set(s);
		return result;
	}

	private static BitSet getComplement(BitSet set) {
		BitSet result = new BitSet();
		result.set(0, set.length());
		result.andNot(set);
		return result;
	}

	public class EntryComparator<K extends Comparable<K>,V> implements Comparator<Entry<K, V>>
	{

		@Override
		public int compare(Entry<K, V> o1, Entry<K, V> o2)
		{
			return o1.getKey().compareTo(o2.getKey());
		}

	}


}
