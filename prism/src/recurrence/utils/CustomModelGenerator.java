package recurrence.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import param.Function;
import param.FunctionFactory;
import param.ModelBuilder;
import parser.State;
import parser.VarList;
import parser.ast.Expression;
import parser.type.Type;
import prism.DefaultModelGenerator;
import prism.ModelGeneratorSymbolic;
import prism.ModelType;
import prism.PrismException;
import recurrence.utils.expression.ExpressionChecker;
import simulator.ModulesFileModelGeneratorSymbolic;

public class CustomModelGenerator extends DefaultModelGenerator implements ModelGeneratorSymbolic
{
	private ModulesFileModelGeneratorSymbolic modelgen;
	private String constraint;
	private FunctionFactory functionFactory;
	private ModelBuilder modelBuilder;

	// Used only when dummy state is needed
	private boolean isDummyStateActivated = false;
	private ArrayList<State> dummyState;
	private ArrayList<Map<State, Function>> dummyProb;
	private int numDummyVars;
	private int recurIndex, dummyVal;

	ExpressionChecker ec;
	private boolean isSatisfied = false;

	public CustomModelGenerator(ModulesFileModelGeneratorSymbolic modelgen) throws PrismException
	{
		this.modelgen = modelgen;
		numDummyVars = 0;
	}

	public void setDummyTargetInfo(int index, int val)
	{
		this.recurIndex = index;
		this.dummyVal = val;
	}

	public void setDummyStateStatus(boolean doActivate)
	{
		isDummyStateActivated = doActivate;
	}

	public boolean getDummyStateStatus()
	{
		return isDummyStateActivated;
	}

	public State getDummyState(int i)
	{
		return this.dummyState.get(i);
	}

	public void setConstraint(String constraint)
	{
		this.constraint = constraint;
		ec = constraint == null ? null : new ExpressionChecker(constraint, modelgen.createVarList());
	}

	public String getConstraint()
	{
		return this.constraint;
	}

	@Override
	public boolean rewardStructHasTransitionRewards(int i)
	{
		return modelgen.rewardStructHasTransitionRewards(i);
	}

	@Override
	public VarList createVarList() throws PrismException
	{
		return modelgen.createVarList();
	}

	@Override
	public ModelType getModelType()
	{
		return modelgen.getModelType();
	}

	@Override
	public int getNumVars()
	{
		return modelgen.getNumVars();
	}

	@Override
	public List<String> getVarNames()
	{
		return modelgen.getVarNames();
	}

	@Override
	public List<Type> getVarTypes()
	{
		return modelgen.getVarTypes();
	}

	@Override
	public List<String> getLabelNames()
	{
		return modelgen.getLabelNames();
	}

	@Override
	public State getInitialState() throws PrismException
	{
		return modelgen.getInitialState();
	}

	@Override
	public void exploreState(State exploreState) throws PrismException
	{
		modelgen.exploreState(exploreState);
		isSatisfied = ec == null ? false : ec.isValid(exploreState);
		if (dummyState == null && isDummyStateActivated)
			createDummyTransition();
	}

	@Override
	public State getExploreState()
	{
		return modelgen.getExploreState();
	}

	@Override
	public int getNumChoices() throws PrismException
	{
		//		if (isSatisfied)
		//			return 1;
		//		else if (dummyTransition.containsKey(getExploreState()))
		//			return 0;
		//		else
		return modelgen.getNumChoices();
	}

	@Override
	public int getNumTransitions(int i) throws PrismException
	{
		if (isSatisfied && isDummyStateActivated)
			return 2;
		else if ((isDummyStateActivated && dummyState.contains(getExploreState()) || (isSatisfied && !isDummyStateActivated)))
			return 1;
		else
			return modelgen.getNumTransitions(i);
	}

	@Override
	public Object getTransitionAction(int i) throws PrismException
	{
		if (isDummyStateActivated && dummyState.contains(getExploreState()))
			return null;
		else
			return modelgen.getTransitionAction(i);
	}

	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException
	{
		if (isDummyStateActivated && dummyState.contains(getExploreState()))
			return null;
		else
			return modelgen.getTransitionAction(i, offset);
	}

	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException
	{
		if (isSatisfied && isDummyStateActivated) {
			Map<State, Function> probs = dummyProb.get(offset);
			State currState = getExploreState();
			if (probs.containsKey(currState)) {
				return probs.get(currState).asBigRational().doubleValue();
			} else {
				createDummyProbVar(currState);
				return probs.get(currState).asBigRational().doubleValue();
			}
		} else if ((isDummyStateActivated && dummyState.contains(getExploreState()) || (isSatisfied && !isDummyStateActivated)))
			return 1;
		else
			return modelgen.getTransitionProbability(i, offset);
	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException
	{
		if (isSatisfied && isDummyStateActivated)
			return dummyState.get(offset);
		else if ((isDummyStateActivated && dummyState.contains(getExploreState()) || (isSatisfied && !isDummyStateActivated)))
			return getExploreState();
		else
			return modelgen.computeTransitionTarget(i, offset);
	}

	@Override
	public void setSymbolic(ModelBuilder modelBuilder, FunctionFactory functionFactory)
	{
		this.functionFactory = functionFactory;
		this.modelBuilder = modelBuilder;
		modelgen.setSymbolic(modelBuilder, functionFactory);
	}

	@Override
	public Expression getUnknownConstantDefinition(String name) throws PrismException
	{
		return modelgen.getUnknownConstantDefinition(name);
	}

	@Override
	public Function getTransitionProbabilityFunction(int i, int offset) throws PrismException
	{
		if (isSatisfied && isDummyStateActivated) {
			Map<State, Function> probs = dummyProb.get(offset);
			State currState = getExploreState();
			if (!probs.containsKey(currState))
				createDummyProbVar(currState);
			return probs.get(currState);
		} else if ((isDummyStateActivated && dummyState.contains(getExploreState()) || (isSatisfied && !isDummyStateActivated)))
			return functionFactory.getOne();
		else
			return modelgen.getTransitionProbabilityFunction(i, offset);
	}

	public void createDummyTransition() throws PrismException
	{
		State dummyTargetA = new State(getExploreState());
		dummyTargetA.var_values[recurIndex] = dummyVal;

		State dummyTargetB = new State(getExploreState());
		dummyTargetB.var_values[recurIndex] = dummyVal + 1;

		dummyState = new ArrayList<State>();
		dummyProb = new ArrayList<Map<State, Function>>();

		dummyState.add(dummyTargetA);
		dummyProb.add(new HashMap<State, Function>());

		dummyState.add(dummyTargetB);
		dummyProb.add(new HashMap<State, Function>());
	}

	public void createDummyProbVar(State currState)
	{
		Function p = functionFactory.getVar("p" + numDummyVars);
		Function one = functionFactory.getOne();
		Function q = one.subtract(p);

		dummyProb.get(0).put(currState, p);
		dummyProb.get(1).put(currState, q);

		numDummyVars++;
	}
}
