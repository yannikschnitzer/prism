package recurrence.model_checking.backward_approach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import param.BigRational;
import param.Function;
import param.ModelBuilder;
import param.ParamResult;
import param.StateValues;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionFilter;
import parser.ast.ExpressionFilter.FilterOperator;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.PrismComponent;
import prism.PrismException;
import prism.Result;
import recurrence.data_structure.Pair;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.data_structure.recursion.FirstOrderRecurrence;
import recurrence.data_structure.recursion.OrdinaryGeneratingFunction;
import recurrence.data_structure.recursion.ReducedRecursion;
import recurrence.log.Level;
import recurrence.log.Log;
import recurrence.math.matrix.GJEPolyFraction;
import recurrence.math.matrix.MatrixHelper;
import recurrence.math.partialfraction.Decomposition;
import recurrence.model_checking.AbstractModelChecker;
import recurrence.utils.Target;

public abstract class AbstractBackwardModelChecker extends AbstractModelChecker
{
	// Required list of states for solving recurrence relations 
	List<State> firstRepStates, firstEntryStates, firstExitStates, secondRepStates, prevFinalRepStates, finalRepStates;
	// List of target states lies in the recurrent block
	Target targ;
	// Recurrence Variable List
	List<Integer> recur_var_list;
	// Chosen Representative Indicator .. Default : Entry states
	boolean isEntryRep = true;

