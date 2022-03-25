/*This file is adopted from https://github.com/GeorgePik/POMCP */
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
import acceptance.AcceptanceReach;
import automata.DA;
import java.util.Iterator;
import cern.colt.Arrays;
import common.StopWatch;
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

 class POMCPNode{
	private double id;
	private POMCPNode parent;
	private double count;
	private int h;
	private Object hAction;
	private double v;
	private double n;
	private POMCPBelief belief;
	private ArrayList<POMCPNode> children;
	private long startTime ;
	public POMCPNode() 
	{
		this.id = -1;
		this.startTime = System.currentTimeMillis();

		//this.parent = new POMCPNode();
		this.h = -1;
		this.hAction = -1;
		this.belief = new POMCPBelief();
		
		this.v = 0;
		this.n = 0;

	}
	public long getStartTime() {
		return startTime;
	}

	public void setH(int h) 
	{
		this.h = h;
	}
	public void setHAction(Object a, boolean isAction) 
	{
		if (isAction) {
			this.hAction = a;
		}
	}
	public int getH() 
	{
		return h;
	}
	public double getN() {
		return n;
	}
	public double getV() {
		return v;
	}
	public void increaseN(double value) {
		n += value;
	}
	public void increaseV(double value) {
		v += value;
	}
	public void setParent(POMCPNode parent) {
		this.parent = parent;
	}
	public POMCPNode getParent() {
		return parent;
	}
	
	public ArrayList<POMCPNode> getChildren() 
	{
		return children;
	}
//	
//	public POMCPNode getRandomPossiblechild(int state) {
//		ArrayList<POMCPNode> possibleChildren = new ArrayList<POMCPNode> (); 
//		for (int i = 0; i < children.size(); i++ ) {
//			POMCPNode child = children.get(i);
//			Object action = allActions.get(child.getH());
//			possibleChildren.add(child);
//		}
//		Random rnd = new Random();
//		return children.get(rnd.nextInt(children.size()));
//	}
	
	public void addChild(POMCPNode child) {
		if (children == null) {
			children = new ArrayList<POMCPNode> ();
		}
		children.add(child);
	}
	public void setBelief(POMCPBelief belief) 
	{
		this.belief = belief;
	}
	public POMCPBelief getBelief() 
	{
		return belief;
	}
	public void addBeliefParticle(int s) 
	{
		belief.addParticle(s);
	}
}

 class POMCPBelief{
	 private ArrayList<Integer> particles;
	 private HashSet<Integer> uniqueStatesInt;
	 private BitSet uniqueStates;
	 POMCPBelief()
	 {
		 this.particles = new ArrayList<Integer>();
		 uniqueStates = new BitSet();
		 uniqueStatesInt = new HashSet<Integer> ();
	 }
	 public void disaplyParticles() {
		 if (particles != null) {
			 for (int i =0; i < particles.size(); i++) {
				 System.out.println(" " + i + particles.get(i));
			 }
		 }
	 }
	 public int sample() {
		 Random rnd = new Random();
		 return particles.get(rnd.nextInt(particles.size()));
	 }
	 public void addParticle(Integer s) {
		 particles.add(s);
		 uniqueStates.set(s);
		 uniqueStatesInt.add(s);
	 }
	 public BitSet getUniqueStates() {
		 return uniqueStates;
	 }
	 public boolean isStateInBelief(int s) {
		 return uniqueStates.get(s);
	 }
	 public void displayUniqueStates() {
		for (int i = uniqueStates.nextSetBit(0); i >=0; i= uniqueStates.nextSetBit(i+1)) {
			System.out.println(i);
		}
	 }
	 public boolean isDepleted() {
		 return (particles.size()==0);
	 }
	 public int size() {
		 if (particles == null) {
			 return 0;
		 }
		 return particles.size();
	 }
	 public double[] getDist(int numStates) {
		 double[] dist = new double[numStates];
		 double sum = 0;
		 for (int p = 0; p < particles.size(); p++) {
			 dist[particles.get(p)] += 1;
			 sum += particles.get(p);
		 }
		 for (int s =0; s < numStates; s++) {
			 dist[s] /= particles.size();
		 }
		 return dist;
	 }
 }

