//==============================================================================
//
//	Copyright (c) 2023-
//	Authors:
//	* Dave Parker <david.parker@cs.ox.ac.uk> (University of Oxford)
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

import prism.PrismException;
import prism.PrismNotSupportedException;
import strat.MDStrategy;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

/**
 * Simple explicit-state representation of a UMDP (uncertain Markov decision process).
 */
public class UMDPSimple<Value> extends ModelExplicit<Value> implements NondetModelSimple<Value>, UMDP<Value>
{
	// Uncertain transition function
	protected List<List<UDistribution<Value>>> trans;

	// Action labels
	protected ChoiceActionsSimple actions;

	// Constructors

	/**
	 * Constructor: empty UMDP.
	 */
	public UMDPSimple()
	{
		initialise(0);
	}

	/**
	 * Constructor: new UMDP with fixed number of states.
	 */
	public UMDPSimple(int numStates)
	{
		initialise(numStates);
	}

	// Mutators (for ModelSimple)

	/**
	 * Copy constructor.
	 */
	public UMDPSimple(UMDPSimple<Value> umdp)
	{
		this(umdp.numStates);
		copyFrom(umdp);
		// Copy storage directly to avoid worrying about duplicate distributions (and for efficiency)
		for (int s = 0; s < numStates; s++) {
			List<UDistribution<Value>> udistrs = trans.get(s);
			for (UDistribution<Value> udistr : umdp.trans.get(s)) {
				udistrs.add(udistr.copy());
			}
		}
		actions = new ChoiceActionsSimple(umdp.actions);
	}

	/**
	 * Construct a UMDP from an existing one and a state index permutation,
	 * i.e. in which state index i becomes index permut[i].
	 * Note: have to build new UDistributions from scratch anyway to do this,
	 * so may as well provide this functionality as a constructor.
	 */
	public UMDPSimple(UMDPSimple<Value> umdp, int[] permut)
	{
		this(umdp.numStates);
		copyFrom(umdp, permut);
		// Copy storage directly to avoid worrying about duplicate distributions (and for efficiency)
		// (Since permut is a bijection, all structures and statistics are identical)
		for (int s = 0; s < numStates; s++) {
			List<UDistribution<Value>> udistrs = trans.get(permut[s]);
			for (UDistribution<Value> udistr : umdp.trans.get(s)) {
				udistrs.add(udistr.copy(permut));
			}
		}
		actions = new ChoiceActionsSimple(umdp.actions, permut);
	}

	// Mutators (for ModelSimple)

	@Override
	public void initialise(int numStates)
	{
		super.initialise(numStates);
		trans = new ArrayList<>(numStates);
		for (int i = 0; i < numStates; i++) {
			trans.add(new ArrayList<>());
		}
		actions = new ChoiceActionsSimple();
	}

	@Override
	public void clearState(int s)
	{
		// Do nothing if state does not exist
		if (s >= numStates || s < 0) {
			return;
		}
		// Clear data structures
		trans.get(s).clear();
		actions.clearState(s);
	}

	@Override
	public int addState()
	{
		addStates(1);
		return numStates - 1;
	}

	@Override
	public void addStates(int numToAdd)
	{
		for (int i = 0; i < numToAdd; i++) {
			trans.add(new ArrayList<>());
			numStates++;
		}
	}

	// Mutators (other)

	/**
	 * Add a choice (uncertain distribution {@code udistr}) to state {@code s} (which must exist).
	 * Returns the index of the (newly added) distribution.
	 * Returns -1 in case of error.
	 */
	public int addChoice(int s, UDistribution<Value> udistr)
	{
		List<UDistribution<Value>> set;
		// Check state exists
		if (s >= numStates || s < 0) {
			return -1;
		}
		// Add distribution
		set = trans.get(s);
		set.add(udistr);
		return set.size() - 1;
	}

