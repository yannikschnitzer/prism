//==============================================================================
//	
//	Copyright (c) 2014-
//	Authors:
//	* Xueyi Zou <xz972@york.ac.uk> (University of York)
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham)
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
import java.util.Random;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;

import acceptance.AcceptanceReach;
import automata.DA;

import java.util.Iterator;
import cern.colt.Arrays;
import common.StopWatch;
import explicit.graphviz.Decoration;
import explicit.graphviz.Decorator;
import explicit.rewards.MDPRewards;
import explicit.rewards.Rewards;
import explicit.rewards.StateRewardsSimple;
import explicit.rewards.WeightedSumMDPRewards;
import parser.State;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationIntUnbounded;
import parser.ast.Expression;
import prism.Accuracy;
import prism.AccuracyFactory;
import prism.Pair;
import prism.Prism;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismNotSupportedException;
import prism.PrismUtils;
//mport solver.BeliefPoint;
//import program.POMDP;
//import solver.BeliefPoint;
//import solver.AlphaVector;
//import solver.BeliefPoint;
//import solver.OutputFileWriter;
//import solver.BeliefPoint;
//import solver.ProbabilitySample;
import explicit.AlphaVector;
import explicit.AlphaMatrix;
import explicit.PartiallyObservableMonteCarloPlanning;
//import solver.BeliefPoint;

/**
 * Explicit-state model checker for partially observable Markov decision processes (POMDPs).
 */
public class POMDPModelChecker extends ProbModelChecker
{
	// Some local data structures for convenience
	
	/**
	 * Info for a single state of a belief MDP:
	 * (1) a list (over choices in the state) of distributions over beliefs, stored as hashmap;
	 * (2) optionally, a list (over choices in the state) of rewards
	 */
	class BeliefMDPState
	{
		public List<HashMap<Belief, Double>> trans;
		public List<Double> rewards;
		public BeliefMDPState()
		{
			trans = new ArrayList<>();
			rewards = new ArrayList<>();
		}
	}
	
	/**
	 * Value backup function for belief state value iteration:
	 * mapping from a state and its definition (reward + transitions)
	 * to a pair of the optimal value + choice index. 
	 */
	@FunctionalInterface
	interface BeliefMDPBackUp extends BiFunction<Belief, BeliefMDPState, Pair<Double, Integer>> {}
	
	/**
	 * A model constructed to represent a fragment of a belief MDP induced by a strategy:
	 * (1) the model (represented as an MDP for ease of storing actions labels)
	 * (2) the indices of the choices made by the strategy in states of the original POMDP
	 * (3) a list of the beliefs corresponding to each state of the model
	 */
	class POMDPStrategyModel
	{
		public MDP mdp;
		public List<Integer> strat;
		public List<Belief> beliefs;
	}
	
	/**
	 * Create a new POMDPModelChecker, inherit basic state from parent (unless null).
	 */
	public POMDPModelChecker(PrismComponent parent) throws PrismException
	{
		super(parent);
	}

	// Model checking functions
	
	protected StateValues checkProbPathFormulaCosafeLTL(Model model, Expression expr, boolean qual, MinMax minMax, BitSet statesOfInterest) throws PrismException
	{
		// For LTL model checking routines
		LTLModelChecker mcLtl = new LTLModelChecker(this);

		// Model check maximal state formulas and construct DFA
		Vector<BitSet> labelBS = new Vector<BitSet>();
		DA<BitSet, AcceptanceReach> da = mcLtl.constructDFAForCosafetyProbLTL(this, model, expr, labelBS);

		
		
		
		StopWatch timer = new StopWatch(getLog());
		mainLog.println("\nConstructing " + model.getModelType() + "-" + da.getAutomataType() + " product...");
		timer.start(model.getModelType() + "-" + da.getAutomataType() + " product");
		LTLModelChecker.LTLProduct<POMDP> product = mcLtl.constructProductModel(da, (POMDP)model, labelBS, statesOfInterest);
		timer.stop("product has " + product.getProductModel().infoString());

		// Output product, if required
		if (getExportProductTrans()) {
				mainLog.println("\nExporting product transition matrix to file \"" + getExportProductTransFilename() + "\"...");
				product.getProductModel().exportToPrismExplicitTra(getExportProductTransFilename());
		}
		if (getExportProductStates()) {
			mainLog.println("\nExporting product state space to file \"" + getExportProductStatesFilename() + "\"...");
			PrismFileLog out = new PrismFileLog(getExportProductStatesFilename());
			VarList newVarList = (VarList) modulesFile.createVarList().clone();
			String daVar = "_da";
			while (newVarList.getIndex(daVar) != -1) {
				daVar = "_" + daVar;
			}
			newVarList.addVar(0, new Declaration(daVar, new DeclarationIntUnbounded()), 1, null);
			product.getProductModel().exportStates(Prism.EXPORT_PLAIN, newVarList, out);
			out.close();
		}
		// Find accepting states + compute reachability rewards
		BitSet acc = ((AcceptanceReach)product.getAcceptance()).getGoalStates();
		BitSet productStatesOfInterest = product.liftFromModel(statesOfInterest);
		mainLog.println("\nComputing reachability probabilities...");
		POMDPModelChecker mcProduct = new POMDPModelChecker(this);
		mcProduct.inheritSettings(this);
		product.getProductModel().exportToDotFile(new PrismFileLog("prod.dot"), acc, true);
		ModelCheckerResult res = mcProduct.computeReachProbs((POMDP)product.getProductModel(), null, acc, minMax.isMin(), productStatesOfInterest);
		StateValues probsProduct = StateValues.createFromDoubleArrayResult(res, product.getProductModel());

		// Output vector over product, if required
		if (getExportProductVector()) {
				mainLog.println("\nExporting product solution vector matrix to file \"" + getExportProductVectorFilename() + "\"...");
				PrismFileLog out = new PrismFileLog(getExportProductVectorFilename());
				probsProduct.print(out, false, false, false, false);
				out.close();
		}

		// Mapping probabilities in the original model
		StateValues probs = product.projectToOriginalModel(probsProduct);
		probsProduct.clear();

		return probs;
	}
	
	/**
	 * Compute rewards for a co-safe LTL reward operator.
	 */
	protected StateValues checkRewardCoSafeLTL(Model model, Rewards modelRewards, Expression expr, MinMax minMax, BitSet statesOfInterest) throws PrismException
	{
		// Build product of POMDP and DFA for the LTL formula, convert rewards and do any required exports
		LTLModelChecker mcLtl = new LTLModelChecker(this);
		LTLModelChecker.LTLProduct<POMDP> product = mcLtl.constructDFAProductForCosafetyReward(this, (POMDP) model, expr, statesOfInterest);
		
		product.getProductModel().exportToDotFile(new PrismFileLog("E:\\Downloads\\prism3\\prism812\\prism\\prism\\tests\\Shield\\POMDP4_product.dot"), ((AcceptanceReach)product.getAcceptance()).getGoalStates(), true);

//		ArrayList<explicit.graphviz.Decorator> decoratorsProduct = new ArrayList<explicit.graphviz.Decorator>();
//		decoratorsProduct.add(new explicit.graphviz.ShowStatesDecorator(((POMDP) product.getProductModel()).getStatesList(), ((PartiallyObservableModel) this)::getObservationAsState));
//		((POMDP) product.getProductModel()).exportToDotFile("E:\\Downloads\\prism3\\prism812\\prism\\prism\\tests\\Shield\\POMDP4_product.dot", decoratorsProduct);

		MDPRewards productRewards = ((MDPRewards) modelRewards).liftFromModel(product);

		doProductExports(product);

		// Find accepting states + compute reachability rewards
		BitSet acc = ((AcceptanceReach)product.getAcceptance()).getGoalStates();

		mainLog.println("\nCosafe: Computing reachability rewards...");
		POMDPModelChecker mcProduct = new POMDPModelChecker(this);
		mcProduct.inheritSettings(this);
		ModelCheckerResult res = mcProduct.computeReachRewards((POMDP)product.getProductModel(), productRewards, acc, minMax.isMin(), null);
		StateValues rewardsProduct = StateValues.createFromDoubleArrayResult(res, product.getProductModel());

		// Output vector over product, if required
		if (getExportProductVector()) {
				mainLog.println("\nExporting product solution vector matrix to file \"" + getExportProductVectorFilename() + "\"...");
				PrismFileLog out = new PrismFileLog(getExportProductVectorFilename());
				rewardsProduct.print(out, false, false, false, false);
				out.close();
		}

		// Mapping rewards in the original model
		StateValues rewards = product.projectToOriginalModel(rewardsProduct);
		rewardsProduct.clear();

		return rewards;
	}
	
	// Numerical computation functions

	/**
	 * Compute reachability/until probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target},
	 * while remaining in those in @{code remain}.
	 * @param pomdp The POMDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param min Min or max probabilities (true=min, false=max)
	 */
	public ModelCheckerResult computeReachProbs(POMDP pomdp, BitSet remain, BitSet target, boolean min, BitSet statesOfInterest) throws PrismException
	{
		ModelCheckerResult res = null;
		long timer;

		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}
		
		// Start probabilistic reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting probabilistic reachability (" + (min ? "min" : "max") + ")...");

		// Compute rewards
		res = computeReachProbsFixedGrid(pomdp, remain, target, min, statesOfInterest.nextSetBit(0));

		// Finished probabilistic reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.println("Probabilistic reachability took " + timer / 1000.0 + " seconds.");