	public AbstractBackwardModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile, String recVar, Expression expr)
			throws PrismException
	{
		super(parent, modulesFile, propertiesFile, recVar, expr);
	}

	@Override
	public abstract void computeRegion() throws PrismException;

	@Override
	public abstract void process() throws PrismException;

	@Override
	public void constructFirstRegion() throws PrismException
	{
		// Setup the first region model builder
		firstRegMB = new ModelBuilder(parent, param.ParamMode.PARAMETRIC);
		// Set the model exploration constraint, the model exploration occurs until the constraint is satisfied
		modelGenSym.setConstraint(recur_var + "=" + (init_val + 1)); // (init + 1) is chosen for the reason of recurrence verification 
		// Construction of the first model
		first_model = firstRegMB.constructModel(null, modelGenSym, paramNames, paramLowerBounds, paramUpperBounds);
		// Identify the necessary states that are relevant for the recurrence solving
		identifyForwardKeyStates();
	}

	@Override
	public abstract void identifyForwardKeyStates() throws PrismException;

	@Override
	public void constructSecondRegion(List<State> initStates) throws PrismException
	{
		// Setup the second region builder
		secondRegMB = new ModelBuilder(parent, param.ParamMode.PARAMETRIC);
		// Set the model exploration constraint, the model exploration occurs until the constraint is satisfied
		modelGenSym.setConstraint(recur_var + "=" + (end_val + 1));
		// Construction of the second model
		second_model = secondRegMB.constructModel(initStates, modelGenSym, paramNames, paramLowerBounds, paramUpperBounds);
		// The relevant states for the recurrence relations
		relevant_states = second_model.getStatesList();
	}

	@Override
	public void constructThirdRegion(List<State> initStates) throws PrismException
	{
		// Setup the third region builder
		thirdRegMB = new ModelBuilder(parent, param.ParamMode.PARAMETRIC);
		// Set the model exploration constraint, the model exploration occurs until the end
		modelGenSym.setConstraint(null);
		// Construction of the second model
		third_model = thirdRegMB.constructModel(initStates, modelGenSym, paramNames, paramLowerBounds, paramUpperBounds);
	}

	@Override
	public void computeRecurBaseProbs(List<State> finalStates) throws PrismException
	{
		Log.p(Level.INFO, "Computing Recurrence Base Probabilities", this.getClass());
		Result result = pmc.check(third_model, new ExpressionFilter("all", expr));
		ParamResult paramResult = (ParamResult) result.getResult();
		StateValues sa = paramResult.getRegionValues().getStateValues();

		List<State> states = third_model.getStatesList();
		recur_base_probs = new HashMap<Integer, BigRational>();

		for (int i = 0; i < finalStates.size(); i++) {
			int index = states.indexOf(finalStates.get(i));
			BigRational prob = ((Function) sa.getStateValue(index)).asBigRational();
			State matchingSrcState = new State(finalStates.get(i));
			matchingSrcState.varValues[modules_file.getVarIndex(recur_var)] = ((Integer) matchingSrcState.varValues[modules_file.getVarIndex(recur_var)]) - 1;
			recur_base_probs.put(relevant_states.indexOf(matchingSrcState), prob);
		}
	}

	@Override
	public void computeRecurTransProb(List<State> currStates, List<State> prevStates) throws PrismException
	{
		Log.p(Level.INFO, "Computing Recurrence Trans Probabilities", this.getClass());
		// Setup the data structure to store the recurrence equations
		recur_trans = new HashMap<Integer, Map<Integer, BigRational>>(currStates.size());

		// Initializing all the coefficients of the recurrence relations as zero
		for (State prevState : prevStates) {
			int srcIndex = relevant_states.indexOf(prevState);
			Map<Integer, BigRational> trans = new HashMap<Integer, BigRational>(currStates.size());
			for (State s : prevStates)
				trans.put(relevant_states.indexOf(s), BigRational.ZERO);
			recur_trans.put(srcIndex, trans);
		}
		// Compute the transition probabilities to the target states lies in the recurrent block
		computeRecurTransTarg(prevStates);
		// Computing the coefficients of the recurrence relations
		for (State target : currStates) {
			State matchingSrcState = new State(target);
			matchingSrcState.varValues[modules_file.getVarIndex(recur_var)] = ((Integer) matchingSrcState.varValues[modules_file.getVarIndex(recur_var)]) - 1;
			int matchingSrcIndex = relevant_states.indexOf(matchingSrcState);

			Expression expr = generateTargetExpression(target, FilterOperator.ALL);
			Result result = pmc.check(second_model, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();

			for (Entry<Integer, Map<Integer, BigRational>> entry : recur_trans.entrySet()) {
				int srcIndex = entry.getKey();
				Map<Integer, BigRational> trans = entry.getValue();
				trans.put(matchingSrcIndex, trans.get(matchingSrcIndex).add(((Function) sa.getStateValue(srcIndex)).asBigRational()));
			}
		}
	}

	/**
	 * Computes transition probabilities from the representative states to the target states within the recurrent block.
	 * In the case representative states are entry the transition probability will be computed to the target states lies within 
	 * the same recurrent block otherwise the next recurrent block.
	 * @param states the representative states of the recurrent block
	 * @throws PrismException
	 */
	public void computeRecurTransTarg(List<State> states) throws PrismException
	{
		int currentRecurVal = (int) states.get(0).varValues[recur_var_index];
		int targRecurVal = isEntryRep ? currentRecurVal : currentRecurVal + 1;

		Set<State> target = targ.getStates(targRecurVal, 2);
		recurTransTarget = new HashMap<Integer, BigRational>();

		if (target.size() > 0) {
			Expression expr = generateTargetExpression(target, FilterOperator.ALL);
			Result result = pmc.check(second_model, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();

			for (State s : states) {
				int index = relevant_states.indexOf(s);
				recurTransTarget.put(index, ((Function) sa.getStateValue(index)).asBigRational());
			}
		}
	}

	@Override
	public void solveX(int state_size) throws PrismException
	{
		// The total number of variables in the recurrence relations
		int numVars = recur_trans.size();
		// Setup the data structure to store the first order recurrence relations
		all_recur_eqns = new ArrayList<FirstOrderRecurrence>(numVars);
		// Initializing the list of variables
		recur_var_list = new ArrayList<Integer>();
		// Adding the variables to the list
		for (Integer varIndex : recur_trans.keySet())
			recur_var_list.add(varIndex);

		for (Entry<Integer, Map<Integer, BigRational>> trans : recur_trans.entrySet()) {
			// Retrieving the index of the subject variable of the equation 
			int actual_var_index = trans.getKey();
			// Initializing the coefficients with zeroes
			ArrayList<BigRational> coeff_vars = new ArrayList<BigRational>(Collections.nCopies(state_size + 1, BigRational.ZERO));
			// Setting up the actual values of the coefficients
			for (Entry<Integer, BigRational> entry : trans.getValue().entrySet())
				coeff_vars.set(recur_var_list.indexOf(entry.getKey()), entry.getValue());
			if (recurTransTarget.containsKey(actual_var_index))
				coeff_vars.set(coeff_vars.size() - 1, recurTransTarget.get(actual_var_index));
			// To setup the target states to have self loop [Special case for transformed DTMCs]
			if (targ.getStates(2).contains(actual_var_index))
				coeff_vars.set(recur_var_list.size(), BigRational.ONE);
			// Add the recurrence relations to the list
			all_recur_eqns.add(new FirstOrderRecurrence(coeff_vars, recur_base_probs.get(actual_var_index), recur_var_list.indexOf(actual_var_index)));
		}

		updated_var_index = new int[all_recur_eqns.size()];
		for (int i = 0; i < updated_var_index.length; i++)
			updated_var_index[i] = i;
		// Eliminate all the variables that are target states / independent variables and update the list
		required_recur_eqns = eliminateEquations(all_recur_eqns, updated_var_index) ? required_recur_eqns : all_recur_eqns;
	}

	@Override
	public void solve(int state_size) throws PrismException
	{
		// The total number of variables in the recurrence relations
		int num_vars = recur_trans.size();
		// Setup the data structure to store the first order recurrence relations
		all_recur_eqns = new ArrayList<FirstOrderRecurrence>(num_vars);
		// Initializing the list of variables
		recur_var_list = new ArrayList<Integer>();
		// Adding the variables to the list
		for (Integer varIndex : recur_trans.keySet())
			recur_var_list.add(varIndex);

		for (Entry<Integer, Map<Integer, BigRational>> trans : recur_trans.entrySet()) {
			// Retrieving the index of the subject variable of the equation 
			int actualVarIndex = trans.getKey();
			// Initializing the coefficients with zeroes
			ArrayList<BigRational> varCoeffs = new ArrayList<BigRational>(Collections.nCopies(state_size + 1, BigRational.ZERO));
			// Setting up the actual values of the coefficients
			for (Entry<Integer, BigRational> entry : trans.getValue().entrySet())
				varCoeffs.set(recur_var_list.indexOf(entry.getKey()), entry.getValue());
			if (recurTransTarget.containsKey(actualVarIndex))
				varCoeffs.set(varCoeffs.size() - 1, recurTransTarget.get(actualVarIndex));
			// To setup the target states to have self loop [Special case for transformed DTMCs]
			if (targ.getStates(2).contains(actualVarIndex))
				varCoeffs.set(recur_var_list.size(), BigRational.ONE);
			// Add the recurrence relations to the list
			all_recur_eqns.add(new FirstOrderRecurrence(varCoeffs, recur_base_probs.get(actualVarIndex), recur_var_list.indexOf(actualVarIndex)));
		}

		updated_var_index = new int[all_recur_eqns.size()];
		for (int i = 0; i < updated_var_index.length; i++)
			updated_var_index[i] = i;
		// Eliminate all the variables that are target states / independent variables and update the list
		required_recur_eqns = eliminateEquations(all_recur_eqns, updated_var_index) ? required_recur_eqns : all_recur_eqns;
		// System.out.println(recurEqns);

		// Convert into OGF form
		OrdinaryGeneratingFunction[] ogfs = new OrdinaryGeneratingFunction[required_recur_eqns.size()];
		for (int i = 0; i < required_recur_eqns.size(); i++)
			ogfs[i] = required_recur_eqns.get(i).getOGFForm();
		// System.out.println(requiredRecurEqns);
		// Generate Matrix A and B for the corresponding OGF
		int size = required_recur_eqns.size();
		PolynomialFraction[][] A = new PolynomialFraction[size][size];
		PolynomialFraction[] B = new PolynomialFraction[size];

		for (int i = 0; i < ogfs.length; i++) {
			OrdinaryGeneratingFunction ogf = ogfs[i];
			B[i] = ogf.getB();
			A[i] = ogf.getA();
		}

		// Solve the matrix A, B
		GJEPolyFraction gje = new GJEPolyFraction(A, B);
		ogffractions = gje.result();

		// Partial fraction decomposition wrt. computed rs
		Decomposition d = new Decomposition();
		solutions = new HashMap<Integer, ReducedRecursion>();
		for (int i = 0; i < ogffractions.length; i++)
			solutions.put(i, d.decompose(ogffractions[i]));
	}

	/**
	 * Eliminates the target variables from each recurrence equations as they reduces into probability 1. 
	 * Also removes the recurrence relation corresponding the target variable from the set of equations to be solved.
	 * @param recurEqns recurrence equations
	 * @param updated_var_index each variables corresponding to the recurrence equations
	 * @return true if any of the recurrence relations are removed
	 */
	public boolean eliminateEquations(List<FirstOrderRecurrence> recurEqns, int[] updated_var_index)
	{
		// Setup the data structure to store the info
		required_recur_eqns = new ArrayList<FirstOrderRecurrence>(recurEqns.size());

		// Take a deep copy of each recurrence equation
		for (int i = 0; i < recurEqns.size(); i++)
			required_recur_eqns.add(recurEqns.get(i).deepCopy());

		// If any equation is removed from the required set of equations
		boolean isEliminated = false;

		// Eliminate the target/independent variables and update the recurrence equations
		int actual_i = 0;
		for (int i = 0; i < required_recur_eqns.size(); i++) {
			if (updated_var_index[actual_i] >= 0 && required_recur_eqns.get(i).isIndependent()) {
				// Remove the equation where the corresponding variable is subject
				FirstOrderRecurrence removed_eqn = required_recur_eqns.remove(i);
				BigRational constant = removed_eqn.getLastCoeff();
				// Eliminate the corresponding variable from all the other equations
				for (FirstOrderRecurrence recEqn : required_recur_eqns) {
					BigRational val = recEqn.removeCoeff(i).multiply(constant);
					recEqn.addCoeff(recEqn.getNumVars(), val);
				}

				for (int j = actual_i + 1; j < updated_var_index.length; j++)
					updated_var_index[j] -= 1;
				updated_var_index[actual_i] = constant.isZero() ? -2 : -1;

				isEliminated = true;
				i--;
			}
			actual_i++;
		}
		return isEliminated;
	}

	@Override
	public void computeEndProbs(List<State> currentStates) throws PrismException
	{
		final_probs = new HashMap<Integer, BigRational>();
		for (int i = 0; i < currentStates.size(); i++) {
			State s = currentStates.get(i);
			Expression expr = generateTargetExpression(s, FilterOperator.FIRST);
			Result result = pmc.check(first_model, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();
			final_probs.put(i, ((Function) sa.getStateValue(0)).asBigRational());
		}

		Set<State> states = new HashSet<State>();

		for (int i = 0; i < (isEntryRep ? init_val : (init_val + 1)); i++)
			states.addAll(targ.getStates(init_val, 1));

		if (states.size() > 0) {
			Expression expr = generateTargetExpression(states, FilterOperator.FIRST);
			Result result = pmc.check(first_model, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();
			init_targ_prob = ((Function) sa.getStateValue(0)).asBigRational();
		}

	}

	@Override
	public void computeTotalProbability(List<State> currentStates)
	{
		result = 0.0;
		str_result = "";
		int n = end_val - init_val;

		for (int i = 0; i < updated_var_index.length; i++) {
			State matchingInitEntryState = new State(relevant_states.get(recur_var_list.get(i)));
			matchingInitEntryState.varValues[modules_file.getVarIndex(recur_var)] = init_val;
			if (updated_var_index[i] == -1) {
				double val = final_probs.get(firstRepStates.indexOf(matchingInitEntryState)).doubleValue();
				result += val;
				str_result += " " + val + " ";

				if (i != (updated_var_index.length - 1))
					str_result += " +";
			} else if (updated_var_index[i] >= 0) {
				double tmpProb = solutions.get(updated_var_index[i]).getValue(n).doubleValue().getReal();
				str_result += " (" + solutions.get(updated_var_index[i]).getEqnString() + ") * ";
				double val = final_probs.get(firstRepStates.indexOf(matchingInitEntryState)).doubleValue();
				tmpProb *= val;
				str_result += val;
				result += tmpProb;

				if (i != (updated_var_index.length - 1))
					str_result += " +";
			}
		}

		if (!init_targ_prob.isZero()) {
			result += init_targ_prob.doubleValue();
			str_result = "(" + str_result + " ) + " + init_targ_prob;
		}

		Log.p(Level.INFO, "Formation of the Equation", this.getClass());
		System.out.println(str_result);
		System.out.println(this);
	}
	
	
	public void computeTotalProbabilityXX(List<State> states)
	{
		result = 0.0;
		str_result = "";
		int n = end_val - init_val;

		double[] probs = new double[recur_base_probs.size()];
		for (int i = 0; i < probs.length; i++) {
			if (updated_var_index[i] == -1) // corresponding state is a target state
				probs[i] = 1;
			else
				probs[i] = recur_base_probs.get(i).doubleValue();
		}

		for (int i = 0; i < n; i++) {
			double[] _probs = new double[probs.length];
			System.arraycopy(probs, 0, _probs, 0, probs.length);
			for (FirstOrderRecurrence f : required_recur_eqns) {
				_probs[f.getVarIndex()] = f.evaluate(probs);
			}
			probs = _probs;
		}

		result = 0;

		for (int i = 0; i < probs.length; i++) {
			result += (probs[i] * final_probs.get(i).doubleValue());
		}

		if (!init_targ_prob.isZero()) {
			result += init_targ_prob.doubleValue();
		}

		Log.p(Level.INFO, "Formation of the Equation", this.getClass());
		System.out.println("Result :" + result);
		System.out.println(this);
	}

	@Override
	public void computeTotalProbabilityX(List<State> states)
	{
		result = 0.0;
		str_result = "";
		int n = end_val - init_val;
		int size = required_recur_eqns.size();

		double[][] a = new double[size + 1][size + 1];
		double[] b = new double[size + 1];

		for (int i = 0; i < size; i++) {
			FirstOrderRecurrence f = required_recur_eqns.get(i);
			System.arraycopy(f.getCoeffs(), 0, a[i], 0, size + 1);
			b[i] = f.getBaseVal().doubleValue();
		}

		// Special case
		a[size][size] = 1.0;
		b[size] = 1.0;

		double[] probs = MatrixHelper.solve(a, b, n);

		result = 0;

		for (int i = 0; i < probs.length - 1; i++) {
			result += (probs[i] * final_probs.get(i).doubleValue());
		}

		if (!init_targ_prob.isZero()) {
			result += init_targ_prob.doubleValue();
		}

		Log.p(Level.INFO, "Formation of the Equation", this.getClass());
		System.out.println("Result :" + result);
		System.out.println(this);
	}
}