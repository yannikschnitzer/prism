package recurrence.model_checking;

import java.util.List;
import java.util.Map;
import java.util.Set;

import param.BigRational;
import param.ParamModel;
import param.ParamModelChecker;
import parser.State;
import parser.ast.Expression;
import parser.ast.ExpressionFilter;
import parser.ast.ExpressionFilter.FilterOperator;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.PrismCL;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLangException;
import recurrence.data_structure.Pair;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.data_structure.recursion.FirstOrderRecurrence;
import recurrence.data_structure.recursion.ReducedRecursion;
import recurrence.utils.CustomModelGenerator;
import simulator.ModulesFileModelGeneratorSymbolic;

/**
 * @author Nishan
 *
 */
public abstract class AbstractModelChecker
{
	protected PrismComponent parent;
	protected ModulesFile modulesFile;
	protected PropertiesFile propertiesFile;

	// Custom built model generator to facilitate the construction of the recurrence relations 
	protected CustomModelGenerator modelGenSym;

	/* ==========================================
	 *  Variable related to recurrence building
	 * ==========================================
	 */

	// The variable which causes the recurrence
	protected String recurVar;
	// The range of the recurrence variable
	protected int initVal, endVal;

	// The Parameter model checker
	protected ParamModelChecker pmc;

	// The parameter informations for the parameter model checker
	protected String[] paramNames;
	protected String[] paramLowerBounds;
	protected String[] paramUpperBounds;

	/* ========================================================
	 * 	 The region builders of the forward/backward approach
	 * ========================================================
	 * 	1. First Region : Includes the initial region 
	 *  2. Second Region : Includes the recurrence region
	 *  3. Third Region : Includes the end region
	 */
	protected param.ModelBuilder firstRegMB, secondRegMB, thirdRegMB;

	// The models that stores the each aforementioned regions
	protected ParamModel firstModel, secondModel, thirdModel;

	// The states relevant to the constructions of the recurrence relations
	protected List<State> relevantStates;

	/* ======================================================================================
	 * 	The data structures to store the relevant information about the recurrence relations
	 * ======================================================================================
	 */

	// The base probabilities of the recurrence relations
	protected Map<Integer, BigRational> recurBaseProbs;
	// The recurrence relations itself
	protected Map<Integer, Map<Integer, BigRational>> recurTrans;
	protected Map<Integer, BigRational> recurTransTarget;
	// The initial region's probability that needs to be attached
	protected Map<Integer, BigRational> finalProbs;
	protected BigRational initTargProb = BigRational.ZERO;

	// The size of the recurrent block
	protected int recurBlockSize;

	// The recurrence equations
	protected List<FirstOrderRecurrence> recurEqns;
	// The simplified ordinary generating functions
	protected PolynomialFraction[] ogffractions;
	// The reduced form of the each corresponding recurrence equations
	protected Map<Integer, ReducedRecursion> solutions;
	// The necessary equations to be solved
	protected List<FirstOrderRecurrence> requiredRecurEqns;
	// The updated list of variable indexes, after elimination  
	protected int[] updatedVarIndex;

	// The property expression
	protected Expression expr;
	// The index of recurrence variable
	protected int recurVarIndex;
	// The final result
	public double result;
	protected String str_result;

