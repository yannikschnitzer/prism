package explicit;

import common.iterable.FunctionalIterator;
import common.iterable.Reducible;
import explicit.rewards.MDPRewards;
import param.BigRational;
import param.Function;
import parser.ast.Expression;
import prism.Evaluator;
import param.Point;
import prism.PrismComponent;
import prism.PrismException;

import java.util.AbstractMap;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;

public class MDPModelCheckerDistributional extends ProbModelChecker
{
	/**
	 * Create a new MDPModelCheckerDistributional, inherit basic state from parent (unless null).
	 */
	public MDPModelCheckerDistributional(PrismComponent parent) throws PrismException
	{
		super(parent);
	}

	public ModelCheckerResult computeReachRewards(MDP<Function> mdp, MDPRewards<Function> mdpRewards, BitSet target, boolean min) throws PrismException
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
}
