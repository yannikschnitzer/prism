package explicit;

import com.gurobi.gurobi.*;
import param.BigRational;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionConstant;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionUnaryOp;
import prism.PrismException;
import prism.PrismLangException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * The ExpressionTranslator class translates PRISM-style linear expressions
 * into Gurobi Expressions, which can be used to define constraints and
 * objectives in a Gurobi optimization model.
 */
public class ExpressionTranslator {

    private final GRBModel model; // Gurobi model to which constraints are added
    private final Map<String, GRBVar> variableMap; // Map to store or retrieve variables by name
    private final Map<Double, GRBVar> constantMap;

    /**
     * Constructor for ExpressionTranslator.
     *
     * @param model The Gurobi model to which constraints and variables are added.
     */
    public ExpressionTranslator(GRBModel model) {
        this.model = model;
        this.variableMap = new HashMap<>();
        this.constantMap = new HashMap<>();
    }

    /**
     * Retrieves the Gurobi optimization model being used.
     *
     * @return The ExpressionsBasedModel instance.
     */
    public GRBModel getModel() {
        return model;
    }

    /**
     * Retrieves or creates a variable in the Gurobi model.
     * If the variable does not exist, it is created and stored in the variableMap.
     *
     * @param name The name of the variable to retrieve or create.
     * @return The Variable instance corresponding to the given name.
     */
    public GRBVar getOrCreateVariable(String name) {
        return variableMap.computeIfAbsent(name, key -> {
            try {
                return model.addVar(-GRB.INFINITY, GRB.INFINITY, 0.0, GRB.CONTINUOUS, name);
            } catch (GRBException e) {
                throw new RuntimeException(e);
            }
        }); // Default lower bound is 0
    }

