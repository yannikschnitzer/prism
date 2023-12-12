package explicit;

import prism.PrismException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

abstract class DistributionalBellman {

    double alpha=1;

    public DistributionalBellman(){}

    public void setAlpha(double a){ alpha=a;}

    // Clear all distr
    public abstract void clear();

    // Empty all distr
    public abstract void emptyAll();

    // Distributional Bellman update using transitions probabilities, discount factor <gamma> and reward <state_reward>
    // This function also performs projection for the distributional operators.
    public abstract DiscreteDistribution step(Iterator<Map.Entry<Integer, Double>> trans_it,
                                              double gamma, double state_reward, int cur_state);

    //TODO
    public abstract DiscreteDistribution step(Iterator<Map.Entry<Integer, DiscreteDistribution>> trans_it,
                                              double gamma, double state_reward,
                                              int cur_state, boolean isTransCategorical);

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
    public abstract double getW(double[] dist1, int state);

    // Get distributional distance between a distribution <dist1> and the distribution for a state <state>
    // assumes same support
    public abstract double getW(DiscreteDistribution dist1, int state);

    // update the saved distribution with temp for a given state, assumes same support
    public abstract void update(DiscreteDistribution temp, int state);

    // Retrieve distribution for a state i
    public abstract DiscreteDistribution getDist(int i);

    // Log distribution for a state to a file <filename> as a csv with columns : support index, probability, support value
    public abstract void writeToFile(int state, String filename);

    public abstract String toString(int state);

    public abstract void setFormat(DecimalFormat d_format);

    // Get parameters for this operator
    public abstract ArrayList<String> getParams();

    // Deep copy of a DistributionalBellman operator into current one
    public abstract void clone(DistributionalBellman source) throws PrismException;

}
