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

import java.util.BitSet;
import java.util.Set;

public class UDistributionIntervals<Value> implements UDistribution<Value>
{
	// Transition probability intervals
	protected Distribution<Interval<Value>> intervals;

	/**
	 * Constructor
	 */
	public UDistributionIntervals(Distribution<Interval<Value>> intervals)
	{
		this.intervals = intervals;
	}

	// Accessors for UDistribution

	@Override
	public boolean contains(int j)
	{
		return intervals.contains(j);
	}

	@Override
	public boolean isSubsetOf(BitSet set)
	{
		return intervals.isSubsetOf(set);
	}

	@Override
	public boolean containsOneOf(BitSet set)
	{
		return intervals.containsOneOf(set);
	}

	@Override
	public Set<Integer> getSupport()
	{
		return intervals.getSupport();
	}

	@Override
	public boolean isEmpty()
	{
		return intervals.isEmpty();
	}

	@Override
	public int size()
	{
		return intervals.size();
	}

	@Override
	public double mvMultUnc(double[] vect, MinMax minMax)
	{
		DoubleIntervalDistribution did = IntervalUtils.extractDoubleIntervalDistribution(((UDistributionIntervals<Double>) this).getIntervals().iterator(), size());
		return IDTMC.mvMultUncSingle(did, vect, minMax);
	}

	@Override
	public UDistribution<Value> copy()
	{
		Distribution<Interval<Value>> intervalsCopy = new Distribution<>(intervals);
		return new UDistributionIntervals<>(intervalsCopy);
	}

	@Override
	public UDistribution<Value> copy(int[] permut)
	{
		Distribution<Interval<Value>> intervalsCopy = new Distribution<>(intervals, permut);
		return new UDistributionIntervals<>(intervalsCopy);
	}

	// Accessors (other)

	public Distribution<Interval<Value>> getIntervals()
	{
		return intervals;
	}

	@Override
	public String toString() {
		return intervals.toString();
	}
}