    public GRBVar getOrCreateConstant(Double value) {
        return constantMap.computeIfAbsent(value, key -> {
            try {
                return model.addVar(value, value, 0.0, GRB.CONTINUOUS, null);
            } catch (GRBException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Translates a PRISM-style linear expression into a Gurobi Expression.
     *
     * @param prismExpression The PRISM expression to translate.
     * @return The Gurobi Expression object representing the constraint.
     * @throws PrismLangException If unsupported or non-linear constructs are encountered.
     */
    public GRBLinExpr translateLinearExpression(parser.ast.Expression prismExpression) throws PrismException {
        GRBLinExpr linearConstraint = new GRBLinExpr(); // Create a new Gurobi Expression
        doTranslate(prismExpression, linearConstraint, 1.0); // Translate the PRISM expression recursively
        return linearConstraint;
    }

    /**
     * Translates a PRISM-style linear expression into a Gurobi Expression with initial multiplier.
     *
     * @param prismExpression The PRISM expression to translate.
     * @return The Gurobi Expression object representing the constraint.
     * @throws PrismLangException If unsupported or non-linear constructs are encountered.
     */
    public GRBLinExpr translateLinearExpression(parser.ast.Expression prismExpression, double multiplier) throws PrismException {
        GRBLinExpr linearConstraint = new GRBLinExpr(); // Create a new Gurobi Expression
        doTranslate(prismExpression, linearConstraint, multiplier); // Translate the PRISM expression recursively
        return linearConstraint;
    }


    /**
     * Recursive helper method to translate a PRISM expression into an Gurobi Expression.
     *
     * @param prismExpression The PRISM expression to process.
     * @param linearConstraint The Gurobi Expression being constructed.
     * @param multiplier Multiplier for the coefficients (for handling negation).
     * @throws PrismLangException If unsupported constructs are encountered.
     */
    private void doTranslate(parser.ast.Expression prismExpression, GRBLinExpr linearConstraint, double multiplier) throws PrismException {
        if (prismExpression instanceof ExpressionBinaryOp op) {
            if (op.getOperator() == ExpressionBinaryOp.TIMES) {
                if (op.getOperand1() instanceof ExpressionLiteral left && op.getOperand2() instanceof ExpressionConstant right) {
                    double coefficient = multiplier;

                    if (left.getValue() instanceof BigRational val) {
                        coefficient *= val.doubleValue();
                    } else if (left.getValue() instanceof BigInteger val) {
                        coefficient *= val.doubleValue();
                    } else {
                        throw new PrismException("Unsupported value type:" + left.getType());
                    }

                    GRBVar variable = getOrCreateVariable(right.getName());
                    linearConstraint.addTerm(coefficient, variable);
                } else {
                    throw new PrismException("Unsupported constraint type");
                }
            } else if (op.getOperator() == ExpressionBinaryOp.PLUS) {
                doTranslate(op.getOperand1(), linearConstraint, multiplier);
                doTranslate(op.getOperand2(), linearConstraint, multiplier);
            } else if (op.getOperator() == ExpressionBinaryOp.MINUS) {
                doTranslate(op.getOperand1(), linearConstraint, multiplier);
                doTranslate(op.getOperand2(), linearConstraint, -multiplier);
            } else {
                throw new PrismException("Unsupported operand type: " + op.getOperatorSymbol());
            }
        } else if (prismExpression instanceof ExpressionUnaryOp op) {
            if (op.getOperator() == ExpressionUnaryOp.MINUS) {
                doTranslate(op.getOperand(), linearConstraint, -multiplier);
            }
        } else if (prismExpression instanceof ExpressionConstant c) {
            // Handle constants, i.e. free variables
            GRBVar variable = getOrCreateVariable(c.getName());
            linearConstraint.addTerm(multiplier, variable);
        } else if (prismExpression instanceof ExpressionLiteral lit) {
            // Handle literal values, i.e., constants
            double value;

            if (lit.getValue() instanceof BigRational val) {
                value = val.doubleValue();
            } else if (lit.getValue() instanceof BigInteger val) {
                value = val.doubleValue();
            } else {
                throw new PrismException("Unsupported value type:" + lit.getType());
            }

            // Create a fixed-value variable to represent the literal
            GRBVar constant = getOrCreateConstant(value);
            linearConstraint.addTerm(multiplier, constant);
        } else {
            throw new PrismException("Unsupported prism expression type");
        }
    }

    /**
     * Formats a GRBLinExpr into a human-readable string.
     *
     * @param expr The linear expression to format.
     * @return A string representing the linear expression.
     * @throws GRBException If there is an issue accessing variable info from Gurobi.
     */
    public static String formatGRBExpression(GRBLinExpr expr) throws GRBException {
        StringBuilder sb = new StringBuilder();

        int numTerms = expr.size(); // number of variable terms
        for (int i = 0; i < numTerms; i++) {
            double coef = expr.getCoeff(i);
            GRBVar var = expr.getVar(i);

            if (i > 0) {
                sb.append(" + ");
            }

            sb.append(coef).append("*").append(var.get(GRB.StringAttr.VarName));
        }

        // Add constant term if nonzero
        double constant = expr.getConstant();
        if (constant != 0.0) {
            if (numTerms > 0) {
                sb.append(" + ");
            }
            sb.append(constant);
        }

        // If there's nothing in the expression (empty), return "0" instead of empty
        if (sb.isEmpty()) {
            sb.append("0");
        }

        return sb.toString();
    }

    public static String formatGBRConstraint(GRBModel model, GRBConstr constr) throws GRBException {
        // 1) Get the LHS as a GRBLinExpr
        GRBLinExpr lhs = model.getRow(constr);

        // 2) Get the sense character (<=, >=, =)
        char sense = constr.get(GRB.CharAttr.Sense);

        // 3) Get the RHS numeric value
        double rhs = constr.get(GRB.DoubleAttr.RHS);

        // 4) Format the LHS expression
        String lhsString = formatGRBExpression(lhs);

        // 5) Convert the sense character to a string
        String senseString = switch (sense) {
            case GRB.LESS_EQUAL -> "<=";
            case GRB.GREATER_EQUAL -> ">=";
            case GRB.EQUAL -> "=";
            default -> "?";
        };

        // 6) Combine into a single string
        return lhsString + " " + senseString + " " + rhs;
    }
}