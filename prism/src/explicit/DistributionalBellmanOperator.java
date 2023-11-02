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

public class DistributionalBellmanOperator extends DistributionalBellman {
    int atoms = 1;
    ArrayList<DiscreteDistribution> distr;
    int nactions = 4;
    double v_min ;
    double v_max ;
    double alpha=1;
    int numStates;
    String distr_type = "C51";
    prism.PrismLog mainLog;
    DecimalFormat df = new DecimalFormat("0.000");

    public DistributionalBellmanOperator(int atoms, double vmin, double vmax, int numStates, String distr_type, prism.PrismLog log){
        super();
        this.atoms = atoms;
        this.v_min = vmin;
        this.v_max = vmax;
        this.numStates = numStates;
        this.mainLog = log;
        this.distr_type = distr_type;

        distr = new ArrayList<DiscreteDistribution> (numStates);

        switch(distr_type)
        {
            case "C51":
                for (int i=0; i<numStates;i++){
                    distr.add(new DistributionCategorical(atoms, vmin, vmax, log));
                }
                break;
            
            case "QR":
                for (int i=0; i<numStates;i++){
                    distr.add(new DistributionQuantile(atoms, log));
                }
                break;
            
            default:
                this.distr_type = "C51";
                for (int i=0; i<numStates;i++){
                    distr.add(new DistributionCategorical(atoms, vmin, vmax, log));
                }
        }

    }

    // Clear a specific state
    public void clear(int state){
        distr.get(i).clear();
    }

    // Clear all distr
    public void clear(){
        distr.forEach( (distr_i) -> distr_i.clear() );
    }

    // get support for a specific state
    // TODO check if this needs to check which distr_type.
    public double [] getZ(int state)
    {
        return distr.get(state).getSupport();
    }

    //TODO
    @Override
    public DiscreteDistribution step(Iterator<Map.Entry<Integer, Double>> trans_it, int numTransitions, double gamma, double state_reward)
    {
        double [] res = update_probabilities(trans_it);
        res = update_support(gamma, state_reward, res);
        return res;
    }

    //TODO
    @Override
    public DiscreteDistribution step(Iterator<Map.Entry<Integer, DiscreteDistribution>> trans_it, int numTransitions, double gamma, double state_reward)
    {
        ArrayList<Double> probs = update_probabilities(trans_it);
        ArrayList<Double> supp = update_support(gamma, state_reward, res);
        DiscreteDistribution res;
        if (distr_type.equals("C51"))
        {
            res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
            res.project(probs, supp);
        }
        else {
            res = new DistributionQuantile(atoms, mainLog);
        }

        
        
        return res;
    }

    //TODO
    // updates probabilities for one action
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

    // updates probabilities for one action
    // TODO fix this for updated iteration with probability transition distributions
    public double[] update_probabilities(Iterator<Map.Entry<Integer, DiscreteDistribution>> trans_it) {
        double [] sum_p= new double[atoms];
        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();
            for (int j = 0; j < atoms; j++) {
                sum_p[j] += e.getValue() * p[e.getKey()][j];
            }

        }
        return sum_p;
    }

    //TODO
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

    // TODO
    public void update(ArrayList<Double> temp, int state){
        p[state] = Arrays.copyOf(temp, temp.length);
    }

    @Override
    public ArrayList<DiscreteDistribution> getDist(int state) {
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
        if (distr_type.equals("C51"))
        {
            return distr.get(state1).getW(distr.get(state2.getValues()));
        }
        else {
            return distr.get(state1).getW(distr.get(state2.getSupport()));
        }
        
    }

    // distributional distance l2 with p=2 between
    // a distribution <dist1> and the saved distribution for state <state>
    // categorical: compare probabilities
    // quantile: compare support
    public double getW(ArrayList<Double> dist1, int state)
    {
        if (distr_type.equals("C51"))
        {
            return distr.get(state1).getW(dist1);
        }
        else {
            return distr.get(state1).getW(dist1);
        }   
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
        try (PrintWriter pw = new PrintWriter(new File("prism/"+filename))) {
            pw.println("r,p,z");
            pw.println(distr.get(i).toString())
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        String temp = "";
        distr.forEach( (distr_i) -> temp += distr_i.toString(df) + "\n-------\n");
        return temp;
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

}