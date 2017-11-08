package recurrence.model_checking.backward_approach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import param.ParamModel;
import parser.State;
import parser.ast.Command;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionTemporal;
import parser.ast.ExpressionUnaryOp;
import parser.ast.ExpressionVar;
import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Update;
import parser.ast.Updates;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLangException;
import recurrence.data_structure.Pair;
import recurrence.data_structure.recursion.FirstOrderRecurrence;
import recurrence.data_structure.recursion.ReducedRecursion;
import recurrence.log.Level;
import recurrence.log.Log;
import recurrence.utils.Target;

public class BackwardModelChecker extends AbstractBackwardModelChecker
{
	// matrix solutions
	List<FirstOrderRecurrence> updatedRecEqns;

	public BackwardModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile, String recVar, Expression expr)
			throws PrismException
	{
		super(parent, modulesFile, propertiesFile, recVar, expr);
	}

	@Override
	public List<Pair<Integer, Integer>> computeRegion() throws PrismLangException
	{
		// Declare the range variables
		Integer low, high;
		// Initialize the list of the ranges
		List<Pair<Integer, Integer>> range = new LinkedList<Pair<Integer, Integer>>();
		// Retrieve the declaration of the recurrence variable from the modules file
		Declaration dec = modulesFile.getVarDeclaration(modulesFile.getVarIndex(recurVar));
		// Retrieve the type of the declaration
		DeclarationInt decInt = null;
		if (dec.getDeclType() instanceof DeclarationInt)
			decInt = (DeclarationInt) dec.getDeclType();
		else
			throw new PrismLangException("The recurrence variable " + recurVar + " is not a type of Integer");

		low = decInt.getLow().evaluateInt() + 1;
		high = decInt.getHigh().evaluateInt() - 1;

		// Assumes there is only one module in the model description
		Module module = modulesFile.getModule(0);

		for (Command c : module.getCommands()) {
			Expression g = c.getGuard();

			Pair<Integer, Integer> r = new Pair<Integer, Integer>(Integer.MIN_VALUE, 0);
			extractBounds(g, r);
			int op = r.first();
			// Update the bounds
			if (r.first() == Integer.MIN_VALUE)
				continue;
			else {
				int val = r.second();
				if (op == 5 || op == 6) {
					if (Math.abs(val - low) > Math.abs(val - high)) {
						if (high > val - 1)
							high = val - 1;
					} else {
						if (low < val + 1)
							low = val + 1;
					}
				} else if (op == 7) {
					if (low < val + 1)
						low = val + 1;
				} else if (op == 8) {
					if (low < val)
						low = val;
				} else if (op == 9) {
					if (high > val - 1)
						high = val - 1;
				} else {
					if (high > val)
						high = val;
				}
			}

			Updates updates = c.getUpdates();
			int num_updates = updates.getNumUpdates();

			for (int i = 0; i < num_updates; i++) {
				// TODO : currently does not check probability. This needs to be sorted out.
				// Reason : It cannot be checked for existence of recurVar as it is already replaced.
				Expression p = updates.getProbability(i);

				Update u = updates.getUpdate(i);
				for (int j = 0; j < u.getNumElements(); j++) {
					// TODO : currently assumes interval is 1. It is not implemented as I have to 
					// figure out how to to check the existence of the variable in the expression.
					// System.out.println(u.getExpression(j));
				}
			}
		}
		// Add the range to the list
		range.add(new Pair<Integer, Integer>(low, high));
		return range;
	}

	private void extractBounds(Expression expression, Pair<Integer, Integer> result) throws PrismLangException
	{
		/**
		 * 	EQ = 5;
		 *	NE = 6;
		 *  GT = 7;
		 *  GE = 8;
		 *  LT = 9;
		 *  LE = 10;
		 */
		Expression expr = expression;
		// remove brackets
		if (expr instanceof ExpressionUnaryOp) {
			expr = ((ExpressionUnaryOp) expr).getOperand();
			extractBounds(expr, result);
		}
		// boolean variables
		else if (expr instanceof ExpressionVar) {
			return;
		} else {
			// cast it as binary expression
			ExpressionBinaryOp bin_expr = (ExpressionBinaryOp) expr;
			int op = bin_expr.getOperator();

			if (op < 5 || op > 10) {
				extractBounds(bin_expr.getOperand1(), result);
				extractBounds(bin_expr.getOperand2(), result);
			} else if (bin_expr.getOperand1().toString().equals(recurVar)) {
				result.setFirst(op);
				result.setSecond(bin_expr.getOperand2().evaluateInt());
			}
		}
	}

	public void identifyForwardKeyStates() throws PrismException
	{
		// Retrieving the index of the recurrent variable
		recurVarIndex = modulesFile.getVarIndex(recurVar);
		// Retrieving the list of states from the first model
		relevantStates = firstModel.getStatesList();

		firstEntryStates = new ArrayList<State>();
		firstExitStates = new ArrayList<State>();

		// The list of necessary states
		firstRepStates = new ArrayList<State>(); // These states are needed for the
		secondRepStates = new ArrayList<State>(); // recurring comparison & init also need for compute prob
		prevFinalRepStates = new ArrayList<State>();
		finalRepStates = new ArrayList<State>();

		// Initialize the size of the recurrent variable that will updated as the states are identified
		recurBlockSize = 0;

		// Store the known initial states
		for (State state : relevantStates) {
			int currentIndex = relevantStates.indexOf(state);
			Iterator<Integer> it = firstModel.getSuccessorsIterator(currentIndex);

			if (state.varValues[recurVarIndex].equals(initVal - 1)) {
				while (it.hasNext()) {
					Integer successorIndex = it.next();
					State successor = relevantStates.get(successorIndex);
					if (successor.varValues[recurVarIndex].equals(initVal) && !firstEntryStates.contains(successor))
						firstEntryStates.add(successor);
				}
			} else if (state.varValues[recurVarIndex].equals(initVal)) {
				recurBlockSize++;
				while (it.hasNext()) {
					Integer successorIndex = it.next();
					State successor = relevantStates.get(successorIndex);
					if (successor.varValues[recurVarIndex].equals(initVal + 1) && !firstExitStates.contains(state))
						firstExitStates.add(state);
				}
			}
		}

		// Assigning the representative states either exit or entry
		if (firstEntryStates.size() > firstExitStates.size()) {
			firstRepStates = new ArrayList<State>(firstExitStates);
			isEntryRep = false;
		} else
			firstRepStates = new ArrayList<State>(firstEntryStates);

		// Check whether initial and exit states are the same
		Set<State> tmp_init = new HashSet<State>(firstEntryStates);
		Set<State> tmp_exit = new HashSet<State>(firstExitStates);

		boolean isOneRowBlock = tmp_init.equals(tmp_exit);
		if (!isOneRowBlock) {
			// Generate the second representative states based on the first
			for (State state : firstRepStates) {
				State secondEntryState = new State(state);
				secondEntryState.varValues[recurVarIndex] = initVal + 1;
				secondRepStates.add(secondEntryState);
			}
		} else {
			secondRepStates.clear();
			for (State state : firstExitStates) {
				int currentIndex = relevantStates.indexOf(state);
				Iterator<Integer> it = firstModel.getSuccessorsIterator(currentIndex);
				while (it.hasNext()) {
					Integer successorIndex = it.next();
					State successor = relevantStates.get(successorIndex);
					if (successor.varValues[recurVarIndex].equals(initVal + 1) && !secondRepStates.contains(successor))
						secondRepStates.add(successor);
				}
			}
			// Explore all parallel coexisting states (that are currently unreachable)
			ArrayList<State> tmp = new ArrayList<State>(secondRepStates);
			// Reset the second entry states
			secondRepStates.clear();

			for (int i = 0; i < tmp.size(); i++) {
				State tmpState = new State(tmp.get(i));
				tmpState.setValue(recurVarIndex, initVal);
				if (!firstRepStates.contains(tmpState)) {
					firstRepStates.add(tmpState);
					modelGenSym.exploreState(tmpState);
					int n = modelGenSym.getNumTransitions();
					// Find the successor 
					for (int j = 0; j < n; j++) {
						State newState = modelGenSym.computeTransitionTarget(0, j);
						if (!(tmpState.equals(newState) || tmp.contains(newState))) {
							tmp.add(newState);
							i--;
						}
					}
				}
			}
		}

		// Introduce all the necessary states with respect to the identified initial states
		for (State state : firstRepStates) {
			if (isOneRowBlock) {
				recurBlockSize++;
				State secondEntryState = new State(state);
				secondEntryState.varValues[recurVarIndex] = initVal + 1;
				secondRepStates.add(secondEntryState);
			}
			State prevFinalEntryState = new State(state);
			prevFinalEntryState.varValues[recurVarIndex] = endVal - 1;
			prevFinalRepStates.add(prevFinalEntryState);
			State finalEntryState = new State(state);
			finalEntryState.varValues[recurVarIndex] = endVal;
			finalRepStates.add(finalEntryState);
		}

		// Sort all the list of states
		Collections.sort(firstRepStates);
		Collections.sort(secondRepStates);
		Collections.sort(prevFinalRepStates);
		Collections.sort(finalRepStates);
	}

	@Override
	public void process() throws PrismException
	{
		double time_it = System.currentTimeMillis();
		// Construct the first region
		Log.p(Level.INFO, "Construction of FIRST Region", this.getClass());
		constructFirstRegion();
		// Construct the second region
		Log.p(Level.INFO, "Construction of SECOND Region", this.getClass());
		constructSecondRegion(prevFinalRepStates);
		// Construct the third region
		Log.p(Level.INFO, "Construction of THIRD Region", this.getClass());
		constructThirdRegion(finalRepStates);
		System.out.println("Construction Time: " + (System.currentTimeMillis() - time_it));

		time_it = System.currentTimeMillis();
		if (isRecurring()) {
			// Identify the targets lies in all three models
			targ = new Target(firstModel, secondModel, thirdModel, propertiesFile, recurVarIndex);
			// For all the target states lies in the recurrent block change the transition probability to itself with 1
			fixTargetStates();
			// compute the primary and second region base probs
			computeRecurBaseProbs(finalRepStates);
			// derive base and transition probabilities for recurrence solving	
			computeRecurTransProb(finalRepStates, prevFinalRepStates);
			// compute the probs
			Log.p(Level.INFO, "Computing End Probabilities", this.getClass());
			computeEndProbs(firstRepStates);
			// System.out.println(finalProbs);

			System.out.println("Forming RRs Time: " + (System.currentTimeMillis() - time_it));
			// derive reduced recurrence equations
			time_it = System.currentTimeMillis();
			solve2(finalRepStates.size());
			System.out.println("Solving RRs Time: " + (System.currentTimeMillis() - time_it));

			// compute altogether
			time_it = System.currentTimeMillis();
			computeTotalProbability2(prevFinalRepStates);
			Log.p(Level.INFO, "Computing Total Probability", this.getClass());
			System.out.println(result);
			System.out.println("Evaluating RRs Time: " + (System.currentTimeMillis() - time_it));
			//Log.p(Level.INFO, this, this.getClass());
		}
	}

	@Override
	public boolean isRecurring() throws PrismException
	{
		Expression targetProp = ((ExpressionTemporal) ((ExpressionProb) propertiesFile.getProperty(0)).getExpression()).getOperand2();

		/**
		 * This is trivial recurring block hence recurrence is not checked, however, we confirm whether 
		 * 	1) the target state lies in the recurrent block 
		 * 	2) and if so then is it repeating
		 */
		for (int i = 0; i < firstRepStates.size(); i++) {
			if (targetProp.evaluateBoolean(firstRepStates.get(i)) && !targetProp.evaluateBoolean(secondRepStates.get(i)))
				throw new PrismException("Target states lies in the recurrent block and does not recur");
		}
		return true;
	}

	public void fixTargetStates() throws PrismException
	{
		ParamModel pm1 = (ParamModel) firstModel;
		for (Integer index : targ.getStates(1))
			pm1.replaceAllForSelfLoop(index, 0);
		firstModel = pm1;

		ParamModel pm2 = (ParamModel) secondModel;
		for (Integer index : targ.getStates(2))
			pm2.replaceAllForSelfLoop(index, 0);
		secondModel = pm2;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder("");
		sb.append("Variable : " + recurVar + "\n");
		sb.append("Range : [ Low : " + initVal + ", High : " + endVal + " ] \n");
		//		String tmp = "(0)=" + relevantStates.get(0);
		//		for (int i = 1; i < relevantStates.size(); i++) {
		//			tmp += ", (" + i + ")=" + relevantStates.get(i);
		//		}

		//		sb.append("All States : " + tmp + "\n");
		//		sb.append("Entry States (" + initVal + ") : " + firstEntryStates + "\n");
		//		sb.append("Exit States (" + initVal + ") : " + firstExitStates + "\n");
		//		sb.append("Rep States (" + initVal + ") : " + firstRepStates + "\n");
		//		sb.append("Rep States (" + (initVal + 1) + ") : " + prevFinalRepStates + "\n");
		//		sb.append("Rep States (" + endVal + ") : " + finalRepStates + "\n");
		//		sb.append("Recur Base Probs : " + recurBaseProbs + "\n");
		//		sb.append("Recur Trans Probs : " + recurTrans + "\n");
		//		sb.append("Final Probs : " + finalProbs + "\n");
		sb.append("Recur Sates Size : " + recurBlockSize + "\n\n");
		sb.append("Entry states Size :" + firstEntryStates.size() + "\n");
		sb.append("Exit states Size :" + firstExitStates.size() + "\n");
		sb.append("First Model Size :" + firstModel.getNumStates() + "\n");
		sb.append("Second Model Size :" + secondModel.getNumStates() + "\n");
		sb.append("Third Model Size :" + thirdModel.getNumStates() + "\n");

		if (recurEqns != null) {
			sb.append("[ Ordinary Generating Functions ]\n");
			sb.append("================================= \n");
			for (int i = 0; i < recurEqns.size(); i++)
				sb.append("[" + (i + 1) + "] " + recurEqns.get(i) + "\n");
		}
		if (ogffractions != null) {
			sb.append("\nSolutions for OGFs\n");
			sb.append("==================\n");
			sb.append("\nFractions (after solving simultaneous equations) : \n");
			for (int i = 0; i < ogffractions.length; i++)
				sb.append("[" + (i + 1) + "] " + ogffractions[i] + "\n");
		}
		if (solutions != null) {
			sb.append("\nReduced Recursion Equations :\n");
			sb.append("=============================\n");

			for (Entry<Integer, ReducedRecursion> entry : solutions.entrySet()) {
				sb.append("\n{\n");
				sb.append("\tState Index (in recursive sample block): " + entry.getKey() + "\n");
				sb.append(entry.getValue());
				sb.append("\n}\n");
			}

			sb.append("End region Probability : " + finalProbs + "\n");
			sb.append("Total Probability : " + result);
		}
		return sb.toString();
	}

}
