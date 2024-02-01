package explicit;

import common.IntSet;
import common.iterable.FunctionalIterator;
import common.iterable.Reducible;
import explicit.rewards.MDPRewards;
import explicit.rewards.StateRewardsArray;
import param.BigRational;
import param.Function;
import parser.ast.Expression;
import prism.Evaluator;
import param.Point;
import prism.Pair;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismSettings;
import strat.MDStrategy;
import strat.MDStrategyArray;
import strat.StrategyInfo;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PrimitiveIterator;

public class MDPModelCheckerDistributional extends ProbModelChecker
{
	/**
	 * Create a new MDPModelCheckerDistributional, inherit basic state from parent (unless null).
	 */
	public MDPModelCheckerDistributional(PrismComponent parent) throws PrismException
	{
		super(parent);
	}

	public ModelCheckerResult computeReachRewardsExample(MDP<Function> mdp, MDPRewards<Function> mdpRewards, BitSet target, boolean min) throws PrismException
	{
		Point paramValues = new Point(new BigRational[]{new BigRational("0.8")});

		// Print out MDP (before and after parameter instantiation)
		System.out.println(mdp);
		int numStates = mdp.getNumStates();
		for (int s = 0; s < numStates; s++) {
			int numChoices = mdp.getNumChoices(s);
			for (int i = 0; i < numChoices; i++) {
				//Iterator<Map.Entry<Integer, Function>> iter = mdp.getTransitionsIterator(s, i);
				FunctionalIterator<Map.Entry<Integer, Double>> iter = mdp.getTransitionsMappedIterator(s, i, p -> p.evaluate(paramValues).doubleValue());
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> e = iter.next();
					mainLog.println(s + "," + mdp.getAction(s, i) + ":" + e.getKey() + "=" + e.getValue());
				}
			}
		}

		// Print out rewards
		for (int s = 0; s < numStates; s++) {
			double rewS = mdpRewards.getStateReward(s).evaluate(paramValues).doubleValue();
			mainLog.println(s + ":" + rewS);
			int numChoices = mdp.getNumChoices(s);
			for (int i = 0; i < numChoices; i++) {
				double rewA = mdpRewards.getTransitionReward(s, i).evaluate(paramValues).doubleValue();
				mainLog.println(s + "," + mdp.getAction(s, i) + ":" + rewA);
			}
		}

