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

import java.util.BitSet;
import java.util.Set;

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

	@Override
	public double mvMultUnc(double[] vect, MinMax minMax)
	{
		throw new UnsupportedOperationException();
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
}
