package explicit;

import explicit.rewards.MDPRewards;
import explicit.rewards.StateRewardsArray;
import prism.PrismException;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

 // FIXME : this class structure needs to be adjusted.
public abstract class DistributionalBellmanAugmented {
    public int b_atoms;
    public double [] b;
    double alpha = 1;
    CVaRProduct prod_mdp;

    public DistributionalBellmanAugmented(){}

    public DistributionalBellmanAugmented(DistributionalBellmanAugmented el){ this.alpha = el.alpha;}

    public abstract DistributionalBellmanAugmented copy() ;

    public void setAlpha(double a){ alpha=a;}

    public abstract double [] step(Iterator<Map.Entry<Integer, Double>> trans_it,  int numTransitions, double gamma, double state_reward);

    public abstract double getExpValue(double [] temp);

    public abstract double getValueCvar(double [] probs, double lim, int idx_b);

    public abstract double getVar(double [] probs, double lim);

    public abstract double getVariance(double [] probs);

    public abstract double getProbThreshold(double[] probs, double lim);

    public abstract double getW(double[] dist1, double[] dist2);

    public abstract double getW(double [] dist1, int state);

    public abstract void update(double[] temp, int state);

    public abstract double[] getDist(int s);

    public abstract double getMagic(double [] temp, int idx_b);

    //  Initializing with augmented state and actions.
    public abstract void initialize(MDP mdp, MDPRewards mdpRewards, double gamma, BitSet target) throws PrismException;

    public abstract int getClosestB(double temp_b);

    public abstract void display();

    public abstract void display(MDP mdp);

    public abstract void display(int s);

    public abstract int[] getStrategy(MDPRewards mdpRewards, StateRewardsArray rewardsArray, int [] choices, double alpha) throws PrismException;

    public abstract double [] adjust_support(TreeMap distr);

    public abstract CVaRProduct getProductMDP();

    public abstract int getB_atoms();

    public abstract double getBVal(int idx);

    public abstract double [] getB();

    public abstract void writeToFile(int state, String filename);
}