		// Update time taken
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute reachability/until probabilities,
	 * i.e. compute the min/max probability of reaching a state in {@code target},
	 * while remaining in those in @{code remain},
	 * using Lovejoy's fixed-resolution grid approach.
	 * This only computes the probabiity from a single start state
	 * @param pomdp The POMDP
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 * @param sInit State to compute for
	 */
	protected ModelCheckerResult computeReachProbsFixedGrid(POMDP pomdp, BitSet remain, BitSet target, boolean min, int sInit) throws PrismException
	{
		// Start fixed-resolution grid approximation
		mainLog.println("calling computeReachProbsFixedGrid!!!");
		long timer = System.currentTimeMillis();
		mainLog.println("Starting fixed-resolution grid approximation (" + (min ? "min" : "max") + ")...");

		// Find out the observations for the target/remain states
		BitSet targetObs = getObservationsMatchingStates(pomdp, target);;
		if (targetObs == null) {
			throw new PrismException("Target for reachability is not observable");
		}
		BitSet remainObs = (remain == null) ? null : getObservationsMatchingStates(pomdp, remain);
		if (remain != null && remainObs == null) {
			throw new PrismException("Left-hand side of until is not observable");
		}
		mainLog.println("target obs=" + targetObs.cardinality() + (remainObs == null ? "" : ", remain obs=" + remainObs.cardinality()));
		
		// Determine set of observations actually need to perform computation for
		BitSet unknownObs = new BitSet();
		unknownObs.set(0, pomdp.getNumObservations());
		unknownObs.andNot(targetObs);
		if (remainObs != null) {
			unknownObs.and(remainObs);
		}

		// Initialise the grid points (just for unknown beliefs)
		List<Belief> gridPoints = initialiseGridPoints(pomdp, unknownObs);
		mainLog.println("Grid statistics: resolution=" + gridResolution + ", points=" + gridPoints.size());
		// Construct grid belief "MDP"
		mainLog.println("Building belief space approximation...");
		List<BeliefMDPState> beliefMDP = buildBeliefMDP(pomdp, null, gridPoints);
		
		// Initialise hashmaps for storing values for the unknown belief states
		HashMap<Belief, Double> vhash = new HashMap<>();
		HashMap<Belief, Double> vhash_backUp = new HashMap<>();
		for (Belief belief : gridPoints) {
			vhash.put(belief, 0.0);
			vhash_backUp.put(belief, 0.0);
		}
		// Define value function for the full set of belief states
		Function<Belief, Double> values = belief -> approximateReachProb(belief, vhash_backUp, targetObs, unknownObs);
		// Define value backup function
		BeliefMDPBackUp backup = (belief, beliefState) -> approximateReachProbBackup(belief, beliefState, values, min);
		
		// Start iterations
		mainLog.println("Solving belief space approximation...");
		long timer2 = System.currentTimeMillis();
		int iters = 0;
		boolean done = false;
		while (!done && iters < maxIters) {
			// Iterate over all (unknown) grid points
			int unK = gridPoints.size();
			for (int b = 0; b < unK; b++) {
				Belief belief = gridPoints.get(b);
				Pair<Double, Integer> valChoice = backup.apply(belief, beliefMDP.get(b));
				vhash.put(belief, valChoice.first);
			}
			// Check termination
			done = PrismUtils.doublesAreClose(vhash, vhash_backUp, termCritParam, termCrit == TermCrit.RELATIVE);
			// back up	
			Set<Map.Entry<Belief, Double>> entries = vhash.entrySet();
			for (Map.Entry<Belief, Double> entry : entries) {
				vhash_backUp.put(entry.getKey(), entry.getValue());
			}
			iters++;
		}
		// Non-convergence is an error (usually)
		if (!done && errorOnNonConverge) {
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}
		timer2 = System.currentTimeMillis() - timer2;
		mainLog.print("Belief space value iteration (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + iters + " iterations and " + timer2 / 1000.0 + " seconds.");
		
		// Extract (approximate) solution value for the initial belief
		// Also get (approximate) accuracy of result from value iteration
		Belief initialBelief = Belief.pointDistribution(sInit, pomdp);
		double outerBound = values.apply(initialBelief);
		double outerBoundMaxDiff = PrismUtils.measureSupNorm(vhash, vhash_backUp, termCrit == TermCrit.RELATIVE);
		Accuracy outerBoundAcc = AccuracyFactory.valueIteration(termCritParam, outerBoundMaxDiff, termCrit == TermCrit.RELATIVE);
		// Print result
		mainLog.println("Outer bound: " + outerBound + " (" + outerBoundAcc.toString(outerBound) + ")");
		
		// Build DTMC to get inner bound (and strategy)
		mainLog.println("\nBuilding strategy-induced model...");
		POMDPStrategyModel psm = buildStrategyModel(pomdp, sInit, null, targetObs, unknownObs, backup);
		MDP mdp = psm.mdp;
		mainLog.print("Strategy-induced model: " + mdp.infoString());
		
		// Export strategy if requested
		// NB: proper storage of strategy for genStrat not yet supported,
		// so just treat it as if -exportadv had been used, with default file (adv.tra)
		if (genStrat || exportAdv) {
			// Export in Dot format if filename extension is .dot
			if (exportAdvFilename.endsWith(".dot")) {
				mdp.exportToDotFile(exportAdvFilename, Collections.singleton(new Decorator()
				{
					@Override
					public Decoration decorateState(int state, Decoration d)
					{
						d.labelAddBelow(psm.beliefs.get(state).toString(pomdp));
						return d;
					}
				}));
			}
			// Otherwise use .tra format
			else {
				mdp.exportToPrismExplicitTra(exportAdvFilename);
			}
		}
		// Create MDP model checker (disable strat generation - if enabled, we want the POMDP one) 
		MDPModelChecker mcMDP = new MDPModelChecker(this);
		mcMDP.setExportAdv(false);
		mcMDP.setGenStrat(false);
		// Solve MDP to get inner bound
		// (just reachability: can ignore "remain" since violating states are absent)
		ModelCheckerResult mcRes = mcMDP.computeReachProbs(mdp, mdp.getLabelStates("target"), true);
		double innerBound = mcRes.soln[0];
		Accuracy innerBoundAcc = mcRes.accuracy;
		// Print result
		String innerBoundStr = "" + innerBound;
		if (innerBoundAcc != null) {
			innerBoundStr += " (" + innerBoundAcc.toString(innerBound) + ")";
		}
		mainLog.println("Inner bound: " + innerBoundStr);

		// Finished fixed-resolution grid approximation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("\nFixed-resolution grid approximation (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + timer / 1000.0 + " seconds.");

		// Extract and store result
		Pair<Double,Accuracy> resultValAndAcc;
		if (min) {
			resultValAndAcc = AccuracyFactory.valueAndAccuracyFromInterval(outerBound, outerBoundAcc, innerBound, innerBoundAcc);
		} else {
			resultValAndAcc = AccuracyFactory.valueAndAccuracyFromInterval(innerBound, innerBoundAcc, outerBound, outerBoundAcc);
		}
		double resultVal = resultValAndAcc.first;
		Accuracy resultAcc = resultValAndAcc.second;
		mainLog.println("Result bounds: [" + resultAcc.getResultLowerBound(resultVal) + "," + resultAcc.getResultUpperBound(resultVal) + "]");
		double soln[] = new double[pomdp.getNumStates()];
		soln[sInit] = resultVal;

		// Return results
		ModelCheckerResult res = new ModelCheckerResult();
		res.soln = soln;
		res.accuracy = resultAcc;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		return res;
	}
	public void generateRewardFunction() {
		mainLog.println("rewards \"states\" ");
		for (int v=0; v<13; v++) {
			for (int h=0; h<3;h++) {
				mainLog.println("v= "+v+ " & h = "+h+" :"+ (h+v*10+10000)+";");
			}
		}
		mainLog.println("endrewards");
	}
	public void generateTonyPOMDP(POMDP pomdp, ArrayList<Integer> endStates, MDPRewards mdpRewards, double minMax) {
		int nStates = pomdp.getNumStates();
		ArrayList<Object> allActions = getAllActions(pomdp);
		int nActions = allActions.size();
		
		mainLog.println("generate tony's file---------------");
		mainLog.println("discount: 0.99 \nvalues: reward\nstates: "+nStates);
		mainLog.print("actions: ");
		for (int i=0; i<nActions; i++) {
			mainLog.print(allActions.get(i)+" ");
		}
		mainLog.println("\nobservations: "+pomdp.getNumObservations()+"\nstart:");
		mainLog.println(Arrays.toString(pomdp.getInitialBeliefInDist()).replace("[", "").replace("]", "").replace(",", " "));
		int endState=endStates.get(0);
		for (int i=0; i<nActions; i++) {
			Object action = allActions.get(i);
			mainLog.println("T: "+action);
			for (int s=0; s<nStates; s++) {
				if(!endStates.contains(s)) {
					for (int sPrime=0; sPrime<nStates; sPrime++) {
						List<Object> availableActions= pomdp.getAvailableActions(s);
						double tranP=0.0;
						if (availableActions.contains(action)) {
							int choice = pomdp.getChoiceByAction(s, action);
							Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(s,choice);
							while (iter.hasNext()) {
								Map.Entry<Integer, Double> trans = iter.next();
								if (trans.getKey()==sPrime) {
									tranP = trans.getValue();	 
								}
							}
						}
						else {
							//if(sPrime==s) {	tranP=1.0;}
							if(sPrime==endState) {tranP=1.0;}
						}
						mainLog.print(String.format("%.20f", tranP)+" ");
					}
				}
				else {
					for (int sPrime=0; sPrime<nStates; sPrime++) {
						double tranP=0.0;
						//if(sPrime==s) {	tranP=1.0;}
						if(sPrime==endState) {tranP=1.0;}
						mainLog.print(String.format("%.20f", tranP)+" ");
					}
				}
				mainLog.println("");
			}
		}
		mainLog.println("\nO: *");
		for (int s=0; s<nStates; s++) {
			for (int o=0; o<pomdp.getNumObservations();o++) {
				double obsP=0.0;
				if(pomdp.getObservation(s)==o) {
					obsP=1.0;
				}
				mainLog.print(obsP+" ");
			}
			mainLog.println("");
		}
		mainLog.println("");
		//R: <action> : <start-state> : <end-state> : <observation> %f
		for (int i=0; i<nActions;i++) {
			for (int s=0; s<nStates; s++) {
				if (endStates.contains(s)) {
					continue;
				}
				Object action = allActions.get(i);
				if (pomdp.getAvailableActions(s).contains(action)) {							
					int choice =  pomdp.getChoiceByAction(s, action);
					double r =  mdpRewards.getTransitionReward(s, choice)+mdpRewards.getStateReward(s) ;
					mainLog.println("R: "+action+": "+s+": "+"* : "+"*  "+r*minMax);
				}
			}
		}
		//R: <action> : <start-state> : <end-state> : <observation> %f
		for (int i=0; i<nActions;i++) {
			for (int s=0; s<nStates; s++) {
				if (endStates.contains(s)) {
					continue;
				}
				Object action = allActions.get(i);
				if (!pomdp.getAvailableActions(s).contains(action)) {							
					mainLog.println("R: "+action+": "+s+": "+"* : "+"*  "+(-100));
				}
			}
		}
		mainLog.println("generate tony's file---------------");
	}
	
	public ModelCheckerResult computeReachRewardsPerseus(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest) throws PrismException
	{ 
		double minMax = min? -1: 1; //negate reward for min problems
		int stageLimit = 30000;
		
		BitSet targetObs = getObservationsMatchingStates(pomdp, target);
		
		for(int i=0; i<pomdp.getNumStates();i++)
			mainLog.println("s = "+i+" obs = "+pomdp.getObservation(i));

		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}	
		
		if (targetObs == null) {
			//throw new PrismException("Target for expected reachability is not observable");
			mainLog.println("Target for expected reachability is not observable");
		}
		// Find _some_ of the states with infinite reward
		// (those from which *every* MDP strategy has prob<1 of reaching the target,
		// and therefore so does every POMDP strategy)
		MDPModelChecker mcProb1 = new MDPModelChecker(this);
		BitSet inf = mcProb1.prob1(pomdp, null, target, false, null);
		mainLog.println("before flipp");
		mainLog.println(inf);
		inf.flip(0, pomdp.getNumStates());
		mainLog.println("after flipp");
		
		mainLog.println(inf);
		// Find observations for which all states are known to have inf reward
		BitSet infObs = getObservationsCoveredByStates(pomdp, inf);
		mainLog.println("infObs"+infObs);

		
		//mainLog.println("target obs=" + targetObs.cardinality() + ", inf obs=" + infObs.cardinality());
		
		// Determine set of observations actually need to perform computation for
		// eg. if obs=1 & unknownObs(obs)=true -> obs=1 needs computation
		// eg. if obs=2 & unknownObs(obs)=false -> obs=1 does not need computation
		BitSet unknownObs = new BitSet();
		unknownObs.set(0, pomdp.getNumObservations());
		if (targetObs!=null) {
			unknownObs.andNot(targetObs);	
		}
		unknownObs.andNot(infObs);

		int nStates = pomdp.getNumStates();
		ArrayList<Object> allActions = getAllActions(pomdp);
		int nActions = allActions.size();
		ModelCheckerResult res = null;
		long timer;
		
		ArrayList<Integer> endStates = new ArrayList<Integer>();
		for (int i=0; i<nStates;i++) {
			if (!unknownObs.get(pomdp.getObservation(i))) {
				//mainLog.println("end state="+i+"Obs="+pomdp.getObservation(i));
				endStates.add(i);
			}
		}
		BitSet observations_of_target = new BitSet();
		for (int i =target.nextSetBit(0); i>=0; i= target.nextSetBit(i+1)) {
			mainLog.println("target"+target.nextSetBit(i)+"obs"+(pomdp.getObservation(target.nextSetBit(i))) );
			observations_of_target.set(pomdp.getObservation(target.nextSetBit(i)));
		}
		
		mainLog.println("target"+target);
		mainLog.println("targetObs ="+targetObs);
		mainLog.println("inf"+inf);
		mainLog.println("infObs"+infObs);
		mainLog.println("unknownObs ="+unknownObs);
		mainLog.println("observations_of_target ="+observations_of_target);
		//mainLog.println("get number of getNumObservations"+pomdp.getNumObservations());
		//mainLog.println("get number of getNumUnobservations"+pomdp.getNumUnobservations());
		
		//generate tony's file
		boolean generateTony = false;
		if (generateTony) {
			generateTonyPOMDP(pomdp, endStates,   mdpRewards, minMax);
			//generateRewardFunction();
		}

		// Start expected reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");
		mainLog.println("=== RUN POMDP SOLVER ===");
		mainLog.println("Algorithm: Perseus (point-based value iteration)");
		mainLog.println("Belief sampling started...");
		// Compute rewards
		mainLog.println("NumeStates/nActions"+nStates+nActions);
		 ArrayList<Belief> B  = randomExploreBeliefs(pomdp, target, statesOfInterest);
		mainLog.println("Defining immediate rewards ");
						
		// create initial vector set and vectors defining immediate rewards
		ArrayList<AlphaVector> V = new ArrayList<AlphaVector>();
		ArrayList<AlphaVector> immediateRewards = new ArrayList<AlphaVector>();
		
		mainLog.println("state "+"action "+"Obs "+"tranReward "+"stagereward ");

		// compute Rmin
		double Rmin = Double.POSITIVE_INFINITY;
		for (int a=0; a<nActions; a++) {
			Object action = allActions.get(a);
			for (int s=0; s<nStates; s++) {
				if (pomdp.getAvailableActions(s).contains(action)) {
					int choice =  pomdp.getChoiceByAction(s, action);
					double immediateReward = minMax*( mdpRewards.getTransitionReward(s, choice) + mdpRewards.getStateReward(s) );
					double stateReward = mdpRewards.getStateReward(s);
					int h = ((int) stateReward) % 10;
					int v = ((int) stateReward -10000) /10 ;
					int obs = pomdp.getObservation(s);
					//mainLog.println("s= "+s +"\to="+obs +"\tv ="+v+"\th="+h+  " \tc = "+choice+" \tr = "+mdpRewards.getTransitionReward(s, choice)+"\tr ="+ mdpRewards.getStateReward(s)+"\ti"+immediateReward);
					//mainLog.println(s+"\t"+obs+"\t"+v+"\t"+h+"\t"+stateReward);
					if (immediateReward<Rmin) {
						Rmin = immediateReward;
						//mainLog.println("Rmin"+Rmin);
					}
				}
			}
		}
		
		for(int a=0; a<nActions; a++) {
			double[] entries = new double[nStates];
			for(int s=0; s<nStates; s++) {
				//entries[s] = pomdp.getReward(s, a); original
				Object action = allActions.get(a);
				entries[s] = 0;
				if (pomdp.getAvailableActions(s).contains(action)) {							
					int choice =  pomdp.getChoiceByAction(s, action);
					//mainLog.println(" "+s+" "+action+" "+pomdp.getObservation(s)+" "+mdpRewards.getTransitionReward(s, choice)+" "+mdpRewards.getStateReward(s) );
					double immediateR = minMax*(mdpRewards.getTransitionReward(s, choice)+mdpRewards.getStateReward(s));
					entries[s] +=  immediateR ;
					if (immediateR<Rmin) {
						Rmin = immediateR;
						//mainLog.println("Rmin"+Rmin);

					}
					
					if(pomdp.allSuccessorsInSet(s, choice, inf)) {
						//mainLog.println("All successor states of "+s+" by action "+action+" is in inf"+entries[s] );
						//entries[s] = -5;
						//entries[s]= 10* Rmin;
					}
					
					
				}
				else {
					if (unknownObs.get(pomdp.getObservation(s)) & min) {
						entries[s]= 10* Rmin;
					}
					if((!inf.get(s))& min) {
						entries[s]= 10* Rmin;
						//mainLog.println("s=="+s+"a=="+a);
					}
				}
				
				/* updated here , comment out
				if (endStates.contains(s)) {
					mainLog.println("s"+s);
					entries[s]=0;
				}
				
	*/
				if(observations_of_target.get(pomdp.getObservation(s))) {
					entries[s]= 0;
				}
				else {
					if(inf.get(s)) {
						if (min || !min) {
							//entries[s] = -10000;	
							entries[s]= 10* Rmin;
						}
						else {
							entries[s] = 0;

						}
					}
				}
			
			}
			AlphaVector av = new AlphaVector(entries);
			av.setAction(a);
			immediateRewards.add(av);
		}
		for (int v=0; v<immediateRewards.size();v++) {
			mainLog.print(v+" immediate ActionIndex = "+ immediateRewards.get(v).getAction()+" ActionName=" + allActions.get(immediateRewards.get(v).getAction()) );
			mainLog.println( "  value = "+ Arrays.toString(immediateRewards.get(v).getEntries()));
			for (int s=0; s<nStates; s++) {
				double r = immediateRewards.get(v).getEntry(s);
				if(Rmin > r) {
					Rmin = r;
				}
			}
		}
		
//		//print states
//		for (int a=0; a<nActions; a++) {
//			Object action = allActions.get(a);
//			for (int s=0; s<nStates; s++) {
//				if (pomdp.getAvailableActions(s).contains(action)) {
//					int choice =  pomdp.getChoiceByAction(s, action);
//					double immediateReward = minMax*( mdpRewards.getTransitionReward(s, choice) + mdpRewards.getStateReward(s) );
//					double stateReward = mdpRewards.getStateReward(s);
//					int h = ((int) stateReward) % 10;
//					int v = ((int) stateReward -10000) /10 ;
//					int obs = pomdp.getObservation(s);
//					//mainLog.println("s= "+s +"\to="+obs +"\tv ="+v+"\th="+h+  " \tc = "+choice+" \tr = "+mdpRewards.getTransitionReward(s, choice)+"\tr ="+ mdpRewards.getStateReward(s)+"\ti"+immediateReward);
//					mainLog.println(s+"\t"+obs+"\t"+v+"\t"+h+"\t"+stateReward+"\t"+inf.get(s)+"\t"+immediateRewards.get(0).getEntry(s)+"\t"+immediateRewards.get(1).getEntry(s)+"\t"+immediateRewards.get(2).getEntry(s));
//					if (immediateReward<Rmin) {
//						Rmin = immediateReward;
//					}
//				}
//			}
//		}
		
		//initial vector 
		mainLog.println("Rmin" + Rmin);
		double[] entries = new double[nStates];
		for(int s = 0; s < nStates; s++) {
			entries[s] = Rmin * 10 * 100; // assume the gamma=0.99
		}
		AlphaVector av = new AlphaVector(entries);
		av.setAction(0);
		V.add(av);
		
		//mainLog.println("vsize" + allActions.get(0));
		
		for (int v = 0; v < V.size(); v++) {
			mainLog.print(v+" V ActionIndex = "+ V.get(v).getAction()+ " ActionName="+ allActions.get(V.get(v).getAction()));
			mainLog.println( "   value = "+ Arrays.toString(V.get(v).getEntries()));
		}
		
		int stage = 0;
		System.out.println("Stage "+stage +": "+V.size()+" vectors");
		for (int v = 0; v < V.size(); v++) {
			mainLog.print(v+" V Action = "+ V.get(v).getAction());
			mainLog.println( "   value = "+ Arrays.toString(V.get(v).getEntries()));
		}
		
		long startTime = System.currentTimeMillis();
		double ValueFunctionTolerance = 1E-05;
		mainLog.println("Error Torleance = " + ValueFunctionTolerance);
		ArrayList<Double> differences =  new ArrayList<Double>();
		differences.add(Double.POSITIVE_INFINITY);
		differences.add(Double.POSITIVE_INFINITY);
		while(true) {
			double elapsed = (System.currentTimeMillis() - startTime) * 0.001;
			double TimeLimit = 7200; //1000 seconds
			if(stage>stageLimit) {
				mainLog.println("reach stage limit at !"+stageLimit);
				break;
			}
			if (elapsed>TimeLimit) {
				mainLog.println("reach time limit "+TimeLimit+" seconds");
				break;
			}
			stage++;
			
			ArrayList<AlphaVector> Vnext = backupStage(pomdp, immediateRewards, V, B, unknownObs,min, stage, target.nextSetBit(0), inf);
			double valueDifference = Double.NEGATIVE_INFINITY;
			for(Belief bel : B) {
				double diff = AlphaVector.getValue(bel.toDistributionOverStates(pomdp), Vnext) - AlphaVector.getValue(bel.toDistributionOverStates(pomdp), V);
				if(diff > valueDifference) valueDifference = diff;
			}
			differences.add(valueDifference);
			if( checkConverge(differences, ValueFunctionTolerance ) ) {
				mainLog.println("converge with tolerance"+ValueFunctionTolerance);
				mainLog.println("diff:"+differences.toString());
				break;
			}
			
			V = Vnext;
			mainLog.println("Stage= "+stage+": "+Vnext.size()+" vectors, diff "+valueDifference+" value "+Math.abs( AlphaVector.getValue(pomdp.getInitialBeliefInDist(), V))+", time elapsed "+elapsed+" sec");
			

			for (int v=0; v<V.size();v++) {
				//mainLog.println(v+" V Action = "+ "   value = "+ Arrays.toString(V.get(v).getEntries()) +"a="+V.get(v).getAction(),0);
			}
			//mainLog.println("+++",1);
			
		}
		double expectedValue = Math.abs( AlphaVector.getValue(pomdp.getInitialBeliefInDist(), V));
		
		//String outputFileAlpha = sp.getOutputDir()+"/"+pomdp.getInstanceName()+".alpha";
		
		// Finished expected reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.println("*********Value" +expectedValue );
		mainLog.println("Expected reachability took " + timer / 1000.0 + " seconds.");

		return res;

	}
	/**Perseus
	 * Compute expected reachability rewards,
	 * i.e. compute the min/max reward accumulated to reach a state in {@code target}.
	 * @param pomdp The POMDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
	public ArrayList<AlphaVector> backupStage(POMDP pomdp, ArrayList<AlphaVector> immediateRewards, ArrayList<AlphaVector> V, ArrayList<Belief> B, BitSet unknownObs, boolean min, int stage, int endState, BitSet inf) {
		int nStates = pomdp.getNumStates();
		double discount = 1;
		if(min) {
			discount = 0.99;
		}
		
		/*updated here, use target as the endState, this should be generally good
		 * added another parameter "endState"
		 * int endState = 1;
		 * for (int i=0; i<nStates;i++) {
			if (!unknownObs.get(pomdp.getObservation(i))) {
				endState=i;
				mainLog.println("endState"+endState);
				break;
			}
		}
		*/
		