public class PartiallyObservableMonteCarloPlanning {
	private double gamma; // discount
	private double e; // threshold below which discount is too little
	private double c; // higer value to encourage UCB exploration
	private double timeout;
	private double noParticles;
	private int K;
	private double[] initialBelief;
	private POMCPBelief initialBeliefParticles;
	private double Tree; // Tree
	ArrayList<ArrayList<Object>> history; 
	ArrayList<Object> allActions; 
	Map <Object, Integer> actionToIndex;
	private POMCPNode root;
	
	private POMDP pomdp;
	private MDPRewards mdpRewards;
	private BitSet target;
	private boolean min;
	private BitSet statesOfInterest;
	private ArrayList<Integer> endStates;
	public PartiallyObservableMonteCarloPlanning(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest, ArrayList<Integer> endStates,
			double gamma, double c, double threshold, double timeout, double noParticles) 
	{
		/*
		 * Generator (function): Specifies a function to be used as a blackbox generator for the underlying POMDP dynamics. This will be called during simulations and should take as arguments the indices of a state and an action in the underlying state and action spaces.
			gamma (float): The discounting factor for cost calculation. Should be <1. Default value is 0.95.
			e (float): Threshold value below which the expected sum of discounted rewards for the POMDP is considered 0. Default value is 0.005.
			c (float): Parameter that controls the importance of exploration in the UCB heuristic. Default value is 1.
			timeout (int): Controls the number of simulations that will be run from the current root at each call of Search(). Default value is 10000.
			no_particles (int): Controls the maximum number of particles that will be kept at each node and the number of particles that will be sampled from the posterior belief after an action is taken. Default value is 1200.
			Parallel (Boolean): Controls if posterior belief is sampled with multiple threads. Default value is False. Tested only on Ubuntu.
		 * */
		this.pomdp = pomdp;
		this.mdpRewards = mdpRewards;
		this.target = target;
		this.min = min;
		this.statesOfInterest = statesOfInterest;
		this.endStates = endStates;
		this.gamma = gamma;
		this.e = threshold;
		this.c = c;
		this.timeout = timeout;
		this.noParticles = noParticles;
		
		this.K = 10000;
		getAllActions();
		setActionToIndex();
		this.initialBelief = pomdp.getInitialBeliefInDist();
		this.history = new ArrayList<ArrayList<Object>>() ;
		
		this.initialBeliefParticles = new POMCPBelief(); 
		
		for (int p = 0; p < K; p ++) {
			int s = drawStateFromBelief(this.initialBelief);
			this.initialBeliefParticles.addParticle(s);
		}
		this.root = new POMCPNode();
		root.setBelief(this.initialBeliefParticles);
		//root.getBelief().disaplyParticles();
	}
	
	
	public void getAllActions(){
		allActions = new ArrayList<Object> ();
		for (int s = 0; s<pomdp.getNumStates();s++) {
			List <Object> availableActionsForState = pomdp.getAvailableActions(s);
			for (Object a: availableActionsForState) {
				if (!allActions.contains(a) & a!= null) {
					allActions.add(a);
				}
			}
		}
	}
	
	public void setActionToIndex() {
		actionToIndex = new HashMap<Object, Integer>();
		for (int i = 0; i < allActions.size(); i++) {
			actionToIndex.put(allActions.get(i), i);
		}
	}
	
	public Integer getActionIndex(Object action) 
	{
		if (allActions.size() <= 0) {
			return -1;
		}
		for (int a = 0; a< allActions.size(); a++) {
			if (allActions.get(a) == action) {
				return a;
			}
		}
		return -1;
	}
	
	public void setRoot(POMCPNode node) 
	{
		this.root = node;
	}
	
	public void update(Object action, int obs) 
	{
		// update/ prune tree given real action and observation
		ArrayList<Object> actionObservation = new ArrayList<Object>();
		actionObservation.add(action);
		actionObservation.add(obs);
		history.add(actionObservation);
		
		POMCPNode aNode = new POMCPNode();
		ArrayList<POMCPNode> children = root.getChildren();
		if (children.size()>0) {
			for (int i =0; i < children.size(); i++) {
				POMCPNode child = children.get(i);
				int actionIndex = getActionIndex(action);
				if (child.getH() == actionIndex) {
					aNode = child;
					break;
				}
			}
		}
		
		POMCPNode oNode = getObsNode(aNode, obs);
		
		invigorateBelief(root, oNode, action, obs);
		
		setRoot(oNode);
	}
	
