package explicit;

import java.util.Iterator;
import java.util.Map;

abstract class DistributionalBellman {

    int atoms = 1;
    double delta_z = 1;
    int nactions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;

    public DistributionalBellman(){}

    public void setAlpha(double a){ alpha=a;}

    public abstract double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it);

    public abstract double [] update_support(double gamma, double state_reward, double []sum_p);

    public abstract double getValue(double [] temp);

    public abstract double getValueCvar(double [] probs, double lim);

    // Wp with p=2
    public abstract double getW(double[] dist1, double[] dist2);

    // Wp with p=2
    public abstract double getW(double[] dist1, int state);

    public abstract void initialize(int n);

    public abstract void update(double [] temp, int state);

    public abstract double[] getDist(int i);

    public abstract double [][] getDist();
}