		// Dummy result
		ModelCheckerResult res = new ModelCheckerResult();
		res.solnObj = new Object[mdp.getNumStates()];
		res.solnObj[0] = 99.0;
		return res;
	}

	public ModelCheckerResult computeReachRewards(MDP<Function> mdp, MDPRewards<Function> mdpRewards, BitSet target, boolean min) throws PrismException
	{
		MDPModelChecker mcMDP = new MDPModelChecker(this);
		mcMDP.inheritSettings(this);

		// Start expected reachability
		long timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		mdp.checkForDeadlocks(target);

		// Store num states
		int n = mdp.getNumStates();

		// Precomputation (not optional)
		long timerProb1 = System.currentTimeMillis();
		BitSet inf = mcMDP.prob1(mdp, null, target, !min, null);
		inf.flip(0, n);
//		timerProb1 = System.currentTimeMillis() - timerProb1;

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
		int min_iter = 8;
		double error_thresh = 0.01;
		double gamma = 1;
		double alpha=0.5;
		Double dtmc_epsilon = null;
		boolean check_reach_dtmc = false;
		boolean check_reach_dtmc_distr = true;
		boolean check_reach_dtmc_vi = false;
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
		DistributionalBellmanOperatorProb operator, temp_p; DiscreteDistribution save_p;
		String distr_type = settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD);

		if (distr_type.equals(c51) || distr_type.equals(qr)) {
			// TODO remove this in final version
			ArrayList<String []> params = mcMDP.readParams(null, -1);
			atoms = Integer.parseInt(params.get(0)[0]);
			double v_min = Double.parseDouble(params.get(0)[1]);
			double v_max = Double.parseDouble(params.get(0)[2]);
			error_thresh = Double.parseDouble(params.get(0)[3]); // 0.7 for uav
			dtmc_epsilon = Double.parseDouble(params.get(0)[4]);
			alpha = Double.parseDouble(params.get(0)[5]);

			operator = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, distr_type, mainLog);
			temp_p = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, distr_type, mainLog);

			if(distr_type.equals(c51)){
				save_p = new DistributionCategorical(atoms, v_min, v_max, mainLog);
			} else{
				save_p = new DistributionQuantile(atoms, mainLog);
			}

			mainLog.println("Distr type: "+ distr_type);
			mainLog.println("----- Parameters:\natoms:"+atoms+" - vmax:"+v_max+" - vmin:"+v_min);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh+ " - epsilon:"+dtmc_epsilon);
		} else{
			atoms=101;
			double v_max = 100;
			double v_min = 0;
			distr_type = "C51";
			mainLog.println("Using default parameters - Distr type: "+ distr_type);
			mainLog.println("----- Parameters:\natoms:"+atoms+" - vmax:"+v_max+" - vmin:"+v_min);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh+ " - epsilon:"+dtmc_epsilon);
			operator = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, "C51", mainLog);
			temp_p = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, "C51", mainLog);
			save_p = new DistributionCategorical(atoms, v_min, v_max, mainLog);
		}

		// TODO : parse distribution info here - store in paramValues
		Point paramValues = new Point(new BigRational[]{new BigRational("0.8")});

		// distributions over transition probabilities:
		ArrayList<Double> trans_distr_succ = new ArrayList<>(Arrays. asList(0.5,0.6,0.7,0.8));
		ArrayList <Double> trans_distr_fail= new ArrayList<>(Arrays. asList(0.5,0.4,0.3,0.2));
		// Probability of having those transition values
		ArrayList <Double>  trans_prob = new ArrayList<>(Arrays. asList(0.1, 0.4, 0.3, 0.2));
		DiscreteDistribution transition_distr_succ;
		DiscreteDistribution transition_distr_fail;
		if (distr_type.equals(c51)) {
			transition_distr_succ = new DistributionCategorical(4, 0.5, 0.8, mainLog);
			transition_distr_fail = new DistributionCategorical(4, 0.2, 0.5, mainLog);
		} else {
			transition_distr_succ = new DistributionQuantile(10,  mainLog);
			transition_distr_fail = new DistributionQuantile(10,  mainLog);
		}
		transition_distr_succ.project(trans_prob, trans_distr_succ);
		transition_distr_fail.project(trans_prob, trans_distr_fail);
		mainLog.println("transitions successful :\n" + transition_distr_succ);
		mainLog.println("transitions 1-p :\n" + transition_distr_fail);

		// Create/initialise solution vector(s)
		DiscreteDistribution m ;
		double [] action_val = new double[nactions];
		double [] action_exp = new double[n];
		Object [] policy = new Object[n];
		int[] choices = new int[n];
		double min_v; int min_a;
		double max_dist ; int numChoices;
		int iters; boolean flag;
		FunctionalIterator<Map.Entry<Integer, Double>> iter;

		// Start iterations - number of episodes
		for (iters = 0; (iters < iterations) ; iters++)
		{
			iteration_timer = System.currentTimeMillis();

			// copy to temp value from operator
			if (iters> 0) {
				temp_p.clone(operator);
			}
			// mainLog.println("iteration : " + iters);

			PrimitiveIterator.OfInt states = unknownStates.iterator();
			while (states.hasNext()) {
				final int s = states.nextInt();
				numChoices = mdp.getNumChoices(s);
				min_v = Float.POSITIVE_INFINITY;
				min_a = 0;
				save_p.clear();
				Arrays.fill(action_val, Float.POSITIVE_INFINITY);
				// mainLog.println("state : " + s);
				for (int choice = 0; choice < numChoices; choice++){ // aka action

					double reward = mdpRewards.getStateReward(s).evaluate(paramValues).doubleValue() ;
					reward +=  mdpRewards.getTransitionReward(s, choice).evaluate(paramValues).doubleValue();

					iter = mdp.getTransitionsMappedIterator(s, choice, p -> p.evaluate(paramValues).doubleValue());

					flag = (s == 0 && mdp.getAction(s, choice).equals("n")) || (s == 1 && mdp.getAction(s, choice).equals("n"));
					flag = flag || (s == 2 && mdp.getAction(s, choice).equals("e"));

					if(flag)
					{
						// Uncertain transition, modify transitions to be distributional
						// TODO: this should be parsed from a file instead of hard coded.
						ArrayList <Map.Entry<Integer, DiscreteDistribution>> prob_trans = new ArrayList<>();
						while (iter.hasNext()) {
							Map.Entry<Integer, Double> e = iter.next();
							if (e.getValue() == 0.66) // if successful transition
							{
								Map.Entry<Integer, DiscreteDistribution> entry = new Pair<>(e.getKey(), transition_distr_succ);
								prob_trans.add(entry);
							}
							else {
								Map.Entry<Integer, DiscreteDistribution> entry = new Pair<>(e.getKey(), transition_distr_fail);
								prob_trans.add(entry);
							}
						}

//						Iterator<Map.Entry<Integer, DiscreteDistribution>> it_prob = prob_trans.iterator();
						m = operator.step(prob_trans, transition_distr_fail.getAtoms(), gamma, reward);
					}
					else {
						m = operator.step(iter, gamma, reward, s);
					}
					mainLog.println("state : "+s+"- choice: "+ mdp.getAction(s, choice)+" -- [" + m.toString(operator.getFormat()) +"]");
					action_val[choice] = m.getExpValue();

					if (action_val[choice] < min_v) {
						min_a = choice;
						min_v = action_val[choice]; action_exp[s] = min_v;
						save_p.clone(m);
					}
				}
				policy[s] = mdp.getAction(s, min_a);
				choices[s] = min_a;
				temp_p.update(save_p, s);
			}

			states = unknownStates.iterator();
			max_dist = 0.0;

			while (states.hasNext()) {
				final int s = states.nextInt();
				double tempo = operator.getW(temp_p.getDist(s), s);
				//if(tempo > max_dist){bad.add(s);}
				max_dist = Math.max(max_dist, tempo);
				operator.update(temp_p.getDist(s), s);
			}
			mainLog.println("Max Wp dist :"+(max_dist) + " error Wp:" + (error_thresh) +" at iter:"+iters);
			if ((max_dist <error_thresh)&(iters>min_iter)) {
				break;
			}

			iteration_timer = System.currentTimeMillis() - iteration_timer;
			max_iteration_timer = Math.max(iteration_timer, max_iteration_timer);
		}

		mainLog.println("\nV[start] at " + (iters + 1) + " with method "+distr_type);
		DecimalFormat df = new DecimalFormat("0.000");
		mainLog.print("[");
		mainLog.print(operator.toString(mdp.getFirstInitialState()));
		mainLog.print("]\n");
		mainLog.println("E at initial state:" + operator.getExpValue(mdp.getFirstInitialState()));

		timer = System.currentTimeMillis() - timer;
		if (verbosity >= 1) {
			mainLog.print("\nValue iteration computation (" + (min ? "min" : "max") + ")");
			mainLog.println(" : " + timer / 1000.0 + " seconds.");
			mainLog.println("Max time for 1 iteration :"+max_iteration_timer/1000.0+"s");
		}

		// Compute distribution on induced DTMC
