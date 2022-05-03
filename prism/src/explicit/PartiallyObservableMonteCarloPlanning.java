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
import java.io.*;
import java.math.BigInteger;

 class POMCPNode{
	private int id;
	private boolean isONode;
	private POMCPNode parent;
	private int h;
	private Object hAction;
	private double v;
	private double n;
	private POMCPBelief belief;
	private ArrayList<POMCPNode> children;
	public POMCPNode() 
	{
		this.id = -1;
		this.belief = new POMCPBelief();
		clear();
	}
	public void clear() 
	{
		this.parent = null;
		this.children = null;
		this.h = -1;
		this.hAction = -1;
		this.isONode = true;
		this.v = 0;
		this.n = 0;
	}
	public boolean isONode()
	{
		return isONode;
	}
	public void setID (int id) 
	{
		this.id = id;
	}
	public int getID () 
	{
		return id;
	}
	public void setH(int h) 
	{
		this.h = h;
	}
	public void setHAction(Object a, boolean isAction) 
	{
		if (isAction) {
			this.hAction = a;
			this.isONode = false;
		}
	}
	public Object getHAction() 
	{
		return hAction;
	}
	public int getH() 
	{
		return h;
	}
	public double getN() 
	{
		return n;
	}
	public double getV()
	{
		return v;
	}
	public void increaseN(double value) 
	{
		n += value;
	}
	public void increaseV(double value) 
	{
		v += value;
	}
	public void setParent(POMCPNode parent) 
	{
		this.parent = parent;
	}
	public POMCPNode getParent() 
	{
		return parent;
	}
	public ArrayList<POMCPNode> getChildren() 
	{
		return children;
	}
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
	 private int observation;
	 private ArrayList<Integer> particles;
	 private HashSet<Integer> uniqueStatesInt;
	 private BitSet uniqueStates;
	 POMCPBelief()
	 {
		 this.particles = new ArrayList<Integer>();
		 uniqueStates = new BitSet();
		 uniqueStatesInt = new HashSet<Integer> ();
	 }
	 public Integer sample() 
	 {
		 Random rnd = new Random();
		 return particles.get(rnd.nextInt(particles.size()));
	 }
	 public void addParticle(Integer s) 
	 {
		 particles.add(s);
		 uniqueStates.set(s);
		 uniqueStatesInt.add(s);
	 }
	 public BitSet getUniqueStates() 
	 {
		 return uniqueStates;
	 }
	 public HashSet<Integer> getUniqueStatesInt(){
		 return uniqueStatesInt;
	 }
	 public boolean isStateInBelief(int s) 
	 {
		 return uniqueStates.get(s);
	 }
	 public void displayUniqueStates() 
	 {
		if(particles.size()==0) 
		{
			System.out.println("No particles");
		}
		else {
			for (int i = uniqueStates.nextSetBit(0); i >= 0; i= uniqueStates.nextSetBit(i+1)) {
				System.out.println(i);
			}
		}
	 }
	 public boolean isDepleted() 
	 {
		 return (particles.size()==0);
	 }
	 public int size() 
	 {
		 if (particles == null) {
			 return 0;
		 }
		 return particles.size();
	 }
	 public double[] getDist(int numStates) 
	 {
		 double[] dist = new double[numStates];
		 double sum = 0;
		 for (int p = 0; p < particles.size(); p++) {
			 dist[particles.get(p)] += 1;
			 sum += particles.get(p);
		 }
//		 for (int s =0; s < numStates; s++) {
//			 dist[s] /= particles.size();
//		 }
		 return dist;
	 }
 }

public class PartiallyObservableMonteCarloPlanning {
	private double numSimulations;
	private int verbose;
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
	private int nodeCount;
	private int TreeDepth;
	private int PeakTreeDepth;
	private double [][] UCB;
	private int shieldLevel;
	private Map<Integer, ArrayList<ArrayList<BigInteger>>> winningRegion; 
	private ArrayList<ArrayList<Integer>> PrismObsToPrismStates ;
	private ArrayList<ArrayList<Integer>> StompyObsToStompyStates;
	private int[] PrismObsToStompyObs;
	private int[] StompyObsToPrismObs;
	private int[] PrismStateToStompyState;

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
		this.numSimulations =  Math.pow(2, 15);
		this.nodeCount = 0;
		this.verbose =  5;
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
		this.TreeDepth = 0;
		this.PeakTreeDepth = 0;
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
		root.setID(nodeCount);
		updateNodeCount();
		root.setBelief(this.initialBeliefParticles);
		//root.getBelief().disaplyParticles();
		initialUCB(10000, 100);
		
