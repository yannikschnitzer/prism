//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham/Oxford)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package explicit;

import java.util.*;

import parser.State;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismNotSupportedException;

/**
 * Class to perform bisimulation minimisation for explicit-state models.
 */
public class Bisimulation<Value> extends PrismComponent
{
	// Local storage of partition info
	protected int numStates;
	protected int[] partition;
	protected int numBlocks;
	protected MDPSimple<Value> mdp;

	private static final class ChoiceSig<V> {
		final Object action;
		final Distribution<V> distr;
		ChoiceSig(Object action, Distribution<V> distr) { this.action = action; this.distr = distr; }
		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ChoiceSig<?> other)) return false;
            return Objects.equals(action, other.action) && Objects.equals(distr, other.distr);
		}
		@Override public int hashCode() { return Objects.hash(action, distr); }
	}

	private static final class Signature<V> {
		final List<ChoiceSig<V>> items; // sorted canonical list
		Signature(List<ChoiceSig<V>> items) { this.items = items; }
		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Signature<?> other)) return false;
            return Objects.equals(items, other.items);
		}
		@Override public int hashCode() { return Objects.hash(items); }
	}

	/**
	 * Construct a new Bisimulation object.
	 */
	public Bisimulation(PrismComponent parent) throws PrismException
	{
		super(parent);
	}

	/**
	 * Perform bisimulation minimisation on a model.
	 * @param model The model
	 * @param propNames Names of the propositions in {@code propBSs}
	 * @param propBSs Propositions (satisfying sets of states) to be preserved by bisimulation.
	 */
	public Model<Value> minimise(Model<Value> model, List<String> propNames, List<BitSet> propBSs) throws PrismException
	{
		switch (model.getModelType()) {
		case DTMC:
			return minimiseDTMC((DTMC<Value>) model, propNames, propBSs);
		case CTMC:
			return minimiseCTMC((CTMC<Value>) model, propNames, propBSs);
		case MDP:
			return minimiseMDP((MDP<Value>) model, propNames, propBSs);
		default:
			throw new PrismNotSupportedException("Bisimulation minimisation not yet supported for " + model.getModelType() + "s");
		}
	}

	/**
	 * Perform bisimulation minimisation on a DTMC.
	 * @param dtmc The DTMC
	 * @param propNames Names of the propositions in {@code propBSs}
	 * @param propBSs Propositions (satisfying sets of states) to be preserved by bisimulation.
	 */
	private DTMC<Value> minimiseDTMC(DTMC<Value> dtmc, List<String> propNames, List<BitSet> propBSs)
	{
		// Create initial partition based on propositions
		initialisePartitionInfo(dtmc, propBSs);
		//printPartition(dtmc);

		// Iterative splitting
		boolean changed = true;
		while (changed)
			changed = splitDTMC(dtmc);
		mainLog.println("Minimisation: " + numStates + " to " + numBlocks + " States");
		//printPartition(dtmc);

		// Build reduced model
		DTMCSimple<Value> dtmcNew = new DTMCSimple<>(numBlocks);
		for (int i = 0; i < numBlocks; i++) {
			for (Map.Entry<Integer, Value> e : mdp.getChoice(i, 0)) {
				dtmcNew.setProbability(i, e.getKey(), e.getValue());
			}
		}
		attachStatesAndLabels(dtmc, dtmcNew, propNames, propBSs);

		return dtmcNew;
	}

	/**
	 * Perform bisimulation minimisation on a CTMC.
	 * @param ctmc The CTMC
	 * @param propNames Names of the propositions in {@code propBSs}
	 * @param propBSs Propositions (satisfying sets of states) to be preserved by bisimulation.
	 */
	private CTMC<Value> minimiseCTMC(CTMC<Value> ctmc, List<String> propNames, List<BitSet> propBSs)
	{
		// Create initial partition based on propositions
		initialisePartitionInfo(ctmc, propBSs);
		//printPartition(ctmc);

		// Iterative splitting
		boolean changed = true;
		while (changed)
			changed = splitDTMC(ctmc);
		mainLog.println("Minimisation: " + numStates + " to " + numBlocks + " States");
		//printPartition(ctmc);

		// Build reduced model
		CTMCSimple<Value> ctmcNew = new CTMCSimple<>(numBlocks);
		for (int i = 0; i < numBlocks; i++) {
			for (Map.Entry<Integer, Value> e : mdp.getChoice(i, 0)) {
				ctmcNew.setProbability(i, e.getKey(), e.getValue());
			}
		}
		attachStatesAndLabels(ctmc, ctmcNew, propNames, propBSs);

		return ctmcNew;
	}

	/**
	 * Perform bisimulation minimisation on an MDP.
	 * States in the quotient share (multi)sets of action-labelled, partition-lifted distributions.
	 */
	private MDP<Value> minimiseMDP(MDP<Value> mdpIn, List<String> propNames, List<BitSet> propBSs)
	{
		// Initial partition by propositions
		initialisePartitionInfo(mdpIn, propBSs);

		// Refine until stable
		boolean changed = true;
		while (changed)
			changed = splitMDP(mdpIn);
		mainLog.println("Minimisation: " + numStates + " to " + numBlocks + " States");

		// Build reduced MDP from a representative of each block
		MDPSimple<Value> mdpNew = new MDPSimple<>(numBlocks);
		mdpNew.setEvaluator(mdpIn.getEvaluator());

		int[] rep = new int[numBlocks];
		java.util.Arrays.fill(rep, -1);
		for (int s = 0; s < numStates; s++) {
			int b = partition[s];
			if (rep[b] == -1) rep[b] = s;
		}

		for (int b = 0; b < numBlocks; b++) {
			int s = rep[b];
			int numChoices = mdpIn.getNumChoices(s);
			for (int i = 0; i < numChoices; i++) {
				Distribution<Value> distrNew = new Distribution<>(mdpIn.getEvaluator());
				for (Iterator<Map.Entry<Integer, Value>> it = mdpIn.getTransitionsIterator(s, i); it.hasNext();) {
					Map.Entry<Integer, Value> e = it.next();
					int tBlock = partition[e.getKey()];
					distrNew.add(tBlock, e.getValue());
				}
				Object action = mdpIn.getAction(s, i);
				if (action != null) {
					mdpNew.addActionLabelledChoice(b, distrNew, action);
				} else {
					mdpNew.addChoice(b, distrNew);
				}
			}
		}

		attachStatesAndLabels(mdpIn, mdpNew, propNames, propBSs);
		return mdpNew;
	}

	/**
	 * Construct the initial partition based on a set of proposition bitsets.
	 * Store info in {@code numStates}, {@code numBlocks} and {@code partition}.
	 */
	private void initialisePartitionInfo(Model<Value> model, List<BitSet> propBSs)
	{
		BitSet bs1, bs0;
		numStates = model.getNumStates();
		partition = new int[numStates];

		// Compute all non-empty combinations of propositions
		List<BitSet> all = new ArrayList<BitSet>();
		bs1 = (BitSet) propBSs.get(0).clone();
		bs0 = (BitSet) bs1.clone();
		bs0.flip(0, numStates);
		all.add(bs1);
		all.add(bs0);
		int n = propBSs.size();
		for (int i = 1; i < n; i++) {
			BitSet bs = propBSs.get(i);
			int m = all.size();
			for (int j = 0; j < m; j++) {
				bs1 = all.get(j);
				bs0 = (BitSet) bs1.clone();
 				bs0.andNot(bs);
				bs1.and(bs);
				if (bs1.isEmpty()) {
					all.set(j, bs0);
				} else {
					if (!bs0.isEmpty())
						all.add(bs0);
				}
			}
		}

		// Construct initial partition
		numBlocks = all.size();
		for (int j = 0; j < numBlocks; j++) {
			BitSet bs = all.get(j);
			for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
				partition[i] = j;
			}
		}
	}

	/**
	 * Perform a split of the current partition, if possible, updating {@code numBlocks} and {@code partition}.
	 * @return whether or not the partition was split 
	 */
	private boolean splitDTMC(DTMC<Value> dtmc)
	{
		int s, a, i, numBlocksNew, numChoicesOld;
		Distribution<Value> distrNew;
		int partitionNew[];

		partitionNew = new int[numStates];
		numBlocksNew = 0;
		// Compute the signature for each state (i.e. the distribution for outgoing
		// transitions, lifted to the current partition)
		// For convenience, we just store them as an MDP, with action label equal to the index of the block
		mdp = new MDPSimple<>(numBlocks);
		for (s = 0; s < numStates; s++) {
			// Build lifted distribution
			Iterator<Map.Entry<Integer, Value>> iter = dtmc.getTransitionsIterator(s);
			distrNew = new Distribution<>(dtmc.getEvaluator());
			while (iter.hasNext()) {
				Map.Entry<Integer, Value> e = iter.next();
				distrNew.add(partition[e.getKey()], e.getValue());
			}
			// Store in MDP, update new partition
			a = partition[s];
			numChoicesOld = mdp.getNumChoices(a);
			i = mdp.addChoice(a, distrNew);
			if (i == numChoicesOld)
				mdp.setAction(a, i, numBlocksNew++);
			partitionNew[s] = (Integer) mdp.getAction(a, i);
		}
		// Debug info
		System.out.println("New partition: " + java.util.Arrays.toString(partitionNew));
		System.out.println("Signatures MDP: " + mdp.infoString());
		System.out.println("Signatures MDP: " + mdp);
		//try { mdp.exportToDotFile("mdp.dot"); } catch (PrismException e) {}
		// Update info
		boolean changed = numBlocks != numBlocksNew;
		if (changed) {
			// Note, once converged, we keep the partition from the previous iter
			// because the transition info in the MDP is in terms of this
			partition = partitionNew;
			numBlocks = numBlocksNew;
		}

		return changed;
	}

	/**
	 * Perform a split of the current partition for an MDP, if possible.
	 * States are equivalent w.r.t. the current partition if the (multi)set of
	 * action-labelled, partition-lifted successor distributions is identical.
	 * @return whether or not the partition was split
	 */
	private boolean splitMDP(MDP<Value> mdpIn)
	{
		int[] partitionNew = new int[numStates];
		int numBlocksNew = 0;

		// For each old block, map canonical signatures to fresh block IDs
		List<Map<Signature<Value>, Integer>> perBlockMaps = new ArrayList<>(numBlocks);
		for (int b = 0; b < numBlocks; b++) perBlockMaps.add(new HashMap<>());

		for (int s = 0; s < numStates; s++) {
			int oldB = partition[s];
			Signature<Value> sig = buildSignatureForState(mdpIn, s);
			Map<Signature<Value>, Integer> m = perBlockMaps.get(oldB);
			Integer id = m.get(sig);
			if (id == null) { id = numBlocksNew++; m.put(sig, id); }
			partitionNew[s] = id;
		}

		// Debug info (mirrors DTMC path)
		System.out.println("New partition (MDP): " + java.util.Arrays.toString(partitionNew));

		boolean changed = numBlocks != numBlocksNew;
		if (changed) {
			partition = partitionNew;
			numBlocks = numBlocksNew;
		}
		return changed;
	}

	/**
	 * Build the canonical signature of a state in an MDP given the current partition.
	 * For each choice, lift its distribution to partition blocks and pair it with the action label.
	 * The list of (action, lifted-distribution) pairs is sorted to make it order-insensitive.
	 */
	private Signature<Value> buildSignatureForState(MDP<Value> mdpIn, int s)
	{
		final prism.Evaluator<Value> eval = mdpIn.getEvaluator();
		final List<ChoiceSig<Value>> items = new ArrayList<>();
		int numChoices = mdpIn.getNumChoices(s);
		for (int i = 0; i < numChoices; i++) {
			Distribution<Value> distrLift = new Distribution<>(eval);
			for (Iterator<Map.Entry<Integer, Value>> it = mdpIn.getTransitionsIterator(s, i); it.hasNext();) {
				Map.Entry<Integer, Value> e = it.next();
				int blk = partition[e.getKey()];
				distrLift.add(blk, e.getValue()); // sums duplicates by block
			}
			Object action = mdpIn.getAction(s, i);
			items.add(new ChoiceSig<>(action, distrLift));
		}
		// Canonical order: first by action string (null -> ""), then by distribution string
		items.sort((a, b) -> {
            String as = (a.action == null) ? "" : a.action.toString();
            String bs = (b.action == null) ? "" : b.action.toString();
            int c = as.compareTo(bs);
            if (c != 0) return c;
            return a.distr.toString().compareTo(b.distr.toString());
        });
		return new Signature<>(items);
	}

	/**
	 * Display the current partition, showing the states in each block.
	 */
	@SuppressWarnings("unused")
	private void printPartition(Model<Value> model)
	{
		for (int i = 0; i < numBlocks; i++) {
			mainLog.print(i + ":");
			for (int j = 0; j < numStates; j++)
				if (partition[j] == i)
					if (model.getStatesList() != null)
						mainLog.print(" " + model.getStatesList().get(j));
					else
						mainLog.print(" " + j);
			mainLog.println();
		}
	}

	/**
	 * Attach a list of states to the minimised model by adding a representative state
	 * from the original model.
	 * Also attach information about the propositions (used for bisimulation minimisation)
	 * to the minimised model, in the form of labels (stored as BitSets).
	 * @param model The original model
	 * @param modelNew The minimised model
	 * @param propNames The names of the propositions
	 * @param propBSs Satisfying states (of the minimised model) for the propositions
	 */
	private void attachStatesAndLabels(Model<Value> model, ModelExplicit<Value> modelNew, List<String> propNames, List<BitSet> propBSs)
	{
		// Attach states
		if (model.getStatesList() != null) {
			List<State> statesList = model.getStatesList();
			List<State> statesListNew = new ArrayList<State>(numBlocks);
			for (int i = 0; i < numBlocks; i++) {
				statesListNew.add(null);
			}
			for (int i = 0; i < numStates; i++) {
				if (statesListNew.get(partition[i]) == null)
					statesListNew.set(partition[i], statesList.get(i));
			}
			modelNew.setStatesList(statesListNew);
		}

		// Build/attach new labels
		int numProps = propBSs.size();
		for (int i = 0; i < numProps; i++) {
			String propName = propNames.get(i);
			BitSet propBS = propBSs.get(i);
			BitSet propBSnew = new BitSet();
			for (int j = propBS.nextSetBit(0); j >= 0; j = propBS.nextSetBit(j + 1))
				propBSnew.set(partition[j]);
			modelNew.addLabel(propName, propBSnew);
		}
	}

	public int[] getPartition() {
		return partition;
	}
}
