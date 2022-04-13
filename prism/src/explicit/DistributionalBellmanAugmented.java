package explicit;

import explicit.rewards.MDPRewards;
import explicit.rewards.StateRewardsArray;
import prism.PrismException;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;

public abstract class DistributionalBellmanAugmented {
    public int b_atoms;
    public double [] b;
    double alpha = 1;

    public DistributionalBellmanAugmented(){}

    public DistributionalBellmanAugmented(DistributionalBellmanAugmented el){ this.alpha = el.alpha;}

    public abstract DistributionalBellmanAugmented copy() ;

    public void setAlpha(double a){ alpha=a;}

    public abstract double [] step(Iterator<Map.Entry<Integer, Double>> trans_it, double cur_b, int [][] choice, int numTransitions, double gamma, double state_reward);

    public abstract double getExpValue(double [] temp);

    public abstract double getValueCvar(double [] probs, double lim, int idx_b);

    public abstract double getVar(double [] probs, double lim);

    public abstract double getVariance(double [] probs);

    // Wp with p=2
    public abstract double getW(double[] dist1, double[] dist2);

    // Wp with p=2
    public abstract double getW(double [] dist1, int state, int idx_b, int idx_a);

    public abstract void initialize(int n);

    public abstract void update(double [] temp, int state, int idx_b, int action);

    public abstract double [] getDist(int s, int idx_b, int a);

    public abstract double [][] getDist(int s, int idx_b);

    public abstract double getMagic(double [] temp, int idx_b);

    public abstract int getClosestB(double temp_b);

    public abstract void display();

    public abstract void display(MDP mdp);

    public abstract void display(int s);

    public abstract void display(int i, int[][] choices);

    public abstract int[] getStrategy(int start, CVaRProduct prodMDP , MDPRewards mdpRewards, StateRewardsArray rewardsArray, int [][] choices, double alpha) throws PrismException;
}