		shieldLevel = 1; //
		if (shieldLevel >0) {
			PrismObsToPrismStates = new ArrayList<ArrayList<Integer>> ();
			PrismObsToStompyObs =  new int[pomdp.getNumObservations()];
			StompyObsToPrismObs =  new int[pomdp.getNumObservations()];
			winningRegion = new HashMap<Integer, ArrayList<ArrayList<BigInteger>>>(); 
			StompyObsToStompyStates = new ArrayList<ArrayList<Integer>> (); // dummy
			importWinningRegion();
		}
	}
	public double fastUCB(int N, int n) 
	{
		if (N < 10000 && n < 100) {
			return UCB[N][n];
		}
		if (n == 0)
			return Double.POSITIVE_INFINITY;
		else
			return this.c * Math.sqrt(Math.log(N + 1) / n);
	}
	public void initialUCB(int UCB_N, int UCB_n)
	{
		UCB = new double [UCB_N][UCB_n];
		for (int N = 0; N < UCB_N; ++N) {
			for (int n = 0; n < UCB_n; ++n) {
				if (n == 0)
					UCB[N][n] = Double.POSITIVE_INFINITY;
				else
					UCB[N][n] = this.c * Math.sqrt(Math.log(N + 1) / n);
			}
		}
	}

	public void setNumSimulations(double n)
	{
		numSimulations = n;
	}
	public int getNodeCount()
	{
		return nodeCount;
	}
	public void updateNodeCount()
	{
		nodeCount += 1;
	}
	public void setVerbose(int v) 
	{
		verbose = v;
	}
	public void getAllActions()
	{
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
	public void setActionToIndex()
	{
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
		oNode.clear();
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
		oNode.setID(nodeCount);
		updateNodeCount();
		aNode.addChild(oNode);
		return oNode;
	}
	public void invigorateBelief(POMCPNode parent, POMCPNode child, Object action, int obs) 
	{
		// fill child belief with particles
		System.out.println("updating belief.." );
		System.out.println("root belief support" + parent.getBelief().getUniqueStatesInt());
		System.out.println("action " + action + " obs " + obs);

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
				if (!child.getBelief().getUniqueStatesInt().contains(nextState)) {
					System.out.println("add new" + nextState);
				}
				child.addBeliefParticle(nextState);
				childBeliefSize += 1;
			}
		}
	}
	
	
	public ArrayList<Double> step(int state, Object action){
		ArrayList<Double> sord = new ArrayList<Double> ();
		//pomdp.getObservationProbAfterChoice(null, state, state);
		if(!pomdp.getAvailableActions(state).contains(action)) {
			System.out.print("error step ");
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
	public Object selectAction() {
		boolean distableTrue = false;
		if (distableTrue) {
			return null;
		}
		else {
			UCTSearch();
		}
		
		int actionIndex = GreedyUCB(root, false);
		
		return allActions.get(actionIndex);
	}
	public void UCTSearch()
	{
		for (int n =0; n < numSimulations; n++) {
			int state = root.getBelief().sample();
			if(!root.getBelief().getUniqueStates().get(state)){
				System.out.println("EERRor" + state);
				root.getBelief().displayUniqueStates();
			}
			if (verbose >= 2 ) {
				System.out.println("================================Start UCT search  sample state" + state + " num Seracrh" + n);
			}
			TreeDepth = 0;
			PeakTreeDepth =0;
			double reward = simulateV(state, root);
			if (verbose >= 2 ) {
				System.out.println("==MCTSMCT after Num Simulation = " + n);
				System.out.println("MCTSMCTS");
				displayValue(2);
				System.out.println("===");
			}
		}
		if (verbose >=1) {
			displayValue(2);
		}
	}
	public double simulateV(int state, POMCPNode vnode)
	{

		if (vnode.getChildren() == null) {
			expand(vnode);
		}
		
		int actionIndex = GreedyUCB(vnode, true);
		PeakTreeDepth = TreeDepth;
		if (TreeDepth == 1) {
			vnode.getBelief().addParticle(state);
		}
		// qnode = vnode ->  Child
		ArrayList<POMCPNode> children = vnode.getChildren();
		POMCPNode qnode = new POMCPNode();
		for (POMCPNode child : children) {
			if (child.getH() ==  actionIndex) {
				qnode = child;
				break;
			}
		}
		
		double totalReward = simulateQ(state, qnode, actionIndex);
		
//		if(totalReward< -20) {
//			System.out.println("? <-20"+TreeDepth);
//		}
//		
		vnode.increaseV(totalReward);
		vnode.increaseN(1);
		return totalReward;
	}
	public double simulateQ(int state, POMCPNode qnode, int actionIndex) 
	{
		double delayedReward = 0;
		Object action = allActions.get(actionIndex);
		ArrayList<Double> sord = step(state, action);
		int nextState = sord.get(0).intValue();
		int observation = sord.get(1).intValue();
		double immediateReward = sord.get(2);
		double done = sord.get(3);
		
//		if(immediateReward< -20) {
//			System.out.println("_______"+TreeDepth);
//			displayState(state);
//			System.out.println("immediateReward? <-20" + "action "+ action + "reward= "+ immediateReward);
//			displayState(nextState);
//		}
//		
		if (verbose >= 3) {
			System.out.println("uct action=" + action + " reward=" + immediateReward );
			displayState(nextState);
		}
		
		boolean isvnode = false;
		POMCPNode vnode = new POMCPNode();

		state = nextState; 
		ArrayList<POMCPNode> children = qnode.getChildren();
		if (children == null) {
			isvnode = false;
		}
		else {
			for (POMCPNode child : children) {
				if (child.getH() ==  observation) {
					vnode = child;
					isvnode = true;
					break;
				}
			}
		}

		int paraExpandCount = 1;
		if (!isvnode && done == 0 && qnode.getN() >= paraExpandCount) {
			vnode = expandNode(state);			
			vnode.setH(observation);
			vnode.setID(nodeCount);
			updateNodeCount();
			vnode.setParent(qnode);
			qnode.addChild(vnode);
			isvnode = true;
		}
		
		if(done == 0) {
			TreeDepth++;
			if(isvnode) {
				delayedReward = simulateV(state, vnode);
			}
			else {
				delayedReward = Rollout(state);	
				//delayedReward = rollout(state , 0);	
			}
			TreeDepth--;
		}

		double totalReward = immediateReward + gamma * delayedReward;
		qnode.increaseN(1);
		qnode.increaseV(totalReward);
		return totalReward;
	}
	
	public POMCPNode expandNode(int state)
	{
		POMCPNode vnode = new POMCPNode ();
		List <Object> availableActions = getLegalActions(state);
		for (Object action : availableActions) {
			int actionIndex = getActionIndex(action);
			POMCPNode qnode = new POMCPNode();
			qnode.setH(actionIndex);
			qnode.setHAction(action, true);
			qnode.setParent(vnode);
			qnode.setID(nodeCount);
			nodeCount += 1;
			vnode.addChild(qnode);
		}
		return vnode;
	}
	
	public void expand(POMCPNode parent)
	{
		BitSet uniqueStates = parent.getBelief().getUniqueStates();
		int state = uniqueStates.nextSetBit(0);
		List <Object> availableActions = getLegalActions(state);
				
		for (Object action : availableActions) {
			POMCPNode newChild = new POMCPNode ();
			int a = actionToIndex.get(action);
			newChild.setH(a);
			newChild.setHAction(action, true);
			newChild.setParent(parent);;
			newChild.setID(nodeCount);
			updateNodeCount();
			parent.addChild(newChild);
		}
	}
	
	public double Rollout(int state)
	{
		double totalReward = 0;
		double discount = 1;
		double done = 0;
		if (verbose >= 3) {
			System.out.println("starting rollout");
		}
		int d = 0;
		while (done == 0) {
			if(discount < e) {
				break;
			}
			List <Object> availableActions = getLegalActions(state);
			if (availableActions.size() <= 0) {
				return 0;
			}
			Random rnd = new Random();
			Object randomAction = availableActions.get(rnd.nextInt(availableActions.size()));
			ArrayList<Double> sord = step(state, randomAction);
			int nextState = sord.get(0).intValue();
			double reward = sord.get(2);

			if (verbose >= 4) {
				System.out.println("V"+ verbose + "rollout action=" + randomAction + " reward=" + reward + " discountR=" + reward*discount + " depth=" + d + " totalR=" + totalReward);
				//displayState(nextState);
			}
			
			totalReward += reward * discount;
			discount *= gamma;
			d++;
			done = sord.get(3);
			state = nextState;
		}
		if (verbose >= 3) {
			System.out.println("Ending rollout after " + d + "steps, with total reward" + totalReward );
		}
		return totalReward;
	}

	public double rollout(int state, int d) {
		if (Math.pow(gamma, d) < e) {
			return 0;
		}
//		List <Object> availableActions = pomdp.getAvailableActions(state);
//		////only legal direction
//		for (int a = availableActions.size() -1 ; a >= 0; a--) {
//			if (step(state,  availableActions.get(a)).get(2) < -20 ) {
//				availableActions.remove(a);
//			}
//		}
//		if (availableActions.size() <= 0) {
//			return 0;
//		}
		List <Object> availableActions = getLegalActions(state);
		Random rnd = new Random();
		
		Object randomAction = availableActions.get(rnd.nextInt(availableActions.size()));
		ArrayList<Double> sord = step(state, randomAction);
		int nextState = sord.get(0).intValue();
		double reward = sord.get(2);
		double done = sord.get(3);
		
		if(verbose >=3) {
			System.out.println("Rollout action = " + randomAction + " Reward = "+ reward);
			displayState(nextState);
		}
		if (done == 1) {
			return reward;
		}
		
		return reward + gamma * rollout(nextState, d + 1);
	}
	
	public List<Object> getLegalActions(int state)
	{
		List <Object> availableActions = pomdp.getAvailableActions(state);
		for (int a = availableActions.size() -1 ; a >= 0; a--) {
			if (step(state,  availableActions.get(a)).get(2) < -30 ) {
//				System.out.println("========\nstate " + state + "action" + availableActions.get(a));
//				displayState(state);
				
				availableActions.remove(a);
			}
		}
		return availableActions;
	}
	
	public int GreedyUCB(POMCPNode vnode, boolean ucb) 
	{
		ArrayList<Integer> besta = new ArrayList<Integer> ();
		double bestq = Double.NEGATIVE_INFINITY;
		
		ArrayList<POMCPNode> children = vnode.getChildren();

		for (int i = 0; i < children.size(); i++) {
			POMCPNode child = children.get(i);
			
			int actionIndex = child.getH();
			Object action = allActions.get(actionIndex);
			
			if ( !ucb  && shieldLevel ==1  && isActionShielded(vnode, action) ) { // shiled only apply to the most up level
				continue;
			}
			
			if (child.getN() == 0) {
				return child.getH();
			}
			double child_UCT_V = child.getV() / child.getN();
			if(ucb) {
				child_UCT_V += fastUCB((int) vnode.getN(), (int) child.getN());
			}
			if (child_UCT_V >= bestq) {
				if (child_UCT_V > bestq) {
					besta.clear();
				}
				bestq = child_UCT_V;
				besta.add(child.getH());				
			}
		}
		
		Random rnd = new Random();
		int actionIndex = besta.get(rnd.nextInt(besta.size()));
		return actionIndex;
	}
	public Object search( ) 
	{
		if(verbose >=3) {
			System.out.println("\nStart search...");
		}
		
		int state = 0;
		int n = 0;
		long startTime = System.currentTimeMillis();
		
		if(root.getChildren() == null) {
			//state = initialBeliefParticles.sample();
			expand(root);
		}
		
		while (n < numSimulations){
			if(verbose >=3) {
				System.out.println("Search num "+n);
			}
			double elapsed = (System.currentTimeMillis() - startTime) * 0.001;
			if (elapsed > timeout) {
				break;
			}
			n += 1;
			state = root.getBelief().sample();
			if (verbose >= 2) {
				System.out.println("==Starting simulation==");
				displayState(state);
			}
			
			//simulate(state, history, 0);
			double totalReward = simulate(state, root, 0);
			
			if (verbose >= 3) {
				System.out.println("Total reward = " + totalReward);
			}
			if (verbose >= 4) {
				displayValue(4);
			}
		}
		//displayValue(14);
		return greedyAction(root, state);		
//		POMCPNode bestNode = uctActionGetAction(root, state);
//		int actionIndex = bestNode.getH();
//		return allActions.get(actionIndex);
		
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
	
	public double simulate(int state, POMCPNode node, int d)
	{
		double simR = 0;
		if ((Math.pow(gamma, d) < e) && d != 0)  {
			return 0;
		}
		if (node.getChildren() == null) {
			expand(node);
			if(verbose >= 3) {
				System.out.println("...Start rollout...");
			}
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
		
		
		if (verbose >= 3) {
			System.out.println("simulateaction = " + action + " reward = "+ reward);
			displayState(nextState);
		}
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
		aNode.increaseV(val);
		
		return simR;
	}
	
	public POMCPNode uctActionGetAction(POMCPNode node, int state) 
	{
		ArrayList<POMCPNode> children = node.getChildren();

		if (node.getN() == 0) {
			Random rnd = new Random();
			return children.get(rnd.nextInt(children.size()));
		}
		double logN = Math.log(node.getN() + 1);
		double maxV = Double.NEGATIVE_INFINITY;
		ArrayList<POMCPNode> maxChildCandidates = new ArrayList<POMCPNode> ();
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
			if (child_UCT_V >= maxV) {
				if (child_UCT_V > maxV) {
					maxChildCandidates.clear();
				}
				maxV = child_UCT_V;
				maxChildCandidates.add(child);				
			}
		}
		Random rnd = new Random();
		POMCPNode maxChild = maxChildCandidates.get(rnd.nextInt(maxChildCandidates.size()));
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
	
	
	public void display()
	{
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
	public void displayRootBelief() 
	{
		int numStates = pomdp.getNumStates();
		double [] beliefDist = root.getBelief().getDist(numStates);
		System.out.println("Belief = ");
		for (int s =0; s < numStates; s++) {
			if (beliefDist[s] > 0) {
				displayState(s);
				System.out.println( "["+ beliefDist[s] + "], ");
			}
		}
		System.out.println("");
	}
	
	public void displayRoot()
	{
		System.out.println("Root" + root.getH());
		root.getBelief().displayUniqueStates();
		System.out.println("__________");
	}
	public void displayVar() 
	{
		String variables = "";
		for (int i =0; i < pomdp.getVarList().getNumVars(); i++) {
			variables += pomdp.getVarList().getName(i) + ",";
		}
		System.out.println(variables);
	}
	public List<String> getVarNames()
	{
		List<String> varNames = new ArrayList<String> ();
		for (int i =0; i < pomdp.getVarList().getNumVars(); i++) {
			varNames.add(pomdp.getVarList().getName(i));
		}
		return varNames;
	}
	public String getStateMeaning(int state) 
	{
		List<String> varNames = getVarNames();
		return pomdp.getStatesList().get(state).toString(varNames);
	}
	public String getStateMeaningAbstract(int state) 
	{
		List<String> varNames = getVarNames();
		String originalMeaning = pomdp.getStatesList().get(state).toString(varNames);
		int ax = Integer.valueOf(getValueFromLine(originalMeaning, "ax=" ).replaceAll("[^0-9.]",""));
		int ay = Integer.valueOf(getValueFromLine(originalMeaning, "ay=" ).replaceAll("[^0-9.]",""));
		originalMeaning = originalMeaning.replace("ax="+ax,  "ax="+(ax/2));
		originalMeaning = originalMeaning.replace("ay="+ay,  "ay="+(ay/2));
		return originalMeaning;
	}
	public void displayState(int state) 
	{
		List<String> varNames = getVarNames();
		System.out.println("s=" + state + pomdp.getStatesList().get(state).toString( varNames));
	}
	public void displayValue(int depth)
	{
		Queue<POMCPNode> queue = new LinkedList<POMCPNode>();
		queue.offer(root);
		int d = 0;
		while(!queue.isEmpty()) {
			if (d >= depth) {
				System.out.println("reach tree print depth "+ depth);
				break;
			}
			d++;
			int size = queue.size();
			if ( d %2 == 0) {
				System.out.println("MCTS layer"+d);
			}
			for (int i =0; i < size; i++) {
				POMCPNode node = queue.poll();
				if (!node.isONode()) {
					displayNode(node);
					System.out.println("");
				}
				ArrayList<POMCPNode> children = node.getChildren();
				if (children != null) {
					for(POMCPNode child : children) {
						if (child.getN() > 0) {
							queue.offer(child);
						}
					}
				}
			}
		}
	}
	public void displayNode(POMCPNode node)
	{
		String info = "";
		if (node.isONode()){
			info +="Id=" + node.getID() + " o=" + node.getH() + " vmean=" + (node.getV()/node.getN()) + " v=" + node.getV() + " n=" + node.getN();
		}
		else {
			info +="Id=" + node.getID() + " a=" + node.getHAction() + " vmean=" + (node.getV()/node.getN()) + " v=" + node.getV() + " n=" + node.getN() ;
		}
		POMCPNode parent = node.getParent();
		if (parent == null) {
			System.out.print(info);
		}
		else {
			System.out.print(info + " | ");			
			displayNode(parent);
		}
	}
	
	public void importWinningRegion() 
	{

		//reading translate
		//String translateFrile = "E:\\Downloads\\prism3\\prism812\\prism\\prism\\tests\\Shield\\ShiledingForPOMDP\\Dropbox" + "\\translate.txt";
		String translateFrile = "E:\\Downloads\\prism3\\prism812\\prism\\prism\\tests\\Shield\\ShiledingForPOMDP" + "\\translate.txt";
		loadTranlateFromFile(translateFrile);

		// reading winning region
		// prismObs -> belief support (represented in big integer, which can be converted in binary, and corrspoding to PrsimObsToPrismStates)
		//String winngingFile = "E:\\Downloads\\prism3\\prism812\\prism\\prism\\tests\\Shield\\ShiledingForPOMDP\\Dropbox\\winningregion" + "\\obstacle-2-fixpoint.wr";
		String winngingFile = "E:\\Downloads\\prism3\\prism812\\prism\\prism\\tests\\Shield\\ShiledingForPOMDP" + "\\rocks-6-initial.wr";
		loadWinningRegionFromFile(winngingFile);
	}
	
	public void loadTranlateFromFile(String translateFrile) 
	{
		ArrayList<Integer> StompyStateToObs = new ArrayList<Integer> ();
		HashMap<String, Integer> StompyMeaning2State = new HashMap<String, Integer>();
		// get StompyStateToObs and StompyMeaningToState
		try {
			List<String> varNames = getVarNames();
			BufferedReader in = new BufferedReader(new FileReader(translateFrile));
			String str;
			while((str = in.readLine()) != null) {
				str = str.replace("\"", "").replace(":", "").replace("}", ",");
				int state = Integer.parseInt(getValueFromLine(str, "state="));
				int obs= Integer.parseInt(getValueFromLine(str, "obs="));
				StompyStateToObs.add(obs);
				String meaning = "";
				for (String varName : varNames) {
					String value = getValueFromLine(str, varName);
					meaning += varName + "=" + value + ",";	
				} 
				meaning = "(" + meaning.substring(0, meaning.length()-1) + ")";
				System.out.println("DDDDDDDDDDDDDD"+ meaning);
				StompyMeaning2State.put(meaning, state);
			}
			in.close();
			translate(StompyStateToObs,  StompyMeaning2State);
		} catch(IOException e) {
		}
	}
	
	public String getValueFromLine(String str, String feature) 
	{
		String value = "";
		int startIndex = str.indexOf(feature) + feature.length();
		int endIndex = startIndex + 1;
		while ((endIndex < str.length()) &&  (str.charAt(endIndex) != ',' || str.charAt(endIndex) != ',')   ) {
			endIndex++;
		}
		value = str.substring(startIndex, endIndex);
		return value;
	}

	
	public void translate( ArrayList<Integer> StompyStateToObs, HashMap<String, Integer> StompyMeaning2State) 
	{
		int nStompyStates = StompyStateToObs.size();
		int nPrismStates= pomdp.getNumStates();
		//		int StompyState = PrismStateToStompyState[sPrime];
		//		int StompyObs = StompyStateToStompyObs[StompyState];
		PrismStateToStompyState = new int[pomdp.getNumStates()];
		HashMap<Integer, ArrayList<Integer>> StompyStateToPrismState = new HashMap<Integer, ArrayList<Integer>>();
		for (int s = 0; s < nStompyStates; s++) {
			StompyStateToPrismState.put(s, new ArrayList<Integer>());
		}
		
		for (int PrismState = 0; PrismState < pomdp.getNumStates(); PrismState++) {
			String meaning = "";
			if (nStompyStates == nPrismStates) {
				meaning = getStateMeaning(PrismState);
			} else {
				meaning = getStateMeaningAbstract(PrismState);
				System.out.println(getStateMeaning(PrismState)+ " to abstact  => "+meaning);
			}
			
			int StompyState = StompyMeaning2State.get(meaning);
			PrismStateToStompyState[PrismState] =  StompyState; //key
			
			ArrayList<Integer> tp = StompyStateToPrismState.get(StompyState);
			tp.add(PrismState);
			StompyStateToPrismState.put(StompyState, tp);
			
			PrismObsToStompyObs[pomdp.getObservation(PrismState)] = StompyStateToObs.get(StompyState);// StompyStateToObs[StompyState];
			System.out.println(pomdp.getObservation(PrismState) + "xxx" + StompyStateToObs.get(StompyState));
//			StompyObsToPrismObs[StompyStateToObs.get(StompyState)] = pomdp.getObservation(PrismState);
		}			
		
//		for (int s = 0; s < nPrismStates; s++) {
//			System.out.println("Prism State " + s + getStateMeaning(s) + " Prism Obs " + pomdp.getObservation(s) + 
//					" Stompy State " + PrismStateToStompyState[s] 
//							+ " Stompy Obs "+ PrismObsToStompyObs[pomdp.getObservation(s)]  +"alt " + StompyStateToObs.get(PrismStateToStompyState[s]) 
//							+ getStateMeaningAbstract(s));
//		}
//		
		
		for (int i = 0; i < pomdp.getNumObservations(); i++) {
			ArrayList<Integer> StompyStatesPerObservation = new ArrayList<Integer> (); // dummy
			StompyObsToStompyStates.add(StompyStatesPerObservation); // dummy
//
//			ArrayList<Integer> PrismStatesPerObservation = new ArrayList<Integer> ();
//			PrismObsToPrismStates.add(PrismStatesPerObservation);
		}
		
		for (int StompyState = 0; StompyState < nStompyStates; StompyState++) {
			int StompyObs = StompyStateToObs.get(StompyState); // dummy
			StompyObsToStompyStates.get(StompyObs).add(StompyState); // dummy

//			if (nStompyStates == nPrismStates) {
//				int PrismState = StompyStateToPrismState.get(StompyState).get(0);
//				int PrismObs = pomdp.getObservation(PrismState);
//				PrismObsToPrismStates.get(PrismObs).add(PrismState);
//			}else {
////				asddddddd
//			}
		}
	}
	public void loadWinningRegionFromFile(String winngingFile) 
	{
		try {
			BufferedReader in = new BufferedReader(new FileReader(winngingFile));
			String str;
			long[] observationSizes = new long[pomdp.getNumObservations()];
			long state = 0;
			int observation = 0;
			String preamblestream;
			//winningregion
			
			while((str = in.readLine()) != null) {
				if(str.length()>0 && str.charAt(0)=='#') {
					continue;
				}
				if(state == 0) {
					//reading preamble
					state = 1;
				} else if (state == 1) {
					if (str.indexOf(":winningregion") >= 0) {
						state = 2;
					}else {
						preamblestream = str;
					}
				} else if (state == 2) {
					String[] entries = str.split(" ");
					for (int ob = 0; ob < entries.length; ob++) {
						observationSizes[ob] = Long.parseLong(entries[ob]);
					}
					// wr = winningRegion(observationSizes);
					state = 3;
				} else if (state == 3) { //eg. 84 1154891893868338944 139664365006618624;84 1442277793550311168 139664365006618624;
					if (str.length() == 0) {
						++observation;
						continue;
					}
					String[] entries = str.split(";");
					for (int i = 0; i < entries.length; i++) {
						String[] subEntries = entries[i].split(" "); //eg. 84 1154891893868338944 139664365006618624;
						ArrayList<BigInteger> bv = new ArrayList<BigInteger> ();
						long obsSize = Long.parseLong(subEntries[0]); // eg. 84
						for (int j = 1; j < subEntries.length; j++) {
							BigInteger beliefSupport = new BigInteger(subEntries[j]);
							//String beliefSupport = subEntries[j]; //eg. 1154891893868338944
							bv.add(beliefSupport);
						}
						updateWinningRegion(winningRegion, observation, bv);
						//updateWinningRegion(winningRegion,  StompyObsToPrismObs[observation], bv);
					}
					++observation;
				}
			}
			in.close();
		} catch(IOException e) {
		}
	}
	public void displayWinningRegion() {
		System.out.println("output winningregion");
		for (int observation = 0; observation < pomdp.getNumObservations(); observation++) {
			ArrayList<ArrayList<BigInteger>> existingWinningSupports = winningRegion.get(observation);
			if (existingWinningSupports == null) {
				System.out.println("Obs = " + observation + " NULL");
			} else {
				System.out.println("Obs = " + observation +" StompyObs = " + PrismObsToStompyObs[observation]);
				for (ArrayList<BigInteger> support: existingWinningSupports) {
					for (BigInteger tp: support) {
						System.out.print(tp + " ");
					}
					System.out.print(";");
				}
				System.out.println("");
			}
		}
	}
	public boolean updateWinningRegion(Map<Integer, ArrayList<ArrayList<BigInteger>>> winningRegion, int observation, ArrayList<BigInteger> winning) 
	{
		ArrayList<ArrayList<BigInteger>> existingWinningSupports = winningRegion.get(observation);
		if (existingWinningSupports == null) {
			existingWinningSupports = new ArrayList<ArrayList<BigInteger>>();
			existingWinningSupports.add(winning);
			winningRegion.put(observation, existingWinningSupports);
			return true;
		}

		ArrayList<ArrayList<BigInteger>> newWinningSupports = new ArrayList<ArrayList<BigInteger>>();
		boolean changed = false;
		for (ArrayList<BigInteger> support : existingWinningSupports) {
			if (isFirstBeliefSupportSubsetOfSecond(winning, support) ){ 
				// This new winning support is already covered.
				return false;
			}
			if (isFirstBeliefSupportSubsetOfSecond(support, winning)) {
				// This new winning support extends the previouse support, thus the previous is now spurious
				changed = true;
			} else {
				newWinningSupports.add(support);
			}
		}
		//only if changed
		if (changed) {
			newWinningSupports.add(winning);
			winningRegion.put(observation, newWinningSupports);
		} else {
			existingWinningSupports.add(winning);
			winningRegion.put(observation, existingWinningSupports);
		}
		return true;
	}
	
	public boolean isActionShielded(POMCPNode node, Object action)
	{
		HashSet<Integer> currentUniqueStates =  node.getBelief().getUniqueStatesInt();
		if (verbose >= 5) {
			System.out.println("\ncurrentUniqueStates" + currentUniqueStates + "considering action " + action);
		}
		//With this action, get all possible observations, and belief support per observation
		HashMap<Integer, HashSet<Integer>> nextObsToUniqueStateIndices= new HashMap<Integer, HashSet<Integer>> ();
		for (int s : currentUniqueStates) {
			int choice = pomdp.getChoiceByAction(s, action);
			Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(s,choice);
			while (iter.hasNext()) {
				Map.Entry<Integer, Double> trans = iter.next();
				int sPrime = trans.getKey();
				int PrismObs =  pomdp.getObservation(sPrime);
				
				if (winningRegion.get(PrismObsToStompyObs[PrismObs]) == null) { // if not winning support for this obs
					if (endStates.contains(sPrime)) {
//						System.out.println("Safe Action " + action);
						return false;
					} else {
						return true;
					}	
				}
				if(nextObsToUniqueStateIndices.get(PrismObs)  == null) {
					nextObsToUniqueStateIndices.put(PrismObs, new HashSet<Integer> ());
				}
				HashSet<Integer> tpp =  nextObsToUniqueStateIndices.get(PrismObs);
				
				int StompyState = PrismStateToStompyState[sPrime];
				int StompyObs = PrismObsToStompyObs[PrismObs];
				int index = StompyObsToStompyStates.get(StompyObs).indexOf(StompyState);
				tpp.add(index);
				
//				tpp.add(PrismObsToPrismStates.get(PrismObs).indexOf(sPrime));	
				nextObsToUniqueStateIndices.put(PrismObs, tpp);
			}
		}
		
		// if suppoorts for all obseravtions are safe, this action should not be shield 
		boolean isAllObsWinning = true;
		for (int PrismObs : nextObsToUniqueStateIndices.keySet()) {
			//code up support from states			
			ArrayList<BigInteger> nextSupport = getBigIntegerFromStateIndices(nextObsToUniqueStateIndices.get(PrismObs),  PrismObs); 
			
//			if (verbose >= 5) {	
				System.out.println("Prism Obs " + PrismObs + " Stompy Obs" + PrismObsToStompyObs[PrismObs] + " winning region" + winningRegion.get(PrismObsToStompyObs[PrismObs]).get(0));
//			}
			
			// test if this support is safe
			if (!isSupportWinning(nextSupport, PrismObs)) {
				isAllObsWinning = false;
				break;
			}
		}
		//if (verbose >= 5) {
			if (isAllObsWinning) {
				System.out.println("Safe Action " + action);
			} else {
				System.out.println("Shiedled " + action);
			}
		//}
		return !isAllObsWinning;	
	}
	
	public ArrayList<BigInteger> getBigIntegerFromStateIndices( HashSet<Integer> stateIndices, int PrismObs)
	{
		ArrayList<BigInteger> nextSupport = new ArrayList<BigInteger> ();
		ArrayList<ArrayList<BigInteger>> beliefSupports = winningRegion.get(PrismObsToStompyObs[PrismObs]);
		int nBigInteger = beliefSupports.get(0).size();
		for (int i  = 0; i < nBigInteger; i++) {
			nextSupport.add(new BigInteger("0"));
		}
		for (int index : stateIndices) {
			int bucket = (int) (index / 64);
			int indexInBucket = 63 - (index - 64 * bucket) ;
			BigInteger base = new BigInteger("2");
			BigInteger tp = nextSupport.get(bucket).add(base.pow(indexInBucket));
			nextSupport.set(bucket, tp);
		}
		return nextSupport;
	}
	public boolean isSupportWinning(ArrayList<BigInteger> nextSupport, int PrismObs) 
	{
		boolean isThisObsWinning = false;
		ArrayList<ArrayList<BigInteger>> beliefSupports = winningRegion.get(PrismObsToStompyObs[PrismObs]);
		for (ArrayList<BigInteger> winning : beliefSupports) {
			if (verbose >= 5) {
				System.out.println("next Support " + nextSupport +   " winning" + winning + " < " + isFirstBeliefSupportSubsetOfSecond(nextSupport, winning));
			}
			if (isFirstBeliefSupportSubsetOfSecond(nextSupport, winning)) {
				isThisObsWinning = true;
				break;
			}
		}
		return isThisObsWinning;
	}
	
	public boolean isFirstBeliefSupportSubsetOfSecond(ArrayList<BigInteger> first, ArrayList<BigInteger> second) 
	{
		for (int i = 0; i < first.size(); i++) {
			BigInteger firstAndSecond = first.get(i).and(second.get(i));
			if (!firstAndSecond.equals(first.get(i))) {
				return false;
			}
		}
		return true;
	}
	

	
}