package explicit;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.*;
import static java.lang.Math.sqrt;

public class DistributionalBellmanCategorical extends DistributionalBellman {
    int atoms = 1;
    double delta_z = 1;
    double [] z ;
    double [][] p;
    int nactions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;
    int numStates;
    prism.PrismLog mainLog;

    public DistributionalBellmanCategorical(int atoms, double vmin, double vmax, int numStates, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.z = new double[atoms];
        if (atoms > 1) {
            this.delta_z = (vmax - vmin) / (atoms - 1);
        }
        else {
                this.delta_z = 0;
        }
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        this.mainLog = log;

        for (int i = 0; i < atoms; i++) {
            this.z[i] = (vmin + i *this.delta_z);
        }
        this.p = new double [numStates][atoms];
    }


    public double [] getZ()
    {
        return this.z;
    }

    public void initialize( int numStates) {

        this.p = new double[numStates][this.atoms];
        double [] temp2 = new double[this.atoms];
        temp2[0] =1.0;
        for (int i = 0; i < numStates; i++) {

            this.p[i]= Arrays.copyOf(temp2, temp2.length);
        }
    }

    public double [] step(Iterator<Map.Entry<Integer, Double>> trans_it, int numTransitions, double gamma, double state_reward)
    {
        double [] res = update_probabilities(trans_it);
        res = update_support(gamma, state_reward, res);
        return res;
    }

    // updates probabilities for 1 action
    public double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it) {
        double [] sum_p= new double[atoms];
        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();

            for (int j = 0; j < atoms; j++) {
                sum_p[j] += e.getValue() * p[e.getKey()][j];
            }

        }
        return sum_p;
    }

    public double [] update_support(double gamma, double state_reward, double []sum_p){

        double [] m = new double [atoms];
        double b = 0;
        for (int j =0; j<atoms; j++){
            double temp = max(v_min, min(v_max, state_reward+gamma*z[j]));
            if (delta_z > 0.0) {
                 b = (temp - v_min)/delta_z;
            }
            int l= (int) floor(b); int u= (int) ceil(b);

            if ( l- u != 0){
                m[l] += sum_p[j] * (u -b);
                m[u] += sum_p[j] * (b-l);
            } else{
                m[l] += sum_p[j];
            }
        }

        return m;
    }

    public void update(double [] temp, int state){
        p[state] = Arrays.copyOf(temp, temp.length);
    }

    @Override
    public double[] getDist(int i) {
        return p[i];
    }

    @Override
    public double[] adjust_support(TreeMap distr) {
        int entry_key; double entry_val; double temp; double index = 0;
        double [] m = new double[atoms];

        for(Object e: distr.entrySet())
        {
            entry_key = (int) ((Map.Entry<?, ?>) e).getKey();
            entry_val = (double) ((Map.Entry<?, ?>) e).getValue();

            temp = max(v_min, min(v_max, entry_key));
            if (delta_z >0) {
                index = (temp - v_min) / delta_z;
            }
            int l= (int) floor(index); int u= (int) ceil(index);

            if ( l- u != 0){
                m[l] += entry_val * (u -index);
                m[u] += entry_val * (index-l);
            } else{
                m[l] += entry_val;
            }

        }

        return m;
    }

    @Override
    public double getExpValue(double [] temp){
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z[j] * temp[j];
        }
        return sum;
    }

    @Override
    public double getValueCvar(double [] probs, double lim){
        double res =0.0;
        double sum_p =0.0;
        double denom = 0.0;
        for (int i=atoms-1; i>=0; i--){
            if (sum_p < lim){
                if(sum_p+ probs[i] < lim){
                    sum_p += probs[i];
                    res += (1/lim) * probs[i] * z[i];
                } else{
                    denom = lim - sum_p;
                    sum_p += denom;
                    res += (1/lim) *denom*z[i];
                }
            }
        }

        return res;
    }

    @Override
    public double getVar(double [] probs, double lim){
        double sum_p = 0.0;
        double res = 0.0;
        for(int j=atoms-1; j>=0; j--){
            if (sum_p < lim){
                if(sum_p + probs[j] < lim){
                    sum_p += probs[j];
                }
                else{
                    res = z[j];
                }
            }
        }

        return res;
    }

    @Override
    public double getVariance(double[] probs) {
        double mu = getExpValue(probs);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((probs[j] * z[j]) - mu), 2);
        }

        return res;
    }

    // Wp with p=2
    public double getW(double[] dist1, double[] dist2)
    {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+= pow(((delta_z)*dist1[i] - (delta_z)*dist2[i]), 2);
        }
        return sqrt(sum);
    }

    // Wp with p=2
    public double getW(double [] dist1, int state)
    {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+=  pow(((delta_z) *dist1[i] - (delta_z) *p[state][i]), 2);
        }
        return sqrt(sum);
    }

    public double [][] getP ()
    {
        return p;
    }

    @Override
    public void writeToFile(int state, String filename){
        if (filename == null) {filename="distr_exp_c51.csv";}
        try (PrintWriter pw = new PrintWriter(new File("prism/"+filename))) {
            pw.println("r,p,z");
            for (int r = 0; r < atoms; r++) {
                Double prob = p[state][r];
                prob = (prob == null) ? 0.0 : prob;
                pw.println(r + "," + prob+","+z[r]);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}