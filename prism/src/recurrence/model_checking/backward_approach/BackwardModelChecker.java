package recurrence.model_checking.backward_approach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionUnaryOp;
import parser.ast.ExpressionVar;
import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Update;
import parser.ast.Updates;
import prism.PrismComponent;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLangException;
import recurrence.RecurrenceModelChecker;
import recurrence.data_structure.Pair;
import recurrence.data_structure.recursion.FirstOrderRecurrence;
import recurrence.data_structure.recursion.ReducedRecursion;
import recurrence.log.Level;
import recurrence.log.Log;
import recurrence.utils.Target;

public class BackwardModelChecker extends AbstractBackwardModelChecker
{
	// Matrix solutions
	List<FirstOrderRecurrence> updatedRecEqns;

	public BackwardModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile, String recVar, Expression expr,
			String recur_param, String recur_param_value) throws PrismException
	{
		super(parent, modulesFile, propertiesFile, recVar, expr, recur_param, recur_param_value);
	}

	@Override
	public void computeRegion() throws PrismException
	{
		// Declare the range variables
		Integer low, high;

		// Retrieve the type of the declaration
		DeclarationInt decInt = null;
		if (decl_recur_var.getDeclType() instanceof DeclarationInt)
			decInt = (DeclarationInt) decl_recur_var.getDeclType();
		else
			throw new PrismLangException("The recurrence variable " + recur_var + " is not a type of Integer");

		low = decInt.getLow().evaluateInt() + 1;
		high = decInt.getHigh().evaluateInt() - 1;

		// Assumes there is only one module in the model description
		Module module = modules_file.getModule(0);

		for (Command c : module.getCommands()) {
			Updates updates = c.getUpdates();
			int num_updates = updates.getNumUpdates();

			for (int i = 0; i < num_updates; i++) {
				Expression p = updates.getProbability(i);
				if (p != null && p.getAllVars().contains(recur_var)) {
					throw new PrismException("This algorithm does not support this model as the probability expression" + " contains the recurrence variable");
				}

				Update u = updates.getUpdate(i);
				for (int j = 0; j < u.getNumElements(); j++) {
					// Currently assumes interval is 1. It is not implemented as I have to 
					Expression update = u.getExpression(j);
					if (update.getAllVars().contains(recur_var)) {
						if (u.getVarIndex(j) != recur_var_index) {
							throw new PrismException("This algorithm does not support this model as another variable depends on the recurrence variable");
						} else {
							ExpressionBinaryOp expr = (ExpressionBinaryOp) update;
							if (expr.getOperator() != 11) {
								throw new PrismException("This algorithm does not support this model as it only supports plus operator in the update");
							} else {
								Expression x = expr.getOperand1();
								Expression y = expr.getOperand2();
								if ((x instanceof ExpressionLiteral && ((ExpressionLiteral) x).evaluateInt() != 1)
										|| (y instanceof ExpressionLiteral && ((ExpressionLiteral) y).evaluateInt() != 1)) {
									throw new PrismException("This algorithm does not support this model as it only supports interval of 1");
								}
							}
						}
					}
				}
			}

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
		}
		setRecurrenceInterval(new Pair<Integer, Integer>(low, high));
	}

	/**
	 * Extract the bounds from the guard expressions.
	 * @param expression guard expression
	 * @param result the number of ranges
	 * @throws PrismLangException
	 */
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
			} else if (bin_expr.getOperand1().toString().equals(recur_var)) {
				result.setFirst(op);
				result.setSecond(bin_expr.getOperand2().evaluateInt());
			}
		}
	}

	public void identifyForwardKeyStates() throws PrismException
	{
		// Retrieving the index of the recurrent variable
		recur_var_index = modules_file.getVarIndex(recur_var);
		// Retrieving the list of states from the first model
		relevant_states = first_model.getStatesList();

		first_entry = new ArrayList<State>();
		first_exit = new ArrayList<State>();

		// The list of necessary states
		first_rep = new ArrayList<State>(); // These states are needed for the
		second_rep = new ArrayList<State>(); // recurring comparison & init also need for compute prob
		second_last_rep = new ArrayList<State>();
		last_rep = new ArrayList<State>();

		// Initialize the size of the recurrent variable that will updated as the states are identified
		recurrent_block_size = 0;

		// Store the known initial states
		for (State state : relevant_states) {
			int currentIndex = relevant_states.indexOf(state);
			Iterator<Integer> it = first_model.getSuccessorsIterator(currentIndex);

			if (state.var_values[recur_var_index].equals(init_val - 1)) {
				while (it.hasNext()) {
					Integer successorIndex = it.next();
					State successor = relevant_states.get(successorIndex);
					if (successor.var_values[recur_var_index].equals(init_val) && !first_entry.contains(successor))
						first_entry.add(successor);
				}
			} else if (state.var_values[recur_var_index].equals(init_val)) {
				recurrent_block_size++;
				while (it.hasNext()) {
					Integer successorIndex = it.next();
					State successor = relevant_states.get(successorIndex);
					if (successor.var_values[recur_var_index].equals(init_val + 1) && !first_exit.contains(state))
						first_exit.add(state);
				}
			}
		}

		// Assigning the representative states either exit or entry
		if (first_entry.size() > first_exit.size()) {
			first_rep = new ArrayList<State>(first_exit);
			isEntryRep = false;
		} else
			first_rep = new ArrayList<State>(first_entry);

		// Check whether initial and exit states are the same
		Set<State> tmp_init = new HashSet<State>(first_entry);
		Set<State> tmp_exit = new HashSet<State>(first_exit);

		boolean isOneRowBlock = tmp_init.equals(tmp_exit);
		if (!isOneRowBlock) {
			// Generate the second representative states based on the first
			for (State state : first_rep) {
				State secondEntryState = new State(state);
				secondEntryState.var_values[recur_var_index] = init_val + 1;
				second_rep.add(secondEntryState);
			}
		} else {
			second_rep.clear();
			for (State state : first_exit) {
				int currentIndex = relevant_states.indexOf(state);
				Iterator<Integer> it = first_model.getSuccessorsIterator(currentIndex);
				while (it.hasNext()) {
					Integer successorIndex = it.next();
					State successor = relevant_states.get(successorIndex);
					if (successor.var_values[recur_var_index].equals(init_val + 1) && !second_rep.contains(successor))
						second_rep.add(successor);
				}
			}
			// Explore all parallel coexisting states (that are currently unreachable)
			ArrayList<State> tmp = new ArrayList<State>(second_rep);
			// Reset the second entry states
			second_rep.clear();

			for (int i = 0; i < tmp.size(); i++) {
				State tmpState = new State(tmp.get(i));
				tmpState.setValue(recur_var_index, init_val);
				if (!first_rep.contains(tmpState)) {
					first_rep.add(tmpState);
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
		for (State state : first_rep) {
			if (isOneRowBlock) {
				recurrent_block_size++;
				State secondEntryState = new State(state);
				secondEntryState.var_values[recur_var_index] = init_val + 1;
				second_rep.add(secondEntryState);
			}
			State prevFinalEntryState = new State(state);
			prevFinalEntryState.var_values[recur_var_index] = end_val - 1;
			second_last_rep.add(prevFinalEntryState);
			State finalEntryState = new State(state);
			finalEntryState.var_values[recur_var_index] = end_val;
			last_rep.add(finalEntryState);
		}

		// Sort all the list of states
		Collections.sort(first_rep);
		Collections.sort(second_rep);
		Collections.sort(second_last_rep);
		Collections.sort(last_rep);
	}

	@Override
	public void process() throws PrismException
	{
		double time_it = System.currentTimeMillis();

		// Ignore the PrismLog for now
		parent.setLog(new PrismDevNullLog());
		// Construct the first region
		Log.p(Level.INFO, "Construction of FIRST Region", this.getClass());
		constructFirstRegion();

		// Construct the second region
		Log.p(Level.INFO, "Construction of SECOND Region", this.getClass());
		constructSecondRegion(second_last_rep);

		// Construct the third region
		Log.p(Level.INFO, "Construction of THIRD Region", this.getClass());
		constructThirdRegion(last_rep);
		System.out.println("Construction Time: " + (System.currentTimeMillis() - time_it));

		time_it = System.currentTimeMillis();

		// Identify the targets lies in all three models
		targ = new Target(first_model, second_model, third_model, properties_file, recur_var_index);
		// Change all the target states lie in the recurrent blocks into absorbing states
		fixTargetStates();
		// Compute the base probabilities of the recurrence relations
		computeRecurBaseProbs(last_rep);
		// Derive required recurrence relations	
		computeRecurTransProb(last_rep, second_last_rep);

		// Compute the end probabilities
		Log.p(Level.INFO, "Computing End Probabilities", this.getClass());
		computeEndProbs(first_rep);

		System.out.println("Forming RRs Time: " + (System.currentTimeMillis() - time_it));

		if (RecurrenceModelChecker.IS_FUNCTION) {
			// derive reduced recurrence equations
			time_it = System.currentTimeMillis();
			solve(last_rep.size());
			System.out.println("Solving RRs Time: " + (System.currentTimeMillis() - time_it));
			
			if (RecurrenceModelChecker.IS_QUANTILE) {
				// compute final result
				time_it = System.currentTimeMillis();
				computeQuantileProbUsingFunc(second_last_rep);
			} else {
				// compute final result
				time_it = System.currentTimeMillis();
				computeFuncAndTotalProbability(second_last_rep);
			}
		} else {
			// derive reduced recurrence equations
			time_it = System.currentTimeMillis();
			solveX(last_rep.size());
			System.out.println("Solving RRs Time: " + (System.currentTimeMillis() - time_it));

			// compute final result
			time_it = System.currentTimeMillis();
			computeTotalProbabilityX(second_last_rep);
		}
		Log.p(Level.INFO, "Computing Total Probability", this.getClass());
		System.out.println("Evaluating RRs Time: " + (System.currentTimeMillis() - time_it));
		//Log.p(Level.INFO, this, this.getClass());
	}

	/**
	 * Replaces all the transition of the target states with a self loop
	 * @throws PrismException
	 */
	public void fixTargetStates() throws PrismException
	{
		ParamModel pm1 = (ParamModel) first_model;
		for (Integer index : targ.getStates(1))
			pm1.replaceAllForSelfLoop(index, 0);
		first_model = pm1;

		ParamModel pm2 = (ParamModel) second_model;
		for (Integer index : targ.getStates(2))
			pm2.replaceAllForSelfLoop(index, 0);
		second_model = pm2;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder("");
		sb.append("Variable : " + recur_var + "\n");
		sb.append("Range : [ Low : " + init_val + ", High : " + end_val + " ] \n");
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
		sb.append("Recur Sates Size : " + recurrent_block_size + "\n\n");
		sb.append("Entry states Size :" + first_entry.size() + "\n");
		sb.append("Exit states Size :" + first_exit.size() + "\n");
		sb.append("First Model Size :" + first_model.getNumStates() + "\n");
		sb.append("Second Model Size :" + second_model.getNumStates() + "\n");
		sb.append("Third Model Size :" + third_model.getNumStates() + "\n");

		if (all_recur_eqns != null) {
			sb.append("[ Ordinary Generating Functions ]\n");
			sb.append("================================= \n");
			for (int i = 0; i < all_recur_eqns.size(); i++)
				sb.append("[" + (i + 1) + "] " + all_recur_eqns.get(i) + "\n");
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

			sb.append("End region Probability : " + final_probs + "\n");
			sb.append("Total Probability : " + result);
		}
		return sb.toString();
	}

}
