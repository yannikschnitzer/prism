package recurrence.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import param.ParamModel;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionTemporal;
import parser.ast.PropertiesFile;
import prism.PrismLangException;

public class Target
{

	Expression targetProp;
	int recurVarIndex;
	ParamModel firstModel, secondModel;
	List<Integer> firstTarget, secondTarget;

	/**
	 * Constructs a storage that can be used to retrieve states of a model elegantly
	 * @param firstModel First model
	 * @param secondModel Second(recurrence) model
	 * @param thirdModel Third model
	 * @param props property to be verified
	 * @param recurVarIndex index of the recurrence variable
	 */
	public Target(ParamModel firstModel, ParamModel secondModel, ParamModel thirdModel, PropertiesFile props, int recurVarIndex)
	{
		// Storing all three models
		this.firstModel = firstModel;
		this.secondModel = secondModel;
		// Storing recurVarIndex
		this.recurVarIndex = recurVarIndex;
		// Storage for the target states
		firstTarget = new ArrayList<Integer>();
		secondTarget = new ArrayList<Integer>();
		// Storing the target expression
		targetProp = ((ExpressionTemporal) ((ExpressionProb) props.getProperty(0)).getExpression()).getOperand2();
		try {
			computeTargets();
		} catch (PrismLangException ple) {
			ple.printStackTrace();
		}
		System.out.println("Done");
	}

	/**
	 * Computes the targets lies in the first and second models
	 * @throws PrismLangException
	 */
	public void computeTargets() throws PrismLangException
	{
		// First Model
		List<State> states = firstModel.getStatesList();
		for (int index = 0; index < states.size(); index++)
			if (targetProp.evaluateBoolean(states.get(index)))
				firstTarget.add(index);
		// Second Model
		states = secondModel.getStatesList();
		for (int index = 0; index < states.size(); index++)
			if (targetProp.evaluateBoolean(states.get(index)))
				secondTarget.add(index);
	}

	/**
	 * Returns the set of states based on the value of the recurrence variabel and the model
	 * @param recurVal the nth recurrent block
	 * @param modelNumber first or second or third model
	 * @return set of state corresponding to the recurrent variables value and the model
	 */
	public Set<State> getStates(int recurVal, int modelNumber)
	{
		Set<State> states = new HashSet<State>();
		if (modelNumber == 1) {
			for (int t : firstTarget) {
				State current = firstModel.getStatesList().get(t);
				if (current.varValues[recurVarIndex].equals(recurVal))
					states.add(current);
			}
		} else {
			for (int t : secondTarget) {
				State current = secondModel.getStatesList().get(t);
				if (current.varValues[recurVarIndex].equals(recurVal))
					states.add(current);
			}
		}
		return states;
	}

	
	/**
	 * Returns a list of states based on the model
	 * @param modelNumber first or second or third model
	 * @return list of states lies in the corresponding model
	 */
	public List<Integer> getStates(int modelNumber)
	{
		if (modelNumber == 1) {
			return firstTarget;
		} else {
			return secondTarget;
		}
	}
}
