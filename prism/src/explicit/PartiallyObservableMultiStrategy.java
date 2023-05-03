package explicit;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.PrimitiveIterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import acceptance.AcceptanceReach;
import automata.DA;
import java.util.Iterator;
import cern.colt.Arrays;
import cern.jet.stat.quantile.EquiDepthHistogram;
import common.StopWatch;
import edu.jas.arith.BigDecimal;
import explicit.graphviz.Decoration;
import explicit.graphviz.Decorator;
import explicit.rewards.MDPRewards;
import explicit.rewards.Rewards;
import explicit.rewards.StateRewardsSimple;
import explicit.rewards.WeightedSumMDPRewards;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationIntUnbounded;
import parser.ast.Expression;
import prism.Accuracy;
import prism.AccuracyFactory;
import prism.Pair;
import prism.Prism;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismNotSupportedException;
import prism.PrismUtils;
import prism.PrismSettings;
import prism.Point;
import java.awt.desktop.SystemSleepEvent;
import java.io.*;
import java.math.BigInteger;
import java.io.BufferedReader;
//import solver.BeliefPoint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

 class State{
	 private int originalState;
	 private int preObservation;
	 private int preActionIndex;
	 private int observation;
	 State(int s)
	 {
		 this.originalState = s;
	 }
	 State(int s, int preObservation, int actionIndex){
		 this.originalState = s;
		 this.preObservation = preObservation;
		 this.preActionIndex = actionIndex;
		 this.observation = -1;
	 }
	 public int getOriginalState() {
		 return originalState;
	 }
	 public int getObservation() {
		 return observation;
	 }
	 public void setObservation(int o) {
		 this.observation = o;
	 }
	 public int getPreobservation() {
		 return preObservation;
	 }
	 public int getPreActionIndex() {
		 return preActionIndex;
	 }
	 public String toString() {
		 return String.format("[%d,%d,%d]", originalState, preObservation, preActionIndex);
	 }
	 @Override
	 public int hashCode() {
		 int result = 7;
		 result = 31 * result + originalState;
		 result = 31 * result + preObservation;
		 result = 31 * result + preActionIndex;
		 return result;
	 }
	 @Override
	 public boolean equals(Object other) {
		 if (other == this) {
			 return true;
		 }
		 if (!(other instanceof State)) {
			 return false;
		 }
		 State s = (State) other;
//		 System.out.println("===");
//		 System.out.println((this.originalState == s.getOriginalState()) );
//
//		 System.out.println((this.observation == s.getObservation()));
//
//		 System.out.println((this.preActionIndex == s.getPreActionIndex()));
//
//		 System.out.println((this.preObservation == s.getPreobservation()));
		 return ((this.originalState == s.getOriginalState())  && 
				 (this.preActionIndex == s.getPreActionIndex()) && (this.preObservation == s.getPreobservation()));
	 }
 }
 class PreObs{
	 private int obs;
	 private int actionIndex;
	 PreObs(int obs, int actionIndex){
		 this.obs = obs;
		 this.actionIndex = actionIndex;
	 }
	 public int getObservation() {
		 return obs;
	 }
	 public int getActionIndex() {
		 return actionIndex;
	 }
	 public String toString() {
		 return String.format("[obs=%d,action=%d]", obs, actionIndex);
	 }
	 @Override
	 public int hashCode() {
		 int result = 7;
		 result = 31 * result + obs;
		 result = 31 * result + actionIndex;
		 return result;
	 }
	 @Override
	 public boolean equals(Object other) {
		 if (other == this) {
			 return true;
		 }
		 if (!(other instanceof PreObs)) {
			 return false;
		 }
		 PreObs s = (PreObs) other;
		 return ((this.obs == s.getObservation())  && (this.actionIndex == s.getActionIndex())) ;
	 }
 }
 class Transition{
	 private State s;
	 private int actionIndex;
	 private State nxt;
	 Transition(State s, int actionIndex, State nxt){
		 this.s = s;
		 this.actionIndex = actionIndex;
		 this.nxt = nxt;
	 }
	 public String toString() {
		 return String.format("[%s,%d,%s]", s.toString(), actionIndex, nxt.toString());
	 }
	 public State getState() {
		 return s;
	 }
	 public State getNxtState() {
		 return nxt;
	 }
	 public int getActionIndex() {
		 return actionIndex;
	 }
	 @Override
	 public int hashCode() {
		 int result = 7;
		 result = 31 * result + s.hashCode();
		 result = 31 * result + actionIndex;
		 result = 31 * result + nxt.hashCode();
		 return result;
	 }
	 @Override
	 public boolean equals(Object other) {
		 if (other == this) {
			 return true;
		 }
		 if (!(other instanceof Transition)) {
			 return false;
		 }
		 Transition t = (Transition) other;
//		 System.out.println("DDDDDDDDDEEEEEEEEEE" + s + " " + t);
//		 System.out.println("tt"+ s.equals(t.getState()) + (this.s == t.getState()));
//		 System.out.println(nxt.equals(t.getNxtState()));
//		 System.out.println((this.actionIndex == t.getActionIndex()));
//		 
//		 System.out.println((this.s == t.getState()));
//		 System.out.println((this.nxt == t.getNxtState()));
//		 System.out.println((this.actionIndex == t.getActionIndex()));
		 return (s.equals(t.getState()) && nxt.equals(t.getNxtState()) && 
				 (this.actionIndex == t.getActionIndex()) );
	 }
 }
 
public class PartiallyObservableMultiStrategy {
	private POMDP pomdp;
	private ArrayList<Object> allActions; 
	private Map <Object, Integer> actionToIndex;
	private HashMap<Integer, Double> rewardFunction;
	private BitSet target;
	private boolean min;
	private BitSet statesOfInterest;
	private int gridSize;
	private int nObservations;
	private int nStates;
	private int nActions;
	
	private HashSet<State> states;
	private HashMap<State, Integer> state_to_observation;
	private HashMap<Transition, Double> transition_probability;
//	private ArrayList<HashMap<State, Double>> transition_probability;
	private MDPRewards mdpRewards;
	private List<MDPRewards> mdpRewardsList;
	private List<MinMax> minMaxList;
	private double lowerBounds[];
	private double upperBounds[];
	private HashMap<State, HashSet<PreObs>> state_to_preobservations;
	private HashMap<Integer, ArrayList<State>> observation_to_states;
	private int observationIndex;
	private HashSet<State> splitGroup;
	private int nObj;
	private HashMap<State, HashSet<State>> statePredecessor;
	private HashMap<Integer, HashSet<PreObs>> observationToPreobservation;
	private HashSet<Integer> endStatesOriginal;
	private HashSet<State> endStates;
	