	public POMCPNode getObsNode(POMCPNode aNode, int obs) 
	{
        //"""Get the observation node from given action node, adding a new node if necessary """
		ArrayList<POMCPNode> children = aNode.getChildren();
		
		/// what if no children
		if(children != null) {
			for (int i =0; i < children.size(); i++) {
				POMCPNode child = children.get(i);
				if (child.getH() ==  obs) {
					return child;
				}
			}
		}
		
		POMCPNode oNode = new POMCPNode();
		oNode.setH(obs);
		oNode.setParent(aNode);
		aNode.addChild(oNode);
		return oNode;
	}
	public void invigorateBelief(POMCPNode parent, POMCPNode child, Object action, int obs) {
		// fill child belief with particles
		
		int childBeliefSize = child.getBelief().size();
		while (childBeliefSize < K) {
			int s = parent.getBelief().sample();
//			int choice = pomdp.getChoiceByAction(s, action);
//			int nextState = step(s, choice);
//			int obsSample = pomdp.getObservation(nextState);
//			double reward = mdpRewards.getTransitionReward(s, choice) + mdpRewards.getStateReward(s);
			ArrayList<Double> sord = step(s, action);
			int nextState = sord.get(0).intValue();
			int obsSample = sord.get(1).intValue();
			double reward = sord.get(2);
			double d = sord.get(3);
			if (obsSample == obs) {
				child.addBeliefParticle(nextState);
				// print Belief patcile
				childBeliefSize += 1;
			}
		}
	}
	
//	public int step(int state, int choice) {
//		Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(state, choice);
//		ArrayList<Integer> nextStates = new ArrayList<Integer> ();
//		ArrayList<Double> nextStatesProbs = new ArrayList<Double> ();
//		while (iter.hasNext()) {
//			Map.Entry<Integer, Double> trans = iter.next();
//			nextStates.add(trans.getKey());
//			nextStatesProbs.add(trans.getValue());
//		}
//		
//		int nextState = drawStateFromDistr(nextStatesProbs);
//		return nextState;
//	}
	
	public ArrayList<Double> step(int state, Object action){
		ArrayList<Double> sord = new ArrayList<Double> ();
		//pomdp.getObservationProbAfterChoice(null, state, state);
		if(!pomdp.getAvailableActions(state).contains(action)) {
			System.out.print("error ");
			sord.add(0.0);
			sord.add(-1.0);
			sord.add(-1.0);
			sord.add(1.0);
		}
		int choice = pomdp.getChoiceByAction(state, action);
		Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(state, choice);
		ArrayList<Integer> nextStates = new ArrayList<Integer> ();
		ArrayList<Double> nextStatesProbs = new ArrayList<Double> ();
		while (iter.hasNext()) {
			Map.Entry<Integer, Double> trans = iter.next();
			nextStates.add(trans.getKey());
			nextStatesProbs.add(trans.getValue());
		}
		
		int nextState = nextStates.get(drawStateFromDistr(nextStatesProbs));
		
		int obs = pomdp.getObservation(nextState);
		
		double reward = mdpRewards.getTransitionReward(state, choice) + mdpRewards.getStateReward(state);
		if (min) {
			reward *= -1;
		}
		double d = 0; // whether next state is terminal
		if (endStates.contains(nextState)){
			d = 1;
		}
		sord.add(Double.valueOf(nextState));
		sord.add(Double.valueOf(obs));
		sord.add(reward);
		sord.add(d);
		return sord;
	}
	public Object search( ) 
	{
		
//		System.out.println("\nsearch...");
		Object action = null;
		int state = 0;
		int numSearch = 20000;
		int n = 0;
		long startTime = System.currentTimeMillis();
		
		if(root.getChildren() == null) {
			//state = initialBeliefParticles.sample();
			expand(root);
		}
		
		while (n < numSearch){
			//System.out.println("Search"+n);
			double elapsed = (System.currentTimeMillis() - startTime) * 0.001;
			if (elapsed > timeout) {
				break;
			}
			n += 1;
			state = root.getBelief().sample();
			//simulate(state, history, 0);
			simulate(state, root, 0);
		}
		
		return greedyAction(root, state);		
	}

