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
import prism.PrismLangException;
import prism.Result;
import recurrence.data_structure.Pair;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.data_structure.recursion.FirstOrderRecurrence;
import recurrence.data_structure.recursion.OrdinaryGeneratingFunction;
import recurrence.data_structure.recursion.ReducedRecursion;
import recurrence.log.Level;
import recurrence.log.Log;
import recurrence.math.matrix.GJEPolyFraction;
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
	List<Integer> recvarList;
	// Chosen Representative Indicator .. Default : Entry states
	boolean isEntryRep = true;

	public AbstractBackwardModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile, String recVar, Expression expr)
			throws PrismException
	{
		super(parent, modulesFile, propertiesFile, recVar, expr);
	}

	@Override
	public abstract List<Pair<Integer, Integer>> computeRegion() throws PrismLangException;

	@Override
	public abstract void process() throws PrismException;

	@Override
	public void constructFirstRegion() throws PrismException
	{
		// Setup the first region model builder
		firstRegMB = new ModelBuilder(parent, param.ParamMode.PARAMETRIC);
		// Set the model exploration constraint, the model exploration occurs until the constraint is satisfied
		modelGenSym.setConstraint(recurVar + "=" + (initVal + 1)); // (init + 1) is chosen for the reason of recurrence verification 
		// Construction of the first model
		firstModel = firstRegMB.constructModel(null, modelGenSym, paramNames, paramLowerBounds, paramUpperBounds);
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
		modelGenSym.setConstraint(recurVar + "=" + (endVal + 1));
		// Construction of the second model
		secondModel = secondRegMB.constructModel(initStates, modelGenSym, paramNames, paramLowerBounds, paramUpperBounds);
		// The relevant states for the recurrence relations
		relevantStates = secondModel.getStatesList();
	}

	@Override
	public abstract boolean isRecurring() throws PrismException;

	@Override
	public void constructThirdRegion(List<State> initStates) throws PrismException
	{
		// Setup the third region builder
		thirdRegMB = new ModelBuilder(parent, param.ParamMode.PARAMETRIC);
		// Set the model exploration constraint, the model exploration occurs until the end
		modelGenSym.setConstraint(null);
		// Construction of the second model
		thirdModel = thirdRegMB.constructModel(initStates, modelGenSym, paramNames, paramLowerBounds, paramUpperBounds);
	}

	@Override
	public void computeRecurBaseProbs(List<State> finalStates) throws PrismException
	{
		Log.p(Level.INFO, "Computing Recurrence Base Probabilities", this.getClass());
		Result result = pmc.check(thirdModel, new ExpressionFilter("all", expr));
		ParamResult paramResult = (ParamResult) result.getResult();
		StateValues sa = paramResult.getRegionValues().getStateValues();

		List<State> states = thirdModel.getStatesList();
		recurBaseProbs = new HashMap<Integer, BigRational>();

		for (int i = 0; i < finalStates.size(); i++) {
			int index = states.indexOf(finalStates.get(i));
			BigRational prob = ((Function) sa.getStateValue(index)).asBigRational();
			State matchingSrcState = new State(finalStates.get(i));
			matchingSrcState.varValues[modulesFile.getVarIndex(recurVar)] = ((Integer) matchingSrcState.varValues[modulesFile.getVarIndex(recurVar)]) - 1;
			recurBaseProbs.put(relevantStates.indexOf(matchingSrcState), prob);
		}
		// System.out.println(recurBaseProbs);
	}

	@Override
	public void computeRecurTransProb(List<State> currStates, List<State> prevStates) throws PrismException
	{
		Log.p(Level.INFO, "Computing Recurrence Trans Probabilities", this.getClass());
		// Setup the data structure to store the recurrence equations
		recurTrans = new HashMap<Integer, Map<Integer, BigRational>>(currStates.size());

		// Initializing all the coefficients of the recurrence relations as zero
		for (State prevState : prevStates) {
			int srcIndex = relevantStates.indexOf(prevState);
			Map<Integer, BigRational> trans = new HashMap<Integer, BigRational>(currStates.size());
			for (State s : prevStates)
				trans.put(relevantStates.indexOf(s), BigRational.ZERO);
			recurTrans.put(srcIndex, trans);
		}
		// Compute the transition probabilities to the target states lies in the recurrent block
		computeRecurTransTarg(prevStates);
		// Computing the coefficients of the recurrence relations
		for (State target : currStates) {
			State matchingSrcState = new State(target);
			matchingSrcState.varValues[modulesFile.getVarIndex(recurVar)] = ((Integer) matchingSrcState.varValues[modulesFile.getVarIndex(recurVar)]) - 1;
			int matchingSrcIndex = relevantStates.indexOf(matchingSrcState);

			Expression expr = generateTargetExpression(target, FilterOperator.ALL);
			Result result = pmc.check(secondModel, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();

			for (Entry<Integer, Map<Integer, BigRational>> entry : recurTrans.entrySet()) {
				int srcIndex = entry.getKey();
				Map<Integer, BigRational> trans = entry.getValue();
				trans.put(matchingSrcIndex, trans.get(matchingSrcIndex).add(((Function) sa.getStateValue(srcIndex)).asBigRational()));
			}
		}
		// System.out.println(recurTrans);
	}

	public void computeRecurTransTarg(List<State> states) throws PrismException
	{
		int currentRecurVal = (int) states.get(0).varValues[recurVarIndex];
		int targRecurVal = isEntryRep ? currentRecurVal : currentRecurVal + 1;

		Set<State> target = targ.getStates(targRecurVal, 2);
		recurTransTarget = new HashMap<Integer, BigRational>();

		if (target.size() > 0) {
			Expression expr = generateTargetExpression(target, FilterOperator.ALL);
			Result result = pmc.check(secondModel, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();

			for (State s : states) {
				int index = relevantStates.indexOf(s);
				recurTransTarget.put(index, ((Function) sa.getStateValue(index)).asBigRational());
			}
		}
	}

	public void solve2(int state_size) throws PrismException
	{
		// The total number of variables in the recurrence relations
		int numVars = recurTrans.size();
		// Setup the data structure to store the first order recurrence relations
		recurEqns = new ArrayList<FirstOrderRecurrence>(numVars);
		// Initializing the list of variables
		recvarList = new ArrayList<Integer>();
		// Adding the variables to the list
		for (Integer varIndex : recurTrans.keySet())
			recvarList.add(varIndex);

		for (Entry<Integer, Map<Integer, BigRational>> trans : recurTrans.entrySet()) {
			// Retrieving the index of the subject variable of the equation 
			int actualVarIndex = trans.getKey();
			// Initializing the coefficients with zeroes
			ArrayList<BigRational> varCoeffs = new ArrayList<BigRational>(Collections.nCopies(state_size + 1, BigRational.ZERO));
			// Setting up the actual values of the coefficients
			for (Entry<Integer, BigRational> entry : trans.getValue().entrySet())
				varCoeffs.set(recvarList.indexOf(entry.getKey()), entry.getValue());
			if (recurTransTarget.containsKey(actualVarIndex))
				varCoeffs.set(varCoeffs.size() - 1, recurTransTarget.get(actualVarIndex));
			// To setup the target states to have self loop [Special case for transformed DTMCs]
			if (targ.getStates(2).contains(actualVarIndex))
				varCoeffs.set(recvarList.size(), BigRational.ONE);
			// Add the recurrence relations to the list
			recurEqns.add(new FirstOrderRecurrence(varCoeffs, recurBaseProbs.get(actualVarIndex), recvarList.indexOf(actualVarIndex)));
		}

		updatedVarIndex = new int[recurEqns.size()];
		for (int i = 0; i < updatedVarIndex.length; i++)
			updatedVarIndex[i] = i;
		// Eliminate all the variables that are target states / independent variables and update the list
		requiredRecurEqns = eliminateEquations(recurEqns, updatedVarIndex) ? requiredRecurEqns : recurEqns;
	}

	@Override
	public void solve(int state_size) throws PrismException
	{
		// The total number of variables in the recurrence relations
		int numVars = recurTrans.size();
		// Setup the data structure to store the first order recurrence relations
		recurEqns = new ArrayList<FirstOrderRecurrence>(numVars);
		// Initializing the list of variables
		recvarList = new ArrayList<Integer>();
		// Adding the variables to the list
		for (Integer varIndex : recurTrans.keySet())
			recvarList.add(varIndex);

		for (Entry<Integer, Map<Integer, BigRational>> trans : recurTrans.entrySet()) {
			// Retrieving the index of the subject variable of the equation 
			int actualVarIndex = trans.getKey();
			// Initializing the coefficients with zeroes
			ArrayList<BigRational> varCoeffs = new ArrayList<BigRational>(Collections.nCopies(state_size + 1, BigRational.ZERO));
			// Setting up the actual values of the coefficients
			for (Entry<Integer, BigRational> entry : trans.getValue().entrySet())
				varCoeffs.set(recvarList.indexOf(entry.getKey()), entry.getValue());
			if (recurTransTarget.containsKey(actualVarIndex))
				varCoeffs.set(varCoeffs.size() - 1, recurTransTarget.get(actualVarIndex));
			// To setup the target states to have self loop [Special case for transformed DTMCs]
			if (targ.getStates(2).contains(actualVarIndex))
				varCoeffs.set(recvarList.size(), BigRational.ONE);
			// Add the recurrence relations to the list
			recurEqns.add(new FirstOrderRecurrence(varCoeffs, recurBaseProbs.get(actualVarIndex), recvarList.indexOf(actualVarIndex)));
		}

		updatedVarIndex = new int[recurEqns.size()];
		for (int i = 0; i < updatedVarIndex.length; i++)
			updatedVarIndex[i] = i;
		// Eliminate all the variables that are target states / independent variables and update the list
		requiredRecurEqns = eliminateEquations(recurEqns, updatedVarIndex) ? requiredRecurEqns : recurEqns;
		// System.out.println(recurEqns);

		// Convert into OGF form
		OrdinaryGeneratingFunction[] ogfs = new OrdinaryGeneratingFunction[requiredRecurEqns.size()];
		for (int i = 0; i < requiredRecurEqns.size(); i++)
			ogfs[i] = requiredRecurEqns.get(i).getOGFForm();
		// System.out.println(requiredRecurEqns);
		// Generate Matrix A and B for the corresponding OGF
		int size = requiredRecurEqns.size();
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

	public boolean eliminateEquations(List<FirstOrderRecurrence> recurEqns, int[] updatedVarIndex)
	{
		// Setup the data structure to store the info
		requiredRecurEqns = new ArrayList<FirstOrderRecurrence>(recurEqns.size());

		// Take a deep copy of each recurrence equations
		for (int i = 0; i < recurEqns.size(); i++)
			requiredRecurEqns.add(recurEqns.get(i).deepCopy());

		// Whether any equation has been removed or not
		boolean isEliminated = false;

		// Eliminate the target/independent variables and update the recurrence equations
		int actual_i = 0;
		for (int i = 0; i < requiredRecurEqns.size(); i++) {
			if (updatedVarIndex[actual_i] >= 0 && requiredRecurEqns.get(i).isIndependent()) {
				// Remove the equation where the corresponding variable is subject
				FirstOrderRecurrence removedEqn = requiredRecurEqns.remove(i);
				BigRational constant = removedEqn.getLastCoeff();
				// Eliminate the corresponding variable from all the other equations
				for (FirstOrderRecurrence recEqn : requiredRecurEqns) {
					BigRational val = recEqn.removeCoeff(i).multiply(constant);
					recEqn.addCoeff(recEqn.getNumVars(), val);
				}

				for (int j = actual_i + 1; j < updatedVarIndex.length; j++)
					updatedVarIndex[j] -= 1;
				updatedVarIndex[actual_i] = constant.isZero() ? -2 : -1;

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
		finalProbs = new HashMap<Integer, BigRational>();
		for (int i = 0; i < currentStates.size(); i++) {
			State s = currentStates.get(i);
			Expression expr = generateTargetExpression(s, FilterOperator.FIRST);
			Result result = pmc.check(firstModel, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();
			finalProbs.put(i, ((Function) sa.getStateValue(0)).asBigRational());
		}

		Set<State> states = new HashSet<State>();

		for (int i = 0; i < (isEntryRep ? initVal : (initVal + 1)); i++)
			states.addAll(targ.getStates(initVal, 1));

		if (states.size() > 0) {
			Expression expr = generateTargetExpression(states, FilterOperator.FIRST);
			Result result = pmc.check(firstModel, expr);
			ParamResult paramResult = (ParamResult) result.getResult();
			StateValues sa = paramResult.getRegionValues().getStateValues();
			initTargProb = ((Function) sa.getStateValue(0)).asBigRational();
		}

	}

	@Override
	public void computeTotalProbability(List<State> currentStates)
	{
		result = 0.0;
		str_result = "";
		int n = endVal - initVal;

		for (int i = 0; i < updatedVarIndex.length; i++) {
			State matchingInitEntryState = new State(relevantStates.get(recvarList.get(i)));
			matchingInitEntryState.varValues[modulesFile.getVarIndex(recurVar)] = initVal;
			if (updatedVarIndex[i] == -1) {
				double val = finalProbs.get(firstRepStates.indexOf(matchingInitEntryState)).doubleValue();
				result += val;
				str_result += " " + val + " ";

				if (i != (updatedVarIndex.length - 1))
					str_result += " +";
			} else if (updatedVarIndex[i] >= 0) {
				double tmpProb = solutions.get(updatedVarIndex[i]).getValue(n).doubleValue().getReal();
				str_result += " (" + solutions.get(updatedVarIndex[i]).getEqnString() + ") * ";
				double val = finalProbs.get(firstRepStates.indexOf(matchingInitEntryState)).doubleValue();
				tmpProb *= val;
				str_result += val;
				result += tmpProb;

				if (i != (updatedVarIndex.length - 1))
					str_result += " +";
			}
		}

		if (!initTargProb.isZero()) {
			result += initTargProb.doubleValue();
			str_result = "(" + str_result + " ) + " + initTargProb;
		}

		Log.p(Level.INFO, "Formation of the Equation", this.getClass());
		System.out.println(str_result);
		System.out.println(this);
	}

	public void computeTotalProbability2(List<State> currentStates)
	{
		result = 0.0;
		str_result = "";
		int n = endVal - initVal;

		double[] probs = new double[recurBaseProbs.size()];
		for (int i = 0; i < probs.length; i++) {
			if (updatedVarIndex[i] == -1)
				probs[i] = 1;
			else
				probs[i] = recurBaseProbs.get(i).doubleValue();
		}

		for (int i = 0; i < n; i++) {
			double[] _probs = new double[probs.length];
			System.arraycopy(probs, 0, _probs, 0, probs.length);
			for (FirstOrderRecurrence f : requiredRecurEqns) {
				_probs[f.getVarIndex()] = f.evaluate(probs);
			}
			probs = _probs;
		}

		result = 0;

		for (int i = 0; i < probs.length; i++) {
			result += (probs[i] * finalProbs.get(i).doubleValue());
		}

		if (!initTargProb.isZero()) {
			result += initTargProb.doubleValue();
		}

		Log.p(Level.INFO, "Formation of the Equation", this.getClass());
		System.out.println("Result :" + result);
		System.out.println(this);
	}
}