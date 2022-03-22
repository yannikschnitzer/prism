package explicit;

import java.util.Iterator;
import java.util.Map;

abstract class DistributionalBellman {

    int atoms = 1;
    int nactions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;

    public DistributionalBellman(){}

    public void setAlpha(double a){ alpha=a;}

    public abstract double [] step(Iterator<Map.Entry<Integer, Double>> trans_it, int numTransitions, double gamma, double state_reward);

    public abstract double getExpValue(double [] temp);

    public abstract double getValueCvar(double [] probs, double lim);

    public abstract double getVar(double [] probs, double lim);

    public abstract double getVariance(double [] probs);

    // Wp with p=2
    public abstract double getW(double[] dist1, double[] dist2);

    // Wp with p=2
    public abstract double getW(double[] dist1, int state);

    public abstract void initialize(int n);

    public abstract void update(double [] temp, int state);

    public abstract double[] getDist(int i);

//    public abstract double [][] getDist();
}
