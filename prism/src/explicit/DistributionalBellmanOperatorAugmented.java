package explicit;
//import java.io.File;
import explicit.rewards.MDPRewards;
import explicit.rewards.StateRewardsArray;
import prism.PrismException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.text.DecimalFormat;

import static java.lang.Math.*;
import static java.lang.Math.ceil;

public class DistributionalBellmanOperatorAugmented extends DistributionalBellman {
    int atoms; // FIXME this should be atoms per state or get it from the distribution
    DiscreteDistribution [] distr;
    double v_min ;
    double v_max ;
    int numStates;
    String distr_type;
    prism.PrismLog mainLog;
    DecimalFormat df = new DecimalFormat("0.000");
    boolean isCategorical ;
    boolean isAdaptive = false;
    double [] b; // array containing values of b
    double delta_b = 0;

    public DistributionalBellmanOperatorAugmented(int atoms, int b_atoms, double vmin, double vmax, double bmin, double bmax, int numStates, String distr_type, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        this.mainLog = log;
        this.distr_type  = distr_type;
        this.isCategorical = (distr_type.equals("C51"));

        // Initialize slack variable atoms
        if (b_atoms >1) {
            this.delta_b = (bmax - bmin) / (b_atoms - 1);
        }
        
        for (int i = 0; i < b_atoms; i++) {
            this.b[i] = (bmin + i *this.delta_b);
            if (i == b_atoms -1){
                b[i] = bmax;
            }
        }
        log.println(" b: "+ Arrays.toString(b));

        // Initialize distributions
        distr = new DiscreteDistribution [numStates];

        switch(distr_type)
        {
            case "C51":
                for (int i=0; i<numStates;i++){
                    if(isAdaptive){ //Adaptive
                        distr[i] = new DistributionCategorical(atoms, 5, log);
                    } else { //normal
                        distr[i] = new DistributionCategorical(atoms, vmin, vmax, log);
                    }

                }
                break;
            
            case "QR":
                isCategorical = false;
                for (int i=0; i<numStates;i++){
                    distr[i] = new DistributionQuantile(atoms, log);
                }
                break;
            
            default:
                distr_type = "C51";
                for (int i=0; i<numStates;i++){
                    distr[i] = new DistributionCategorical(atoms, vmin, vmax, log);
                }
        }

    }

    // Clear a specific state
    public void clear(int state){
        distr[state].clear();
    }

    // Clear all distr
    @Override
    public void clear(){
        for (DiscreteDistribution d : distr){
            d.clear();
        }
    }

    // Empty all distr
    @Override
    public void emptyAll(){
        for (DiscreteDistribution d : distr){
            d.empty();
        }
    }

    public double [] computeStartingB(double alpha, CVaRProduct prod_mdp)
    {
        double [] res = new double [3]; // contains the min index + min cvar + state in paroduct.
        double cvar; res[1] = Float.POSITIVE_INFINITY; res[2] = -1; int idx_b;
        Iterable<Integer> initials = prod_mdp.getProductModel().getInitialStates();
        // iterate over initial states
        // -> this should correspond to starting state of MDP + all possible values of b
        for (int prod_state : initials) {
            idx_b = prod_mdp.getAutomatonState(prod_state);
            cvar = distr[prod_state].getCvarValue(alpha, b[idx_b]);

            // save minimum cvar value initial state
            if (cvar <= res[1]) {
                res[0] = idx_b;
                res[1] = cvar;
                res[2] = prod_state;
            }
        }

        mainLog.println("b :"+b[(int)res[0]] + " cvar = " + res[1]+" start="+res[2]);

        return res;
    }

    public void setInitialStrategy(double alpha, CVaRProduct prod_mdp) throws PrismException {
        double [] cvar_info = computeStartingB(alpha, prod_mdp);
        int idx_b = (int) cvar_info[0];

        //  Update product mdp initial state to correct b
        if (prod_mdp.productModel instanceof ModelExplicit) {
            if ((int)cvar_info[2] != -1) { // Update the initial state of the product MDP
                ((ModelExplicit) prod_mdp.productModel).clearInitialStates();
                ((ModelExplicit) prod_mdp.productModel).addInitialState((int) cvar_info[2]);
            }
            else { throw new PrismException("Error: stategy was not able to find an initial state.");}
        }
        else { throw new PrismException("Error updating initial states productMDP is not an instance of ModelExplicit");}

        mainLog.println("\nV[0] at state: " + (int)cvar_info[2]
                + " original model:" + prod_mdp.getModelState((int)cvar_info[2])
                + " b:"+ b[idx_b] + " alpha:" + alpha);
        mainLog.println(this.toString((int)cvar_info[2], idx_b));
    }

