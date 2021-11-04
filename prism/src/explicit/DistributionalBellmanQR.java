package explicit;

import prism.Prism;
import prism.PrismLog;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class DistributionalBellmanQR extends DistributionalBellman {

    double [][] z ;
    double [] p;

    public DistributionalBellmanQR(int atoms, double v_min, double v_max, int n, PrismLog mainLog)
    {
        super();
    }

    @Override
    public double getValue(double[] temp) {
        return 0;
    }

    @Override
    public double getValueCvar(double[] probs, double lim) {
        return 0;
    }

    @Override
    public double getW(double[] dist1, double[] dist2) {
        return 0;
    }

    @Override
    public double getW(double[] dist1, int state) {
        return 0;
    }

    public double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it){
        return new double [atoms];
    }

    public double [] update_support(double gamma, double state_reward, double []sum_p) {
     return new double[atoms];
    }

    public void update(double [] temp, int state){
        z[state] = Arrays.copyOf(temp, temp.length);
    }

    @Override
    public double[] getDist(int i) {
        return z[i];
    }

    @Override
    public double[][] getDist() {
        return z;
    }

    @Override
    public void initialize(int n) {

    }

}
