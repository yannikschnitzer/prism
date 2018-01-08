package recurrence.model_checking;

import java.util.List;
import java.util.Map;
import java.util.Set;

import param.BigRational;
import param.ParamModel;
import param.ParamModelChecker;
import parser.State;
import parser.ast.Declaration;
import parser.ast.Expression;
import parser.ast.ExpressionFilter;
import parser.ast.ExpressionFilter.FilterOperator;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.PrismCL;
import prism.PrismComponent;
import prism.PrismDevNullLog;
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
	protected ModulesFile modules_file;
	protected PropertiesFile properties_file;

	// Custom built model generator to facilitate the construction of the recurrence relations 
	protected CustomModelGenerator modelGenSym;

	/* ==========================================
	 *  Variable related to recurrence building
	 * ==========================================
	 */

	// The variable which causes the recurrence
	protected String recur_var;
	// The range of the recurrence variable
	protected int init_val, end_val;

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
	protected ParamModel first_model, second_model, third_model;

	// The states relevant to the constructions of the recurrence relations
	protected List<State> relevant_states;

	/* ======================================================================================
	 * 	The data structures to store the relevant information about the recurrence relations
	 * ======================================================================================
	 */

	// The base probabilities of the recurrence relations
	protected Map<Integer, BigRational> recur_base_probs;
	// The recurrence relations itself
	protected Map<Integer, Map<Integer, BigRational>> recur_trans;
	protected Map<Integer, BigRational> recurTransTarget;
	// The initial region's probability that needs to be attached
	protected Map<Integer, BigRational> final_probs;
	protected BigRational init_targ_prob = BigRational.ZERO;

	// The size of the recurrent block
	protected int recurrent_block_size;

	// The recurrence equations
	protected List<FirstOrderRecurrence> all_recur_eqns;
	// The simplified ordinary generating functions
	protected PolynomialFraction[] ogffractions;
	// The reduced form of the each corresponding recurrence equations
	protected Map<Integer, ReducedRecursion> solutions;
	// The necessary equations to be solved
	protected List<FirstOrderRecurrence> required_recur_eqns;
	// The updated list of variable indexes, after elimination  
	protected int[] updated_var_index;

	// The property expression
	protected Expression expr;
	// The index of recurrence variable
	protected int recur_var_index;
	// The final result
	public double result;
	protected String str_func;

	// Recurrence parameter and the value range
	protected String recur_param;
	protected String recur_param_value;
	
	// Declaration of recurrence variable 
	protected Declaration decl_recur_var;

	public AbstractModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile, String recVar, Expression expr,
			String recur_param, String recur_param_value) throws PrismException
	{
		// Setup the required variables
		this.parent = parent;
		this.modules_file = modulesFile;
		this.properties_file = propertiesFile;
		this.recur_var = recVar;
		this.expr = expr;
		this.recur_param = recur_param;
		this.recur_param_value = recur_param_value;

		// Replace the known constant values from both modules and properties files.
		this.modules_file.replaceConstants(modulesFile.getConstantValues());
		this.properties_file.replaceConstants(modulesFile.getConstantValues());

		// Setup the custom model wrapper
		modelGenSym = new CustomModelGenerator(new ModulesFileModelGeneratorSymbolic(modulesFile, parent));

		// Dummy parameter info to satisfy the parameter model checker
		paramNames = new String[] { "p0" };
		paramLowerBounds = new String[] { "0" };
		paramUpperBounds = new String[] { "1" };

		// Setup the parameter model checker 
		pmc = new ParamModelChecker(parent, param.ParamMode.PARAMETRIC);
		pmc.setLog(new PrismDevNullLog());
		pmc.setParameters(paramNames, paramLowerBounds, paramUpperBounds);
		pmc.setModulesFileAndPropertiesFile(modulesFile, propertiesFile);

		// Retrieve the declaration of the recurrence variable from the modules file
		decl_recur_var = modules_file.getVarDeclaration(modules_file.getVarIndex(recur_var));
	}

	/**
	 * Computes the recurrent borderline
	 * @return the recurrent borderline
	 * @throws PrismLangException
	 */
	public abstract void computeRegion() throws PrismException;

	/**
	 * Sets the recurrent borderline
	 * @param range recurrent borderline
	 */
	public void setRecurrenceInterval(Pair<Integer, Integer> range)
	{
		this.init_val = range.first();
		this.end_val = range.second();
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
	public abstract void solveX(int state_size) throws PrismException;

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
	public abstract void computeFuncAndTotalProbability(List<State> states) throws PrismLangException;

	/**
	 * Evaluates the recurrence relations for the current value of the recurrence variable
	 * @param states
	 */
	public abstract void computeTotalProbabilityX(List<State> states) throws PrismLangException;

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
		String core = modules_file.getVarName(0) + " = " + state.var_values[0];
		for (int i = 1; i < state.var_values.length; i++)
			core += " & " + modules_file.getVarName(i) + " = " + state.var_values[i];
		Expression expr = PrismCL.prism.parsePropertiesString(modules_file, "P=? [F (" + core + ")]").getPropertyObject(0).getExpression();
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
			core += " (" + modules_file.getVarName(0) + " = " + s.var_values[0];
			for (int i = 1; i < s.var_values.length; i++)
				core += " & " + modules_file.getVarName(i) + " = " + s.var_values[i];
			core += ") |";
		}
		core = core.substring(0, core.length() - 1);
		Expression expr = PrismCL.prism.parsePropertiesString(modules_file, "P=? [F (" + core + ")]").getPropertyObject(0).getExpression();
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
