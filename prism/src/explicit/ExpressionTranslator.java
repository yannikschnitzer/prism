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
import java.util.Map;

public class ExpressionTranslator {

    private final ExpressionsBasedModel model;
    private final Map<String, Variable> variableMap;

    public ExpressionTranslator(ExpressionsBasedModel model) {
        this.model = model;
        this.variableMap = new HashMap<>();
    }

    public ExpressionsBasedModel getModel() {
        return model;
    }

    public Variable getOrCreateVariable(String name) {
        return variableMap.computeIfAbsent(name, key -> model.addVariable(name)); // Default lower bound 0
    }

    /**
     * Translate linear expression as yielded when converting a Function into an Expression, into a OjAlgo expresion.
     * @param prismExpression The prism expression to translate
     * @param linearConstraint The OjAlgo expression to translate into
     * @throws PrismLangException Unsupported or non-linear constraint
     */
    public void translateLinearExpression(parser.ast.Expression prismExpression, Expression linearConstraint) throws PrismException {
        doTranslate(prismExpression, linearConstraint, 1.0);
    }

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

                    System.out.println("Coefficient: " + coefficient);
                    System.out.println("Variable: " + variable);

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
        }  else if (prismExpression instanceof ExpressionConstant c) {
            Variable variable = getOrCreateVariable(c.getName());
            System.out.println("Coefficient: " + multiplier);
            System.out.println("Variable: " + variable);
            linearConstraint.add(variable, multiplier);
        } else if (prismExpression instanceof ExpressionLiteral lit) {
            double value = 0.0;
            if (lit.getValue() instanceof BigRational val) {
                value = val.doubleValue();
            } else if (lit.getValue() instanceof BigInteger val) {
                value = val.doubleValue();
            } else {
                throw new PrismException("Unsupported value type:" + lit.getType());
            }
            Variable constant = model.addVariable().level(value);

            System.out.println("Coefficient: " + multiplier);
            System.out.println("Constant:" + value);
            linearConstraint.add(constant, multiplier);
        }
    }
}

