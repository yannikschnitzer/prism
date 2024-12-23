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

import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.LinearSolver;

import java.util.*;

public class UDistributionPolytope<Value> implements UDistribution<Value>
{
	// Transition frequencies
	protected int[] support;

	private final LinearSolver.Builder builder;

	private final MatrixR064 eqMatrix;
	private final MatrixR064 eqVector;
	private final MatrixR064 ineqMatrix;
	private final MatrixR064 ineqVector;

	/**
	 * Constructor
	 */
	public UDistributionPolytope(int[] support,
								 double[][] eqMatrix,
								 double[] eqVector,
								 double[][] ineqMatrix,
								 double[] ineqVector) {
		this.support = support;
		this.builder = LinearSolver.newBuilder();

		// Add equalities to LP
		if (eqMatrix.length > 0) {
			this.eqMatrix = MatrixR064.FACTORY.rows(eqMatrix);
			this.eqVector = MatrixR064.FACTORY.column(eqVector);
			this.builder.equalities(this.eqMatrix, this.eqVector);
		} else {
			this.eqMatrix = null;
			this.eqVector = null;
		}

		// Add inequalities to LP
		if (ineqMatrix.length > 0) {
			this.ineqMatrix = MatrixR064.FACTORY.rows(ineqMatrix);
			this.ineqVector = MatrixR064.FACTORY.column(ineqVector);
			this.builder.inequalities(this.ineqMatrix, this.ineqVector);
		} else {
			this.ineqMatrix = null;
			this.ineqVector = null;
		}

		// Add local simplex and probability constraints
		double[] simplexConstraint = new double[support.length];
		Arrays.fill(simplexConstraint, 1.0);
		this.builder.equality(1, simplexConstraint);
		this.builder.lower(0.0);
		this.builder.upper(1.0);

		// Check feasibility of constraints
		Optimisation.Result res = builder.objective(0,0).solve();
		assert res.getState().isFeasible() : "Empty interior";
	}

	@Override
	public boolean contains(int j)
	{
		return Arrays.stream(support).anyMatch(o -> o == j);
	}

	@Override
	public boolean isSubsetOf(BitSet set) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean containsOneOf(BitSet set)
	{
		return set.stream().anyMatch(this::contains);
	}

	@Override
	public Set<Integer> getSupport()
	{
		HashSet<Integer> set = new HashSet<>();
        for (int j : support) {
            set.add(j);
        }
		return set;
	}

	@Override
	public boolean isEmpty()
	{
		return support.length == 0;
	}

	@Override
	public int size()
	{
		return support.length;
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
		double[] objective = new double[support.length];

		for (int i = 0; i < support.length; i++) {
			objective[i] = minMax.isMinUnc() ? vect[support[i]] : -vect[support[i]];
		}

		Optimisation.Result optRes = builder.objective(objective).solve();
		double res = optRes.getValue();

		return minMax.isMinUnc() ? res : -res;
	}

	@Override
	public UDistribution<Value> copy()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public UDistribution<Value> copy(int[] permut)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString()
	{
		String s = "Polytopic, ";
		s += "Support: " + Arrays.toString(support);
		return s;
	}
}
