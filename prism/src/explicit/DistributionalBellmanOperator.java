package explicit;


//import java.io.File;
import prism.PrismException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.text.DecimalFormat;

public class DistributionalBellmanOperator extends DistributionalBellman {
    int atoms; // FIXME this should be atoms per state or get it from the distribution
    ArrayList<DiscreteDistribution> distr;
    double v_min ;
    double v_max ;
    int numStates;
    String distr_type;
    prism.PrismLog mainLog;
    DecimalFormat df = new DecimalFormat("0.000");
    boolean isCategorical ;

    boolean isAdaptive = false;

    public DistributionalBellmanOperator(int atoms, double vmin, double vmax, int numStates, String distr_type, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        this.mainLog = log;
        this.distr_type  = distr_type;
        this.isCategorical = (distr_type.equals("C51"));

        distr = new ArrayList<> (numStates);

        switch(distr_type)
        {
            case "C51":
                for (int i=0; i<numStates;i++){
                    if(isAdaptive){ //Adaptive
                        distr.add(new DistributionCategorical(atoms, 5, log));
                    } else { //normal
                        distr.add(new DistributionCategorical(atoms, vmin, vmax, log));
                    }

                }
                break;
            
            case "QR":
                isCategorical = false;
                for (int i=0; i<numStates;i++){
                    distr.add(new DistributionQuantile(atoms, log));
                }
                break;
            
            default:
                distr_type = "C51";
                for (int i=0; i<numStates;i++){
                    distr.add(new DistributionCategorical(atoms, vmin, vmax, log));
                }
        }

    }

    // Clear a specific state
    public void clear(int state){
        distr.get(state).clear();
    }

    // Clear all distr
    @Override
    public void clear(){
        distr.forEach(DiscreteDistribution::clear);
    }

    // Empty all distr
    @Override
    public void emptyAll(){
        distr.forEach(DiscreteDistribution::empty);
    }

    // get support for a specific state
    // FIXME: check if this needs to check which distr_type.
    public ArrayList<Double> getZ(int state)
    {
        return distr.get(state).getSupports();
    }

    // TODO: convert to treemap
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

        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();
            
