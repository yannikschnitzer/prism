//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package explicit;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import acceptance.AcceptanceReach;
import acceptance.AcceptanceType;
import common.IntSet;
import common.IterableBitSet;
import common.IterableStateSet;
import common.StopWatch;
import csv.BasicReader;
import csv.CsvFormatException;
import csv.CsvReader;
import explicit.modelviews.EquivalenceRelationInteger;
import explicit.modelviews.MDPDroppedAllChoices;
import explicit.modelviews.MDPEquiv;
import explicit.rewards.MCRewards;
import explicit.rewards.MCRewardsFromMDPRewards;
import explicit.rewards.MDPRewards;
import explicit.rewards.Rewards;
import explicit.rewards.StateRewardsArray;
import parser.State;
import parser.ast.Expression;
import parser.type.TypeDouble;
import prism.AccuracyFactory;
import prism.OptionsIntervalIteration;
import prism.Prism;
import prism.PrismComponent;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.PrismNotSupportedException;
import prism.PrismSettings;
import prism.PrismUtils;
import strat.MDStrategy;
import strat.MDStrategyArray;
import strat.Strategy;
import strat.StrategyInfo.UndefinedReason;

import static java.lang.Math.*;
import strat.FMDStrategyProduct;
import strat.FMDStrategyStep;
import strat.MDStrategy;
import strat.MDStrategyArray;
import strat.Strategy;

/**
 * Explicit-state model checker for Markov decision processes (MDPs).
 */
public class MDPModelChecker extends ProbModelChecker
{
	/**
	 * Create a new MDPModelChecker, inherit basic state from parent (unless null).
	 */
	public MDPModelChecker(PrismComponent parent) throws PrismException
	{
		super(parent);
	}
	
	// Model checking functions

	@Override
	protected StateValues checkProbPathFormulaLTL(Model model, Expression expr, boolean qual, MinMax minMax, BitSet statesOfInterest) throws PrismException
	{
		// For min probabilities, need to negate the formula
		// (add parentheses to allow re-parsing if required)
		if (minMax.isMin()) {
			expr = Expression.Not(Expression.Parenth(expr.deepCopy()));
		}

		// Build product of MDP and DA for the LTL formula, and do any required exports
		LTLModelChecker mcLtl = new LTLModelChecker(this);
		AcceptanceType[] allowedAcceptance = {
				AcceptanceType.BUCHI,
				AcceptanceType.RABIN,
				AcceptanceType.GENERALIZED_RABIN,
				AcceptanceType.REACH
		};
		LTLModelChecker.LTLProduct<MDP> product = mcLtl.constructDAProductForLTLFormula(this, (MDP) model, expr, statesOfInterest, allowedAcceptance);
		doProductExports(product);
		
		// Find accepting states + compute reachability probabilities
		BitSet acc;
		if (product.getAcceptance() instanceof AcceptanceReach) {
			mainLog.println("\nSkipping accepting MEC computation since acceptance is defined via goal states...");
			acc = ((AcceptanceReach)product.getAcceptance()).getGoalStates();
		} else {
			mainLog.println("\nFinding accepting MECs...");
			acc = mcLtl.findAcceptingECStates(product.getProductModel(), product.getAcceptance());
		}
		mainLog.println("\nComputing reachability probabilities...");
		MDPModelChecker mcProduct = new MDPModelChecker(this);
		mcProduct.inheritSettings(this);
		ModelCheckerResult res = mcProduct.computeReachProbs((MDP) product.getProductModel(), acc, false);
		StateValues probsProduct = StateValues.createFromDoubleArrayResult(res, product.getProductModel());

		// Subtract from 1 if we're model checking a negated formula for regular Pmin
		if (minMax.isMin()) {
			probsProduct.applyFunction(TypeDouble.getInstance(), v -> 1.0 - (double) v);
		}

		// Output vector over product, if required
		if (getExportProductVector()) {
				mainLog.println("\nExporting product solution vector matrix to file \"" + getExportProductVectorFilename() + "\"...");
				PrismFileLog out = new PrismFileLog(getExportProductVectorFilename());
				probsProduct.print(out, false, false, false, false);
				out.close();
		}
		
		// If a strategy was generated, lift it to the product and store
		if (res.strat != null) {
			Strategy stratProduct = new FMDStrategyProduct(product, (MDStrategy) res.strat);
			result.setStrategy(stratProduct);
		}
		
		// Mapping probabilities in the original model
		StateValues probs = product.projectToOriginalModel(probsProduct);
		probsProduct.clear();

		return probs;
	}

	/**
	 * Compute rewards for a co-safe LTL reward operator.
	 */
	protected StateValues checkRewardCoSafeLTL(Model model, Rewards modelRewards, Expression expr, MinMax minMax, BitSet statesOfInterest, String modifier) throws PrismException
	{
		// Build product of MDP and DFA for the LTL formula, convert rewards and do any required exports
		LTLModelChecker mcLtl = new LTLModelChecker(this);
		LTLModelChecker.LTLProduct<MDP> product = mcLtl.constructDFAProductForCosafetyReward(this, (MDP) model, expr, statesOfInterest);
		MDPRewards productRewards = ((MDPRewards) modelRewards).liftFromModel(product);
		doProductExports(product);

		// Find accepting states + compute reachability rewards
		BitSet acc = ((AcceptanceReach)product.getAcceptance()).getGoalStates();

		mainLog.println("\nComputing reachability rewards...");
		MDPModelChecker mcProduct = new MDPModelChecker(this);
		mcProduct.inheritSettings(this);
		
		ModelCheckerResult res = null;
		
		if (modifier != null && modifier.equals("cvar")) {
			res = mcProduct.computeReachRewardsCvar((MDP)product.getProductModel(), productRewards, acc, minMax.isMin());
		} else if (modifier != null && modifier.equals("dist")){
			res = mcProduct.computeReachRewardsDistr((MDP)product.getProductModel(), productRewards, acc, minMax.isMin());
		} else {
			res = mcProduct.computeReachRewards((MDP)product.getProductModel(), productRewards, acc, minMax.isMin());
		}
		StateValues rewardsProduct = StateValues.createFromDoubleArrayResult(res, product.getProductModel());

		// Output vector over product, if required
		if (getExportProductVector()) {
				mainLog.println("\nExporting product solution vector matrix to file \"" + getExportProductVectorFilename() + "\"...");
				PrismFileLog out = new PrismFileLog(getExportProductVectorFilename());
				rewardsProduct.print(out, false, false, false, false);
				out.close();
		}

		// If a strategy was generated, lift it to the product and store
		if (res.strat != null) {
			Strategy stratProduct = new FMDStrategyProduct(product, (MDStrategy) res.strat);
			result.setStrategy(stratProduct);
		}
		
		// Mapping rewards in the original model
		StateValues rewards = product.projectToOriginalModel(rewardsProduct);
		rewardsProduct.clear();

		return rewards;
	}
	
	// Numerical computation functions