    // Udpate the rewards model for product mdp
    public int [] getUpdatedRewards(MDPRewards mdpRewards, StateRewardsArray rewardsArray, int [] choices, CVaRProduct prod_mdp){
        double r; int [] res = new int [numStates];
        for (int i = 0; i < numStates; i++) {
            res[i] = choices[i];
            // Compute reward
            r = mdpRewards.getStateReward(prod_mdp.getModelState(i)) ;
            r += mdpRewards.getTransitionReward(prod_mdp.getModelState(i), res[i]);
            rewardsArray.setStateReward(i, r);
//            mainLog.println ("policy: "+res[i]+" - rew:"+r+" - new b :"+b[prod_mdp.getAutomatonState(i)]);
        }

        return res;
    }

    //  Update supports and values then return projected result
    @Override
    public DiscreteDistribution step(Iterator<Map.Entry<Integer, Double>> trans_it, double gamma, double state_reward, int cur_state)
    {
        // treemap for the updated particles distribution with the support as key and probabilities
        // using a treemap means it will automaticatlly be sorted based on the value
        TreeMap<Double, Double>  sum_p= new TreeMap<>();
        Double temp_supp, temp_value; int temp_atoms;
        DiscreteDistribution res = null;
        
        if(isCategorical){
            if(isAdaptive){
                res = new DistributionCategorical(atoms, 5, mainLog);
            } else {
                res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
            }
        } else {
            res = new DistributionQuantile(atoms, mainLog);
        }

        if (isCategorical && !isAdaptive){
            return this.optimized_step(trans_it, gamma, state_reward, cur_state, res);
        }
        else {
            // tree map construction
            while (trans_it.hasNext()) {
                Map.Entry<Integer, Double> e = trans_it.next();
                if (isCategorical){
                    double [] successor_p = distr[e.getKey()].getValues();
                    temp_atoms = distr[e.getKey()].getAtoms();
                    for (int j = 0; j < temp_atoms; j++) {
                        // new support value = cur state reward + discount * next state atom reward
                        temp_supp = state_reward+gamma*distr[e.getKey()].getSupport(j);
                        // new probability value = current probability of particle + transition probability * success probability of particle
                        temp_value = e.getValue() * successor_p[j];
                        
                        // if it already exists, increase probability; else, create particle
                        if(temp_value > 0) {
                            if (sum_p.containsKey(temp_supp)) {
                                sum_p.put(temp_supp, sum_p.get(temp_supp) + temp_value);
                            } else {
                                sum_p.put(temp_supp, temp_value);
                            }
                        }
                    }

                } else {
                    // all particle probability values  for the same state are the same for quantile
                    Double successor_p = distr[e.getKey()].getValue(0);
                    for (int j = 0; j < atoms; j++) {
                        // new support value = cur state reward + discount * next state atom reward
                        temp_supp = state_reward+gamma*distr[e.getKey()].getSupport(j);
                        // new probability value = current probability of particle + transition probability * success probability of particle
                        temp_value = e.getValue() * successor_p;

                        // if it already exists, increase probability; else, create particle
                        if(temp_value > 0){
                            if(sum_p.containsKey(temp_supp)){
                                sum_p.put(temp_supp, sum_p.get(temp_supp) + temp_value);
                            } else{
                                sum_p.put(temp_supp, temp_value);
                            }
                        }
                    }
                }
            }

            // Perform projection on the intermediate target
            res.project(sum_p);
        }

        return res;
    }

    public DiscreteDistribution optimized_step(Iterator<Map.Entry<Integer, Double>> trans_it, double gamma, double state_reward, int cur_state, DiscreteDistribution res){

        int temp_atoms = distr[0].getAtoms();
        double [] sum_p_cat = new double [temp_atoms];

        while(trans_it.hasNext()){
            Map.Entry<Integer, Double> e = trans_it.next();
            double [] successor_p = distr[e.getKey()].getValues();
            for (int j = 0; j < temp_atoms; j++) { 
                // increase probability
                sum_p_cat[j]+= e.getValue() * successor_p[j];
            }
        }

        res.project(sum_p_cat, gamma, state_reward);

        return res;
    }

