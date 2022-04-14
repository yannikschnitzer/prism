package explicit;

import common.IterableStateSet;
import explicit.rewards.MDPRewards;
import parser.State;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import prism.ModelType;
import prism.PrismException;
import prism.PrismLog;

import java.awt.*;
import java.util.*;
import java.util.List;

// TODO maybe rename this to Augmented MDP ?
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

    public CVaRProduct(CVaRProduct el){
        super(el.getProductModel(), el.getOriginalModel());
        memSize = el.memSize;
        invMap = el.invMap;

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


    /**
     //	 * Construct the product of a model for CVaR purposes using the operators' b array for augmented states.
     //	 * @param model The model (MDP)
     //  * @param mdpRewards the rewards structure for the MDP
     //	 * @param gamma discount factor
     //	 * @param statesOfInterest the set of states for which values should be calculated (null = all states)
     //	 * @return The product model
     //	 */
    public static  <M extends Model> CVaRProduct makeProduct(DistributionalBellmanAugmented operator, MDP model, MDPRewards mdpRewards, double gamma, BitSet statesOfInterest, PrismLog mainLog) throws PrismException
    {
        ModelType modelType = model.getModelType();
        int mdpNumStates = model.getNumStates();
        int prodNumStates;
        List<State> prodStatesList = null, bStatesList = null; List<Integer> initialStatesList = null;
        int s_1, s_2, q_1, q_2;
        int b_atoms = operator.getB_atoms();

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
            // Inform for bounds of idx_b
            Declaration decl = new Declaration(bVar, new DeclarationInt(Expression.Int(0), Expression.Int(b_atoms)));
            newVarList.addVar(0, decl, 1, model.getConstantValues());
        }

        // Create a (simple, mutable) MDP
        MDPSimple mdpProd = new MDPSimple();
        mdpProd.setVarList(newVarList); // MDP var list + new b variable

        // Encoding:
        // each state s' = <s, idx_b> = s * b_atoms + idx_b
        // s(s') = s' / b_atoms
        // b(s') = s' % b_atoms

        LinkedList<Point> queue = new LinkedList<Point>();
        int map[] = new int[prodNumStates];
        Arrays.fill(map, -1);

        if (model.getStatesList() != null) {
            prodStatesList = new ArrayList<State>();
            initialStatesList = new ArrayList<Integer>();
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

                mdpProd.addInitialState(mdpProd.getNumStates() -1);
                map[s_0 * b_atoms + i] = mdpProd.getNumStates() -1;
                if (prodStatesList != null) {
                    // Store state information for the product
                    State temp = new State(2);
                    temp.setValue(0, i);
                    temp.setValue(1, model.getStatesList().get(s_0));

                    prodStatesList.add(temp);
                    if (model.isInitialState(s_0))
                    {
                        initialStatesList.add(s_0 * b_atoms + i);
                    }
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
                    q_2 = operator.getClosestB((operator.getBVal(q_1)-reward)/gamma);

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
                            temp.setValue(0, q_2);
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

        // Update initialState with initial state from MDP x (all possible b)
        if(initialStatesList != null){
            mdpProd.clearInitialStates();
            for (int state :initialStatesList){
                mdpProd.addInitialState(state);
            }
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

