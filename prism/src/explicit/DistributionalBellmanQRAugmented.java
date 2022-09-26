package explicit;


import edu.jas.util.MapEntry;
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

public class DistributionalBellmanQRAugmented extends DistributionalBellmanAugmented {
    int atoms = 1;
//    double delta_z = 1;
    double [][][] z ;
    double [] p;
    int n_actions = 4;
    double b_min; double b_max;
    double alpha=1;
    int numStates;
    double delta_p = 1;
    double [] tau_hat;

    // slack variable b
    int b_atoms;
    double delta_b;
    double [] b; // array containing b values

    prism.PrismLog mainLog;
    DecimalFormat df;

    // new constructor to take b into account
    // should this have its own bounds? b_min and b_max?
    public DistributionalBellmanQRAugmented(int atoms, int b_atoms, double bmin, double bmax, int numStates, int n_actions, prism.PrismLog log){
        super();
        this.atoms = atoms;
        // TODO potentially remove creation of array here
        this.z = new double[numStates][n_actions][atoms];
        this.b = new double[b_atoms];
        this.p = new double[atoms];
        this.tau_hat = new double[atoms];
        this.b_min = bmin;
        this.b_max = bmax;
        this.numStates = numStates;
        this.n_actions = n_actions;
        this.mainLog = log;
        this.delta_p = 1.0/atoms;
        df = new DecimalFormat("0.000");

        // Initialize distribution atoms
        for (int i = 0; i < atoms; i++) {
            this.tau_hat[i] = ( (2*i +1)*delta_p/2.0);
            this.p[i] =  delta_p;
        }
        log.println(" tau_hat: "+ Arrays.toString(tau_hat));

        // Initialize slack variable atoms
        this.b_atoms = b_atoms;
        if (b_atoms > 1) {
            this.delta_b = (bmax - bmin) / (b_atoms - 1);
        } else {
            this.delta_b = 0;
        }

        for (int i = 0; i < b_atoms; i++) {
            this.b[i] = (bmin + i *this.delta_b);
        }
        log.println(" b: "+ Arrays.toString(b));

    }

    public DistributionalBellmanQRAugmented(DistributionalBellmanQRAugmented el)
    {
        super(el);
        df = el.df;
        alpha = el.alpha;
        atoms = el.atoms;
        p = Arrays.copyOf(el.p, atoms);
        delta_p = el.delta_p;
        b_min = el.b_min;
        b_max = el.b_max;
        tau_hat = Arrays.copyOf(el.tau_hat, atoms);
        numStates = el.numStates;
        n_actions = el.n_actions;
        mainLog = el.mainLog;

        // Initialize slack variable atoms
        b_atoms = el.b_atoms;
        delta_b = el.delta_b;
        b = Arrays.copyOf(el.b, b_atoms);

        // Deep copy distribution
        this.z = new double[numStates][n_actions][atoms];
        for (int s=0; s<numStates; s++) {
            for (int a = 0; a < n_actions; a++) {
                z[s][a] = Arrays.copyOf(el.z[s][a], atoms);
            }
        }

        // Deep Copy product MDP
        prod_mdp = new CVaRProduct(el.prod_mdp);
    }

    @Override
    public DistributionalBellmanQRAugmented copy() {
        return new DistributionalBellmanQRAugmented(this);
    }

    //  Initializing with augmented state and actions.
    @Override
    public void initialize( MDP mdp, MDPRewards mdpRewards, double gamma, BitSet target) throws PrismException {

        prod_mdp = CVaRProduct.makeProduct(this, mdp, mdpRewards, gamma, target, mainLog);

        // Update to augmented states
        numStates = prod_mdp.getProductModel().getNumStates();

        mainLog.println("#b: "+b_atoms+ " atoms: "+atoms+" Max Choices: "+n_actions);
        mainLog.println("Size of probability array: "+numStates*n_actions*atoms);

        this.z = new double[numStates][n_actions][atoms];
    }


    public double [] step(Iterator<Map.Entry<Integer, Double>> trans_it, int [] choices, int numTransitions, double gamma, double state_reward)
    {
        ArrayList<MapEntry<Double, Double>> multimap = new ArrayList<>();
        double [] result;
        int action  =0;
        // Update based on transition probabilities and support values
        while (trans_it.hasNext()) {
            Map.Entry<Integer, Double> e = trans_it.next();
            for (int j = 0; j < atoms; j++) {
                action  = choices[e.getKey()];
                multimap.add(new MapEntry<>(delta_p * e.getValue(), gamma*z[e.getKey()][action][j] + state_reward));
            }
        }

        // Sort the list using lambda expression
        multimap.sort(Map.Entry.comparingByValue());

        // Consolidate based on probability
        result = consolidate(multimap.iterator());
        return result;
    }

    public double[] consolidate(Iterator<MapEntry<Double, Double>> it){
        double cum_p = 0.0;
        int index =0;
        Map.Entry<Double, Double> entry;
        double [] result = new double[atoms];

        while(it.hasNext() & index < atoms)
        {
            entry = it.next();
            cum_p += entry.getKey();
            if(cum_p >= tau_hat[index]) {
                result[index] = entry.getValue();
                index += 1;
            }
        }

        return result;
    }

    // Interpolate to find the closest b index
    public int getClosestB(double temp_b){
        double new_b = max(b_min, min(temp_b,b_max)); double index = 0;
        if (delta_b > 0){
            index = new_b/delta_b;
        }
        int l= (int) floor(index); int u= (int) ceil(index);

        double diff_l = abs(b[(int) index] - b[l]);
        double diff_u = abs(b[u] - b[(int) index]);

        // check which index is closest:
        if (diff_u >= diff_l){
            return l;
        }
        else {
            return u;
        }
    }