    // TODO: case where transition prob is a distribution
    @Override
    public DiscreteDistribution step(Iterator<Map.Entry<Integer, DiscreteDistribution>> trans_it,
                                     double gamma, double state_reward, int cur_state, boolean isTransCategorical)
    {
        ArrayList<ArrayList<Double>> probs = update_probabilities(trans_it, isTransCategorical);
        ArrayList<ArrayList<Double>> supp = update_support(gamma, state_reward, cur_state, isTransCategorical);

        return combine_distr(probs, supp, isTransCategorical);
    }

    private DiscreteDistribution combine_distr(ArrayList<ArrayList<Double>> probs, ArrayList<ArrayList<Double>> supp, boolean isTransCategorical) {
        DiscreteDistribution res;
        if (isCategorical)
        {
            res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
        }
        else {
            res = new DistributionQuantile(atoms, mainLog);
        }

        //res.project(probs, supp);
        return res;
    }

    // updates probabilities for one action
    public ArrayList<Double> update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it) {
        ArrayList<Double>  sum_p= new ArrayList<> (atoms);
        sum_p.addAll(Collections.nCopies(atoms, 0.0));

        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();
            if (isCategorical)
            {
                double [] successor_p = distr[e.getKey()].getValues();
                for (int j = 0; j < atoms; j++) {
                    sum_p.set(j, sum_p.get(j) + e.getValue() * successor_p[j]);
                }
            } else{
                // for quantile all the values in succesor_p are the same
                double successor_p = distr[e.getKey()].getValue(0);

                for (int j = 0; j < atoms; j++) {
                    sum_p.set(j, sum_p.get(j) + e.getValue() * successor_p);
                }
            }

        }
        return sum_p;
    }

    // updates probabilities for one action
    // TODO: fix this for updated iteration with probability transition distributions
    public ArrayList<ArrayList<Double>> update_probabilities(Iterator<Map.Entry<Integer, DiscreteDistribution>> trans_it,
                                                             boolean isTransCategorical) {
        ArrayList<ArrayList<Double>> res = new ArrayList<>();
        ArrayList<Double>  sum_p= new ArrayList<> (atoms);
        sum_p.addAll(Collections.nCopies(atoms, 0.0));

        while (trans_it.hasNext()) {

            Map.Entry<Integer, DiscreteDistribution> e = trans_it.next();
           double [] successor_p = distr[e.getKey()].getValues();
            // FIXME There should be another loop over values of successor distr
            // TODO write combination code.
            for (int j = 0; j < atoms; j++) {
                sum_p.set(j, e.getValue().getValue(j) * successor_p[j]);
            }

        }

        // FIXME should be one set of probabilities for each unknown parameter value
        res.add(sum_p);
        return res;
    }

    // Shift distribution using discount and reward for a given state
    public ArrayList<Double> update_support(double gamma, double state_reward, int cur_state){

        ArrayList<Double> m = new ArrayList<> (atoms);
        for (int j =0; j<atoms; j++){
                // should be successor state not cur_state
                m.add(state_reward+gamma*distr[cur_state].getSupport(j));
        }
        
        return m;
    }

    // Shift distribution using discount and reward for a given state
    public ArrayList<ArrayList<Double>> update_support(double gamma, double state_reward, int cur_state,
                                                       boolean isTransCategorical){
        ArrayList<ArrayList<Double>> res = new ArrayList<>();
        ArrayList<Double> m = new ArrayList<> (atoms);

        for (int j =0; j<atoms; j++){
            m.add(state_reward+gamma*distr[cur_state].getSupport(j));
        }

        // FIXME: should be one set of supports for each unknown parameter value
        res.add(m);
        return res;
    }

    // update a specific state
    @Override
    public void update(DiscreteDistribution temp, int state){

        if(isCategorical){
            this.distr[state].update(temp.getValues());
        }
        else{
            this.distr[state].update(temp.getSupports());
        }

    }

    @Override
    public DiscreteDistribution getDist(int state) {
        return distr[state];
    }

    @Override
    public double getExpValue(int state){
        return distr[state].getExpValue();
    }


