package explicit;


import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.*;
import static java.lang.Math.sqrt;

public class DistributionalBellmanCategoricalAugmented extends DistributionalBellmanAugmented {
    int atoms = 1;
    double delta_z = 1;
    double [] z ;
    double [][][][] p;
    int n_actions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;
    int numStates;

    // slack variable b
    int b_atoms;
    double delta_b;
    double [] b; // array containing b values

    prism.PrismLog mainLog;
    DecimalFormat df;

    // new constructor to take b into account
    // should this have its own bounds? b_min and b_max?
    public DistributionalBellmanCategoricalAugmented(int atoms, int b_atoms, double vmin, double vmax, int numStates, int n_actions, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.z = new double[atoms];
        this.b = new double[b_atoms];
        this.delta_z = (vmax - vmin) / (atoms -1);
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        this.n_actions = n_actions;
        this.mainLog = log;
        df = new DecimalFormat("0.000");

        // INFO right now saving augmented state-action distributions
        this.p = new double[numStates][b_atoms][n_actions][atoms]; 

        // Initialize distribution atoms 
        for (int i = 0; i < atoms; i++) {
            this.z[i] = (vmin + i *this.delta_z);
        }
        log.println(" z: "+ Arrays.toString(z));

        // Initialize slack variable atoms 
        this.b_atoms = b_atoms;
        this.delta_b = (vmax - vmin) / (b_atoms -1);
        for (int i = 0; i < b_atoms; i++) {
            this.b[i] = (vmin + i *this.delta_b);
        }
    }

    public DistributionalBellmanCategoricalAugmented(DistributionalBellmanCategoricalAugmented el)
    {
        super(el);

        atoms = el.atoms;
        z = Arrays.copyOf(el.z, atoms);
        delta_z = (el.v_max - el.v_min) / (atoms -1);
        v_min = el.v_min;
        v_max = el.v_max;
        numStates = el.numStates;
        n_actions = el.n_actions;
        mainLog = el.mainLog;

        // Initialize slack variable atoms
        b_atoms = el.b_atoms;
        delta_b = el.delta_b;
        b = Arrays.copyOf(el.b, b_atoms);

        // Deep copy distribution
        this.p = new double[numStates][b_atoms][n_actions][atoms];
        for (int s=0; s<numStates; s++) {
            for (int idx_b = 0; idx_b < b_atoms; idx_b++) {
                for (int a = 0; a < n_actions; a++) {
                    p[s][idx_b][a] = Arrays.copyOf(el.p[s][idx_b][a], atoms);
                }
            }
        }

    }

    @Override
    public DistributionalBellmanAugmented copy() {
        return new DistributionalBellmanCategoricalAugmented(this);
    }


    public double [] getZ()
    {
        return this.z;
    }

    //  Initializing with augmented state and actions.
    // FIXME sending numStates is redundant?
    @Override
    public void initialize( int numStates) {

        this.p = new double[numStates][b_atoms][n_actions][atoms];
        double [] temp2 = new double[atoms];
        temp2[0] =1.0;

        for (int i = 0; i < numStates; i++) {
            for (int idx_b=0; idx_b < b_atoms; idx_b++){
                for (int a = 0; a<n_actions; a++){
                    this.p[i][idx_b][a]= Arrays.copyOf(temp2, temp2.length);
                }
            }
        }
    }


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
    public int getClosestB(double temp_b){
        double new_b = max(b[0], min(temp_b,b[b_atoms-1]));
        double index = new_b/delta_b;
        int l= (int) floor(new_b); int u= (int) ceil(new_b);

        // TODO : right now I'm choosing a slightly more lax approach by 
        // choosing lower index -> intuition :"we have used less budget than we actually have"
        return l;
    }

    @Override
    public void display() {
        for (int s=0; s<numStates; s++) {
            display(s);
        }
    }

    @Override
    public void display(MDP mdp) {
        for (int s=0; s<numStates; s++) {
            display(s, mdp.getNumChoices(s));
        }
    }

    @Override
    public void display(int s) {
        mainLog.println("------- state:"+s);
        for (int idx_b = 0; idx_b < b_atoms; idx_b++) {
            mainLog.println("------");
            for (double[] doubles : p[s][idx_b]) {
                mainLog.print("[");
                Arrays.stream(doubles).forEach(e -> mainLog.print(df.format(e) + ", "));
                mainLog.print("]\n");
            }
        }

    }

    public void display(int s, int num_actions) {
        mainLog.println("------- state:"+s);
        for (int idx_b = 0; idx_b < b_atoms; idx_b++) {
            mainLog.println("------ b:"+df.format(b[idx_b]));
            for (int j =0; j< num_actions; j++) {
                mainLog.print("[");
                Arrays.stream(p[s][idx_b][j]).forEach(e -> mainLog.print(df.format(e) + ", "));
                mainLog.print("]\n");
            }
        }

    }

    public void display(int s, int [][] policy) {

        for (int idx_b = 0; idx_b < b_atoms; idx_b++) {
            double[] doubles = p[s][idx_b][policy[s][idx_b]];
            mainLog.print("[");
            Arrays.stream(doubles).forEach(e -> mainLog.print(df.format(e) + ", "));
            mainLog.print("]\n");

        }

    }

    @Override
    public void update(double [] temp, int state, int idx_b, int action){
        p[state][idx_b][action] = Arrays.copyOf(temp, temp.length);
    }


    @Override
    public double[][] getDist(int s, int idx_b) {
        return p[s][idx_b];
    }

    @Override
    public double[] getDist(int s, int idx_b, int a) {
        return p[s][idx_b][a];
    }

    // TODO probably rename this
    // Compute inner optimization from Bauerle and Ott
    // paper : Markov Decision Processes with Average-Value-At-Risk Criteria
    //  E[[dist-b]+]
    @Override
    public double getMagic(double [] temp, int idx_b)
    {
        int res = 0;
        for (int j=0; j<atoms; j++){
            res += temp[j] * max(0, (z[j] - b[idx_b]));
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

    public double getValueCvar(double [] probs, double lim, int idx_b){
        double res = 0;
        int expected_c= 0;
        for (int i=0; i<atoms; i++){
            if (probs[i] > 0){
                expected_c += probs[i] * max(0, z[i]-b[idx_b]);
            }
        }

        res = b[idx_b] + 1/(1-lim) * expected_c;

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

        for( int j = 0; j<atoms; j++) {
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
    public double getW(double [] dist1, int state, int idx_b, int idx_a)
    {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+=  pow(((delta_z) *dist1[i] - (delta_z) *p[state][idx_b][idx_a][i]), 2);
        }
        return sqrt(sum);
    }

    public double [][][][] getP ()
    {
        return p;
    }
}