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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

public class UDistributionLogLikelihood<Value> implements UDistribution<Value>
{
	// Transition frequencies
	protected Distribution<Value> frequencies;

	// KL-Divergence threshold
	protected Value beta;

	protected double delta = 0.00001; // Precision value for bisection stopping criterion

	/**
	 * Constructor
	 */
	public UDistributionLogLikelihood(Distribution<Value> frequencies, Value beta)
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
	 * Compute lambda(mu) function as per Equation (43) of [Nilim et al. 2005]
	 */
	public double lambdaf(double mu, DoubleDistribution dd, double[] vect) {
		double sum = 0.0;
		for(int i = 0; i < dd.size; i++) {
			sum += dd.probs[i] / (mu - vect[dd.index[i]]);
		}
		return 1.0 / sum;
	}

	/**
	 * Compute d(lambda)/d(mu) as per Appendix B of [Nilim et al. 2005]
	 */
	public double lambda_grad(double mu, DoubleDistribution dd, double[] vect) {
		double nom = 0.0;
		double denom = 0.0;

		for(int i = 0; i < dd.size; i++) {
			nom += Math.pow(dd.probs[i] / (mu - vect[dd.index[i]]), 2.0);
			denom += dd.probs[i] / (mu - vect[dd.index[i]]);
		}
		denom = Math.pow(denom, 2.0);

		return nom / denom;
	}

	/**
	 * Compute h(lambda, mu) function as per Equation (41) of [Nilim et al. 2005]
	 */
	public double h(double lambda, double mu, DoubleDistribution dd, double[] vect) {
		double sum = 0.0;

		sum += mu;
		sum -= (1 + (double) this.beta) * lambda;

		for(int i = 0; i < dd.size; i++) {
			sum += lambda * dd.probs[i] * Math.log(lambda * dd.probs[i] / (mu - vect[dd.index[i]]));
		}

		return sum;
	}

	/**
	 * Compute d(h)/d(lambda) as per Equation (45) of [Nilim et al. 2005]
	 */
	public double h_grad(double mu, DoubleDistribution dd, double[] vect) {
		double sum = -((double) beta);
		for(int i = 0; i < dd.size; i++) {
			sum += dd.probs[i] * Math.log(lambdaf(mu, dd, vect) * dd.probs[i] / (mu - vect[dd.index[i]]));
		}
		return sum;
	}

    /**
     * Compute dual function for optimisation
	 * See Equation (41) in Section 5.2 of [Nilim et al. 2005]
	 * @param mu: Optimisation variable
     * @return Value of the dual
	 */
	public double sigma(double mu, DoubleDistribution dd, double[] vect) {
		return h(lambdaf(mu, dd, vect), mu, dd, vect);
	}

	/**
	 * Compute gradient of the dual function for optimisation
	 * @param mu: Optimisation variable
	 * @return Value of the gradient
	 */
	public double sigma_grad(double mu, DoubleDistribution dd, double[] vect) {
		return h_grad(mu, dd, vect) * lambda_grad(mu, dd, vect);
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

		// Compute beta_max, the maximum permissible beta ensuring non-empty interior of the uncertainty set
		double beta_max = 0.0;

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

		// Initialise upper and lower bounds for bisection
		double low = max_v;
		double high;

		double sum = 0.0;
		for (int i = 0; i < dd.size; i++) {
			sum += dd.probs[i] * vect_minmax[dd.index[i]];
			beta_max += dd.probs[i] * Math.log(dd.probs[i]);
		}
		System.out.println("Beta max: "+ beta_max);

		high = (max_v - Math.exp((double) beta - beta_max) * sum) / ( 1 - Math.exp((double) beta - beta_max));

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
		return new UDistributionLogLikelihood<>(frequenciesCopy, beta);
	}

	@Override
	public UDistribution<Value> copy(int[] permut)
	{
		Distribution<Value> frequenciesCopy = new Distribution<>(frequencies, permut);
		return new UDistributionLogLikelihood<>(frequenciesCopy, beta);
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
