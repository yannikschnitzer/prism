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
import java.util.*;

import static explicit.DistributionalBellmanOperatorProb.toBigRationalPoint;

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
		Point paramValues =  new Point(new BigRational[]{new BigRational("0.8"), new BigRational("0.6")});

		// Print out MDP (before and after parameter instantiation)
		System.out.println(mdp);
		int numStates = mdp.getNumStates();
		for (int s = 0; s < numStates; s++) {
			int numChoices = mdp.getNumChoices(s);
			for (int i = 0; i < numChoices; i++) {

				Iterator<Map.Entry<Integer, Function>> iter = mdp.getTransitionsIterator(s, i);
//				FunctionalIterator<Map.Entry<Integer, Double>> iter = mdp.getTransitionsMappedIterator(s, i, p -> p.evaluate(paramValues).doubleValue());
				while (iter.hasNext()) {
					Map.Entry<Integer, Function> e = iter.next();
					mainLog.println(s + "," + mdp.getAction(s, i) + ":" + e.getKey() + "=" + e.getValue().evaluate(paramValues).doubleValue());
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

	/**
	 * Compute expected reachability rewards for an uncertain MDP with transition probabilities specified as a distribution.
	 * @param mdp The parametric MDP
	 * @param mdpRewards The rewards
	 * @param target Target states
	 * @param min Min or max rewards (true=min, false=max)
	 */
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
		boolean check_dtmc_distr = true;
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
		int uncertain_atoms; double  u_vmax; double u_vmin;

		if (distr_type.equals(c51) || distr_type.equals(qr)) {
			ArrayList<String []> params = mcMDP.readParams(null, -1);
			atoms = Integer.parseInt(params.get(0)[0]);
			double v_min = Double.parseDouble(params.get(0)[1]);
			double v_max = Double.parseDouble(params.get(0)[2]);
			error_thresh = Double.parseDouble(params.get(0)[3]); // 0.7 for uav
			dtmc_epsilon = Double.parseDouble(params.get(0)[4]);
			alpha = Double.parseDouble(params.get(0)[5]);

			// Uncertain parameter distribution info
			uncertain_atoms = Integer.parseInt(params.get(0)[6]);
			u_vmin = Double.parseDouble(params.get(0)[7]);
			u_vmax = Double.parseDouble(params.get(0)[8]);

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
			uncertain_atoms = 11;
			u_vmin =0.0; u_vmax = 1.0;
			mainLog.println("Using default parameters - Distr type: "+ distr_type);
			mainLog.println("----- Parameters:\natoms:"+atoms+" - vmax:"+v_max+" - vmin:"+v_min);
			mainLog.println("alpha:"+alpha+" - discount:"+gamma+" - max iterations:"+iterations+
					" - error thresh:"+error_thresh+ " - epsilon:"+dtmc_epsilon);
			operator = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, "C51", mainLog);
			temp_p = new DistributionalBellmanOperatorProb(atoms, v_min, v_max, n, "C51", mainLog);
			save_p = new DistributionCategorical(atoms, v_min, v_max, mainLog);
		}

		//  Parse uncertain parameter distribution info here
		//  TODO: DO this for each parameter!!!
		ArrayList<String []> params = mcMDP.readParams("prism/tests/param_distr/param_p.csv", 2);
		ArrayList<Double> trans_distr_file = new ArrayList<>(uncertain_atoms);
		ArrayList <Double>  trans_prob = new ArrayList<>(uncertain_atoms);

		{
			int i = 0;
			// parse distributional information for
			for (String param : params.get(0)) {
				// distributions over transition probabilities
				trans_distr_file.add(Double.parseDouble(param));;
				// Probability of having those transition values
				trans_prob.add(Double.parseDouble(params.get(1)[i]));
				i+=1;
			}
		}

		DiscreteDistribution transition_distr;
		if (distr_type.equals(c51)) {
			transition_distr = new DistributionCategorical(uncertain_atoms,
					u_vmin, u_vmax, mainLog);
		} else {
			transition_distr = new DistributionQuantile(uncertain_atoms,  mainLog);
		}
		transition_distr.project(trans_prob, trans_distr_file);
		mainLog.println("Param p distr:");
		mainLog.println(transition_distr.getValues());
		mainLog.println(transition_distr.getSupports());

		// Create/initialise solution vector(s)
		DiscreteDistribution m = null;
		double [] action_val = new double[nactions];
		double [] action_exp = new double[n];
		Object [] policy = new Object[n];
		int[] choices = new int[n];
		double min_v; int min_a;
		double max_dist ; int numChoices;
		int iters; boolean isUncertain;

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
				min_v = Float.POSITIVE_INFINITY; min_a = 0;
				save_p.clear();
				Arrays.fill(action_val, Float.POSITIVE_INFINITY);
				// mainLog.println("state : " + s);
				for (int choice = 0; choice < numChoices; choice++){ // aka action

					// INFO: the value passed to evaluate doesn't matter here because the rewards don't depend on the
					// 		  transition probabilities
					double reward = mdpRewards.getStateReward(s).evaluate(toBigRationalPoint(0.0)).doubleValue() ;
					reward +=  mdpRewards.getTransitionReward(s, choice).evaluate(toBigRationalPoint(0.0)).doubleValue();

					// TODO: probably have the check for which parameter here
					// check if the transition is uncertain
					isUncertain = true;
					Iterator<Map.Entry<Integer, Function>> iter3 = mdp.getTransitionsIterator(s, choice);
					while (iter3.hasNext()) {
						Map.Entry<Integer, Function> e = iter3.next();
						isUncertain &= e.getValue().isConstant();
//						mainLog.println(e.getValue());
//						mainLog.println(isUncertain);
					}

					if(!isUncertain)
					{
						// Uncertain transition step
						// TODO : send the distr for the right parameter, the parameter idx, and the number of parameters
						m = operator.step(mdp, transition_distr,0, s, choice, gamma, reward);

					}
					else {
						Iterator<Map.Entry<Integer, Double>> iter2;
						iter2 = mdp.getTransitionsMappedIterator(s, choice, p -> p.evaluate(toBigRationalPoint(0.0)).doubleValue());
						m = operator.step(iter2, gamma, reward, s);
					}
					// mainLog.println("state : "+s+"- choice: "+ mdp.getAction(s, choice)+" -- [" + m.toString(operator.getFormat()) +"]");
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

			mainLog.print("initial s: "+ mdp.getFirstInitialState()+" -- [");
			mainLog.print(operator.toString(mdp.getFirstInitialState()));
			mainLog.print("]\n");

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

		// Compute distribution on induced DTMC for each param
		// TODO create a function that takes mdp<mdp> + param index
		mainLog.println("Computing distribution on induced DTMC...");
		if (check_dtmc_distr) {
			double expected_dtmc = 0;
			double [] exp_dtmc_atom = new  double[uncertain_atoms];
			for(int i=0; i<uncertain_atoms; i++) {
				int finalI=i;
				MDP<Double> atom_mdp = new MDPSimple<>(mdp,
						p->p.evaluate(toBigRationalPoint(transition_distr.getSupport(finalI))).doubleValue(),
						Evaluator.forDouble());

				MDStrategy strat = new MDStrategyArray(atom_mdp, choices);
				DTMC dtmc = new DTMCFromMDPAndMDStrategy(atom_mdp, strat);
				StateRewardsArray mcRewards = new StateRewardsArray(n);

				for (int s = 0; s < n; s++) {
					double reward = mdpRewards.getStateReward(s).evaluate(toBigRationalPoint(0.0)).doubleValue() ;
					reward +=  mdpRewards.getTransitionReward(s, choices[s]).evaluate(toBigRationalPoint(0.0)).doubleValue();
					mcRewards.setStateReward(s, reward);
				}
				DTMCModelChecker mcDTMC = new DTMCModelChecker(this);

				timer = System.currentTimeMillis();
				ModelCheckerResult dtmc_result = mcDTMC.computeReachRewardsDistr(dtmc, mcRewards, target, "prism/distr_dtmc_exp_prob_"+i+".csv", dtmc_epsilon);
				timer = System.currentTimeMillis() - timer;
				if (verbosity >= 1) {
					mainLog.print("\nDTMC computation (" + (min ? "min" : "max") + ")");
					mainLog.println(" : " + timer / 1000.0 + " seconds.");
				}
				TreeMap<Integer,Double> result_i = (TreeMap<Integer, Double>) dtmc_result.solnObj[dtmc.getFirstInitialState()];

				for(Map.Entry<Integer, Double> entry : result_i.entrySet())
				{
					exp_dtmc_atom[i]+= entry.getKey() *entry.getValue();
				}

				expected_dtmc +=  transition_distr.getValue(i)*exp_dtmc_atom[i];
			}
			mainLog.println("-----Atoms: "+ Arrays.toString(transition_distr.getSupports()));
			mainLog.println("---Weights: "+ Arrays.toString(transition_distr.getValues()));
			mainLog.println("Exp values: "+ Arrays.toString(exp_dtmc_atom));
			mainLog.println("DTMC weighted expected value :" + expected_dtmc);
			mainLog.print(choices);
		}


		// TODO create a function that takes mdp<mdp> + param index
//		MDPSimple mdpToSimulate = new MDPSimple(mdp);
//		if(gen_trace) {
//			mcMDP.exportTrace(mdpToSimulate, target, strat, "exp");
//		}

		// Store results
		operator.writeToFile(mdp.getFirstInitialState(), null);
		ModelCheckerResult res = new ModelCheckerResult();
		res.soln = Arrays.copyOf(action_exp, action_exp.length); // return the expected values for each state
		res.numIters = iterations;
		res.timeTaken = (System.currentTimeMillis() - total_timer) / 1000.0;
		// Store strategy
		if (genStrat) {
			res.strat = new MDStrategyArray<Function>(mdp, choices);
		}
		return res;
	}

}
