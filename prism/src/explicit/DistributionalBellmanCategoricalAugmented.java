package explicit;


import explicit.rewards.MDPRewards;
import explicit.rewards.StateRewardsArray;
import prism.PrismException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

import static java.lang.Math.*;
import static java.lang.Math.sqrt;

public class DistributionalBellmanCategoricalAugmented extends DistributionalBellmanAugmented {
    int atoms = 1;
    double delta_z = 1;
    double [] z ;
    double [][] p;
    int n_actions = 4;
    double v_min ;
    double v_max ;
    double b_min; double b_max;
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
    public DistributionalBellmanCategoricalAugmented(int atoms, int b_atoms, double vmin, double vmax, double bmin, double bmax, int numStates, int n_actions, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.z = new double[atoms];
        this.b = new double[b_atoms];
        if (atoms > 1) {
            this.delta_z = (vmax - vmin) / (atoms - 1);
        }
        else {
            this.delta_z = 0;
        }
        this.v_min = vmin;
        this.v_max = vmax;
        this.b_min = bmin;
        this.b_max = bmax;
        this.numStates = numStates;
        this.n_actions = n_actions;
        this.mainLog = log;
        df = new DecimalFormat("0.000");

        // INFO right now saving augmented state-action distributions
        this.p = new double[numStates][atoms];

        // Initialize distribution atoms 
        for (int i = 0; i < atoms; i++) {
            this.z[i] = (vmin + i *this.delta_z);
        }
        log.println(" z: "+ Arrays.toString(z));

        // Initialize slack variable atoms 
        this.b_atoms = b_atoms;
        if (b_atoms >1) {
            this.delta_b = (bmax - bmin) / (b_atoms - 1);
        } else {
            this.delta_b = 0;
        }
        for (int i = 0; i < b_atoms; i++) {
            this.b[i] = (bmin + i *this.delta_b);
        }
        log.println(" b: "+ Arrays.toString(b));

    }

    public DistributionalBellmanCategoricalAugmented(DistributionalBellmanCategoricalAugmented el)
    {
        super(el);
        df = el.df;
        alpha = el.alpha;
        atoms = el.atoms;
        z = Arrays.copyOf(el.z, atoms);
        if (el.atoms > 1) {
            delta_z = (el.v_max - el.v_min) / (atoms - 1);
        }
        else{
            delta_z = 0;
        }
        v_min = el.v_min;
        v_max = el.v_max;
        b_min = el.b_min;
        b_max = el.b_max;
        numStates = el.numStates;
        n_actions = el.n_actions;
        mainLog = el.mainLog;

        // Initialize slack variable atoms
        b_atoms = el.b_atoms;
        delta_b = el.delta_b;
        b = Arrays.copyOf(el.b, b_atoms);

        // Deep copy distribution
        this.p = new double[numStates][atoms];
        for (int s=0; s<numStates; s++) {
            p[s] = Arrays.copyOf(el.p[s], atoms);
        }

        // Deep Copy product MDP
        prod_mdp = new CVaRProduct(el.prod_mdp);
    }

    @Override
    public DistributionalBellmanAugmented copy() {
        return new DistributionalBellmanCategoricalAugmented(this);
    }

