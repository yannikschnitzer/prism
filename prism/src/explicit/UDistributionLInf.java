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

import common.Interval;

import java.util.*;

public class UDistributionLInf<Value> implements UDistribution<Value>
{
	// Transition frequencies
	protected Distribution<Value> frequencies;

	// L1 norm threshold
	protected Value linf_max;

	/**
	 * Constructor
	 */
	public UDistributionLInf(Distribution<Value> frequencies, Value linf_max)
	{
		this.frequencies = frequencies;
		this.linf_max = linf_max;
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
		// Build L-inf ball around center distribution, represented as interval distribution
		DoubleIntervalDistribution did = new DoubleIntervalDistribution(size());
		Iterator<Map.Entry<Integer, Value>> iter = frequencies.iterator();

		int i = 0;
		while (iter.hasNext()) {
			Map.Entry<Integer, Value> e = iter.next();
			double freq = (double) e.getValue();
			did.lower[i] = Math.max(freq - (double) linf_max, 0.0);
			did.upper[i] = Math.min(freq + (double) linf_max, 1.0);
			did.index[i] = e.getKey();
			i++;
		}

		return IDTMC.mvMultUncSingle(did, vect, minMax);
	}

	@Override
	public UDistribution<Value> copy()
	{
		Distribution<Value> frequenciesCopy = new Distribution<>(frequencies);
		return new UDistributionLInf<>(frequenciesCopy, linf_max);
	}

	@Override
	public UDistribution<Value> copy(int[] permut)
	{
		Distribution<Value> frequenciesCopy = new Distribution<>(frequencies, permut);
		return new UDistributionLInf<>(frequenciesCopy, linf_max);
	}

	@Override
	public String toString()
	{
		String s = "";
		s += frequencies.toString();
		s += ", L_Inf Max: " + linf_max.toString();
		return s;
	}
}
