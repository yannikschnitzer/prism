package learning.ParametricConvex;

import com.gurobi.gurobi.*;
import param.BigRational;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionConstant;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionUnaryOp;
import prism.PrismException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Translates PRISM expressions to GRB linear expressions.
 * Adds McCormick envelopes for bilinear terms (p*q) and quadratic terms (p^2).
 *
 * IMPORTANT:
 * - We never call GRBVar.get(...) inside this class (robust).
 * - Bounds for McCormick are taken from the local bounds map (default [0.001,0.999]).
 *   If you know tighter bounds, call setVarBounds("p", lb, ub) BEFORE adding any
 *   constraints that mention products/squares of "p".
 * - Products must be monomials (scalar * var, scalar * var*var, or scalar * var^2).
 *   We do not distribute over sums like (a+b)*c; such forms should be pre-expanded
 *   into sums of monomials by the caller/generator.
 */
public class ExpressionTranslator {

    private final GRBModel model;
    // base variables by symbol name
    private final Map<String, GRBVar> variableMap = new HashMap<>();
    // fixed-value "constants" (lb=ub=value)
    private final Map<Double, GRBVar> constantMap = new HashMap<>();
    // cache for auxiliary variables (bilinear/quadratic)
    private final Map<String, GRBVar> auxCache = new HashMap<>();
    // optional per-symbol bounds for McCormick (default [0.001, 0.999])
    private final Map<String, double[]> varBounds = new HashMap<>();

    private final double lowerBoundVar = 0.0;
    private final double upperBoundVar = 1.0;

    // Ordered list of parameter names, in insertion order (only base vars, not aux or constants)
    public final ArrayList<String> paramNames = new ArrayList<>();
    // Fast reverse lookup: parameter GRBVar -> index (identity-based)
    private final java.util.IdentityHashMap<GRBVar, Integer> paramIndex = new java.util.IdentityHashMap<>();

    // reverse map for fixed-value "constant" variables
    private final java.util.IdentityHashMap<GRBVar, Double> constantReverse = new java.util.IdentityHashMap<>();

    public ExpressionTranslator(GRBModel model) {
        this.model = model;
    }

    public GRBModel getModel() {
        return model;
    }

    /** Provide tighter bounds for a parameter symbol used in products/squares. */
    public void setVarBounds(String varName, double lb, double ub) {
        varBounds.put(varName, new double[]{lb, ub});
    }
    private double[] getBounds(String varName) {
        return varBounds.getOrDefault(varName, new double[]{0.001, 0.999});
    }

