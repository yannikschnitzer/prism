package explicit;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.Expression;
import param.BigRational;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionConstant;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionUnaryOp;
import prism.PrismException;
import prism.PrismLangException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ExpressionTranslator class translates PRISM-style linear expressions
 * into ojAlgo Expressions, which can be used to define constraints and
 * objectives in an ojAlgo optimization model.
 */
public class ExpressionTranslator {

    private final ExpressionsBasedModel model; // ojAlgo model to which constraints are added
    private final Map<String, Variable> variableMap; // Map to store or retrieve variables by name
    private final Map<Double, Variable> constantMap;

    /**
     * Constructor for ExpressionTranslator.
     *
     * @param model The ojAlgo model to which constraints and variables are added.
     */
    public ExpressionTranslator(ExpressionsBasedModel model) {
        this.model = model;
        this.variableMap = new HashMap<>();
        this.constantMap = new HashMap<>();
    }

    /**
     * Retrieves the ojAlgo optimization model being used.
     *
     * @return The ExpressionsBasedModel instance.
     */
    public ExpressionsBasedModel getModel() {
        return model;
    }

    /**
     * Retrieves or creates a variable in the ojAlgo model.
     * If the variable does not exist, it is created and stored in the variableMap.
     *
     * @param name The name of the variable to retrieve or create.
     * @return The Variable instance corresponding to the given name.
     */
    public Variable getOrCreateVariable(String name) {
        return variableMap.computeIfAbsent(name, key -> model.addVariable(name)); // Default lower bound is 0
    }

    public Variable getOrCreateConstant(Double value) {
        return constantMap.computeIfAbsent(value, key -> model.addVariable().level(value));
    }

    /**
     * Translates a PRISM-style linear expression into an ojAlgo Expression.
     *
     * @param prismExpression The PRISM expression to translate.
     * @return The ojAlgo Expression object representing the constraint.
     * @throws PrismLangException If unsupported or non-linear constructs are encountered.
     */
    public Expression translateLinearExpression(parser.ast.Expression prismExpression) throws PrismException {
        Expression linearConstraint = model.addExpression(); // Create a new ojAlgo Expression
        doTranslate(prismExpression, linearConstraint, 1.0); // Translate the PRISM expression recursively
        return linearConstraint;
    }

    /**
     * Recursive helper method to translate a PRISM expression into an ojAlgo Expression.
     *
     * @param prismExpression The PRISM expression to process.
     * @param linearConstraint The ojAlgo Expression being constructed.
     * @param multiplier Multiplier for the coefficients (for handling negation).
     * @throws PrismLangException If unsupported constructs are encountered.
     */
    private void doTranslate(parser.ast.Expression prismExpression, Expression linearConstraint, double multiplier) throws PrismException {
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

                    Variable variable = getOrCreateVariable(right.getName());
                    linearConstraint.add(variable, coefficient);
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
            Variable variable = getOrCreateVariable(c.getName());
            linearConstraint.add(variable, multiplier);
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
            Variable constant = getOrCreateConstant(value);
            linearConstraint.add(constant, multiplier);
        } else {
            throw new PrismException("Unsupported prism expression type");
        }
    }

    /**
     * Constructs a human-readable string representation of an ojAlgo Expression.
     * Includes variable coefficients and bounds.
     *
     * @param expression The ojAlgo Expression to represent.
     * @param variables List of variables used in the model.
     * @return A formatted string representing the constraint equation.
     */
    public static String getConstraintEquation(Expression expression, List<Variable> variables) {
        StringBuilder equation = new StringBuilder();

        for (Variable variable : variables) {
            double coefficient = expression.get(variable).doubleValue();
            if (coefficient != 0) {
                if (!equation.isEmpty()) {
                    equation.append(" + ");
                }
                equation.append(coefficient).append(" * ").append(variable.getName());
            }
        }

        if (expression.getLowerLimit() != null) {
            equation.insert(0, expression.getLowerLimit() + " <= ");
        }
        if (expression.getUpperLimit() != null) {
            equation.append(" <= ").append(expression.getUpperLimit());
        }

        return equation.toString();
    }
}