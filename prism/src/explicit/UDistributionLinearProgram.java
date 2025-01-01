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

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.linear.LinearSolver;
import param.Function;
import prism.PrismException;

import java.util.*;

public class UDistributionLinearProgram<Value> implements UDistribution<Value>
{
	// Transition frequencies
	protected Distribution<Function> pdist;
	protected GRBModel model;
	protected ExpressionTranslator trans;

	/**
	 * Constructor
	 */
	public UDistributionLinearProgram(Distribution<Function> pdist,
									  GRBModel model,
									  ExpressionTranslator trans) {
		this.pdist = pdist;
		this.model = model;
		this.trans = trans;
	}

	@Override
	public boolean contains(int j)
	{
		return pdist.contains(j);
	}

	@Override
	public boolean isSubsetOf(BitSet set) {
		return pdist.isSubsetOf(set);
	}

	@Override
	public boolean containsOneOf(BitSet set)
	{
		return pdist.containsOneOf(set);
	}

	@Override
	public Set<Integer> getSupport()
	{
		return pdist.getSupport();
	}

	@Override
	public boolean isEmpty()
	{
		return pdist.isEmpty();
	}

	@Override
	public int size()
	{
		return pdist.size();
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
		try {
			ParametricDistribution pd = ParametricDistribution.extractParametricDistribution(pdist);
			if (pdist.size() == 1) {
				return vect[pd.index[0]];
			}

			// Add the objective w.r.t. probability expressions and values of successors
			GRBLinExpr expr = new GRBLinExpr();
			for (int i = 0; i < pd.size; i++) {
				expr.add(trans.translateLinearExpression(pd.probs[i].asExpression(), vect[pd.index[i]]));
			}

			model.setObjective(expr, minMax.isMinUnc() ? GRB.MINIMIZE : GRB.MAXIMIZE);
			model.optimize();
			return model.get(GRB.DoubleAttr.ObjVal);

		} catch (GRBException | PrismException e) {
			throw new RuntimeException(e);
		}
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
		s += "Support: " + pdist.getSupport();
		return s;
	}
}
