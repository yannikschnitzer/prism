package explicit;

import prism.Prism;
import prism.PrismLog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.*;
import static java.lang.Math.ceil;

public class DistributionalBellmanQR extends DistributionalBellman {

    double [][] z ;
    double [] p;
    double alpha=1;
    int numStates;
    int atoms = 1;
    double delta_p = 1;
    prism.PrismLog mainLog;
    double [] tau;


    public DistributionalBellmanQR(int atoms, int n, PrismLog log)
    {
        super();
        this.atoms = atoms;
        this.numStates = n;
        mainLog = log;
        this.delta_p = 1.0/atoms;
        this.p = new double[atoms];
        this.tau = new double[atoms];
        z = new double[n][atoms];

        for (int i = 1; i <= atoms; i++) {
            this.tau[i-1] = ( (i) *this.delta_p);
            this.p[i-1] =  delta_p;
        }
    }

    @Override
    public double getValue(double[] temp) {
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= p[j] * temp[j];
        }
        return sum;
    }

    @Override
    public double getValueCvar(double[] probs, double lim) {
        double res =0.0;
        double sum_p =0.0;
        double denom = 0.0;
        double [] temp = Arrays.copyOf(probs, probs.length);
        Arrays.sort(temp);
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < lim){
                if(sum_p+ p[i] < lim){
                    sum_p += p[i];
                    res += (1/lim) * temp[i] * p[i];
                } else{
                    denom = lim - sum_p;
                    sum_p += denom;
                    res += (1/lim) *denom*p[i];
                }
            }
        }

        return res;
    }

    @Override
    public double getW(double[] dist1, double[] dist2) {
        double sum = 0;
        for (int i =0; i<atoms-1; i++)
        {
            sum+= (p[i+1] - p[i]) * pow((dist1[i] - dist2[i]), 2);
        }
        return sqrt(sum);
    }

    @Override
    public double getW(double[] dist1, int state) {
        double sum = 0;
        for (int i =0; i<atoms-1; i++)
        {
            sum+= (p[i+1] - p[i]) * pow((dist1[i] - z[state][i]), 2);
        }
        return sqrt(sum);
    }

    public double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it, int numSuccessors){
        double [] sup = new double[atoms];
        int sum =0;
        double [] ind = new double[numSuccessors];
        int [] next = new int[numSuccessors];
        int i=0; double rem_p;
        while (trans_it.hasNext()) {
            Map.Entry<Integer, Double> e = trans_it.next();
            double temp = new BigDecimal(e.getValue()/delta_p).setScale(2, RoundingMode.HALF_UP).doubleValue();
            sum+= temp;
            ind[i] = sum;
            next[i] = e.getKey();
            i+=1;
        }
        i=0;
        for (int j = 0; j < atoms; j++) {
            if (j+1 <= floor(ind[i])) {
                sup[j] = z[next[i]][j];
            } else {
                rem_p=  ind[i] - floor(ind[i]);
                if (rem_p > 0){
                   sup[j] = rem_p * z[next[i]][j] + (1-rem_p) * z[next[i+1]][j];
                }
                else sup[j] = z[next[i+1]][j];
                i+=1;
            }
        }
        return sup;
    }

    public double [] update_support(double gamma ,double state_reward,double[] sum_z) {
        double [] m = new double [atoms];

        for (int j =0; j<atoms; j++){
            double temp = (state_reward+gamma*sum_z[j]);
            m[j] += temp;
        }

        return m;
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