    /** Create/get a base decision variable for symbol name. */
    public GRBVar getOrCreateVariable(String name) {
        GRBVar existing = variableMap.get(name);
        if (existing != null) return existing;

        try {
            GRBVar v = model.addVar(lowerBoundVar, upperBoundVar, 0.0, GRB.CONTINUOUS, name);
            variableMap.put(name, v);

            // Register as a parameter
            int idx = paramNames.size();
            paramNames.add(name);
            paramIndex.put(v, idx);

            return v;
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    /** Fixed value variable (lb=ub=value) to carry literals into GRBLinExpr. */
    public GRBVar getOrCreateConstant(Double value) {
        return constantMap.computeIfAbsent(value, key -> {
            try {
                GRBVar v = model.addVar(value, value, 0.0, GRB.CONTINUOUS, "c_" + value);
                constantReverse.put(v, value);
                return v;
            } catch (GRBException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Public entry: translate expression to GRBLinExpr, adding constraints as needed. */
    public GRBLinExpr translateLinearExpression(parser.ast.Expression prismExpression) throws PrismException {
        GRBLinExpr linear = new GRBLinExpr();
        doTranslate(prismExpression, linear, 1.0);
        return linear;
    }

    /** Overload with multiplier. */
    public GRBLinExpr translateLinearExpression(parser.ast.Expression prismExpression, double multiplier) throws PrismException {
        GRBLinExpr linear = new GRBLinExpr();
        doTranslate(prismExpression, linear, multiplier);
        return linear;
    }

    // ==================== core recursive translator ====================

    private void doTranslate(parser.ast.Expression expr, GRBLinExpr acc, double mult) throws PrismException {
        try {
            if (expr instanceof ExpressionBinaryOp op) {
                // ---- Addition/Subtraction ----
                if (op.getOperator() == ExpressionBinaryOp.PLUS) {
                    doTranslate(op.getOperand1(), acc, mult);
                    doTranslate(op.getOperand2(), acc, mult);
                    return;
                }
                if (op.getOperator() == ExpressionBinaryOp.MINUS) {
                    doTranslate(op.getOperand1(), acc, mult);
                    doTranslate(op.getOperand2(), acc, -mult);
                    return;
                }

                // ---- Multiplication (monomials only) ----
                if (op.getOperator() == ExpressionBinaryOp.TIMES) {
                    Monomial m = flattenProduct(op);
                    addMonomial(acc, m, mult);
                    return;
                }

                // ---- Division ----
                if (op.getOperator() == ExpressionBinaryOp.DIVIDE) {
                    // (literal)/literal -> constant
                    if (op.getOperand1() instanceof ExpressionLiteral l1 && op.getOperand2() instanceof ExpressionLiteral l2) {
                        double val = literalToDouble(l1) / literalToDouble(l2);
                        acc.addTerm(mult, getOrCreateConstant(val));
                        return;
                    }
                    // (linear)/literal -> scale
                    if (op.getOperand2() instanceof ExpressionLiteral denom) {
                        double d = literalToDouble(denom);
                        doTranslate(op.getOperand1(), acc, mult / d);
                        return;
                    }
                    throw new PrismException("Unsupported division form (denominator must be literal)");
                }

                // ---- Power (only variable^2) ----
                if (op.getOperator() == ExpressionBinaryOp.POW) {
                    if (op.getOperand1() instanceof ExpressionConstant base
                            && op.getOperand2() instanceof ExpressionLiteral lit) {
                        double d = literalToDouble(lit);
                        int n = (int) Math.round(d);
                        if (Math.abs(d - n) > 1e-12 || n != 2) {
                            throw new PrismException("Only variable^2 is supported in POW");
                        }
                        GRBVar z = squareVarByName(base.getName());
                        acc.addTerm(mult, z);
                        return;
                    }
                    throw new PrismException("POW only supported as variable^2");
                }

                throw new PrismException("Unsupported binary operator: " + op.getOperatorSymbol());
            }
            else if (expr instanceof ExpressionUnaryOp uop) {
                if (uop.getOperator() == ExpressionUnaryOp.MINUS) {
                    doTranslate(uop.getOperand(), acc, -mult);
                    return;
                }
                throw new PrismException("Unsupported unary operator");
            }
            else if (expr instanceof ExpressionConstant c) {
                acc.addTerm(mult, getOrCreateVariable(c.getName()));
                return;
            }
            else if (expr instanceof ExpressionLiteral lit) {
                acc.addTerm(mult, getOrCreateConstant(literalToDouble(lit)));
                return;
            }

            throw new PrismException("Unsupported PRISM expression type: " + expr.getClass());
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== Monomial flattening for TIMES ====================

    private static final class Monomial {
        double scalar = 1.0;
        // store up to 2 variables' names (degree ≤ 2 supported)
        final ArrayList<String> vars = new ArrayList<>(2);
    }

    /** Flatten TIMES node into scalar * (var)^a * (var)^b with degree ≤ 2. */
    private Monomial flattenProduct(parser.ast.Expression e) throws PrismException {
        Monomial m = new Monomial();
        collectProduct(e, m);
        if (m.vars.size() > 2)
            throw new PrismException("Only degree-2 monomials supported (saw degree " + m.vars.size() + ")");
        return m;
    }

    private void collectProduct(parser.ast.Expression e, Monomial m) throws PrismException {
        if (e instanceof ExpressionLiteral lit) {
            m.scalar *= literalToDouble(lit);
            return;
        }
        if (e instanceof ExpressionConstant c) {
            m.vars.add(c.getName());
            return;
        }
        if (e instanceof ExpressionBinaryOp bop && bop.getOperator() == ExpressionBinaryOp.TIMES) {
            collectProduct(bop.getOperand1(), m);
            collectProduct(bop.getOperand2(), m);
            return;
        }
        if (e instanceof ExpressionBinaryOp bop && bop.getOperator() == ExpressionBinaryOp.POW) {
            if (bop.getOperand1() instanceof ExpressionConstant base && bop.getOperand2() instanceof ExpressionLiteral lit) {
                double d = literalToDouble(lit);
                int n = (int) Math.round(d);
                if (Math.abs(d - n) > 1e-12 || (n != 2))
                    throw new PrismException("Only variable^2 supported in monomials");
                // add the base variable twice
                m.vars.add(base.getName());
                m.vars.add(base.getName());
                return;
            }
            throw new PrismException("POW must be var^2 inside products");
        }
        // we do not distribute over sums
        throw new PrismException("Non-monomial product term encountered; expand beforehand");
    }

    private void addMonomial(GRBLinExpr acc, Monomial m, double mult) throws GRBException, PrismException {
        double coef = mult * m.scalar;
        if (m.vars.isEmpty()) {
            // pure numeric
            acc.addTerm(coef, getOrCreateConstant(1.0)); // keep constants uniform
            return;
        }
        if (m.vars.size() == 1) {
            GRBVar v = getOrCreateVariable(m.vars.get(0));
            acc.addTerm(coef, v);
            return;
        }
        // degree 2
        String v1 = m.vars.get(0);
        String v2 = m.vars.get(1);
        GRBVar z = bilinearVarByName(v1, v2); // square if same name
        acc.addTerm(coef, z);
    }

    // ==================== McCormick helpers (by symbol name) ====================

    private GRBVar bilinearVarByName(String aName, String bName) throws GRBException {
        final String key = aName + "*" + bName;
        GRBVar z = auxCache.get(key);
        if (z != null) return z;

        GRBVar a = getOrCreateVariable(aName);
        GRBVar b = getOrCreateVariable(bName);
        double[] ba = getBounds(aName);
        double[] bb = getBounds(bName);

        z = ensureBilinearVar(key, a, b, ba[0], ba[1], bb[0], bb[1]);
        auxCache.put(key, z);
        return z;
    }

    private GRBVar squareVarByName(String baseName) throws GRBException {
        final String key = baseName + "^2";
        GRBVar z = auxCache.get(key);
        if (z != null) return z;

        GRBVar x = getOrCreateVariable(baseName);
        double[] bx = getBounds(baseName);

        z = ensureBilinearVar(key, x, x, bx[0], bx[1], bx[0], bx[1]);
        auxCache.put(key, z);
        return z;
    }

    /** Add McCormick envelope constraints for z = a*b over [La,Ua]×[Lb,Ub]. */
    private GRBVar ensureBilinearVar(String name,
                                     GRBVar a, GRBVar b,
                                     double La, double Ua, double Lb, double Ub) throws GRBException {
        GRBVar z = model.addVar(lowerBoundVar, upperBoundVar, 0.0, GRB.CONTINUOUS, name);

        // z >= La*b + Lb*a - La*Lb
        { GRBLinExpr e = new GRBLinExpr();
            e.addTerm(1.0, z); e.addTerm(-La, b); e.addTerm(-Lb, a); e.addConstant(La*Lb);
            model.addConstr(e, GRB.GREATER_EQUAL, 0.0, name+"_mcc1"); }

        // z >= Ua*b + Ub*a - Ua*Ub
        { GRBLinExpr e = new GRBLinExpr();
            e.addTerm(1.0, z); e.addTerm(-Ua, b); e.addTerm(-Ub, a); e.addConstant(Ua*Ub);
            model.addConstr(e, GRB.GREATER_EQUAL, 0.0, name+"_mcc2"); }

        // z <= Ua*b + Lb*a - Ua*Lb   (as >= 0 with -z)
        { GRBLinExpr e = new GRBLinExpr();
            e.addTerm(Ua, b); e.addTerm(Lb, a); e.addConstant(-Ua*Lb); e.addTerm(-1.0, z);
            model.addConstr(e, GRB.GREATER_EQUAL, 0.0, name+"_mcc3"); }

        // z <= La*b + Ub*a - La*Ub   (as >= 0 with -z)
        { GRBLinExpr e = new GRBLinExpr();
            e.addTerm(La, b); e.addTerm(Ub, a); e.addConstant(-La*Ub); e.addTerm(-1.0, z);
            model.addConstr(e, GRB.GREATER_EQUAL, 0.0, name+"_mcc4"); }

        return z;
    }

    // ==================== small utilities ====================
    /** Number of parameter/base variables the translator manages. */
    public int getNumParameters() {
        return paramNames.size();
    }

    /** Index of parameter for this GRBVar, or -1 if it's not a parameter/base var. */
    public int getParamIndex(GRBVar v) {
        Integer idx = paramIndex.get(v);
        return (idx == null) ? -1 : idx;
    }

    /** Lower bound for parameter j (uses the translator's bounds if set, else defaults). */
    public double getParamLB(int j) {
        if (j < 0 || j >= paramNames.size())
            throw new IndexOutOfBoundsException("param index " + j);
        String name = paramNames.get(j);
        return getBounds(name)[0];  // pulls from varBounds or default [0.001, 0.999]
    }

    /** Upper bound for parameter j (uses the translator's bounds if set, else defaults). */
    public double getParamUB(int j) {
        if (j < 0 || j >= paramNames.size())
            throw new IndexOutOfBoundsException("param index " + j);
        String name = paramNames.get(j);
        return getBounds(name)[1];
    }

    /** If v is a fixed-value constant var, return its numeric value; else null. */
    public Double getConstantValueIfKnown(GRBVar v) {
        return constantReverse.get(v);
    }

    private static double literalToDouble(ExpressionLiteral lit) throws PrismException {
        if (lit.getValue() instanceof BigRational br) return br.doubleValue();
        if (lit.getValue() instanceof BigInteger bi)  return bi.doubleValue();
        if (lit.getValue() instanceof Double dbl)     return dbl;
        if (lit.getValue() instanceof Integer i)      return i.doubleValue();
        throw new PrismException("Unsupported literal type: " + lit.getType());
    }

    // Debug formatting (safe to keep; call only after model.update())
    public static String formatGRBExpression(GRBLinExpr expr) throws GRBException {
        StringBuilder sb = new StringBuilder();
        int n = expr.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(" + ");
            sb.append(expr.getCoeff(i)).append("*").append(expr.getVar(i).get(GRB.StringAttr.VarName));
        }
        double c = expr.getConstant();
        if (Math.abs(c) > 0) {
            if (n > 0) sb.append(" + ");
            sb.append(c);
        }
        if (sb.isEmpty()) sb.append("0");
        return sb.toString();
    }

    public static String formatGBRConstraint(GRBModel model, GRBConstr constr) throws GRBException {
        GRBLinExpr lhs = model.getRow(constr);
        char s = constr.get(GRB.CharAttr.Sense);
        double rhs = constr.get(GRB.DoubleAttr.RHS);
        String ss = switch (s) {
            case GRB.LESS_EQUAL -> "<=";
            case GRB.GREATER_EQUAL -> ">=";
            case GRB.EQUAL -> "=";
            default -> "?";
        };
        return formatGRBExpression(lhs) + " " + ss + " " + rhs;
    }
}