	/**
	 * Add a choice (uncertain distribution {@code udistr}) labelled with {@code action} to state {@code s} (which must exist).
	 * Returns the index of the (newly added) distribution.
	 * Returns -1 in case of error.
	 */
	public int addActionLabelledChoice(int s, UDistribution<Value> udistr, Object action)
	{
		List<UDistribution<Value>> set;
		// Check state exists
		if (s >= numStates || s < 0) {
			return -1;
		}
		// Add distribution/action
		set = trans.get(s);
		set.add(udistr);
		// Set action
		actions.setAction(s, set.size() - 1, action);
		return set.size() - 1;
	}

	/**
	 * Set the action label for choice i in some state s.
	 */
	public void setAction(int s, int i, Object action)
	{
		actions.setAction(s, i, action);
	}

	// Accessors (for Model)

	@Override
	public void findDeadlocks(boolean fix) throws PrismException
	{
		for (int i = 0; i < numStates; i++) {
			// Note that no distributions is a deadlock, not an empty distribution
			if (trans.get(i).isEmpty()) {
				addDeadlockState(i);
				if (fix) {
					throw new PrismNotSupportedException("Deadlock fixing not yet implemented");
//					Distribution<Value> distr = new Distribution<>(getEvaluator());
//					distr.add(i, getEvaluator().one());
//					addChoice(i, distr);
				}
			}
		}
	}

	@Override
	public void checkLowerBoundsArePositive() throws PrismException
	{
		;
	}

	@Override
	public void checkForDeadlocks(BitSet except) throws PrismException
	{
		for (int i = 0; i < numStates; i++) {
			if (trans.get(i).isEmpty() && (except == null || !except.get(i)))
				throw new PrismException("UMDP has a deadlock in state " + i);
		}
	}

	// Accessors (for NondetModel)

	@Override
	public int getNumChoices(int s)
	{
		return trans.get(s).size();
	}

	@Override
	public Object getAction(int s, int i)
	{
		return actions.getAction(s, i);
	}

	@Override
	public int getNumTransitions(int s, int i)
	{
		return trans.get(s).get(i).size();
	}

	@Override
	public boolean allSuccessorsInSet(int s, int i, BitSet set)
	{
		return trans.get(s).get(i).isSubsetOf(set);
	}

	@Override
	public boolean someSuccessorsInSet(int s, int i, BitSet set)
	{
		return trans.get(s).get(i).containsOneOf(set);
	}

	@Override
	public Iterator<Integer> getSuccessorsIterator(final int s, final int i)
	{
		return trans.get(s).get(i).getSupport().iterator();
	}

	@Override
	public SuccessorsIterator getSuccessors(final int s, final int i)
	{
		return SuccessorsIterator.from(getSuccessorsIterator(s, i), true);
	}

	@Override
	public Model<Value> constructInducedModel(MDStrategy<Value> strat)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	// Accessors (for UMDP)

	@Override
	public UDistribution<Value> getUncertainDistribution(int s, int i)
	{
		return trans.get(s).get(i);
	}

	@Override
	public double mvMultUncSingle(int s, int k, double[] vect, MinMax minMax)
	{
		return trans.get(s).get(k).mvMultUnc(vect, minMax);
	}

	@Override
	public String toString()
	{
		String s = "";
		s = "[ ";

		for (int i = 0; i < getNumStates(); i++) {
			if (i > 0) {
				s += ", ";
			}
			s += i + ": ";
			s += "[";
			int n = getNumChoices(i);
			for (int j = 0; j < n; j++) {
				if (j > 0) {
					s += ",";
				}
				Object o = getAction(i, n);
				if (o != null) {
					s += o + ":";
				}
				s += getUncertainDistribution(i, j).toString();
			}
			s += "]";
		}
		s += " ]\n";
		return s;
	}

	/**
	 * Test code
	 */
	public static void main(String[] args)
	{
		UMDPSimple<Double> umdp = new UMDPSimple<>();
		umdp.addStates(5);
		Distribution<Double> distr = Distribution.ofDouble();
		distr.add(1, 0.1);
		distr.add(3, 0.5);
		distr.add(4, 0.4);
		UDistribution<Double> udistr = new UDistributionL1Max<>(distr, 0.25);
		umdp.addActionLabelledChoice(0, udistr, "a");
		System.out.println(umdp);
		System.out.println(umdp.getUncertainDistribution(0,0));
	}
}