	public PartiallyObservableMultiStrategy(POMDP pomdp, List<MDPRewards> mdpRewardsList, BitSet target,
											List<MinMax> minMaxList, BitSet statesOfInterest, HashSet<Integer> endStatesOriginal)
	{
		this.pomdp = pomdp;
		this.mdpRewardsList = mdpRewardsList;
		this.target = target;
		this.minMaxList = minMaxList;
		this.statesOfInterest = statesOfInterest;
		this.endStatesOriginal = endStatesOriginal;
		initializeTransition();
		
		nObj = mdpRewardsList.size();
		lowerBounds = new double[nObj];
		upperBounds = new double[nObj];
	}
	public PartiallyObservableMultiStrategy(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest) 
	{
		this.pomdp = pomdp;
		this.mdpRewards = mdpRewards;
		this.statesOfInterest = statesOfInterest;
		this.target = target;
		this.min = min;
//		this.endStates = endStates;
		initializeTransition();
	}
	public void initializeTransition() {
		this.states =  new HashSet<State> ();
		this.transition_probability = new HashMap<Transition, Double>();
		this.observation_to_states = new HashMap<Integer, ArrayList<State>> ();
		for (int s = 0; s < pomdp.getNumStates(); s++) {
			State st = new State(s,-1,-1);
			int obs = pomdp.getObservation(s);
			st.setObservation(obs);
//			if (endStatesOriginal.contains(s)) {
//				endStates
//			}
			states.add(st);
		}
		observationIndex = pomdp.getNumObservations();
		
		allActions = new ArrayList<Object> ();
		for (int s = 0; s < pomdp.getNumStates(); s++) {
			List <Object> availableActionsForState = pomdp.getAvailableActions(s);
			for (Object a: availableActionsForState) {
				if (!allActions.contains(a) & a!= null) {
					allActions.add(a);
				}
			}
		}
		nActions = allActions.size();
		actionToIndex = new HashMap<Object, Integer> ();
		for (int a = 0; a < nActions; a ++) {
			actionToIndex.put(allActions.get(a), a);
		}
		
		for (int s = 0; s < pomdp.getNumStates(); s++) {
			for (int a = 0; a < nActions; a++) {
				Object action = allActions.get(a);
				if (!pomdp.getAvailableActions(s).contains(action)) {
					continue;
				}
				int choice = pomdp.getChoiceByAction(s, action);
				int observation = pomdp.getObservation(s);
//				double reward = (mdpRewards.getTransitionReward(s, choice) + mdpRewards.getStateReward(s) );
				Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(s,choice);
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> trans = iter.next();
					int next_state = trans.getKey();
					double transition_prob = trans.getValue();
					State cur = new State(s,-1,-1);
					cur.setObservation(pomdp.getObservation(s));
					State nxt = new State(next_state, -1, -1);
					nxt.setObservation(pomdp.getObservation(next_state));
					Transition t = new Transition(cur, a, nxt);
					transition_probability.put(t, transition_prob);
				}
			}
		}
	}
	public void getPredecessor() {
		statePredecessor = new HashMap<State, HashSet<State>> ();
		observationToPreobservation = new HashMap<Integer, HashSet<PreObs>> ();
		
		for(Transition t: transition_probability.keySet()) {
			double prob = transition_probability.get(t);
			if (prob == 0) {
				continue;
			}
			State pre = t.getState();
			int preobservation = pre.getObservation();
			int actionIndex = t.getActionIndex();
			State nxt = t.getNxtState();
			int nxtObservation = nxt.getObservation();
			if (!statePredecessor.containsKey(nxt)) {
				statePredecessor.put(nxt, new HashSet<State> ());
			}
			statePredecessor.get(nxt).add(pre);
			
			if (!observationToPreobservation.containsKey(nxtObservation)){
				observationToPreobservation.put(nxtObservation, new HashSet<PreObs> ());
			}
			observationToPreobservation.get(nxtObservation).add(new PreObs(preobservation, actionIndex));
		}
		System.out.println("S");
	}
	public void resetPreobservation(){
		state_to_preobservations = new HashMap<State, HashSet<PreObs>>(); //TODO do we need to reset everytime
		observation_to_states = new HashMap<Integer, ArrayList<State>>(); // TODO
		for (State s : states) {
			int obs = s.getObservation();
			if (observation_to_states.get(obs) == null) {
				observation_to_states.put(obs, new ArrayList<State> ());
			} 
			observation_to_states.get(obs).add(s);
			for (int a = 0; a < nActions; a++) {
				for (State nxt: states) {
					Double prob = transition_probability.get(new Transition(s,a, nxt));
					if (prob != null && prob > 0) {
						if (state_to_preobservations.get(nxt)  == null) {
							state_to_preobservations.put(nxt, new HashSet<PreObs>());
						}
						state_to_preobservations.get(nxt).add(new PreObs(obs, a));
					}
				}
			}
		}
	}
	
	public void connect(PreObs A, PreObs B, HashMap<PreObs, PreObs> record) {
		if (!record.containsKey(A)) {
			record.put(A, A);
		}
		if (!record.containsKey(B)) {
			record.put(B, B);
		}
		PreObs rootA = getParent(A, record);
		PreObs rootB = getParent(B, record);
		if (!rootA.equals(rootB)) {
			record.put(rootB, rootA);
		}
	}
	public PreObs getParent(PreObs node, HashMap<PreObs, PreObs> record) {
		if (!record.containsKey(node)) {
			record.put(node, node);
		}
		PreObs root = node;
		if (root.getObservation()==1 && root.getActionIndex()==3) {
			System.out.println(root.toString() + "D");
		}
		while (!(record.get(root).equals(root))){
			root = record.get(root);
		}
		while (!(record.get(node).equals(root))) {
			PreObs tp = record.get(node);
			record.put(node, root);
			node = tp;
		}
		return root;
	}
	
	public void splitObservations() {
		resetPreobservation();
		HashSet<Integer> obs = new HashSet<Integer> ();
		for (State s: states) {
			obs.add(s.getObservation());
		}
		nObservations = obs.size();
//		nStates = states.size();
		for (int z: obs) {
			splitObservation(z);
		}
	}
	public void splitObservation(int z) 
	{
//		HashMap<State, HashSet<PreObs>>  pred =  resetPreobservation(); // TODO
//		HashMap<Integer, ArrayList<Integer>> observation_to_states = new HashMap<Integer, ArrayList<Integer>> ();
		ArrayList<State> states_to_be_spilt = observation_to_states.get(z);
		if (states_to_be_spilt == null) {
			return;
		}
		HashMap<PreObs, PreObs> record_preobs = new HashMap<PreObs, PreObs> ();
		
		System.out.println("Before Observation splitting" + z);
		for (State s: states) {
			System.out.println(s.toString() + " " + s.getObservation());
		}
		for (State s : states_to_be_spilt) {
			if (!state_to_preobservations.containsKey(s)) {
				continue;
			}
			ArrayList<PreObs> pre = new ArrayList<> (state_to_preobservations.get(s));
			if (pre== null || pre.size() == 0) {
				System.out.println(s + "EEEEE");
			}
			PreObs represent = pre.get(0);			
			for (int i = 1; i < pre.size(); i++) {
				connect(represent, pre.get(i), record_preobs);
			}
//			PreObs parent = getParent(represent, record_preobs);
//			if (!preobs_to_states.containsKey(parent)) {
//				preobs_to_states.put(parent, new HashSet<State>());
//			}
//			preobs_to_states.get(parent).add(s);
		}
		HashMap<PreObs, HashSet<State>> preobs_to_states = new HashMap<PreObs, HashSet<State>> ();
		for (State s: states_to_be_spilt) {
			if (!state_to_preobservations.containsKey(s)) {
				continue;
			}
			ArrayList<PreObs> pre = new ArrayList<> (state_to_preobservations.get(s));
			if (pre== null || pre.size() == 0) {
				System.out.println(s + "EEEEE");
			}
			PreObs represent = pre.get(0);	
			PreObs parent = getParent(represent, record_preobs);
			if (!preobs_to_states.containsKey(parent)) {
				preobs_to_states.put(parent, new HashSet<State>());
			}
			preobs_to_states.get(parent).add(s);
		}

		HashSet<PreObs> spilt = new HashSet<PreObs> ();
		for (PreObs p : preobs_to_states.keySet()) {
			if (p == getParent(p, record_preobs)) {
				spilt.add(p);
			}
		}
		
		if (spilt.size() <= 1) {
			return;
		}

		for (PreObs p : spilt) {
			observationIndex++;
			for (State s : preobs_to_states.get(p)) {
				s.setObservation(observationIndex);
			}
		}
		System.out.println("After Observation splitting" + z);
		for (State s: states) {
			System.out.println(s.toString() + " " + s.getObservation());
		}
		
	}
	public void spiltStates(HashSet<State> splitGroup) {
		if (splitGroup==null) {
			splitGroup = states;
		}
		 //Assume a state won't be split more than once.
		
//		HashSet<State> newStates = new HashSet<State> ();
//		for (State s: spiltGroup) {
//			if (state_to_preobservations.get(s).size()<=1) {
//				newStates.add(s);
//				continue;
//			}
//			
//			for (PreObs p: state_to_preobservations.get(s)) {
//				State newState = new State(s.getOriginalState(), p.getObservation(), p.getActionIndex());
//				newStates.add(newState);
//			}
//		}
		
		for (State s : splitGroup) {
//			if (state_to_preobservations.get(s).size()<=1) {
//				continue;
//			}
			for (Transition t : transition_probability.keySet()) {
				double prob = transition_probability.get(t);
//				if (prob > 0)
//					System.out.println("b?" + t + " " + prob);
			}
			
			HashSet<State> newStates_A = new HashSet<State>();
			HashSet<State> newStates_B = new HashSet<State>();
			HashSet<State> newStates = new HashSet<State>();
			for (State newState: states) {
				if (!newState.equals(s)) {
					newStates_A.add(newState);
					newStates.add(newState);
				}
			}
			if (!state_to_preobservations.containsKey(s)) {
				continue;
			}
			HashSet<PreObs> pres = state_to_preobservations.get(s);
			if (pres == null) {
				continue;
			}
			for (PreObs p: pres) {
				State newState = new State(s.getOriginalState(), p.getObservation(), p.getActionIndex());
				newState.setObservation(pomdp.getObservation(newState.getOriginalState()));
				newStates_B.add(newState);
				newStates.add(newState);
			}
			HashMap<Transition, Double> new_transition_probability = new HashMap<Transition, Double> ();
			for (int a = 0; a < nActions; a++) {
//				double prob = transition_probability.get(new Transition(s, a, s));
				for (State t: newStates) {
					for (State tPrime: newStates) {
						if (a ==3 && t.getOriginalState()==4 && tPrime.getOriginalState()==4  && tPrime.getPreActionIndex()==3) {
							System.out.println("EE");
						}
						double prob = 0.0;
						Transition tran = null;
						if (newStates_A.contains(t) && newStates_A.contains(tPrime)) {
							tran = new Transition(t,a,tPrime);
							if (transition_probability.containsKey(tran)) {
								prob = transition_probability.get(tran);
							}
							// what if it is null ?
						} else if((t.getOriginalState() == s.getOriginalState())&&
								(tPrime.getOriginalState() == s.getOriginalState())&&
//								(t.getPreobservation() == tPrime.getPreobservation())&&
								(tPrime.getPreActionIndex() == a)
								) {
							System.out.println(t.toString() + "-> " + s.toString() + " " + tPrime.toString());
							tran = new Transition(s,a,s);
							if (transition_probability.containsKey(tran)) {
								prob = transition_probability.get(tran);
							}
						} else if ((newStates_A.contains(t))&&
								(tPrime.getOriginalState() == s.getOriginalState())&&
								(tPrime.getPreobservation() ==  t.getObservation()) &&
								(tPrime.getPreActionIndex() == a)
								) {
							tran = new Transition(t,a,s);
							if (transition_probability.containsKey(tran)) {
								prob = transition_probability.get(tran);
							}
						} else if ( (t.getOriginalState() == s.getOriginalState()) &&
								(newStates_A.contains(tPrime))
								){
							tran = new Transition(s,a,tPrime);
							if (transition_probability.containsKey(tran)) {
								prob = transition_probability.get(tran);
							}
						} else {
							prob = 0.0;
						}
						new_transition_probability.put(new Transition(t, a, tPrime), prob);
					}
				}
			}
			states = newStates;
			transition_probability = new_transition_probability;
		}
	
	}
	public void displayTransition() {
		for (Transition t : transition_probability.keySet()) {
			double prob = transition_probability.get(t);
			if (prob > 0)
				System.out.println("?" + t + " " + prob);
		}
	}
	public void displayStates() {
		for (State s: states) {
			System.out.println("state = " + s + " observation = " + s.getObservation());
		}
		for (int i = 0; i < nActions; i++) {
			System.out.println(i+ " action " + allActions.get(i));
		}
	}