	public AbstractModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile, String recVar, Expression expr)
			throws PrismException
	{
		// Setup the required variables
		this.parent = parent;
		this.modulesFile = modulesFile;
		this.propertiesFile = propertiesFile;
		this.recurVar = recVar;
		this.expr = expr;

		// Replace the known constant values from both modules and properties files.
		this.modulesFile.replaceConstants(modulesFile.getConstantValues());
		this.propertiesFile.replaceConstants(modulesFile.getConstantValues());

		// Setup the custom model wrapper
		modelGenSym = new CustomModelGenerator(new ModulesFileModelGeneratorSymbolic(modulesFile, parent));

		// Dummy parameter info to satisfy the parameter model checker
		paramNames = new String[] { "p0" };
		paramLowerBounds = new String[] { "0" };
		paramUpperBounds = new String[] { "1" };

		// Setup the parameter model checker 
		pmc = new ParamModelChecker(parent, param.ParamMode.PARAMETRIC);
		pmc.setParameters(paramNames, paramLowerBounds, paramUpperBounds);
		pmc.setModulesFileAndPropertiesFile(modulesFile, propertiesFile);
	}

	/**
	 * Computes the recurrent borderline
	 * @return the recurrent borderline
	 * @throws PrismLangException
	 */
	public abstract List<Pair<Integer, Integer>> computeRegion() throws PrismException;

	/**
	 * Sets the recurrent borderline
	 * @param range recurrent borderline
	 */
	public void setRange(Pair<Integer, Integer> range)
	{
		this.initVal = range.first();
		this.endVal = range.second();
	}

	/**
	 * This method calls all the relevant methods in the order to construct the regions, 
	 * solve and evaluates the recurrence relations. 
	 * @throws PrismException
	 */
	public abstract void process() throws PrismException;

	/**
	 * Constructs the first region with respect to the recurrent borderline, i.e. the region 
	 * exists before the starting point of the recurrent borderline. 
	 * @throws PrismException
	 */
	public abstract void constructFirstRegion() throws PrismException;

	/**
	 * Identify the key states for example, representative states of the first and last recurrent block, 
	 * initial states, target states etc.
	 * @throws PrismException
	 */
	public abstract void identifyForwardKeyStates() throws PrismException;

	/**
	 * Constructs the second (recurrence region), i.e. the last and the one before last recurrent block 
	 * @param states the entry states of the first recurrent block
	 * @throws PrismException
	 */
	public abstract void constructSecondRegion(List<State> states) throws PrismException;

	/**
	 * Check if two contiguous recurrent blocks are recurrently similar
	 * @return true if the two contiguous recurrent blocks are recurrently similar
	 * @throws PrismException
	 */
	public abstract boolean isRecurring() throws PrismException;

	/**
	 * Construct the third region, i.e. the region that exists after the last recurrent block.
	 * @param states the exit states of the last recurrent block
	 * @throws PrismException
	 */
	public abstract void constructThirdRegion(List<State> states) throws PrismException;

	/**
	 * Computes the probability to reach the target states from the exit states of the last recurrent block
	 * where these probabilities represents the base condition for the recurrence relation of each 
	 * representative states.
	 * @param states the exit states of the last recurrent block
	 * @throws PrismException
	 */
	public abstract void computeRecurBaseProbs(List<State> states) throws PrismException;

	/**
	 * Compute the transition probabilities between the two contiguous representative states of the recurrent blocks.
	 * These probabilities will be later used to form the recurrence relations. 
	 * @param last_states the representative states of the last recurrent block
	 * @param previous_last_states the representative states of the the recurrent block before the last one
	 * @throws PrismException
	 */
	public abstract void computeRecurTransProb(List<State> last_states, List<State> previous_last_states) throws PrismException;

	/**
	 * Solves the recurrence relations and forms the closed functions for each of them. 
	 * @param state_size size of the recurrent block
	 * @throws PrismException
	 */
	public abstract void solve(int state_size) throws PrismException;
	
	/**
	 * Forms the recurrence relations but does not finds the closed functions for them. 
	 * @param state_size size of the recurrent block
	 * @throws PrismException
	 */
	public abstract void solve2(int state_size) throws PrismException;

	/**
	 * The transition probability to reach the the representative states of the first recurrent block from
	 * the initial states of the model 
	 * @param states the representative states of the first recurrent block
	 * @throws PrismException
	 */
	public abstract void computeEndProbs(List<State> states) throws PrismException;

	/**
	 * Forms an end function for the respective inductive model and also produces the result for the current
	 * value of the recurrence variable
	 * @param states
	 */
	public abstract void computeTotalProbability(List<State> states);
	
	/**
	 * Evaluates the recurrence relations for the current value of the recurrence variable
	 * @param states
	 */
	public abstract void computeTotalProbability2(List<State> states);

	/**
	 * Generates a property expression to compute the probability to reach the corresponding state from
	 * other states.
	 * @param state single target state
	 * @param op filter operator whether all or first states
	 * @return the property expression
	 * @throws PrismLangException
	 */
	public Expression generateTargetExpression(State state, FilterOperator op) throws PrismLangException
	{
		String core = modulesFile.getVarName(0) + " = " + state.varValues[0];
		for (int i = 1; i < state.varValues.length; i++)
			core += " & " + modulesFile.getVarName(i) + " = " + state.varValues[i];
		Expression expr = PrismCL.prism.parsePropertiesString(modulesFile, "P=? [F (" + core + ")]").getPropertyObject(0).getExpression();
		String operator = (op == FilterOperator.ALL ? "all" : "first");
		expr = new ExpressionFilter(operator, expr);
		return expr;
	}

	/**
	 * Generates a property expression to compute the probability to reach the corresponding states from
	 * other states.
	 * @param states multiple target states
	 * @param op filter operator whether all or first states
	 * @return the property expression
	 * @throws PrismLangException
	 */
	public Expression generateTargetExpression(Set<State> states, FilterOperator op) throws PrismLangException
	{
		String core = "";
		for (State s : states) {
			core += " (" + modulesFile.getVarName(0) + " = " + s.varValues[0];
			for (int i = 1; i < s.varValues.length; i++)
				core += " & " + modulesFile.getVarName(i) + " = " + s.varValues[i];
			core += ") |";
		}
		core = core.substring(0, core.length() - 1);
		Expression expr = PrismCL.prism.parsePropertiesString(modulesFile, "P=? [F (" + core + ")]").getPropertyObject(0).getExpression();
		String operator = (op == FilterOperator.ALL ? "all" : "first");
		expr = new ExpressionFilter(operator, expr);
		return expr;
	}

	/**
	 * Generates given number of dummy variables for the purpose of parametric model checking
	 * @param num number of dummy variables
	 */
	public void createDummyVars(int num)
	{
		paramNames = new String[num];
		paramLowerBounds = new String[num];
		paramUpperBounds = new String[num];

		for (int i = 0; i < num; i++) {
			paramNames[i] = "p" + i;
			paramLowerBounds[i] = "0";
			paramUpperBounds[i] = "1";
		}
	}
}