    @Override
    public void display() {
        for (int s=0; s<numStates; s++) {
            display(s);
        }
    }

    // FIXME remove one of these
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
        for (int j =0; j< prod_mdp.productModel.getNumChoices(s); j++) {
            mainLog.print("[");
            Arrays.stream(z[s][j]).forEach(e -> mainLog.print(df.format(e) + ", "));
            mainLog.print("]\n");
        }


    }

    public void display(int s, int [] choices) {
        double[] doubles = z[s][choices[s]];
        mainLog.print("[");
        Arrays.stream(doubles).forEach(e -> mainLog.print(df.format(e) + ", "));
        mainLog.print("]\n");
    }

    @Override
    public void update(double [] temp, int state, int action){
        z[state][action] = Arrays.copyOf(temp, temp.length);
    }


    @Override
    public double[][] getDist(int s) {
        return z[s];
    }

    @Override
    public double[] getDist(int s, int a) {
        return z[s][a];
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
            res += p[j] * max(0, (temp[j] - b[idx_b]));
        }

        return res;
    }


    // Compute expected value for a given augmented state.
    @Override
    public double getExpValue(double [] temp){
        double sum =0;
        for (int j=0; j<atoms; j++)
        {
            sum+= p[j] * temp[j];
        }
        return sum;
    }

    public double getValueCvar(double [] probs, double lim, int idx_b){
        double res = 0;
        int expected_c= 0;
        for (int i=0; i<atoms; i++){
            if (probs[i] > 0){
                expected_c += p[i] * max(0, probs[i]-b[idx_b]);
            }
        }

        res = b[idx_b] + 1/(1-lim) * expected_c;

        return res;
    }


    @Override
    public double getVar(double [] probs, double lim){
        double sum_p = 0.0;
        double res = 0.0;
        double [] temp = Arrays.copyOf(probs, probs.length);
        Arrays.sort(temp);

        for(int j=atoms-1; j>=0; j--){
            if (sum_p < lim){
                if(sum_p + p[j] < lim){
                    sum_p += p[j];
                }
                else{
                    res = temp[j];
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
            res += (1.0 / atoms) * pow(((probs[j] * p[j]) - mu), 2);
        }

        return res;
    }

    @Override
    public double getProbThreshold(double [] probs, double lim){
        double res = 0.0;
        double [] temp = Arrays.copyOf(probs, probs.length);
        Arrays.sort(temp);

        for(int j=atoms-1; j>=0; j--){
            if (temp[j] >= lim){
                res += p[j];
            } else{
                break;
            }
        }

        return res;
    }

    // Wp with p=1
    @Override
    public double getW(double[] dist1, double[] dist2)
    {
        double sum = 0;

        for (int i =0; i<atoms; i++)
        {
            sum+= abs((dist1[i]) - (dist2[i]));
        }
        return sum* (1.0/atoms);
    }

    // Wp with p=1
    @Override
    public double getW(double [] dist1, int state, int idx_a)
    {
        double sum = 0;
        for (int i =0; i<atoms; i++)
        {
            sum+= abs((dist1[i]) - (z[state][idx_a][i]));
        }
        return sum* (1.0/atoms);
    }

//    // Kolmogorov-Smirnov test
//    @Override
//    public double getKSTest(double [] dist, int state, int idx_a)
//    {
//        KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
//    }

    public double [][][] getZ ()
    {
        return z;
    }

    // Find the starting that minimizes CVAR at initial state based on a given alpha
    public double [] computeStartingB( double alpha, int [] choices){
        double [] res = new double [3]; // contains the min index + min cvar.
        double cvar = 0;
        res [1] = Float.POSITIVE_INFINITY;
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
                double j = z[prod_state][choices[prod_state]][i];
                if (j >0){
                    expected_cost += p[i] * max(0, j - b[idx_b]);
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

        double [] cvar_info = computeStartingB(alpha, choices);
        int idx_b = (int) cvar_info[0];

        mainLog.println("b :"+b[idx_b] + " cvar = " + cvar_info[1]+" start="+cvar_info[2]);

        //  Update product mdp initial state to correct b
        if (prod_mdp.productModel instanceof ModelExplicit) {
            ((ModelExplicit) prod_mdp.productModel).clearInitialStates();
            ((ModelExplicit) prod_mdp.productModel).addInitialState((int)cvar_info[2]);
        }
        else {
            throw new PrismException("Error updating initial states productMDP is not an instance of ModelExplicit");
        }

        mainLog.println("\nV[0] at state: " + (int)cvar_info[2]
                + " original model:" + prod_mdp.getModelState((int)cvar_info[2])
                + " b:"+ b[idx_b] + " alpha:" + alpha);
        this.display((int)cvar_info[2], choices);

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
        int entry_key; double entry_val;
        double [] m ;
        ArrayList<MapEntry<Double, Double>> multimap = new ArrayList<>();
        for(Object e: distr.entrySet())
        {
            entry_key = (int) ((Map.Entry<?, ?>) e).getKey();
            entry_val = (double) ((Map.Entry<?, ?>) e).getValue();
            multimap.add(new MapEntry<Double, Double>(entry_val, (double) entry_key));
        }

        // Sort the list based on values
        multimap.sort(Map.Entry.comparingByValue());

        // Consolidate based on probability
        m = consolidate(multimap.iterator());

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
    public void writeToFile(int state, int action, String filename){
        if (filename == null) {filename="distr_cvar_qr.csv";}
        try (PrintWriter pw = new PrintWriter(new File("prism/"+filename))) {
            pw.println("r,p,z");
            for (int r = 0; r < atoms; r++) {
                double val = z[state][action][r];
                pw.println(val + "," +p[r]+","+ tau_hat[r]);
            }
        } catch (FileNotFoundException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }
}