	/**
	 * Compute next=state probabilities.
	 * i.e. compute the probability of being in a state in {@code target} in the next step.
	 * @param mdp The MDP
	 * @param target Target states
	 * @param min Min or max probabilities (true=min, false=max)
	 */
	public ModelCheckerResult computeNextProbs(MDP mdp, BitSet target, boolean min) throws PrismException
	{
		ModelCheckerResult res = null;
		int n;
		double soln[], soln2[];
		long timer;

		timer = System.currentTimeMillis();

		// Store num states
		n = mdp.getNumStates();

		// Create/initialise solution vector(s)
		soln = Utils.bitsetToDoubleArray(target, n);
		soln2 = new double[n];

		// Next-step probabilities 
		mdp.mvMultMinMax(soln, min, soln2, null, false, null);

		// Return results
		res = new ModelCheckerResult();
		res.accuracy = AccuracyFactory.boundedNumericalIterations();
		res.soln = soln2;
		res.numIters = 1;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Given a value vector x, compute the probability:
	 *   v(s) = min/max sched [ Sum_s' P_sched(s,s')*x(s') ]  for s labeled with a,
	 *   v(s) = 0   for s not labeled with a.
	 *
	 * Clears the StateValues object x.
	 *
	 * @param tr the transition matrix
	 * @param a the set of states labeled with a
	 * @param x the value vector
	 * @param min compute min instead of max
	 */
	public double[] computeRestrictedNext(MDP mdp, BitSet a, double[] x, boolean min)
	{
		int n;
		double soln[];

		// Store num states
		n = mdp.getNumStates();

		// initialized to 0.0
		soln = new double[n];

		// Next-step probabilities multiplication
		// restricted to a states
		mdp.mvMultMinMax(x, min, soln, a, false, null);

		return soln;
	}

	/**
	 * Compute reachability probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target}.
	 * @param mdp The MDP
	 * @param target Target states
	 * @param min Min or max probabilities (true=min, false=max)
	 */
	public ModelCheckerResult computeReachProbs(MDP mdp, BitSet target, boolean min) throws PrismException
	{
		return computeReachProbs(mdp, null, target, min, null, null);
	}

	/**
	 * Compute until probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target},
	 * while remaining in those in {@code remain}.
	 * @param mdp The MDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param min Min or max probabilities (true=min, false=max)
	 */
	public ModelCheckerResult computeUntilProbs(MDP mdp, BitSet remain, BitSet target, boolean min) throws PrismException
	{
		return computeReachProbs(mdp, remain, target, min, null, null);
	}

	/**
	 * Compute reachability/until probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target},
	 * while remaining in those in {@code remain}.
	 * @param mdp The MDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param init Optionally, an initial solution vector (may be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values).
	 * Also, 'known' values cannot be passed for some solution methods, e.g. policy iteration.  
	 */
	public ModelCheckerResult computeReachProbs(MDP mdp, BitSet remain, BitSet target, boolean min, double init[], BitSet known) throws PrismException
	{
		ModelCheckerResult res = null;
		BitSet no, yes;
		int n, numYes, numNo;
		long timer, timerProb0, timerProb1;
		int strat[] = null;
		// Local copy of setting
		MDPSolnMethod mdpSolnMethod = this.mdpSolnMethod;

		boolean doPmaxQuotient = this.doPmaxQuotient;

		// Switch to a supported method, if necessary
		if (mdpSolnMethod == MDPSolnMethod.LINEAR_PROGRAMMING) {
			mdpSolnMethod = MDPSolnMethod.GAUSS_SEIDEL;
			mainLog.printWarning("Switching to MDP solution method \"" + mdpSolnMethod.fullName() + "\"");
		}

		// Check for some unsupported combinations
		if (mdpSolnMethod == MDPSolnMethod.VALUE_ITERATION && valIterDir == ValIterDir.ABOVE) {
			if (!(precomp && prob0))
				throw new PrismException("Precomputation (Prob0) must be enabled for value iteration from above");
			if (!min)
				throw new PrismException("Value iteration from above only works for minimum probabilities");
		}
		if (doIntervalIteration) {
			if (!min && (genStrat || exportAdv)) {
				throw new PrismNotSupportedException("Currently, explicit engine does not support adversary construction for interval iteration and Pmax");
			}
			if (mdpSolnMethod != MDPSolnMethod.VALUE_ITERATION && mdpSolnMethod != MDPSolnMethod.GAUSS_SEIDEL) {
				throw new PrismNotSupportedException("Currently, explicit engine only supports interval iteration with value iteration or Gauss-Seidel for MDPs");
			}
			if (init != null)
				throw new PrismNotSupportedException("Interval iteration currently not supported with provided initial values");
			if (!(precomp && prob0 && prob1)) {
				throw new PrismNotSupportedException("Precomputations (Prob0 & Prob1) must be enabled for interval iteration");
			}

			if (!min) {
				doPmaxQuotient = true;
			}
		}
		if (mdpSolnMethod == MDPSolnMethod.POLICY_ITERATION || mdpSolnMethod == MDPSolnMethod.MODIFIED_POLICY_ITERATION) {
			if (known != null) {
				throw new PrismException("Policy iteration methods cannot be passed 'known' values for some states");
			}
		}

		if (doPmaxQuotient && min) {
			// for Pmin, don't do quotient
			doPmaxQuotient = false;
		}

		// Start probabilistic reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting probabilistic reachability (" + (min ? "min" : "max") + ")...");

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		mdp.checkForDeadlocks(target);

		// Store num states
		n = mdp.getNumStates();

		// Optimise by enlarging target set (if more info is available)
		if (init != null && known != null && !known.isEmpty()) {
			BitSet targetNew = (BitSet) target.clone();
			for (int i : new IterableBitSet(known)) {
				if (init[i] == 1.0) {
					targetNew.set(i);
				}
			}
			target = targetNew;
		}

		// If required, export info about target states 
		if (getExportTarget()) {
			BitSet bsInit = new BitSet(n);
			for (int i = 0; i < n; i++) {
				bsInit.set(i, mdp.isInitialState(i));
			}
			List<BitSet> labels = Arrays.asList(bsInit, target);
			List<String> labelNames = Arrays.asList("init", "target");
			mainLog.println("\nExporting target states info to file \"" + getExportTargetFilename() + "\"...");
			PrismLog out = new PrismFileLog(getExportTargetFilename());
			exportLabels(mdp, labels, labelNames, Prism.EXPORT_PLAIN, out);
			out.close();
		}

		// If required, create/initialise strategy storage
		// Set choices to -1, denoting unknown
		// (except for target states, which are -2, denoting arbitrary)
		if (genStrat || exportAdv) {
			strat = new int[n];
			for (int i = 0; i < n; i++) {
				strat[i] = target.get(i) ? -2 : -1;
			}
		}

		// Precomputation
		timerProb0 = System.currentTimeMillis();
		if (precomp && prob0) {
			no = prob0(mdp, remain, target, min, strat);
		} else {
			no = new BitSet();
		}
		timerProb0 = System.currentTimeMillis() - timerProb0;
		timerProb1 = System.currentTimeMillis();
		if (precomp && prob1) {
			yes = prob1(mdp, remain, target, min, strat);
		} else {
			yes = (BitSet) target.clone();
		}
		timerProb1 = System.currentTimeMillis() - timerProb1;

		// Print results of precomputation
		numYes = yes.cardinality();
		numNo = no.cardinality();
		mainLog.println("target=" + target.cardinality() + ", yes=" + numYes + ", no=" + numNo + ", maybe=" + (n - (numYes + numNo)));

		// If still required, store strategy for no/yes (0/1) states.
		// This is just for the cases max=0 and min=1, where arbitrary choices suffice (denoted by -2)
		if (genStrat || exportAdv) {
			if (min) {
				for (int i = yes.nextSetBit(0); i >= 0; i = yes.nextSetBit(i + 1)) {
					if (!target.get(i))
						strat[i] = -2;
				}
			} else {
				for (int i = no.nextSetBit(0); i >= 0; i = no.nextSetBit(i + 1)) {
					strat[i] = -2;
				}
			}
		}

		// Compute probabilities (if needed)
		if (numYes + numNo < n) {

			if (!min && doPmaxQuotient) {
				MDPEquiv maxQuotient = maxQuotient(mdp, yes, no);
				// MDPEquiv retains original state space, making the states that are not used
				// trap states.
				// yesInQuotient is the representative for the yes equivalence class
				BitSet yesInQuotient = new BitSet();
				yesInQuotient.set(maxQuotient.mapStateToRestrictedModel(yes.nextSetBit(0)));
				// noInQuotient is the representative for the no equivalence class as well
				// as the non-representative states (the states in any equivalence class
				// that are not the representative for the class). As the latter states
				// are traps, we can just add them to the no set
				BitSet noInQuotient = new BitSet();
				noInQuotient.set(maxQuotient.mapStateToRestrictedModel(no.nextSetBit(0)));
				noInQuotient.or(maxQuotient.getNonRepresentativeStates());
				MDPSparse quotientModel = new MDPSparse(maxQuotient);

				ModelCheckerResult res1 = computeReachProbsNumeric(quotientModel,
				                                                   mdpSolnMethod,
				                                                   noInQuotient,
				                                                   yesInQuotient,
				                                                   min,
				                                                   init,
				                                                   known,
				                                                   strat);

				res = new ModelCheckerResult();
				res.numIters = res1.numIters;
				res.timeTaken = res1.timeTaken;
				res.soln = new double[mdp.getNumStates()];
				for (int i = 0; i < n; i++) {
					if (yes.get(i)) {
						res.soln[i] = 1.0;
					} else if (no.get(i)) {
						res.soln[i] = 0.0;
					} else {
						res.soln[i] = res1.soln[maxQuotient.mapStateToRestrictedModel(i)];
					}
				}
				res.accuracy = res1.accuracy;
			} else {
				res = computeReachProbsNumeric(mdp, mdpSolnMethod, no, yes, min, init, known, strat);
			}
		} else {
			res = new ModelCheckerResult();
			res.soln = Utils.bitsetToDoubleArray(yes, n);
			res.accuracy = AccuracyFactory.doublesFromQualitative();
		}

		// Finished probabilistic reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.println("Probabilistic reachability took " + timer / 1000.0 + " seconds.");

		// Store strategy
		if (genStrat) {
			res.strat = new MDStrategyArray(mdp, strat);
		}
		// Export adversary
		if (exportAdv) {
			// Prune strategy, if needed
			if (getRestrictStratToReach()) {
				restrictStrategyToReachableStates(mdp, strat);
			}
			// Export
			PrismLog out = new PrismFileLog(exportAdvFilename);
			int precision = settings.getInteger(PrismSettings.PRISM_EXPORT_MODEL_PRECISION);
			new DTMCFromMDPMemorylessAdversary(mdp, strat).exportToPrismExplicitTra(out, precision);
			out.close();
		}

		// Update time taken
		res.timeTaken = timer / 1000.0;
		res.timeProb0 = timerProb0 / 1000.0;
		res.timePre = (timerProb0 + timerProb1) / 1000.0;

		return res;
	}

	protected ModelCheckerResult computeReachProbsNumeric(MDP mdp, MDPSolnMethod method, BitSet no, BitSet yes, boolean min, double init[], BitSet known, int strat[]) throws PrismException
	{
		ModelCheckerResult res = null;

		IterationMethod iterationMethod = null;
		switch (method) {
		case VALUE_ITERATION:
			iterationMethod = new IterationMethodPower(termCrit == TermCrit.ABSOLUTE, termCritParam);
			break;
		case GAUSS_SEIDEL:
			iterationMethod = new IterationMethodGS(termCrit == TermCrit.ABSOLUTE, termCritParam, false);
			break;
		case POLICY_ITERATION:
			if (doIntervalIteration) {
				throw new PrismNotSupportedException("Interval iteration currently not supported for policy iteration");
			}
			res = computeReachProbsPolIter(mdp, no, yes, min, strat);
			break;
		case MODIFIED_POLICY_ITERATION:
			if (doIntervalIteration) {
				throw new PrismNotSupportedException("Interval iteration currently not supported for policy iteration");
			}
			res = computeReachProbsModPolIter(mdp, no, yes, min, strat);
			break;
		default:
			throw new PrismException("Unknown MDP solution method " + mdpSolnMethod.fullName());
		}

		if (res == null) { // not yet computed, use iterationMethod
			if (!doIntervalIteration) {
				res = doValueIterationReachProbs(mdp, no, yes, min, init, known, iterationMethod, getDoTopologicalValueIteration(), strat);
			} else {
				res = doIntervalIterationReachProbs(mdp, no, yes, min, init, known, iterationMethod, getDoTopologicalValueIteration(), strat);
			}
		}

		return res;
	}

	/**
	 * Prob0 precomputation algorithm.
	 * i.e. determine the states of an MDP which, with min/max probability 0,
	 * reach a state in {@code target}, while remaining in those in {@code remain}.
	 * {@code min}=true gives Prob0E, {@code min}=false gives Prob0A. 
	 * Optionally, for min only, store optimal (memoryless) strategy info for 0 states. 
	 * @param mdp The MDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 */
	public BitSet prob0(MDPGeneric<?> mdp, BitSet remain, BitSet target, boolean min, int strat[])
	{
		int n, iters;
		BitSet u, soln, unknown;
		boolean u_done;
		long timer;

		// Start precomputation
		timer = System.currentTimeMillis();
		if (!silentPrecomputations)
			mainLog.println("Starting Prob0 (" + (min ? "min" : "max") + ")...");

		// Special case: no target states
		if (target.cardinality() == 0) {
			soln = new BitSet(mdp.getNumStates());
			soln.set(0, mdp.getNumStates());

			// for min, generate strategy, any choice (-2) is fine
			if (min && strat != null) {
				Arrays.fill(strat, -2);
			}
			return soln;
		}

		// Initialise vectors
		n = mdp.getNumStates();
		u = new BitSet(n);
		soln = new BitSet(n);

		// Determine set of states actually need to perform computation for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		if (remain != null)
			unknown.and(remain);

		// Fixed point loop
		iters = 0;
		u_done = false;
		// Least fixed point - should start from 0 but we optimise by
		// starting from 'target', thus bypassing first iteration
		u.or(target);
		soln.or(target);
		while (!u_done) {
			iters++;
			// Single step of Prob0
			mdp.prob0step(unknown, u, min, soln);
			// Check termination
			u_done = soln.equals(u);
			// u = soln
			u.clear();
			u.or(soln);
		}

		// Negate
		u.flip(0, n);

		// Finished precomputation
		timer = System.currentTimeMillis() - timer;
		if (!silentPrecomputations) {
			mainLog.print("Prob0 (" + (min ? "min" : "max") + ")");
			mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");
		}

		// If required, generate strategy. This is for min probs,
		// so it can be done *after* the main prob0 algorithm (unlike for prob1).
		// We simply pick, for all "no" states, the first choice for which all transitions stay in "no"
		if (strat != null) {
			for (int i = u.nextSetBit(0); i >= 0; i = u.nextSetBit(i + 1)) {
				int numChoices = mdp.getNumChoices(i);
				for (int k = 0; k < numChoices; k++) {
					if (mdp.allSuccessorsInSet(i, k, u)) {
						strat[i] = k;
						continue;
					}
				}
			}
		}

		return u;
	}

	/**
	 * Prob1 precomputation algorithm.
	 * i.e. determine the states of an MDP which, with min/max probability 1,
	 * reach a state in {@code target}, while remaining in those in {@code remain}.
	 * {@code min}=true gives Prob1A, {@code min}=false gives Prob1E. 
	 * Optionally, for max only, store optimal (memoryless) strategy info for 1 states. 
	 * @param mdp The MDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 */
	public BitSet prob1(MDPGeneric<?> mdp, BitSet remain, BitSet target, boolean min, int strat[])
	{
		int n, iters;
		BitSet u, v, soln, unknown;
		boolean u_done, v_done;
		long timer;

		// Start precomputation
		timer = System.currentTimeMillis();
		if (!silentPrecomputations)
			mainLog.println("Starting Prob1 (" + (min ? "min" : "max") + ")...");

		// Special case: no target states
		if (target.cardinality() == 0) {
			return new BitSet(mdp.getNumStates());
		}

		// Initialise vectors
		n = mdp.getNumStates();
		u = new BitSet(n);
		v = new BitSet(n);
		soln = new BitSet(n);

		// Determine set of states actually need to perform computation for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		if (remain != null)
			unknown.and(remain);

		// Nested fixed point loop
		iters = 0;
		u_done = false;
		// Greatest fixed point
		u.set(0, n);
		while (!u_done) {
			v_done = false;
			// Least fixed point - should start from 0 but we optimise by
			// starting from 'target', thus bypassing first iteration
			v.clear();
			v.or(target);
			soln.clear();
			soln.or(target);
			while (!v_done) {
				iters++;
				// Single step of Prob1
				if (min)
					mdp.prob1Astep(unknown, u, v, soln);
				else
					mdp.prob1Estep(unknown, u, v, soln, null);
				// Check termination (inner)
				v_done = soln.equals(v);
				// v = soln
				v.clear();
				v.or(soln);
			}
			// Check termination (outer)
			u_done = v.equals(u);
			// u = v
			u.clear();
			u.or(v);
		}

		// If we need to generate a strategy, do another iteration of the inner loop for this
		// We could do this during the main double fixed point above, but we would generate surplus
		// strategy info for non-1 states during early iterations of the outer loop,
		// which are not straightforward to remove since this method does not know which states
		// already have valid strategy info from Prob0.
		// Notice that we only need to look at states in u (since we already know the answer),
		// so we restrict 'unknown' further 
		unknown.and(u);
		if (!min && strat != null) {
			v_done = false;
			v.clear();
			v.or(target);
			soln.clear();
			soln.or(target);
			while (!v_done) {
				mdp.prob1Estep(unknown, u, v, soln, strat);
				v_done = soln.equals(v);
				v.clear();
				v.or(soln);
			}
			u_done = v.equals(u);
		}

		// Finished precomputation
		timer = System.currentTimeMillis() - timer;
		if (!silentPrecomputations) {
			mainLog.print("Prob1 (" + (min ? "min" : "max") + ")");
			mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");
		}

		return u;
	}

	/**
	 * Compute reachability probabilities using value iteration.
	 * Optionally, store optimal (memoryless) strategy info. 
	 * @param mdp The MDP
	 * @param no Probability 0 states
	 * @param yes Probability 1 states
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.  
	 */
	protected ModelCheckerResult computeReachProbsValIter(MDP mdp, BitSet no, BitSet yes, boolean min, double init[], BitSet known, int strat[])
			throws PrismException
	{
		IterationMethodPower iterationMethod = new IterationMethodPower(termCrit == TermCrit.ABSOLUTE, termCritParam);
		return doValueIterationReachProbs(mdp, no, yes, min, init, known, iterationMethod, false, strat);
	}

	/**
	 * Compute reachability probabilities using value iteration.
	 * Optionally, store optimal (memoryless) strategy info.
	 * @param mdp The MDP
	 * @param no Probability 0 states
	 * @param yes Probability 1 states
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten)
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param iterationMethod The iteration method
	 * @param topological Do topological value iteration?
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null), 'init' must also be given and is used for the exact values.
	 */
	protected ModelCheckerResult doValueIterationReachProbs(MDP mdp, BitSet no, BitSet yes, boolean min, double init[], BitSet known, IterationMethod iterationMethod, boolean topological, int strat[])
			throws PrismException
	{
		BitSet unknown;
		int i, n;
		double initVal;
		long timer;

		// Start value iteration
		timer = System.currentTimeMillis();
		String description = (min ? "min" : "max")
				+ (topological ? ", topological": "" )
				+ ", with " + iterationMethod.getDescriptionShort();

		mainLog.println("Starting value iteration (" + description + ")...");

		ExportIterations iterationsExport = null;
		if (settings.getBoolean(PrismSettings.PRISM_EXPORT_ITERATIONS)) {
			iterationsExport = new ExportIterations("Explicit MDP ReachProbs value iteration (" + description + ")");
			mainLog.println("Exporting iterations to " + iterationsExport.getFileName());
		}

		// Store num states
		n = mdp.getNumStates();

		// Initialise solution vectors. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 1.0/0.0 if in yes/no; (3) passed in initial value; (4) initVal
		// where initVal is 0.0 or 1.0, depending on whether we converge from below/above. 
		initVal = (valIterDir == ValIterDir.BELOW) ? 0.0 : 1.0;
		if (init != null) {
			if (known != null) {
				for (i = 0; i < n; i++)
					init[i] = known.get(i) ? init[i] : yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i];
			} else {
				for (i = 0; i < n; i++)
					init[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i];
			}
		} else {
			init = new double[n];
			for (i = 0; i < n; i++)
				init[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : initVal;
		}

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(yes);
		unknown.andNot(no);
		if (known != null)
			unknown.andNot(known);

		if (iterationsExport != null)
			iterationsExport.exportVector(init, 0);

		IterationMethod.IterationValIter iteration = iterationMethod.forMvMultMinMax(mdp, min, strat);
		iteration.init(init);

		IntSet unknownStates = IntSet.asIntSet(unknown);

		if (topological) {
			// Compute SCCInfo, including trivial SCCs in the subgraph obtained when only considering
			// states in unknown
			SCCInfo sccs = SCCComputer.computeTopologicalOrdering(this, mdp, true, unknown::get);

			IterationMethod.SingletonSCCSolver singletonSCCSolver = (int s, double[] soln) -> {
				soln[s] = mdp.mvMultJacMinMaxSingle(s, soln, min, strat);
			};

			// run the actual value iteration
			return iterationMethod.doTopologicalValueIteration(this, description, sccs, iteration, singletonSCCSolver, timer, iterationsExport);
		} else {
			// run the actual value iteration
			return iterationMethod.doValueIteration(this, description, iteration, unknownStates, timer, iterationsExport);
		}
	}

	/**
	 * Compute reachability probabilities using interval iteration.
	 * Optionally, store optimal (memoryless) strategy info.
	 * @param mdp The MDP
	 * @param no Probability 0 states
	 * @param yes Probability 1 states
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten)
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param iterationMethod The iteration method
	 * @param topological Do topological value iteration?
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.
	 */
	protected ModelCheckerResult doIntervalIterationReachProbs(MDP mdp, BitSet no, BitSet yes, boolean min, double init[], BitSet known, IterationMethod iterationMethod, boolean topological, int strat[])
			throws PrismException
	{
		BitSet unknown;
		int i, n;
		double initBelow[], initAbove[];
		long timer;

		// Start value iteration
		timer = System.currentTimeMillis();
		String description = (min ? "min" : "max")
				+ (topological ? ", topological": "" )
				+ ", with " + iterationMethod.getDescriptionShort();

		mainLog.println("Starting interval iteration (" + description + ")...");

		ExportIterations iterationsExport = null;
		if (settings.getBoolean(PrismSettings.PRISM_EXPORT_ITERATIONS)) {
			iterationsExport = new ExportIterations("Explicit MDP ReachProbs interval iteration (" + description + ")");
			mainLog.println("Exporting iterations to " + iterationsExport.getFileName());
		}

		// Store num states
		n = mdp.getNumStates();

		// Create solution vector(s)
		initBelow = (init == null) ? new double[n] : init;
		initAbove = new double[n];

		// Initialise solution vectors. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 1.0/0.0 if in yes/no; (3) initVal
		// where initVal is 0.0 or 1.0, depending on whether we converge from below/above.
		if (known != null && init != null) {
			for (i = 0; i < n; i++) {
				initBelow[i] = known.get(i) ? init[i] : yes.get(i) ? 1.0 : no.get(i) ? 0.0 : 0.0;
				initAbove[i] = known.get(i) ? init[i] : yes.get(i) ? 1.0 : no.get(i) ? 0.0 : 1.0;
			}
		} else {
			for (i = 0; i < n; i++) {
				initBelow[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 :  0.0;
				initAbove[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 :  1.0;
			}
		}

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(yes);
		unknown.andNot(no);
		if (known != null)
			unknown.andNot(known);

		if (iterationsExport != null) {
			iterationsExport.exportVector(initBelow, 0);
			iterationsExport.exportVector(initAbove, 1);
		}

		OptionsIntervalIteration iiOptions = OptionsIntervalIteration.from(this);

		final boolean enforceMonotonicFromBelow = iiOptions.isEnforceMonotonicityFromBelow();
		final boolean enforceMonotonicFromAbove = iiOptions.isEnforceMonotonicityFromAbove();
		final boolean checkMonotonic = iiOptions.isCheckMonotonicity();

		if (!enforceMonotonicFromAbove) {
			getLog().println("Note: Interval iteration is configured to not enforce monotonicity from above.");
		}
		if (!enforceMonotonicFromBelow) {
			getLog().println("Note: Interval iteration is configured to not enforce monotonicity from below.");
		}

		IterationMethod.IterationIntervalIter below = iterationMethod.forMvMultMinMaxInterval(mdp, min, strat, true, enforceMonotonicFromBelow, checkMonotonic);
		IterationMethod.IterationIntervalIter above = iterationMethod.forMvMultMinMaxInterval(mdp, min, strat, false, enforceMonotonicFromAbove, checkMonotonic);
		below.init(initBelow);
		above.init(initAbove);

		IntSet unknownStates = IntSet.asIntSet(unknown);

		if (topological) {
			// Compute SCCInfo, including trivial SCCs in the subgraph obtained when only considering
			// states in unknown
			SCCInfo sccs = SCCComputer.computeTopologicalOrdering(this, mdp, true, unknown::get);

			IterationMethod.SingletonSCCSolver singletonSCCSolver = (int s, double[] soln) -> {
				soln[s] = mdp.mvMultJacMinMaxSingle(s, soln, min, strat);
			};

			// run the actual value iteration
			return iterationMethod.doTopologicalIntervalIteration(this, description, sccs, below, above, singletonSCCSolver, timer, iterationsExport);
		} else {
			// run the actual value iteration
			return iterationMethod.doIntervalIteration(this, description, below, above, unknownStates, timer, iterationsExport);
		}
	}

	/**
	 * Compute reachability probabilities using Gauss-Seidel (including Jacobi-style updates).
	 * @param mdp The MDP
	 * @param no Probability 0 states
	 * @param yes Probability 1 states
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.  
	 */
	protected ModelCheckerResult computeReachProbsGaussSeidel(MDP mdp, BitSet no, BitSet yes, boolean min, double init[], BitSet known, int strat[])
			throws PrismException
	{
		IterationMethodGS iterationMethod = new IterationMethodGS(termCrit == TermCrit.ABSOLUTE, termCritParam, false);
		return doValueIterationReachProbs(mdp, no, yes, min, init, known, iterationMethod, false, strat);
	}

	/**
	 * Compute reachability probabilities using policy iteration.
	 * Optionally, store optimal (memoryless) strategy info. 
	 * @param mdp: The MDP
	 * @param no: Probability 0 states
	 * @param yes: Probability 1 states
	 * @param min: Min or max probabilities (true=min, false=max)
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 */
	protected ModelCheckerResult computeReachProbsPolIter(MDP mdp, BitSet no, BitSet yes, boolean min, int strat[]) throws PrismException
	{
		ModelCheckerResult res;
		int i, n, iters, totalIters;
		double soln[], soln2[];
		boolean done;
		long timer;
		DTMCModelChecker mcDTMC;
		DTMC dtmc;

		// Re-use solution to solve each new policy (strategy)?
		boolean reUseSoln = true;

		// Start policy iteration
		timer = System.currentTimeMillis();
		mainLog.println("Starting policy iteration (" + (min ? "min" : "max") + ")...");

		// Create a DTMC model checker (for solving policies)
		mcDTMC = new DTMCModelChecker(this);
		mcDTMC.inheritSettings(this);
		mcDTMC.setLog(new PrismDevNullLog());

		// Store num states
		n = mdp.getNumStates();

		// Create solution vectors
		soln = new double[n];
		soln2 = new double[n];

		// Initialise solution vectors.
		for (i = 0; i < n; i++)
			soln[i] = soln2[i] = yes.get(i) ? 1.0 : 0.0;

		// If not passed in, create new storage for strategy and initialise
		// Initial strategy just picks first choice (0) everywhere
		if (strat == null) {
			strat = new int[n];
			for (i = 0; i < n; i++)
				strat[i] = 0;
		}
		// Otherwise, just initialise for states not in yes/no
		// (Optimal choices for yes/no should already be known)
		else {
			for (i = 0; i < n; i++)
				if (!(no.get(i) || yes.get(i)))
					strat[i] = 0;
		}

		boolean backwardsGS = (linEqMethod == LinEqMethod.BACKWARDS_GAUSS_SEIDEL);

		// Start iterations
		iters = totalIters = 0;
		done = false;
		while (!done) {
			iters++;
			// Solve induced DTMC for strategy
			dtmc = new DTMCFromMDPMemorylessAdversary(mdp, strat);
			res = mcDTMC.computeReachProbsGaussSeidel(dtmc, no, yes, reUseSoln ? soln : null, null, backwardsGS);
			soln = res.soln;
			totalIters += res.numIters;
			// Check if optimal, improve non-optimal choices
			mdp.mvMultMinMax(soln, min, soln2, null, false, null);
			done = true;
			for (i = 0; i < n; i++) {
				// Don't look at no/yes states - we may not have strategy info for them,
				// so they might appear non-optimal
				if (no.get(i) || yes.get(i))
					continue;
				if (!PrismUtils.doublesAreClose(soln[i], soln2[i], termCritParam, termCrit == TermCrit.ABSOLUTE)) {
					done = false;
					List<Integer> opt = mdp.mvMultMinMaxSingleChoices(i, soln, min, soln2[i]);
					// Only update strategy if strictly better
					if (!opt.contains(strat[i]))
						strat[i] = opt.get(0);
				}
			}
		}

		// Finished policy iteration
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Policy iteration");
		mainLog.println(" took " + iters + " cycles (" + totalIters + " iterations in total) and " + timer / 1000.0 + " seconds.");

		// Return results
		// (Note we don't add the strategy - the one passed in is already there
		// and might have some existing choices stored for other states).
		res = new ModelCheckerResult();
		res.soln = soln;
		res.numIters = totalIters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute reachability probabilities using modified policy iteration.
	 * @param mdp: The MDP
	 * @param no: Probability 0 states
	 * @param yes: Probability 1 states
	 * @param min: Min or max probabilities (true=min, false=max)
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 */
	protected ModelCheckerResult computeReachProbsModPolIter(MDP mdp, BitSet no, BitSet yes, boolean min, int strat[]) throws PrismException
	{
		ModelCheckerResult res;
		int i, n, iters, totalIters;
		double soln[], soln2[];
		boolean done;
		long timer;
		DTMCModelChecker mcDTMC;
		DTMC dtmc;

		// Start value iteration
		timer = System.currentTimeMillis();
		mainLog.println("Starting modified policy iteration (" + (min ? "min" : "max") + ")...");

		// Create a DTMC model checker (for solving policies)
		mcDTMC = new DTMCModelChecker(this);
		mcDTMC.inheritSettings(this);
		mcDTMC.setLog(new PrismDevNullLog());

		// Limit iters for DTMC solution - this implements "modified" policy iteration
		mcDTMC.setMaxIters(100);
		mcDTMC.setErrorOnNonConverge(false);

		// Store num states
		n = mdp.getNumStates();

		// Create solution vectors
		soln = new double[n];
		soln2 = new double[n];

		// Initialise solution vectors.
		for (i = 0; i < n; i++)
			soln[i] = soln2[i] = yes.get(i) ? 1.0 : 0.0;

		// If not passed in, create new storage for strategy and initialise
		// Initial strategy just picks first choice (0) everywhere
		if (strat == null) {
			strat = new int[n];
			for (i = 0; i < n; i++)
				strat[i] = 0;
		}
		// Otherwise, just initialise for states not in yes/no
		// (Optimal choices for yes/no should already be known)
		else {
			for (i = 0; i < n; i++)
				if (!(no.get(i) || yes.get(i)))
					strat[i] = 0;
		}

		boolean backwardsGS = (linEqMethod == LinEqMethod.BACKWARDS_GAUSS_SEIDEL);

		// Start iterations
		iters = totalIters = 0;
		done = false;
		while (!done) {
			iters++;
			// Solve induced DTMC for strategy
			dtmc = new DTMCFromMDPMemorylessAdversary(mdp, strat);
			res = mcDTMC.computeReachProbsGaussSeidel(dtmc, no, yes, soln, null, backwardsGS);
			soln = res.soln;
			totalIters += res.numIters;
			// Check if optimal, improve non-optimal choices
			mdp.mvMultMinMax(soln, min, soln2, null, false, null);
			done = true;
			for (i = 0; i < n; i++) {
				// Don't look at no/yes states - we don't store strategy info for them,
				// so they might appear non-optimal
				if (no.get(i) || yes.get(i))
					continue;
				if (!PrismUtils.doublesAreClose(soln[i], soln2[i], termCritParam, termCrit == TermCrit.ABSOLUTE)) {
					done = false;
					List<Integer> opt = mdp.mvMultMinMaxSingleChoices(i, soln, min, soln2[i]);
					strat[i] = opt.get(0);
				}
			}
		}

		// Finished policy iteration
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Modified policy iteration");
		mainLog.println(" took " + iters + " cycles (" + totalIters + " iterations in total) and " + timer / 1000.0 + " seconds.");

		// Return results
		// (Note we don't add the strategy - the one passed in is already there
		// and might have some existing choices stored for other states).
		res = new ModelCheckerResult();
		res.soln = soln;
		res.numIters = totalIters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Construct strategy information for min/max reachability probabilities.
	 * (More precisely, list of indices of choices resulting in min/max.)
	 * (Note: indices are guaranteed to be sorted in ascending order.)
	 * @param mdp The MDP
	 * @param state The state to generate strategy info for
	 * @param target The set of target states to reach
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param lastSoln Vector of values from which to recompute in one iteration 
	 */
	public List<Integer> probReachStrategy(MDP mdp, int state, BitSet target, boolean min, double lastSoln[]) throws PrismException
	{
		double val = mdp.mvMultMinMaxSingle(state, lastSoln, min, null);
		return mdp.mvMultMinMaxSingleChoices(state, lastSoln, min, val);
	}

	/**
	 * Compute bounded reachability probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target} within k steps.
	 * @param mdp The MDP
	 * @param target Target states
	 * @param k Bound
	 * @param min Min or max probabilities (true=min, false=max)
	 */
	public ModelCheckerResult computeBoundedReachProbs(MDP mdp, BitSet target, int k, boolean min) throws PrismException
	{
		return computeBoundedReachProbs(mdp, null, target, k, min, null, null);
	}

	/**
	 * Compute bounded until probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target},
	 * within k steps, and while remaining in states in {@code remain}.
	 * @param mdp The MDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param k Bound
	 * @param min Min or max probabilities (true=min, false=max)
	 */
	public ModelCheckerResult computeBoundedUntilProbs(MDP mdp, BitSet remain, BitSet target, int k, boolean min) throws PrismException
	{
		return computeBoundedReachProbs(mdp, remain, target, k, min, null, null);
	}

	/**
	 * Compute bounded reachability/until probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target},
	 * within k steps, and while remaining in states in {@code remain}.
	 * @param mdp The MDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param k Bound
	 * @param min Min or max probabilities (true=min, false=max)
	 * @param init Optionally, an initial solution vector (may be overwritten) 
	 * @param results Optional array of size k+1 to store (init state) results for each step (null if unused)
	 */
	public ModelCheckerResult computeBoundedReachProbs(MDP mdp, BitSet remain, BitSet target, int k, boolean min, double init[], double results[])
			throws PrismException
	{
		ModelCheckerResult res = null;
		BitSet unknown;
		int n, iters;
		double soln[], soln2[], tmpsoln[];
		long timer;
		int strat[] = null;
		FMDStrategyStep fmdStrat = null;

		// Start bounded probabilistic reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting bounded probabilistic reachability (" + (min ? "min" : "max") + ")...");

		// Store num states
		n = mdp.getNumStates();

		// Create solution vector(s)
		soln = new double[n];
		soln2 = (init == null) ? new double[n] : init;

		// If required, create/initialise strategy storage
		// Set choices to -1, denoting unknown
		// (except for target states, which are -2, denoting arbitrary)
		if (genStrat) {
			strat = new int[n];
			for (int i = 0; i < n; i++) {
				strat[i] = target.get(i) ? -2 : -1;
			}
			fmdStrat = new FMDStrategyStep(mdp, k);
		}
		
		// Initialise solution vectors. Use passed in initial vector, if present
		if (init != null) {
			for (int i = 0; i < n; i++)
				soln[i] = soln2[i] = target.get(i) ? 1.0 : init[i];
		} else {
			for (int i = 0; i < n; i++)
				soln[i] = soln2[i] = target.get(i) ? 1.0 : 0.0;
		}
		// Store intermediate results if required
		// (compute min/max value over initial states for first step)
		if (results != null) {
			// TODO: whether this is min or max should be specified somehow
			results[0] = Utils.minMaxOverArraySubset(soln2, mdp.getInitialStates(), true);
		}

		// Determine set of states actually need to perform computation for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		if (remain != null)
			unknown.and(remain);

		// Start iterations
		iters = 0;
		while (iters < k) {
			iters++;
			// Matrix-vector multiply and min/max ops
			mdp.mvMultMinMax(soln, min, soln2, unknown, false, strat);
			if (genStrat) {
				fmdStrat.setStepChoices(k - iters, strat);
			}
			// Store intermediate results if required
			// (compute min/max value over initial states for this step)
			if (results != null) {
				// TODO: whether this is min or max should be specified somehow
				results[iters] = Utils.minMaxOverArraySubset(soln2, mdp.getInitialStates(), true);
			}
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}
		// Finished bounded probabilistic reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Bounded probabilistic reachability (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.lastSoln = soln2;
		res.accuracy = AccuracyFactory.boundedNumericalIterations();
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		res.timePre = 0.0;
		if (genStrat) {
			res.strat = fmdStrat;
		}
		return res;
	}

	/**
	 * Compute expected cumulative (step-bounded) rewards.
	 * i.e. compute the min/max reward accumulated within {@code k} steps.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
	public ModelCheckerResult computeCumulativeRewards(MDP mdp, MDPRewards mdpRewards, int k, boolean min) throws PrismException
	{
		ModelCheckerResult res = null;
		int i, n, iters;
		long timer;
		double soln[], soln2[], tmpsoln[];

		// Start expected cumulative reward
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected cumulative reward (" + (min ? "min" : "max") + ")...");

		// Store num states
		n = mdp.getNumStates();

		// Create/initialise solution vector(s)
		soln = new double[n];
		soln2 = new double[n];
		for (i = 0; i < n; i++)
			soln[i] = soln2[i] = 0.0;

		// Start iterations
		iters = 0;
		while (iters < k) {
			iters++;
			// Matrix-vector multiply and min/max ops
			mdp.mvMultRewMinMax(soln, mdpRewards, min, soln2, null, false, null);
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished value iteration
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Expected cumulative reward (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.accuracy = AccuracyFactory.boundedNumericalIterations();
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;

		return res;
	}

	/**
	 * Compute upper bound for maximum expected reward, with the method specified in the settings.
	 * @param mdp the model
	 * @param mdpRewards the rewards
	 * @param target the target states
	 * @param unknown the states that are not target or infinity states
	 * @param inf the infinite states
	 * @return upper bound on Rmax=?[ F target ] for all states
	 */
	double computeReachRewardsMaxUpperBound(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet unknown, BitSet inf) throws PrismException
	{
		if (unknown.isEmpty()) {
			mainLog.println("Skipping upper bound computation, no unknown states...");
			return 0;
		}

		// inf and target states become trap states (with dropped choices)
		BitSet trapStates = (BitSet) target.clone();
		trapStates.or(inf);
		MDP cleanedMDP = new MDPDroppedAllChoices(mdp, trapStates);

		OptionsIntervalIteration iiOptions = OptionsIntervalIteration.from(this);

		double upperBound = 0.0;
		String method = null;
		switch (iiOptions.getBoundMethod()) {
		case VARIANT_1_COARSE:
			upperBound = computeReachRewardsMaxUpperBoundVariant1Coarse(cleanedMDP, mdpRewards, target, unknown, inf);
			method = "variant 1, coarse";
			break;
		case VARIANT_1_FINE:
			upperBound = computeReachRewardsMaxUpperBoundVariant1Fine(cleanedMDP, mdpRewards, target, unknown, inf);
			method = "variant 1, fine";
			break;
		case DEFAULT:
		case VARIANT_2:
			upperBound = computeReachRewardsMaxUpperBoundVariant2(cleanedMDP, mdpRewards, target, unknown, inf);
			method = "variant 2";
			break;
		case DSMPI:
			throw new PrismNotSupportedException("Dijkstra Sweep MPI upper bound heuristic can not be used for Rmax");
		}

		if (method == null) {
			throw new PrismException("Unknown upper bound heuristic");
		}

		mainLog.println("Upper bound for max expectation (" + method + "): " + upperBound);
		return upperBound;
	}

	/**
	 * Compute upper bound for minimum expected reward, with the method specified in the settings.
	 * @param mdp the model
	 * @param mdpRewards the rewards
	 * @param target the target states
	 * @param unknown the states that are not target or infinity states
	 * @param inf the infinite states
	 * @return upper bound on Rmin=?[ F target ] for all unknown states
	 */
	double computeReachRewardsMinUpperBound(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet unknown, BitSet inf) throws PrismException
	{
		// inf and target states become trap states (with dropped choices)
		BitSet trapStates = (BitSet) target.clone();
		trapStates.or(inf);
		MDP cleanedMDP = new MDPDroppedAllChoices(mdp, trapStates);

		OptionsIntervalIteration iiOptions = OptionsIntervalIteration.from(this);

		double upperBound = 0.0;
		String method = null;
		switch (iiOptions.getBoundMethod()) {
		case DEFAULT:
		case DSMPI:
			upperBound = DijkstraSweepMPI.computeUpperBound(this, mdp, mdpRewards, target, unknown);
			method = "Dijkstra Sweep MPI";
			break;
		case VARIANT_1_COARSE:
			upperBound = computeReachRewardsMaxUpperBoundVariant1Coarse(cleanedMDP, mdpRewards, target, unknown, inf);
			method = "using Rmax upper bound via variant 1, coarse";
			break;
		case VARIANT_1_FINE:
			upperBound = computeReachRewardsMaxUpperBoundVariant1Fine(cleanedMDP, mdpRewards, target, unknown, inf);
			method = "using Rmax upper bound via variant 1, fine";
			break;
		case VARIANT_2:
			upperBound = computeReachRewardsMaxUpperBoundVariant2(cleanedMDP, mdpRewards, target, unknown, inf);
			method = "using Rmax upper bound via variant 2";
			break;
		}

		if (method == null) {
			throw new PrismException("Unknown upper bound heuristic");
		}

		mainLog.println("Upper bound for min expectation (" + method + "): " + upperBound);
		return upperBound;
	}

	/**
	 * Return true if the MDP is contracting for all states in the 'unknown'
	 * set, i.e., if Pmin=1( unknown U target) holds.
	 */
	private boolean isContracting(MDP mdp, BitSet unknown, BitSet target)
	{
		// compute Pmin=1( unknown U target )
		BitSet pmin1 = prob1(mdp, unknown, target, true, null);
		BitSet tmp = (BitSet) unknown.clone();
		tmp.andNot(pmin1);
		if (!tmp.isEmpty()) {
			// unknown is not contained in pmin1, not contracting
			return false;
		}
		return true;
	}

	/**
	 * Compute upper bound for maximum expected reward (variant 1, coarse),
	 * i.e., does not compute separate q_t / p_t per SCC.
	 * Uses Rs = S, i.e., does not take reachability into account.
	 * @param mdp the model
	 * @param mdpRewards the rewards
	 * @param target the target states
	 * @param unknown the states that are not target or infinity states
	 * @return upper bound on Rmax=?[ F target ] for all states
	 */
	double computeReachRewardsMaxUpperBoundVariant1Coarse(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet unknown, BitSet inf) throws PrismException
	{
		double[] boundsOnExpectedVisits = new double[mdp.getNumStates()];
		double[] maxRews = new double[mdp.getNumStates()];
		int[] Ct = new int[mdp.getNumStates()];

		StopWatch timer = new StopWatch(getLog());
		timer.start("computing an upper bound for maximal expected reward");

		SCCInfo sccs = SCCComputer.computeTopologicalOrdering(this, mdp, true, null);
		BitSet trivial = new BitSet();

		double q = 0;
		for (int scc = 0, numSCCs = sccs.getNumSCCs(); scc < numSCCs; scc++) {
			IntSet statesForSCC = sccs.getStatesForSCC(scc);

			int cardinality = Math.toIntExact(statesForSCC.cardinality());

			PrimitiveIterator.OfInt itSCC = statesForSCC.iterator();
			while (itSCC.hasNext()) {
				int s = itSCC.nextInt();
				Ct[s] = cardinality;

				boolean hasSelfloop = false;
				for (int ch = 0; ch < mdp.getNumChoices(s); ch++) {
					double probRemain = 0;
					boolean allRemain = true;  // all successors remain in the SCC?
					for (Iterator<Entry<Integer, Double>> it = mdp.getTransitionsIterator(s, ch); it.hasNext(); ) {
						Entry<Integer, Double> t = it.next();
						if (statesForSCC.get(t.getKey())) {
							probRemain += t.getValue();
							hasSelfloop = true;
						} else {
							allRemain = false;
						}
					}

					if (!allRemain) { // action in the set X
						q = max(q, probRemain);
					}
				}

				if (cardinality == 1 && !hasSelfloop) {
					trivial.set(s);
				}
			}
		}

		double p = 1;
		for (int s = 0; s < mdp.getNumStates(); s++) {
			double maxRew = 0;
			for (int ch = 0; ch < mdp.getNumChoices(s); ch++) {
				for (Iterator<Entry<Integer, Double>> it = mdp.getTransitionsIterator(s, ch); it.hasNext(); ) {
					Entry<Integer, Double> t = it.next();
					p = min(p, t.getValue());

					double rew = mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, ch);
					maxRew = max(maxRew, rew);
				}
			}
			maxRews[s] = maxRew;
		}

		double upperBound = 0;
		for (int s = 0; s < mdp.getNumStates(); s++) {
			if (target.get(s) || inf.get(s)) {
				// inf or target states: not relevant, set visits to 0, ignore in summation
				boundsOnExpectedVisits[s] = 0.0;
			} else if (unknown.get(s)) {
				if (trivial.get(s)) {
					// s is a trivial SCC: seen at most once
					boundsOnExpectedVisits[s] = 1.0;
				} else {
					boundsOnExpectedVisits[s] = 1 / (Math.pow(p, Ct[s]-1) * (1.0-q));
				}
				upperBound += boundsOnExpectedVisits[s] * maxRews[s];
			}
		}

		if (OptionsIntervalIteration.from(this).isBoundComputationVerbose()) {
			mainLog.println("Upper bound for max expectation computation (variant 1, coarse):");
			mainLog.println("p = " + p);
			mainLog.println("q = " + q);
			mainLog.println("|Ct| = " + Arrays.toString(Ct));
			mainLog.println("ζ* = " + Arrays.toString(boundsOnExpectedVisits));
			mainLog.println("maxRews = " + Arrays.toString(maxRews));
		}

		timer.stop();
		// mainLog.println("Upper bound for max expectation (variant 1, coarse): " + upperBound);

		if (!Double.isFinite(upperBound)) {
			throw new PrismException("Problem computing an upper bound for the expectation, did not get finite result");
		}

		return upperBound;
	}

	/**
	 * Compute upper bound for maximum expected reward (variant 1, fine).
	 * i.e., does compute separate q_t / p_t per SCC.
	 * Uses Rs = S, i.e., does not take reachability into account.
	 * @param mdp the model
	 * @param mdpRewards the rewards
	 * @param target the target states
	 * @param unknown the states that are not target or infinity states
	 * @return upper bound on Rmax=?[ F target ] for all states
	 */
	double computeReachRewardsMaxUpperBoundVariant1Fine(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet unknown, BitSet inf) throws PrismException
	{
		double[] boundsOnExpectedVisits = new double[mdp.getNumStates()];
		double[] qt = new double[mdp.getNumStates()];
		double[] pt = new double[mdp.getNumStates()];
		double[] maxRews = new double[mdp.getNumStates()];
		int[] Ct = new int[mdp.getNumStates()];

		StopWatch timer = new StopWatch(getLog());
		timer.start("computing an upper bound for maximal expected reward");

		SCCInfo sccs = SCCComputer.computeTopologicalOrdering(this, mdp, true, null);
		BitSet trivial = new BitSet();

		for (int scc = 0, numSCCs = sccs.getNumSCCs(); scc < numSCCs; scc++) {
			IntSet statesForSCC = sccs.getStatesForSCC(scc);

			double q = 0;
			double p = 1;

			int cardinality = Math.toIntExact(statesForSCC.cardinality());

			PrimitiveIterator.OfInt itSCC = statesForSCC.iterator();
			while (itSCC.hasNext()) {
				int s = itSCC.nextInt();

				Ct[s] = cardinality;
				boolean hasSelfloop = false;

				for (int ch = 0; ch < mdp.getNumChoices(s); ch++) {

					double probRemain = 0;
					boolean allRemain = true;  // all successors remain in the SCC?
					for (Iterator<Entry<Integer, Double>> it = mdp.getTransitionsIterator(s, ch); it.hasNext(); ) {
						Entry<Integer, Double> t = it.next();
						if (statesForSCC.get(t.getKey())) {
							probRemain += t.getValue();
							p = min(p, t.getValue());
							hasSelfloop = true;
						} else {
							allRemain = false;
						}
					}

					if (!allRemain) { // action in the set Xt
						q = max(q, probRemain);
					}
				}

				if (cardinality == 1 && !hasSelfloop) {
					trivial.set(s);
				}
			}

			for (int s : statesForSCC) {
				qt[s] = q;
				pt[s] = p;
			}
		}

		for (int s = 0; s < mdp.getNumStates(); s++) {
			double maxRew = 0;
			for (int ch = 0; ch < mdp.getNumChoices(s); ch++) {
				double rew = mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, ch);
				maxRew = max(maxRew, rew);
			}
			maxRews[s] = maxRew;
		}

		double upperBound = 0;
		for (int s = 0; s < mdp.getNumStates(); s++) {
			if (target.get(s) || inf.get(s)) {
				// inf or target states: not relevant, set visits to 0, ignore in summation
				boundsOnExpectedVisits[s] = 0.0;
			} else if (unknown.get(s)) {
				if (trivial.get(s)) {
					// s is a trivial SCC: seen at most once
					boundsOnExpectedVisits[s] = 1.0;
				} else {
					boundsOnExpectedVisits[s] = 1 / (Math.pow(pt[s], Ct[s]-1) * (1.0-qt[s]));
				}
				upperBound += boundsOnExpectedVisits[s] * maxRews[s];
			}
		}

		timer.stop();

		if (OptionsIntervalIteration.from(this).isBoundComputationVerbose()) {
			mainLog.println("Upper bound for max expectation computation (variant 1, fine):");
			mainLog.println("pt = " + Arrays.toString(pt));
			mainLog.println("qt = " + Arrays.toString(qt));
			mainLog.println("|Ct| = " + Arrays.toString(Ct));
			mainLog.println("ζ* = " + Arrays.toString(boundsOnExpectedVisits));
			mainLog.println("maxRews = " + Arrays.toString(maxRews));
		}

		// mainLog.println("Upper bound for max expectation (variant 1, fine): " + upperBound);

		if (!Double.isFinite(upperBound)) {
			throw new PrismException("Problem computing an upper bound for the expectation, did not get finite result");
		}

		return upperBound;
	}

	/**
	 * Compute upper bound for maximum expected reward (variant 2).
	 * Uses Rs = S, i.e., does not take reachability into account.
	 * @param dtmc the model
	 * @param mcRewards the rewards
	 * @param target the target states
	 * @param unknown the states that are not target or infinity states
	 * @param inf the infinity states
	 * @return upper bound on R=?[ F target ] for all states
	 */
	double computeReachRewardsMaxUpperBoundVariant2(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet unknown, BitSet inf) throws PrismException
	{
		double[] dt = new double[mdp.getNumStates()];
		double[] boundsOnExpectedVisits = new double[mdp.getNumStates()];
		double[] maxRews = new double[mdp.getNumStates()];

		StopWatch timer = new StopWatch(getLog());
		timer.start("computing an upper bound for expected reward");

		SCCInfo sccs = SCCComputer.computeTopologicalOrdering(this, mdp, true, unknown::get);

		BitSet T = (BitSet) target.clone();

		@SuppressWarnings("unused")
		int i = 0;
		while (true) {
			BitSet Si = new BitSet();
			i++;

			// TODO: might be inefficient, worst-case quadratic runtime...
			for (PrimitiveIterator.OfInt it = IterableBitSet.getClearBits(T, mdp.getNumStates() -1 ).iterator(); it.hasNext(); ) {
				int s = it.nextInt();
				// mainLog.println("Check " + s + " against " + T);
				boolean allActionsReachT = true;
				for (int choice = 0, choices = mdp.getNumChoices(s); choice < choices; choice++) {
					if (!mdp.someSuccessorsInSet(s, choice, T)) {
						allActionsReachT = false;
						break;
					}
				}
				if (allActionsReachT) {
					Si.set(s);
				}
			}

			if (Si.isEmpty()) {
				break;
			}

			// mainLog.println("S" + i + " = " + Si);
			// mainLog.println("T = " + T);

			for (PrimitiveIterator.OfInt it = IterableBitSet.getSetBits(Si).iterator(); it.hasNext(); ) {
				final int t = it.nextInt();
				final int sccIndexForT = sccs.getSCCIndex(t);

				double min = Double.POSITIVE_INFINITY;
				for (int choice = 0, choices = mdp.getNumChoices(t); choice < choices; choice++) {
					// mainLog.println("State " + t + ", choice = " + choice);
					double d = mdp.sumOverTransitions(t, choice, (int __, int u, double prob) -> {
						// mainLog.println("t = " + t + ", u = " + u + ", prob = " + prob);
						if (!T.get(u))
							return 0.0;

						boolean inSameSCC = (sccs.getSCCIndex(u) == sccIndexForT);
						double d_u_t = inSameSCC ? dt[u] : 1.0;
						// mainLog.println("d_u_t = " + d_u_t);
						return d_u_t * prob;
					});
					if (d < min) {
						min = d;
					}
				}
				dt[t] = min;
				// mainLog.println("d["+t+"] = " + dt[t]);
			}

			T.or(Si);
		}

		for (int s = 0; s < mdp.getNumStates(); s++) {
			double maxRew = 0;
			for (int ch = 0; ch < mdp.getNumChoices(s); ch++) {
				double rew = mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, ch);
				maxRew = max(maxRew, rew);
			}
			maxRews[s] = maxRew;
		}

		double upperBound = 0;
		for (PrimitiveIterator.OfInt it = IterableBitSet.getSetBits(unknown).iterator(); it.hasNext();) {
			int s = it.nextInt();
			boundsOnExpectedVisits[s] = 1 / dt[s];
			upperBound += boundsOnExpectedVisits[s] * maxRews[s];
		}

		timer.stop();

		if (OptionsIntervalIteration.from(this).isBoundComputationVerbose()) {
			mainLog.println("Upper bound for max expectation computation (variant 2):");
			mainLog.println("d_t = " + Arrays.toString(dt));
			mainLog.println("ζ* = " + Arrays.toString(boundsOnExpectedVisits));
		}

		// mainLog.println("Upper bound for expectation (variant 2): " + upperBound);

		if (!Double.isFinite(upperBound)) {
			throw new PrismException("Problem computing an upper bound for the expectation, did not get finite result");
		}

		return upperBound;
	}

	/**
	 * Compute expected instantaneous reward,
	 * i.e. compute the min/max expected reward of the states after {@code k} steps.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param k the number of steps
	 * @param min Min or max rewards (true=min, false=max)
	 */
	public ModelCheckerResult computeInstantaneousRewards(MDP mdp, MDPRewards mdpRewards, final int k, boolean min)
	{
		ModelCheckerResult res = null;
		int i, n, iters;
		double soln[], soln2[], tmpsoln[];
		long timer;

		// Store num states
		n = mdp.getNumStates();

		// Start backwards transient computation
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting backwards instantaneous rewards computation...");

		// Create solution vector(s)
		soln = new double[n];
		soln2 = new double[n];

		// Initialise solution vectors.
		for (i = 0; i < n; i++)
			soln[i] = mdpRewards.getStateReward(i);

		// Start iterations
		for (iters = 0; iters < k; iters++) {
			// Matrix-vector multiply
			mdp.mvMultMinMax(soln, min, soln2, null, false, null);
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished backwards transient computation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Backwards transient instantaneous rewards computation");
		mainLog.println(" took " + iters + " iters and " + timer / 1000.0 + " seconds.");

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.lastSoln = soln2;
		res.accuracy = AccuracyFactory.boundedNumericalIterations();
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		res.timePre = 0.0;
		return res;
	}

	/**
	 * Compute total expected rewards.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param min Min or max rewards (true=min, false=max)
	 */
	public ModelCheckerResult computeTotalRewards(MDP mdp, MDPRewards mdpRewards, boolean min) throws PrismException
	{
		if (min) {
			throw new PrismNotSupportedException("Minimum total expected reward not supported in explicit engine");
		} else {
			// max. We don't know if there are positive ECs, so we can't skip precomputation
			return computeTotalRewardsMax(mdp, mdpRewards, false);
		}
	}

	/**
	 * Compute maximal total expected rewards.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param noPositiveECs if true, there are no positive ECs, i.e., all states have finite values (skip precomputation)
	 */
	public ModelCheckerResult computeTotalRewardsMax(MDP mdp, MDPRewards mdpRewards, boolean noPositiveECs) throws PrismException
	{
		ModelCheckerResult res = null;
		int n;
		long timer;
		BitSet inf;

		// Local copy of setting
		MDPSolnMethod mdpSolnMethod = this.mdpSolnMethod;

		// Switch to a supported method, if necessary
		if (!(mdpSolnMethod == MDPSolnMethod.VALUE_ITERATION || mdpSolnMethod == MDPSolnMethod.GAUSS_SEIDEL || mdpSolnMethod == MDPSolnMethod.POLICY_ITERATION)) {
			mdpSolnMethod = MDPSolnMethod.GAUSS_SEIDEL;
			mainLog.printWarning("Switching to MDP solution method \"" + mdpSolnMethod.fullName() + "\"");
		}
		if (getDoIntervalIteration()) {
			throw new PrismNotSupportedException("Interval iteration for total rewards is currently not supported");
		}

		// Start expected total reward
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting total expected reward (max)...");

		// Store num states
		n = mdp.getNumStates();

		long timerPre;

		if (noPositiveECs) {
			// no inf states
			inf = new BitSet();
			timerPre = 0;
		} else {
			mainLog.println("Precomputation: Find positive end components...");

			timerPre = System.currentTimeMillis();

			ECComputer ecs = ECComputer.createECComputer(this, mdp);
			ecs.computeMECStates();
			BitSet positiveECs = new BitSet();
			for (BitSet ec : ecs.getMECStates()) {
				// check if this MEC is positive
				boolean positiveEC = false;
				for (int state : new IterableStateSet(ec, n)) {
					if (mdpRewards.getStateReward(state) > 0) {
						// state with positive reward in this MEC
						positiveEC = true;
						break;
					}
					for (int choice = 0, numChoices = mdp.getNumChoices(state); choice < numChoices; choice++) {
						if (mdpRewards.getTransitionReward(state, choice) > 0 &&
								mdp.allSuccessorsInSet(state, choice, ec)) {
							// choice from this state with positive reward back into this MEC
							positiveEC = true;
							break;
						}
					}
				}
				if (positiveEC) {
					positiveECs.or(ec);
				}
			}

			// inf = Pmax[ <> positiveECs ] > 0
			//     = ! (Pmax[ <> positiveECs ] = 0)
			inf = prob0(mdp, null, positiveECs, false, null);  // Pmax[ <> positiveECs ] = 0
			inf.flip(0,n);  // !(Pmax[ <> positive ECs ] = 0) = Pmax[ <> positiveECs ] > 0

			timerPre = System.currentTimeMillis() - timerPre;
			mainLog.println("Precomputation took " + timerPre / 1000.0 + " seconds, " + inf.cardinality() + " infinite states, " + (n - inf.cardinality()) + " states remaining.");
		}

		// Compute rewards
		// do standard max reward calculation, but with empty target set
		switch (mdpSolnMethod) {
		case VALUE_ITERATION:
			res = computeReachRewardsValIter(mdp, mdpRewards, new BitSet(), inf, false, null, null, null);
			break;
		case GAUSS_SEIDEL:
			res = computeReachRewardsGaussSeidel(mdp, mdpRewards, new BitSet(), inf, false, null, null, null);
			break;
		case POLICY_ITERATION:
			res = computeReachRewardsPolIter(mdp, mdpRewards, new BitSet(), inf, false, null);
			break;
		default:
			throw new PrismException("Unknown MDP solution method " + mdpSolnMethod.fullName());
		}

		// Finished expected total reward
		timer = System.currentTimeMillis() - timer;
		mainLog.println("Expected total reward took " + timer / 1000.0 + " seconds.");

		// Update time taken
		res.timeTaken = timer / 1000.0;
		res.timePre = timerPre / 1000.0;

		// Return results
		return res;
	}


	/**
	 * Compute expected reachability rewards.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
	public ModelCheckerResult computeReachRewards(MDP mdp, MDPRewards mdpRewards, BitSet target, boolean min) throws PrismException
	{
		return computeReachRewards(mdp, mdpRewards, target, min, null, null);
	}

	/**
	 * Compute expected reachability rewards.
	 * i.e. compute the min/max reward accumulated to reach a state in {@code target}.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 * @param init Optionally, an initial solution vector (may be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values).  
	 * Also, 'known' values cannot be passed for some solution methods, e.g. policy iteration.  
	 */
	public ModelCheckerResult computeReachRewards(MDP mdp, MDPRewards mdpRewards, BitSet target, boolean min, double init[], BitSet known)
			throws PrismException
	{
		ModelCheckerResult res = null;
		BitSet inf;
		int n, numTarget, numInf;
		long timer, timerProb1;
		int strat[] = null;
		Object [] strat_name = null;
		// Local copy of setting
		MDPSolnMethod mdpSolnMethod = this.mdpSolnMethod;

		// Switch to a supported method, if necessary
		if (!(mdpSolnMethod == MDPSolnMethod.VALUE_ITERATION || mdpSolnMethod == MDPSolnMethod.GAUSS_SEIDEL || mdpSolnMethod == MDPSolnMethod.POLICY_ITERATION)) {
			mdpSolnMethod = MDPSolnMethod.GAUSS_SEIDEL;
			mainLog.printWarning("Switching to MDP solution method \"" + mdpSolnMethod.fullName() + "\"");
		}

		// Check for some unsupported combinations
		if (mdpSolnMethod == MDPSolnMethod.POLICY_ITERATION) {
			if (known != null) {
				throw new PrismException("Policy iteration methods cannot be passed 'known' values for some states");
			}
		}
		if (doIntervalIteration) {
			if (mdpSolnMethod != MDPSolnMethod.VALUE_ITERATION && mdpSolnMethod != MDPSolnMethod.GAUSS_SEIDEL) {
				throw new PrismNotSupportedException("Currently, explicit engine only supports interval iteration with value iteration or Gauss-Seidel for MDPs");
			}
		}

		// Start expected reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		mdp.checkForDeadlocks(target);

		// Store num states
		n = mdp.getNumStates();
		// Optimise by enlarging target set (if more info is available)
		if (init != null && known != null && !known.isEmpty()) {
			BitSet targetNew = (BitSet) target.clone();
			for (int i : new IterableBitSet(known)) {
				if (init[i] == 1.0) {
					targetNew.set(i);
				}
			}
			target = targetNew;
		}

		// If required, export info about target states 
		if (getExportTarget()) {
			BitSet bsInit = new BitSet(n);
			for (int i = 0; i < n; i++) {
				bsInit.set(i, mdp.isInitialState(i));
			}
			List<BitSet> labels = Arrays.asList(bsInit, target);
			List<String> labelNames = Arrays.asList("init", "target");
			mainLog.println("\nExporting target states info to file \"" + getExportTargetFilename() + "\"...");
			PrismLog out = new PrismFileLog(getExportTargetFilename());
			exportLabels(mdp, labels, labelNames, Prism.EXPORT_PLAIN, out);
			out.close();
		}

		// If required, create/initialise strategy storage
		// Set choices to -1, denoting unknown
		// (except for target states, which are -2, denoting arbitrary)
		if (genStrat || exportAdv || mdpSolnMethod == MDPSolnMethod.POLICY_ITERATION) {
			strat = new int[n];
			for (int i = 0; i < n; i++) {
				strat[i] = target.get(i) ? -2 : -1;
			}
		}
		
		// Precomputation (not optional)
		timerProb1 = System.currentTimeMillis();
		inf = prob1(mdp, null, target, !min, strat);
		inf.flip(0, n);
		timerProb1 = System.currentTimeMillis() - timerProb1;
		
		// Print results of precomputation
		numTarget = target.cardinality();
		numInf = inf.cardinality();
		mainLog.println("target=" + numTarget + ", inf=" + numInf + ", rest=" + (n - (numTarget + numInf)));

		// If required, generate strategy for "inf" states.
		if (genStrat || exportAdv || mdpSolnMethod == MDPSolnMethod.POLICY_ITERATION) {
			if (min) {
				// If min reward is infinite, all choices give infinity
				// So the choice can be arbitrary, denoted by -2; 
				for (int i = inf.nextSetBit(0); i >= 0; i = inf.nextSetBit(i + 1)) {
					strat[i] = -2;
				}
			} else {
				// If max reward is infinite, there is at least one choice giving infinity.
				// So we pick, for all "inf" states, the first choice for which some transitions stays in "inf".
				for (int i = inf.nextSetBit(0); i >= 0; i = inf.nextSetBit(i + 1)) {
					int numChoices = mdp.getNumChoices(i);
					for (int k = 0; k < numChoices; k++) {
						if (mdp.someSuccessorsInSet(i, k, inf)) {
							strat[i] = k;
							continue;
						}
					}
				}
			}
		}

		// Compute rewards (if needed)
		if (numTarget + numInf < n) {
			
			ZeroRewardECQuotient quotient = null;
			boolean doZeroMECCheckForMin = true;
			if (min & doZeroMECCheckForMin) {
				StopWatch zeroMECTimer = new StopWatch(mainLog);
				zeroMECTimer.start("checking for zero-reward ECs");
				mainLog.println("For Rmin, checking for zero-reward ECs...");
				BitSet unknown = (BitSet) inf.clone();
				unknown.flip(0, mdp.getNumStates());
				unknown.andNot(target);
				quotient = ZeroRewardECQuotient.getQuotient(this, mdp, unknown, mdpRewards);
	
				if (quotient == null) {
					zeroMECTimer.stop("no zero-reward ECs found, proceeding normally");
				} else {
					zeroMECTimer.stop("built quotient MDP with " + quotient.getNumberOfZeroRewardMECs() + " zero-reward MECs");
					if (strat != null) {
						throw new PrismException("Constructing a strategy for Rmin in the presence of zero-reward ECs is currently not supported");
					}
				}
			}
	
			if (quotient != null) {
				BitSet newInfStates = (BitSet)inf.clone();
				newInfStates.or(quotient.getNonRepresentativeStates());
				int quotientModelStates = quotient.getModel().getNumStates() - newInfStates.cardinality();
				mainLog.println("Computing Rmin in zero-reward EC quotient model (" + quotientModelStates + " relevant states)...");
				res = computeReachRewardsNumeric(quotient.getModel(), quotient.getRewards(), mdpSolnMethod, target, newInfStates, min, init, known, strat);
				quotient.mapResults(res.soln);
			} else {
				res = computeReachRewardsNumeric(mdp, mdpRewards, mdpSolnMethod, target, inf, min, init, known, strat);
			}
		} else {
			res = new ModelCheckerResult();
			res.soln = Utils.bitsetToDoubleArray(inf, n, Double.POSITIVE_INFINITY);
			res.accuracy = AccuracyFactory.doublesFromQualitative();
		}
		
		// Store strategy
		if (genStrat) {
			res.strat = new MDStrategyArray(mdp, strat);
			strat_name = new Object[n];
			for (int i = 0; i < n; i++) {
				strat_name[i] = mdp.getAction(i, strat[i]);
			}
//			printToFile(strat_name, res.soln, "prism/tests/gridmap/vi/"+"strat_"+n+".dot", n);

		}
		// Export adversary
		if (exportAdv) {
			// Prune strategy, if needed
			if (getRestrictStratToReach()) {
				restrictStrategyToReachableStates(mdp, strat);
			}
			// Export
			PrismLog out = new PrismFileLog(exportAdvFilename);
			int precision = settings.getInteger(PrismSettings.PRISM_EXPORT_MODEL_PRECISION);
			new DTMCFromMDPMemorylessAdversary(mdp, strat).exportToPrismExplicitTra(out, precision);
			out.close();
		}

		// Finished expected reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.println("Expected reachability took " + timer / 1000.0 + " seconds.");

		// Update time taken
		res.timeTaken = timer / 1000.0;
		res.timePre = timerProb1 / 1000.0;

		return res;
	}

	protected ModelCheckerResult computeReachRewardsNumeric(MDP mdp, MDPRewards mdpRewards, MDPSolnMethod method, BitSet target, BitSet inf, boolean min, double init[], BitSet known, int strat[]) throws PrismException
	{
		ModelCheckerResult res = null;

		IterationMethod iterationMethod = null;
		switch (method) {
		case VALUE_ITERATION:
			iterationMethod = new IterationMethodPower(termCrit == TermCrit.ABSOLUTE, termCritParam);
			break;
		case GAUSS_SEIDEL:
			iterationMethod = new IterationMethodGS(termCrit == TermCrit.ABSOLUTE, termCritParam, false);
			break;
		case POLICY_ITERATION:
			if (doIntervalIteration) {
				throw new PrismNotSupportedException("Interval iteration currently not supported for policy iteration");
			}
			res = computeReachRewardsPolIter(mdp, mdpRewards, target, inf, min, strat);
			break;
		default:
			throw new PrismException("Unknown MDP solution method " + method.fullName());
		}

		if (res == null) { // not yet computed, use iterationMethod
			if (!doIntervalIteration) {
				res = doValueIterationReachRewards(mdp, mdpRewards, iterationMethod, target, inf, min, init, known, getDoTopologicalValueIteration(), strat);
			} else {
				res = doIntervalIterationReachRewards(mdp, mdpRewards, iterationMethod, target, inf, min, init, known, getDoTopologicalValueIteration(), strat);
			}
		}

		return res;
	}

	/**
	 * Compute expected reachability rewards using value iteration.
	 * Optionally, store optimal (memoryless) strategy info. 
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param min Min or max rewards (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.
	 */
	protected ModelCheckerResult computeReachRewardsValIter(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet inf, boolean min, double init[], BitSet known, int strat[])
			throws PrismException
	{
		IterationMethodPower iterationMethod = new IterationMethodPower(termCrit == TermCrit.ABSOLUTE, termCritParam);
		return doValueIterationReachRewards(mdp, mdpRewards, iterationMethod, target, inf, min, init, known, false, strat);
	}

	/**
	 * Compute expected reachability rewards using value iteration.
	 * Optionally, store optimal (memoryless) strategy info.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param min Min or max rewards (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten)
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param topological Do topological value iteration?
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.
	 */
	protected ModelCheckerResult doValueIterationReachRewards(MDP mdp, MDPRewards mdpRewards, IterationMethod iterationMethod, BitSet target, BitSet inf, boolean min, double init[], BitSet known, boolean topological, int strat[])
			throws PrismException
	{
		BitSet unknown;
		int i, n;
		long timer;

		// Start value iteration
		timer = System.currentTimeMillis();
		String description = (min ? "min" : "max") + (topological ? ", topological" : "" ) + ", with " + iterationMethod.getDescriptionShort();
		mainLog.println("Starting value iteration (" + description + ")...");

		ExportIterations iterationsExport = null;
		if (settings.getBoolean(PrismSettings.PRISM_EXPORT_ITERATIONS)) {
			iterationsExport = new ExportIterations("Explicit MDP ReachRewards value iteration (" + description +")");
			mainLog.println("Exporting iterations to " + iterationsExport.getFileName());
		}

		// Store num states
		n = mdp.getNumStates();

		// Initialise solution vectors. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 0.0/infinity if in target/inf; (3) passed in initial value; (4) 0.0
		if (init != null) {
			if (known != null) {
				for (i = 0; i < n; i++)
					init[i] = known.get(i) ? init[i] : target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : init[i];
			} else {
				for (i = 0; i < n; i++)
					init[i] = target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : init[i];
			}
		} else {
			init = new double[n];
			for (i = 0; i < n; i++)
				init[i] = target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : 0.0;
		}

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		unknown.andNot(inf);
		if (known != null)
			unknown.andNot(known);

		if (iterationsExport != null)
			iterationsExport.exportVector(init, 0);

		IterationMethod.IterationValIter forMvMultRewMinMax = iterationMethod.forMvMultRewMinMax(mdp, mdpRewards, min, strat);
		forMvMultRewMinMax.init(init);

		IntSet unknownStates = IntSet.asIntSet(unknown);

		if (topological) {
			// Compute SCCInfo, including trivial SCCs in the subgraph obtained when only considering
			// states in unknown
			SCCInfo sccs = SCCComputer.computeTopologicalOrdering(this, mdp, true, unknown::get);

			IterationMethod.SingletonSCCSolver singletonSCCSolver = (int s, double[] soln) -> {
				soln[s] = mdp.mvMultRewJacMinMaxSingle(s, soln, mdpRewards, min, strat);
			};

			// run the actual value iteration
			return iterationMethod.doTopologicalValueIteration(this, description, sccs, forMvMultRewMinMax, singletonSCCSolver, timer, iterationsExport);
		} else {
			// run the actual value iteration
			return iterationMethod.doValueIteration(this, description, forMvMultRewMinMax, unknownStates, timer, iterationsExport);
		}
	}

	/**
	 * Compute expected reachability rewards using Gauss-Seidel (including Jacobi-style updates).
	 * Optionally, store optimal (memoryless) strategy info. 
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param min Min or max rewards (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.
	 */
	protected ModelCheckerResult computeReachRewardsGaussSeidel(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet inf, boolean min, double init[],
			BitSet known, int strat[]) throws PrismException
	{
		IterationMethodGS iterationMethod = new IterationMethodGS(termCrit == TermCrit.ABSOLUTE, termCritParam, false);
		return doValueIterationReachRewards(mdp, mdpRewards, iterationMethod, target, inf, min, init, known, false, strat);
	}

	/**
	 * Compute expected reachability rewards using interval iteration
	 * Optionally, store optimal (memoryless) strategy info.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param min Min or max rewards (true=min, false=max)
	 * @param init Optionally, an initial solution vector (will be overwritten)
	 * @param known Optionally, a set of states for which the exact answer is known
	 * @param topological do topological interval iteration
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.
	 */
	protected ModelCheckerResult doIntervalIterationReachRewards(MDP mdp, MDPRewards mdpRewards, IterationMethod iterationMethod, BitSet target, BitSet inf, boolean min, double init[], BitSet known, boolean topological, int strat[])
			throws PrismException
	{
		BitSet unknown;
		int i, n;
		double initBelow[], initAbove[];
		long timer;

		// Store num states
		n = mdp.getNumStates();

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		unknown.andNot(inf);
		if (known != null)
			unknown.andNot(known);

		OptionsIntervalIteration iiOptions = OptionsIntervalIteration.from(this);

		double upperBound;
		if (iiOptions.hasManualUpperBound()) {
			upperBound = iiOptions.getManualUpperBound();
			getLog().printWarning("Upper bound for interval iteration manually set to " + upperBound);
		} else {
			if (min) {
				upperBound = computeReachRewardsMinUpperBound(mdp, mdpRewards, target, unknown, inf);
			} else {
				upperBound = computeReachRewardsMaxUpperBound(mdp, mdpRewards, target, unknown, inf);
			}
		}

		double lowerBound;
		if (iiOptions.hasManualLowerBound()) {
			lowerBound = iiOptions.getManualLowerBound();
			getLog().printWarning("Lower bound for interval iteration manually set to " + lowerBound);
		} else {
			lowerBound = 0.0;
		}

		if (min) {
			if (!isContracting(mdp, unknown, target)) {
				throw new PrismNotSupportedException("Interval iteration for Rmin and non-contracting MDP currently not supported");
			} else {
				mainLog.println("Relevant sub-MDP is contracting, proceed...");
			}
		}

		// Start value iteration
		timer = System.currentTimeMillis();
		String description = (min ? "min" : "max") + (topological ? ", topological" : "") + ", with " + iterationMethod.getDescriptionShort();
		mainLog.println("Starting interval iteration (" + description + ")...");

		ExportIterations iterationsExport = null;
		if (settings.getBoolean(PrismSettings.PRISM_EXPORT_ITERATIONS)) {
			iterationsExport = new ExportIterations("Explicit MDP ReachRewards interval iteration (" + description + ")");
			mainLog.println("Exporting iterations to " + iterationsExport.getFileName());
		}

		// Create initial solution vector(s)
		initBelow = (init == null) ? new double[n] : init;
		initAbove = new double[n];

		// Initialise solution vector from below. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 0.0/infinity if in target/inf; (3) lowerBound
		if (init != null && known != null) {
			for (i = 0; i < n; i++)
				initBelow[i] = known.get(i) ? init[i] : target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : lowerBound;
		} else {
			for (i = 0; i < n; i++)
				initBelow[i] = target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : lowerBound;
		}

		// Initialise solution vector from above. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 0.0/infinity if in target/inf; (3) upperBound
		if (init != null && known != null) {
			for (i = 0; i < n; i++)
				initAbove[i] = known.get(i) ? init[i] : target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : upperBound;
		} else {
			for (i = 0; i < n; i++)
				initAbove[i] = target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : upperBound;
		}

		if (iterationsExport != null) {
			iterationsExport.exportVector(initBelow, 0);
			iterationsExport.exportVector(initAbove, 1);
		}

		final boolean enforceMonotonicFromBelow = iiOptions.isEnforceMonotonicityFromBelow();
		final boolean enforceMonotonicFromAbove = iiOptions.isEnforceMonotonicityFromAbove();
		final boolean checkMonotonic = iiOptions.isCheckMonotonicity();

		if (!enforceMonotonicFromAbove) {
			getLog().println("Note: Interval iteration is configured to not enforce monotonicity from above.");
		}
		if (!enforceMonotonicFromBelow) {
			getLog().println("Note: Interval iteration is configured to not enforce monotonicity from below.");
		}

		IterationMethod.IterationIntervalIter below = iterationMethod.forMvMultRewMinMaxInterval(mdp, mdpRewards, min, strat, true, enforceMonotonicFromBelow, checkMonotonic);
		IterationMethod.IterationIntervalIter above = iterationMethod.forMvMultRewMinMaxInterval(mdp, mdpRewards, min, strat, false, enforceMonotonicFromAbove, checkMonotonic);
		below.init(initBelow);
		above.init(initAbove);

		IntSet unknownStates = IntSet.asIntSet(unknown);

		ModelCheckerResult rv;
		if (topological) {
			// Compute SCCInfo, including trivial SCCs in the subgraph obtained when only considering
			// states in unknown
			SCCInfo sccs = SCCComputer.computeTopologicalOrdering(this, mdp, true, unknown::get);

			IterationMethod.SingletonSCCSolver singletonSCCSolver = (int s, double[] soln) -> {
				soln[s] = mdp.mvMultRewJacMinMaxSingle(s, soln, mdpRewards, min, strat);
			};

			// run the actual value iteration
			rv = iterationMethod.doTopologicalIntervalIteration(this, description, sccs, below, above, singletonSCCSolver, timer, iterationsExport);
		} else {
			// run the actual value iteration
			rv = iterationMethod.doIntervalIteration(this, description, below, above, unknownStates, timer, iterationsExport);
		}

		double max_v = PrismUtils.findMaxFinite(rv.soln, unknownStates.iterator());
		if (max_v != Double.NEGATIVE_INFINITY) {
			mainLog.println("Maximum finite value in solution vector at end of interval iteration: " + max_v);
		}

		return rv;
	}

	/**
	 * Compute expected reachability rewards using policy iteration.
	 * The array {@code strat} is used both to pass in the initial strategy for policy iteration,
	 * and as storage for the resulting optimal strategy (if needed).
	 * Passing in an initial strategy is required when some states have infinite reward,
	 * to avoid the possibility of policy iteration getting stuck on an infinite-value strategy.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param min Min or max rewards (true=min, false=max)
	 * @param strat Storage for (memoryless) strategy choice indices (ignored if null)
	 */
	protected ModelCheckerResult computeReachRewardsPolIter(MDP mdp, MDPRewards mdpRewards, BitSet target, BitSet inf, boolean min, int strat[])
			throws PrismException
	{
		ModelCheckerResult res;
		int i, n, iters, totalIters;
		double soln[], soln2[];
		boolean done;
		long timer;
		DTMCModelChecker mcDTMC;
		DTMC dtmc;
		MCRewards mcRewards;

		// Re-use solution to solve each new policy (strategy)?
		boolean reUseSoln = true;

		// Start policy iteration
		timer = System.currentTimeMillis();
		mainLog.println("Starting policy iteration (" + (min ? "min" : "max") + ")...");

		// Create a DTMC model checker (for solving policies)
		mcDTMC = new DTMCModelChecker(this);
		mcDTMC.inheritSettings(this);
		mcDTMC.setLog(new PrismDevNullLog());

		// Store num states
		n = mdp.getNumStates();

		// Create solution vector(s)
		soln = new double[n];
		soln2 = new double[n];

		// Initialise solution vectors.
		for (i = 0; i < n; i++)
			soln[i] = soln2[i] = target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : 0.0;

		// If not passed in, create new storage for strategy and initialise
		// Initial strategy just picks first choice (0) everywhere
		if (strat == null) {
			strat = new int[n];
			for (i = 0; i < n; i++)
				strat[i] = 0;
		}
			
		// Start iterations
		iters = totalIters = 0;
		done = false;
		while (!done && iters < maxIters) {
			iters++;
			// Solve induced DTMC for strategy
			dtmc = new DTMCFromMDPMemorylessAdversary(mdp, strat);
			mcRewards = new MCRewardsFromMDPRewards(mdpRewards, strat);
			res = mcDTMC.computeReachRewardsValIter(dtmc, mcRewards, target, inf, reUseSoln ? soln : null, null);
			soln = res.soln;
			totalIters += res.numIters;
			// Check if optimal, improve non-optimal choices
			mdp.mvMultRewMinMax(soln, mdpRewards, min, soln2, null, false, null);
			done = true;
			for (i = 0; i < n; i++) {
				// Don't look at target/inf states - we may not have strategy info for them,
				// so they might appear non-optimal
				if (target.get(i) || inf.get(i))
					continue;
				if (!PrismUtils.doublesAreClose(soln[i], soln2[i], termCritParam, termCrit == TermCrit.ABSOLUTE)) {
					done = false;
					List<Integer> opt = mdp.mvMultRewMinMaxSingleChoices(i, soln, mdpRewards, min, soln2[i]);
					// Only update strategy if strictly better
					if (!opt.contains(strat[i]))
						strat[i] = opt.get(0);
				}
			}
		}

		// Finished policy iteration
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Policy iteration");
		mainLog.println(" took " + iters + " cycles (" + totalIters + " iterations in total) and " + timer / 1000.0 + " seconds.");

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.numIters = totalIters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute expected reachability rewards.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
	public ModelCheckerResult computeReachRewardsDistr(MDP mdp, MDPRewards mdpRewards, BitSet target, boolean min) throws PrismException {
		// Start expected reachability
		long timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		mdp.checkForDeadlocks(target);

		// Store num states
		int n = mdp.getNumStates();
		
		// Precomputation (not optional)
		long timerProb1 = System.currentTimeMillis();
		BitSet inf = prob1(mdp, null, target, !min, null);
		inf.flip(0, n);
		timerProb1 = System.currentTimeMillis() - timerProb1;

		// Print results of precomputation
		int numTarget = target.cardinality();
		int numInf = inf.cardinality();
		mainLog.println("target=" + numTarget + ", inf=" + numInf + ", rest=" + (n - (numTarget + numInf)));

		// Timers
		timer = System.currentTimeMillis();
		long total_timer = System.currentTimeMillis();
		long iteration_timer; long max_iteration_timer=-1;

		// Set up distribution variables
		int atoms;
		int iterations = 1500;
		int min_iter = 20;
		double error_thresh = 0.01;
		double error_thresh_cvar = 2;
		double gamma = 1;
		double alpha=0.5;
		Double dtmc_epsilon = null;
		boolean check_prob_reach_dtmc = false;
		boolean check_reach_dtmc_distr = true;
		boolean check_prob_reach_dtmc_vi = false;
		boolean check_reach_dtmc_distr_vi = true;
		boolean gen_trace = true;
		boolean compute_dtmc_vi = true; // Toggle computing non distr Exp VI
		String bad_states_label = "obs";

		String c51 = "C51";
		String qr = "QR";

		int nactions = mdp.getMaxNumChoices();

		// Determine set of states actually need to compute values for
		BitSet unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		unknown.andNot(inf);
		IntSet unknownStates = IntSet.asIntSet(unknown);
		//	int numS = unknownStates.cardinality();
		DistributionalBellman operator;


		if (settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD).equals(c51)) {
			// TODO remove this in final version
			String [] params = readParams(null);
			atoms = Integer.parseInt(params[0]);
			double v_min = Double.parseDouble(params[1]);
			double v_max = Double.parseDouble(params[2]);
			error_thresh = Double.parseDouble(params[3]); // 0.7 for uav
			dtmc_epsilon = Double.parseDouble(params[4]);
			alpha = Double.parseDouble(params[5]);

			operator = new DistributionalBellmanCategorical(atoms, v_min, v_max, n, mainLog);
			operator.initialize(n); // initialization based on parameters.
			mainLog.println("----- Parameters:\natoms:"+atoms+" - vmax:"+v_max+" - vmin:"+v_min);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh+ " - epsilon:"+dtmc_epsilon);
		} else if (settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD).equals(qr)) {
			String [] params = readParams(null);
			atoms = Integer.parseInt(params[0]);
			error_thresh = Double.parseDouble(params[3]); // 0.7 for uav
			dtmc_epsilon = Double.parseDouble(params[4]);
			alpha = Double.parseDouble(params[5]);

			operator = new DistributionalBellmanQR(atoms, n, mainLog);
			operator.initialize(n); // initialization based on parameters.
			mainLog.println("----- Parameters:\natoms:"+atoms+
					" - alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh + " - epsilon:"+dtmc_epsilon);
		}
		else{
			atoms=101;
			double v_max = 100;
			double v_min = 0;
			operator = new DistributionalBellmanCategorical(atoms, v_min, v_max, n, mainLog);
			operator.initialize(n); // initialization based on parameters.
		}

		// Create/initialise solution vector(s)
		double[][] temp_p;
		double [][] action_val = new double[n][nactions];
		double [] action_cvar = new double[n];
		Object [] policy = new Object[n];
		int[] choices = new int[n];
		double min_v;
		double max_dist ;
		double max_cvar_dist ;
		int iters;

		// Start iterations - number of episodes
		for (iters = 0; (iters < iterations) ; iters++)
		{
			iteration_timer = System.currentTimeMillis();
			temp_p = new double[n][atoms];
			// copy to temp value soln2
			for (int k=0; k<n; k++) {
				temp_p[k] = Arrays.copyOf(operator.getDist(k), operator.getDist(k).length);
			}

			PrimitiveIterator.OfInt states = unknownStates.iterator();
			while (states.hasNext()) {
				final int s = states.nextInt();
				int numChoices = mdp.getNumChoices(s);
				int numTransitions = 0;
				double[][] save_p = new double[numChoices][atoms];
				Arrays.fill(action_val[s], Float.POSITIVE_INFINITY);

				for (int choice = 0; choice < numChoices; choice++){ // aka action
					double [] m ; numTransitions = mdp.getNumTransitions(s, choice);
					Iterator<Entry<Integer, Double>>it = mdp.getTransitionsIterator(s,choice);

					double reward = mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, choice);
					m = operator.step(it, numTransitions, gamma, reward);

					action_val[s][choice] = operator.getExpValue(m);
					save_p[choice] = Arrays.copyOf(m, m.length);
				}
				 // TODO optimize this to be consistent with cvar distr VI
				int min_i = 0;
				min_v = Float.POSITIVE_INFINITY;
				for (int i =0; i<numChoices; i++) {
					if (action_val[s][i] < min_v){ min_i = i; min_v = action_val[s][i]; action_cvar[s]=min_v; policy[s] = mdp.getAction(s, i);choices[s] = i;}
				}
				temp_p[s] = Arrays.copyOf(save_p[min_i], save_p[min_i].length);
			}

			states = unknownStates.iterator();
			max_dist = 0.0;

			//ArrayList<Integer> bad = new ArrayList<>();
			while (states.hasNext()) {
				final int s = states.nextInt();
				double tempo = operator.getW(temp_p[s], s);
				//if(tempo > max_dist){bad.add(s);}
				max_dist = max(max_dist, tempo);

				operator.update(temp_p[s], s);
			}
			mainLog.println("Max Wp dist :"+(max_dist) + " error Wp:" + (error_thresh) +" at iter:"+iters);
			if ((max_dist <error_thresh)&(iters>min_iter)) {
				break;
			}

			iteration_timer = System.currentTimeMillis() - iteration_timer;
			max_iteration_timer = max(iteration_timer, max_iteration_timer);
		}

		// Print to file
		boolean print= false;
		if (print) {
			printToFile(policy, action_val, alpha, "gridmap/cvar_out_"+n+"_"+ settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD) +"_"+alpha+".out", n, mdp.getMaxNumChoices());
		}

		mainLog.println("\nV[start] at " + (iters + 1) + " with method "+settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD));
		DecimalFormat df = new DecimalFormat("0.000");
		mainLog.print("[");
		Arrays.stream(operator.getDist(mdp.getFirstInitialState())).forEach(e -> mainLog.print(df.format(e) + ", "));
		mainLog.print("]\n");

		// Policy
//		mainLog.println("\nPolicy");
//		//Arrays.toString(policy);
//		mainLog.println(Arrays.toString(policy));

		timer = System.currentTimeMillis() - timer;
		if (verbosity >= 1) {
			mainLog.print("\nValue iteration computation (" + (min ? "min" : "max") + ")");
			mainLog.println(" : " + timer / 1000.0 + " seconds.");
			mainLog.println("Max time for 1 iteration :"+max_iteration_timer/1000.0+"s");
		}
		timer = System.currentTimeMillis();

		// Compute distribution on induced DTMC
		mainLog.println("Computing distribution on induced DTMC...");
		MDStrategy strat = new MDStrategyArray(mdp, choices);
		DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
		int initialState = dtmc.getFirstInitialState();
		StateRewardsArray mcRewards = new StateRewardsArray(n);
		for (int s = 0; s < n; s++) {
			mcRewards.setStateReward(s, mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, choices[s]));
		}
		DTMCModelChecker mcDTMC = new DTMCModelChecker(this);
		if(check_reach_dtmc_distr) {
			timer = System.currentTimeMillis();
			ModelCheckerResult dtmc_result = mcDTMC.computeReachRewardsDistr(dtmc, mcRewards, target, "prism/distr_dtmc_exp.csv", dtmc_epsilon);
			timer = System.currentTimeMillis() - timer;
			if (verbosity >= 1) {
				mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
				mainLog.println(" : " + timer / 1000.0 + " seconds.");
				mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
				mainLog.println(" : " + timer / 1000.0 + " seconds.");
			}
			timer = System.currentTimeMillis();
			double [] adjusted_dtmc_distr=operator.adjust_support(((TreeMap)dtmc_result.solnObj[initialState]));
			mainLog.println("Wasserstein p="+(settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD).equals(c51) ? "2" : "1")+" dtmc vs code distributions: "+operator.getW(adjusted_dtmc_distr, initialState));
		}

		if (check_prob_reach_dtmc){
			BitSet obs_states= mdp.getLabelStates(bad_states_label);
			ModelCheckerResult result_obs = mcDTMC.computeReachProbs(dtmc, obs_states);
			mainLog.println("Probs of reaching bad states :" + result_obs.soln[initialState]);
		}

		operator.writeToFile(dtmc.getFirstInitialState(), null);
		MDPSimple mdpToSimulate = new MDPSimple(mdp);
		if(gen_trace) {
			exportTrace(mdpToSimulate, target, strat, "exp");
		}

		// Calling regular Value Iteration for comparison metrics.
		if(compute_dtmc_vi){
			mainLog.println("---------------------------------------\nStarting PRISM VI");
			DTMCModelChecker vi_mcDTMC= new DTMCModelChecker(this);
			timer = System.currentTimeMillis();
			ModelCheckerResult vi_res = this.computeReachRewards(mdp, mdpRewards, target, min);
			timer = System.currentTimeMillis() - timer;
			if (verbosity >= 1) {
				mainLog.print("\nPRISM VI");
				mainLog.println(" : " + timer / 1000.0 + " seconds.");
			}
			timer = System.currentTimeMillis();

			mainLog.println("\nVI result in initial state:"+ vi_res.soln[mdp.getFirstInitialState()]);
			StateRewardsArray vi_mcRewards = new StateRewardsArray(n); // Compute rewards array
			for (int s = 0; s < n; s++) {
				if(target.get(s)) {
					vi_mcRewards.setStateReward(s,0);
					((MDStrategyArray) vi_res.strat).choices[s] = 0;
				}
				else {
					double transition_reward = 0;
					if (((MDStrategy) vi_res.strat).isChoiceDefined(s)) {
						transition_reward = mdpRewards.getTransitionReward(s, ((MDStrategy) vi_res.strat).getChoiceIndex(s));
					}
					else if(((MDStrategy) vi_res.strat).whyUndefined(s, -1) == UndefinedReason.ARBITRARY) {
						transition_reward = mdpRewards.getTransitionReward(s, 0);
					}
					else {
						mainLog.println(" Error in strategy: choice is :"+ ((MDStrategy) vi_res.strat).getChoiceActionString(s, -1));
					}
					vi_mcRewards.setStateReward(s, mdpRewards.getStateReward(s) + transition_reward);
				}
			}
			DTMC vi_dtmc = new DTMCFromMDPAndMDStrategy(mdp, (MDStrategy) vi_res.strat);
			if (check_reach_dtmc_distr_vi) {
				timer = System.currentTimeMillis();
				vi_mcDTMC.computeReachRewardsDistr(vi_dtmc, vi_mcRewards, target, "prism/distr_dtmc_vi.csv", dtmc_epsilon);
				timer = System.currentTimeMillis() - timer;
				if (verbosity >= 1) {
					mainLog.print("\nDTMC computation VI");
					mainLog.println(" : " + timer / 1000.0 + " seconds.");
				}
			}

			if (check_prob_reach_dtmc_vi){
				BitSet obs_states= mdp.getLabelStates(bad_states_label);
				ModelCheckerResult result_obs = mcDTMC.computeReachProbs(vi_dtmc, obs_states);
				mainLog.println("Probs of reaching bad states :" + result_obs.soln[vi_dtmc.getFirstInitialState()]);
			}

			if(gen_trace) {
				exportTrace(mdpToSimulate, target, (MDStrategy) vi_res.strat, "vi");
			}

		}
		// Store results
		ModelCheckerResult res = new ModelCheckerResult();
		res.soln = Arrays.copyOf(action_cvar, action_cvar.length); // FIXME make it based on y parameter and iterate over columns to get result
		res.numIters = iterations;
		res.timeTaken = (System.currentTimeMillis() - total_timer) / 1000.0;
		return res;
	}

	public void exportTrace(MDPSimple mdp, BitSet target, MDStrategy strat, String method)
	{
		List<State> mdpStatesList = mdp.getStatesList();
		int maxPathLen = 1000;
		int s = mdp.getFirstInitialState();
		int idx_b;
		mainLog.println("Generating random trace");
//		mainLog.println(mdpStatesList.get(s));
		PrismFileLog trace_out = new PrismFileLog("prism/tests/traces/distr_"+method+"_"+ settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD) +"_trace.csv");
		trace_out.println("s; actions; policy");
		int pathLen = 0;
		List<Object> available = new ArrayList<>();
		while (pathLen < maxPathLen  && mdp.getNumTransitions(s, strat.getChoiceIndex(s)) > 0) {
			available=mdp.getAvailableActions(s);
			trace_out.println(mdpStatesList.get(s).toString()+";"+available.toString()+";"+strat.getChoiceAction(s));
			Distribution distr = mdp.getChoice(s, strat.getChoiceIndex(s));
			if (target.get(s))
			{
				mainLog.println("Terminal state reached s: "+mdpStatesList.get(s).toString());
				break;
			}
			s = distr.sample();
			pathLen++;
		}
		trace_out.close();
	}


	// alternative version to be integrated with cvar focused iteration
	public ModelCheckerResult computeReachRewardsCvar(MDP mdp, MDPRewards mdpRewards, BitSet target, boolean min) throws PrismException {
		// Start expected reachability
		long timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		mdp.checkForDeadlocks(target);

		// Store num states
		int n = mdp.getNumStates();
		int n_actions = mdp.getMaxNumChoices();

		// Precomputation (not optional)
		long timerProb1 = System.currentTimeMillis();
		BitSet inf = prob1(mdp, null, target, !min, null);
		inf.flip(0, n);
		timerProb1 = System.currentTimeMillis() - timerProb1;

		// Print results of precomputation
		int numTarget = target.cardinality();
		int numInf = inf.cardinality();
		mainLog.println("target=" + numTarget + ", inf=" + numInf + ", rest=" + (n - (numTarget + numInf)));

		if (numInf == n){
			throw new PrismException("All states are infinite");
		}

		// Timers
		timer = System.currentTimeMillis();
		long total_timer = System.currentTimeMillis();
		long max_iteration_timer = -1; long iteration_timer;

		// Set up CVAR variables
		int atoms;
		int iterations = 1500;
		double error_thresh = 0.01;
		Double dtmc_epsilon = null;
		int min_iter = 50;
		double gamma = 1;
		double alpha = 0.7;
		String bad_states_label = "obs";
		boolean check_prob_reach_dtmc = false;
		boolean check_reach_dtmc_distr= true;
		boolean gen_trace = true;

		String c51 = "C51";
		String qr = "QR";

		int nactions = mdp.getMaxNumChoices();
		DistributionalBellmanAugmented operator;
		int b_atoms;

		BitSet unknown_original = new BitSet();
		unknown_original.set(0, n);
		unknown_original.andNot(target);
		unknown_original.andNot(inf);

		mainLog.println(" Starting Cvar iteration with method: "+settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD));

		if (settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD).equals(c51)) {
			// TODO make this a point variable or something to be a bit cleaner
			String [] params = readParams(null);
			atoms = Integer.parseInt(params[0]);
			double v_min = Double.parseDouble(params[1]);
			double v_max = Double.parseDouble(params[2]);
			error_thresh = Double.parseDouble(params[3]);
			dtmc_epsilon = Double.parseDouble(params[4]);
			alpha = Double.parseDouble(params[5]);

			params = readParams("prism/tests/params_b.csv");
			b_atoms = Integer.parseInt(params[0]);
			double b_min = Double.parseDouble(params[1]);
			double b_max = Double.parseDouble(params[2]);

			operator = new DistributionalBellmanCategoricalAugmented(atoms, b_atoms, v_min, v_max, b_min, b_max, n, n_actions, mainLog);
			operator.initialize(mdp, mdpRewards, gamma, unknown_original); // initialization based on parameters.
			mainLog.println("----- Parameters:\natoms:"+atoms+" - vmax:"+v_max+" - vmin:"+v_min+" - b_atoms:"+b_atoms+" - bmin:"+b_min+" - bmax:"+b_max);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh + " - epsilon:"+ dtmc_epsilon);
		}
		else if (settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD).equals(qr)) {
			//error_thresh = 1.0/atoms*3.1; // 0.7 for uav
			String [] params = readParams(null);
			atoms = Integer.parseInt(params[0]);
			error_thresh = Double.parseDouble(params[3]);
			dtmc_epsilon = Double.parseDouble(params[4]);
			alpha = Double.parseDouble(params[5]);

			params = readParams("prism/tests/params_b.csv");
			b_atoms = Integer.parseInt(params[0]);
			double b_min = Double.parseDouble(params[1]);
			double b_max = Double.parseDouble(params[2]);

			operator = new DistributionalBellmanQRAugmented(atoms, b_atoms, b_min, b_max, n, n_actions, mainLog);
			operator.initialize(mdp, mdpRewards, gamma, unknown_original); // initialization based on parameters.
			mainLog.println("----- Parameters:\natoms:"+atoms+" - b_atoms:"+b_atoms+" - bmin:"+b_min+" - bmax:"+b_max);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh+ " - epsilon:"+ dtmc_epsilon);
		}
		else {
			atoms = 101;
			b_atoms = 11;
			double b_max = 20;
			double b_min = 0;
			double v_max = 100;
			double v_min = 0;
			operator = new DistributionalBellmanCategoricalAugmented(atoms, b_atoms, v_min, v_max, b_min, b_max, n, n_actions, mainLog);
			operator.initialize(mdp, mdpRewards, gamma, unknown_original); // initialization based on parameters.
		}

		CVaRProduct cvar_mdp = operator.getProductMDP();
		int product_n = cvar_mdp.getProductModel().getNumStates();
		BitSet product_target = cvar_mdp.liftFromModel(target); // compute the target states in the product MDP

		// Determine set of states actually need to compute values for in the augmented MDP
		BitSet unknown = new BitSet();
		unknown.set(0, product_n);
		unknown.andNot(product_target);
		unknown.andNot(inf);
		IntSet unknownStates = IntSet.asIntSet(unknown);

		// Create/initialise solution vector(s)
		// adjust dimensions to augmented
		DistributionalBellmanAugmented temp_p;
		double[][] action_val = new double[product_n][nactions];
		Object[] policy = new Object[product_n]; // policy is now state x slack variable b
		int[] choices = new int[product_n];
		double min_v;
		double max_dist;
		double max_cvar_dist;
		int action;
		int iters;
		double reward = 0;
		int numChoices;
		int numTransitions;
		int model_s;

		// Start iterations - number of episodes
		for (iters = 0; (iters < iterations); iters++) {

			iteration_timer = System.currentTimeMillis();
			temp_p = operator.copy();

			PrimitiveIterator.OfInt states = unknownStates.iterator();
			// Loop over augmented state
			while (states.hasNext()) {
				final int s = states.nextInt();
				numChoices = cvar_mdp.getProductModel().getNumChoices(s);
				model_s = cvar_mdp.getModelState(s);
				Arrays.fill(action_val[s], Float.POSITIVE_INFINITY);
				double min_magic = Float.POSITIVE_INFINITY;
				int min_a = 0;
				double [] save_p = new double[atoms];

				for (int choice = 0; choice < numChoices; choice++) { // aka action
					double[] m;
					numTransitions = cvar_mdp.getProductModel().getNumTransitions(s, choice);
					Iterator<Entry<Integer, Double>> it = cvar_mdp.getProductModel().getTransitionsIterator(s, choice);

					reward = mdpRewards.getStateReward(model_s) + mdpRewards.getTransitionReward(model_s, choice);
					m = operator.step(it, numTransitions, gamma, reward);

					action_val[s][choice] = operator.getMagic(m, cvar_mdp.getAutomatonState(s));
					if (action_val[s][choice] < min_magic) {
						min_a = choice;
						min_magic = action_val[s][choice];
						save_p = Arrays.copyOf(m, m.length);
					}
				}
				policy[s] = cvar_mdp.getProductModel().getAction(s, min_a);
				choices[s] = min_a;
				temp_p.update(save_p, s);
			}

			states = unknownStates.iterator();
			max_dist = 0.0;
			max_cvar_dist = 0.0;
			action = 0;
//			ArrayList<Integer> bad = new ArrayList<>();

			while (states.hasNext()) {
				final int s = states.nextInt();
				int b = cvar_mdp.getAutomatonState(s);
				double tempo = operator.getW(temp_p.getDist(s), s);
				max_dist = max(max_dist, tempo);
//				max_cvar_dist = max(max_cvar_dist,
//						abs(operator.getValueCvar(temp_p.getDist(s), alpha, b) - operator.getValueCvar(operator.getDist(s), alpha, b)));
				operator.update(temp_p.getDist(s), s);

			}

			mainLog.println("Max Wp dist :"+(max_dist) + " error Wp:" + (error_thresh) +" at iter:"+iters);

//			& (max_cvar_dist < error_thresh_cvar)
			if ((max_dist <error_thresh) &(iters>min_iter)) {
				break;
			}

			iteration_timer = System.currentTimeMillis() - iteration_timer;
			max_iteration_timer = max(iteration_timer, max_iteration_timer);
		}

		mainLog.println("\nV[0] at " + (iters + 1) + " with method " + settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD));
		operator.display(cvar_mdp.getProductModel().getFirstInitialState());

		// Policy