//	public double compueMILP_back() {
//		try {
//			GRBEnv env = new GRBEnv("gurobi.log");
//			env.set(GRB.IntParam.OutputFlag, 0);
//			GRBModel model = new GRBModel(env);
//			env.start();
//			
//			// Set up MILP variables (real)
//			// add binary variable for strategy over observation and action
//			GRBVar var_strategy_observation[][] = new GRBVar[nObservations][nActions];
//			for (int s = 0; s < nStates; s ++) {
//				int obs = pomdp.getObservation(s);
//				List <Object> availableActionsForState = pomdp.getAvailableActions(s);
//				for (Object action : availableActionsForState) {
//					int action_index = action_to_index.get(action);
//					if (var_strategy_observation[obs][action_index] == null) {
//						var_strategy_observation[obs][action_index] = model.addVar(0, 1, 0, GRB.BINARY, String.format("strategy_observation%d_action%s", obs, action));
//					}
//				}
//			} 
//	
//			// add continuous variables for values
//			GRBVar var_value[] = new GRBVar[nStates];
//			for (int s = 0; s < nStates; s++) {
//				if (var_value[s] == null) {
//					var_value[s] = model.addVar(-max_value, max_value, 0, GRB.CONTINUOUS, String.format("value_state_%d", s));
//				}
//			}
//	
//			// set objectives
//			GRBLinExpr expr = new GRBLinExpr();
//			expr.addTerm(1.0, var_value[0]);
//			model.setObjective(expr, GRB.MAXIMIZE);
//			
//			int constarint_count = 0;
//			// Add constraints: each observation take one action
//			for (int obs = 0; obs < nObservations; obs++) {
//				expr = new GRBLinExpr();
//				for (int a = 0; a < nActions; a++) {
//					if (var_strategy_observation[obs][a] != null) {
//						expr.addTerm(1, var_strategy_observation[obs][a]);
//					}
//				}
//				model.addConstr(expr, GRB.EQUAL, 1, String.format("c%d", constarint_count++ ));
//			}
//			
//			for (int s = 0; s < nStates; s++) {
//				for (int a = 0; a < nActions; a++) {
//					Object action = allActions.get(a);
//					if (!pomdp.getAvailableActions(s).contains(action)) {
//						continue;
//					}
//					expr = new GRBLinExpr();
//					int choice = pomdp.getChoiceByAction(s, action);
//					int observation = pomdp.getObservation(s);
//					double reward = (mdpRewards.getTransitionReward(s, choice) + mdpRewards.getStateReward(s) );
//					expr.addTerm(1, var_value[s]);
//					expr.addTerm(max_value, var_strategy_observation[observation][a]);
//					Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(s,choice);
//					while (iter.hasNext()) {
//						Map.Entry<Integer, Double> trans = iter.next();
//						int next_state = trans.getKey();
//						double transition_prob = trans.getValue();
//						expr.addTerm(-1 * discount * transition_prob, var_value[next_state]);
//					}
//					model.addConstr(expr, GRB.LESS_EQUAL, max_value - reward, String.format("c%d", constarint_count++ ));
//				}
//			}
//			model.write("aaa_gurobi.lp");
//			model.optimize();
//			
//			System.out.println("aaa Obj: " + model.get(GRB.DoubleAttr.ObjVal));
//			model.write("aaa_gurobi.sol");
//			
//	      // Dispose of model and environment
//	      model.dispose();
//	      env.dispose();
//		} catch (GRBException e) {
//			throw new PrismException("Error solving LP: " +e.getMessage());
//		}
//	
//	}
	public double computeMILP(int batch) throws PrismException
	{
		try {
			double max_value = 1000;
			double discount = 0.95;
			GRBEnv env = new GRBEnv("gurobi.log");
			env.set(GRB.IntParam.OutputFlag, 0);
			GRBModel model = new GRBModel(env);
			env.start();
			
			// Set up MILP variables (real)
			// add binary variable for strategy over observation and action
			
//			GRBVar var_strategy_observation[][] = new GRBVar[nObservations][nActions];
			ArrayList<HashMap<Integer, GRBVar>> var_strategy_observation =  new ArrayList<HashMap<Integer, GRBVar>>();
			for (int a = 0; a < nActions; a++) {
				var_strategy_observation.add(new HashMap<Integer, GRBVar>());
			}
			for (State s : states) {
				int obs = s.getObservation();
				System.out.println("s"+s + "obs" +obs);
				List <Object> availableActionsForState = pomdp.getAvailableActions(s.getOriginalState());
				
				for (Object action: availableActionsForState) {
					System.out.println(action);
					int actionIndex = actionToIndex.get(action);
					if (var_strategy_observation.get(actionIndex).get(obs)==null) {
						var_strategy_observation.get(actionIndex).put(obs, model.addVar(0, 1, 0, GRB.BINARY, String.format("strategy_observation%d_action%s", obs, action)));	
					}
				}
			}
			 
			// add continuous variables for values
			HashMap<State, GRBVar> var_value = new HashMap<State, GRBVar> ();
			for (State s: states) {
				if (var_value.get(s) == null) {
					var_value.put(s, model.addVar(-max_value, max_value, 0, GRB.CONTINUOUS, String.format("value_state_%s", s.toString())));
				}
			}
//			System.out.println("SDDDdddddddddddddd");
//			for (State s: var_value.keySet()) {
////				System.out.println(s);
////				System.out.println(var_value.get(s));
////				System.out.println(var_value.get(new State(0,-1,-1)));
//				if (s.getObservation()==0) {
//					// set objectives
//					GRBLinExpr expr = new GRBLinExpr();
//					expr.addTerm(1.0, var_value.get(s));
//					model.setObjective(expr, GRB.MAXIMIZE);
////					break;
//				}
//				
//			}
//			System.out.println("SDDDdddddddddddddd");

			// set objectives
			GRBLinExpr expr = new GRBLinExpr();
			expr.addTerm(1.0, var_value.get(new State(0,-1,-1)));
			model.setObjective(expr, GRB.MAXIMIZE);
			
			int constarint_count = 0;
			// Add constraints: each observation take one action
			HashSet<Integer> record = new HashSet<Integer> ();
			for (State s: states) {
				int obs = s.getObservation();
				if (record.contains(obs)) {
					continue;
				}
				expr = new GRBLinExpr();
				for (int a = 0; a < nActions; a++) {
					if (var_strategy_observation.get(a).get(obs) != null) {
						expr.addTerm(1, var_strategy_observation.get(a).get(obs));
					}
				}
				model.addConstr(expr, GRB.EQUAL, 1, String.format("c%d", constarint_count++ ));
				record.add(obs);
			}
			
			for (State s: states) {
				for (int a = 0; a < nActions; a++) {
					Object action = allActions.get(a);
					if (!pomdp.getAvailableActions(s.getOriginalState()).contains(action)) {
						continue;
					}
					expr = new GRBLinExpr();
					int observation = s.getObservation();
					//TODO can be transformed to a reward function
					int choice = pomdp.getChoiceByAction(s.getOriginalState(), action);
					double reward = (mdpRewards.getTransitionReward(s.getOriginalState(), choice) + mdpRewards.getStateReward(s.getOriginalState()) );
					if (min) {
						reward *= -1;
					}
					expr.addTerm(1, var_value.get(s));
					expr.addTerm(max_value, var_strategy_observation.get(a).get(observation));
					
					for (State sPrime: states) {
						Transition t = new Transition(s, a, sPrime);
						if (!transition_probability.containsKey(t)) {
							continue;
						}
						double prob = transition_probability.get(new Transition(s, a, sPrime));
						if (prob <= 0) {
							continue;
						} 
						expr.addTerm(-1 * discount * prob, var_value.get(sPrime));
					}
					model.addConstr(expr, GRB.LESS_EQUAL, max_value + reward, String.format("c%d", constarint_count++ ));
				}
			}

			model.write(String.format("split_gurobi_%d.lp", batch));
			model.optimize();
			double val = model.get(GRB.DoubleAttr.ObjVal);
			System.out.println("Strength Obj: " + val);
			model.write(String.format("split_gurobi_%d.sol", batch));
			
			
			splitGroup = new HashSet<State> ();
			for (State s: states) {
				int obs = s.getObservation();
				int policy_selection = -1;
				for (int a = 0; a < nActions; a++) {
					GRBVar var = var_strategy_observation.get(a).get(obs);
					if (var == null) {
						continue;
					}
					double xValue = var.get(GRB.DoubleAttr.X);
					if (xValue == 1) {
						policy_selection = a;
						break;
					}
				}
				
				int ideal_selection = -1;
				double best = 0;
				for (int a = 0; a < nActions; a++) {
					double value = 0;
					for (State sPrime: states) {
						Transition t = new Transition(s, a, sPrime);
						if (transition_probability.get(t) != null) {
							value += transition_probability.get(t) * var_value.get(sPrime).get(GRB.DoubleAttr.X);
						}
					}
					if ((value > best) || ((value==best) && a == policy_selection)) {
						best = value;
						ideal_selection = a;
					}
				}
				resetPreobservation();
				if (policy_selection!=ideal_selection) {
					if (state_to_preobservations.get(s) != null && state_to_preobservations.get(s).size() > 1) {
						System.out.println("states to be spilt"  + s);
						splitGroup.add(s);	
					}
				}
			}
			
	      // Dispose of model and environment
	      model.dispose();
	      env.dispose();
	      return val;
		} catch (GRBException e) {
			throw new PrismException("Error solving LP: " +e.getMessage());
		}
	}
	public void heuristic() throws PrismException{
		int count = 0;
		double oldResult = Double.NEGATIVE_INFINITY;
		splitObservations();
		for(State s: states) {
			System.out.println(s.toString() + " " + s.getObservation());
		}
		System.out.println("starting here");
		double newResult = computeMILP(count++);
		for (int a = 0; a < nActions; a++) {
			System.out.println(""+a+" "+allActions.get(a));
		}
		while(newResult > oldResult) {
			oldResult = newResult;
//			computeSpilitGroup(oldResult);
//			spiltStates(splitGroup);
			spiltStates(states);
			splitObservations();
			newResult = computeMILP(count++);
			for (Transition t: transition_probability.keySet()) {
				double prob = transition_probability.get(t);
//				if (prob>0)
//					System.out.println("EE"+ t.toString() + prob + " " + t.getState().getObservation() + " " + t.getNxtState().getObservation());
			}
		}
	}
	
	public double computeMultiStrategyMILP(int batch) throws PrismException
	{
//		setObjectiveBounds();
		try {
			double max_value = 1000;
			double discount = 0.95;
			GRBEnv env = new GRBEnv("gurobi.log");
			env.set(GRB.IntParam.OutputFlag, 0);
			GRBModel model = new GRBModel(env);
			env.start();
			State sInit = new State(0,-1,-1);
			// Set up MILP variables (real)
			// add binary variable for strategy over observation and action
			
//			GRBVar var_strategy_observation[][] = new GRBVar[nObservations][nActions];
			ArrayList<HashMap<Integer, GRBVar>> var_strategy_observation =  new ArrayList<HashMap<Integer, GRBVar>>();
			for (int a = 0; a < nActions; a++) {
				var_strategy_observation.add(new HashMap<Integer, GRBVar>());
			}
			for (State s : states) {
				int obs = s.getObservation();
				System.out.println("s"+s + "obs" +obs);
				List <Object> availableActionsForState = pomdp.getAvailableActions(s.getOriginalState());
				for (Object action: availableActionsForState) {
//					System.out.println(action);
					int actionIndex = actionToIndex.get(action);
					if (var_strategy_observation.get(actionIndex).get(obs)==null) {
						var_strategy_observation.get(actionIndex).put(obs, model.addVar(0, 1, 0, GRB.BINARY, String.format("strategy_observation%d_action%s", obs, action)));	
					}
				}
			}
			 
			// add continuous variables for values
			ArrayList<HashMap<State, GRBVar>> var_value_lo = new ArrayList<HashMap<State, GRBVar>>();
			ArrayList<HashMap<State, GRBVar>> var_value_up = new ArrayList<HashMap<State, GRBVar>>();

			for (int i = 0; i < nObj; i++) {
				var_value_lo.add(new HashMap<State, GRBVar>());
				var_value_up.add(new HashMap<State, GRBVar>());
				for (State s: states) {
					//TODO ? set value of target state as zero? 
					//BitSet maxRew0 = rewTot0(mdp, mdpRewardsList.get(i), false); // Find states where max expected total reward is 0
					if (var_value_lo.get(i).get(s) == null) {
						if (endStatesOriginal.contains(s.getOriginalState())) {
							var_value_lo.get(i).put(s, model.addVar(0, 0, 0, GRB.CONTINUOUS, String.format("value_state_lo_%s_%d", s.toString(), i)));	
						} else {
							var_value_lo.get(i).put(s, model.addVar(0, max_value, 0, GRB.CONTINUOUS, String.format("value_state_lo_%s_%d", s.toString(), i)));
						}
					}
					
					if (var_value_up.get(i).get(s) == null) {
						if (endStatesOriginal.contains(s.getOriginalState())) {
							var_value_up.get(i).put(s, model.addVar(0, 0, 0, GRB.CONTINUOUS, String.format("value_state_up_%s_%d", s.toString(), i)));
						} else {
							var_value_up.get(i).put(s, model.addVar(0, max_value, 0, GRB.CONTINUOUS, String.format("value_state_up_%s_%d", s.toString(), i)));
						}
					}
				}
			}
			
//			for (State s: var_value.keySet()) {
////				System.out.println(s);
////				System.out.println(var_value.get(s));
////				System.out.println(var_value.get(new State(0,-1,-1)));
//				if (s.getObservation()==0) {
//					// set objectives
//					GRBLinExpr expr = new GRBLinExpr();
//					expr.addTerm(1.0, var_value.get(s));
//					model.setObjective(expr, GRB.MAXIMIZE);
////					break;
//				}
//			}
//			System.out.println("SDDDdddddddddddddd");

			// set objectives
			GRBLinExpr expr = new GRBLinExpr();

			for (int i = 0; i < nObj; i++) {
				expr.addTerm(-1.0, var_value_lo.get(i).get(new State(0,-1,-1)));
				expr.addTerm(1.0, var_value_up.get(i).get(new State(0,-1,-1)));
			}
			
			HashSet<Integer> record_observation = new HashSet<Integer> ();
			for (State s: states) {
				int obs = s.getObservation();
				if (record_observation.contains(obs)) {
					continue;
				}
				List<Object> actions = pomdp.getAvailableActions(s.getOriginalState());
				for (Object action: actions) {
					int actionIndex = actionToIndex.get(action);
					expr.addTerm(-max_value, var_strategy_observation.get(actionIndex).get(obs));
					expr.addConstant(max_value);
				}
				record_observation.add(obs);
			}
			model.setObjective(expr, GRB.MINIMIZE);
			
			
			int constarint_count = 0;
//			stateToPredecessor
			getPredecessor();
			
			
			// constraints for incoming transitions
			HashSet<Integer> record = new HashSet<Integer> ();
			GRBLinExpr expr1 = null;
			GRBLinExpr expr2 = null;
			
			for (State s: states) {
				int obs = s.getObservation();
				if (record.contains(obs)) {
					continue;
				}
				record.add(obs);
				expr1 = new GRBLinExpr();
				expr2 = new GRBLinExpr();
				for (Object action: pomdp.getAvailableActions(s.getOriginalState())) {
					int actionIndex = actionToIndex.get(action);
					expr1.addTerm(1.0, var_strategy_observation.get(actionIndex).get(obs));
					expr2.addTerm(max_value, var_strategy_observation.get(actionIndex).get(obs));
				}				
				if (s.getOriginalState()==0) { // initial State
					model.addConstr(expr1, GRB.LESS_EQUAL, max_value, "c" + constarint_count++);
					model.addConstr(expr2, GRB.GREATER_EQUAL, 1.0, "c" + constarint_count++);
					continue;
				} 
				
				HashSet<PreObs> pres =  observationToPreobservation.get(obs);
				if (pres == null) {
					System.out.println("obs"+obs);
				}
				if (pres!= null && pres.size() != 0) {
					for (PreObs p: pres) {
						int preobservation = p.getObservation();
						int actionIndex = p.getActionIndex();
						//TODO check it be skiped?
						if (preobservation == obs){
							continue;
						}
						expr1.addTerm(-max_value, var_strategy_observation.get(actionIndex).get(preobservation));
						expr2.addTerm(-1.0, var_strategy_observation.get(actionIndex).get(preobservation));
					}
				}
				System.out.println("adding constraint"+obs);
				model.addConstr(expr1, GRB.LESS_EQUAL, 0.0, "1b_c" + constarint_count++ + "oobs"+obs);
				model.addConstr(expr2, GRB.GREATER_EQUAL, 0.0, "1c_c" + constarint_count++);
			}

			if (false){
				for (int a = 1; a < 5; a++) {
					expr1 = new GRBLinExpr();
					expr1.addTerm(1.0, var_strategy_observation.get(a).get(6));
					model.addConstr(expr1, GRB.EQUAL, 0.0, "zz_c" + ( constarint_count++) + "oobs"+6);
				}
				
				for (int a = 1; a < 5; a++) {
					expr1 = new GRBLinExpr();
					expr1.addTerm(1.0, var_strategy_observation.get(a).get(7));
					model.addConstr(expr1, GRB.EQUAL, 0.0, "zz_c" + ( constarint_count++) + "oobs"+7);
				}
				
				for (int actionIndex = 1; actionIndex < 5; actionIndex++) {
					expr1 = new GRBLinExpr();
					expr1.addTerm(1.0, var_strategy_observation.get(actionIndex).get(5));
					if (actionIndex == 4) {
						model.addConstr(expr1, GRB.EQUAL, 1.0, "zz_c" + 10 * constarint_count++ + "oobs"+5);	
					}
					else {
						model.addConstr(expr1, GRB.EQUAL, 0.0, "zz_c" + 10 * constarint_count++ + "oobs"+5);	
					}
				}
				
				for (int actionIndex = 1; actionIndex < 5; actionIndex++) {
					expr1 = new GRBLinExpr();
					expr1.addTerm(1.0, var_strategy_observation.get(actionIndex).get(8));
					if (actionIndex == 1) {
						model.addConstr(expr1, GRB.EQUAL, 1.0, "zz_c" + 10 * constarint_count++ + "oobs"+8);	
					}
					else {
						model.addConstr(expr1, GRB.EQUAL, 0.0, "zz_c" + 10 * constarint_count++ + "oobs"+8);	
					}
				}
				
			}
			
			// constraints for transition relations and rewards
			
			for (State s: states) {
				int obs = s.getObservation();
				for (Object action: pomdp.getAvailableActions(s.getOriginalState())) {
					int actionIndex = actionToIndex.get(action);
					for (int i = 0; i < nObj; i++) {
						expr1 = new GRBLinExpr();
						expr2 = new GRBLinExpr();
						expr1.addTerm(1, var_value_lo.get(i).get(s));
						expr2.addTerm(1, var_value_up.get(i).get(s));

						String a = "";
						
//						a = var_value_up.get(i).get(s).get(GRB.StringAttr.VarName);

						int originalState = s.getOriginalState();
						int choice = pomdp.getChoiceByAction(originalState, action);
						double r = mdpRewardsList.get(i).getTransitionReward(originalState, choice) + mdpRewardsList.get(i).getStateReward(originalState); // 
						System.out.println("Rewad" + originalState + " " + action + " " + r);
//						if (minMaxList.get(i).isMin()) {
//							r *= -1;
//						}
						for (State nxt: states) {
							Transition tran = new Transition(s, actionIndex, nxt);
							if (!transition_probability.containsKey(tran) || transition_probability.get(tran) == 0) {
								continue;
							}
							double prob =  transition_probability.get(tran);
							expr1.addTerm(-prob * discount, var_value_lo.get(i).get(nxt));
							expr2.addTerm(-prob * discount, var_value_up.get(i).get(nxt));
						}
						
						expr1.addTerm(max_value, var_strategy_observation.get(actionIndex).get(obs));
						expr2.addTerm(-max_value, var_strategy_observation.get(actionIndex).get(obs));
						model.addConstr(expr1, GRB.LESS_EQUAL, r + max_value,  
								 a + "1d_c" + constarint_count++);
						
						model.addConstr(expr2, GRB.GREATER_EQUAL, r - max_value, "1e_c" + constarint_count++);
					}
				}
			}
			
			// constraints for objective bounds
			for (int i = 0; i < nObj; i++) {
				expr1 = new GRBLinExpr();
				expr1.addTerm(1.0, var_value_lo.get(i).get(sInit));
				model.addConstr(expr1, GRB.GREATER_EQUAL, lowerBounds[i], "1f_c" + constarint_count++);

				expr2 = new GRBLinExpr();
				expr2.addTerm(1.0, var_value_up.get(i).get(sInit));
				model.addConstr(expr2, GRB.LESS_EQUAL, upperBounds[i], "1g_c" + constarint_count++);
			}

			model.write(String.format("MultiStrategy_%d.lp", batch));
			model.optimize();
			double val = 0;
			if (model.get(GRB.IntAttr.Status)== GRB.Status.INFEASIBLE) {
				System.out.println("model infeasible");
				model.computeIIS();
				model.write(String.format("MultiStrategy__Infeasible_%d.ilp",batch));		
				model.feasRelax(0, false, true, true);
				model.optimize();
//				model.write(String.format("MultiStrategy__Infeasible_relaxed_%d.ilp",batch));
//				
			}else {
				System.out.println("Mdoel feasible with value of : " + val);
				val = model.get(GRB.DoubleAttr.ObjVal);
				model.write(String.format("MultiStrategy_%d.sol", batch));
			}
//			
			System.out.println("MultiStrategy_ Obj: " + val);
			
	      // Dispose of model and environment
	      model.dispose();
	      env.dispose();
	      return val;
		} catch (GRBException e) {
			throw new PrismException("Error solving LP: " +e.getMessage());
		}
	}
	public void computeBounds(String prefString, StateValues sv) throws PrismException{
		// Store MDP info
		int objNum = minMaxList.size();
		// Parsing preference weights as a set of extreme points
		// Format: ([obj1_lower, obj1_upper], [obj2_lower, obj2_upper], ...)
		ArrayList<ArrayList<Double>> pref = new ArrayList<ArrayList<Double>>();
		Pattern p = Pattern.compile("\\[(.*?)\\]");
		Matcher m = p.matcher(prefString);
		while (m.find()) {
			ArrayList<Double> w = new ArrayList<Double>();
			String s = m.group(1);
			String[] sa = s.split(", ");
			for(int i=0; i<sa.length;i++) {
				double v = Double.parseDouble(sa[i]);
				w.add(v);
			}
			pref.add(w);
		}
//		mainLog.println("Preferences: " + pref);

		// convert preference to format ([l_obj1, l_obj2, ...],...,[l_obj1, l_obj2, ...])
		ArrayList<ArrayList<Double>> prefWeights = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> pref_lower = new ArrayList<Double>();
		ArrayList<Double> pref_upper = new ArrayList<Double>();
		for(int i=0; i<pref.size(); i++){
			pref_lower.add(pref.get(i).get(0));
			pref_upper.add(pref.get(i).get(1));
		}
		prefWeights.add(pref_lower);
		prefWeights.add(pref_upper);
//		mainLog.println("PrefWeights: " + prefWeights);


		// compute EPs corresponding to the given prefWeights
	    Set<ArrayList<Double>> prefWeights_EPs = new HashSet<ArrayList<Double>>();
	    if (objNum==2){
	    	// fix x on boundaries and check y
	    	for(int i=0; i<2; i++){
	    		Double x = prefWeights.get(i).get(0);
	    		Double y = 1-x;
	    		if (y >= prefWeights.get(0).get(1) && y <= prefWeights.get(1).get(1)){
	    			ArrayList<Double> EP = new ArrayList<Double>();
	    			EP.add(Math.round(x * 100.0)/100.0);
	    			EP.add(Math.round(y * 100.0)/100.0);
	    			prefWeights_EPs.add(EP);  // add (x,y) to EPs
	    		}
	    	}
	    	// fix y on boundaries an check x
	    	for(int i=0; i<2; i++){
	    		Double y = prefWeights.get(i).get(1);
	    		Double x = 1-y;
	    		if (x >= prefWeights.get(0).get(0) && x <= prefWeights.get(1).get(0)){
	    			ArrayList<Double> EP = new ArrayList<Double>();
	    			EP.add(Math.round(x * 100.0)/100.0);
	    			EP.add(Math.round(y * 100.0)/100.0);
	    			prefWeights_EPs.add(EP);  // add (x,y) to EPs
	    		}
	    	}
	    }
	    else if (objNum==3){
	    	// fix x,y, check z
	    	for(int i=0; i<2; i++){
	    		for(int j=0; j<2; j++){
	    			Double x = prefWeights.get(i).get(0);
	    			Double y = prefWeights.get(j).get(1);
	    			Double z = 1-x-y;
	    			if (z >= prefWeights.get(0).get(2) && z <= prefWeights.get(1).get(2)){
	    				ArrayList<Double> EP = new ArrayList<Double>();
	    				EP.add(Math.round(x * 100.0)/100.0);
	    				EP.add(Math.round(y * 100.0)/100.0);
	    				EP.add(Math.round(z * 100.0)/100.0);
	    				prefWeights_EPs.add(EP);  // add (x,y) to EPs
	    			}
	    		}
	    	}
	
	    	// fix x,z, check y
	    	for(int i=0; i<2; i++){
	    		for(int j=0; j<2; j++){
	    			Double x = prefWeights.get(i).get(0);
	    			Double z = prefWeights.get(j).get(2);
	    			Double y = 1-x-z;
	    			if (y >= prefWeights.get(0).get(1) && y <= prefWeights.get(1).get(1)){
	    				ArrayList<Double> EP = new ArrayList<Double>();
	    				EP.add(Math.round(x * 100.0)/100.0);
	    				EP.add(Math.round(y * 100.0)/100.0);
	    				EP.add(Math.round(z * 100.0)/100.0);
	    				prefWeights_EPs.add(EP);  // add (x,y) to EPs
	    			}
	    		}
	    	}
	
	    	// fix y,z, check x
	    	for(int i=0; i<2; i++){
	    		for(int j=0; j<2; j++){
	    			Double y = prefWeights.get(i).get(1);
	    			Double z = prefWeights.get(j).get(2);
	    			Double x = 1-y-z;
	    			if (x >= prefWeights.get(0).get(0) && x <= prefWeights.get(1).get(0)){
	    				ArrayList<Double> EP = new ArrayList<Double>();
	    				EP.add(Math.round(x * 100.0)/100.0);
	    				EP.add(Math.round(y * 100.0)/100.0);
	    				EP.add(Math.round(z * 100.0)/100.0);
	    				prefWeights_EPs.add(EP);  // add (x,y) to EPs
	    			}
	    		}
	    	}
	
	    }
		
	    
		int stateNum = pomdp.getNumStates();
		int sInit = pomdp.getFirstInitialState();

		ArrayList<Point> paretoPoints = (ArrayList<Point>) sv.getValue(sInit);
		
	    System.out.println("prefWeights_EPs: " + prefWeights_EPs);
		// Step 1: find the corresponding Pareto point for each preference weight
		ArrayList<Integer> chosenPoints = new ArrayList<Integer>();
		Iterator iter = prefWeights_EPs.iterator();
		while (iter.hasNext()) {
			ArrayList<Double> weights = (ArrayList<Double>) iter.next();
			double rewardSum = Double.POSITIVE_INFINITY;
			int pointIndex = -1;
			for (int i=0; i<paretoPoints.size(); i++) {
				Point pp = paretoPoints.get(i);
				double[] pv = pp.getCoords();
				double tmpSum = 0.0;
				for (int j=0; j<objNum; j++) {
					if (minMaxList.get(j).isMin()) {
						tmpSum += weights.get(j) * pv[j];
					} else {
						tmpSum += weights.get(j) * (-pv[j]);
					}
				}
				if (tmpSum < rewardSum) {
					rewardSum = tmpSum;
					pointIndex = i;
				}
			}
			chosenPoints.add(pointIndex);
			System.out.println("Preference weight extreme point " + weights + " -> Pareto point " + paretoPoints.get(pointIndex));
			
			//TODO: check if necessary
//			boolean sanity = false; 	// Sanity check 
//			if (sanity) {
//				ModelCheckerResult res = computeMultiReachRewards(pomdp, weights, mdpRewardsList, target, true, null);
//				List<Double> point = (List<Double>) res.solnObj[sInit];
//				System.out.println("Preference weights " + weights + " -> Pareto point (PRISM weighted)" + point);
//			}
			
		}
		
		// Step 2: determine the lower/upper bounds for each objective based on the set of chosen Pareto points
//		double[] lowerBounds = new double[objNum];
//		double[] upperBounds = new double[objNum];
		for (int i=0; i<objNum; i++) {
			lowerBounds[i] = Double.POSITIVE_INFINITY;
			upperBounds[i] = Double.NEGATIVE_INFINITY;
		}
		Iterator itp = chosenPoints.iterator();
		while (itp.hasNext()) {
			int pIndex = (Integer) itp.next();
			Point pp = paretoPoints.get(pIndex);
			double[] pv = pp.getCoords();
			for (int i=0; i<objNum; i++) {
				if (lowerBounds[i] > pv[i]) {
					lowerBounds[i] = Math.floor(pv[i]);
				}
				if (upperBounds[i] < pv[i]) {
					upperBounds[i] = Math.ceil(pv[i]);
				}
			}
		}
		System.out.println("*********** Objective bounds ************");
		System.out.println("Lower bounds [obj1, obj2, ...]: " + Arrays.toString(lowerBounds));
		System.out.println("Upper bounds [obj1, obj2, ...]: " + Arrays.toString(upperBounds));
		
//		setObjectiveBounds();
		
	}
	protected double[][] setObjectiveBounds(double []lo, double []up) throws PrismException
    {
    	double bounds[][] = new double[2][nObj];
		lowerBounds = lo;
		upperBounds = up; 
		bounds[0] = lowerBounds;
		bounds[1] = upperBounds;
    	return bounds;
    }
	protected double[][] setObjectiveBounds() throws PrismException
    {
    	int nObj = minMaxList.size();
    	double bounds[][] = new double[2][nObj];

		//TODO
		lowerBounds[0] = 46/3*0.95;
		lowerBounds[1] = 46/3*0.95;
		upperBounds[0] = 84/3*0.95;
		upperBounds[1] = 84/3*0.95;
		
		double lo = 2.8;
		lowerBounds[0] = lo;
		lowerBounds[1] = lo;
		double up = 33;
		upperBounds[0] = up;
		upperBounds[1] = up;
		
		bounds[0] = lowerBounds;
		bounds[1] = upperBounds;
    	return bounds;
    }
} 
