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

import com.gurobi.gurobi.*;
import param.Function;
import prism.PrismException;

import java.util.*;

public class UDistributionLinearProgram<Value> implements UDistribution<Value>
{
    // Transition frequencies
    protected GRBModel model;
    protected int[] support;
    protected GRBVar[] vars;

    /**
     * Constructor
     */
    public UDistributionLinearProgram(int[] support,
            GRBModel model) {
        this.model = model;
        this.support = support;
        this.vars = model.getVars();
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
        try {
            if (support.length == 1) {
                return vect[support[0]];
            }

            GRBLinExpr expr = new GRBLinExpr();
            for (int i = 0; i < support.length; i++) {
                expr.addTerm(vect[support[i]], vars[i]);
            }

            model.setObjective(expr, minMax.isMinUnc() ? GRB.MINIMIZE : GRB.MAXIMIZE);
            model.optimize();
            return model.get(GRB.DoubleAttr.ObjVal);

        } catch (GRBException e) {
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
        s += "Support: " + support;
        return s;
    }
}