		int nObservations = pomdp.getNumObservations();
		ArrayList<Object> allActions =getAllActions(pomdp);
		int nActions = allActions.size();
		
		ArrayList<AlphaVector> Vnext = new ArrayList<AlphaVector>();
		List<Belief> Btilde = new ArrayList<Belief>();
		Btilde.addAll(B);
		
		AlphaVector[][][] gkao = new AlphaVector[V.size()][nActions][nObservations];
		for(int k=0; k<V.size(); k++) {
			for(int a=0; a<nActions; a++) {
				//if(V.get(k).getAction()!=a)
					//continue;
				for(int o=0; o<nObservations; o++) {
					double[] entries = new double[nStates];
					
					for(int s=0; s<nStates; s++) {
						double val = 0.0;
						Object action = allActions.get(a);
						List<Object> availableActions= pomdp.getAvailableActions(s);
						if (availableActions.contains(action)) {
							// if that action will only leads to inf.
							for(int sPrime=0; sPrime<nStates; sPrime++) {
								double value = V.get(k).getEntry(sPrime);
								double obsP = pomdp.getObservationProb(sPrime, o);
								if (obsP==0) {
									continue;
								}
								double tranP=0.0;
								int choice = pomdp.getChoiceByAction(s, action);
								Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(s,choice);
								while (iter.hasNext()) {
									Map.Entry<Integer, Double> trans = iter.next();
									if (trans.getKey()==sPrime) {
										tranP = trans.getValue();	 
									}
								}
								val += obsP * tranP * value*discount;
							}
						}
						else {
							int sPrime = endState;
							double value = V.get(k).getEntry(sPrime);
							double tranP=1;
							double obsP = pomdp.getObservationProb(sPrime, o);
							val += obsP * tranP * value*discount;
							if(val>-100000) {
								//mainLog.println("state="+s+" action="+action+" val="+val+" obsP="+obsP+" value="+value,0);
							}
						}
						entries[s] = val;
					}
					
					AlphaVector av = new AlphaVector(entries);
					av.setAction(a);
					gkao[k][a][o] = av;
					//mainLog.print(k+" "+a+" "+o+" "+" gkao Action = "+ av.getAction()+ " action name = "+allActions.get(av.getAction()));
					//mainLog.println( "   value = "+ Arrays.toString(av.getEntries()));
				}
			}
		}
		
		Random rnd = new Random();
		int count =0;
		//mainLog.println("Bsize="+Btilde.size());
		