//		mainLog.println("Computing distribution on induced DTMC...");
//		MDStrategy strat = new MDStrategyArray(mdp, choices);
//		DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
//		int initialState = dtmc.getFirstInitialState();
//		StateRewardsArray mcRewards = new StateRewardsArray(n);
//		for (int s = 0; s < n; s++) {
//			mcRewards.setStateReward(s, mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, choices[s]));
//		}
//		DTMCModelChecker mcDTMC = new DTMCModelChecker(this);
//		if(check_reach_dtmc_distr) {
//			timer = System.currentTimeMillis();
//			ModelCheckerResult dtmc_result = mcDTMC.computeReachRewardsDistr(dtmc, mcRewards, target, "prism/distr_dtmc_exp.csv", dtmc_epsilon);
//			timer = System.currentTimeMillis() - timer;
//			if (verbosity >= 1) {
//				mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
//				mainLog.println(" : " + timer / 1000.0 + " seconds.");
//				mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
//				mainLog.println(" : " + timer / 1000.0 + " seconds.");
//			}
//		}
//
//		if (check_reach_dtmc){
//			BitSet obs_states= mdp.getLabelStates(bad_states_label);
//			ModelCheckerResult result_obs = mcDTMC.computeReachProbs(dtmc, obs_states);
//			mainLog.println("Probs of reaching bad states :" + result_obs.soln[initialState]);
//		}
		// TODO create a function that takes mdp<mdp> + param index
