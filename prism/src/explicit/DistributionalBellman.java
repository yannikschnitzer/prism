package explicit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

abstract class DistributionalBellman {

    double alpha=1;

    public DistributionalBellman(){}

    public void setAlpha(double a){ alpha=a;}

    // Distributional Bellman update using transitions probabilities, discount factor <gamma> and reward <state_reward>
    // This function also performs projection for the distributional operators.
    public abstract DiscreteDistribution step(Iterator<Map.Entry<Integer, Double>> trans_it, int numTransitions, double gamma, double state_reward);

    // Get Expected value of a distribution <probs> with an alpha = <lim>
    public abstract double getExpValue(int state);

    // Get Conditional Value at Risk value of a distribution <probs> with an alpha = <lim> , assumes same support
    public abstract double getValueCvar(int state, double lim);

    // Get Value at risk of a distribution <probs> with an alpha = <lim> , assumes same support
    public abstract double getVar(int state, double lim);

    // Get Variance of a distribution <probs>, assumes same support
    public abstract double getVariance(int state);

//    // Get distributional distance between two distributions <dist1> and <dist2>, assumes same support
//    public abstract double getW(ArrayList<Double> dist1, ArrayList<Double> dist2);

    // Get distributional distance between a distribution <dist1> and the distribution for a state <state>
    // assumes same support
    public abstract double getW(ArrayList<Double> dist1, int state);

    // update the saved distribution with temp for a given state, assumes same support
    public abstract void update(ArrayList<Double> temp, int state);

    // Retrieve distribution for a state i
    public abstract DiscreteDistribution getDist(int i);

    // Log distribution for a state to a file <filename> as a csv with columns : support index, probability, support value
    public abstract void writeToFile(int state, String filename);
//    public abstract double [][] getDist();
}