		// run the backup stage
		while(Btilde.size() > 0) {
			// sample a belief point uniformly at random
			int beliefIndex = rnd.nextInt(Btilde.size());
			//beliefIndex =0;
			/*if(stage==1 & count==0) {
				beliefIndex=67;
			}
			*/
			Belief b = Btilde.get(beliefIndex);
			//Btilde.remove(Btilde.indexOf(b));
			// compute backup(b)
			//mainLog.println("sta"+stage+" count="+count+"*************ready to back up for"+b );
			//mainLog.println("b dis ="+ Arrays.toString(b.toDistributionOverStates(pomdp)));
			count++;
			AlphaVector alpha = backup(pomdp, immediateRewards, gkao, b, V, inf);
			if (alpha==null) {
				Btilde.remove(beliefIndex);
				continue;
			}
			int goodAlpha =1;
			double[] b_dis = b.toDistributionOverStates(pomdp);
			for (int s=0; s< b_dis.length; s++) {
				if (b_dis[s]>0) {
					if (!pomdp.getAvailableActions(s).contains(allActions.get(alpha.getAction()))) {
						//mainLog.println("wrong Action no available s= "+s+" actionN="+alpha.getAction()+"actionName="+allActions.get(alpha.getAction()));
					}
					int c = pomdp.getChoiceByAction(s, allActions.get(alpha.getAction()));
					if (pomdp.allSuccessorsInSet(s, c, inf) ) {
						//mainLog.println("wrong s= "+s+" actionN="+alpha.getAction()+"actionName="+allActions.get(alpha.getAction()));
						goodAlpha = 0;
					}
				}
			}
		
			

			//mainLog.println("alpha a="+alpha.getAction());
			//ArrayList<Integer> possibleObservationsForBeliefAction = getPossibleObservationsForBeliefAction(b, allActions.get(alpha.getAction()), pomdp);
			//for (int p=0; p<possibleObservationsForBeliefAction.size() ;p++)
			//	mainLog.print("o="+possibleObservationsForBeliefAction.get(p)+" ");
			
			//mainLog.println("alpha v="+alpha.getAction()+Arrays.toString(alpha.getEntries()));
			
			// check if we need to add alpha
			//double oldValue = (getPossibleValue(b,V,pomdp));
			double oldValue= AlphaVector.getValue(b.toDistributionOverStates(pomdp), V);
			//mainLog.println("oldValue="+oldValue);
			//double oldValue = AlphaVector.getValue(b.toDistributionOverStates(pomdp), V));
			
			double newValue =  (alpha.getDotProduct(b.toDistributionOverStates(pomdp)));
			//mainLog.println("newValueValue="+newValue);
			//if(oldValue!=newValue)
			//	mainLog.println("different="+(oldValue-newValue));
			double diff =0.0;
			 
			diff= newValue-oldValue;
			if(diff >=0) {
				assert alpha.getAction() >= 0 && alpha.getAction() < pomdp.getMaxNumChoices() : "invalid action: "+alpha.getAction();
//				if (!Vnext.contains(alpha)) {
				if (!AlphaVector.setContainsVector(Vnext, alpha)) {
					Vnext.add(alpha);
					//mainLog.println("1 Adding vector action ="+alpha.getAction());
					//mainLog.println("1 Adding vector action ="+Arrays.toString(alpha.getEntries()));
				}
				ArrayList<Belief> newB = new ArrayList<Belief> ();
				Btilde.remove(beliefIndex);
	
				for (int r =0; r<Btilde.size();r++) {
					Belief br = Btilde.get(r);
					//double VValue = Math.abs(getPossibleValue(br,V,pomdp));
//					double VValue = getPossibleValue(br,V,pomdp);
					double VValue = AlphaVector.getValue(br.toDistributionOverStates(pomdp),V);
					//if (!getPossibleActionsForBelief(br,pomdp).contains(allActions.get(alpha.getAction()))) {
						//mainLog.println("belief does not have this action"+br);
						//newB.add(br);
					//}
					//else {
						//double alphaValue = Math.abs(alpha.getDotProduct(br.toDistributionOverStates(pomdp)));
						
						double alphaValue = alpha.getDotProduct(br.toDistributionOverStates(pomdp));
						//ol= AlphaVector.getValue(br.toDistributionOverStates(pomdp),V);
						if (alphaValue < VValue ) {
							newB.add(br);
							//mainLog.println("keep"+br);
							//mainLog.println("VValue = "+VValue+" alphaValue = "+alphaValue);
						}
						else {
							;//mainLog.println("remove"+br);
							//mainLog.println("VValue = "+VValue+" alphaValue = "+alphaValue);
						}
					//}
				}
				Btilde = newB;
				//mainLog.println("prune Bsize="+newB.size()+"Vsize="+Vnext.size());
			}
			else {
				int bestVectorIndex = AlphaVector.getBestVectorIndex(b.toDistributionOverStates(pomdp), V);
				AlphaVector alphaBest= V.get(bestVectorIndex);
				//AlphaVector alphaBest = getBestPossibleAlpha(b, V,pomdp);
				//assert V.get(bestVectorIndex).getAction() >= 0 && V.get(bestVectorIndex).getAction() < pomdp.getMaxNumChoices() : "invalid action: "+V.get(bestVectorIndex).getAction();
				//mainLog.println("Best alpha action="+alphaBest.getAction()+Arrays.toString(alphaBest.getEntries()));
	
			if (!Vnext.contains(alphaBest)) {
					Vnext.add(alphaBest);
					//mainLog.println("2 Adding vector action ="+alphaBest.getAction());
					//mainLog.println("2 Adding vector action ="+Arrays.toString(alphaBest.getEntries()));
				}
				//Btilde.remove(Btilde.indexOf(b));
				Btilde.remove(beliefIndex);
			}
			
			// compute new Btilde containing non-improved belief points
			//ArrayList<Belief> n = new ArrayList<Belief>();	
			//for(Belief bp : B) {
				//double oV = AlphaVector.getValue(bp.toDistributionOverStates(pomdp), V);
				//double nV = AlphaVector.getValue(bp.toDistributionOverStates(pomdp), Vnext);
				//if(nV < oV) 					newBtilde.add(bp);				
			//}	
		

			//mainLog.println("Btilde"+Btilde.size());
		}
		return Vnext;
	}
	public AlphaVector backup(POMDP pomdp, List<AlphaVector> immediateRewards, AlphaVector[][][] gkao, Belief b, ArrayList<AlphaVector> V, BitSet inf) {
		int nStates = pomdp.getNumStates();
		//int nActions = pomdp.getMaxNumChoices();
		int nObservations = pomdp.getNumObservations();
		ArrayList<AlphaVector> ga = new ArrayList<AlphaVector>();
		ArrayList<Object> allActions = getAllActions(pomdp);

		int nActions = allActions.size(); // nActions = the number of  the all the actions available 

		ArrayList<Object> possibelActionsForBelief = getPossibleActionsForBelief(b,pomdp);
		//for (int i =0; i<possibelActionsForBelief.size(); i++) {
		//	mainLog.println(""+possibelActionsForBelief.get(i));
		//}
		for(int a=0; a<nActions; a++) {
			//mainLog.println("actionName="+allActions.get(a)+"a="+a+"/"+nActions);

			if (!possibelActionsForBelief.contains(allActions.get(a))) {
				//mainLog.println("no such action"+allActions.get(a));
				continue;
			}
			
			//mainLog.println("\ncomputing for action ="+allActions.get(a));
			//ArrayList<Integer> possibleObservationsForBeliefAction = getPossibleObservationsForBeliefAction(b, allActions.get(a), pomdp);
			//mainLog.println("kao=");
			
			List<AlphaVector> oVectors = new ArrayList<AlphaVector>();
			
			for(int o=0; o<nObservations; o++) {
				double maxVal = Double.NEGATIVE_INFINITY;
				AlphaVector maxVector = null;
				int choice = possibelActionsForBelief.indexOf(allActions.get(a));
				//mainLog.print("choice"+choice);
				Belief updatedBelief= pomdp.getBeliefAfterChoiceAndObservation(b, choice, o);
		
				//mainLog.println("updatedBelief="+updatedBelief);
				//ArrayList<Object> futureActions = getPossibleActionsForBelief(updatedBelief,pomdp);
				int K = gkao.length;
				for(int k=0; k<K; k++) {
					//double [] alphaValue = gkao[k][a][o].getEntries();
					////boolean isEmpty = False;
					double product = gkao[k][a][o].getDotProduct(b.toDistributionOverStates(pomdp));
					if(product > maxVal ) {
						maxVal = product;
						maxVector = gkao[k][a][o];
					}
					//mainLog.println("kao="+k+a+o+"->"+product);
					//mainLog.println("maxVal"+k+a+o+"->"+maxVal);
				}
				assert maxVector != null;
				if (maxVector==null) 
					continue;
				oVectors.add(maxVector);
				//mainLog.println("Action "+a+" Final maxVal->"+maxVal);
				//mainLog.println("Action "+a+" Final maxVector action ->"+maxVector.getAction());
				//mainLog.println("Action "+a+" Final maxVect value->"+Arrays.toString(maxVector.getEntries()));

			}
			if(oVectors.size()==0) {
				continue;
			}
			assert oVectors.size() > 0;
			// take sum of the vectors
			AlphaVector sumVector = oVectors.get(0);
			
			for(int j=1; j<oVectors.size(); j++) {
				sumVector = AlphaVector.sumVectors(sumVector, oVectors.get(j));
			}
			// multiply by discount factor
			double[] sumVectorEntries = sumVector.getEntries();
			for(int s=0; s<nStates; s++) {
				sumVectorEntries[s] =  sumVectorEntries[s];
			}
			sumVector.setEntries(sumVectorEntries);
			//mainLog.println("Action "+a+" Final sumVector action ->"+sumVector.getAction());
			//mainLog.println("Action "+a+" Final sumVector value->"+Arrays.toString(sumVector.getEntries()));
			AlphaVector av = AlphaVector.sumVectors(immediateRewards.get(a), sumVector);
			av.setAction(a);
			ga.add(av);
			


		}
		//mainLog.println("ga.size="+ga.size()+" nAction="+nActions);
		assert ga.size() == nActions;
		// find the maximizing vector
		double maxVal = AlphaVector.getValue(b.toDistributionOverStates(pomdp),ga);
		//mainLog.println("ga.size="+ga.size()+" maxVal Predict="+maxVal);

		ArrayList<Integer> candiateMax = new ArrayList<Integer>();
		AlphaVector vFinal = null;
	
		for(AlphaVector av : ga) {
			;
			double product = av.getDotProduct(b.toDistributionOverStates(pomdp));
			//mainLog.println("*********  av action ->"+av.getAction());
			//mainLog.println("********* av value->"+Arrays.toString(av.getEntries()));
			//mainLog.println("********* product->"+ product);
			//if(product > maxVal) {
			//	maxVal = product;
			//	vFinal = av;
			//}
		}
		

		for (int i=0; i< ga.size();i++) {
			AlphaVector av = ga.get(i);
			double product = av.getDotProduct(b.toDistributionOverStates(pomdp));
			//mainLog.println("product "+ product+"ga a="+av.getAction()+"v="+Arrays.toString(av.getEntries()));
			if (product==maxVal) {
				candiateMax.add(i);
			}
		}
		
		/*Random rnd = new Random();
		int index = rnd.nextInt(ga.size());
		vFinal= ga.get(index);
*/
		Random rnd = new Random();
		if(candiateMax.size()==0) {
			return null;
		}
		int ind = rnd.nextInt(candiateMax.size());
		
		int index = candiateMax.get(ind);
		vFinal= ga.get(index);
		 
		//mainLog.println("choose "+ index+ "from "+candiateMax.size());
		assert vFinal != null;
		
		for (int s=0; s<nStates; s++) {
			int atp = vFinal.getAction();
			Object actiontp = allActions.get(atp);
			int choicetp = pomdp.getChoiceByAction(s, actiontp);
			if (b.toDistributionOverStates(pomdp)[s]>0) {
				if( pomdp.allSuccessorsInSet(s, choicetp, inf) ) {
					mainLog.println("wrong state="+s+"action"+actiontp+"candiateSize"+candiateMax.size());
				}
			}
		}
		
		return vFinal;
	}

	public ArrayList<Object> getPossibleActionsForBelief(Belief b, POMDP pomdp) {
		ArrayList <Object> availableActionsForBelief = new ArrayList<Object> ();
		for (int s = 0; s < pomdp.getNumStates(); s++) {
			if ((b.toDistributionOverStates(pomdp)[s])>0){
				List <Object> availableActionsForState = pomdp.getAvailableActions(s);
				for (Object a: availableActionsForState) {
					if (!availableActionsForBelief.contains(a)) {
						availableActionsForBelief.add(a);
					}
				}
			}
		}
		return availableActionsForBelief;
		
	}
	public ArrayList<Object> getAllActions(POMDP pomdp){
		ArrayList <Object> allActions = new ArrayList<Object> ();
		for (int s =0; s<pomdp.getNumStates();s++) {
			List <Object> availableActionsForState = pomdp.getAvailableActions(s);

			for (Object a: availableActionsForState) {
//				System.out.println("?"+a);
				if (!allActions.contains(a) & a!= null) {
					allActions.add(a);
				}
			}
		}
		return allActions;
	}

	public boolean checkConverge (ArrayList<Double > diff, double tol){
		int size = diff.size();
		
		int consecutive_stage = 30;
		if(size>consecutive_stage) {
			for (int i=0; i<consecutive_stage-1;i++) {
				if (diff.get(size-i-1)>tol) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
	public ArrayList<Belief> randomExploreBeliefs(POMDP pomdp, BitSet target,  BitSet statesOfInterest) throws PrismException
	{
		BitSet targetObs = getObservationsMatchingStates(pomdp, target);
		
		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}	
		
		if (targetObs == null) {
			//throw new PrismException("Target for expected reachability is not observable");
			mainLog.println("Target for expected reachability is not observable");
		}
		// Find _some_ of the states with infinite reward
		// (those from which *every* MDP strategy has prob<1 of reaching the target,
		// and therefore so does every POMDP strategy)
		MDPModelChecker mcProb1 = new MDPModelChecker(this);
		BitSet inf = mcProb1.prob1(pomdp, null, target, false, null);
		inf.flip(0, pomdp.getNumStates());
		// Find observations for which all states are known to have inf reward
		BitSet infObs = getObservationsCoveredByStates(pomdp, inf);
		//mainLog.println("target obs=" + targetObs.cardinality() + ", inf obs=" + infObs.cardinality());
		
		// Determine set of observations actually need to perform computation for
		// eg. if obs=1 & unknownObs(obs)=true -> obs=1 needs computation
		// eg. if obs=2 & unknownObs(obs)=false -> obs=1 does not need computation
		BitSet unknownObs = new BitSet();
		unknownObs.set(0, pomdp.getNumObservations());
		if(targetObs!=null) {
			unknownObs.andNot(targetObs);
		}
		unknownObs.andNot(infObs);

		BitSet observations_of_target = new BitSet();
		
		for (int i = target.nextSetBit(0); i>=0; i= target.nextSetBit(i+1)) {
			//mainLog.println("target"+target.nextSetBit(i)+"obs"+(pomdp.getObservation(target.nextSetBit(i))) );
			observations_of_target.set(pomdp.getObservation(target.nextSetBit(i)));
		}
		
		//mainLog.println(observations_of_target);
		if(observations_of_target!=null) {
			unknownObs.andNot(observations_of_target);
		}
		
		ArrayList<Belief> B = new ArrayList<Belief>();
		ArrayList<Belief>Bset = new ArrayList<Belief>();
		B.add(pomdp.getInitialBelief());
		int BeliefSamplingSteps=100;
		int BeliefSamplingRuns=200;
		Belief b = pomdp.getInitialBelief();
		for(int run=0; run<BeliefSamplingRuns; run++) {
			for(int step=0; step<BeliefSamplingSteps; step++) {
				double [] b_dis = b.toDistributionOverStates(pomdp);
				if (!Bset.contains(b)) {
					
					//mainLog.println("belief previous");
				//	mainLog.println(b);
					for (int s=0; s<pomdp.getNumStates(); s++) {
						if (b.toDistributionOverStates(pomdp)[s]>0) {
							//mainLog.print(s+" ");
						}
					}
					
					for(int o=0; o<pomdp.getNumObservations(); o++) {
						//mainLog.println("obs = "+o);
						HashSet<Integer> availableChoices = new HashSet<Integer> ();
						//find the available choices
						for (int i=0; i<b_dis.length; i++) {
							if (b_dis[i]>0) {
								List<Object> availbleActions = pomdp.getAvailableActions(i);
								for (Object availbleAction : availbleActions) {
									availableChoices.add(pomdp.getChoiceByAction(i, availbleAction));
								}
							}
						}
						//iterate all choices
						//add all possible updated choices 
						for(int a: availableChoices) {
							double probs= pomdp.getObservationProbAfterChoice(b, a, o);
							if (probs>0) {
								Belief bao = pomdp.getBeliefAfterChoiceAndObservation(b, a, o);
								if ((!B.contains(bao)) & unknownObs.get(bao.so)){
									//if ((!B.contains(bao))){
									B.add(bao);
								//	mainLog.println("\nbelief after");
									//mainLog.println(bao);
									for (int s=0; s<pomdp.getNumStates(); s++) {
										if (bao.toDistributionOverStates(pomdp)[s]>0) {
											//	mainLog.print(s+" ");
										}
									}
								}
							}
						}
					}
				}
				Random rnd = new Random();
				Bset.add(b);
				//randomly choose a successor Belief to continue;
				b = B.get(rnd.nextInt(B.size()));
				if (B.size()>1000) {
					break;
				}
				//mainLog.println("Bsize="+B.size());
			}
		}
		/*
		// add corner beliefs
		for(int s=0; s<pomdp.getNumStates(); s++) {
			double[] beliefEntries = new double[pomdp.getNumStates()];
			beliefEntries[s] = 1.0;
			Belief corner = new Belief(beliefEntries, pomdp);
			//if ((!B.contains(corner)) & unknownObs.get(corner.so)) {
							if ((!B.contains(corner)) ) {
				B.add(corner);
			}
		}
		*/
//		for (int i=0; i<B.size();i++) {
//			mainLog.println("index="+i+" "+B.get(i)+" full="+Arrays.toString(B.get(i).toDistributionOverStates(pomdp)));
//		}
		return B;
	}
	
	
	
	/**
	 * Compute expected reachability rewards,
	 * i.e. compute the min/max reward accumulated to reach a state in {@code target}.
	 * @param pomdp The POMDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
	
	
	/**
	 *  make a copy of AlphaMatrix set
	 *  @param A AlphaMatrix Set
	 *  @return a copy of AlphaMatrix Set
	*/
	public ArrayList<AlphaMatrix> copyAlphaMatrixSet(ArrayList<AlphaMatrix> A){
		ArrayList<AlphaMatrix> B = new ArrayList<AlphaMatrix>();
		
		for (int i =0; i<A.size(); i++) {
			AlphaMatrix alphaMatrix = A.get(i);
			AlphaMatrix alphaMatrix_copy = new AlphaMatrix(alphaMatrix.getMatrix());
			alphaMatrix_copy.setAction(alphaMatrix.getAction());
			B.add(alphaMatrix_copy);
		}
		return B;
	}
	/**
	 *  make a copy of Belief set
	 *  @param B Belief Set
	 *  @return B_copy a copy of BeliefSet
	*/
	public ArrayList<Belief> copyBeliefSet(ArrayList<Belief> B){
		ArrayList<Belief> B_copy = new ArrayList<Belief> ();
		for (int i=0; i<B.size(); i++) {
			Belief b_copy = B.get(i);
			B_copy.add(b_copy);
		}
		return B_copy;
	}
	
	/* backup stage
	 * (see Eqs 9~11 in "Point-Based Planning for Multi-Objective POMDPs")
	 * @param A A set of AlphaMatrix // Not used
	 * @param b Belief
	 * @param weights weights over objectives
	 * @param pomdp POMDP model
	 * @param immediateRewards immediate rewards 
	 * @param gkao A cache of back projections based on A
	 * @return AlphaMatrix that maximize the reward  
	 * */
	public AlphaMatrix backupStageMO(ArrayList<AlphaMatrix> A, Belief b, double [] weights, POMDP pomdp, ArrayList<AlphaMatrix> immediateRewards, AlphaMatrix [][][] gkao)
	{
		//int nStates = pomdp.getNumStates();
		int nActions = pomdp.getMaxNumChoices();
		int nObservations = pomdp.getNumObservations();
		ArrayList<AlphaMatrix> ga = new ArrayList<AlphaMatrix>();
		ArrayList<Object> allActions = getAllActions(pomdp);
		ArrayList<Object> possibelActionsForBelief = getPossibleActionsForBelief(b,pomdp);
		for (int a=0; a<nActions; a++) {
			if (!possibelActionsForBelief.contains(allActions.get(a))) {
				continue;
			}
			//ArrayList<Integer> possibleObservationsForBeliefAction = getPossibleObservationsForBeliefAction(b, allActions.get(a), pomdp);
			ArrayList<AlphaMatrix> oMatrices = new ArrayList<AlphaMatrix>();
			for (int o=0; o<nObservations; o++) {
				double maxVal = Double.NEGATIVE_INFINITY;
				AlphaMatrix maxMatrix = null;
				//int choice = possibelActionsForBelief.indexOf(allActions.get(a));
				//Belief updatedBelief= pomdp.getBeliefAfterChoiceAndObservation(b,choice, o);
				//ArrayList<Object> futureActions = getPossibleActionsForBelief(updatedBelief, pomdp);
				int K = gkao.length;
				for(int k=0; k<K; k++) {
					double product= gkao[k][a][o].value(b, weights, pomdp);
					if(product > maxVal ) {
						maxVal = product;
						maxMatrix= gkao[k][a][o];
					}
				}
				if (maxMatrix==null) {
					continue;
				}
				oMatrices.add(maxMatrix.clone());
			}
			if (oMatrices.size()==0) {
				continue;
			}
			AlphaMatrix sumMatrix = oMatrices.get(0).clone();
			for (int j =1; j<oMatrices.size();j++) {
				sumMatrix = AlphaMatrix.sumMatrices(sumMatrix, oMatrices.get(j));
			}
			AlphaMatrix am = AlphaMatrix.sumMatrices(immediateRewards.get(a), sumMatrix);
			am.setAction(a);
			ga.add(am.clone());
		}
		//mainLog.println("gasize="+ga.size());
		int bestAlphaMatrixIndex = AlphaMatrix.getMaxValueIndex(b, ga, weights, pomdp);
		AlphaMatrix bestAlphaMatrix = ga.get(bestAlphaMatrixIndex);
		//mainLog.println("best am"+bestAlphaMatrix);
		return bestAlphaMatrix;
	}
	
	/* Cahce GKao based a set of AlphaMatrix V
	 * @param V, the initial set of AlphaMatrix to begin backup
	 * @return Arrays of AlphaMatrix
	 * */
	public AlphaMatrix [][][] cacheGKao(ArrayList<AlphaMatrix> V, POMDP pomdp, int endState){
		ArrayList<Object> allActions =getAllActions(pomdp);
		int nActions = allActions.size();
		int nObservations = pomdp.getNumObservations();
		int nObjectives= V.get(0).getNumObjectives();
		int	nStates = pomdp.getNumStates();
		
		// Eq.9 Initial GAO
		AlphaMatrix[][][] gkao = new AlphaMatrix[V.size()][nActions][nObservations];
		for (int k=0; k<V.size(); k++) {
			for (int a=0; a<nActions; a++) {
				for (int o=0; o<nObservations; o++) {
					double[][] matrix = new double[nStates][nObjectives];
					for (int s=0; s<nStates; s++)	{
						double[] val = new double [nObjectives];
						Object action = allActions.get(a);
						List<Object> availableActions= pomdp.getAvailableActions(s);
						if (availableActions.contains(action)) {
							for(int sPrime=0; sPrime<nStates; sPrime++) {
								double[] value = V.get(k).getValues(sPrime);
								double obsP = pomdp.getObservationProb(sPrime, o);
								if (obsP==0.0) {
									continue;
								}
								double tranP=0.0;
								int choice = pomdp.getChoiceByAction(s, action);
								Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(s,choice);
								while (iter.hasNext()) {
									Map.Entry<Integer, Double> trans = iter.next();
									if (trans.getKey()==sPrime) {
										tranP = trans.getValue();	 
									}
								}
								for (int v=0; v<val.length; v++) {
									val[v] += value[v]* obsP *tranP*0.99;
								}
							}
						}
						else {
							int sPrime=endState;
							double[] value = V.get(k).getValues(sPrime);
							double tranP=1;
							double obsP = pomdp.getObservationProb(sPrime, o);
							for (int v=0; v<val.length; v++) {
								val[v] += value[v]* obsP *tranP*0.99;
							}							
						}
						for (int v=0; v<val.length; v++) {
							matrix[s][v]=val[v];
						}
					}
					AlphaMatrix am = new AlphaMatrix(matrix);
					am.setAction(a);
					gkao[k][a][o] = am;
					//mainLog.println("GKAO="+k+a+o);
					//mainLog.println(am);
				}
			}
		}
		return gkao;
	}
	
	/* OLS-compliant Perseus algorithm 
	 * See Algorithm 2 in "Point-Based Planning for Multi-Objective POMDPs"
	 * It is an alternative implementation of solveScalarizedPOMDP
	 * Modification on how to choose belief 
	 * @param A, A set of AlphaMatrix to begin back up
	 * @param B, A set of belief points based on random sampling
	 * @param weights, weights over objectives
	 * @param eta, threshold for termination
	 * @param pomdp, POMDP model
	 * @param endState, one of the the end State, used in cache GKAO
	 * */
	
	public ArrayList<AlphaMatrix> computeMultiReachRewardPerseus(ArrayList<AlphaMatrix> A, ArrayList<Belief> B, double [] weights, double eta, POMDP pomdp, ArrayList<AlphaMatrix> immediateRewards, int endState, long startTime)
	{
		mainLog.println("calling solve scalarized POMDP");
		//mainLog.println("Weights="+Arrays.toString(weights));
		ArrayList<AlphaMatrix> Aprime =  copyAlphaMatrixSet (A); // L1
		//L2
		for (int i=0; i<A.size(); i++) {
			AlphaMatrix am = A.get(i);
			double[][] matrix = new double [am.getNumStates()][am.getNumObjectives()];
			for (int s=0; s<am.getNumStates();s++) {
				for (int obj=0; obj<am.getNumObjectives();obj++) {
					matrix [s][obj] = -99999999;//Double.NEGATIVE_INFINITY;
				}
			}
			am.setMatrix(matrix);
		}
		Random rnd = new Random();
		int count=0;
		while(true) {
			//line 3
			count+=1;
			double diff = Double.NEGATIVE_INFINITY;
			for( Belief b : B){
				double value_Aprime = AlphaMatrix.getMaxValue(b, Aprime, weights, pomdp);
				double value_A= AlphaMatrix.getMaxValue(b, A, weights, pomdp);
				if (value_Aprime-value_A > diff) {
					diff = value_Aprime-value_A;
				}
			}
			double elapsed = (System.currentTimeMillis() - startTime) * 0.001;
			double expectedValue = Math.abs(AlphaMatrix.getMaxValue(pomdp.getInitialBelief(), A, weights, pomdp));
			mainLog.println("iteration="+count+" dif="+diff+" value="+expectedValue+" time elapsed ="+elapsed );
			if (diff <= eta) {
				mainLog.println("converge with tolerance "+eta);
				break;
			}
			if(count>5000) {
				mainLog.println("reach stage limit 5000");
				break;
			}
			//mainLog.println("AprimeAprimeAprimeAprimeAprimeAprimeAprime");
			//for (int i=0; i<Aprime.size();i++) {
			//	mainLog.println(Aprime.get(i));
			//}
			//Line 4
			A = copyAlphaMatrixSet(Aprime);
			Aprime = new ArrayList<AlphaMatrix> ();
			ArrayList<Belief> Bprime = copyBeliefSet(B);
			//mainLog.println("<<<<<<<<<<<<<<<<<<<<<stage"+count);
			//mainLog.println("AAAAAAAAAAAAAAsize"+A.size());
			//for (int i=0; i<A.size();i++) {
			//	mainLog.println(A.get(i));
			//}
			AlphaMatrix [][][] gkao = cacheGKao(A, pomdp, endState);
			//Line 5
			while (Bprime.size()>0) {
				//Line 6 get random belief
				int beliefIndex = rnd.nextInt(Bprime.size());
				Belief b = Bprime.get(beliefIndex);
				Bprime.remove(beliefIndex);

				//Line 7 Backup AlphaMatrixSet belief weights 
				//mainLog.println("ready to back up for "+b);
				AlphaMatrix Am = backupStageMO (A, b, weights, pomdp, immediateRewards, gkao);
				double newValue= Am.value(b, weights, pomdp);
				double oldValue = AlphaMatrix.getMaxValue(b, A, weights, pomdp);
				//mainLog.println("new value="+Am.value(b, weights, pomdp));
				//mainLog.println("old value="+AlphaMatrix.getMaxValue(b, A, weights, pomdp));

				//Line 8 update A'
				ArrayList<AlphaMatrix> A_tp = copyAlphaMatrixSet(A);
				A_tp.add(Am);
				int bestAlphaMatrixIndex = AlphaMatrix.getMaxValueIndex(b, A_tp, weights, pomdp);
				AlphaMatrix bestAlphaMatrix = A_tp.get(bestAlphaMatrixIndex);
				//mainLog.println("best="+bestAlphaMatrix);
				if (!AlphaMatrix.contains(Aprime, bestAlphaMatrix) ) {
					Aprime.add(bestAlphaMatrix);
				}
				if(newValue>oldValue) {
					//Line 9 update Belief set
					ArrayList<Belief> B_new = new ArrayList<Belief> ();
					for (Belief br : Bprime) {
						if (AlphaMatrix.getMaxValue(br, Aprime, weights, pomdp) < AlphaMatrix.getMaxValue(br, A, weights, pomdp) ) {
							B_new.add(br);
						}
					}
					Bprime = B_new;
				}
				//mainLog.println("B size="+Bprime.size());
				//mainLog.println("A size="+Aprime.size());
			}
			
			/*
			while (Bprime.size()>0) {
				//Line 6 get random belief
				int beliefIndex = rnd.nextInt(Bprime.size());
				Belief b = Bprime.get(beliefIndex);
				Bprime.remove(beliefIndex);
				
				//Line 7 Backup AlphaMatrixSet belief weights 
				//mainLog.println("ready to back up for "+b);
				AlphaMatrix Am = backupStageMO (A, b, weights, pomdp, immediateRewards, gkao);
				double newValue= Am.value(b, weights, pomdp);
				double oldValue = AlphaMatrix.getMaxValue(b, A, weights, pomdp);
				//mainLog.println("new value="+Am.value(b, weights, pomdp));
				//mainLog.println("old value="+AlphaMatrix.getMaxValue(b, A, weights, pomdp));
				if (newValue-oldValue>=0) {
					if (!AlphaMatrix.contains(Aprime, Am) ) {
						Aprime.add(Am);
					}					
					ArrayList<Belief> B_new = new ArrayList<Belief> ();
					for (Belief br : Bprime) {
						if (AlphaMatrix.getMaxValue(br, Aprime, weights, pomdp) < AlphaMatrix.getMaxValue(br, A, weights, pomdp) ) {
							B_new.add(br);
						}
					}
					Bprime = B_new;
				}
				else {
					int bestAlphaMatrixIndex = AlphaMatrix.getMaxValueIndex(b, A, weights, pomdp);
					AlphaMatrix bestAlphaMatrix = A.get(bestAlphaMatrixIndex);
					if (!AlphaMatrix.contains(Aprime, bestAlphaMatrix) ) {
						Aprime.add(bestAlphaMatrix);
					}
				}
			}
			*/
		}
		return Aprime;
	}
	
	
	public ModelCheckerResult computeReachRewardsRTBSS(ArrayList<AlphaMatrix> immediate_reward, double[] weights,  POMDP pomdp, BitSet target,  BitSet statesOfInterest) throws PrismException
	{ 
		mainLog.println("start RTBSS");
		Belief b = pomdp.getInitialBelief();
		int d = 2;
		RTBSS(immediate_reward, weights, b,d, pomdp,  target,  statesOfInterest);
		
		return null;
	}
	public double RTBSS(ArrayList<AlphaMatrix> immediate_reward, double[] weights, Belief b, int d, POMDP pomdp, BitSet target,  BitSet statesOfInterest) throws PrismException
	{
		int bestAction = -1;
		if (d==0) {
			//return U(b)
			double reward = Double.NEGATIVE_INFINITY;
			for (int a=0; a<immediate_reward.size(); a++) {
				double value = immediate_reward.get(a).value(b, weights, pomdp);
				if (value>reward)
					reward = value;
			}
			return reward;
		}
		
		ArrayList<Integer> actionList = new ArrayList<Integer> ();
		actionList.add(0);
		ArrayList<Double> valueList = new ArrayList<Double>();
		valueList.add(immediate_reward.get(0).value(b, weights, pomdp));
		for (int a=1; a<immediate_reward.size(); a++) {
			double value = immediate_reward.get(a).value(b, weights, pomdp);
			int insertIndex = -1;
			for (int i=0; i<valueList.size(); i++) {
				if (value<valueList.get(i)) {
					insertIndex = i;
					break;
				}
				insertIndex =i;
			}
			actionList.add(insertIndex, a);
		}
		
		double max = Double.NEGATIVE_INFINITY;
		
		int Rmax= 3;
		for (int a : actionList) {
			int choice = a; ////////////////////////////////////
			double curReward = immediate_reward.get(a).value(b, weights, pomdp);
			double heuristic = 0;
			for (int i=1; i<d+1; i++) {
				heuristic = Math.pow(0.9, i) * Rmax;
			}
			double uBound = curReward+heuristic;
			if (uBound>max) {
				int nObservations = pomdp.getNumObservations();
				for (int o=0; o<nObservations; o++) {
					Belief b_next = pomdp.getBeliefAfterChoiceAndObservation(b, choice, o);
					curReward += 0.9 * pomdp.getObservationProbAfterChoice(b, choice, o)* RTBSS(immediate_reward, weights, b_next, d-1, pomdp,  target,  statesOfInterest);
				}
				if (curReward > max) {
					max = curReward;
					if (d==3) {
						bestAction = a;
					}
				}
			}
		}
		return max;
	}
	
	public ModelCheckerResult computeReachRewardsPOMCP(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest) throws PrismException
	{

		BitSet targetObs = getObservationsMatchingStates(pomdp, target);
		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}	
		
		if (targetObs == null) {
			throw new PrismException("Target for expected reachability is not observable");
		}
		
		// Find _some_ of the states with infinite reward
		// (those from which *every* MDP strategy has prob<1 of reaching the target,
		// and therefore so does every POMDP strategy)
		MDPModelChecker mcProb1 = new MDPModelChecker(this);
		BitSet inf = mcProb1.prob1(pomdp, null, target, false, null);
		inf.flip(0, pomdp.getNumStates());
		// Find observations for which all states are known to have inf reward
		BitSet infObs = getObservationsCoveredByStates(pomdp, inf);
		BitSet unknownObs = new BitSet();
		unknownObs.set(0, pomdp.getNumObservations());
		if (targetObs != null) {
			unknownObs.andNot(targetObs);	
		}
		unknownObs.andNot(infObs);
		int nStates = pomdp.getNumStates();
		ModelCheckerResult res = null;
		mainLog.println("endS ");
		ArrayList<Integer> endStates = new ArrayList<Integer>();
		for (int i=0; i<nStates;i++) {
			if (!unknownObs.get(pomdp.getObservation(i))) {
				endStates.add(i);
				mainLog.print(i + " ");
			}
		}
		mainLog.println(" ");
		int numEpisode = 100;
		ArrayList<Double> rewards = new ArrayList<Double> ();
		double rewardAverage = 0;
		for (int n =0; n < numEpisode; n++) {
			mainLog.println("start Episode"+n+" out of "+numEpisode);
			double reward = computeReachRewardsPOMCPEpisode(pomdp,  mdpRewards,  target,  min,  statesOfInterest, endStates);
			rewards.add(reward);
			rewardAverage += reward;
		}
		rewardAverage = rewardAverage / rewards.size();
		mainLog.println("average reward = "+ rewardAverage);
		return res; 
	}
	

	public double computeReachRewardsPOMCPEpisode(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest, ArrayList<Integer> endStates) throws PrismException
	{
		//pomdp.exportToDotFile(mainLog);;
		
		mainLog.println("start running episode");
		double[] initialBelief = pomdp.getInitialBeliefInDist();
		int initialState = POMCPDrawStateFromBelief(initialBelief);
		double[] currentBelief = pomdp.getInitialBeliefInDist();
		
		ArrayList<ArrayList<Object>> history = new ArrayList<ArrayList<Object>>();
		int state = initialState;
		double totalReward = 0;
		double discount = 1;
		discount = 1;
		double c = 1;
		double threshold = 0.005;
		double timeout = 10000;
		double noParticles = 1200; 
		ArrayList<Object> allActions = getAllActions(pomdp);
		//Object east = allActions.get(3);
//		public PartiallyObservableMonteCarloPlanning(POMDP pomdp, double gamma, double c, double threshold, double timeout, double noParticles ) 
		PartiallyObservableMonteCarloPlanning pomcp = new PartiallyObservableMonteCarloPlanning(pomdp, mdpRewards, target, min, statesOfInterest, endStates,  discount, c, threshold, timeout, noParticles);
		int step = 0;
		int stepLimit = 1000;
		while (! endStates.contains(state)) {
			// policy <- Search(history)
			if (step > stepLimit) {
				mainLog.println("reaching step limit" + stepLimit);
				break;
			}
			step += 1;
			if(step >= 2) {
				mainLog.println("step = " + step);
			}
			Object action = pomcp.search();

			ArrayList<Double> sord = pomcp.step(state, action);
			int nextState = sord.get(0).intValue();
			int obsSample = sord.get(1).intValue();
			double reward = sord.get(2);
			double done = sord.get(3);
			if (reward == -100) {
				pomcp.search();
			}
			
			pomcp.update(action,  obsSample);
			totalReward += reward;
			mainLog.println("\nStep =============== "+ step + " Cur state");
			pomcp.displayVar();
			mainLog.println("Action = "+ action + " reward = "+ reward + "states after action :");
			pomcp.displayState(nextState);
			
//			mainLog.println("Updated Belief=");
//			pomcp.displayRoot();
		    //pomcp.display();
			state = nextState;
		}
		mainLog.println("totoal reward = " + totalReward);
		return totalReward;
	}
	
	public int POMCPDrawStateFromBelief(double[] belief) 
	{
		int state = 0;
		double randomThreshold = Math.random();
		double cumulativeProb = 0;
		for (int i = 0; i < belief.length; i++) {
			cumulativeProb += belief[i];
			if (cumulativeProb >= randomThreshold) {
				state = i;
				break;
			}
		}
		return state; 
	}
	
	
	public ModelCheckerResult computeReachRewards(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest) throws PrismException
	{
		
		mainLog.println("Calling Perseus pomdp solver");
		computeReachRewardsPerseus( pomdp,  mdpRewards,  target,  min,  statesOfInterest);
		//computeReachRewardsPOMCP( pomdp,  mdpRewards,  target,  min,  statesOfInterest);
		mainLog.println("End calling Perseus pomdp solver");
		
		
		ModelCheckerResult res = null;
		long timer;
		
		
		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}
		
		// Start expected reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");

		// Compute rewards
		res = computeReachRewardsFixedGrid(pomdp, mdpRewards, target, min, statesOfInterest.nextSetBit(0));

		// Finished expected reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.println("Expected reachability took " + timer / 1000.0 + " seconds.");

		// Update time taken
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute expected reachability rewards using Lovejoy's fixed-resolution grid approach.
	 * This only computes the expected reward from a single start state
	 * @param pomdp The POMMDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param min Min or max rewards (true=min, false=max)
	 * @param sInit State to compute for
	 */
	protected ModelCheckerResult computeReachRewardsFixedGrid(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, int sInit) throws PrismException
	{
		// Start fixed-resolution grid approximation
		mainLog.println("calling computeReachRewardsFixedGrid!!!!");
		long timer = System.currentTimeMillis();
		mainLog.println("Starting fixed-resolution grid approximation (" + (min ? "min" : "max") + ")...");

		// Find out the observations for the target states

		BitSet targetObs = getObservationsMatchingStates(pomdp, target);

		if (targetObs == null) {
			throw new PrismException("Target for expected reachability is not observable");
		}
		
		// Find _some_ of the states with infinite reward
		// (those from which *every* MDP strategy has prob<1 of reaching the target,
		// and therefore so does every POMDP strategy)
		MDPModelChecker mcProb1 = new MDPModelChecker(this);
		BitSet inf = mcProb1.prob1(pomdp, null, target, false, null);
		inf.flip(0, pomdp.getNumStates());
		// Find observations for which all states are known to have inf reward
		BitSet infObs = getObservationsCoveredByStates(pomdp, inf);
		mainLog.println("target obs=" + targetObs.cardinality() + ", inf obs=" + infObs.cardinality());
		
		// Determine set of observations actually need to perform computation for
		BitSet unknownObs = new BitSet();
		unknownObs.set(0, pomdp.getNumObservations());
		unknownObs.andNot(targetObs);
		unknownObs.andNot(infObs);

		// Initialise the grid points (just for unknown beliefs)
		List<Belief> gridPoints = initialiseGridPoints(pomdp, unknownObs);
		mainLog.println("Grid statistics: resolution=" + gridResolution + ", points=" + gridPoints.size());
		// Construct grid belief "MDP"
		mainLog.println("Building belief space approximation...");
		List<BeliefMDPState> beliefMDP = buildBeliefMDP(pomdp, mdpRewards, gridPoints);
		
		// Initialise hashmaps for storing values for the unknown belief states
		HashMap<Belief, Double> vhash = new HashMap<>();
		HashMap<Belief, Double> vhash_backUp = new HashMap<>();
		for (Belief belief : gridPoints) {
			vhash.put(belief, 0.0);
			vhash_backUp.put(belief, 0.0);
		}
		// Define value function for the full set of belief states
		Function<Belief, Double> values = belief -> approximateReachReward(belief, vhash_backUp, targetObs, infObs);
		// Define value backup function
		BeliefMDPBackUp backup = (belief, beliefState) -> approximateReachRewardBackup(belief, beliefState, values, min);
		
		// Start iterations
		mainLog.println("Solving belief space approximation...");
		long timer2 = System.currentTimeMillis();
		int iters = 0;
		boolean done = false;
		while (!done && iters < maxIters) {
			// Iterate over all (unknown) grid points
			int unK = gridPoints.size();
			for (int b = 0; b < unK; b++) {
				Belief belief = gridPoints.get(b);
				Pair<Double, Integer> valChoice = backup.apply(belief, beliefMDP.get(b));
				vhash.put(belief, valChoice.first);
			}
			// Check termination
			done = PrismUtils.doublesAreClose(vhash, vhash_backUp, termCritParam, termCrit == TermCrit.RELATIVE);
			// back up	
			Set<Map.Entry<Belief, Double>> entries = vhash.entrySet();
			for (Map.Entry<Belief, Double> entry : entries) {
				vhash_backUp.put(entry.getKey(), entry.getValue());
			}
			iters++;
		}

		// Non-convergence is an error (usually)
		if (!done && errorOnNonConverge) {
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}
		timer2 = System.currentTimeMillis() - timer2;
		mainLog.print("Belief space value iteration (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + iters + " iterations and " + timer2 / 1000.0 + " seconds.");

		// Extract (approximate) solution value for the initial belief
		// Also get (approximate) accuracy of result from value iteration
		Belief initialBelief = Belief.pointDistribution(sInit, pomdp);
		double outerBound = values.apply(initialBelief);
		double outerBoundMaxDiff = PrismUtils.measureSupNorm(vhash, vhash_backUp, termCrit == TermCrit.RELATIVE);
		Accuracy outerBoundAcc = AccuracyFactory.valueIteration(termCritParam, outerBoundMaxDiff, termCrit == TermCrit.RELATIVE);
		// Print result
		mainLog.println("Outer bound: " + outerBound + " (" + outerBoundAcc.toString(outerBound) + ")");
			
		// Build DTMC to get inner bound (and strategy)
		mainLog.println("\nBuilding strategy-induced model...");
		POMDPStrategyModel psm = buildStrategyModel(pomdp, sInit, mdpRewards, targetObs, unknownObs, backup);
		MDP mdp = psm.mdp;
		MDPRewards mdpRewardsNew = liftRewardsToStrategyModel(pomdp, mdpRewards, psm);
		mainLog.print("Strategy-induced model: " + mdp.infoString());
		
		// Export strategy if requested
		// NB: proper storage of strategy for genStrat not yet supported,
		// so just treat it as if -exportadv had been used, with default file (adv.tra)
		if (genStrat || exportAdv) {
			// Export in Dot format if filename extension is .dot
			if (exportAdvFilename.endsWith(".dot")) {
				mdp.exportToDotFile(exportAdvFilename, Collections.singleton(new Decorator()
				{
					@Override
					public Decoration decorateState(int state, Decoration d)
					{
						d.labelAddBelow(psm.beliefs.get(state).toString(pomdp));
						return d;
					}
				}));
			}
			// Otherwise use .tra format
			else {
				mdp.exportToPrismExplicitTra(exportAdvFilename);
			}
		}

		// Create MDP model checker (disable strat generation - if enabled, we want the POMDP one) 
		MDPModelChecker mcMDP = new MDPModelChecker(this);
		mcMDP.setExportAdv(false);
		mcMDP.setGenStrat(false);
		// Solve MDP to get inner bound
		ModelCheckerResult mcRes = mcMDP.computeReachRewards(mdp, mdpRewardsNew, mdp.getLabelStates("target"), true);
		double innerBound = mcRes.soln[0];
		Accuracy innerBoundAcc = mcRes.accuracy;
		// Print result
		String innerBoundStr = "" + innerBound;
		if (innerBoundAcc != null) {
			innerBoundStr += " (" + innerBoundAcc.toString(innerBound) + ")";
		}
		mainLog.println("Inner bound: " + innerBoundStr);

		// Finished fixed-resolution grid approximation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("\nFixed-resolution grid approximation (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + timer / 1000.0 + " seconds.");

		// Extract and store result
		Pair<Double,Accuracy> resultValAndAcc;
		if (min) {
			resultValAndAcc = AccuracyFactory.valueAndAccuracyFromInterval(outerBound, outerBoundAcc, innerBound, innerBoundAcc);
		} else {
			resultValAndAcc = AccuracyFactory.valueAndAccuracyFromInterval(innerBound, innerBoundAcc, outerBound, outerBoundAcc);
		}
		double resultVal = resultValAndAcc.first;
		Accuracy resultAcc = resultValAndAcc.second;
		mainLog.println("Result bounds: [" + resultAcc.getResultLowerBound(resultVal) + "," + resultAcc.getResultUpperBound(resultVal) + "]");
		double soln[] = new double[pomdp.getNumStates()];
		soln[sInit] = resultVal;

		// Return results
		ModelCheckerResult res = new ModelCheckerResult();
		res.soln = soln;
		res.accuracy = resultAcc;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute weighted multi-objective expected reachability rewards,
	 * i.e. compute the min/max weighted multi-objective reward accumulated to reach a state in {@code target}.
	 * @param pomdp The POMDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
	public ModelCheckerResult computeMultiReachRewards(POMDP pomdp, List<Double> weights, List<MDPRewards> mdpRewardsList, BitSet target, boolean min, BitSet statesOfInterest) throws PrismException
	{
		ModelCheckerResult res = null;
		long timer;

		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}
		
		// Start expected reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");

//		// Compute rewards
//		res = computeMultiReachRewardsFixedGrid(pomdp, weights, mdpRewardsList, target, min, statesOfInterest.nextSetBit(0));
//
//		// Finished expected reachability
//		timer = System.currentTimeMillis() - timer;
//		mainLog.println("Expected reachability took " + timer / 1000.0 + " seconds.");
//
//		// Update time taken
//		res.timeTaken = timer / 1000.0;
		
		/////202203 temp code; for verification of POMCP algorithms
		// Build a combined reward structure
		int numRewards = weights.size();
		WeightedSumMDPRewards mdpRewardsWeighted = new WeightedSumMDPRewards();
		for (int i = 0; i < numRewards; i++) {
			mdpRewardsWeighted.addRewards(weights.get(i), mdpRewardsList.get(i));
		}
		computeReachRewardsPOMCP(pomdp, mdpRewardsWeighted, target, min, statesOfInterest);
		////////////
		
		
		return res;
	}

	/**
	 * Compute weighted multi-objective expected reachability rewards using Lovejoy's fixed-resolution grid approach.
	 * This only computes the weighted multi-objective expected reward from a single start state
	 * @param pomdp The POMMDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param min Min or max rewards (true=min, false=max)
	 * @param sInit State to compute for
	 */
	protected ModelCheckerResult computeMultiReachRewardsFixedGrid(POMDP pomdp, List<Double> weights, List<MDPRewards> mdpRewardsList, BitSet target, boolean min, int sInit) throws PrismException
	{
		// Start fixed-resolution grid approximation
		long timer = System.currentTimeMillis();
		mainLog.println("calling computeMultiReachRewardsFixedGrid!!!");
		mainLog.println("Starting fixed-resolution grid approximation (" + (min ? "min" : "max") + ")...");



		// Find out the observations for the target states
		BitSet targetObs = getObservationsMatchingStates(pomdp, target);
		
		mainLog.println("target states obs"+targetObs.size());
		

		
		if (targetObs == null) {
			throw new PrismException("Target for expected reachability is not observable");
		}
		
		// Find _some_ of the states with infinite reward
		// (those from which *every* MDP strategy has prob<1 of reaching the target,
		// and therefore so does every POMDP strategy)
		MDPModelChecker mcProb1 = new MDPModelChecker(this);
		BitSet inf = mcProb1.prob1(pomdp, null, target, false, null);
		inf.flip(0, pomdp.getNumStates());
		// Find observations for which all states are known to have inf reward
		BitSet infObs = getObservationsCoveredByStates(pomdp, inf);
		mainLog.println("target obs=" + targetObs.cardinality() + ", inf obs=" + infObs.cardinality());
		
		// Determine set of observations actually need to perform computation for
		BitSet unknownObs = new BitSet();

		unknownObs.set(0, pomdp.getNumObservations());
		unknownObs.andNot(targetObs);
		unknownObs.andNot(infObs);

		// Build a combined reward structure
		int numRewards = weights.size();
		WeightedSumMDPRewards mdpRewardsWeighted = new WeightedSumMDPRewards();
		for (int i = 0; i < numRewards; i++) {
			mdpRewardsWeighted.addRewards(weights.get(i), mdpRewardsList.get(i));
		}
		
		// Initialise the grid points (just for unknown beliefs)
		List<Belief> gridPoints = initialiseGridPoints(pomdp, unknownObs);
		mainLog.println("Grid statistics: resolution=" + gridResolution + ", points=" + gridPoints.size());
		/*
		for(int q=0; q<gridPoints.size();q++) {
			mainLog.println(q);
			mainLog.println("index"+gridPoints.get(q).so);
			mainLog.println(gridPoints.get(q).bu);
		}*/
		// Construct grid belief "MDP"
		mainLog.println("Building belief space approximation...");
		List<BeliefMDPState> beliefMDP = buildBeliefMDP(pomdp, mdpRewardsWeighted, gridPoints);
		
		// Initialise hashmaps for storing values for the unknown belief states
		HashMap<Belief, Double> vhash = new HashMap<>();
		HashMap<Belief, Double> vhash_backUp = new HashMap<>();
		for (Belief belief : gridPoints) {
			vhash.put(belief, 0.0);
			vhash_backUp.put(belief, 0.0);
		}
		// Define value function for the full set of belief states
		Function<Belief, Double> values = belief -> approximateReachReward(belief, vhash_backUp, targetObs, infObs);
		// Define value backup function
		BeliefMDPBackUp backup = (belief, beliefState) -> approximateReachRewardBackup(belief, beliefState, values, min);
		
		
		
		// Start iterations
		mainLog.println("Solving belief space approximation...");
		long timer2 = System.currentTimeMillis();
		int iters = 0;
		boolean done = false;
		while (!done && iters < maxIters) {
			// Iterate over all (unknown) grid points
			int unK = gridPoints.size();

			for (int b = 0; b < unK; b++) {
				Belief belief = gridPoints.get(b);
				Pair<Double, Integer> valChoice = backup.apply(belief, beliefMDP.get(b));
				vhash.put(belief, valChoice.first);				//updating vhash, not vhash_backup
				}
			// Check termination
			done = PrismUtils.doublesAreClose(vhash, vhash_backUp, termCritParam, termCrit == TermCrit.RELATIVE);
			// back up	
			Set<Map.Entry<Belief, Double>> entries = vhash.entrySet();
			for (Map.Entry<Belief, Double> entry : entries) {
				vhash_backUp.put(entry.getKey(), entry.getValue());
			}
			iters++;
		}
		/*
		mainLog.println("vhash");
		for (Object key:  vhash.keySet()) {
			mainLog.println(key);
			mainLog.println(vhash.get(key));
		}
		mainLog.println("vhash_backup");
		for (Object key:  vhash_backUp.keySet()) {
			mainLog.println(key);
			mainLog.println(vhash_backUp.get(key));
		}*/
		// Non-convergence is an error (usually)
		if (!done && errorOnNonConverge) {
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}
		timer2 = System.currentTimeMillis() - timer2;
		mainLog.print("Belief space value iteration (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + iters + " iterations and " + timer2 / 1000.0 + " seconds.");

		// Extract (approximate) solution value for the initial belief
		// Also get (approximate) accuracy of result from value iteration
		Belief initialBelief = Belief.pointDistribution(sInit, pomdp);
		
		double outerBound = values.apply(initialBelief);
		double outerBoundMaxDiff = PrismUtils.measureSupNorm(vhash, vhash_backUp, termCrit == TermCrit.RELATIVE);
		Accuracy outerBoundAcc = AccuracyFactory.valueIteration(termCritParam, outerBoundMaxDiff, termCrit == TermCrit.RELATIVE);
		// Print result
		mainLog.println("Outer bound: " + outerBound + " (" + outerBoundAcc.toString(outerBound) + ")");
		
		// Build DTMC to get inner bound (and strategy)
		mainLog.println("\nBuilding strategy-induced model...");
		POMDPStrategyModel psm = buildStrategyModel(pomdp, sInit, mdpRewardsWeighted, targetObs, unknownObs, backup);
		MDP mdp = psm.mdp;
		MDPRewards mdpRewardsWeightedNew = liftRewardsToStrategyModel(pomdp, mdpRewardsWeighted, psm);
		List<MDPRewards> mdpRewardsListNew = new ArrayList<>();
		for (MDPRewards mdpRewards : mdpRewardsList) {
			mdpRewardsListNew.add(liftRewardsToStrategyModel(pomdp, mdpRewards, psm));
		}
		mainLog.print("Strategy-induced model: " + mdp.infoString());
		
		// Export strategy if requested
		// NB: proper storage of strategy for genStrat not yet supported,
		// so just treat it as if -exportadv had been used, with default file (adv.tra)
		if (genStrat || exportAdv) {
			// Export in Dot format if filename extension is .dot
			if (exportAdvFilename.endsWith(".dot")) {
				mdp.exportToDotFile(exportAdvFilename, Collections.singleton(new Decorator()
				{
					@Override
					public Decoration decorateState(int state, Decoration d)
					{
						d.labelAddBelow(psm.beliefs.get(state).toString(pomdp));
						return d;
					}
				}));
			}
			// Otherwise use .tra format
			else {
				mdp.exportToPrismExplicitTra(exportAdvFilename);
			}
		}

		
		// Create MDP model checker (disable strat generation - if enabled, we want the POMDP one) 
		MDPModelChecker mcMDP = new MDPModelChecker(this);
		mcMDP.setExportAdv(false);
		mcMDP.setGenStrat(false);
		// Solve MDP to get inner bound
		List<Double> point = new ArrayList<>();
		
		//get inner bound
		ModelCheckerResult mcRes = mcMDP.computeReachRewards(mdp, mdpRewardsWeightedNew, mdp.getLabelStates("target"), true);
		//get inner bound
		
		for (MDPRewards mdpRewards : mdpRewardsListNew) {
			//get value for each obs
			ModelCheckerResult mcResTmp = mcMDP.computeReachRewards(mdp, mdpRewards, mdp.getLabelStates("target"), true);
			//get value for each obs
			
			mainLog.println(mcResTmp.soln);
			
			point.add(mcResTmp.soln[0]);
		}
		double innerBound = mcRes.soln[0];
		Accuracy innerBoundAcc = mcRes.accuracy;
		// Print result
		String innerBoundStr = "" + innerBound;
		if (innerBoundAcc != null) {
			innerBoundStr += " (" + innerBoundAcc.toString(innerBound) + ")";
		}
		mainLog.println("Inner bound: " + innerBoundStr);

		// Finished fixed-resolution grid approximation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("\nFixed-resolution grid approximation (" + (min ? "min" : "max") + ")");
		mainLog.println(" took " + timer / 1000.0 + " seconds.");

		// Extract and store result
		Pair<Double,Accuracy> resultValAndAcc;
		if (min) {
			resultValAndAcc = AccuracyFactory.valueAndAccuracyFromInterval(outerBound, outerBoundAcc, innerBound, innerBoundAcc);
		} else {
			resultValAndAcc = AccuracyFactory.valueAndAccuracyFromInterval(innerBound, innerBoundAcc, outerBound, outerBoundAcc);
		}
		double resultVal = resultValAndAcc.first;
		Accuracy resultAcc = resultValAndAcc.second;
		mainLog.println("Result bounds: [" + resultAcc.getResultLowerBound(resultVal) + "," + resultAcc.getResultUpperBound(resultVal) + "]");
		Object solnObj[] = new Object[pomdp.getNumStates()];
		solnObj[sInit] = point; //resultVal;

		// Return results
		ModelCheckerResult res = new ModelCheckerResult();
		res.solnObj = solnObj;
		res.accuracy = resultAcc;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Get a list of observations from a set of states
	 * (both are represented by BitSets over their indices).
	 * The states should correspond exactly to a set of observations,
	 * i.e., if a state corresponding to an observation is in the set,
	 * then all other states corresponding to it should also be.
	 * Returns null if not.
	 */
	protected BitSet getObservationsMatchingStates(POMDP pomdp, BitSet set)
	{
		// Find observations corresponding to each state in the set
		BitSet setObs = new BitSet();
		for (int s = set.nextSetBit(0); s >= 0; s = set.nextSetBit(s + 1)) {
			setObs.set(pomdp.getObservation(s)); 
		}
		// Recreate the set of states from the observations and make sure it matches
		BitSet set2 = new BitSet();
		int numStates = pomdp.getNumStates();
		for (int s = 0; s < numStates; s++) {
			if (setObs.get(pomdp.getObservation(s))) { 
				set2.set(s);
			}
		}
		if (!set.equals(set2)) {
			return null;
		}
		return setObs;
	}
	
	/**
	 * Get a list of observations from a set of states
	 * (both are represented by BitSets over their indices).
	 * Observations are included only if all their corresponding states
	 * are included in the passed in set.
	 */
	protected BitSet getObservationsCoveredByStates(POMDP pomdp, BitSet set) throws PrismException
	{
		// Find observations corresponding to each state in the set
		BitSet setObs = new BitSet();
		for (int s = set.nextSetBit(0); s >= 0; s = set.nextSetBit(s + 1)) {
			setObs.set(pomdp.getObservation(s));
		}
		// Find observations for which not all states are in the set
		// and remove them from the observation set to be returned
		int numStates = pomdp.getNumStates();
		for (int o = setObs.nextSetBit(0); o >= 0; o = setObs.nextSetBit(o + 1)) {
			for (int s = 0; s < numStates; s++) {
				if (pomdp.getObservation(s) == o && !set.get(s)) {
					setObs.set(o, false);
					break;
				}
			}


		}
		return setObs;
	}
	
	/**
	 * Construct a list of beliefs for a grid-based approximation of the belief space.
	 * Only beliefs with observable values from {@code unknownObs) are added.
	 */
	protected List<Belief> initialiseGridPoints(POMDP pomdp, BitSet unknownObs)
	{
		List<Belief> gridPoints = new ArrayList<>();
		ArrayList<ArrayList<Double>> assignment;
		int numUnobservations = pomdp.getNumUnobservations();
		int numStates = pomdp.getNumStates();

		for (int so = unknownObs.nextSetBit(0); so >= 0; so = unknownObs.nextSetBit(so + 1)) {

			ArrayList<Integer> unobservsForObserv = new ArrayList<>();
			for (int s = 0; s < numStates; s++) {
				if (so == pomdp.getObservation(s)) {
					unobservsForObserv.add(pomdp.getUnobservation(s));
				}
			}

			assignment = fullAssignment(unobservsForObserv.size(), gridResolution);
			for (ArrayList<Double> inner : assignment) {
				double[] bu = new double[numUnobservations];
				int k = 0;
				for (int unobservForObserv : unobservsForObserv) {
					bu[unobservForObserv] = inner.get(k);
					k++;
				}
				gridPoints.add(new Belief(so, bu));
			}
		}
		return gridPoints;
	}
	
	/**
	 * Construct (part of) a belief MDP, just for the set of passed in belief states.
	 * If provided, also construct a list of rewards for each state.
	 * It is stored as a list (over source beliefs) of BeliefMDPState objects.
	 */
	protected List<BeliefMDPState> buildBeliefMDP(POMDP pomdp, MDPRewards mdpRewards, List<Belief> beliefs)
	{
		List<BeliefMDPState> beliefMDP = new ArrayList<>();
		for (Belief belief: beliefs) {
			beliefMDP.add(buildBeliefMDPState(pomdp, mdpRewards, belief));
		}
		return beliefMDP;
	}
	
	/**
	 * Construct a single single state (belief) of a belief MDP, stored as a
	 * list (over choices) of distributions over target beliefs.
	 * If provided, also construct a list of rewards for the state.
	 * It is stored as a BeliefMDPState object.
	 */
	protected BeliefMDPState buildBeliefMDPState(POMDP pomdp, MDPRewards mdpRewards, Belief belief)
	{

		double[] beliefInDist = belief.toDistributionOverStates(pomdp);


		BeliefMDPState beliefMDPState = new BeliefMDPState();
		// And for each choice
		
		int numChoices = pomdp.getNumChoicesForObservation(belief.so);

		for (int i = 0; i < numChoices; i++) {
			// Get successor observations and their probs
			HashMap<Integer, Double> obsProbs = pomdp.computeObservationProbsAfterAction(beliefInDist, i);
			HashMap<Belief, Double> beliefDist = new HashMap<>();
			// Find the belief for each observation
			for (Map.Entry<Integer, Double> entry : obsProbs.entrySet()) {
				int o = entry.getKey();
				Belief nextBelief = pomdp.getBeliefAfterChoiceAndObservation(belief, i, o);
				//mainLog.println("Next Belief"+nextBelief);
				beliefDist.put(nextBelief, entry.getValue());
			}
			beliefMDPState.trans.add(beliefDist);
			// Store reward too, if required
			if (mdpRewards != null) {
				beliefMDPState.rewards.add(pomdp.getRewardAfterChoice(belief, i, mdpRewards));
			}
		}
		return beliefMDPState;
	}
	
	/**
	 * Perform a single backup step of (approximate) value iteration for probabilistic reachability
	 */
	protected Pair<Double, Integer> approximateReachProbBackup(Belief belief, BeliefMDPState beliefMDPState, Function<Belief, Double> values, boolean min)
	{
		int numChoices = beliefMDPState.trans.size();
		double chosenValue = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		int chosenActionIndex = -1;
		for (int i = 0; i < numChoices; i++) {
			double value = 0;
			for (Map.Entry<Belief, Double> entry : beliefMDPState.trans.get(i).entrySet()) {
				double nextBeliefProb = entry.getValue();
				Belief nextBelief = entry.getKey();
				value += nextBeliefProb * values.apply(nextBelief);
			}
			if ((min && chosenValue - value > 1.0e-6) || (!min && value - chosenValue > 1.0e-6)) {
				chosenValue = value;
				chosenActionIndex = i;
			} else if (Math.abs(value - chosenValue) < 1.0e-6) {
				chosenActionIndex = i;
			}
		}
		return new Pair<Double, Integer>(chosenValue, chosenActionIndex);
	}
	
	/**
	 * Perform a single backup step of (approximate) value iteration for reward reachability
	 */
	protected Pair<Double, Integer> approximateReachRewardBackup(Belief belief, BeliefMDPState beliefMDPState, Function<Belief, Double> values, boolean min)
	{
		int numChoices = beliefMDPState.trans.size();
		double chosenValue = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		int chosenActionIndex = 0;
		for (int i = 0; i < numChoices; i++) {
			double value = beliefMDPState.rewards.get(i);
			for (Map.Entry<Belief, Double> entry : beliefMDPState.trans.get(i).entrySet()) {
				double nextBeliefProb = entry.getValue();
				Belief nextBelief = entry.getKey();
				value += nextBeliefProb * values.apply(nextBelief);
			}
			if ((min && chosenValue - value > 1.0e-6) || (!min && value - chosenValue > 1.0e-6)) {
				chosenValue = value;
				chosenActionIndex = i;
			} else if (Math.abs(value - chosenValue) < 1.0e-6) {
				chosenActionIndex = i;
			}
		}
		return new Pair<Double, Integer>(chosenValue, chosenActionIndex);
	}
	
	/**
	 * Compute the grid-based approximate value for a belief for probabilistic reachability
	 */
	protected double approximateReachProb(Belief belief, HashMap<Belief, Double> gridValues, BitSet targetObs, BitSet unknownObs)
	{
		// 1 for target states
		if (targetObs.get(belief.so)) {
			return 1.0;
		}
		// 0 for other non-unknown states
		else if (!unknownObs.get(belief.so)) {
			return 0.0;
		}
		// Otherwise approximate vie interpolation over grid points
		else {
			return interpolateOverGrid(belief, gridValues);
		}
	}
	
	/**
	 * Compute the grid-based approximate value for a belief for reward reachability
	 */
	protected double approximateReachReward(Belief belief, HashMap<Belief, Double> gridValues, BitSet targetObs, BitSet infObs)
	{
		// 0 for target states
		if (targetObs.get(belief.so)) {
			return 0.0;
		}
		// +Inf for states in "inf"
		else if (infObs.get(belief.so)) {
			return Double.POSITIVE_INFINITY;
		}
		// Otherwise approximate vie interpolation over grid points
		else {
			return interpolateOverGrid(belief, gridValues);
		}
	}
	
	/**
	 * Approximate the value for a belief {@code belief} by interpolating over values {@code gridValues}
	 * for a representative set of beliefs whose convex hull is the full belief space.
	 */
	protected double interpolateOverGrid(Belief belief, HashMap<Belief, Double> gridValues)
	{
		ArrayList<double[]> subSimplex = new ArrayList<>();
		double[] lambdas = new double[belief.bu.length];
		getSubSimplexAndLambdas(belief.bu, subSimplex, lambdas, gridResolution);
		double val = 0;
		for (int j = 0; j < lambdas.length; j++) {
			if (lambdas[j] >= 1e-6) {
				val += lambdas[j] * gridValues.get(new Belief(belief.so, subSimplex.get(j)));
			}
		}
		return val;
	}
	
	/**
	 * Build a (Markov chain) model representing the fragment of the belief MDP induced by an optimal strategy.
	 * The model is stored as an MDP to allow easier attachment of optional actions.
	 * @param pomdp
	 * @param sInit
	 * @param mdpRewards
	 * @param vhash
	 * @param vhash_backUp
	 * @param target
	 * @param min
	 * @param listBeliefs
	 */
	protected POMDPStrategyModel buildStrategyModel(POMDP pomdp, int sInit, MDPRewards mdpRewards, BitSet targetObs, BitSet unknownObs, BeliefMDPBackUp backup) throws PrismException
	{
		// Initialise model/strat/state storage
		MDPSimple mdp = new MDPSimple();
		List<Integer> strat = new ArrayList<>();
		IndexedSet<Belief> exploredBeliefs = new IndexedSet<>(true);
		LinkedList<Belief> toBeExploredBeliefs = new LinkedList<>();
		BitSet mdpTarget = new BitSet();
		// Add initial state
		Belief initialBelief = Belief.pointDistribution(sInit, pomdp);
		exploredBeliefs.add(initialBelief);
		toBeExploredBeliefs.offer(initialBelief);
		mdp.addState();
		mdp.addInitialState(0);
		
		// Explore model
		int src = -1;
		while (!toBeExploredBeliefs.isEmpty()) {
			Belief belief = toBeExploredBeliefs.pollFirst();
			src++;
			// Remember if this is a target state
			if (targetObs.get(belief.so)) {
				mdpTarget.set(src);
			}
			// Only explore "unknown" states
			if (unknownObs.get(belief.so)) {
				// Build the belief MDP for this belief state and solve
				BeliefMDPState beliefMDPState = buildBeliefMDPState(pomdp, mdpRewards, belief);
				Pair<Double, Integer> valChoice = backup.apply(belief, beliefMDPState);
				int chosenActionIndex = valChoice.second;
				// Build a distribution over successor belief states and add to MDP
				Distribution distr = new Distribution();
				for (Map.Entry<Belief, Double> entry : beliefMDPState.trans.get(chosenActionIndex).entrySet()) {
					double nextBeliefProb = entry.getValue();
					Belief nextBelief = entry.getKey();
					// Add each successor belief to the MDP and the "to explore" set if new
					if (exploredBeliefs.add(nextBelief)) {
						toBeExploredBeliefs.add(nextBelief);
						mdp.addState();
					}
					// Get index of state in state set
					int dest = exploredBeliefs.getIndexOfLastAdd();
					distr.add(dest, nextBeliefProb);
				}
				// Add transition distribution, with optimal choice action attached
				mdp.addActionLabelledChoice(src, distr, pomdp.getActionForObservation(belief.so, chosenActionIndex));
				// Also remember the optimal choice index for later use
				strat.add(chosenActionIndex);
			} else {
				// No transition so store dummy choice index
				strat.add(-1);
			}
		}
		// Add deadlocks to unexplored (known-value) states
		mdp.findDeadlocks(true);
		// Attach a label marking target states
		mdp.addLabel("target", mdpTarget);
		// Return
		POMDPStrategyModel psm = new POMDPStrategyModel();
		psm.mdp = mdp;
		psm.strat = strat;
		psm.beliefs = new ArrayList<>();
		psm.beliefs.addAll(exploredBeliefs.toArrayList());
		return psm;
	}
	
	/**
	 * Construct a reward structure for the model representing the fragment of the belief MDP
	 * that is induced by an optimal strategy, from a reward structure for the original POMDP.
	 */
	MDPRewards liftRewardsToStrategyModel(POMDP pomdp, MDPRewards mdpRewards, POMDPStrategyModel psm)
	{
		// Markov chain so just store as state rewards
		StateRewardsSimple stateRewards = new StateRewardsSimple();
		int numStates = psm.mdp.getNumStates();
		for (int s = 0; s < numStates; s++) {
			Belief belief = psm.beliefs.get(s);
			int ch = psm.strat.get(s);
			// Zero reward if no transitions; otherwise compute from belief
			double rew = ch == -1 ? 0.0 : pomdp.getRewardAfterChoice(belief, ch, mdpRewards);
			stateRewards.setStateReward(s, rew);
		}
		return stateRewards;
	}
	
	protected ArrayList<ArrayList<Integer>> assignGPrime(int startIndex, int min, int max, int length)
	{
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		if (startIndex == length - 1) {
			for (int i = min; i <= max; i++) {
				ArrayList<Integer> innerList = new ArrayList<>();
				innerList.add(i);
				result.add(innerList);
			}
		} else {
			for (int i = min; i <= max; i++) {
				ArrayList<ArrayList<Integer>> nextResult = assignGPrime(startIndex + 1, 0, i, length);
				for (ArrayList<Integer> nextReulstInner : nextResult) {
					ArrayList<Integer> innerList = new ArrayList<>();
					innerList.add(i);
					for (Integer a : nextReulstInner) {
						innerList.add(a);
					}
					result.add(innerList);
				}
			}
		}

		return result;
	}

	private ArrayList<ArrayList<Double>> fullAssignment(int length, int resolution)
	{
		ArrayList<ArrayList<Integer>> GPrime = assignGPrime(0, resolution, resolution, length);
		ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
		for (ArrayList<Integer> GPrimeInner : GPrime) {
			ArrayList<Double> innerList = new ArrayList<>();
			int i;
			for (i = 0; i < length - 1; i++) {
				int temp = GPrimeInner.get(i) - GPrimeInner.get(i + 1);
				innerList.add((double) temp / resolution);
			}
			innerList.add((double) GPrimeInner.get(i) / resolution);
			result.add(innerList);
		}
		return result;
	}

	private int[] getSortedPermutation(double[] inputArray)
	{
		int n = inputArray.length;
		double[] inputCopy = new double[n];
		int[] permutation = new int[n];
		int iState = 0, iIteration = 0;
		int iNonZeroEntry = 0, iZeroEntry = n - 1;
		boolean bDone = false;

		for (iState = n - 1; iState >= 0; iState--) {
			if (inputArray[iState] == 0.0) {
				inputCopy[iZeroEntry] = 0.0;
				permutation[iZeroEntry] = iState;
				iZeroEntry--;
			}

		}

		for (iState = 0; iState < n; iState++) {
			if (inputArray[iState] != 0.0) {
				inputCopy[iNonZeroEntry] = inputArray[iState];
				permutation[iNonZeroEntry] = iState;
				iNonZeroEntry++;
			}
		}

		while (!bDone) {
			bDone = true;
			for (iState = 0; iState < iNonZeroEntry - iIteration - 1; iState++) {
				if (inputCopy[iState] < inputCopy[iState + 1]) {
					swap(inputCopy, iState, iState + 1);
					swap(permutation, iState, iState + 1);
					bDone = false;
				}
			}
			iIteration++;
		}

		return permutation;
	}

	private void swap(int[] aiArray, int i, int j)
	{
		int temp = aiArray[i];
		aiArray[i] = aiArray[j];
		aiArray[j] = temp;
	}

	private void swap(double[] aiArray, int i, int j)
	{
		double temp = aiArray[i];
		aiArray[i] = aiArray[j];
		aiArray[j] = temp;
	}

	protected boolean getSubSimplexAndLambdas(double[] b, ArrayList<double[]> subSimplex, double[] lambdas, int resolution)
	{
		int n = b.length;
		int M = resolution;

		double[] X = new double[n];
		int[] V = new int[n];
		double[] D = new double[n];
		for (int i = 0; i < n; i++) {
			X[i] = 0;
			for (int j = i; j < n; j++) {
				X[i] += M * b[j];
			}
			X[i] = Math.round(X[i] * 1e6) / 1e6;
			V[i] = (int) Math.floor(X[i]);
			D[i] = X[i] - V[i];
		}

		int[] P = getSortedPermutation(D);
		//		mainLog.println("X: "+ Arrays.toString(X));
		//		mainLog.println("V: "+ Arrays.toString(V));
		//		mainLog.println("D: "+ Arrays.toString(D));
		//		mainLog.println("P: "+ Arrays.toString(P));

		ArrayList<int[]> Qs = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			int[] Q = new int[n];
			if (i == 0) {
				for (int j = 0; j < n; j++) {
					Q[j] = V[j];
				}
				Qs.add(Q);
			} else {
				for (int j = 0; j < n; j++) {
					if (j == P[i - 1]) {
						Q[j] = Qs.get(i - 1)[j] + 1;
					} else {
						Q[j] = Qs.get(i - 1)[j];
					}

				}
				Qs.add(Q);
			}
			//			mainLog.println(Arrays.toString(Q));
		}

		for (int[] Q : Qs) {
			double[] node = new double[n];
			int i;
			for (i = 0; i < n - 1; i++) {
				int temp = Q[i] - Q[i + 1];
				node[i] = (double) temp / M;
			}
			node[i] = (double) Q[i] / M;
			subSimplex.add(node);
		}

		double sum = 0;
		for (int i = 1; i < n; i++) {
			double lambda = D[P[i - 1]] - D[P[i]];
			lambdas[i] = lambda;
			sum = sum + lambda;
		}
		lambdas[0] = 1 - sum;

		for (int i = 0; i < n; i++) {
			double sum2 = 0;
			for (int j = 0; j < n; j++) {
				sum2 += lambdas[j] * subSimplex.get(j)[i];
			}
			//			mainLog.println("b["+i+"]: "+b[i]+"  b^[i]:"+sum2);
			if (Math.abs(b[i] - sum2) > 1e-4) {
				return false;
			}

		}
		return true;
	}

	public static boolean isTargetBelief(double[] belief, BitSet target)
	{
		 double prob=0;
		 for (int i = target.nextSetBit(0); i >= 0; i = target.nextSetBit(i+1)) 
		 {
			 prob+=belief[i];
		 }
		 if(Math.abs(prob-1.0)<1.0e-6)
		 {
			 return true;
		 }
		 return false;
	}	

	/**
	 * Simple test program.
	 */
	public static void main(String args[])
	{
		POMDPModelChecker mc;
		POMDPSimple pomdp;
		ModelCheckerResult res;
		BitSet init, target;
		Map<String, BitSet> labels;
		boolean min = true;
		try {
			mc = new POMDPModelChecker(null);
			MDPSimple mdp = new MDPSimple();
			mdp.buildFromPrismExplicit(args[0]);
			//mainLog.println(mdp);
			labels = mc.loadLabelsFile(args[1]);
			//mainLog.println(labels);
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
			pomdp = new POMDPSimple(mdp);
			res = mc.computeReachRewards(pomdp, null, target, min, null);
			System.out.println(res.soln[init.nextSetBit(0)]);
		} catch (PrismException e) {
			System.out.println(e);
		}
	}
}


