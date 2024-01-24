package explicit;

// TODO: this class is for when the transitions are uncertain (only the transitions are the source of distribution)
// This class does not do distribution over the returns, only over the values of the uncertain parameter
// the main difference is the step + updates, since it will generate a distribution

// Still saves a distribution for each state. the number of atoms 
// is based on the number of atoms in the parameter distribution.

import prism.PrismException;
import prism.PrismLog;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

class DistributionalBellmanOperatorProb extends DistributionalBellmanOperator {

    public DistributionalBellmanOperatorProb(int atoms, double vmin, double vmax, int numStates, String distr_type, prism.PrismLog log) {
        super(atoms, vmin, vmax, numStates, distr_type, log);

    }

    // this is the uncertain transition step
    // Distributional Bellman update using transitions probabilities, discount factor <gamma> and reward <state_reward>
    // This function also performs projection for the distributional operators.
    // This is for transitions as a distribution, but successors will be also be a distribution
    // FIXME: this is the old version
    public DiscreteDistribution step(Iterator<Map.Entry<Integer, DiscreteDistribution>> trans_it,
                                     double gamma, double state_reward,
                                     int cur_state, boolean isTransCategorical) {
        TreeMap<Double, Double> sum_p = new TreeMap<>();
        Double temp_supp, temp_value;
        int temp_atoms;
        DiscreteDistribution res;

         ArrayList<Double> probsyyy = new ArrayList<>();
         ArrayList<Double> suppsyyy = new ArrayList<>();

        if (isCategorical) {
            res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
        } else {
            res = new DistributionQuantile(atoms, mainLog);
        }

        // tree map construction
        while (trans_it.hasNext()) {
            // TODO : transition_vals *transition_p can be precomputed
            // TODO : think about matrix mult for update
            Map.Entry<Integer, DiscreteDistribution> e = trans_it.next();
            if (isCategorical) {
                double[] successor_p = distr[e.getKey()].getValues();
                double [] transition_vals = e.getValue().getSupports(); // possible values for the transition probability
                double [] transition_p = e.getValue().getValues(); // prob of each transition prob
                temp_atoms = distr[e.getKey()].getAtoms();
                for (int i=0; i< transition_vals.length; i++) {
                    for (int j = 0; j < temp_atoms; j++) {
                        // supp = cur state reward + discount * supp[next state] * possible transition probability[i]
                        temp_supp = state_reward + gamma * distr[e.getKey()].getSupport(j) * transition_vals[i];
                        // prob value = current Pr[supp] + Pr[next state | s, choice] * Pr[supp | next state]
                        temp_value = transition_vals[i] * transition_p[i] * successor_p[j];

                        // if it already exists, increase probability; else, create particle
                        if (temp_value > 0) {
                            if (sum_p.containsKey(temp_supp)) {
                                sum_p.put(temp_supp, sum_p.get(temp_supp) + temp_value);
                            } else {
                                sum_p.put(temp_supp, temp_value);
                            }
                        }
                    }
                }
            }
           else{
                // all particle probability values  for the same state are the same for quantile
                double successor_p = distr[e.getKey()].getValue(0);
                double [] transition_vals = e.getValue().getSupports(); // possible values for the transition probability
                // if quantile, all values are the same
                double transition_p = e.getValue().getValue(0); // prob of each transition prob

                for (int i=0; i<atoms; i++) {
                    for (int j = 0; j < atoms; j++) {
                        // supp = cur state reward + discount * supp[next state] * possible transition probability[i]
                        temp_supp = state_reward + gamma * distr[e.getKey()].getSupport(j) * transition_vals[i];
                        // prob value = current Pr[supp] + Pr[next state | s, choice] * Pr[supp | next state]
                        temp_value = transition_vals[i] * transition_p * successor_p;

                        // if it already exists, increase probability; else, create particle
                        if (temp_value > 0) {
                            if (sum_p.containsKey(temp_supp)) {
                                sum_p.put(temp_supp, sum_p.get(temp_supp) + temp_value);
                            } else {
                                sum_p.put(temp_supp, temp_value);
                            }
                        }
                    }
                }
            }
        }

        mainLog.println(sum_p.keySet());
        //mainLog.println(sum_p.values());

        // Perform projection on the intermediate target
        res.project(sum_p);
        return res;
    }

