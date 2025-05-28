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

import prism.Evaluator;

import java.util.*;

public class UDistributionL1<Value> implements UDistribution<Value>
{
	// Transition frequencies
	protected Distribution<Value> frequencies;

	// L1 norm threshold
	protected Value l1max;

	/**
	 * Standard Constructor
	 */
	public UDistributionL1(Distribution<Value> frequencies, Value l1max)
	{
		this.frequencies = frequencies;
		this.l1max = l1max;
	}

	/**
	 * Build the product L1-distribution from a list of marginals.
	 * Each marginal contributes its frequency-distribution and its radius.
	 *
	 * @param marginals    the list of UDistributionL1 to multiply together
	 * @param supportArray an array of “combined” support indices, one per product outcome
	 */
	public UDistributionL1(List<UDistributionL1<Value>> marginals, int[] supportArray) {
		if (marginals == null || marginals.isEmpty()) {
			throw new IllegalArgumentException("Must supply at least one marginal");
		}

		// 1) all marginals share the same numeric evaluator
		Evaluator<Value> eval = marginals.getFirst().frequencies.getEvaluator();

		// 2) sum up all the l1 radii
		Value sumRadius = eval.zero();
		for (UDistributionL1<Value> d : marginals) {
			sumRadius = eval.add(sumRadius, d.l1max);
		}
		this.l1max = sumRadius;

		List<List<Value>> marginalFrequencies = new ArrayList<>();
		for (UDistributionL1<Value> d : marginals) {
			marginalFrequencies.add(d.frequencies.frequencies);
		}

		// 4) delegate to Distribution’s product‐constructor:
		//    it will multiply all the freqLists together and map them
		//    onto the supplied supportArray, building our new frequencies.
		this.frequencies = new Distribution<>(marginalFrequencies, supportArray, eval);
	}

	@Override
	public boolean contains(int j)
	{
		return frequencies.contains(j);
	}

	@Override
	public boolean isSubsetOf(BitSet set)
	{
		return frequencies.isSubsetOf(set);
	}

	@Override
	public boolean containsOneOf(BitSet set)
	{
		return frequencies.containsOneOf(set);
	}

	@Override
	public Set<Integer> getSupport()
	{
		return frequencies.getSupport();
	}

	@Override
	public boolean isEmpty()
	{
		return frequencies.isEmpty();
	}

	@Override
	public int size()
	{
		return frequencies.size();
	}

	/**
	 * Do a single row of matrix-vector multiplication followed by min/max,
	 * i.e. return min/max_P { sum_j P(s,j)*vect[j] }
	 * @param vect Vector to multiply by
	 * @param minMax Min/max uncertainty (via isMinUnc/isMaxUnc)
	 */
	@Override
	public double mvMultUnc(double[] vect, MinMax minMax)
	{
		DoubleDistribution dd = extractDoubleDistribution();
		if (frequencies.size() == 1) {
			return vect[dd.index[0]];
		}

		// Get a list of indices for the transition, sorted according to the successor values
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < dd.size; i++) {
			indices.add(i);
		}
		if (minMax.isMaxUnc()) {
			indices.sort((o1, o2) -> -Double.compare(vect[dd.index[o1]], vect[dd.index[o2]]));
		} else {
			indices.sort((o1, o2) -> Double.compare(vect[dd.index[o1]], vect[dd.index[o2]]));
		}

        // Distribute the positive budget to the best states
		double budget = (double) l1max / 2.0;
		int k = indices.getFirst();
		dd.probs[k] = Double.min(1.0, dd.probs[k] + budget);

		// Distribute the negative budget to the worst states
		budget = dd.sum() - 1;
		for (int i = dd.size - 1; i >= 0; i--) {
			int j = indices.get(i);
			dd.probs[j] = Double.max(0.0, dd.probs[j] - budget);
			budget = dd.sum() - 1;
			if (budget <= 0) {
				break;
			}
		}

		double res = 0.0;
		for (int i = 0; i < dd.size; i++) {
			res += dd.probs[i] * vect[dd.index[i]];
		}

		return res;
	}

	private DoubleDistribution extractDoubleDistribution() {
		DoubleDistribution dist = new DoubleDistribution(frequencies.size());
		int i = 0;
		for (Map.Entry<Integer, Value> entry : frequencies.map.entrySet()) {
			dist.probs[i] = ((Map.Entry<Integer, Double>) entry).getValue();
			dist.index[i] = entry.getKey();
			i++;
		}
		return dist;
	}

	@Override
	public UDistribution<Value> copy()
	{
		Distribution<Value> frequenciesCopy = new Distribution<>(frequencies);
		return new UDistributionL1<>(frequenciesCopy, l1max);
	}

	@Override
	public UDistribution<Value> copy(int[] permut)
	{
		Distribution<Value> frequenciesCopy = new Distribution<>(frequencies, permut);
		return new UDistributionL1<>(frequenciesCopy, l1max);
	}

	@Override
	public String toString()
	{
		String s = "[";
		s += frequencies.toString();
		s += ", L1Max: " + l1max.toString();
		s += "]";
		return s;
	}
}