//		MDPSimple mdpToSimulate = new MDPSimple(mdp);
//		if(gen_trace) {
//			mcMDP.exportTrace(mdpToSimulate, target, strat, "exp");
//		}
//
//		// Calling regular Value Iteration for comparison metrics.
//		if(compute_dtmc_vi){
//			mainLog.println("---------------------------------------\nStarting PRISM VI");
//			DTMCModelChecker vi_mcDTMC= new DTMCModelChecker(this);
//			timer = System.currentTimeMillis();
//			ModelCheckerResult vi_res = mcMDP.computeReachRewards(mdp, mdpRewards, target, min);
//			timer = System.currentTimeMillis() - timer;
//			if (verbosity >= 1) {
//				mainLog.print("\nPRISM VI");
//				mainLog.println(" : " + timer / 1000.0 + " seconds.");
//			}
//
//			mainLog.println("\nVI result in initial state:"+ vi_res.soln[mdp.getFirstInitialState()]);
//			StateRewardsArray vi_mcRewards = new StateRewardsArray(n); // Compute rewards array
//			for (int s = 0; s < n; s++) {
//				if(target.get(s)) {
//					vi_mcRewards.setStateReward(s,0);
//					((MDStrategyArray) vi_res.strat).choices[s] = 0;
//				}
//				else {
//					double transition_reward = 0;
//					if (((MDStrategy) vi_res.strat).isChoiceDefined(s)) {
//						transition_reward = mdpRewards.getTransitionReward(s, ((MDStrategy) vi_res.strat).getChoiceIndex(s));
//					}
//					else if(vi_res.strat.whyUndefined(s, -1) == StrategyInfo.UndefinedReason.ARBITRARY) {
//						transition_reward = mdpRewards.getTransitionReward(s, 0);
//					}
//					else {
//						mainLog.println(" Error in strategy: choice is :"+ ((MDStrategy) vi_res.strat).getChoiceActionString(s, -1));
//					}
//					vi_mcRewards.setStateReward(s, mdpRewards.getStateReward(s) + transition_reward);
//				}
//			}
//			DTMC vi_dtmc = new DTMCFromMDPAndMDStrategy(mdp, (MDStrategy) vi_res.strat);
//			if (check_reach_dtmc_distr_vi) {
//				timer = System.currentTimeMillis();
//				vi_mcDTMC.computeReachRewardsDistr(vi_dtmc, vi_mcRewards, target, "prism/distr_dtmc_vi.csv", dtmc_epsilon);
//				timer = System.currentTimeMillis() - timer;
//				if (verbosity >= 1) {
//					mainLog.print("\nDTMC computation VI");
//					mainLog.println(" : " + timer / 1000.0 + " seconds.");
//				}
//			}
//
//			if (check_reach_dtmc_vi){
//				BitSet obs_states= mdp.getLabelStates(bad_states_label);
//				ModelCheckerResult result_obs = mcDTMC.computeReachProbs(vi_dtmc, obs_states);
//				mainLog.println("Probs of reaching bad states :" + result_obs.soln[vi_dtmc.getFirstInitialState()]);
//			}
//
//			if(gen_trace) {
//				mcMDP.exportTrace(mdpToSimulate, target, (MDStrategy) vi_res.strat, "vi");
//			}
//
//		}

		// Store results
		operator.writeToFile(mdp.getFirstInitialState(), null);
		ModelCheckerResult res = new ModelCheckerResult();
		res.soln = Arrays.copyOf(action_exp, action_exp.length); // return the expected values for each state
		res.numIters = iterations;
		res.timeTaken = (System.currentTimeMillis() - total_timer) / 1000.0;
		return res;
	}

	/**
	 * Compute expected reachability rewards.
	 * @param mdp The MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
	// DVI for uncertain distributional transition with hard coded transitions
	public ModelCheckerResult computeReachRewardsDistHardCoded(MDP<Double> mdp, MDPRewards<Double> mdpRewards, BitSet target, boolean min) throws PrismException
	{
		MDPModelChecker mcMDP = new MDPModelChecker(this);
		mcMDP.inheritSettings(this);

		// Start expected reachability
		long timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability (" + (min ? "min" : "max") + ")...");

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		mdp.checkForDeadlocks(target);

		// Store num states
		int n = mdp.getNumStates();

		// Precomputation (not optional)
		long timerProb1 = System.currentTimeMillis();
		BitSet inf = mcMDP.prob1(mdp, null, target, !min, null);
		inf.flip(0, n);
//		timerProb1 = System.currentTimeMillis() - timerProb1;

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
		int min_iter = 8;
		double error_thresh = 0.01;
		double gamma = 1;
		double alpha=0.5;
		Double dtmc_epsilon = null;
		boolean check_reach_dtmc = false;
		boolean check_reach_dtmc_distr = true;
		boolean check_reach_dtmc_vi = false;
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
		DistributionalBellmanOperatorProb operator, temp_p; DiscreteDistribution save_p;
		String distr_type = settings.getString(PrismSettings.PRISM_DISTR_SOLN_METHOD);

		if (distr_type.equals(c51) || distr_type.equals(qr)) {
			// TODO remove this in final version
			ArrayList<String []> params = mcMDP.readParams(null, 1);
			atoms = Integer.parseInt(params.get(0)[0]);
			double v_min = Double.parseDouble(params.get(0)[1]);
			double v_max = Double.parseDouble(params.get(0)[2]);
			error_thresh = Double.parseDouble(params.get(0)[3]); // 0.7 for uav
			dtmc_epsilon = Double.parseDouble(params.get(0)[4]);
			alpha = Double.parseDouble(params.get(0)[5]);

			operator = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, distr_type, mainLog);
			temp_p = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, distr_type, mainLog);

			if(distr_type.equals(c51)){
				save_p = new DistributionCategorical(atoms, v_min, v_max, mainLog);
			} else{
				save_p = new DistributionQuantile(atoms, mainLog);
			}

			mainLog.println("Distr type: "+ distr_type);
			mainLog.println("----- Parameters:\natoms:"+atoms+" - vmax:"+v_max+" - vmin:"+v_min);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh+ " - epsilon:"+dtmc_epsilon);
		} else{
			atoms=101;
			double v_max = 100;
			double v_min = 0;
			distr_type = "C51";
			mainLog.println("Using default parameters - Distr type: "+ distr_type);
			mainLog.println("----- Parameters:\natoms:"+atoms+" - vmax:"+v_max+" - vmin:"+v_min);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh+ " - epsilon:"+dtmc_epsilon);
			operator = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, "C51", mainLog);
			temp_p = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, "C51", mainLog);
			save_p = new DistributionCategorical(atoms, v_min, v_max, mainLog);
		}

		// Create/initialise solution vector(s)
		DiscreteDistribution m ;
		double [] action_val = new double[nactions];
		double [] action_exp = new double[n];
		Object [] policy = new Object[n];
		int[] choices = new int[n];
		double min_v; int min_a;
		double max_dist ; int numChoices;
		int iters; boolean flag;

		// tODO : remove this eventually
		// distributions over transition probabilities:
		ArrayList<Double> trans_distr_succ = new ArrayList<>(Arrays. asList(0.5,0.6,0.7,0.8));
		ArrayList <Double> trans_distr_fail= new ArrayList<>(Arrays. asList(0.5,0.4,0.3,0.2));
		// Probability of having those transition values
		ArrayList <Double>  trans_prob = new ArrayList<>(Arrays. asList(0.1, 0.4, 0.3, 0.2));
		DiscreteDistribution transition_distr_succ;
		DiscreteDistribution transition_distr_fail;
		if (distr_type.equals(c51)) {
			transition_distr_succ = new DistributionCategorical(4, 0.5, 0.8, mainLog);
			transition_distr_fail = new DistributionCategorical(4, 0.2, 0.5, mainLog);
		} else {
			transition_distr_succ = new DistributionQuantile(10,  mainLog);
			transition_distr_fail = new DistributionQuantile(10,  mainLog);
		}
		transition_distr_succ.project(trans_prob, trans_distr_succ);
		transition_distr_fail.project(trans_prob, trans_distr_fail);
		mainLog.println("transitions successful :\n" + transition_distr_succ);
		mainLog.println("transitions 1-p :\n" + transition_distr_fail);

		// Start iterations - number of episodes
		for (iters = 0; (iters < iterations) ; iters++)
		{
			iteration_timer = System.currentTimeMillis();

			// copy to temp value from operator
			if (iters> 0) {
				temp_p.clone(operator);
			}
			// mainLog.println("iteration : " + iters);

			PrimitiveIterator.OfInt states = unknownStates.iterator();
			while (states.hasNext()) {
				final int s = states.nextInt();
				numChoices = mdp.getNumChoices(s);
				min_v = Float.POSITIVE_INFINITY;
				min_a = 0;
				save_p.clear();
				Arrays.fill(action_val, Float.POSITIVE_INFINITY);
				// mainLog.println("state : " + s);
				for (int choice = 0; choice < numChoices; choice++){ // aka action

					Iterator<Map.Entry<Integer, Double>>it = mdp.getTransitionsIterator(s,choice);
					double reward = mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, choice);

					flag = (s == 0 && mdp.getAction(s, choice).equals("n")) || (s == 1 && mdp.getAction(s, choice).equals("n"));
					flag = flag || (s == 2 && mdp.getAction(s, choice).equals("e"));

					if(flag)
					{
						// Uncertain transition, modify transitions to be distributional
						// TODO: this should be parsed from a file instead of hard coded.
						ArrayList <Map.Entry<Integer, DiscreteDistribution>> prob_trans = new ArrayList<>();
						while (it.hasNext()) {
							Map.Entry<Integer, Double> e = it.next();
							if (e.getValue() == 0.66) // if successful transition
							{
								Map.Entry<Integer, DiscreteDistribution> entry = new Pair<>(e.getKey(), transition_distr_succ);
								prob_trans.add(entry);
							}
							else {
								Map.Entry<Integer, DiscreteDistribution> entry = new Pair<>(e.getKey(), transition_distr_fail);
								prob_trans.add(entry);
							}
						}

//						Iterator<Map.Entry<Integer, DiscreteDistribution>> it_prob = prob_trans.iterator();
						m = operator.step(prob_trans, transition_distr_fail.getAtoms(), gamma, reward);
					}
					else {
						m = operator.step(it, gamma, reward, s);
					}
					mainLog.println("state : "+s+"- choice: "+ mdp.getAction(s, choice)+" -- [" + m.toString(operator.getFormat()) +"]");
					action_val[choice] = m.getExpValue();

					if (action_val[choice] < min_v) {
						min_a = choice;
						min_v = action_val[choice]; action_exp[s] = min_v;
						save_p.clone(m);
					}
				}
				policy[s] = mdp.getAction(s, min_a);
				choices[s] = min_a;
				temp_p.update(save_p, s);
			}

			states = unknownStates.iterator();
			max_dist = 0.0;

			while (states.hasNext()) {
				final int s = states.nextInt();
				double tempo = operator.getW(temp_p.getDist(s), s);
				//if(tempo > max_dist){bad.add(s);}
				max_dist = Math.max(max_dist, tempo);
				operator.update(temp_p.getDist(s), s);
			}
			mainLog.println("Max Wp dist :"+(max_dist) + " error Wp:" + (error_thresh) +" at iter:"+iters);
			if ((max_dist <error_thresh)&(iters>min_iter)) {
				break;
			}

			iteration_timer = System.currentTimeMillis() - iteration_timer;
			max_iteration_timer = Math.max(iteration_timer, max_iteration_timer);
		}

		mainLog.println("\nV[start] at " + (iters + 1) + " with method "+distr_type);
		DecimalFormat df = new DecimalFormat("0.000");
		mainLog.print("[");
		mainLog.print(operator.toString(mdp.getFirstInitialState()));
		mainLog.print("]\n");
		mainLog.println("E at initial state:" + operator.getExpValue(mdp.getFirstInitialState()));

		timer = System.currentTimeMillis() - timer;
		if (verbosity >= 1) {
			mainLog.print("\nValue iteration computation (" + (min ? "min" : "max") + ")");
			mainLog.println(" : " + timer / 1000.0 + " seconds.");
			mainLog.println("Max time for 1 iteration :"+max_iteration_timer/1000.0+"s");
		}

		// Compute distribution on induced DTMC
//		mainLog.println("Computing distribution on induced DTMC...");
//		MDStrategy strat = new MDStrategyArray(mdp, choices);
//		DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
//		int initialState = dtmc.getFirstInitialState();
//		StateRewardsArray mcRewards = new StateRewardsArray(n);
//		for (int s = 0; s < n; s++) {
//			mcRewards.setStateReward(s, mdpRewards.getStateReward(s) + mdpRewards.getTransitionReward(s, choices[s]));
//		}
//		DTMCModelChecker mcDTMC = new DTMCModelChecker(this);
//		if(check_reach_dtmc_distr) {
//			timer = System.currentTimeMillis();
//			ModelCheckerResult dtmc_result = mcDTMC.computeReachRewardsDistr(dtmc, mcRewards, target, "prism/distr_dtmc_exp.csv", dtmc_epsilon);
//			timer = System.currentTimeMillis() - timer;
//			if (verbosity >= 1) {
//				mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
//				mainLog.println(" : " + timer / 1000.0 + " seconds.");
//				mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
//				mainLog.println(" : " + timer / 1000.0 + " seconds.");
//			}
//		}
//
//		if (check_reach_dtmc){
//			BitSet obs_states= mdp.getLabelStates(bad_states_label);
//			ModelCheckerResult result_obs = mcDTMC.computeReachProbs(dtmc, obs_states);
//			mainLog.println("Probs of reaching bad states :" + result_obs.soln[initialState]);
//		}
		// TODO create a function that takes mdp<mdp> + param index
//		MDPSimple mdpToSimulate = new MDPSimple(mdp);
//		if(gen_trace) {
//			mcMDP.exportTrace(mdpToSimulate, target, strat, "exp");
//		}
//
//		// Calling regular Value Iteration for comparison metrics.
//		if(compute_dtmc_vi){
//			mainLog.println("---------------------------------------\nStarting PRISM VI");
//			DTMCModelChecker vi_mcDTMC= new DTMCModelChecker(this);
//			timer = System.currentTimeMillis();
//			ModelCheckerResult vi_res = mcMDP.computeReachRewards(mdp, mdpRewards, target, min);
//			timer = System.currentTimeMillis() - timer;
//			if (verbosity >= 1) {
//				mainLog.print("\nPRISM VI");
//				mainLog.println(" : " + timer / 1000.0 + " seconds.");
//			}
//
//			mainLog.println("\nVI result in initial state:"+ vi_res.soln[mdp.getFirstInitialState()]);
//			StateRewardsArray vi_mcRewards = new StateRewardsArray(n); // Compute rewards array
//			for (int s = 0; s < n; s++) {
//				if(target.get(s)) {
//					vi_mcRewards.setStateReward(s,0);
//					((MDStrategyArray) vi_res.strat).choices[s] = 0;
//				}
//				else {
//					double transition_reward = 0;
//					if (((MDStrategy) vi_res.strat).isChoiceDefined(s)) {
//						transition_reward = mdpRewards.getTransitionReward(s, ((MDStrategy) vi_res.strat).getChoiceIndex(s));
//					}
//					else if(vi_res.strat.whyUndefined(s, -1) == StrategyInfo.UndefinedReason.ARBITRARY) {
//						transition_reward = mdpRewards.getTransitionReward(s, 0);
//					}
//					else {
//						mainLog.println(" Error in strategy: choice is :"+ ((MDStrategy) vi_res.strat).getChoiceActionString(s, -1));
//					}
//					vi_mcRewards.setStateReward(s, mdpRewards.getStateReward(s) + transition_reward);
//				}
//			}
//			DTMC vi_dtmc = new DTMCFromMDPAndMDStrategy(mdp, (MDStrategy) vi_res.strat);
//			if (check_reach_dtmc_distr_vi) {
//				timer = System.currentTimeMillis();
//				vi_mcDTMC.computeReachRewardsDistr(vi_dtmc, vi_mcRewards, target, "prism/distr_dtmc_vi.csv", dtmc_epsilon);
//				timer = System.currentTimeMillis() - timer;
//				if (verbosity >= 1) {
//					mainLog.print("\nDTMC computation VI");
//					mainLog.println(" : " + timer / 1000.0 + " seconds.");
//				}
//			}
//
//			if (check_reach_dtmc_vi){
//				BitSet obs_states= mdp.getLabelStates(bad_states_label);
//				ModelCheckerResult result_obs = mcDTMC.computeReachProbs(vi_dtmc, obs_states);
//				mainLog.println("Probs of reaching bad states :" + result_obs.soln[vi_dtmc.getFirstInitialState()]);
//			}
//
//			if(gen_trace) {
//				mcMDP.exportTrace(mdpToSimulate, target, (MDStrategy) vi_res.strat, "vi");
//			}
//
//		}

		// Store results
		operator.writeToFile(mdp.getFirstInitialState(), null);
		ModelCheckerResult res = new ModelCheckerResult();
		res.soln = Arrays.copyOf(action_exp, action_exp.length); // return the expected values for each state
		res.numIters = iterations;
		res.timeTaken = (System.currentTimeMillis() - total_timer) / 1000.0;
		return res;
	}
}
