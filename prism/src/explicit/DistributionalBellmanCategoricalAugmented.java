package explicit;


import prism.PrismLog;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.*;
import static java.lang.Math.sqrt;

public class DistributionalBellmanCategoricalAugmented extends DistributionalBellman {
    int atoms = 1;
    double delta_z = 1;
    double [] z ;
    double [][] p;
    int nactions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;
    int numStates;

    // slack variable b
    int b_atoms;
    double delta_b;
    double [] b; // array containing b values

    prism.PrismLog mainLog;

    // TODO new constructor to take b into account
    // should this have its own bounds? b_min and b_max?
    public DistributionalBellmanCategoricalAugmented(int atoms, int b_atoms, double vmin, double vmax, int numStates, int n_actions, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.z = new double[atoms];
        this.delta_z = (vmax - vmin) / (atoms -1);
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        this.n_actions = n_actions;
        this.mainLog = log;

        // TODO right now saving augmented state-action distributions 
        this.p = new double[numStates][b_atoms][n_actions][atoms]; 

        // Initialize distribution atoms 
        for (int i = 0; i < atoms; i++) {
            this.z[i] = (vmin + i *this.delta_z);
        }
        
        // Initialize slack variable atoms 
        this.b_atoms = b_atoms;
        this.delta_b = (vmax - vmin) / (b_atoms -1);
        for (int i = 0; i < b_atoms; i++) {
            this.b[i] = (vmin + i *this.delta_b);
        }
    }


    public double [] getZ()
    {
        return this.z;
    }


    // TODO add option for initializing with augmented state and actions.
    // FIXME sending numStates is redundant?
    public void initialize( int numStates) {

        this.p = new double[numStates][b_atoms][n_actions][atoms];
        double [] temp2 = new double[atoms];
        temp2[0] =1.0;

        for (int i = 0; i < numStates; i++) {
            for (int idx_b; idx_b < b_atoms; idx_b++){
                for (int a = 0; a<n_actions; a++){
                    this.p[i][idx_b][a]= Arrays.copyOf(temp2, temp2.length);
                }
            }
        }
    }


    // FIXME how to pass current b + current choice action with class definitions
    public double [] step(Iterator<Map.Entry<Integer, Double>> trans_it, double cur_b, int choice, int numTransitions, double gamma, double state_reward)
    {
        double temp_b = (cur_b-state_reward)/gamma;
        int idx_b = getClosestB(temp_b);

        double [] res = update_probabilities(trans_it, idx_b, choice);
        res = update_support(gamma, state_reward, res);
        return res;
    }

    // updates probabilities for 1 action
    public double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it, int idx_b, int action) {
        double [] sum_p= new double[atoms];
        trans_it.reset(); // FIXME reset iterator
        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();

            for (int j = 0; j < atoms; j++) {
                sum_p[j] += e.getValue() * p[e.getKey()][idx_b][action][j];
            }

        }
        return sum_p;
    }

    public double [] update_support(double gamma, double state_reward, double []sum_p){

        double [] m = new double [atoms];
        trans_it.reset(); // FIXME reset iterator
        // FIXME do I need to use transition probability -> prob not since R(s,a) and not R(s,a,s')

        for (int j =0; j<atoms; j++){
            
            double temp = max(v_min, min(v_max, state_reward+gamma*z[j]));
            double index = (temp - v_min)/delta_z;
            int l= (int) floor(index); int u= (int) ceil(index);

            if ( l- u != 0){
                m[l] += sum_p[j] * (u -index);
                m[u] += sum_p[j] * (index-l);
            } else{
                m[l] += sum_p[j];
            }
            
        }

        return m;
    }

    // Interpolate to find the closest b index
    public int getClosestB(double b){
        int new_b = max(b[0], min(b[b_atoms-1]));
        double index = new_b/delta_b;
        int l= (int) floor(b); int u= (int) ceil(b);

        // TODO : right now I'm choosing a slightly more lax approach by 
        // choosing lower index -> intuition :"we have used less budget than we actually have"
        return l;
    }


    public void update(double [] temp, int state, int idx_b, int action){
        p[state] = Arrays.copyOf(temp, temp.length);
    }

    @Override
    public double[] getDist(int i) {
        return p[i];
    }

    @Override
    public double[][] getDist() {
        return p;
    }

    // TODO probably rename this
    // Compute inner optimization from Bauerle and Ott
    // paper : Markov Decision Processes with Average-Value-At-Risk Criteria
    //  E[[dist-b]+]
    public double getMagic(double [] temp, int idx_b)
    {
        int res = 0;
        for (int j=0; j<atoms; j++){
            res += temp[j] * max(0, (atoms[j] - b[idx_b]));
        }

        return res;
    }


    // Compute expected value for a given augmented state.
    @Override
    public double getExpValue(double [] temp){
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= z[j] * temp[j];
        }
        return sum;
    }

    // FIXME here we need to send augmented state
    @Override
    public double getValueCvar(double [][] probs, double lim){
        double [] res = new double [b_atoms];
        int min_b = 0;
        double min_cvar= 1000000;
        int expected_c;
        for (int idx_b = 0; idx_b< b_atoms; b++){
            expected_c = 0;
            for (int i=0; i<atoms; i++){
                if (probs[idx_b][i] > 0){
                    expected_c += probs[idx_b][i] * max(0, z[i]-b[idx_b]);
                }

            }
            res[idx_b] = b[idx_b] + 1/(1-lim) * expected_c;

            if (res[idx_b] < min_cvar){
                min_b = idx_b;
                min_cvar = res[idx_b];
            }
        }


        return res;
    }

    // TODO: change following functions to take into account slack variable
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

    // FIXME return [][][]
    public double [][] getP ()
    {
        return p;
    }
}