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

public class DistributionalBellmanCategoricalProb {
    int atoms = 1;
    double delta_z = 1;
    double [] z ;
    double [][] p;
    int nactions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;
    int numTransitions;
    prism.PrismLog mainLog;

    public DistributionalBellmanCategoricalProb(int atoms, double vmin, double vmax, int numTransitions, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.z = new double[atoms];
        // TODO add parameter distribution here
        if (atoms > 1) {
            this.delta_z = (vmax - vmin) / (atoms - 1);
        }
        else {
                this.delta_z = 0;
        }
        this.v_min = vmin;
        this.v_max = vmax; // Vmin and vmix should be [0, 1]
        this.numTransitions = numTransitions;
        this.mainLog = log;

        for (int i = 0; i < atoms; i++) {
            this.z[i] = (vmin + i *this.delta_z);
        }
        // this.p = new double [paramAtoms][numTransitions][atoms]
        this.p = new double [numTransitions][atoms];
    }


    public double [] getZ()
    {
        return this.z;
    }

    // Update this.p = new double [paramAtoms][numTransitions][atoms]
    public void initialize( int numTransitions) {

        this.p = new double[numTransitions][this.atoms];
        double [] temp2 = new double[this.atoms];
        temp2[0] =1.0;
        for (int i = 0; i < numTransitions; i++) {

            this.p[i]= Arrays.copyOf(temp2, temp2.length);
        }
    }

    // @Override
    // , double [] distribution
    public double [] step(Iterator<Map.Entry<Integer, Distribution>> trans_it, int numTransitions, double gamma, double state_reward)
    {
        double [][] res = update_probabilities(trans_it);
        for (int i = 0; i<paramAtoms; i++){
            res[i] = update_support(gamma, state_reward, res[i]);
        }
        return res;
    }

    // updates probabilities for one action
    public double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it) {
        double [][] sum_p= new double[atoms];
        int transition_prob = 0;
        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();
            for (int i=0; i<paramAtoms; i++){
                transition_prob =  e.getValue();
                for (int j = 0; j < atoms; j++) {
                    sum_p[j] += transition_prob[i] * p[e.getKey()][j];
                }
            }

        }
        return sum_p;
    }

    // Shift distribution using discount and reward, then perform projection
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

    // @Override
    public double[] getDist(int i) {
        return p[i];
    }

    // @Override
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

    // @Override
    public double getExpValue(double [] temp){
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z[j] * temp[j];
        }
        return sum;
    }

    // @Override
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

    // @Override
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

    // Get variance for a distribution <probs>
    // @Override
    public double getVariance(double[] probs) {
        double mu = getExpValue(probs);
        double res = 0.0;

        for( int j = 0; j<atoms; j++)
        {
            res += (1.0 / atoms) * pow(((probs[j] * z[j]) - mu), 2);
        }

        return res;
    }

    // distributional distance l2 with p=2 between two distributions
    public double getW(double[] dist1, double[] dist2)
    {
        double sum = 0;
        double [] cum_p = new double[2];
        for (int i =0; i<atoms; i++)
        {
            cum_p[0] += dist1[i];
            cum_p[1] += dist2[i];
            sum+= pow((cum_p[0] - cum_p[1]), 2) * delta_z;
        }
        return sqrt(sum);
    }

    // distributional distance l2 with p=2 between
    // a distribution <dist1> and the saved distribution for state <state>
    public double getW(double [] dist1, int state)
    {
        double sum = 0;
        double [] cum_p = new double[2];
        for (int i =0; i<atoms; i++)
        {
            cum_p[0] += dist1[i];
            cum_p[1] += p[state][i];
            sum+= pow((cum_p[0] - cum_p[1]), 2) * delta_z;
        }
        return sqrt(sum);
    }

    // Get full saved distributions for all states
    public double [][] getP ()
    {
        return p;
    }

    // Log distribution for a state to a file <filename> as a csv with columns : support index, probability, support value
    // @Override
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