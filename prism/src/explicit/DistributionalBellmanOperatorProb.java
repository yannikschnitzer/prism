package explicit;

// TODO: this class is for when the transitions are uncertain (only the transitions are the source of distribution)
// This class does not do distribution over the returns, only over the values of the uncertain parameter
// the main difference is the step + updates, since it will generate a distribution

// Still saves a distribution for each state. the number of atoms 
// is based on the number of atoms in the parameter distribution.

import param.BigRational;
import param.Function;
import param.Point;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

 class DistributionalBellmanOperatorProb extends DistributionalBellmanOperator {

   public DistributionalBellmanOperatorProb(int atoms, double vmin, double vmax, int numStates, String distr_type, prism.PrismLog log) {
        super(atoms, vmin, vmax, numStates, distr_type, log);

    }

    // Step for when the support represents the possible expected values and the transition is uncertain
    // Assumption : only one uncertain parameter is associated with a state-action pair.
/*       public DiscreteDistribution step(MDP<Function> mdp, double param, double param_val, Map<Integer, Point> jointMap, int s, int choice, double gamma, double state_reward) {
        TreeMap<Double, Double> sum_p = new TreeMap<>();
        Iterator<Map.Entry<Integer,Double>> iter;
        double exp_value;
        int temp_atoms;
        DiscreteDistribution res;

        if (isCategorical) {
            res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
        } else {
            res = new DistributionQuantile(atoms, mainLog);
        }
        // tree map construction
        //System.out.println(param.getValue(0));
        if (isCategorical) {
            //System.out.println("Hi 3 from 1");

            // Iterate over each possible transition probability group
            // For ex. (p, 1-p), (1-p, p/2, p/2), etc.
            //for (int i = 0; i < param.getAtoms(); i++) {

                exp_value = 0.0;

                //if(param.getValue(i)>0) {
                    // instantiate the transitions for a possible parameter
                    //int finalI = i;
                    //double p_value = param.getSupport(finalI);
                    //System.out.println(p_value);
                    //System.out.println(p_value);
                    //System.out.println("Starting state: " + s + " - uncertainty: " + p_value);
                    // For multiple parameters, use the jointmap to get the support, and the joint distr for values
                    if (!jointMap.isEmpty()){
                        iter = mdp.getTransitionsMappedIterator(s, choice,
                                p -> p.evaluate(jointMap.get(0)).doubleValue());
                    } else {
                        iter = mdp.getTransitionsMappedIterator(s, choice,
                                p -> p.evaluate(toBigRationalPoint(param)).doubleValue());
                    }

                    // Iterate over possible transitions and successors
                    while (iter.hasNext()) {
                        Map.Entry<Integer, Double> e = iter.next();
                        //System.out.println(e.getKey());
                        double transition_val = e.getValue(); // possible values for the transition probability
                        //System.out.println(transition_val);
                        
                        temp_atoms = distr[e.getKey()].getAtoms();
                        //System.out.println(distr[e.getKey()].getValue(0));
                        //System.out.println("   Next State: " + e.getKey() + ", uncertainty: " + param.getSupport(i));
                        for (int j = 0; j < temp_atoms; j++) {
                            // exp += nextstate_val[j] * nextstate_supp[j] * Pr[nextstate | s,a][i]
                            //System.out.println("get_support: "+distr[e.getKey()].getValue(j));
                            exp_value += (distr[e.getKey()].getValue(j) * distr[e.getKey()].getSupport(j) * transition_val);
                            //System.out.println(exp_value);
                            //double temp = distr[e.getKey()].getValue(j) * distr[e.getKey()].getSupport(j) * transition_val;
                            // if (temp > 0) {
                            //     mainLog.print("i:" + i + " - val: " + distr[e.getKey()].getValue(j) + " - supp: " + distr[e.getKey()].getSupport(j)
                            //             + " - parameter val: " + transition_val + "- exp: " + exp_value + "\n");
                            // }
                        }
                    }
                    //System.out.println("--------");

                    //System.out.println("During State: " + s + ", uncertainty: " + param.getSupport((i)));

                    // mainLog.println("------------------------------------------");
                    exp_value = exp_value * gamma; // discount information from successor states
                    exp_value += state_reward; // add reward for current state
                    //System.out.println("final: " + exp_value);
                    // if it already exists, increase probability; else, create particle
                    //System.out.println("Jeez: " + sum_p.get(exp_value));
                    if (sum_p.containsKey(exp_value)) {
                        sum_p.put(exp_value, sum_p.get(exp_value) + param_val);
                    } else {
                        sum_p.put(exp_value,  param_val);
                    }

                //}
            //}
            //System.out.println(sum_p);
        }
        /* else{
            // since its quantile, probability for each parameter value is the same and strictly positive
            //System.out.println("Hi 3 from 2");
            double temp_value = param.getValue(0); // probability of each parameter atom
            double temp_p= 0.0;
            // INFO: quantile representation for
            for (int i = 0; i < param.getAtoms(); i++) {
                exp_value = 0.0;
                int finalI = i;
                //System.out.println("Realization: " + i + " - Support: " + param.getSupport(finalI) + ", Value: " + param.getValue(finalI));
                // For multiple parameters, use the jointmap to get the support, and the joint distr for values
                if (!jointMap.isEmpty()){
                    iter = mdp.getTransitionsMappedIterator(s, choice,
                            p -> p.evaluate(jointMap.get(finalI)).doubleValue());
                } else {
                    iter = mdp.getTransitionsMappedIterator(s, choice,
                            p -> p.evaluate(toBigRationalPoint(param.getSupport(finalI))).doubleValue());
                }

                // Iterate over possible transitiopns and successors
                while (iter.hasNext()) {
                    Map.Entry<Integer, Double> e = iter.next();

                    double transition_val = e.getValue(); // possible values for the transition probability
                    //System.out.println("State: " + s + ", Next State: " + e.getKey() + ", p-value: " + transition_val);
                    temp_atoms = distr[e.getKey()].getAtoms();
                    temp_p = distr[e.getKey()].getValue(0); // all the same
                    for (int j = 0; j < temp_atoms; j++) {
                        // exp += nextstate_val[j] * nextstate_supp[j] * Pr[nextstate | s,a][i]
                        exp_value += (temp_p * distr[e.getKey()].getSupport(j) * transition_val);
                    }
                }
                exp_value = exp_value * gamma; // discount information from successor states
                exp_value += state_reward; // add reward for current state
                // if it already exists, increase probability; else, create particle
                if (sum_p.containsKey(exp_value)) {
                    sum_p.put(exp_value, sum_p.get(exp_value) + temp_value);
                } else {
                    sum_p.put(exp_value, temp_value);
                }

            } 
        }


        //mainLog.println(sum_p.keySet());
        //mainLog.println(sum_p.values());

        // Perform projection on the intermediate target
        res.project(sum_p);
        //System.out.println(res);
        return res;
    } */
          public DiscreteDistribution step(MDP<Function> mdp, DiscreteDistribution param, Map<Integer, Point> jointMap, int s, int choice, double gamma, double state_reward) {
        TreeMap<Double, Double> sum_p = new TreeMap<>();
        Iterator<Map.Entry<Integer,Double>> iter;
        double exp_value;
        int temp_atoms;
        DiscreteDistribution res;

        if (isCategorical) {
            res = new DistributionCategorical(atoms, v_min, v_max, mainLog);
        } else {
            res = new DistributionQuantile(atoms, mainLog);
        }
        // tree map construction
        //System.out.println(param.getValue(0));
        if (isCategorical) {
            //System.out.println("Hi 3 from 1");

            // Iterate over each possible transition probability group
            // For ex. (p, 1-p), (1-p, p/2, p/2), etc.
            for (int i = 0; i < param.getAtoms(); i++) {

                exp_value = 0.0;

                if(param.getValue(i)>0) {
                    // instantiate the transitions for a possible parameter
                    int finalI = i;
                    double p_value = param.getSupport(finalI);
                    //System.out.println(p_value);
                    //System.out.println(p_value);
                    //System.out.println("Starting state: " + s + " - uncertainty: " + p_value);
                    // For multiple parameters, use the jointmap to get the support, and the joint distr for values
                    if (!jointMap.isEmpty()){
                        iter = mdp.getTransitionsMappedIterator(s, choice,
                                p -> p.evaluate(jointMap.get(finalI)).doubleValue());
                    } else {
                        iter = mdp.getTransitionsMappedIterator(s, choice,
                                p -> p.evaluate(toBigRationalPoint(param.getSupport(finalI))).doubleValue());
                    }

                    // Iterate over possible transitions and successors
                    while (iter.hasNext()) {
                        Map.Entry<Integer, Double> e = iter.next();
                        //System.out.println(e.getKey());
                        double transition_val = e.getValue(); // possible values for the transition probability
                        //System.out.println(transition_val);
                        
                        temp_atoms = distr[e.getKey()].getAtoms();
                        //System.out.println(distr[e.getKey()].getValue(0));
                        //System.out.println("   Next State: " + e.getKey() + ", uncertainty: " + param.getSupport(i));
                        for (int j = 0; j < temp_atoms; j++) {
                            // exp += nextstate_val[j] * nextstate_supp[j] * Pr[nextstate | s,a][i]
                            //System.out.println("get_support: "+distr[e.getKey()].getValue(j));
                            exp_value += (distr[e.getKey()].getValue(j) * distr[e.getKey()].getSupport(j) * transition_val);
                            //System.out.println(exp_value);
                            //double temp = distr[e.getKey()].getValue(j) * distr[e.getKey()].getSupport(j) * transition_val;
                            // if (temp > 0) {
                            //     mainLog.print("i:" + i + " - val: " + distr[e.getKey()].getValue(j) + " - supp: " + distr[e.getKey()].getSupport(j)
                            //             + " - parameter val: " + transition_val + "- exp: " + exp_value + "\n");
                            // }
                        }
                    }
                    //System.out.println("--------");

                    //System.out.println("During State: " + s + ", uncertainty: " + param.getSupport((i)));

                    // mainLog.println("------------------------------------------");
                    exp_value = exp_value * gamma; // discount information from successor states
                    exp_value += state_reward; // add reward for current state
                    //System.out.println("final: " + exp_value);
                    // if it already exists, increase probability; else, create particle
                    System.out.println("Jeez: " + sum_p.get(exp_value));
                    if (sum_p.containsKey(exp_value)) {
                        sum_p.put(exp_value, sum_p.get(exp_value) + param.getValue(i));
                    } else {
                        sum_p.put(exp_value,  param.getValue(i));
                    }

                }
            }
            //System.out.println(sum_p);
        }
        else{
            // since its quantile, probability for each parameter value is the same and strictly positive
            //System.out.println("Hi 3 from 2");
            double temp_value = param.getValue(0); // probability of each parameter atom
            double temp_p= 0.0;
            // INFO: quantile representation for
            for (int i = 0; i < param.getAtoms(); i++) {
                exp_value = 0.0;
                int finalI = i;
                //System.out.println("Realization: " + i + " - Support: " + param.getSupport(finalI) + ", Value: " + param.getValue(finalI));
                // For multiple parameters, use the jointmap to get the support, and the joint distr for values
                if (!jointMap.isEmpty()){
                    iter = mdp.getTransitionsMappedIterator(s, choice,
                            p -> p.evaluate(jointMap.get(finalI)).doubleValue());
                } else {
                    iter = mdp.getTransitionsMappedIterator(s, choice,
                            p -> p.evaluate(toBigRationalPoint(param.getSupport(finalI))).doubleValue());
                }

                // Iterate over possible transitiopns and successors
                while (iter.hasNext()) {
                    Map.Entry<Integer, Double> e = iter.next();

                    double transition_val = e.getValue(); // possible values for the transition probability
                    //System.out.println("State: " + s + ", Next State: " + e.getKey() + ", p-value: " + transition_val);
                    temp_atoms = distr[e.getKey()].getAtoms();
                    temp_p = distr[e.getKey()].getValue(0); // all the same
                    for (int j = 0; j < temp_atoms; j++) {
                        // exp += nextstate_val[j] * nextstate_supp[j] * Pr[nextstate | s,a][i]
                        exp_value += (temp_p * distr[e.getKey()].getSupport(j) * transition_val);
                    }
                }
                exp_value = exp_value * gamma; // discount information from successor states
                exp_value += state_reward; // add reward for current state
                // if it already exists, increase probability; else, create particle
                if (sum_p.containsKey(exp_value)) {
                    sum_p.put(exp_value, sum_p.get(exp_value) + temp_value);
                } else {
                    sum_p.put(exp_value, temp_value);
                }

            }
        }


        //mainLog.println(sum_p.keySet());
        //mainLog.println(sum_p.values());

        // Perform projection on the intermediate target
        res.project(sum_p);
        System.out.println(res);
        return res;
    }
   
   

    // Log distribution for a state to a file <filename> as a csv with columns : support index, probability, support value
    // Categorical : support index, probability, support value
    // Quantile : support value, probability, cumulative probability
    @Override
    public void writeToFile(int state, String filename){
        if (filename == null) {filename="distr_exp_prob_"+distr_type.toLowerCase()+".csv";}
        try (PrintWriter pw = new PrintWriter("prism/"+filename)) {
            pw.println("r,p,z");
            pw.println(distr[state].toFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Point toBigRationalPoint(Double input)
    {
        return new Point(new BigRational[]{new BigRational(input)});
    }

    public static Point toBigRationalPoint(Double [] input)
    {
        BigRational [] temp = new BigRational[input.length];
        for (int i=0; i<input.length; i++){
            temp[i] = new BigRational(input[i]);
        }
        return new Point(temp);
    }

}