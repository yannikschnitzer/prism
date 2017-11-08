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

	public abstract List<Pair<Integer, Integer>> computeRegion() throws PrismLangException;

	public void setRange(Pair<Integer, Integer> range)
	{
		this.initVal = range.first();
		this.endVal = range.second();
	}

	public abstract void process() throws PrismException;

	public abstract void constructFirstRegion() throws PrismException;

	public abstract void identifyForwardKeyStates() throws PrismException;

	public abstract void constructSecondRegion(List<State> initStates) throws PrismException;

	public abstract boolean isRecurring() throws PrismException;

	public abstract void constructThirdRegion(List<State> initStates) throws PrismException;

	public abstract void computeRecurBaseProbs(List<State> finalStates) throws PrismException;

	public abstract void computeRecurTransProb(List<State> currStates, List<State> prevStates) throws PrismException;

	public abstract void solve(int state_size) throws PrismException;

	public abstract void computeEndProbs(List<State> currentStates) throws PrismException;

	public abstract void computeTotalProbability(List<State> currentStates);

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
