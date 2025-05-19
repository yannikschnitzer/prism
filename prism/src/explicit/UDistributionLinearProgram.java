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
import common.Interval;
import common.iterable.Reducible;

import java.util.*;
import java.util.stream.Collectors;

public class UDistributionLinearProgram<Value> implements UDistribution<Value>
{
    // Transition frequencies
    protected GRBModel model;
    protected int[] support;
    protected HashSet<Integer> supportSet;
    protected GRBVar[] vars;

    // Store marginals when generated from interval product
    List<List<Interval<Value>>> marginals;

    /**
     * Constructor
     */
    public UDistributionLinearProgram(int[] support,
            GRBModel model) {
        this.model = model;
        this.support = support;
        this.vars = model.getVars();

        // Store support also as HashSet for efficient look up
        this.supportSet = new HashSet<>();
        for (int j : support) {
            supportSet.add(j);
        }
    }

    public UDistributionLinearProgram(List<List<Interval<Value>>> marginals, List<Integer> support, GRBEnv env) {
        this.support = support.stream().mapToInt(Integer::intValue).toArray();
        this.supportSet = new HashSet<>(support);
        this.marginals = marginals;

        //System.out.println("Support: " + Arrays.toString(this.support) + " Marginals: " + marginals);

        // Build McCormick LP from marginals
        try {
            this.model = new GRBModel(env);
            buildMcCormickLP();

            model.set(GRB.IntParam.Method, 1);         // Use simplex (Method=1)
            model.set(GRB.IntParam.Threads, 4);        // Parallelise solving
            model.update();

            //printModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UDistributionLinearProgram(List<List<Interval<Value>>> marginals, int[] support, GRBEnv env) {
        this.support = support;
        this.supportSet = (HashSet<Integer>) Arrays.stream(support)
                                            .boxed()
                                            .collect(Collectors.toSet());
        this.marginals = marginals;

        //System.out.println("Support: " + Arrays.toString(this.support) + " Marginals: " + marginals);

        // Build McCormick LP from marginals
        try {
            this.model = new GRBModel(env);
            buildMcCormickLP();

            model.set(GRB.IntParam.Method, 1);         // Use simplex (Method=1)
            model.set(GRB.IntParam.Threads, 4);        // Parallelise solving
            model.update();

            //printModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void buildMcCormickLP() throws GRBException {
        buildMcCormickRelaxation(this.marginals);
        this.model.update();
    }

    // A helper class to hold a product variable and its associated lower and upper bounds.
    private static class ProductVar {
        public GRBVar var;
        public double L; // product of lower bounds so far
        public double U; // product of upper bounds so far

        public ProductVar(GRBVar var, double L, double U) {
            this.var = var;
            this.L = L;
            this.U = U;
        }
    }

    /**
     * Generates the LP relaxation (via recursive McCormick envelopes) for the multilinear
     * product over several marginal distributions.
     *
     * The method creates new variables (the “z” variables) along with linking and McCormick constraints,
     * and then stores the final relaxation variables in the member variable "vars".
     *
     * @param marginals the list of marginal distributions (each as a list of intervals)
     * @throws GRBException if a Gurobi error occurs
     */
    public void buildMcCormickRelaxation(List<List<Interval<Value>>> marginals) throws GRBException {
        // Determine the number of distributions and their sizes.
        int numDists = marginals.size();
        int[] sizes = new int[numDists];
        for (int j = 0; j < numDists; j++) {
            sizes[j] = marginals.get(j).size();
        }

        // Create "x" variables for each distribution.
        // Each x variable is defined on [0,1] and we then add constraints to force
        // x[i] to be between its marginal’s lower and upper bounds.
        List<GRBVar[]> xList = new ArrayList<>();
        for (int j = 0; j < numDists; j++) {
            GRBVar[] xVars = new GRBVar[sizes[j]];
            for (int i = 0; i < sizes[j]; i++) {
                xVars[i] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "x_" + j + "_" + i);
            }
            // Constraint: sum_i xVars[i] == 1
            GRBLinExpr sumExpr = new GRBLinExpr();
            for (int i = 0; i < sizes[j]; i++) {
                sumExpr.addTerm(1.0, xVars[i]);
                // Add individual bounds:
                double lower = (double) marginals.get(j).get(i).getLower();
                double upper = (double) marginals.get(j).get(i).getUpper();
                // xVars[i] >= lower   -->   xVars[i] - lower >= 0
                GRBLinExpr lbExpr = new GRBLinExpr();
                lbExpr.addTerm(1.0, xVars[i]);
                lbExpr.addConstant(-lower);
                model.addConstr(lbExpr, GRB.GREATER_EQUAL, 0.0, "lb_x_" + j + "_" + i);
                // xVars[i] <= upper   -->   upper - xVars[i] >= 0
                GRBLinExpr ubExpr = new GRBLinExpr();
                ubExpr.addConstant(upper);
                ubExpr.addTerm(-1.0, xVars[i]);
                model.addConstr(ubExpr, GRB.GREATER_EQUAL, 0.0, "ub_x_" + j + "_" + i);
            }
            model.addConstr(sumExpr, GRB.EQUAL, 1.0, "sum_x_" + j);
            xList.add(xVars);
        }

        // Update the model to register all the x variables and constraints.
        model.update();

        // --- Single marginal case
        if (numDists == 1) {
            this.vars = xList.getFirst();
            return;
        }

        // --- Stage 1: Build the bilinear product z^(2) = x^(0) * x^(1)
        // We store these new variables and their implied lower/upper bounds in zPrev.
        Map<String, ProductVar> zPrev = new HashMap<>();
        for (int i = 0; i < sizes[0]; i++) {
            double L_i = (double) marginals.get(0).get(i).getLower();
            double U_i = (double) marginals.get(0).get(i).getUpper();
            for (int j = 0; j < sizes[1]; j++) {
                double L_j = (double) marginals.get(1).get(j).getLower();
                double U_j = (double) marginals.get(1).get(j).getUpper();
                double L_val = L_i * L_j;
                double U_val = U_i * U_j;
                GRBVar zVar = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "z_2_" + i + "_" + j);
                String key = i + "_" + j;
                zPrev.put(key, new ProductVar(zVar, L_val, U_val));
            }
        }
        // Linking constraints for stage 1:
        // For each index i in distribution 0: sum_{j} z^(2)_{i,j} == x^(0)[i]
        for (int i = 0; i < sizes[0]; i++) {
            GRBLinExpr expr = new GRBLinExpr();
            for (int j = 0; j < sizes[1]; j++) {
                String key = i + "_" + j;
                expr.addTerm(1.0, zPrev.get(key).var);
            }
            expr.addTerm(-1.0, xList.get(0)[i]);
            model.addConstr(expr, GRB.EQUAL, 0.0, "link_x0_" + i);
        }
        // For each index j in distribution 1: sum_{i} z^(2)_{i,j} == x^(1)[j]
        for (int j = 0; j < sizes[1]; j++) {
            GRBLinExpr expr = new GRBLinExpr();
            for (int i = 0; i < sizes[0]; i++) {
                String key = i + "_" + j;
                expr.addTerm(1.0, zPrev.get(key).var);
            }
            expr.addTerm(-1.0, xList.get(1)[j]);
            model.addConstr(expr, GRB.EQUAL, 0.0, "link_x1_" + j);
        }
        // McCormick constraints for stage 1.
        // For each (i,j), we add four constraints based on the bilinear McCormick relaxation.
        for (int i = 0; i < sizes[0]; i++) {
            double L_i = (double) marginals.get(0).get(i).getLower();
            double U_i = (double) marginals.get(0).get(i).getUpper();
            for (int j = 0; j < sizes[1]; j++) {
                double L_j = (double) marginals.get(1).get(j).getLower();
                double U_j = (double) marginals.get(1).get(j).getUpper();
                String key = i + "_" + j;
                GRBVar zVar = zPrev.get(key).var;
                // Constraint 1: z >= L_i*x^(1)[j] + L_j*x^(0)[i] - L_i*L_j
                {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, zVar);
                    expr.addTerm(-L_i, xList.get(1)[j]);
                    expr.addTerm(-L_j, xList.get(0)[i]);
                    expr.addConstant(L_i * L_j);
                    model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc1_" + i + "_" + j);
                }
                // Constraint 2: z >= U_i*x^(1)[j] + U_j*x^(0)[i] - U_i*U_j
                {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, zVar);
                    expr.addTerm(-U_i, xList.get(1)[j]);
                    expr.addTerm(-U_j, xList.get(0)[i]);
                    expr.addConstant(U_i * U_j);
                    model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc2_" + i + "_" + j);
                }
                // Constraint 3: z <= U_i*x^(1)[j] + L_j*x^(0)[i] - U_i*L_j
                {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(U_i, xList.get(1)[j]);
                    expr.addTerm(L_j, xList.get(0)[i]);
                    expr.addConstant(-U_i * L_j);
                    expr.addTerm(-1.0, zVar);
                    model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc3_" + i + "_" + j);
                }
                // Constraint 4: z <= L_i*x^(1)[j] + U_j*x^(0)[i] - L_i*U_j
                {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(L_i, xList.get(1)[j]);
                    expr.addTerm(U_j, xList.get(0)[i]);
                    expr.addConstant(-L_i * U_j);
                    expr.addTerm(-1.0, zVar);
                    model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc4_" + i + "_" + j);
                }
            }
        }

        // --- Stages 2 to (numDists-1): Recursively "lift" the product.
        // For k = 3 up to numDists, we extend the product one distribution at a time.
        for (int k = 3; k <= numDists; k++) {
            Map<String, ProductVar> zNew = new HashMap<>();
            int distIndex = k - 1; // current distribution index (0-indexed)
            // Create new variables for the extended product.
            for (Map.Entry<String, ProductVar> entry : zPrev.entrySet()) {
                String prevKey = entry.getKey();
                ProductVar prevPV = entry.getValue();
                for (int i = 0; i < sizes[distIndex]; i++) {
                    double L_x = (double) marginals.get(distIndex).get(i).getLower();
                    double U_x = (double) marginals.get(distIndex).get(i).getUpper();
                    double L_new = prevPV.L * L_x;
                    double U_new = prevPV.U * U_x;
                    String newKey = prevKey + "_" + i;
                    GRBVar newVar = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "z_" + k + "_" + newKey);
                    zNew.put(newKey, new ProductVar(newVar, L_new, U_new));
                }
            }
            // Linking constraints:
            // (a) For each previous product variable, the sum over the new indices equals it.
            for (Map.Entry<String, ProductVar> entry : zPrev.entrySet()) {
                String prevKey = entry.getKey();
                ProductVar prevPV = entry.getValue();
                GRBLinExpr expr = new GRBLinExpr();
                for (int i = 0; i < sizes[distIndex]; i++) {
                    String newKey = prevKey + "_" + i;
                    expr.addTerm(1.0, zNew.get(newKey).var);
                }
                expr.addTerm(-1.0, prevPV.var);
                model.addConstr(expr, GRB.EQUAL, 0.0, "link_prev_" + prevKey);
            }
            // (b) For each index in the current distribution, the sum over all previous keys equals x[distIndex][i].
            for (int i = 0; i < sizes[distIndex]; i++) {
                GRBLinExpr expr = new GRBLinExpr();
                for (String prevKey : zPrev.keySet()) {
                    String newKey = prevKey + "_" + i;
                    expr.addTerm(1.0, zNew.get(newKey).var);
                }
                expr.addTerm(-1.0, xList.get(distIndex)[i]);
                model.addConstr(expr, GRB.EQUAL, 0.0, "link_curr_" + distIndex + "_" + i);
            }
            // McCormick constraints for the new bilinear products.
            for (Map.Entry<String, ProductVar> entry : zPrev.entrySet()) {
                String prevKey = entry.getKey();
                ProductVar prevPV = entry.getValue();
                for (int i = 0; i < sizes[distIndex]; i++) {
                    String newKey = prevKey + "_" + i;
                    GRBVar newVar = zNew.get(newKey).var;
                    double L_prev = prevPV.L;
                    double U_prev = prevPV.U;
                    double L_x = (double) marginals.get(distIndex).get(i).getLower();
                    double U_x = (double) marginals.get(distIndex).get(i).getUpper();
                    // Constraint 1: newVar >= L_prev*x[distIndex][i] + L_x*prevPV.var - L_prev*L_x
                    {
                        GRBLinExpr expr = new GRBLinExpr();
                        expr.addTerm(1.0, newVar);
                        expr.addTerm(-L_prev, xList.get(distIndex)[i]);
                        expr.addTerm(-L_x, prevPV.var);
                        expr.addConstant(L_prev * L_x);
                        model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc_" + k + "_1_" + newKey);
                    }
                    // Constraint 2: newVar >= U_prev*x[distIndex][i] + U_x*prevPV.var - U_prev*U_x
                    {
                        GRBLinExpr expr = new GRBLinExpr();
                        expr.addTerm(1.0, newVar);
                        expr.addTerm(-U_prev, xList.get(distIndex)[i]);
                        expr.addTerm(-U_x, prevPV.var);
                        expr.addConstant(U_prev * U_x);
                        model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc_" + k + "_2_" + newKey);
                    }
                    // Constraint 3: newVar <= U_prev*x[distIndex][i] + L_x*prevPV.var - U_prev*L_x
                    {
                        GRBLinExpr expr = new GRBLinExpr();
                        expr.addTerm(U_prev, xList.get(distIndex)[i]);
                        expr.addTerm(L_x, prevPV.var);
                        expr.addConstant(-U_prev * L_x);
                        expr.addTerm(-1.0, newVar);
                        model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc_" + k + "_3_" + newKey);
                    }
                    // Constraint 4: newVar <= L_prev*x[distIndex][i] + U_x*prevPV.var - L_prev*U_x
                    {
                        GRBLinExpr expr = new GRBLinExpr();
                        expr.addTerm(L_prev, xList.get(distIndex)[i]);
                        expr.addTerm(U_x, prevPV.var);
                        expr.addConstant(-L_prev * U_x);
                        expr.addTerm(-1.0, newVar);
                        model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "mcc_" + k + "_4_" + newKey);
                    }
                }
            }
            // Update zPrev to the newly created variables.
            zPrev = zNew;
        }

        //TODO: think about replacing this a-posteriori re-ordering, if it becomes to expensive

        // The final z variables (stored in zPrev) represent the multilinear product relaxation.
        // Save them (in an arbitrary order) to the member variable 'vars' so that your mvMultUnc() can use them.
        // Create a list from the map entries so we can sort them.
        List<Map.Entry<String, ProductVar>> entries = new ArrayList<>(zPrev.entrySet());
        // Sort entries by comparing the numeric parts of the keys in reverse order.
        entries.sort(new Comparator<Map.Entry<String, ProductVar>>() {
            @Override
            public int compare(Map.Entry<String, ProductVar> e1, Map.Entry<String, ProductVar> e2) {
                String[] parts1 = e1.getKey().split("_");
                String[] parts2 = e2.getKey().split("_");
                // Compare from the last index towards the first.
                int len = Math.min(parts1.length, parts2.length);
                for (int i = len - 1; i >= 0; i--) {
                    int cmp = Integer.compare(Integer.parseInt(parts1[i]), Integer.parseInt(parts2[i]));
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            }
        });

        // Build the final sorted list of variables.
        List<GRBVar> finalVars = new ArrayList<>();
        for (Map.Entry<String, ProductVar> entry : entries) {
            finalVars.add(entry.getValue().var);
        }
        this.vars = finalVars.toArray(new GRBVar[finalVars.size()]);
    }

    public void printModel() throws GRBException {
        // Print variables
        System.out.println("Final Variables: ");
        for (GRBVar var : vars) {
            String name = var.get(GRB.StringAttr.VarName);
            System.out.println(name);
        }

        // Print constraints
        System.out.println("Constraints: ");
        // Get all constraints from the model
        GRBConstr[] constraints = model.getConstrs();

        for (GRBConstr constr : constraints) {
            // Get the constraint name
            String constrName = constr.get(GRB.StringAttr.ConstrName);
            // Get the linear expression (the left-hand side) of the constraint
            GRBLinExpr expr = model.getRow(constr);

            // Start printing the constraint
            System.out.print(constrName + ": ");

            // Loop over each term in the expression
            for (int j = 0; j < expr.size(); j++) {
                double coeff = expr.getCoeff(j);
                GRBVar var = expr.getVar(j);
                String varName = var.get(GRB.StringAttr.VarName);

                System.out.print(coeff + " * " + varName);
                if (j < expr.size() - 1) {
                    System.out.print(" + ");
                }
            }

            // Get the sense and right-hand side of the constraint
            char sense = constr.get(GRB.CharAttr.Sense);
            double rhs = constr.get(GRB.DoubleAttr.RHS);

            // Print the sense (e.g. '<', '=', '>') and the RHS
            System.out.println(" " + sense + " " + rhs);
        }
    }

    @Override
    public boolean contains(int j)
    {
        return supportSet.contains(j);
    }

    @Override
    public boolean isSubsetOf(BitSet set) {
        return Reducible.extend(getSupport()).allMatch(set::get);
    }

    @Override
    public boolean containsOneOf(BitSet set)
    {
        return Reducible.extend(getSupport()).anyMatch(set::get);
    }

    @Override
    public Set<Integer> getSupport()
    {
        return supportSet;
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
    public double mvMultUnc(double[] vect, MinMax minMax) {
        try {
            if (support.length == 1) {
                return vect[support[0]];
            }

            // Update each variable's objective coefficient to preserve the current basis.
            for (int i = 0; i < support.length; i++) {
                vars[i].set(GRB.DoubleAttr.Obj, vect[support[i]]);
            }

            // Set the objective sense using the correct attribute.
            model.set(GRB.IntAttr.ModelSense, minMax.isMinUnc() ? GRB.MINIMIZE : GRB.MAXIMIZE);

            // Reoptimize; since only the objective has changed, the previous basis is reused.
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
        String s = "[Polytopic, ";
        s += "Support: " + Arrays.toString(support) + "]";
        return s;
    }
}