            if (isCategorical){
                ArrayList<Double> successor_p = distr.get(e.getKey()).getValues();
                temp_atoms = distr.get(e.getKey()).getAtoms();
                for (int j = 0; j < temp_atoms; j++) {
                    // new support value = cur state reward + discount * next state atom reward
                    temp_supp = state_reward+gamma*distr.get(e.getKey()).getSupport(j);
                    // new probability value = current probability of particle + transition probability * success probability of particle
                    temp_value = e.getValue() * successor_p.get(j);
                    
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
                Double successor_p = distr.get(e.getKey()).getValue(0);
                for (int j = 0; j < atoms; j++) {
                    // new support value = cur state reward + discount * next state atom reward
                    temp_supp = state_reward+gamma*distr.get(e.getKey()).getSupport(j);
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

        res.project(sum_p);

        return res;
    }

    // Old version for reference
    // FIXME: remove this
    public DiscreteDistribution step_old(Iterator<Map.Entry<Integer, Double>> trans_it,
                                     double gamma, double state_reward, int cur_state)
    {
        ArrayList<Double> probs = update_probabilities(trans_it);
        ArrayList<Double> supp = update_support(gamma, state_reward, cur_state);
        DiscreteDistribution res;
        if (isCategorical)
        {
            res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
        }
        else {
            res = new DistributionQuantile(atoms, mainLog);
        }

        res.project(probs, supp);

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

    // TODO: fix this for quantile
    // updates probabilities for one action
    public ArrayList<Double> update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it) {
        ArrayList<Double>  sum_p= new ArrayList<> (atoms);
        sum_p.addAll(Collections.nCopies(atoms, 0.0));

        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();
            if (isCategorical)
            {
                ArrayList<Double> successor_p = distr.get(e.getKey()).getValues();
                for (int j = 0; j < atoms; j++) {
                    sum_p.set(j, sum_p.get(j) + e.getValue() * successor_p.get(j));
                }
            } else{
                // for quantile all the values in succesor_p are the same
                double successor_p = distr.get(e.getKey()).getValue(0);

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
            ArrayList<Double> successor_p = distr.get(e.getKey()).getValues();
            // FIXME There should be another loop over values of successor distr
            // TODO write combination code.
            for (int j = 0; j < atoms; j++) {
                sum_p.set(j, e.getValue().getValue(j) * successor_p.get(j));
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
            // TODO: should be successor state not cur_state
                m.add(state_reward+gamma*distr.get(cur_state).getSupport(j));
        }
        
        return m;
    }

    // Shift distribution using discount and reward for a given state
    public ArrayList<ArrayList<Double>> update_support(double gamma, double state_reward, int cur_state,
                                                       boolean isTransCategorical){
        ArrayList<ArrayList<Double>> res = new ArrayList<>();
        ArrayList<Double> m = new ArrayList<> (atoms);

        for (int j =0; j<atoms; j++){
            m.add(state_reward+gamma*distr.get(cur_state).getSupport(j));
        }

        // FIXME: should be one set of supports for each unknown parameter value
        res.add(m);
        return res;
    }

    // update a specific state
    @Override
    public void update(DiscreteDistribution temp, int state){

        if(isCategorical){
            this.distr.get(state).update(temp.getValues());
        }
        else{
            this.distr.get(state).update(temp.getSupports());
        }

    }

    @Override
    public DiscreteDistribution getDist(int state) {
        return distr.get(state);
    }

    @Override
    public double getExpValue(int state){
        return distr.get(state).getExpValue();
    }

    @Override
    public double getValueCvar(int state, double lim){
        return distr.get(state).getCvarValue(lim);
    }

    @Override
    public double getVar(int state, double lim){
        return distr.get(state).getVar(lim);
    }

    // Get variance for a distribution <probs>
    @Override
    public double getVariance(int state) {
        return distr.get(state).getVariance();
    }

    // distributional distance l2 with p=2 between two distributions
    // categorical: compare probabilities
    // quantile: compare support
    public double getW(int state1, int state2)
    {   
        if (isCategorical)
        {
            return distr.get(state1).getW(distr.get(state2).getValues());
        }
        else {
            return distr.get(state1).getW(distr.get(state2).getSupports());
        }
        
    }

    // distributional distance l2 with p=2 between
    // a distribution <dist1> and the saved distribution for state <state>
    // categorical: compare probabilities
    // quantile: compare support
    public double getW(ArrayList<Double> dist1, int state)
    {
        return distr.get(state).getW(dist1);
    }

    @Override
    public double getW(DiscreteDistribution dist1, int state) {
        return distr.get(state).getW(dist1);
    }

    // Get full saved distributions for all states
    public ArrayList<DiscreteDistribution> getP ()
    {
        return distr;
    }

    // Log distribution for a state to a file <filename> as a csv with columns : 
    // Categorical : support index, probability, support value
    // Quantile : support value, probability, cumulative probability
    @Override
    public void writeToFile(int state, String filename){
        if (filename == null) {filename="distr_exp_"+distr_type.toLowerCase()+".csv";}
        try (PrintWriter pw = new PrintWriter("prism/"+filename)) {
            pw.println("r,p,z");
            pw.println(distr.get(state).toString());
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

    @Override
    public String toString(int state)
    {
        return distr.get(state).toString(df);
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
        ArrayList<String> res =new ArrayList<>(5);
        res.add(String.valueOf(v_min));
        res.add(String.valueOf(v_max));
        res.add(String.valueOf(atoms));
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
                this.distr.get(ind).clone(source.getDist(ind));
            }
        }
        else {
            throw new PrismException("Trying to clone two different array operators (parameters don't match)\n"
                    + "source : "+ param_source + "destination : "+ param_dest );
        }

    }

}