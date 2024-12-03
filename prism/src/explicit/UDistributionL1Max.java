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

import java.util.*;

public class UDistributionL1Max<Value> implements UDistribution<Value>
{
	// Transition frequencies
	protected Distribution<Value> frequencies;

	// L1 norm threshold
	protected Value l1max;

	/**
	 * Constructor
	 */
	public UDistributionL1Max(Distribution<Value> frequencies, Value l1max)
	{
		this.frequencies = frequencies;
		this.l1max = l1max;
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
			Collections.sort(indices, (o1, o2) -> -Double.compare(vect[dd.index[o1]], vect[dd.index[o2]]));
		} else {
			Collections.sort(indices, (o1, o2) -> Double.compare(vect[dd.index[o1]], vect[dd.index[o2]]));
		}

		// Maximum budget for positive and negative residuals

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
		return new UDistributionL1Max<>(frequenciesCopy, l1max);
	}

	@Override
	public UDistribution<Value> copy(int[] permut)
	{
		Distribution<Value> frequenciesCopy = new Distribution<>(frequencies, permut);
		return new UDistributionL1Max<>(frequenciesCopy, l1max);
	}

	@Override
	public String toString()
	{
		String s = "";
		s += frequencies.toString();
		s += ", L1Max: " + l1max.toString();
		return s;
	}
}
