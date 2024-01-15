package explicit;
//import java.io.File;
import prism.PrismException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.text.DecimalFormat;

public class DistributionalBellmanOperator extends DistributionalBellman {
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

    public DistributionalBellmanOperator(int atoms, double vmin, double vmax, int numStates, String distr_type, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        this.mainLog = log;
        this.distr_type  = distr_type;
        this.isCategorical = (distr_type.equals("C51"));

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

    // get support for a specific state
    // FIXME: check if this needs to check which distr_type.
    public double [] getZ(int state)
    {
        return distr[state].getSupports();
    }

    //  Update supports and values then return projected result
    @Override
    public DiscreteDistribution step(Iterator<Map.Entry<Integer, Double>> trans_it, double gamma, double state_reward, int cur_state)
    {
        // treemap for the updated particles distribution with the support as key and probabilities
        // using a treemap means it will automaticatlly be sorted based on the value
        TreeMap<Double, Double>  sum_p= new TreeMap<>();
        Double temp_supp, temp_value; int temp_atoms;
        DiscreteDistribution res ;
        
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
            return this.optimized_step(trans_it, gamma, state_reward, res);
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

    public DiscreteDistribution optimized_step(Iterator<Map.Entry<Integer, Double>> trans_it, double gamma, double state_reward, DiscreteDistribution res){

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

    // Get cvar value for a state and an alpha limit
    public double getValueCvar(int state, double lim){
        return distr[state].getCvarValue(lim);
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

    // Get full saved distributions for all states
    public DiscreteDistribution[] getP ()
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

    // Convert a state to string
    @Override
    public String toString(int state)
    {
        return distr[state].toString(df);
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
                this.distr[ind].clone(source.getDist(ind));
            }
        }
        else {
            throw new PrismException("Trying to clone two different array operators (parameters don't match)\n"
                    + "source : "+ param_source + "destination : "+ param_dest );
        }
    }

}