	public int drawStateFromBelief(double[] belief) 
	{
		int state = 0;
		double randomThreshold = Math.random();
		double cumulativeProb = 0;
		for (int i = 0; i < belief.length; i++) {
			cumulativeProb += belief[i];
			if (cumulativeProb >= randomThreshold) {
				state = i;
				break;
			}
		}
		return state; 
	}
	
	public int drawStateFromDistr(ArrayList<Double> distr) 
	{
		int state = 0;
		double randomThreshold = Math.random();
		double cumulativeProb = 0;
		for (int i = 0; i < distr.size(); i++) {
			cumulativeProb += distr.get(i);
			if (cumulativeProb >= randomThreshold) {
				state = i;
				break;
			}
		}
		return state; 
	}
	
//	public double simulate(int state, ArrayList<ArrayList<Object>> history, int d) {
//		// if maximum depth, return
//		if ((Math.pow(gamma, d) < e) && d != 0)  {
//			return 0;
//		}
//		// if it is a leave node, 
//		//if (history.size() == 0) {
//			// expand
//			// return rollout
//		//}
//		return 0;
//	}
	public double simulate(int state, POMCPNode node, int d) {
		double simR = 0;
		if ((Math.pow(gamma, d) < e) && d != 0)  {
			return 0;
		}
		if (node.getChildren()  == null) {
			expand(node);
			double rollR = rollout(state, d);
			return rollR;
		}

		POMCPNode aNode = uctActionGetAction(node, state);
		int a = aNode.getH();
		Object action = allActions.get(a);
		
		//List<Object>  availableActions = pomdp.getAvailableActions(state);
		//System.out.println("legit action"+availableActions.contains(action));
		
		
		// action = uct_action(node)
		// next_s, o, r, d, = self.M.step(s, a)
		ArrayList<Double> sord = step(state, action);
		
		int nextState = sord.get(0).intValue();
		int obsSample = sord.get(1).intValue();
		double reward = sord.get(2);
		double done = sord.get(3);
		POMCPNode ONode = getObsNode(aNode, obsSample);
		
		ONode.addBeliefParticle(nextState);
		
		if (done == 1) {
			simR = reward;
		}
		else {
			simR = reward + gamma * simulate(nextState, ONode, d + 1);
		}
		if (node.getH() != -1) {
			node.addBeliefParticle(state);
		}
		node.increaseN(1);
		aNode.increaseN(1);
		double val = (simR - aNode.getV()) / aNode.getN();
		///val = Math.abs(val);
		aNode.increaseV(val);
		
		return simR;
	}
	
	
	public void expand(POMCPNode parent) {

		BitSet uniqueStates = parent.getBelief().getUniqueStates();
		HashSet <Object> availableActionsForBelief = new HashSet<Object> ();

		for (int i = uniqueStates.nextSetBit(0); i >= 0; i= uniqueStates.nextSetBit(i+1)) {
			List<Object> availableActionsForState = pomdp.getAvailableActions(i);
			availableActionsForBelief.addAll(availableActionsForState);
		}
		for (Object action : availableActionsForBelief) {
			POMCPNode newChild = new POMCPNode ();
			int a = actionToIndex.get(action);
			newChild.setH(a);
			newChild.setHAction(action, true);
			newChild.setParent(parent);;
			parent.addChild(newChild);
		}
		
//	//////////////////	
//		for (int a = 0; a < allActions.size(); a++) {
//			POMCPNode newChild = new POMCPNode ();
//			// how to make sure this action is legal????????
//			newChild.setH(a);
//			newChild.setParent(parent);;
//			parent.addChild(newChild);
//		}
//		
	}
	
