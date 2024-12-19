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

public class UDistributionKLDivergence<Value> implements UDistribution<Value>
{
	// Transition frequencies
	protected Distribution<Value> frequencies;

	// KL-Divergence threshold
	protected Value beta;

	protected double delta = 0.00001; // Precision value for bisection stopping criterion

	/**
	 * Constructor
	 */
	public UDistributionKLDivergence(Distribution<Value> frequencies, Value beta)
	{
		this.frequencies = frequencies;
		this.beta = beta;
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
	 * Compute dual function for optimisation
	 * See Equation (47) in Section 6.2 of [Nilim et al. 2005]
	 * @param lambda: Optimisation variable
	 * @return Value of the dual
	 */
	public double sigma(double lambda, DoubleDistribution dd, double[] vect) {
		double res = (double) beta * lambda;
		double sum = 0.0;

		for (int i = 0; i < dd.size; i++) {
			sum += dd.probs[i] * Math.exp(vect[dd.index[i]] / lambda);
		}

		res += lambda * Math.log(sum);
		return res;
	}

	/**
	 * Compute gradient of the dual function for optimisation
	 * @param lambda: Optimisation variable
	 * @return Value of the gradient
	 */
	public double sigma_grad(double lambda, DoubleDistribution dd, double[] vect) {
		double res = (double) beta;

		double sum_exp = 0.0;
		double sum_squares = 0.0;

		for (int i = 0; i < dd.size; i++) {
			sum_exp += dd.probs[i] * Math.exp(vect[dd.index[i]]/lambda);
			sum_squares -= dd.probs[i] * vect[dd.index[i]] * Math.exp(vect[dd.index[i]] / lambda) / (Math.pow(lambda,2.0));
		}

		res += Math.log(sum_exp);
		res += lambda * sum_squares / sum_exp;

		return res;
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

		// Invert values if minimising, as bisection algorithm maximises
		double[] vect_minmax = vect.clone();
		if (minMax.isMinUnc()) {
			for (int i = 0; i < vect.length; i++) {
				vect_minmax[i] = -vect[i];
			}
		}

		double max_v = Arrays.stream(vect_minmax).max().getAsDouble();

		if (frequencies.size() == 1) {
			return vect_minmax[dd.index[0]];
		}

		// Upper bound on valid beta, as per Section 6.3 in [Nilim et al. 2005]
		if ((double) beta > -Math.log(getProbSumForValue(max_v, vect_minmax, dd))) {
			return max_v;
		}

		// Initialise upper and lower bounds for bisection
		double low = 0.0;
		double high;

		double sum = 0.0;
		for (int i = 0; i < dd.size; i++) {
			sum += dd.probs[i] * vect_minmax[dd.index[i]];
		}

		high = (max_v - sum) / (double) beta;

		// Mid-point and gradient
		double mu = 1.0;
		double grad;

		// Stopping criteria
		double stop1 = delta * (1 + low + low);
		//double stop2 = sigma_grad(high, dd, vect) - sigma_grad(low, dd, vect);

		// Bisection
		while (high - low > stop1) {
			mu = (high + low) / 2.0;
			grad = sigma_grad(mu, dd, vect_minmax);

			if (grad > 0) {
				high = mu;
			} else {
				low = mu;
			}
		}

		return minMax.isMaxUnc() ? sigma(mu, dd, vect_minmax) : -sigma(mu, dd, vect_minmax);
	}

	public double getProbSumForValue(double val, double[] vect, DoubleDistribution dd) {
		double sum = 0.0;
		for (int i = 0; i < dd.size; i++) {
			if (vect[dd.index[i]] == val) {
				sum += dd.probs[i];
			}
		}

		return sum;
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
		return new UDistributionKLDivergence<>(frequenciesCopy, beta);
	}

	@Override
	public UDistribution<Value> copy(int[] permut)
	{
		Distribution<Value> frequenciesCopy = new Distribution<>(frequencies, permut);
		return new UDistributionKLDivergence<>(frequenciesCopy, beta);
	}

	@Override
	public String toString()
	{
		String s = "";
		s += frequencies.toString();
		s += ", Beta: " + beta.toString();
		return s;
	}
}
