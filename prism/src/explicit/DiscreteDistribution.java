package explicit;

// import java.util.Iterator;
// import java.util.Map;
import java.text.DecimalFormat;
import java.util.ArrayList;

abstract class DiscreteDistribution {

    // Constructor
    public DiscreteDistribution(){}

    // initialize the distribution
    public abstract void clear();

    // Remove memory
    public abstract void empty();

    // Deep copy from source with same parameters
    public abstract void clone(DiscreteDistribution source);

    // project a given array to finite support
    public abstract void project(ArrayList<Double> arr);

    // project a given array to finite support (different distribution parameters but same number of atoms)
    public abstract void project(ArrayList<Double> probs, ArrayList<Double> supp);

    // project a given array to finite support (different distribution parameters but same number of atoms)
    public abstract void project(ArrayList<Double> probs, ArrayList<Double> supp, double vmin, double vmax);

    // project using a tree map
    public abstract void project(TreeMap<Double, Double> particles);

    // update saved distribution
    public abstract void update(ArrayList<Double> arr);

    // compute expected value of the distribution
    public abstract double getExpValue();

    public abstract double getExpValue(ArrayList<Double> temp);

    // compute CVaR with a given alpha
    public abstract double getCvarValue(double alpha);

    // compute CVaR with a given alpha
    public abstract double getCvarValue(ArrayList<Double> probs, double alpha);

    // compute Value at Risk
    public abstract double getVar(double alpha);

    // compute value at risk
    public abstract double getVar(ArrayList<Double> probs, double alpha);

    // compute variance
    public abstract double getVariance();

    // compute variance
    public abstract double getVariance(ArrayList<Double> probs);

    // compute W for relevant p, categorical: p=2, quantile p=1
    public abstract double getW(ArrayList<Double> arr);

    // compute W for relevant p, categorical: p=2, quantile p=1
    public abstract double getW(DiscreteDistribution arr);

    // Compute W for relevant p, categorical: p=2, quantile p=1
    public abstract double getW(ArrayList<Double> arr, ArrayList<Double> arr2 );

    // get the probability values of the distribution
    public abstract ArrayList<Double> getValues();

    // get the probability values of one atom
    public abstract Double getValue(int index);

    // get the support values of the distribution
    public abstract ArrayList<Double> getSupports();

    // get the support values of one atom
    public abstract Double getSupport(int index);

    public abstract String toString(DecimalFormat df);

    // Printing for files (csv
    public abstract String toFile();

    // Printing for files (csv
    public abstract String toFile(DecimalFormat df);

    // return the size of the distribution aka number of atoms
    public abstract int size();

    // return the number of atoms
    public abstract int getAtoms();
}