    //  Initializing with augmented state and actions.
    @Override
    public void initialize( MDP mdp, MDPRewards mdpRewards, double gamma, BitSet target) throws PrismException {

        prod_mdp = CVaRProduct.makeProduct(this, mdp, mdpRewards, gamma, target, mainLog);

        // Update to augmented states
        numStates = prod_mdp.getProductModel().getNumStates();

        mainLog.println("#b: "+b_atoms+ " atoms: "+atoms+" Max Choices: "+n_actions);
        mainLog.println("Size of probability array: "+numStates*atoms);

        this.p = new double[numStates][atoms];
        double [] temp2 = new double[atoms];
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
        double index = 0;
        // INFO do I need to use transition probability -> prob not since R(s,a) and not R(s,a,s')

        for (int j =0; j<atoms; j++){
            
            double temp = max(v_min, min(v_max, state_reward+gamma*z[j]));
            if (delta_z > 0) {
                index = (temp - v_min) / delta_z;
            }
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
    // choosing lower index -> intuition :"we have used less budget than we actually have"
    // opposite of chap 7 -> they take floor since they are doing max and we are doing min -> cost approach
    public int getClosestB(double temp_b){
        double new_b = max(b_min, min(temp_b,b_max)); double index = 0;
        if (delta_b > 0){
            index = new_b/delta_b;
        }
        int l= (int) floor(index); int u= (int) ceil(index);
        return l;
//        double diff_l = abs(new_b - b[l]);
//        double diff_u = abs(b[u] - new_b);
//        // check which index is closest:
//        if (diff_u >= diff_l){
//            return l;
//        }
//        else {
//            return u;
//        }
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
            display(s);
        }
    }

    @Override
    public void display(int s) {
        mainLog.println("------- state:"+s);
        int idx_b = prod_mdp.getAutomatonState(s);
        mainLog.println("------ b:"+df.format(b[idx_b]));
        mainLog.print("[");
        Arrays.stream(p[s]).forEach(e -> mainLog.print(df.format(e) + ", "));
        mainLog.print("]\n");
    }

//    public void display(int s) {
//        double[] doubles = p[s];
//        mainLog.print("[");
//        Arrays.stream(doubles).forEach(e -> mainLog.print(df.format(e) + ", "));
//        mainLog.print("]\n");
//    }

    @Override
    public void update(double [] temp, int state){
        p[state]= Arrays.copyOf(temp, temp.length);
    }


    @Override
    public double[] getDist(int s) {
        return p[s];
    }

    // TODO probably rename this
    // Compute inner optimization from Bauerle and Ott
    // paper : Markov Decision Processes with Average-Value-At-Risk Criteria
    //  E[[dist-b]+]
    @Override
    public double getMagic(double [] temp, int idx_b)
    {
        double res = 0;
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

    @Override
    public double getProbThreshold(double [] probs, double lim){
        double res = 0.0;

        for(int j=atoms-1; j>=0; j--){
            if (z[j] >= lim){
                res += probs[j];
            } else{
                break;
            }
        }

        return res;
    }

    // Wp with p=2 -> cramer distance
    @Override
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

    // Wp with p=2
    @Override
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

//    // Kolmogorov-Smirnov test
//    @Override
//    public double getKSTest(double [] dist, int state, int idx_a)
//    {
//        KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
//    }

    public double [][] getP ()
    {
        return p;
    }

    // Find the starting that minimizes CVAR at initial state based on a given alpha
    public double [] computeStartingB( double alpha){
        double [] res = new double [3]; // contains the min index + min cvar.
        double cvar = 0;
        res [1] = Float.POSITIVE_INFINITY;
        res[2] = -1;
        double expected_cost =0;
        int idx_b = 0;

        Iterable<Integer> initials = prod_mdp.getProductModel().getInitialStates();
        Iterator<Integer> initials_it = initials.iterator();
        int startStateIdx = 0;
        // iterate over initial states
        // -> this should correspond to starting state of MDP + all possible values of b
        while(initials_it.hasNext()){
            expected_cost = 0;
            int prod_state = initials_it.next();
            idx_b = prod_mdp.getAutomatonState(prod_state);
            for ( int i =0; i < atoms; i++){
                double j = p[prod_state][i];
                if (j >0){
                    expected_cost += j * max(0, z[i] - b[idx_b]);
                }
            }
            cvar = b[idx_b] + 1/(1-alpha) * expected_cost;
            if (cvar <= res[1]){
                res[0] = idx_b;
                res[1] = cvar;
                res[2] = prod_state;
            }

            startStateIdx +=1;
        }
        return res;
    }

    public int [] getStrategy(MDPRewards mdpRewards, StateRewardsArray rewardsArray, int [] choices, double alpha) throws PrismException {
        int prodNumStates = prod_mdp.getProductModel().getNumStates();
        int [] res = new int [prodNumStates];

        double [] cvar_info = computeStartingB(alpha);
        int idx_b = (int) cvar_info[0];

        mainLog.println("b :"+b[idx_b] + " cvar = " + cvar_info[1]+" start="+cvar_info[2]);

        //  Update product mdp initial state to correct b
        if (prod_mdp.productModel instanceof ModelExplicit) {
            if ((int)cvar_info[2] != -1) {
                ((ModelExplicit) prod_mdp.productModel).clearInitialStates();
                ((ModelExplicit) prod_mdp.productModel).addInitialState((int) cvar_info[2]);
            }
            else {
                throw new PrismException("Error: stategy was not able to find an initial state.");
            }
        }
        else {
            throw new PrismException("Error updating initial states productMDP is not an instance of ModelExplicit");
        }

        mainLog.println("\nV[0] at state: " + (int)cvar_info[2]
                + " original model:" + prod_mdp.getModelState((int)cvar_info[2])
                + " b:"+ b[idx_b] + " alpha:" + alpha);
        this.display((int)cvar_info[2]);

        double r ;
        for (int i = 0; i < prodNumStates; i++) {
            res[i] = choices[i];
            // Compute reward
            r = mdpRewards.getStateReward(prod_mdp.getModelState(i)) ;
            r += mdpRewards.getTransitionReward(prod_mdp.getModelState(i), res[i]);

            rewardsArray.setStateReward(i, r);

//            mainLog.println ("policy: "+res[i]+" - rew:"+r+" - new b :"+b[prod_mdp.getAutomatonState(i)]);
        }

        return res;
    }

    @Override
    public double [] adjust_support(TreeMap distr)
    {
        int entry_key; double entry_val; double temp; double index=0;
        double [] m = new double[atoms];

        for(Object e: distr.entrySet())
        {
            entry_key = (int) ((Map.Entry<?, ?>) e).getKey();
            entry_val = (double) ((Map.Entry<?, ?>) e).getValue();

            temp = max(v_min, min(v_max, entry_key));
            if (delta_z > 0) {
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
    public CVaRProduct getProductMDP() {
        return prod_mdp;
    }

    @Override
    public int getB_atoms(){
        return b_atoms;
    }

    @Override
    public double getBVal(int idx){
        return b[idx];
    }

    @Override
    public double [] getB() {return b;}

    @Override
    public void writeToFile(int state, String filename){
        if (filename == null) {filename="distr_cvar_c51.csv";}
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