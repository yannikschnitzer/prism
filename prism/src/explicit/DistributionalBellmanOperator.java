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
    boolean isCategorical = true;

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
                isCategorical = false;
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

    //TODO fix this for quantile
    // updates probabilities for one action
    public ArrayList<Double> update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it) {
        ArrayList<Double>  sum_p= new ArrayList<Double> (atoms);
        sum_p.addAll(Collections.nCopies(atoms, 0));

        while (trans_it.hasNext()) {

            Map.Entry<Integer, Double> e = trans_it.next();
            ArrayList<Double> successor_p = distr.get(e.getKey()).getValues();
            for (int j = 0; j < atoms; j++) {
                sum_p[j] += e.getValue() * successor_p.get(j);
            }

        }
        return sum_p;
    }

    // updates probabilities for one action
    // TODO fix this for updated iteration with probability transition distributions
    // public ArrayList<Double> update_probabilities(Iterator<Map.Entry<Integer, DiscreteDistribution>> trans_it) {
    //     ArrayList<Double>  sum_p= new ArrayList<Double> (atoms);
    //     sum_p.addAll(Collections.nCopies(atoms, 0));

    //     while (trans_it.hasNext()) {
    //         Map.Entry<Integer, DiscreteDistribution> e = trans_it.next();
    //         ArrayList<Double> successor_p = distr.get(e.getKey()).getValues();
    //         //number of atoms in transition prob
    //         if (e.getValue().size() > 1){

    //         }
    //         else {

    //         }
    //         for (int j = 0; j < atoms; j++) {
    //             sum_p[j] += e.getValue() * successor_p.get(j);
    //         }

    //     }
    //     return sum_p;
    // }

    // Shift distribution using discount and reward for a given state
    public ArrayList<Double> update_support(double gamma, double state_reward, int cur_state){

        ArrayList<Double> m = new ArrayList<Double> (atoms);
        double b = 0;
        for (int j =0; j<atoms; j++){
                m.add(state_reward+gamma*distr.get(cur_state).getSupport(i));
        }
        
        return m;
    }

    // update a specific state
    public void update(ArrayList<Double> temp, int state){
        this.distr.get(state).update(temp);
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
        if (isCategorical)
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
        if (isCategorical)
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