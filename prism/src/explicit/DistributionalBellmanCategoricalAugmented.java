package explicit;


import common.IterableStateSet;
import explicit.rewards.MDPRewards;
import explicit.rewards.StateRewardsArray;
import parser.State;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import prism.ModelType;
import prism.Pair;
import prism.PrismException;
import strat.MDStrategy;
import strat.MDStrategyArray;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

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
        df = el.df;
        alpha = el.alpha;
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


    public double [] step(Iterator<Map.Entry<Integer, Double>> trans_it, double cur_b, int [][] choices, int numTransitions, double gamma, double state_reward)
    {
        double temp_b = (cur_b-state_reward)/gamma;
        int idx_b = getClosestB(temp_b);

        double [] res = update_probabilities(trans_it, idx_b, choices);
        res = update_support(gamma, state_reward, res);
        return res;
    }

    // updates probabilities for 1 action
    public double[] update_probabilities(Iterator<Map.Entry<Integer, Double>> trans_it, int idx_b, int [][] choices) {
        double [] sum_p= new double[atoms];
        int action = 0;

        while (trans_it.hasNext()) {
            Map.Entry<Integer, Double> e = trans_it.next();
            for (int j = 0; j < atoms; j++) {
                // FIXME here action should be the action at the next state.
                //  -> but which idx_b - it shouldn't be the same as for current state since it is next state.
                //  -> but this might be ok because this is adjusted b
                action  = choices[e.getKey()][idx_b];
                sum_p[j] += e.getValue() * p[e.getKey()][idx_b][action][j];
            }
        }
        return sum_p;
    }

    public double [] update_support(double gamma, double state_reward, double []sum_p){

        double [] m = new double [atoms];
        // INFO do I need to use transition probability -> prob not since R(s,a) and not R(s,a,s')

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

        //  right now I'm choosing a slightly more conservative approach by
        // choosing lower index -> intuition :"we have used less budget than we actually have"
        // opposite of chap 7 -> they take floor since they are doing max and we are doing min -> cost approach
        return u;
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

    public double [] computeStartingB(int startState, double alpha, int [][] choices){
        double [] res = new double [2]; // contains the min index + min cvar.
        double cvar = 0;
        res [1] = Float.POSITIVE_INFINITY;
        double expected_cost =0;

        for(int idx_b=0; idx_b < b_atoms; idx_b++){
            expected_cost = 0;
            for ( int i =0; i < atoms; i++){
                double j = p[startState][idx_b][choices[startState][idx_b]][i];
                if (j >0){
                    expected_cost += j * max(0, z[i] - b[idx_b]);
                }
            }
            cvar = b[idx_b] + 1/(1-alpha) * expected_cost;
            if (cvar < res[1]){
                res[0] = idx_b;
                res[1] = cvar;
            }
        }
        return res;
    }

    public int [] getStrategy(int start, MDPRewards mdpRewards, StateRewardsArray rewardsArray, int [][] choices, double alpha, double gamma)
    {
        int [] res = new int [numStates];


        double [] cvar_info = computeStartingB(start, alpha, choices);

        int idx_b = (int) cvar_info[0];
        double r = 0;
        mainLog.println("b :"+b[idx_b] + " cvar = " + cvar_info[1]);


        for (int i = 0; i < numStates; i++) {
            res[i] = choices[i][idx_b];
            // Compute reward
            r = mdpRewards.getStateReward(i) ;
            r += mdpRewards.getTransitionReward(i, choices[i][idx_b]);

            rewardsArray.setStateReward(i, r);

            // update b
            idx_b = getClosestB((b[idx_b] - r) /gamma);
            mainLog.println ("policy: "+res[i]+" - rew:"+r+" - new b :"+b[idx_b]);
        }

        return res;
    }

    public class CVaRProduct extends Product<MDP>
    {
        private int memSize;
        private int invMap[];

        public CVaRProduct(MDP productModel, MDP originalModel, int memSize, int[] invMap)
        {
            super(productModel, originalModel);
            this.memSize = memSize;
            this.invMap = invMap;
        }

        @Override
        public int getModelState(int productState)
        {
            return invMap[productState] / memSize;
        }

        // This returns b
        @Override
        public int getAutomatonState(int productState)
        {
            return invMap[productState] % memSize;
        }
    }


    /**
     //	 * Construct the product of a model for CVaR purposes using the operators' b array for augmented states.
     //	 * @param model The model (MDP)
     //  * @param mdpRewards the rewards structure for the MDP
     //	 * @param gamma discount factor
     //	 * @param statesOfInterest the set of states for which values should be calculated (null = all states)
     //	 * @return The product model
     //	 */
    public <M extends Model> CVaRProduct makeProduct(MDP model, MDPRewards mdpRewards, double gamma, BitSet statesOfInterest) throws PrismException
    {
        ModelType modelType = model.getModelType();
        int mdpNumStates = model.getNumStates();
        int prodNumStates;
        List<State> prodStatesList = null, bStatesList = null;
        int s_1, s_2, q_1, q_2;

        // TODO should I make a new MDP rewards object for the new MDP?

        try {
            prodNumStates = Math.multiplyExact(mdpNumStates, b_atoms);
        } catch (ArithmeticException e) {
            throw new PrismException("Size of product state space of model and automaton is too large for explicit engine");
        }

        VarList newVarList = null;

        if (model.getVarList() != null) {
            VarList varList = model.getVarList();
            // Create a (new, unique) name for the variable that will represent the value for b
            String bVar = "b";
            while (varList.getIndex(bVar) != -1) {
                bVar = "_" + bVar;
            }

            newVarList = (VarList) varList.clone();
            // NB: if DA only has one state, we add an extra dummy state
            //TODO: b bounds don't have to be ints.
            Declaration decl = new Declaration(bVar, new DeclarationDouble(Expression.Int(b[0]), Expression.Int(b[b_atoms-1])));
            newVarList.addVar(0, decl, 1, model.getConstantValues()); // FIXME ??
        }

        // Create a (simple, mutable) MDP
        MDPSimple mdpProd = new MDPSimple();
        mdpProd.setVarList(newVarList); // MDP var list + new b variable

        // Encoding:
        // FIXME
        // each state s' = <s, idx_b> = s * b_atoms + idx_b ??
        // s(s') = s' / b_atoms
        // b(s') = s' % b_atoms

        LinkedList<Point> queue = new LinkedList<Point>();
        int map[] = new int[prodNumStates];
        Arrays.fill(map, -1);

        if (model.getStatesList() != null) {
            prodStatesList = new ArrayList<State>();
            bStatesList = new ArrayList<State>(b_atoms);
            for (int i = 0; i < b_atoms; i++) {
                bStatesList.add(new State(1).setValue(0, i));
            }
        }

        // We need results for all states of the original model in statesOfInterest
        // We thus explore states of the product starting from these states.
        // These are designated as initial states of the product model
        // (a) to ensure reachability is done for these states; and
        // (b) to later identify the corresponding product state for the original states
        //     of interest
        for (int s_0 : new IterableStateSet(statesOfInterest, model.getNumStates())) {

            // All b values are possible initial states
            for(int i =0; i<b_atoms; i++)
            {
                // Add (initial) state to product
                queue.add(new Point(s_0, i));
                mdpProd.addState();
                // FIXME : double check this
                mdpProd.addInitialState(mdpProd.getNumStates() - i -1);
                map[s_0 * b_atoms + i] = mdpProd.getNumStates() - i -1;
                if (prodStatesList != null) {
                    // Store state information for the product
                    State temp = new State(2);
                    temp.setValue(0, b[i]);
                    temp.setValue(1, model.getStatesList().get(s_0));

                    prodStatesList.add(temp);
                }
            }

        }

        // Product states
        BitSet visited = new BitSet(prodNumStates);
        while (!queue.isEmpty()) {
            Point p = queue.pop();
            s_1 = p.x;
            q_1 = p.y;
            visited.set(s_1 * b_atoms + q_1);

            // Go through transitions from state s_1 in original model
            int numChoices = (model instanceof NondetModel) ? ((NondetModel) model).getNumChoices(s_1) : 1;

            for (int j = 0; j < numChoices; j++) {
                Iterator<Map.Entry<Integer, Double>> iter;
                iter = ((MDP) model).getTransitionsIterator(s_1, j);
                Distribution prodDistr = null;
                if (modelType.nondeterministic()) {
                    prodDistr = new Distribution();
                }
                while (iter.hasNext()) {
                    Map.Entry<Integer, Double> e = iter.next();
                    s_2 = e.getKey();
                    double prob = e.getValue();
                    double reward = mdpRewards.getStateReward(s_1) ;
                    reward += mdpRewards.getTransitionReward(s_1, j);

                    // Find corresponding successor in b
                    q_2 = getClosestB((b[q_1]-reward)/gamma);

                    if (q_2 < 0) {
                        throw new PrismException("Cannot find closest b  (b = " + q_1 + ")");
                    }
                    // Add state/transition to model
                    if (!visited.get(s_2 * b_atoms + q_2) && map[s_2 * b_atoms + q_2] == -1) {
                        queue.add(new Point(s_2, q_2));
                        mdpProd.addState();
                        map[s_2 * b_atoms + q_2] = mdpProd.getNumStates() - 1;
                        if (prodStatesList != null) {
                            State temp = new State(2);
                            temp.setValue(0, b[q_2]);
                            temp.setValue(1, model.getStatesList().get(s_2));
                            // Store state information for the product
                            prodStatesList.add(temp);
                        }
                    }
                    prodDistr.set(map[s_2 * b_atoms + q_2], prob);
                }
                mdpProd.addActionLabelledChoice(map[s_1 * b_atoms + q_1], prodDistr, ((MDP) model).getAction(s_1, j));
            }
        }

        // Build a mapping from state indices to states (s,q), encoded as (s * daSize + q)
        int[] invMap = new int[mdpProd.getNumStates()];
        for (int i = 0; i < map.length; i++) {
            if (map[i] != -1) {
                invMap[map[i]] = i;
            }
        }

        mdpProd.findDeadlocks(false);

        if (prodStatesList != null) {
            mdpProd.setStatesList(prodStatesList);
        }

        CVaRProduct product = new CVaRProduct(mdpProd, model, b_atoms, invMap);

        // lift the labels
        for (String label : model.getLabels()) {
            BitSet liftedLabel = product.liftFromModel(model.getLabelStates(label));
            mdpProd.addLabel(label, liftedLabel);
        }

        return product;

    }
}