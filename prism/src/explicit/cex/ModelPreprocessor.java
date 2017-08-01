package explicit.cex;

import java.util.ArrayList;
import java.util.BitSet;

import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionLabel;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionTemporal;
import parser.ast.ExpressionUnaryOp;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Property;
import parser.ast.RelOp;
import parser.type.TypeDouble;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;
import prism.UndefinedConstants;
import explicit.Model;
import explicit.cex.util.CexParams;

public class ModelPreprocessor
{
	
	private final SimplifiedExprProb expr;
	private final BitSet remainSet;
	private final BitSet targetSet;
	private final Model modelWithNewLabels;

	public ModelPreprocessor(Model currentModelExpl, Property prop, UndefinedConstants constValuation, ModulesFile currentModulesFile, PropertiesFile propertiesFile, Prism prism) throws PrismException {
		PrismLog mainLog = prism.getMainLog();
		
		// Create the bitsets that we refer to in counterexample computation
		// TODO: Move into SimplifiedExprProb
		explicit.StateModelChecker mc = explicit.StateModelChecker.createModelChecker(currentModelExpl.getModelType(), prism);
		mc.setModulesFileAndPropertiesFile(currentModulesFile, propertiesFile);
		ArrayList<String> propNames = new ArrayList<String>();
		ArrayList<BitSet> propBSs = new ArrayList<BitSet>();
		Expression exprNew = mc.checkMaximalPropositionalFormulas(currentModelExpl, prop.getExpression().deepCopy(), propNames, propBSs);
		
		mainLog.println("Modified property: " + exprNew);

		// Extract threshold and target set from property
		try {
			expr = new SimplifiedExprProb(exprNew, constValuation);
		} catch (IllegalArgumentException e) {
			throw new PrismException(e.getMessage());
		}

		mainLog.println("Simplified property: " + expr);

		mainLog.println("New labels:");
		for (int i = 0; i < propNames.size(); i++) {
			BitSet set = propBSs.get(i);
			mainLog.println((i+1) + ". " + propNames.get(i) + ": " + set.cardinality() + " states");
		}

		String remainLabel = "";
		BitSet remainSet = null;
		String targetLabel = "";
		BitSet targetSet = null;

		for (int i = 0; i < propNames.size(); i++) {
			if (expr.isRemainLabel(propNames.get(i))) {
				remainLabel = propNames.get(i);
				remainSet = propBSs.get(i);
			}
			if (expr.isTargetLabel(propNames.get(i))) {
				targetLabel = propNames.get(i);
				targetSet = propBSs.get(i);
			}
		}
		assert (targetSet != null);
		this.targetSet = targetSet;
		this.remainSet = remainSet;

		mainLog.println("Target label: " + targetLabel + "; Probability bound: " + expr.getProbability());

		modelWithNewLabels = currentModelExpl;
	}

	public CexParams getCexParams() throws PrismLangException {
		return new CexParams(expr.getProbability(), CexParams.UNBOUNDED, modelWithNewLabels, expr.getTargetLabel(), targetSet);
	}
	
	private class SimplifiedExprProb {
		public RelOp relop = null;
		public Expression prob = null;
		public Expression stepBound = null;
		public Expression operand1 = null;
		public Expression operand2 = null;
		
		private boolean isNegated = false;
		private UndefinedConstants constValuation;
		
		private void invertProb() {
			// prob = 1 - prob
			prob = new ExpressionBinaryOp(ExpressionBinaryOp.MINUS, new ExpressionLiteral(TypeDouble.getInstance(), 1.0), prob);
			
			// invert operator
			switch (relop) {
			case EQ:
				break;
			case GEQ:
				relop = RelOp.LT;
				break;
			case GT:
				relop = RelOp.LEQ;
				break;
			case LEQ:
				relop = RelOp.GT;
				break;
			case LT:
				relop = RelOp.GEQ;
				break;
			default:
				throw new IllegalStateException("Illegal operator " + relop);
			}
		}
		
		@Override
		public String toString() {
			return (isNegated ? "! " : "") + "P" + relop + prob + " [ " + operand1 + " U " + operand2 + " ]";
		}
		
		public String getTargetLabel() {
			try {
				return ((ExpressionLabel)operand2).getName();
			} catch (Exception e) {
				return null;
			}
		}
		
		public String getRemainLabel() {
			try {
				return ((ExpressionLabel)operand1).getName();
			} catch (Exception e) {
				return null;
			}
		}
		
		public boolean isTargetLabel(String label) {
			if (operand2 == null || operand2 instanceof ExpressionLiteral) {
				return false;
			} else {
				assert(operand2 instanceof ExpressionLabel);
				return ((ExpressionLabel)operand2).getName().equals(label);
			}
		}
		
		public boolean isRemainLabel(String label) {
			if (operand1 == null || operand1 instanceof ExpressionLiteral) {
				return false;
			} else {
				assert(operand1 instanceof ExpressionLabel);
				return ((ExpressionLabel)operand1).getName().equals(label);
			}
		}
		
		public SimplifiedExprProb(Expression expr, UndefinedConstants constValuation) {
			this.constValuation = constValuation;
			
			if (expr instanceof ExpressionProb) {
				ExpressionProb exprProb = (ExpressionProb)expr;
				
				prob = exprProb.getProb();
				relop = exprProb.getRelOp();
				
				// Go into the contained temporal expression
				expr = exprProb.getExpression();
				while (!(expr instanceof ExpressionTemporal)) {
					if (!(expr instanceof ExpressionUnaryOp)) {
						throw new IllegalArgumentException("Only simple path formulas are allowed as arguments to P operators in counterexample generation");
					}
					
					ExpressionUnaryOp exprUnop = (ExpressionUnaryOp) expr;
					if (exprUnop.getOperator() == ExpressionUnaryOp.NOT) {
						isNegated = !isNegated;
					}
					expr = exprUnop.getOperand();
				}
				
				// Convert to until formula
				try {
					expr = ((ExpressionTemporal)expr).convertToUntilForm();
				} catch (PrismLangException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// The conversion might have introduced another negation,
				// get rid of that by inverting the operator and probability goal
				if (!(expr instanceof ExpressionTemporal)) {
					assert (expr instanceof ExpressionUnaryOp);
					
					ExpressionUnaryOp exprUnop = (ExpressionUnaryOp) expr;
					if (exprUnop.getOperator() == ExpressionUnaryOp.NOT) {
						invertProb();
					}
					expr = exprUnop.getOperand();
				}
				
				ExpressionTemporal untilFormula = (ExpressionTemporal)expr;
				operand1 = untilFormula.getOperand1();
				operand2 = untilFormula.getOperand2();
				stepBound = untilFormula.getUpperBound();
				
				// Change relational operator if applicable
				if (relop == RelOp.GEQ) {
					isNegated = !isNegated;
					relop = RelOp.LT;
				}
				if (relop == RelOp.GT) {
					isNegated = !isNegated;
					relop = RelOp.LEQ;
				}
			} else {
				throw new IllegalArgumentException("Can only compute counterexamples for P operators");
			}
		}

		public double getProbability() throws PrismLangException
		{
			return prob.evaluateDouble(constValuation.getPFConstantValues());
		}
		
		public int getStepBound() throws PrismLangException {
			return stepBound == null ? CexParams.UNBOUNDED : stepBound.evaluateInt(constValuation.getPFConstantValues()); 
		}
	}

	public Model doPreprocessing()
	{
		// TODO Preprocessing
		return modelWithNewLabels;
	}
	
}