    public DiscreteDistribution step(ArrayList<Map.Entry<Integer, DiscreteDistribution>> transitions, int parameter_atoms,
                                     double gamma, double state_reward) {
        TreeMap<Double, Double> sum_p = new TreeMap<>();
        Double temp_supp, temp_value; Double exp_value;
        int temp_atoms;
        DiscreteDistribution res;
        Iterator<Map.Entry<Integer, DiscreteDistribution>> temp_it ;

        ArrayList<Double> probsyyy = new ArrayList<>();
        ArrayList<Double> suppsyyy = new ArrayList<>();

        if (isCategorical) {
            res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
        } else {
            res = new DistributionQuantile(atoms, mainLog);
        }

        // tree map construction

        if (isCategorical) {


            for (int i = 0; i < parameter_atoms; i++) {
                temp_supp = 0.0;
                temp_value = 0.0;
                exp_value = 0.0;
                temp_it = transitions.iterator();

                while (temp_it.hasNext()) {
                    // TODO : transition_vals *transition_p can be precomputed
                    // TODO : think about matrix mult for update
                    Map.Entry<Integer, DiscreteDistribution> e = temp_it.next();

                    double[] transition_vals = e.getValue().getSupports(); // possible values for the transition probability
                    double[] transition_p = e.getValue().getValues(); // prob of each transition prob

                    temp_atoms = distr[e.getKey()].getAtoms();

                    for (int j = 0; j < temp_atoms; j++) {
                        // supp = supp[next state] * possible transition probability[i]
                        temp_supp = distr[e.getKey()].getSupport(j) * transition_vals[i];
                        // prob value = current Pr[supp] * Pr[next state | s, choice]
                        temp_value = distr[e.getKey()].getValue(j) * transition_p[i];
                        exp_value += temp_value*temp_supp;
                    }

                    temp_value = transition_p[i];

                }
                exp_value = exp_value * gamma;
                exp_value += state_reward;

                // if it already exists, increase probability; else, create particle
                if (temp_value > 0) {
                    if (sum_p.containsKey(exp_value)) {
                        sum_p.put(exp_value, sum_p.get(exp_value) + temp_value);
                    } else {
                        sum_p.put(exp_value, temp_value);
                    }
                }
            }
        }
//        else{
//            while (trans_it.hasNext()) {
//                // TODO : transition_vals *transition_p can be precomputed
//                // TODO : think about matrix mult for update
//                Map.Entry<Integer, Double> e = trans_it.next();
//                // all particle probability values  for the same state are the same for quantile
//                double successor_p = distr[e.getKey()].getValue(0);
//                double[] transition_vals = e.getValue().getSupports(); // possible values for the transition probability
//                // if quantile, all values are the same
//                double transition_p = e.getValue().getValue(0); // prob of each transition prob
//
//                for (int i = 0; i < atoms; i++) {
//                    for (int j = 0; j < atoms; j++) {
//                        // supp = cur state reward + discount * supp[next state] * possible transition probability[i]
//                        temp_supp = state_reward + gamma * distr[e.getKey()].getSupport(j) * transition_vals[i];
//                        // prob value = current Pr[supp] + Pr[next state | s, choice] * Pr[supp | next state]
//                        temp_value = transition_vals[i] * transition_p * successor_p;
//
//                        // if it already exists, increase probability; else, create particle
//                        if (temp_value > 0) {
//                            if (sum_p.containsKey(temp_supp)) {
//                                sum_p.put(temp_supp, sum_p.get(temp_supp) + temp_value);
//                            } else {
//                                sum_p.put(temp_supp, temp_value);
//                            }
//                        }
//                    }
//                }
//            }
//        }


        mainLog.println(sum_p.keySet());
        //mainLog.println(sum_p.values());

        // Perform projection on the intermediate target
        res.project(sum_p);
        return res;
    }

    // Log distribution for a state to a file <filename> as a csv with columns : support index, probability, support value
    // Categorical : support index, probability, support value
    // Quantile : support value, probability, cumulative probability
    @Override
    public void writeToFile(int state, String filename){
        if (filename == null) {filename="distr_exp_"+distr_type.toLowerCase()+"_prob.csv";}
        try (PrintWriter pw = new PrintWriter("prism/"+filename)) {
            pw.println("r,p,z");
            pw.println(distr[state].toFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}