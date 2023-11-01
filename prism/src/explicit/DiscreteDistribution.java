package explicit;

// import java.util.Iterator;
// import java.util.Map;
import java.util.ArrayList;

abstract class DiscreteDistribution {

    // Constructor
    public DiscreteDistribution(){}

    // initialize the distribution
    public abstract void clear();

    // project a given array to finite support
    public abstract void project(ArrayList<Double> arr);

    // update saved distribution
    public abstract void update(ArrayList<Double> arr);

    // compute expected value of the distribution
    public abstract double getExpValue();

    // compute CVaR with a given alpha 
    public abstract double getCvarValue(double alpha);

    // compute Value at Risk
    public abstract double getVar(double alpha);

    // compute variance
    public abstract double getVariance();

    // compute W for relevant p, categorical: p=2, quantile p=1
    public abstract double getW(ArrayList<Double> arr);

    // compute W for relevant p, categorical: p=2, quantile p=1
    public abstract double getW(DiscreteDistribution arr);

    // Compute W for relevant p, categorical: p=2, quantile p=1
    public abstract double getW(ArrayList<Double> arr, ArrayList<Double> arr2 )

    // get the probability values of the distribution
    public abstract ArrayList<Double> getValues();

    // get the support values of the distribution
    public abstract ArrayList<Double> getValues();
}