//		mainLog.println("\nPolicy");
//		//Arrays.toString(policy);
//		Arrays.stream(policy).forEach(e -> mainLog.print(e + ", "));

		// Finished CVAR
		timer = System.currentTimeMillis() - timer;
		if (verbosity >= 1) {
			mainLog.print("\nCVAR (" + (min ? "min" : "max") + ")");
			mainLog.println(" ran " + iters + " iterations and " + timer / 1000.0 + " seconds.");
			mainLog.println("Max time for 1 iteration :"+max_iteration_timer/1000.0+"s");
		}

		// Assumption: the original MDP model has only one initial state.
		StateRewardsArray mcRewards = new StateRewardsArray(cvar_mdp.getProductModel().getNumStates());


		int [] pol =operator.getStrategy(mdpRewards, mcRewards, choices, alpha);
		MDStrategyArray strat = new MDStrategyArray(cvar_mdp.productModel, pol);

		if (gen_trace) {
			MDPSimple mdpToSimulate = (MDPSimple) cvar_mdp.productModel;
			List<State> mdpStatesList = mdpToSimulate.getStatesList();
			int maxPathLen = 1000;
			int s = mdpToSimulate.getFirstInitialState();
			int idx_b;
			mainLog.println("Generating random trace");
//		mainLog.println(mdpStatesList.get(s));
			PrismFileLog trace_out = new PrismFileLog("prism/tests/traces/distr_cvar_" + settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD) + "_trace.csv");
			trace_out.println("s; actions; policy; b");
			int pathLen = 0;
			List<Object> available = new ArrayList<>();
			while (pathLen < maxPathLen && mdpToSimulate.getNumTransitions(s, strat.getChoiceIndex(s)) > 0) {
				available = mdpToSimulate.getAvailableActions(s);
				idx_b = cvar_mdp.getAutomatonState(s);

				trace_out.println(mdpStatesList.get(s).toString() + ";" + available.toString() + ";" + policy[s] + ";" + operator.getBVal(idx_b));
				Distribution distr = mdpToSimulate.getChoice(s, strat.getChoiceIndex(s));
				if (product_target.get(s)) {
					mainLog.println("Terminal state reached s: " + mdpStatesList.get(s).toString());
					break;
				}
				s = distr.sample();
				pathLen++;
			}
			trace_out.close();
		}

		// Compute distribution on induced DTMC
		mainLog.println("\n\nComputing distribution on induced DTMC...");

		DTMC dtmc = new DTMCFromMDPAndMDStrategy(cvar_mdp.productModel, strat);
		DTMCModelChecker mcDTMC = new DTMCModelChecker(this);

		int initialState = dtmc.getFirstInitialState();
		if (check_reach_dtmc_distr) {
			timer = System.currentTimeMillis();
			ModelCheckerResult dtmc_result = mcDTMC.computeReachRewardsDistr(dtmc, mcRewards, product_target, "prism/distr_dtmc_cvar.csv", dtmc_epsilon);
			timer = System.currentTimeMillis() - timer;
			if (verbosity >= 1) {
				mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
				mainLog.println(" : " + timer / 1000.0 + " seconds.");
			}

			double [] adjusted_dtmc_distr=operator.adjust_support(((TreeMap)dtmc_result.solnObj[initialState]));
			mainLog.println(adjusted_dtmc_distr);
			mainLog.println("Wasserstein p="+(settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD).equals(c51) ? "2" : "1")+" dtmc vs code distributions: "+operator.getW(adjusted_dtmc_distr, initialState));
		}
		if (check_prob_reach_dtmc){
			BitSet obs_states= cvar_mdp.getProductModel().getLabelStates(bad_states_label);
			timer = System.currentTimeMillis();
			ModelCheckerResult result_obs = mcDTMC.computeReachProbs(dtmc, obs_states);
			timer = System.currentTimeMillis() - timer;
			if (verbosity >= 1) {
				mainLog.print("\nChecking Probability of bad events :");
				mainLog.println(" ran " + iters + " iterations and " + timer / 1000.0 + " seconds.");
			}
			mainLog.println("Probs of reaching obstacle :" + result_obs.soln[initialState]);
		}

		int starting = 0;
		if (dtmc.getInitialStates().iterator().hasNext()) {
			if (verbosity >= 1){
				mainLog.println("# Initial states: ", dtmc.getNumInitialStates());
			}
			starting = cvar_mdp.getAutomatonState(cvar_mdp.getProductModel().getFirstInitialState());
		} else {
			if (verbosity >= 1){
				mainLog.println("Error no initial states: ", dtmc.getNumInitialStates());
			}
		}

		boolean print= false;
		if (print) {
			printToFile(policy, choices, action_val, alpha, b_atoms, operator.getB(), starting, "out_files/cvar_out_"+n+"_"+ settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD) +"_"+atoms+".out", n, mdp.getMaxNumChoices());
		}

		operator.writeToFile(initialState, null);
		// Store results
		//FIXME value im returning is policy not value
		ModelCheckerResult res = new ModelCheckerResult();
		res.soln = Arrays.stream(pol).asDoubleStream().toArray();
		res.numIters = iterations;
		res.timeTaken = (System.currentTimeMillis() - total_timer) / 1000.0;
		return res;
	}

	public String[] readParams(String filename)
	{
		if (filename == null){
			filename = "prism/tests/params_vi.csv";
		}
		String [] params = null;
		try {
			BasicReader r = new BasicReader.Wrapper(new FileReader(filename));
			CsvReader reader = new CsvReader(r, true, true, true, CsvReader.COMMA, BasicReader.LF);
			params = reader.nextRecord();

			r.close();
			reader.close();

		} catch (IOException | CsvFormatException e) {
			e.printStackTrace();
		}

		return params;
	}

	public void printToFile(Object [] policy, double [] value, String filename, int n)
	{
		mainLog.println("\nExporting solution to file \"" + filename + "\"...");
		PrismFileLog out = new PrismFileLog(filename);
		out.println("States");
		out.println(n);
		out.println("Policy");
		out.println(Arrays.toString(policy));
		out.println("Alpha value");
		out.println(0);

		DecimalFormat df = new DecimalFormat("0.000");
		Arrays.stream(value).forEach(e -> out.print(df.format(e) + ","));
		out.close();

	}
	public void printToFile(Object [] policy, double [][] action_cvar, double alpha, String filename, int n, int maxchoices)
	{
		mainLog.println("\nExporting solution to file \"" + filename + "\"...");
		PrismFileLog out = new PrismFileLog("prism/tests/"+filename);
		out.println("States");
		out.println(n);
		out.println("Policy");
		out.println(Arrays.toString(policy));

		out.println("Alpha value");
		out.println(alpha);

		out.println("Max number of actions");
		out.println(maxchoices);

		for (double[] doubles : action_cvar) // copy  temp value soln2 back to soln -> corresponds to Value table
		{
			DecimalFormat df = new DecimalFormat("0.000");
			Arrays.stream(doubles).forEach(e -> {
				if (e==Float.POSITIVE_INFINITY) {out.print("0.000,");}
			    else {out.print(df.format(e) + ",");}});
		}

		out.print("\n");
		out.close();
	}

	public void printToFile(Object [] policy, int [] choices, double [][] action_cvar, double alpha, int b_atoms, double[] b, int startB, String filename, int n, int maxchoices)
	{
		mainLog.println("\nExporting solution to file \"" + filename + "\"...");
		PrismFileLog out = new PrismFileLog("prism/tests/"+filename);
		out.println("This is new code !");
		out.println("States");
		out.println(n);
//		out.println("Policy");
//		out.println(Arrays.toString(policy));

		out.println("Alpha value");
		out.println(alpha);

		out.println("Max number of actions");
		out.println(maxchoices);

		for (int i=0; i<n; i++) // copy  temp value soln2 back to soln -> corresponds to Value table
		{
			double [] state_vals = action_cvar[i];
			DecimalFormat df = new DecimalFormat("0.000");
			DecimalFormat df_state = new DecimalFormat("0");
			int finalI = i;
			Arrays.stream(state_vals).forEach(e -> {
				if (e == Float.POSITIVE_INFINITY) {
					out.println(df_state.format(finalI)+ ", "+df.format(choices[finalI]) +", 0.000,");
				} else {
					out.println(df_state.format(finalI)+ ", "+df.format(choices[finalI]) + ", "+df.format(e) + ",");
				}
			});
		}

		out.println("# b");
		out.println(b_atoms);

		out.println("b vals");
		out.println(Arrays.toString(b));

		out.println("Optimal start b");
		out.println(startB);

		out.print("\n");
		out.close();
	}


	/**
	 * generates n logarithmically-spaced points between d1 and d2 using the
	 * provided base.
	 *
	 * @param atoms the number of supports
	 * @param numStates the number of MDP states
	 * @return p size of p -> states * actions * number of supports
	 */

	/**
	 * Construct strategy information for min/max expected reachability.
	 * (More precisely, list of indices of choices resulting in min/max.)
	 * (Note: indices are guaranteed to be sorted in ascending order.)
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param state The state to generate strategy info for
	 * @param target The set of target states to reach
	 * @param min Min or max rewards (true=min, false=max)
	 * @param lastSoln Vector of values from which to recompute in one iteration 
	 */
	public List<Integer> expReachStrategy(MDP mdp, MDPRewards mdpRewards, int state, BitSet target, boolean min, double lastSoln[]) throws PrismException
	{
		double val = mdp.mvMultRewMinMaxSingle(state, lastSoln, mdpRewards, min, null);
		return mdp.mvMultRewMinMaxSingleChoices(state, lastSoln, mdpRewards, min, val);
	}

	/**
	 * Restrict a (memoryless) strategy for an MDP, stored as an integer array of choice indices,
	 * to the states of the MDP that are reachable under that strategy.  
	 * @param mdp The MDP
	 * @param strat The strategy
	 */
	public void restrictStrategyToReachableStates(MDP mdp, int strat[])
	{
		BitSet restrict = new BitSet();
		BitSet explore = new BitSet();
		// Get initial states
		for (int is : mdp.getInitialStates()) {
			restrict.set(is);
			explore.set(is);
		}
		// Compute reachable states (store in 'restrict') 
		boolean foundMore = true;
		while (foundMore) {
			foundMore = false;
			for (int s = explore.nextSetBit(0); s >= 0; s = explore.nextSetBit(s + 1)) {
				explore.set(s, false);
				if (strat[s] >= 0) {
					Iterator<Map.Entry<Integer, Double>> iter = mdp.getTransitionsIterator(s, strat[s]);
					while (iter.hasNext()) {
						Map.Entry<Integer, Double> e = iter.next();
						int dest = e.getKey();
						if (!restrict.get(dest)) {
							foundMore = true;
							restrict.set(dest);
							explore.set(dest);
						}
					}
				}
			}
		}
		// Set strategy choice for non-reachable state to -1
		int n = mdp.getNumStates();
		for (int s = restrict.nextClearBit(0); s < n; s = restrict.nextClearBit(s + 1)) {
			strat[s] = -3;
		}
	}

	/**
	 * Compute the end component quotient (for use with PMax),
	 * each maximal end component is collapsed to a single state,
	 * likewise the yes and no regions, respectively.
	 */
	private MDPEquiv maxQuotient(MDP mdp, BitSet yes, BitSet no) throws PrismException
	{
		BitSet maybe = new BitSet();
		maybe.set(0, mdp.getNumStates());
		maybe.andNot(yes);
		maybe.andNot(no);

		ECComputer ec = ECComputer.createECComputer(this, mdp);

		ec.computeMECStates(maybe);
		List<BitSet> mecs = ec.getMECStates();
		mecs.add(yes);
		mecs.add(no);

		EquivalenceRelationInteger eq = new EquivalenceRelationInteger(mecs);
		BasicModelTransformation<MDP, MDPEquiv> quotientTransform = MDPEquiv.transformDroppingLoops(mdp, eq);
		MDPEquiv quotient = quotientTransform.getTransformedModel();

		//mdp.exportToDotFile("original.dot");
		//quotient.exportToDotFile("maxQuotient.dot");

		int realStates = quotient.getNumStates() - quotient.getNonRepresentativeStates().cardinality();
		mainLog.println("Max-Quotient MDP: " + realStates + " equivalence classes / non-trap states.");

		return quotient;
	}


	/**
	 * Simple test program.
	 */
	public static void main(String args[])
	{
		MDPModelChecker mc;
		MDPSimple mdp;
		ModelCheckerResult res;
		BitSet init, target;
		Map<String, BitSet> labels;
		boolean min = true;
		try {
			mc = new MDPModelChecker(null);
			mdp = new MDPSimple();
			mdp.buildFromPrismExplicit(args[0]);
			mdp.addInitialState(0);
			//System.out.println(mdp);
			labels = StateModelChecker.loadLabelsFile(args[1]);
			//System.out.println(labels);
			init = labels.get("init");
			target = labels.get(args[2]);
			if (target == null)
				throw new PrismException("Unknown label \"" + args[2] + "\"");
			for (int i = 3; i < args.length; i++) {
				if (args[i].equals("-min"))
					min = true;
				else if (args[i].equals("-max"))
					min = false;
				else if (args[i].equals("-nopre"))
					mc.setPrecomp(false);
			}
			res = mc.computeReachProbs(mdp, target, min);
			System.out.println(res.soln[init.nextSetBit(0)]);
		} catch (PrismException e) {
			System.out.println(e);
		}
	}

}
