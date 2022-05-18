package explicit;


import explicit.rewards.MDPRewards;
import explicit.rewards.StateRewardsArray;
import prism.PrismException;
import prism.PrismLog;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;

public class DistributionalBellmanQRAugmented extends DistributionalBellmanAugmented {
    int atoms = 1;
    double delta_z = 1;
    double [] z ;
    double [][][][] p;
    int n_actions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;
    int numStates;

    // slack variable b
    int b_atoms;
    double delta_b;
    double [] b; // array containing b values

    PrismLog mainLog;

    @Override
    public DistributionalBellmanAugmented copy() {
        return null;
    }

    @Override
    public double[] step(Iterator<Map.Entry<Integer, Double>> trans_it, int[] choices, int numTransitions, double gamma, double state_reward) {
        return new double[0];
    }

    @Override
    public double getExpValue(double[] temp) {
        return 0;
    }

    @Override
    public double getValueCvar(double[] probs, double lim, int idx_b) {
        return 0;
    }

    @Override
    public double getVar(double[] probs, double lim) {
        return 0;
    }

    @Override
    public double getVariance(double[] probs) {
        return 0;
    }

    @Override
    public double getW(double[] dist1, double[] dist2) {
        return 0;
    }

    @Override
    public double getW(double[] dist1, int state, int idx_a) {
        return 0;
    }

    @Override
    public void update(double[] temp, int state, int action) {

    }

    @Override
    public double[][] getDist(int s) {
        return new double[0][];
    }

    @Override
    public double[] getDist(int s, int a) {
        return new double[0];
    }

    @Override
    public double getMagic(double[] temp, int idx_b) {
        return 0;
    }

    @Override
    public void initialize(MDP mdp, MDPRewards mdpRewards, double gamma, BitSet target) throws PrismException {

    }

    @Override
    public int getClosestB(double temp_b) {
        return 0;
    }

    @Override
    public void display() {

    }

    @Override
    public void display(MDP mdp) {

    }

    @Override
    public void display(int s) {

    }

    @Override
    public void display(int i, int[] choices) {

    }

    @Override
    public int[] getStrategy(MDPRewards mdpRewards, StateRewardsArray rewardsArray, int[] choices, double alpha) throws PrismException {
        return new int[0];
    }

    @Override
    public CVaRProduct getProductMDP() {
        return prod_mdp;
    }

    @Override
    public int getB_atoms() {
        return 0;
    }

    @Override
    public double getBVal(int idx) {
        return 0;
    }

    @Override
    public double[] getB() {
        return new double[0];
    }

}