//    public double getValueCvar(int state, double lim) {
//        return distr[state].getCvarValue(lim);
//    }

    //@Override
    public double getValueCvar(int state, double lim, int idx_b){
        return distr[state].getCvarValue(lim, b[idx_b]);
    }

    @Override
    public double getVar(int state, double lim){
        return distr[state].getVar(lim);
    }

    // Get variance for a distribution <probs>
    @Override
    public double getVariance(int state) {
        return distr[state].getVariance();
    }

    // distributional distance l2 with p=2 between two distributions
    // categorical: compare probabilities
    // quantile: compare support
    public double getW(int state1, int state2)
    {   
        if (isCategorical)
        {
            return distr[state1].getW(distr[state2].getValues());
        }
        else {
            return distr[state1].getW(distr[state2].getSupports());
        }
        
    }

    // distributional distance l2 with p=2 between
    // a distribution <dist1> and the saved distribution for state <state>
    // categorical: compare probabilities
    // quantile: compare support
    public double getW(double [] dist1, int state)
    {
        return distr[state].getW(dist1);
    }

    @Override
    public double getW(DiscreteDistribution dist1, int state) {
        return distr[state].getW(dist1);
    }

    public double getInnerOpt(DiscreteDistribution dist, int idx_b){
        if(isCategorical)
        {
            return distr[0].getInnerOpt(dist.getValues(), b[idx_b]);
        } else{
            return distr[0].getInnerOpt(dist.getSupports(), b[idx_b]);
        }
    }

    public double getInnerOpt(int state, int idx_b){
        return distr[state].getInnerOpt(b[idx_b]);
    }

    public double getInnerOpt(double [] arr, int idx_b){
        return distr[0].getInnerOpt(arr, b[idx_b]);
    }

    public double getBVal(int idx_b){
        return b[idx_b];
    }

    public int getB_atoms(){return b.length;}

    // Interpolate to find the closest b index
    // choosing lower index -> intuition :"we have used less budget than we actually have"
    public int getClosestB(double temp_b){
        double new_b = max(b[0], min(temp_b,b[b.length-1])); double index = 0;
        if (delta_b > 0){
            index = (new_b- b[0])/delta_b;
        }
        int l= (int) floor(index); int u= (int) ceil(index);
        return l;
    }

    // Get full saved distributions for all states
    public DiscreteDistribution[] getP ()
    {
        return distr;
    }
    
    // get support for a specific state
    // FIXME: check if this needs to check which distr_type.
    public double [] getZ(int state)
    {
        return distr[state].getSupports();
    }

    // Log distribution for a state to a file <filename> as a csv with columns : 
    // Categorical : support index, probability, support value
    // Quantile : support value, probability, cumulative probability
    @Override
    public void writeToFile(int state, String filename){
        if (filename == null) {filename="distr_exp_"+distr_type.toLowerCase()+".csv";}
        try (PrintWriter pw = new PrintWriter("prism/"+filename)) {
            pw.println("r,p,z");
            pw.println(distr[state].toFile());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder temp = new StringBuilder();
        int index = 0;
        for (DiscreteDistribution distr_i: distr)
        {
            temp.append("State ").append(index).append(":");
            temp.append(distr_i.toString(df));
            temp.append("\n-------\n");
            index ++;

        }
        return temp.toString();
    }
    
//    public String toString()
//    {
//        StringBuilder temp = new StringBuilder();
//        int index = 0;
//        for (DiscreteDistribution distr_i: distr)
//        {
//            temp.append("State ").append(index).append(":");
//            temp.append(distr_i.toString(df));
//            temp.append("\n-------\n");
//            index ++;
//
//        }
//        return temp.toString();
//    }

    // convert a state to a string
    @Override
    public String toString(int state)
    {
        return distr[state].toString(df);
    }

    public String toString(int state, int idx_b)
    {
        return distr[state].toString(df, b[idx_b]);
    }

    @Override
    public void setFormat(DecimalFormat d_format)
    {
        this.df = d_format;
    }

    // Get parameters for this operator
    @Override
    public ArrayList<String> getParams()
    {
        ArrayList<String> res =new ArrayList<>(6);
        res.add(String.valueOf(v_min));
        res.add(String.valueOf(v_max));
        res.add(String.valueOf(atoms));
        res.add(String.valueOf(b.length));
        res.add(String.valueOf(isCategorical));
        res.add(String.valueOf(numStates));
        return res;
    }
    @Override
    public void clone(DistributionalBellman source) throws PrismException {
        ArrayList <String> param_source = source.getParams();
        ArrayList <String> param_dest = this.getParams();

        if (param_dest.equals(param_source)){
            this.clear();
            for (int ind = 0; ind < numStates; ind ++)
            {
                this.distr[ind].clone(source.getDist(ind));
            }
        }
        else {
            throw new PrismException("Trying to clone two different array operators (parameters don't match)\n"
                    + "source : "+ param_source + "destination : "+ param_dest );
        }
    }

}