	public double rollout(int state, int d) {
		if (Math.pow(gamma, d) < e) {
			return 0;
		}
		List <Object> availableActions = pomdp.getAvailableActions(state);
		
		//only legal direction
		for (int a = availableActions.size() -1 ; a >=0; a--) {
			if (step(state,  availableActions.get(a)).get(2) == -100 ) {
				availableActions.remove(a);
			}
		}
		
		if (availableActions.size() <= 0) {
			return 0;
		}
		Random rnd = new Random();
		
		Object randomAction = availableActions.get(rnd.nextInt(availableActions.size()));
		
		ArrayList<Double> sord = step(state, randomAction);
		int nextState = sord.get(0).intValue();
		double reward = sord.get(2);
		double done = sord.get(3);
		if (done == 1) {
			return reward;
		}
		return reward + gamma * rollout(nextState, d + 1);
	}
	
	public POMCPNode uctActionGetAction(POMCPNode node, int state) 
	{
		ArrayList<POMCPNode> children = node.getChildren();

		if (node.getN() == 0) {
			Random rnd = new Random();
			return children.get(rnd.nextInt(children.size()));
			
//			ArrayList<POMCPNode> possibleChildren = new ArrayList<POMCPNode> (); 
//			for (int i = 0; i < children.size(); i++ ) {
//				POMCPNode child = children.get(i);
//				Object action = allActions.get(child.getH());
//				if (pomdp.getAvailableActions(state).contains(action) ) {
//					possibleChildren.add(child);
//				}
//			}
//			return possibleChildren.get(rnd.nextInt(possibleChildren.size()));
//			
//			
			
			
		}
		
		double logN = Math.log(node.getN());
		double maxV = Double.NEGATIVE_INFINITY;
		POMCPNode maxChild = new POMCPNode();
		
		for (int i = 0; i < children.size(); i++) {
			POMCPNode child = children.get(i);
			Object action = allActions.get(child.getH());
			if (!pomdp.getAvailableActions(state).contains(action)) {
				continue;
			}
			if (child.getN() == 0) {
				return child;
			}
			double child_UCT_V = child.getV() + this.c * Math.sqrt(logN / child.getN());
			if (child_UCT_V > maxV) {
				maxV = child_UCT_V;
				maxChild = child;
			}
		}
		if (!pomdp.getAvailableActions(state).contains(allActions.get(maxChild.getH()))) {
			System.out.println("Er");
		}
		return maxChild;
	}
	
	public Object greedyAction(POMCPNode node, int state){
		double maxV = Double.NEGATIVE_INFINITY;
		int maxA = -1;
		ArrayList<POMCPNode> children = node.getChildren();
		for (int i = 0; i < children.size(); i++) {
			POMCPNode child = children.get(i);
			Object action = allActions.get(child.getH());
			if (! pomdp.getAvailableActions(state).contains(action)) {
				continue;
			}
			if(maxV < child.getV()) {
				maxV = child.getV();
				maxA = child.getH();
			}
		}
		return allActions.get(maxA);
	}
	public void display() {
		ArrayList<POMCPNode> q = new ArrayList<POMCPNode> ();
		ArrayList<POMCPNode> q2= new ArrayList<POMCPNode> ();
		q.add(root);
		int layer = 0;
		while (q.size()>0) {
			int size = q.size();
			q2.clear();
			System.out.println("============="+q.size()+"layer = "+ layer++);
			for (int i = 0; i < size; i++) {
				POMCPNode cur = q.get(i);
				System.out.println("h = "+ cur.getH() + " N = "+ cur.getN() + " V = "+ cur.getV() + "startTime = " + String.valueOf(cur.getStartTime()));
				ArrayList<POMCPNode> children = cur.getChildren();
				if (children != null) {
					for (int j = 0; j < children.size(); j++) {
						if (children.get(j).getH()!= -1) {
							q2.add(children.get(j));
						}
					}
				}
			}
			ArrayList<POMCPNode> temp = q;
			q = q2;
			q2 = temp;
		}
	}
	public void displayRoot() {
		System.out.println("Root" + root.getH());
		root.getBelief().displayUniqueStates();
		System.out.println("__________");
		
	}
	public void displayVar() {
		String variables = "";
		for (int i =0; i < pomdp.getVarList().getNumVars(); i++) {
			variables += pomdp.getVarList().getName(i) + ",";
		}
		System.out.println(variables);
	}
	
	public void displayState(int state) {
		System.out.println("state = " + state + pomdp.getStatesList().get(state));
	}
}


	






