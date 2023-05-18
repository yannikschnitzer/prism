package explicit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.LinkedList;
import explicit.rewards.MDPRewards;
import explicit.PartiallyObservableMultiStrategy;
import parser.type.TypeDouble;
import prism.Point;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismNotSupportedException;
import prism.PrismSettings;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;


public class MultiObjModelChecker extends PrismComponent
{
	protected ProbModelChecker mc;
	
	/**
	 * Create a new MultiObjModelChecker, inherit basic state from parent (unless null).
	 */
	public MultiObjModelChecker(ProbModelChecker mc)
	{
		super(mc);
		this.mc = mc;
	}
//	public void buildBeliefTree(POMDP pomdp) {
//		
//		Belief init = pomdp.getInitialBelief();
//		int bid = 0;
//		int oid = 0;
//		BeliefNode root = new BeliefNode(pomdp.getInitialBeliefInDist(), init.so, 1, null, bid);
//		int layer = 0;
//		Queue<BeliefNode> queue = new LinkedList<BeliefNode>();
//		queue.offer(root);
//		while (layer++ < 10) {
//			BeliefNode bnode = queue.poll();
//			double[] beliefDist = bnode.getBelief();
////			int obs = node.getObservation();
//			int state = 0;
//			for (Object action : pomdp.getAvailableActions(state)) {
//				int choice = pomdp.getChoiceByAction(state, action);
//				int actionIndex = actionToIndex.get(action);
//				ObsNode oNode = new ObsNode(actionIndex, bnode, oid++);
//				bnode.addChild(actionIndex, oNode);
//				
//				HashMap<Integer, Double> obsToProb = pomdp.computeObservationProbsAfterAction(beliefDist, choice);
//				for (int obs: obsToProb.keySet()) {
//					double[] nxtBelief = pomdp.getBeliefInDistAfterChoiceAndObservation(beliefDist, choice, obs);
//					BeliefNode nxt = new BeliefNode(nxtBelief, obs, obsToProb.get(obs), oNode, bid++);
//					oNode.addChild(obs, nxt);
//					queue.offer(nxt);
//				}
//			}
//		}
//	}
	
    /* Multi-objective MDP solver
     * This algorithm is based on "Linear Support for Multi-Objective Coordination Graphs" by Roijers et al.
	 * The part of code for computing new weights in OLS algorithm is modified based the following git hub repo
	 * https://github.com/Svalorzen/morl_guts/blob/master/gp_preference/pymodem/optimistic_linear_support.py
	 * @param model the model of POMDP
     * */
    
    protected StateValues checkExpressionParetoMultiObjMDPWithOLS(Model model, List<MDPRewards<Double>> mdpRewardsList, BitSet target, List<MinMax> minMaxList, BitSet statesOfInterest) throws PrismException
    {
    	int numObjs = minMaxList.size();
    	
		// Dummy return value
    	double threshold = 0.00001;
		ArrayList<ArrayList<Double>> partial_CCS = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> partial_CCS_weights = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> w_v_checked = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> vector_checked = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> weights_checked = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> priority_queue = new ArrayList<ArrayList<Double>>();
		HashMap <ArrayList<Double>, ArrayList<ArrayList<Double> >> corner_to_value = new HashMap < ArrayList<Double>, ArrayList<ArrayList<Double>>> ();
		HashMap <ArrayList<Double>, ArrayList<ArrayList<Double> >> value_to_corner = new HashMap < ArrayList<Double>, ArrayList<ArrayList<Double>>> ();
		priority_queue =  initialQueue(minMaxList, corner_to_value, model);

		while(priority_queue.size()>0){
			mainLog.println("+++++++++++");
			mainLog.println("Current Q (weight, priority) Before Pop"+Arrays.toString(priority_queue.toArray()));
			mainLog.println("Current paritial CCS Before Pop"+Arrays.toString(partial_CCS.toArray()));
			mainLog.println("Current paritial CCS weights Before Pop"+Arrays.toString(partial_CCS_weights.toArray()));
			if(corner_to_value.keySet().size()>0){
				mainLog.println(corner_to_value.keySet().size());
				mainLog.println(corner_to_value.keySet());
				for (Object  key:corner_to_value.keySet()){
					ArrayList<ArrayList<Double>> tp = (ArrayList<ArrayList<Double>>) corner_to_value.get(key);
					mainLog.println(key);
					mainLog.println(tp.size());
					if (tp.size()>0){
						for (int i=0; i< tp.size(); i++){
							mainLog.println(key+" -> "+ Arrays.toString(((ArrayList<Double>) tp.get(i) ).toArray()));
						}
					}
				}
			}
			mainLog.println("value_to_corner");
			if(value_to_corner.keySet().size()>0){
				for (Object  key:value_to_corner.keySet()){
					ArrayList<ArrayList<Double>> tp = (ArrayList<ArrayList<Double>>) value_to_corner.get(key);
					for (int i=0; i< tp.size(); i++){
						mainLog.println(key+" -> "+ Arrays.toString(((ArrayList<Double>) tp.get(i) ).toArray()));
					}
				}
			}
			mainLog.println("+++++++++++");
			ArrayList<Double> w_pop = deQueue(priority_queue);
			mainLog.println("Pop weight with top priority: "+Arrays.toString(w_pop.toArray()));
			mainLog.println("Current Q (weight, priority)  After pop"+Arrays.toString(priority_queue.toArray()));
			
			StateValues sv = mc.checkExpressionWeightedMultiObj(model, w_pop, mdpRewardsList, target, minMaxList, statesOfInterest);
			ArrayList<Double> u = (ArrayList<Double>) sv.getValue(model.getFirstInitialState());
			mainLog.println("Value vector: "+u);
			
			int countNewWeights = 0; //number of weights generated by u

			w_v_checked.add( w_pop); //w_v_checked.add(copyArrayList(w_pop));
			weights_checked.add(w_pop);
			w_v_checked.add(u);
			vector_checked.add(u);

			mainLog.println("w_pop: "+Arrays.toString(w_pop.toArray()));
			mainLog.println("u: "+Arrays.toString(u.toArray()));
			mainLog.println("w_pop*u: "+innerProduct(w_pop,u));
			mainLog.println("w_v_checked"+Arrays.toString(w_v_checked.toArray()));
			
			if (!containsWithError(partial_CCS,u, 1E-5)) {
				computeNewWeights(partial_CCS, partial_CCS_weights, w_v_checked, vector_checked, weights_checked, priority_queue,
						w_pop, u, corner_to_value, value_to_corner, minMaxList, model, numObjs, threshold, countNewWeights);
			}else{
					mainLog.println("Vector already in the partial CCS");
			}
		}
		
		mainLog.println("ALl weights checked:"+ weights_checked.size());
		HashSet values_OLS = new HashSet();

		for (int iprint=0; iprint<weights_checked.size();iprint++){
			mainLog.print("weight: "+Arrays.toString(weights_checked.get(iprint).toArray())+"; vector: "+Arrays.toString(vector_checked.get(iprint).toArray())+"\n");
		}
		mainLog.println("*********************ParetoCurve by optimal linear solution*******************************");
		for (int iprint=0; iprint<partial_CCS.size();iprint++){
			mainLog.print("weight: "+Arrays.toString(partial_CCS_weights.get(iprint).toArray())+"; vector: "+Arrays.toString(partial_CCS.get(iprint).toArray())+"\n");
			values_OLS.add(innerProduct(partial_CCS_weights.get(iprint),partial_CCS.get(iprint)));
		}
		mainLog.print("#weights: "+partial_CCS_weights.size()+"\n");
		for (int iprint=0; iprint<partial_CCS.size();iprint++){
			mainLog.print(Arrays.toString(partial_CCS_weights.get(iprint).toArray())+"\n");
		}
		mainLog.println("#Pareto Curve points: "+partial_CCS.size()+"\n");
		for (int iprint=0; iprint<partial_CCS.size();iprint++){
			mainLog.print(Arrays.toString(partial_CCS.get(iprint).toArray())+"\n");
		}
				
		// Return Pareto curve as Point list
		List<Point> points = new ArrayList<>();
		for (ArrayList<Double> paretoPoint : partial_CCS) {
			Point point = new Point(paretoPoint.size());
			for (int dim = 0; dim < paretoPoint.size(); dim++) {
				point.setCoord(dim, paretoPoint.get(dim));
			}
			points.add(point);
		}
		Object array[] = new Object[model.getNumStates()];
		array[model.getFirstInitialState()] = points;
		return StateValues.createFromObjectArray(TypeDouble.getInstance(), array, model);
    }
    
    protected StateValues checkExpressionParetoMultiObjMDPWithRandomSampling(Model model, List<MDPRewards<Double>> mdpRewardsList, BitSet target, List<MinMax> minMaxList, BitSet statesOfInterest) throws PrismException
    {
    	int numObjs = minMaxList.size();
    	
		// Dummy return value
		HashSet<List<Double>> paretoCurve = new HashSet<>();

		// Random sampling:
		ArrayList<ArrayList<Double>> w_v_checked_rs = new ArrayList<ArrayList<Double>>();
		double rs =1;
		if (rs>0){
			if (numObjs == 3) {
				for (int i =0;i<11;i++){
					double w1 = ((double) i )*0.1;
					for (int j=0; j<11; j++){
						double w2= ((double) j )*0.1;
						if (w1+w2<=1){
							double w3= 1-w1-w2;
							ArrayList<Double> weights = new ArrayList<>();
							weights.add(w1);
							weights.add(w2);
							weights.add(w3);
							StateValues sv = mc.checkExpressionWeightedMultiObj(model, weights, mdpRewardsList, target, minMaxList, statesOfInterest);
							ArrayList<Double> point = (ArrayList<Double>) sv.getValue(model.getFirstInitialState());
							mainLog.println("weights: "+Arrays.toString(weights.toArray()));
							mainLog.println("Points: "+Arrays.toString(point.toArray()));
							paretoCurve.add(point);
							if((!containsWithError(w_v_checked_rs,point,1E-06)) || true){
								w_v_checked_rs.add(weights);
								w_v_checked_rs.add(point);
							}
							mainLog.println("\nPareto curve: " + paretoCurve);
							mainLog.println("w_v_checked: "+Arrays.toString(w_v_checked_rs.toArray()));
						}
					}
				}		
				mainLog.println("\n finishing Pareto curve: " + paretoCurve);
				mainLog.println("finishing w_v_checked: "+Arrays.toString(w_v_checked_rs.toArray()));
				//return StateValues.createFromSingleValue(TypeDouble.getInstance(), 0.0, model);
			}
			if (numObjs == 2) {		
				//HashSet<List<Double>> paretoCurve = new HashSet<>();
				int numPoints = 10;
				for (int i = 0; i <= numPoints; i++) {
					double w1 = ((double) i) / numPoints;
					double w2 = 1.0 - w1;
					ArrayList<Double> weights = new ArrayList<>();
					weights.add(w1);
					weights.add(w2);

					StateValues sv = mc.checkExpressionWeightedMultiObj(model, weights, mdpRewardsList, target, minMaxList, statesOfInterest);
					ArrayList<Double> point = (ArrayList<Double>) sv.getValue(model.getFirstInitialState());

					w_v_checked_rs.add(weights);
					w_v_checked_rs.add(point);
					paretoCurve.add(point);
					mainLog.println(w1 + ":" + w2 + " = " + point);
				}
				mainLog.println("\nPareto curve: " + paretoCurve);
			}
		}
	 	
		mainLog.println("*********************ParetoCurve by iterating [w1 w2] Random Sampling*******************************");
		
		mainLog.println("\nPareto curve: " +paretoCurve.size()+"\n"+ paretoCurve);
		ArrayList<ArrayList<Double>> paretoCurveCompact = new ArrayList<ArrayList<Double>>();
		for (int iprint=0; iprint<paretoCurve.size(); iprint++){
			if (!containsWithError(paretoCurveCompact, (ArrayList<Double>) paretoCurve.toArray()[iprint],1E-06)){
				paretoCurveCompact.add((ArrayList<Double>)paretoCurve.toArray()[iprint]);
			}
		}
		mainLog.println("\nPareto curve (compact)" + paretoCurveCompact.size());

		for (int iprint=0; iprint<paretoCurve.size(); iprint++){
				mainLog.println(paretoCurve.toArray()[iprint]);
		}


		HashSet values_rs = new HashSet();
		mainLog.println("Weights and vectors checked by random sampling start: ");
		for (int iprint=0; iprint<w_v_checked_rs.size()/2;iprint++){
			mainLog.print("weight: "+Arrays.toString(w_v_checked_rs.get(iprint*2).toArray())+"; vector: "+Arrays.toString(w_v_checked_rs.get(iprint*2+1).toArray())+"\n");
			values_rs.add(innerProduct(w_v_checked_rs.get(iprint*2),w_v_checked_rs.get(iprint*2+1)));
		}
		mainLog.println("Weights and vectors checked by random sampling end: ");
		
		// Return Pareto curve as Point list
		List<Point> points = new ArrayList<>();
		for (ArrayList<Double> paretoPoint : paretoCurveCompact) {
			Point point = new Point(paretoPoint.size());
			for (int dim = 0; dim < paretoPoint.size(); dim++) {
				point.setCoord(dim, paretoPoint.get(dim));
			}
			points.add(point);
		}
		Object array[] = new Object[model.getNumStates()];
		array[model.getFirstInitialState()] = points;
		return StateValues.createFromObjectArray(TypeDouble.getInstance(), array, model);
    }
    
    protected StateValues checkExpressionParetoMultiObjPOMDP(POMDP<Double> pomdp, List<MDPRewards<Double>> mdpRewardsList, BitSet target, List<MinMax> minMaxList, BitSet statesOfInterest) throws PrismException
    {
    	//mainLog.println("Random sampling***********");
   	//checkExpressionParetoMultiObjMDPWithRandomSampling( pomdp,  mdpRewardsList, target, minMaxList, statesOfInterest);
    	//mainLog.println("Random sampling***********");
    	long startTime = System.currentTimeMillis();

    	int numObjs = minMaxList.size();
		int nStates = pomdp.getNumStates();
		
		BitSet targetObs = ((POMDPModelChecker) mc).getObservationsMatchingStates(pomdp, target);
		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}	
		
		if (targetObs == null) {
			throw new PrismException("Target for expected reachability is not observable");
		}
		// Find _some_ of the states with infinite reward
		// (those from which *every* MDP strategy has prob<1 of reaching the target,
		// and therefore so does every POMDP strategy)
		MDPModelChecker mcProb1 = new MDPModelChecker(this);
		BitSet inf = mcProb1.prob1(pomdp, null, target, false, null);
		inf.flip(0, pomdp.getNumStates());
		// Find observations for which all states are known to have inf reward
		BitSet infObs = ((POMDPModelChecker) mc).getObservationsCoveredByStates(pomdp, inf);
		//mainLog.println("target obs=" + targetObs.cardinality() + ", inf obs=" + infObs.cardinality());
		
		// Determine set of observations actually need to perform computation for
		// eg. if obs=1 & unknownObs(obs)=true -> obs=1 needs computation
		// eg. if obs=2 & unknownObs(obs)=false -> obs=1 does not need computation
		BitSet unknownObs = new BitSet();
		unknownObs.set(0, pomdp.getNumObservations());
		unknownObs.andNot(targetObs);
		unknownObs.andNot(infObs);
		ArrayList<Integer> endStates = new ArrayList<Integer>();
		for (int i=0; i<nStates;i++) {
			if (!unknownObs.get(pomdp.getObservation(i))) {
//				mainLog.println("end state="+i+"Obs="+pomdp.getObservation(i));
				endStates.add(i);
			}
		}
		
		int endState = -1;
		for (int i=0; i<nStates;i++) {
			if (!unknownObs.get(pomdp.getObservation(i))) {
				endState=i;
				break;
			}
		}
		
		ArrayList<Object> allActions = ((POMDPModelChecker) mc).getAllActions(pomdp);
		int nActions = allActions.size();
		HashMap<Object, Integer> actionToIndex = new HashMap<Object, Integer>();
		for (int a = 0; a < nActions; a++) {
//			mainLog.println("action index = " + a + "action " + allActions.get(a));
			actionToIndex.put(allActions.get(a), a);
		}
		int numRewards = mdpRewardsList.size();
		
    	HashSet<List<Double>> paretoCurve = new HashSet<>();
		ArrayList<ArrayList<Double>> partial_CCS = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> partial_CCS_weights = new ArrayList<ArrayList<Double>>();

		//Line 2
		ArrayList<ArrayList<Double>> w_v_checked = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> vector_checked = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> weights_checked = new ArrayList<ArrayList<Double>>();
		
		HashMap <ArrayList<Double>, ArrayList<ArrayList<Double> >> corner_to_value = new HashMap < ArrayList<Double>, ArrayList<ArrayList<Double>>> ();
		HashMap <ArrayList<Double>, ArrayList<ArrayList<Double> >> value_to_corner = new HashMap < ArrayList<Double>, ArrayList<ArrayList<Double>>> ();
		
		//Line 3 
		ArrayList<ArrayList<Double>> priority_queue = new ArrayList<ArrayList<Double>>();
		//Line 4
		priority_queue= initialQueue(minMaxList, corner_to_value, pomdp);
		
		//line 5
		ArrayList<AlphaMatrix> A_all = new ArrayList<AlphaMatrix> ();
		ArrayList<AlphaMatrix> immediateRewards = new ArrayList<AlphaMatrix>();
		ArrayList<AlphaMatrix> V= new ArrayList<AlphaMatrix>();
		
		//immediate reward vector
		ArrayList<Double> Rmin = new ArrayList<Double>();
		for(int obj=0; obj<numRewards; obj++) {
			Rmin.add(Double.POSITIVE_INFINITY);
		}
		for (int a=0; a<nActions; a++) {
			Object action = allActions.get(a);
			for (int s=0; s<nStates; s++) {
				for(int obj=0; obj<numRewards; obj++) {
					if (pomdp.getAvailableActions(s).contains(action)) {
						int choice = pomdp.getChoiceByAction(s, action);
						double immediateReward =  mdpRewardsList.get(obj).getTransitionReward(s,choice) + mdpRewardsList.get(obj).getStateReward(s);
						immediateReward *= minMaxList.get(obj).isMin() ? -1 :1;
						if (immediateReward < Rmin.get(obj)) {
							Rmin.set(obj, immediateReward);
						}
					}
				}
			}
		}

		for (int a =0; a<nActions; a++) {
			double [][]matrix = new double [nStates][numRewards];
			Object action = allActions.get(a);
			for (int s=0; s<nStates; s++) {
				for (int obj=0; obj<numRewards; obj++) {
					if (pomdp.getAvailableActions(s).contains(action)) {
						int choice = pomdp.getChoiceByAction(s, action);
						double immediateReward =  mdpRewardsList.get(obj).getTransitionReward(s,choice) + mdpRewardsList.get(obj).getStateReward(s);
						immediateReward *= minMaxList.get(obj).isMin() ? -1 :1;
						matrix[s][obj] =  immediateReward;
					}
					else {
						matrix[s][obj] = Rmin.get(obj)*10;
					}
					if (endStates.contains(s)) {
						matrix[s][obj] = 0 ;
					}
				}
			}
			AlphaMatrix am = new AlphaMatrix(matrix);
			am.setAction(a);
			immediateRewards.add(am);
			//V.add(am);
			//A_all.add(am);
		}
		
		//
		
		//double[] we = {1, 0};
		//((POMDPModelChecker) mc).computeReachRewardsRTBSS(immediateRewards, we, pomdp,  target,  statesOfInterest);
		//
		
		
		// initial vector 
		double [][]matrixInit = new double [nStates][numRewards];
		for (int s=0; s<nStates; s++) {
			for (int obj=0; obj<numRewards; obj++) {
				matrixInit [s][obj] = Rmin.get(obj)*10*1000;
			}
		}
		AlphaMatrix amInit  = new AlphaMatrix(matrixInit);
		amInit.setAction(0);
		V.add(amInit);
		A_all.add(amInit);
		
		
		immediateRewards =((POMDPModelChecker) mc).copyAlphaMatrixSet(immediateRewards);
		A_all =((POMDPModelChecker) mc).copyAlphaMatrixSet(A_all);
		/*for (int a=0; a<allActions.size();a++) { 
			double [][] matrix = new double [nStates][numObjs];
			double Rmin =0.1;
			for (int i=0; i<nStates; i++) {
				for (int j=0; j<numObjs; j++) {
					matrix[i][j] = Rmin;
				}
			}
			AlphaMatrix am = new AlphaMatrix(matrix); //alphaMatrix
			am.setAction(a);
			A_all.add(am);
		}
		*/
		//check
//		mainLog.println("immediate reward");
////		for (int i=0; i<immediateRewards.size(); i++){
//			AlphaMatrix am= immediateRewards.get(i);
//			mainLog.println(allActions.get(am.getAction()));
//			mainLog.println(am);
//		}
		
//		mainLog.println("initial vector");
//		for (int i=0; i<A_all.size(); i++){
//			AlphaMatrix am= A_all.get(i);
//			mainLog.println(am);
//		}
		
		ArrayList<Belief> belief_set = ((POMDPModelChecker) mc).randomExploreBeliefs(pomdp, target, statesOfInterest);
		mainLog.println("Belief set size= "+belief_set.size());
		
		//store weights and alpha matrices
		ArrayList<ArrayList<AlphaMatrix>> alphaMatricesChecked = new ArrayList<ArrayList<AlphaMatrix>> ();
		
		
		while(priority_queue.size()>0){

			mainLog.println("Current Q (weight, priority) Before Pop"+Arrays.toString(priority_queue.toArray()));
			ArrayList<Double> w_pop = deQueue(priority_queue);
			double[] w_pop_array = new double [w_pop.size()];
			for (int w=0; w<w_pop.size(); w++) {
				w_pop_array[w] = (double) w_pop.get(w);
			}
			mainLog.println("Current weight "+Arrays.toString(w_pop_array));
			
			mainLog.println("Current Q (weight, priority) After Pop"+Arrays.toString(priority_queue.toArray()));
			
			//Line 9  Select the best A from A_all for each b \belong belief, give w
			ArrayList<AlphaMatrix> Ar =  new ArrayList<AlphaMatrix> ();
			for (int i=0; i<belief_set.size();i++){
				Belief belief_candidate =  belief_set.get(i);
				int maxValueIndex = AlphaMatrix.getMaxValueIndex(belief_candidate, A_all, w_pop_array, pomdp);
				AlphaMatrix A_candidate = A_all.get(maxValueIndex);
				if(!Ar.contains(A_candidate)) {
				Ar.add(A_candidate);
				}
			}
			mainLog.println("Ar size= "+Ar.size());

	    	double threshold = 0.00001; // threshold of allowable error to determine if two weights are the same
	    	
			//line 10 
			double eta = 1E-5;

			ArrayList<AlphaMatrix> Aw = ((POMDPModelChecker) mc).computeMultiReachRewardPerseus(Ar, belief_set, w_pop_array, eta, pomdp, immediateRewards, endState,startTime);
			
//			mainLog.println("HERED============");
//			mainLog.println("weight" + w_pop.toString());
//			for (AlphaMatrix a: Aw) {
//				mainLog.println(a);
//			}
//			mainLog.println("============");
			//Line 11
			Belief b0=pomdp.getInitialBelief();
			int bestAlphaMatrixIndex = AlphaMatrix.getMaxValueIndex(b0, Aw, w_pop_array, pomdp);
			AlphaMatrix bestAlphaMatrix = Aw.get(bestAlphaMatrixIndex);
			mainLog.println("Best matrix="+bestAlphaMatrix);
			double [][] matrix = bestAlphaMatrix.getMatrix();
			double [] belief0 = b0.toDistributionOverStates(pomdp);
			ArrayList<Double> Vb0 = new ArrayList<Double> ();
			for (int j=0; j<numObjs; j++) {
				double value = 0;
				for (int i=0; i<belief0.length; i++) {
					value += belief0[i] * matrix[i][j];
				}
				Vb0.add(value);
				mainLog.println("value"+value);
			}
			
			//Line 12 update A_all
			for (int i=0; i<Aw.size(); i++) {
				A_all.add(Aw.get(i).clone());
			}
			int countNewWeights = 0; //number of weights generated by u

			//Line 13 TO CHecker
			ArrayList<Double> u = new ArrayList<Double> ();
			for (int i=0; i<Vb0.size(); i++) {
				u.add(Math.abs(Vb0.get(i)));
			}
			mainLog.println("u="+u);
			
			alphaMatricesChecked.add((ArrayList<AlphaMatrix>)Aw.clone());
			w_v_checked.add(w_pop);//			w_v_checked.add(copyArrayList(w_pop));
			weights_checked.add(w_pop);
			w_v_checked.add(u);
			vector_checked.add(u);
			
			if (!containsWithError(partial_CCS,u, 1E-5)) {
				computeNewWeights(partial_CCS, partial_CCS_weights, w_v_checked, vector_checked, weights_checked, priority_queue,
						w_pop, u, corner_to_value, value_to_corner, minMaxList, pomdp, numObjs, threshold, countNewWeights);
			}else{
					mainLog.println("Vector already in the partial CCS");
			}
		}
		double elapsed = (System.currentTimeMillis() - startTime) * 0.001;
		mainLog.println("total time elapsed = " + elapsed);
		mainLog.println("ALl weights checked:"+ weights_checked.size());
		HashSet values_OLS = new HashSet();

		for (int iprint=0; iprint<weights_checked.size();iprint++){
			mainLog.print("weight: "+Arrays.toString(weights_checked.get(iprint).toArray())+"; vector: "+Arrays.toString(vector_checked.get(iprint).toArray())+"\n");
		}
		mainLog.println("*********************ParetoCurve by optimal linear solution*******************************");

		for (int iprint=0; iprint<partial_CCS.size();iprint++){
			mainLog.print("weight: "+Arrays.toString(partial_CCS_weights.get(iprint).toArray())+"; vector: "+Arrays.toString(partial_CCS.get(iprint).toArray())+"\n");
			values_OLS.add(innerProduct(partial_CCS_weights.get(iprint),partial_CCS.get(iprint)));

		}
		mainLog.print("#weights: "+partial_CCS_weights.size()+"\n");
		for (int iprint=0; iprint<partial_CCS.size();iprint++){
			mainLog.print(Arrays.toString(partial_CCS_weights.get(iprint).toArray())+"\n");
		}
		mainLog.println("#Parecto Curve points: "+partial_CCS.size()+"\n");
		for (int iprint=0; iprint<partial_CCS.size();iprint++){
			mainLog.print(Arrays.toString(partial_CCS.get(iprint).toArray())+"\n");
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Dummy return value
		
		// Return Pareto curve as Point list
		List<Point> points = new ArrayList<>();
		for (ArrayList<Double> paretoPoint : partial_CCS) {
			Point point = new Point(paretoPoint.size());
			for (int dim = 0; dim < paretoPoint.size(); dim++) {
				point.setCoord(dim, paretoPoint.get(dim));
			}
			points.add(point);
		}
		Object array[] = new Object[pomdp.getNumStates()];
		array[pomdp.getFirstInitialState()] = points;
		
		
		
//		
//		//Simulate Mul
//		double[] b = pomdp.getInitialBeliefInDist();
//		int s = drawFromDistr(b);
//		int nObj = weights_checked.get(0).size();
//		double[][] bounds = setObjectiveBounds(nObj);
//		double discount = 1;
//		while (!endStates.contains(s)) {
//			mainLog.println("current b"+ Arrays.toString(b));
//			ArrayList<Integer> possibleActions = new ArrayList<Integer>();
//			for (Object action :pomdp.getAvailableActions(s)) {
//				possibleActions.add(actionToIndex.get(action));
//			}
//			ArrayList<Integer> allowedActions = getAllowedActions(weights_checked, alphaMatricesChecked, b, bounds, possibleActions);
//			mainLog.println(allowedActions.toString());
//			
//			Random rand = new Random();
//			int randomIndex = rand.nextInt(allowedActions.size());
//			int actionIndex = allowedActions.get(randomIndex);
//			Object action = allActions.get(actionIndex);
//			int choice = pomdp.getChoiceByAction(s, action);
//			
//			double[] expected = immediateRewards.get(actionIndex).values(b);
//			for (int i = 0; i < 2; i++) {
//				for (int j = 0; j < nObj; j++) {
//					bounds[i][j] -= expected[j] * discount;
//				}
//			}
//			discount *= 0.95; 
//			HashMap<Integer, Double> observationProb = pomdp.computeObservationProbsAfterAction(b, choice);
//			int obs = drawFromDistr(observationProb);
//			mainLog.println("actionTaken"+actionIndex+ " obs="+obs);
//			b = pomdp.getBeliefInDistAfterChoiceAndObservation(b, choice, obs);
//			s = drawFromDistr(b);	
//		}
//		
		return StateValues.createFromObjectArray(TypeDouble.getInstance(), array, pomdp);
    }
	
    public double[][] setObjectiveBounds(int nObj) throws PrismException
    {
    	double bounds[][] = new double[2][nObj];
    	double[] lowerBounds = new double[nObj];
    	double[] upperBounds = new double[nObj];
		//TODO
		lowerBounds[0] = 46/3*0.95;
		lowerBounds[1] = 46/3*0.95;
		upperBounds[0] = 84/3*0.95;
		upperBounds[1] = 84/3*0.95;
		
		double lo = -4;
		lowerBounds[0] = lo;
		lowerBounds[1] = lo;
		double up = -3;
		upperBounds[0] = up;
		upperBounds[1] = up;
		
		bounds[0] = lowerBounds;
		bounds[1] = upperBounds;
    	return bounds;
    }
    public ArrayList<Integer> getAllowedActions(ArrayList<ArrayList<Double>> weights, ArrayList<ArrayList<AlphaMatrix>> alphaMatricesChecked, 
    											double[] b, double[][] bounds, ArrayList<Integer> possibleActions )
    {
//    	ArrayList<Integer> allowedActionIndex = new ArrayList<Integer> ();
//    	for (int i = 0; i < nActions; i++) {
//    		allowedActionIndex.add(i);
//    	}
    	for (ArrayList<AlphaMatrix> matrices: alphaMatricesChecked) {
    		ArrayList<Integer> allowedActionsForOneWeight = new ArrayList<Integer> ();
    		for (AlphaMatrix matrix: matrices) {
    			double [] values = matrix.values(b);
    			System.out.println(Arrays.toString(values)+ matrix.getAction());
    			if (inBound(values, bounds)) {
    				allowedActionsForOneWeight.add(matrix.getAction());
    			}
    		}
    		possibleActions.retainAll(allowedActionsForOneWeight);
    	}	
    	return possibleActions;
    }
    public boolean inBound(double [] values, double [][]bounds) 
    {
    	double [] low = bounds[0];
    	double [] up = bounds[1];
    	for (int i = 0; i < values.length; i++) {
    		if (low[i] > values[i] || values[i] > up[i]) {
    			return false;
    		}
    	}
    	return true;
    }
    
	public int step(POMDP pomdp, int state, int actionIndex, List<Object> allActions) //TODO can be deleted
	{
		Object action = allActions.get(actionIndex);
		if(!pomdp.getAvailableActions(state).contains(action)) {
			System.out.print("error step " + action + state);
			return -1;
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
		int nextState = nextStates.get(drawFromDistr(nextStatesProbs));
		//TODO add reward for final state
//		if (endStates.contains(nextState)){
//			reward += mdpRewards.getStateReward(nextState) * gamma;
//		}
//		return new stepReturn(nextState, reward );
		return nextState;
	}
	
	public int drawFromDistr(HashMap<Integer, Double>distr) // TODO can be improved?
	{
		int state = 0;
		double randomThreshold = Math.random();
		double cumulativeProb = 0;
		for (int key: distr.keySet()) {
			cumulativeProb += distr.get(key);
			if (cumulativeProb >= randomThreshold) {
				state = key;
				break;
			}
		}
		return state;
	}
	
	public int drawFromDistr(ArrayList<Double> distr) // TODO can be improved?
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
	public int drawFromDistr(double[] distr) // TODO can be improved?
	{
		int state = 0;
		double randomThreshold = Math.random();
		double cumulativeProb = 0;
		for (int i = 0; i < distr.length; i++) {
			cumulativeProb += distr[i];
			if (cumulativeProb >= randomThreshold) {
				state = i;
				break;
			}
		}
		return state; 
	}
	public ArrayList<Double> linSolver(ArrayList<ArrayList<Double>> A, ArrayList<Double> b)
	{
		if (A.get(0).size()!=A.size()){
			mainLog.println("Matrix is not square!");
		}
		ArrayList<ArrayList<Double>> Ab = new ArrayList<ArrayList<Double>>();
		for (int i=0; i<A.size(); i++) {
			//Ab.add( (ArrayList<Double>) A.get(i).clone() );
			Ab.add( A.get(i));
			Ab.get(i).add(b.get(i));
		}
		//mainLog.println("\nA:"+Arrays.toString(A.toArray()));
		//mainLog.println("\nb:"+Arrays.toString(b.toArray()));		
		for (int k=0; k<Ab.size()-1; k++) {
			double pivot = Ab.get(k).get(k);
			// find the max and swap
			double max=pivot;
			int max_location=k;
			for (int imax=k;imax<Ab.size();imax++) {
				if (Ab.get(imax).get(k)>max) {
					max = Ab.get(imax).get(k);
					max_location=imax;
				}
			}
//			ArrayList<Double> tpmax= (ArrayList<Double>) Ab.get(k).clone();
	//		Ab.set(k, (ArrayList<Double>) Ab.get(max_location).clone());
			ArrayList<Double> tpmax= Ab.get(k);
			Ab.set(k,Ab.get(max_location));
			
			Ab.set(max_location, tpmax);
			pivot=Ab.get(k).get(k);
			if (pivot==0) {
				continue;
			}
			for (int j=k; j<Ab.get(0).size(); j++) {
				Ab.get(k).set(j, Ab.get(k).get(j)/pivot);
			}

			for (int i= k+1; i<Ab.size(); i++) {
				pivot = Ab.get(i).get(k);
				for (int j=0; j<Ab.get(0).size(); j++) {
					Ab.get(i).set(j, Ab.get(i).get(j)-Ab.get(k).get(j)*pivot);
				}
			}

		}
		for (int k=Ab.size()-1; k>0;k--) {
			double pivot = Ab.get(k).get(k);
			if (pivot==0){
				continue;
			}
			for (int j=0; j<Ab.get(0).size();j++) {
				Ab.get(k).set(j, Ab.get(k).get(j)/pivot);
			}
			for (int i=k-1; i>=0; i--) {
				pivot=Ab.get(i).get(k);
				for (int j=0; j<Ab.get(0).size();j++) {
					Ab.get(i).set(j, Ab.get(i).get(j)-Ab.get(k).get(j)*pivot);
				}
			}
		}

		ArrayList<Double> w_new = new ArrayList<Double>();
		for (int i=0; i<Ab.size(); i++) {
			w_new.add(Ab.get(i).get(Ab.get(i).size()-1));
		}
		mainLog.println("solved Ab:"+Arrays.toString(Ab.toArray()));
		return w_new;
	}
	public boolean containsWithError(ArrayList<ArrayList<Double>> S, ArrayList<Double> u, double error_threshold){
		for (int i=0; i<S.size(); i++){
			double error =0.0;
			for (int j=0; j<u.size(); j++){
				error += Math.abs(S.get(i).get(j)-u.get(j));
			}
			if (error<error_threshold){
				return true;
			}
		}
		return false;
	}

//	combinations2(old_value_vectors, numObjs-1, 0, subset, subsets);


    public void combinations2(ArrayList<ArrayList<Double>> arr, int len, int startPosition, ArrayList<ArrayList<Double>> result,  ArrayList<ArrayList<ArrayList<Double>>> subsets ){
        if (len == 0){
        	mainLog.println("one instace of combinations");
		    mainLog.println(Arrays.toString(result.toArray()));
//            subsets.add((ArrayList<ArrayList<Double>> )result.clone());
            subsets.add(copyArrayListList(result));
            return;
        } 

        for (int i = startPosition; i <= arr.size()-len; i++){
            //result[result.length - len] = arr[i];
            result.set(result.size()-len, (ArrayList<Double>) arr.get(i));
            //result.add((ArrayList<Double>) arr.get(i));
            combinations2(arr, len-1, i+1, result, subsets);
            
        }
    }       

    public double innerProduct(ArrayList<Double> A, ArrayList<Double> B){
    	//compute innerproduct of vector A, B
    	double result=0.0;
    	if(A.size()!=B.size()){
    		mainLog.println("vectors should have same size");
    	}
    	else{
    		for (int i=0; i<A.size();i++){
    			result += ((double) A.get(i) )* ((double) B.get(i));
    		}
    	}
    	return result;
    }
    public double innerProduct(double [] A, ArrayList<Double> B){
    	//compute innerproduct of vector A, B
    	double result=0.0;
    	if(A.length!=B.size()){
    		mainLog.println("vectors should have same size");
    	}
    	else{
    		for (int i=0; i<B.size();i++){
    			result += (A[i] )* ((double) B.get(i));
    		}
    	}
    	return result;
    }

    public ArrayList<Double> adjustWeight(ArrayList<Double> weights, List<MinMax> minMaxList, Model model)throws PrismException
    {
    	// this function is to convert weigths for 'min' to 'max'
    	ArrayList<Double> weights_adjust_min_max = new ArrayList<Double>();

	    for (int i_weight=0;i_weight<minMaxList.size();i_weight++){
			if (minMaxList.get(i_weight).isMin()){
				weights_adjust_min_max.add(-1.0*((double) weights.get(i_weight)));
			}
			else{
				weights_adjust_min_max.add(((double) weights.get(i_weight)));
			}
	    }
	    return weights_adjust_min_max;
    }
    
    /* compute the max improvement 
     * See "Linear Support for Multi-Objective Coordination Graphs"
     * @param objs, list of reward expression, used to adapt min/max
     * @param model, model, used to adapt min/max
     * @param w_new, the newly generated weight, for which this function is computing the priority
     * @param partial_CCS, list of payoff vectors
     * @param partial_CCS, list of weights corresponding to the payoff vectors
     * @param u, the newly computed payoff vector (see Line 8 Algorithm 1 )
     * @param w_pop, the newly popped weight (see Line 7 Algorithm 1) 
     * */
    public double  maxValueLP(List<MinMax> minMaxList, Model model, ArrayList<Double> w_new, ArrayList<ArrayList<Double>> partial_CCS , ArrayList<ArrayList<Double>> partial_CCS_weights, ArrayList<Double> u, ArrayList<Double> w_pop ) throws PrismException 
    {
    	double value = 1;
    	try {
    		lpsolve.LpSolve solver = lpsolve.LpSolve.makeLp(0, w_new.size());
        	double[] objFun = adjustWeight( w_new,minMaxList, model).stream().mapToDouble(Double::doubleValue).toArray();
        	solver.strSetObjFn(Arrays.toString(objFun).replace("[", "").replace("]", "").replace(",", ""));
			solver.setVerbose(lpsolve.LpSolve.CRITICAL);

    		solver.setMaxim();
    		for (int i=0; i<partial_CCS_weights.size(); i++) {
    			double [] w = adjustWeight( partial_CCS_weights.get(i),minMaxList, model).stream().mapToDouble(Double::doubleValue).toArray();
    			double v = innerProduct(adjustWeight( partial_CCS_weights.get(i),minMaxList, model), partial_CCS.get(i));
        		solver.strAddConstraint(Arrays.toString(w).replace("[", "").replace("]", "").replace(",", ""), lpsolve.LpSolve.LE , v);
    		}
    		double [] w = adjustWeight(w_pop,minMaxList, model).stream().mapToDouble(Double::doubleValue).toArray();
    		double v = innerProduct(u, adjustWeight(w_pop,minMaxList, model));
    		solver.strAddConstraint(Arrays.toString(w).replace("[", "").replace("]", "").replace(",", ""), lpsolve.LpSolve.LE , v);
            solver.solve();
            value =  solver.getObjective();
    	}
    	catch (lpsolve.LpSolveException ex) {
			PrismException ex2 = new PrismException("lpsolve threw an exception: " + ex.getMessage());
			throw ex2;
		}
    	return value;
    }
    
    public ArrayList<Double> deQueue(ArrayList<ArrayList<Double>> priority_queue ){
		ArrayList<Double> w_pop = new ArrayList<Double>();
		if (priority_queue.size()>0){
			double top_priority = -1;
			int top_priority_index =0;
			for (int i=0; i<priority_queue.size(); i++) {
				if (priority_queue.get(i).get(priority_queue.get(i).size()-1) >= top_priority) {
					top_priority = priority_queue.get(i).get(priority_queue.get(i).size()-1);
					top_priority_index = i;
				}
			}			
			w_pop = priority_queue.get(top_priority_index);
			priority_queue.remove(top_priority_index);
			w_pop.remove(w_pop.size()-1);
			/*
			//Ensure weights sum to 1
			double tp_sum=0.0;
			for (int i=0; i<w_pop.size()-1;i++){
				tp_sum += (double) w_pop.get(i);
			}
			w_pop.set(w_pop.size()-1, 1- tp_sum);
			*/
		}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
		return w_pop;
    }
    public ArrayList<Double> copyArrayList(ArrayList<Double> a){
    	ArrayList<Double> copy = new ArrayList<Double>();
    	for (int i =0; i<a.size();i++) {
    		copy.add(a.get(i));
    	}
    	return copy;
    }
    public ArrayList<ArrayList<Double>> copyArrayListList(ArrayList<ArrayList<Double>> a){
    	ArrayList<ArrayList<Double>>copy = new ArrayList<ArrayList<Double>>();
    	for (int i =0; i<a.size();i++) {
    		copy.add(copyArrayList(a.get(i)));
    	}
    	return copy;
    }
    public ArrayList<ArrayList<Double>> initialQueue(List<MinMax> minMaxList, HashMap <ArrayList<Double>, ArrayList<ArrayList<Double> >>  corner_to_value, Model model)throws PrismException
    {
    	ArrayList<ArrayList<Double>> priority_queue = new ArrayList<ArrayList<Double>> ();
    	// create initial value vector for the exterme corner point
    	ArrayList<Double> initial_value_vector_weight = new ArrayList<Double>();
    	for (int i=0; i<minMaxList.size();i++){
			initial_value_vector_weight.add(-1.0);
    	}
    	
    	// initial value vector is adjust for "min, max"
    	// if (max max max) add (-inf, -inf, -inf)
    	initial_value_vector_weight = adjustWeight(initial_value_vector_weight, minMaxList, model);
    	ArrayList<Double> initial_value_vector = new ArrayList<Double>();
    	for (int i=0; i<minMaxList.size();i++){
			initial_value_vector.add(((double) initial_value_vector_weight.get(i)) * (Double.POSITIVE_INFINITY) );
    	}
    	ArrayList<ArrayList<Double>> initial_value_vector_sets = new ArrayList<ArrayList<Double>> ();
    	initial_value_vector_sets.add(initial_value_vector);
    	//add extreme points in the queue
		for (int i =0; i<minMaxList.size(); i++) {
			ArrayList<Double> w = new ArrayList<Double>();
			for (int j =0; j<minMaxList.size(); j++) {
				w.add(0.0);
			}
			w.set(i, 1.0); //Extremum
			corner_to_value.put(copyArrayList(w), initial_value_vector_sets); // create a map from extrema to value vector
			double priority = minMaxList.get(i).isMin()? 1E6:1E5; // give min higher priority
			w.add(priority); //Add extrema with infinite priority
			
			priority_queue.add(copyArrayList(w));
		}

		mainLog.println("Initialize Q (weight, priority)"+Arrays.toString(priority_queue.toArray()));
		mainLog.println("initial corner_to_value");
		for (Object  key:corner_to_value.keySet()){
			ArrayList<ArrayList<Double>> tp = (ArrayList<ArrayList<Double>>) corner_to_value.get(key);
			for (int i=0; i< tp.size(); i++){
				mainLog.println(key+" -> "+ Arrays.toString(((ArrayList<Double>) tp.get(i) ).toArray()));
			}
		}
		return priority_queue;
    }

    public void computeNewWeights(
    		ArrayList<ArrayList<Double>>	partial_CCS	,
    		ArrayList<ArrayList<Double>>	partial_CCS_weights	,
    		ArrayList<ArrayList<Double>>	w_v_checked	,
    		ArrayList<ArrayList<Double>>	vector_checked	,
    		ArrayList<ArrayList<Double>>	weights_checked	,
    		ArrayList<ArrayList<Double>>	priority_queue,
    		ArrayList<Double> w_pop, 
    		ArrayList<Double> u,
    		HashMap <ArrayList<Double>, ArrayList<ArrayList<Double> >> corner_to_value,
    		HashMap <ArrayList<Double>, ArrayList<ArrayList<Double> >> value_to_corner,  
    		List<MinMax> minMaxList,
    		Model model,
    		int numObjs,
    		double threshold,
    		int countNewWeights
    		) throws PrismException
    {
			if (partial_CCS.size()==0) {// when to compute
				for (int i_queue=0; i_queue<priority_queue.size();i_queue++){
					ArrayList<Double> weight_tp = copyArrayList(priority_queue.get(i_queue));
					weight_tp.remove(weight_tp.size()-1);
//					ArrayList<ArrayList<Double>> current_value_set = (ArrayList<ArrayList<Double>>) corner_to_value.get(weight_tp).clone();
					ArrayList<ArrayList<Double>> current_value_set =  copyArrayListList(corner_to_value.get(weight_tp));
					current_value_set.add(u);
					corner_to_value.put(weight_tp, current_value_set );
					ArrayList<ArrayList<Double>> weight_tp_set =  new ArrayList<ArrayList<Double>> ();
					if(value_to_corner.containsKey(u))
						weight_tp_set = copyArrayListList( value_to_corner.get(u));
					weight_tp_set.add(weight_tp);
					value_to_corner.put(u,weight_tp_set);
				}
				mainLog.println("add value vector from 1st exterme weights");
				partial_CCS.add(copyArrayList(u));
				partial_CCS_weights.add(copyArrayList(w_pop));
				mainLog.println(partial_CCS.size());
			}
			else {
				mainLog.println("else");
				double original_value = innerProduct(adjustWeight(w_pop, minMaxList, model), u);
				double other_prod = innerProduct(adjustWeight(w_pop,minMaxList,model), corner_to_value.get(w_pop).get(corner_to_value.get(w_pop).size()-1));
				if ((original_value - other_prod)>1E-08){

					//remove from value dict
					ArrayList<ArrayList<Double>> existing_value_vectors = corner_to_value.get(w_pop);
					ArrayList<ArrayList<Double>> existing_weights = new ArrayList<ArrayList<Double>> () ;
					for (int i_evv=0; i_evv<existing_value_vectors.size(); i_evv++){
						ArrayList<Double> existing_value_vector = existing_value_vectors.get(i_evv);
						if (value_to_corner.containsKey(existing_value_vector)){
							existing_weights = value_to_corner.get(existing_value_vector);
							for (int j_ew=0; j_ew<existing_weights.size();j_ew++){
								if ( containsWithError(existing_weights, w_pop, threshold) && (!(w_pop.contains(1.0)))){
									int tp_index = existing_weights.indexOf(w_pop);
									value_to_corner.get(existing_value_vector).remove(tp_index);
								}
							}
						}
					}

					//check obsolete 
					ArrayList<ArrayList<Double>> weight_list = new ArrayList<ArrayList<Double>> ();
					weight_list.add(w_pop);
					ArrayList<ArrayList<Double>> obsolete_list = new ArrayList<ArrayList<Double>> ();
					while (weight_list.size()>0){
						ArrayList<Double> weight = weight_list.get(weight_list.size()-1);
						weight_list.remove(weight_list.size()-1);
						if (corner_to_value.containsKey(weight)){
							existing_value_vectors =  copyArrayListList(corner_to_value.get(weight));
							ArrayList<Double> existing_value_vector = existing_value_vectors.get(existing_value_vectors.size()-1);
							double scalarized_value = innerProduct(existing_value_vector, adjustWeight(weight,minMaxList,model));
							if (original_value>scalarized_value){
								for (int i_evv=0; i_evv<existing_value_vectors.size(); i_evv++){
									existing_value_vector = existing_value_vectors.get(i_evv);
									if (value_to_corner.containsKey(existing_value_vector)){

										mainLog.println("+++++++++corner_to_value");
										for (Object  key:corner_to_value.keySet()){
											ArrayList<ArrayList<Double>> tp = (ArrayList<ArrayList<Double>>) corner_to_value.get(key);
											for (int i=0; i< tp.size(); i++){
												mainLog.println(key+" -> "+ Arrays.toString(((ArrayList<Double>) tp.get(i) ).toArray()));
											}
										}
										mainLog.println("+++++++++value_to_corner");
										for (Object  key:value_to_corner.keySet()){
											ArrayList<ArrayList<Double>> tp = (ArrayList<ArrayList<Double>>) value_to_corner.get(key);
											for (int i=0; i< tp.size(); i++){
												mainLog.println(key+" -> "+ Arrays.toString(((ArrayList<Double>) tp.get(i) ).toArray()));
											}
										}
										ArrayList<ArrayList<Double>> current_weights = value_to_corner.get(existing_value_vector);
										for (int j_cw=0; j_cw<current_weights.size(); j_cw++){
											ArrayList<Double> current_weight = current_weights.get(j_cw);
											if ((!current_weights.equals(weight)) && (obsolete_list.contains(current_weight))){
												weight_list.add(copyArrayList(current_weight));
											}
										}											
									}
								}
								obsolete_list.add(copyArrayList(weight));
							}
						}
					}

					for (int iprint=0; iprint<obsolete_list.size();iprint++){
						mainLog.println("obsolete_list: "+Arrays.toString(obsolete_list.get(iprint).toArray())+"\n");
					}

					///compute new corner
					ArrayList<ArrayList<Double>> boundaries=new ArrayList<ArrayList<Double>>();
					for (int i_obs=0; i_obs<obsolete_list.size();i_obs++){
						ArrayList<Double> obsolete_weight = obsolete_list.get(i_obs);
						for (int j_obs=0; j_obs<obsolete_weight.size();j_obs++){
							ArrayList<Double> boundary = new ArrayList<Double> ();
							boundary.add((double) j_obs);
							if (((double) obsolete_weight.get(j_obs)==0) && (!boundaries.contains(boundary)))
								boundaries.add(boundary);
						}
					}
					ArrayList<ArrayList<Double>> old_value_vectors = new ArrayList<ArrayList<Double>>  ();

					for (int i_obs=0; i_obs<obsolete_list.size();i_obs++){
						ArrayList<Double> found_weight = obsolete_list.get(i_obs);
						ArrayList<ArrayList<Double>> found_values = corner_to_value.get(found_weight);
						for (int j_fv=0; j_fv<found_values.size(); j_fv++){
							ArrayList<Double> found_value = found_values.get(j_fv);
							mainLog.println("found_value"+Arrays.toString(found_value.toArray()));
							mainLog.println(!old_value_vectors.contains(found_value));
							mainLog.println("u"+Arrays.toString(u.toArray()));
							mainLog.println(!found_value.equals(u));
							if((!old_value_vectors.contains(found_value))&&(!found_value.equals(u))) {
								old_value_vectors.add( copyArrayList(found_value));
							}
						}
					}

					// /*
					old_value_vectors = new ArrayList<ArrayList<Double>>  ();

					for (int i_obs=0; i_obs<obsolete_list.size();i_obs++){
						double bestValue = innerProduct(adjustWeight((ArrayList<Double>) obsolete_list.get(i_obs), minMaxList, model), partial_CCS.get(0));
						for (int j_partialCSS=0; j_partialCSS < partial_CCS.size(); j_partialCSS++){
							if (innerProduct(adjustWeight((ArrayList<Double>) obsolete_list.get(i_obs), minMaxList, model), partial_CCS.get(j_partialCSS))>bestValue){
								bestValue = innerProduct(adjustWeight((ArrayList<Double>) obsolete_list.get(i_obs), minMaxList, model), partial_CCS.get(j_partialCSS));
							}
						}
						for (int j_partialCSS=0; j_partialCSS < partial_CCS.size(); j_partialCSS++){
							if (Math.abs(innerProduct(adjustWeight((ArrayList<Double>) obsolete_list.get(i_obs), minMaxList, model), partial_CCS.get(j_partialCSS))-bestValue)<1E-06){
								if (old_value_vectors.size()<numObjs){ //if Vs(w) contians fewer than dvalue vecotrs
									old_value_vectors.add(partial_CCS.get(j_partialCSS));
								}
							}
						}
					}


					for (int i_b=0; i_b<boundaries.size();i_b++){
						old_value_vectors.add(copyArrayList(boundaries.get(i_b)));
					}

					for (int iprint=0; iprint<old_value_vectors.size();iprint++){
						mainLog.println("old_value_vectors: "+Arrays.toString(old_value_vectors.get(iprint).toArray())+"\n");
					}

					//compute new points
					//Add to queue
					ArrayList<ArrayList<Double>> subset = new ArrayList<ArrayList<Double>>(); //one kind of combination

					ArrayList<Double> tpp= new ArrayList<Double>();
					tpp.add(1.0);
					tpp.add(1.0);
					for (int i_ojs=0; i_ojs<numObjs-1;i_ojs++){
						subset.add(tpp);
					}
					ArrayList<ArrayList<ArrayList<Double>>> subsets = new ArrayList<ArrayList<ArrayList<Double>>>(); //all combination
					mainLog.println("old_value_vectors size"+old_value_vectors.size());
					combinations2(old_value_vectors, numObjs-1, 0, subset, subsets);

					mainLog.println("allsubsets: total number of combinations"+Arrays.toString(subsets.toArray())+subsets.size());
					
					for(int i_subset=0; i_subset<subsets.size();i_subset++){
						ArrayList<ArrayList<Double>> hCCS = new ArrayList<ArrayList<Double>>(); //optimistic hypothetical CCS
						ArrayList<ArrayList<Double>> A = new ArrayList<ArrayList<Double>>();
						ArrayList<Double> augumented_vector = new ArrayList<Double>();
						ArrayList<Double> bound_from_w_obsolete = new ArrayList<Double>();
						ArrayList<ArrayList<Double>> oneCombination = copyArrayListList( subsets.get(i_subset));
						
						for (int j=0; j<oneCombination.size(); j++){
							augumented_vector = copyArrayList( oneCombination.get(j));
							
							if (augumented_vector.size()==numObjs){
								
								// if one object k is minimizing, then convert it to u[k] *= -1 
								for (int k=0; k<numObjs; k++) {
									if (minMaxList.get(k).isMin()) {
										augumented_vector.set(k, -1* augumented_vector.get(k));
										mainLog.println("convert "+ Arrays.toString(augumented_vector.toArray()));
									}
								}
								
								hCCS.add(copyArrayList(augumented_vector));
								
								augumented_vector.add(-1.0);
								A.add(augumented_vector);
							}
							else{
								bound_from_w_obsolete.add((double) augumented_vector.get(0));
							}
						}
						hCCS.add(copyArrayList(u));
						augumented_vector = copyArrayList(u);
						
						for (int k=0; k<numObjs; k++) {
							if (minMaxList.get(k).isMin()) {
								augumented_vector.set(k, -1* augumented_vector.get(k));
								mainLog.println("convert "+ Arrays.toString(augumented_vector.toArray()));
							}
						}
						
						augumented_vector.add(-1.0);
						A.add(copyArrayList(augumented_vector));

						//simplex constraint
						ArrayList<Double> bound = new ArrayList<Double>();
						for (int i=0; i<numObjs;i++) {
							bound.add(1.0);
						}
						bound.add(0.0);
						A.add(bound);


						// remove column that has bound. (E.g. for weight [0.5,0.5,0], the boundary index is 2, then remove column 2 from A)
						
						for (int i_bf=0; i_bf<bound_from_w_obsolete.size();i_bf++){
							double removeIndex = (double) bound_from_w_obsolete.get(i_bf);
							int remove = (int) removeIndex;
							for (int i_A=0; i_A<A.size(); i_A++){
								A.get(i_A).remove(remove);
							}
						}
						
						ArrayList<Double> b = new ArrayList<Double>();
						for (int i=0; i<A.size()-1; i++) {
							b.add(0.0);
						}
						b.add(1.0);

						ArrayList<Double> w_new = new ArrayList<Double>();
						w_new = linSolver(A,b);
						w_new.remove(w_new.size()-1);
						
						for (int i_bf=0; i_bf<bound_from_w_obsolete.size();i_bf++){
							double insertIndex = (double) bound_from_w_obsolete.get(i_bf);
							int insert = (int) insertIndex;
							w_new.add(insert,0.0);
						}
						
						mainLog.println("weights caculated: "+Arrays.toString(w_new.toArray()));
						Boolean allPositive = true;
						for (int iw=0; iw<w_new.size();iw++){
							if ((double) w_new.get(iw)<0){
								allPositive = false;
							}
						}
						if (!allPositive){
							mainLog.println("Negative... continue");
							continue;
						}

						if (w_new.contains(1.0)){
							mainLog.println("Extreme... continue");
							continue;
						}
						if (w_new.contains(Double.NaN)){
							mainLog.println("NaN... continue");
							continue;
						}
						mainLog.println("generateing new weights by"+Arrays.toString(u.toArray()));
						
						countNewWeights++;
						mainLog.println(countNewWeights+"Number of New weights generated by :"+Arrays.toString(u.toArray()));
						mainLog.println("w_new"+Arrays.toString(w_new.toArray()));
						if (countNewWeights>numObjs)
							mainLog.println("More new weights than expected");

						//////// this is for rounding
						
						double weigth_sum = 0.0;
						for (int iw=0;iw<w_new.size()-1;iw++){
							w_new.set(iw, (double) Math.round((double)w_new.get(iw) * 1000000)/1000000);
							weigth_sum += (double) w_new.get(iw);
						}
						w_new.set(w_new.size()-1, (double) 1-weigth_sum);
						mainLog.println("w_new (sum to 1)"+Arrays.toString(w_new.toArray()));
						

						//update value_to_corner & corner_to_value
						ArrayList<ArrayList<Double>> default_value_vecotrs = new  ArrayList<ArrayList<Double>>();
						default_value_vecotrs.add(copyArrayList(u));
						corner_to_value.put(copyArrayList(w_new), default_value_vecotrs);

						ArrayList<ArrayList<Double>> new_weights = new  ArrayList<ArrayList<Double>>();
						new_weights.add(copyArrayList(w_new));
						value_to_corner.put(u,new_weights);


						// Add to priority queue
						double priority=1.0;

						mainLog.println("computing priority = "+hCCS.size());
						
						double VCCS = maxValueLP(minMaxList, model, w_new, partial_CCS , partial_CCS_weights, u, w_pop);
						double Vsw = innerProduct(u, adjustWeight(w_new, minMaxList, model));
						priority = Math.abs((VCCS-Vsw)/(VCCS));
						
						
						if (priority > threshold){
							w_new.add(priority);
							priority_queue.add(copyArrayList(w_new));
						}

						//delete obsolete list from priority queue
						for (int i_obs=0; i_obs<obsolete_list.size();i_obs++){
							ArrayList<Double> obsolete_weight =  obsolete_list.get(i_obs);
							if (!obsolete_weight.contains(1.0)){
								for (int j_queue=0; j_queue<priority_queue.size(); j_queue++){
									ArrayList<Double> tp_weight = copyArrayList(priority_queue.get(j_queue));
									tp_weight.remove(tp_weight.size()-1);
									if (tp_weight.equals(obsolete_weight)){
										mainLog.println("Removing obsolete_weight:"+ Arrays.toString(obsolete_weight.toArray()));
										priority_queue.remove(j_queue);
									}
								}
							}
						}
					}
				}
				//add to solution
				partial_CCS.add(copyArrayList(u));
				partial_CCS_weights.add(copyArrayList(w_pop));
			}
    }
    

    protected double[][] computeObjectiveBounds(POMDP<Double> pomdp, List<MDPRewards<Double>> mdpRewardsList, BitSet target, List<MinMax> minMaxList, BitSet statesOfInterest) throws PrismException
    {
    	int objNum = minMaxList.size();
    	int nObj = minMaxList.size();
    	double bounds[][] = new double[2][objNum];
		double[] lowerBounds = new double[objNum];
		double[] upperBounds = new double[objNum];
		//TODO
		lowerBounds[0] = 0;
		lowerBounds[1] = 0;
		upperBounds[0] = 114;
		upperBounds[1] = 114;
		bounds[0] = lowerBounds;
		bounds[1] = upperBounds;
    	return bounds;
    }
    protected StateValues computeMultiStrategyMultiObjPOMDP(POMDP<Double> pomdp, List<MDPRewards<Double>> mdpRewardsList, BitSet target, List<MinMax> minMaxList, BitSet statesOfInterest) throws PrismException
    {
    	BitSet targetObs = ((POMDPModelChecker) mc).getObservationsMatchingStates(pomdp, target);
		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}	
		if (targetObs == null) {
			throw new PrismException("Target for expected reachability is not observable");
		}
    	
    	long startTime = System.currentTimeMillis();

    	double bounds[][] = computeObjectiveBounds(pomdp, mdpRewardsList, target, minMaxList, statesOfInterest);
    	double lowerBounds[] = bounds[0];
    	double upperBounds[] = bounds[1];
    	
    	int nObj = minMaxList.size();

    	ModelCheckerResult res = null;
		int nStates = pomdp.getNumStates();
		ArrayList<Object> allActions = ((POMDPModelChecker) mc).getAllActions(pomdp);
		int nActions = allActions.size();
		HashMap<Object, Integer> action_to_index = new HashMap<Object, Integer> ();
		for (int a = 0; a < nActions; a ++) {
			action_to_index.put(allActions.get(a), a);
		}
		
		double probTransition[][][] = new double [nActions][nStates][nStates];
		for (int s = 0 ; s < nStates; s++) {
			for (int a = 0; a < nActions; a++) {
				Object action = allActions.get(a);
				if (!pomdp.getAvailableActions(s).contains(action)) {
					continue;
				}
				int choice = pomdp.getChoiceByAction(s, action);
				Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(s,choice);
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> trans = iter.next();
					int sPrime = trans.getKey();
					probTransition[a][s][sPrime] = trans.getValue();
				}
			}
		}
		

		double reward[][][] = new double[nObj][nStates][nActions];
		for (int s = 0; s < nStates; s++) {
			for (int a = 0; a < nActions; a++) {
				//what if action is unavailable??
				Object action = allActions.get(a);
				if(!pomdp.getAvailableActions(s).contains(action)) {
					continue;
				}
				int choice = pomdp.getChoiceByAction(s, action);	
				for (int i = 0 ; i < nObj; i++) {
					double r_s_a = mdpRewardsList.get(i).getTransitionReward(s, choice) + mdpRewardsList.get(i).getStateReward(s); // 
//					if (minMaxList.get(i).isMin()) {
//						r_s_a *= -1;
//					}
					reward[i][s][a] = r_s_a;
				}
			}
		}
		
		for (int i = 0; i < nObj; i++) {
			mainLog.println("objective " + i);
			for (int s = 0; s < nStates; s++) {
				mainLog.println(Arrays.toString(reward[i][s]));
			}
		}
		
		for (int a = 0; a < nActions; a++) {
			mainLog.println("action = "  + allActions.get(a));
			for (int s = 0; s < nStates; s++) {
				mainLog.println(Arrays.toString(probTransition[a][s]));
			}
		}
		int max_value = 1000;
		double discount = 0.95;
		int nObservations = pomdp.getNumObservations();
		try {
			ArrayList<String> varNames = new ArrayList<String>();
			for (int i = 0; i < pomdp.getVarList().getNumVars(); i++) {
				varNames.add(pomdp.getVarList().getName(i));
			}
			for (int s = 0; s < nStates; s++) {
				mainLog.println("state="+s+", obs="+pomdp.getObservation(s)+", meaning="+ pomdp.getStatesList().get(s).toString(varNames));
			}
			
			GRBEnv env = new GRBEnv("gurobi.log");
			env.set(GRB.IntParam.OutputFlag, 0);
			GRBModel model = new GRBModel(env);
			env.start();

			int nNodes = nObservations;
			nNodes = 3;
			double initial_belief_n_s[][] = new double[nNodes][nStates];
			
			//set the initial belief of (s,n): b(s,n)
//			for (int n = 0; n < nNodes; n++) {
//				initial_belief_n_s[n][0] =   1.0 ;
//			}
			initial_belief_n_s[0][0] = 1;
			// Create variables
			
			// 1 x(n,s,a)
			GRBVar X_n_s_a[][][] = new GRBVar[nNodes][nStates][nActions];
			
			for (int n = 0; n < nNodes; n++) {
				for (int s = 0; s < nStates; s++) {
					for (int a = 0; a < nActions; a++) {
						if (X_n_s_a[n][s][a] == null) {
							X_n_s_a[n][s][a] = model.addVar(0, 1000, 0, GRB.CONTINUOUS, String.format("X_n_s_a_%d_%d_%s", n, s, allActions.get(a)));
						}
					}
				}
			}
			
			// 2 x(n,s,a, n'y)
			GRBVar X_n_s_a_y_nPrime [][][][][] = new GRBVar[nNodes][nStates][nActions][nObservations][nNodes];
			for (int n = 0; n < nNodes; n++) {
				for (int s = 0; s < nStates; s++) {
					for (int a = 0; a < nActions; a++) {
						for (int y = 0; y < nObservations; y++) {
							for (int nPrime = 0; nPrime < nNodes; nPrime++) {
								if (X_n_s_a_y_nPrime[n][s][a][y][nPrime] == null) {
									X_n_s_a_y_nPrime[n][s][a][y][nPrime] =  model.addVar(0, 1000, 0, GRB.CONTINUOUS, 
																			String.format("X_n_s_a_y_nPrime_%d_%d_%s_%d_%d", n, s, allActions.get(a), y, nPrime));
								}
							}
						}
					}
				}
			}
			
			
			// 3x(n,a)
			GRBVar X_n_a[][] = new GRBVar[nNodes][nActions];
			
			for (int n = 0; n < nNodes; n++) {
				for (int a = 0; a < nActions; a++) {
					if (X_n_a[n][a] == null) {
						X_n_a[n][a] = model.addVar(0, 1000, 0, GRB.CONTINUOUS, String.format("X_n_a_%d_%s", n, allActions.get(a)));
					}
				}
			}
			
			// 4 x(n)
			GRBVar X_n[] = new GRBVar[nNodes];
			
			for (int n = 0; n < nNodes; n++) {
				if (X_n[n] == null) {
					X_n[n] = model.addVar(0, 1000, 0, GRB.CONTINUOUS, String.format("X_n_%d", n));
				}
			}
			
			// 5 x(n, n'y)
			GRBVar X_n_y_nPrime[][][] = new GRBVar[nNodes][nObservations][nNodes];
			for (int n = 0; n < nNodes; n++) {
				for (int y = 0; y < nObservations; y++) {
					for (int nPrime = 0; nPrime < nNodes; nPrime++) {
						if (X_n_y_nPrime[n][y][nPrime] == null) {
							X_n_y_nPrime[n][y][nPrime] = model.addVar(0, 1000, 0, GRB.CONTINUOUS, String.format("X_n_y_nPrime_%d_%d_%d", n, y, nPrime));
						}
					}
				}
			}
			
			// 6 x(a|n)
			GRBVar X_a__n[][] = new GRBVar[nActions][nNodes];

			for (int a = 0; a < nActions; a++) {
				for (int n = 0; n < nNodes; n++) {
					if (X_a__n[a][n] == null) {
						X_a__n[a][n] = model.addVar(0, 1000, 0, GRB.BINARY, String.format("X_a__n_%s_%d", allActions.get(a), n));
					}
				}
			}
			
			
			// 7 x(n'|n,y)
			GRBVar X_nPrime__n__y[][][] = new GRBVar [nNodes][nNodes][nObservations];
			for (int n = 0; n < nNodes; n++) {
				for (int nPrime = 0; nPrime < nNodes; nPrime++) {
					for (int y = 0; y < nObservations; y++) {
						X_nPrime__n__y [nPrime][n][y] =  model.addVar(0, 1000, 0, GRB.BINARY, String.format("X_nPrime__n__y_%d_%d_%d",  nPrime, n, y));
					}
				}
			}
			
			// TODO added varibale
			GRBVar X_Valuelow[] = new GRBVar [nObj];
			for (int i = 0; i < nObj; i++) {
				X_Valuelow[i]=  model.addVar(0, 1000, 0, GRB.CONTINUOUS, String.format("X_valuelow_%d",i));
			}
				

			GRBVar X_Valueup[] = new GRBVar [nObj];
			for (int i = 0; i < nObj; i++) {
				X_Valueup[i]=  model.addVar(0, 1000, 0, GRB.CONTINUOUS, String.format("X_valueup_%d",i));
			}

			//Set Objective: Max sum_n{sum_a{x(|n)}} / TODO
			GRBLinExpr expr = new GRBLinExpr();
			for (int n = 0; n < nNodes; n++) {
				for (int a = 0; a < nActions; a++) {
						expr.addTerm(1, X_a__n[a][n]);
				}
			}
			model.setObjective(expr, GRB.MAXIMIZE);
			///////////// add constraint
			// 15
			int constraint_count = 0;
			
			for (int nPrime = 0; nPrime < nNodes; nPrime++) {
				for (int sPrime = 0; sPrime < nStates; sPrime++) {
					expr = new GRBLinExpr();
					
					for (int a = 0; a < nActions; a++) {
						Object action = allActions.get(a);
						
						if (!pomdp.getAvailableActions(sPrime).contains(action)) {
							GRBLinExpr expr_unavailable = new GRBLinExpr();
							expr_unavailable.addTerm(1, X_n_s_a[nPrime][sPrime][a]);
							model.addConstr(0, GRB.EQUAL, expr_unavailable, String.format("c25_%d",constraint_count++));
						}
//						else {
							expr.addTerm(1, X_n_s_a[nPrime][sPrime][a]);
//						}
					}
					
					
					for (int n = 0; n < nNodes; n++) {
						for (int s = 0; s < nStates; s++) {
							for (int a = 0; a < nActions; a++) {
								if (probTransition[a][s][sPrime] == 0) {
									continue;
								}
								for (int y = 0; y < nObservations; y++) {
//									int y = pomdp.getObservation(sPrime);
									double obs_p = 0.0;
									if (y == pomdp.getObservation(sPrime)) {
										obs_p = 1.0;
									}
									expr.addTerm(-discount * probTransition[a][s][sPrime] * obs_p, X_n_s_a_y_nPrime[n][s][a][y][nPrime]);
								}
							}
						}
					}
////					if (initial_belief_n_s[nPrime][sPrime]>0) {
//						mainLog.println(nPrime + " " + sPrime + " " + initial_belief_n_s[nPrime][sPrime]);
////					}
					model.addConstr(initial_belief_n_s[nPrime][sPrime], GRB.EQUAL, expr, String.format("c15_%d",constraint_count++));
				}
			}
			
			
			//16 x(n,s,a) = sum_nPrime {x(n,s,a,n'y)}
			for (int n = 0; n < nNodes; n++) {
				for (int s = 0; s < nStates; s++) {
					for (int a = 0; a < nActions; a++) {
						Object action = allActions.get(a);
						for (int y = 0; y < nObservations; y++) {
							expr = new GRBLinExpr();
							expr.addTerm(1, X_n_s_a[n][s][a]);
							
							
							for (int nPrime = 0; nPrime < nNodes; nPrime++) {
								expr.addTerm(-1, X_n_s_a_y_nPrime[n][s][a][y][nPrime]);
							}
							model.addConstr(0, GRB.EQUAL, expr, String.format("c16_%d",constraint_count++));
						}
					}
				}
			}
			
			//17
			for (int n = 0; n < nNodes; n++) {
				for (int a = 0; a < nActions; a++) {
					expr = new GRBLinExpr();
					expr.addTerm(1, X_n_a[n][a]);
					for (int s = 0; s < nStates; s++) {
						expr.addTerm(-1, X_n_s_a[n][s][a]);
					}
					model.addConstr(0, GRB.EQUAL, expr, String.format("c17_%d",constraint_count++));
				}
			}
			
			//18
			for (int n = 0; n < nNodes; n++) {
				expr = new GRBLinExpr();
				expr.addTerm(1, X_n[n]);
				for (int a = 0; a < nActions; a++) {
					expr.addTerm(-1, X_n_a[n][a]);
				}
				model.addConstr(0, GRB.EQUAL, expr, String.format("c18_%d",constraint_count++));
			}
			
			// 19
			for (int n = 0; n < nNodes; n++) {
				for (int y = 0; y < nObservations; y++) {
					for (int nPrime = 0; nPrime < nNodes; nPrime++) {
						expr = new GRBLinExpr();
						expr.addTerm(1, X_n_y_nPrime[n][y][nPrime]);
						for (int s = 0; s < nStates; s++) {
							for (int a = 0; a < nActions; a++) {
								expr.addTerm(-1, X_n_s_a_y_nPrime[n][s][a][y][nPrime]);
							}
						}
						model.addConstr(0, GRB.EQUAL, expr, String.format("c19_%d",constraint_count++));
					}
				}
			}
			
			// 20
//			for (int n = 0; n < nNodes; n++) {
//				for (int a = 0; a < nActions; a++) {
//					expr = new GRBLinExpr();
//					expr.addTerm(-1 * (1-discount), X_n[n]);
//					expr.addTerm(1 * (1-discount), X_n_a[n][a]);
//					expr.addTerm(-1, X_a__n[a][n]);
//					model.addConstr(expr, GRB.GREATER_EQUAL, -1, String.format("c20_%d",constraint_count++));
//				}
//			}

			//21
			for (int n = 0; n < nNodes; n++) {
				for (int y = 0; y < nObservations; y++) {
					for (int nPrime = 0; nPrime < nNodes; nPrime++) {
						expr =  new GRBLinExpr();
						expr.addTerm(-1 * (1-discount), X_n[n]);
						expr.addTerm(1 * (1-discount), X_n_y_nPrime[n][y][nPrime]);
						expr.addTerm(-1, X_nPrime__n__y[nPrime][n][y]);
						model.addConstr(expr, GRB.GREATER_EQUAL, -1, String.format("c21_%d",constraint_count++));
					}
				}
			}
			

			//  22b 
			for (int i = 0; i < nObj; i++) {
				GRBLinExpr expr1 = new GRBLinExpr();
				for (int n = 0; n < nNodes; n++) {
					for (int s = 0; s < nStates; s++) {
						for (int a = 0; a < nActions; a++) {
							//TODO what if action is unavaiable
							// Maybe not to worry since alreay enforece x(n,s,a)=0
							expr1.addTerm(reward[i][s][a], X_n_s_a[n][s][a]);
						}
					}
				}
//				expectedValue
				
				model.addConstr(expr1, GRB.GREATER_EQUAL, lowerBounds[i], "c22b_" + constraint_count++);
				model.addConstr(expr1, GRB.LESS_EQUAL, upperBounds[i], "c22b_" + constraint_count++);
				
//				expr1.addTerm(-1, X_Valuelow[i]);
//				expr1.addTerm(-1, X_Valueup[i]);
				model.addConstr(expr1, GRB.EQUAL, 0, "c22c_" + constraint_count++);
				
			}
			
//////			// 22 original
//			for (int n = 0; n < nNodes; n++) {
//				expr = new GRBLinExpr();
//				for (int a= 0; a < nActions; a++) {
//					expr.addTerm(1, X_a__n[a][n]);
//				}
//				model.addConstr(expr, GRB.EQUAL,1 , String.format("c22c_%d",constraint_count++));
//			}
			
			// 23
			for (int n = 0; n < nNodes; n++) {
				for (int y = 0; y < nObservations; y++) {
					expr = new GRBLinExpr();
					for (int nPrime = 0; nPrime < nNodes; nPrime++) {
						expr.addTerm(1, X_nPrime__n__y[nPrime][n][y]);
					}
					model.addConstr(1, GRB.EQUAL, expr, String.format("c23_%d",constraint_count++));
				}
			}
			
			
			model.write("multi_aaab_gurobi.lp");
			model.optimize();
			if (model.get(GRB.IntAttr.Status)== GRB.Status.INFEASIBLE) {
				mainLog.println("model infeasible");
				model.computeIIS();
				model.write("multi_Infeasible.ilp");		
				model.feasRelax(0, false, true, true);
				model.optimize();
				model.write("multi_aaab_gurobi_relaed.sol");

			}
//			

			model.write("multi_aaab_gurobi.sol");
//			
			System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
			mainLog.println(model.getJSONSolution());
			
	      // Dispose of model and environment
	      model.dispose();
	      env.dispose();
		} catch (GRBException e) {
			
			throw new PrismException("Error solving LP: " +e.getMessage());
		}
    	
    	
    	
    	mainLog.println("Time for computing multi-strategy" + (System.currentTimeMillis() - startTime));
		return StateValues.createFromSingleValue(TypeDouble.getInstance(), 0.0, pomdp);
    }
    public void chekcExpressionMultiStrategy(POMDP<Double> pomdp, List<MDPRewards<Double>> mdpRewardsList, BitSet target,
    		List<MinMax> minMaxList, BitSet statesOfInterest, StateValues ParentoPoints) throws PrismException 
    {
    	int numObjs = minMaxList.size();
		int nStates = pomdp.getNumStates();    	
    	BitSet targetObs = ((POMDPModelChecker) mc).getObservationsMatchingStates(pomdp, target);
		// Check we are only computing for a single state (and use initial state if unspecified)
		if (statesOfInterest == null) {
			statesOfInterest = new BitSet();
			statesOfInterest.set(pomdp.getFirstInitialState());
		} else if (statesOfInterest.cardinality() > 1) {
			throw new PrismNotSupportedException("POMDPs can only be solved from a single start state");
		}	
		
		if (targetObs == null) {
			throw new PrismException("Target for expected reachability is not observable");
		}
		// Find _some_ of the states with infinite reward
		// (those from which *every* MDP strategy has prob<1 of reaching the target,
		// and therefore so does every POMDP strategy)
		MDPModelChecker mcProb1 = new MDPModelChecker(this);
		BitSet inf = mcProb1.prob1(pomdp, null, target, false, null);
		inf.flip(0, pomdp.getNumStates());
		// Find observations for which all states are known to have inf reward
		BitSet infObs = ((POMDPModelChecker) mc).getObservationsCoveredByStates(pomdp, inf);
		//mainLog.println("target obs=" + targetObs.cardinality() + ", inf obs=" + infObs.cardinality());
		
		// Determine set of observations actually need to perform computation for
		// eg. if obs=1 & unknownObs(obs)=true -> obs=1 needs computation
		// eg. if obs=2 & unknownObs(obs)=false -> obs=1 does not need computation
		BitSet unknownObs = new BitSet();
		unknownObs.set(0, pomdp.getNumObservations());
		unknownObs.andNot(targetObs);
		unknownObs.andNot(infObs);
		HashSet<Integer> endStates = new HashSet<Integer>();
		for (int i=0; i<nStates;i++) {
			if (!unknownObs.get(pomdp.getObservation(i))) {
				mainLog.println("end state="+i+"Obs="+pomdp.getObservation(i));
				endStates.add(i);
			}
		}
		
//		if (ParentoPoints == null) {
//			ParentoPoints = checkExpressionParetoMultiObjPOMDP(pomdp, mdpRewardsList, target, minMaxList, statesOfInterest);
////			StateValues sv = checkExpressionParetoMultiObjMDPWithOLS(pomdp, mdpRewardsList, target, minMaxList, null);
////			StateValues sv = mc.checkExpressionParetoMultiObjMDPWithRandomSampling(mdp, mdpRewardsList, target, minMaxList, null);
//		}
		PartiallyObservableMultiStrategy p = new PartiallyObservableMultiStrategy(pomdp, mdpRewardsList, target, minMaxList, statesOfInterest, endStates, unknownObs);
		String prefString = getSettings().getString(PrismSettings.PRISM_WEIGHTS_STRING);
		
		p.computeBounds(prefString, ParentoPoints);
//		p.setObjectiveBounds(new double[]{0,0}, new double []{333.8,333.8});
		p.computeBeliefBasedMultiStrategy();
		
//		p.setObjectiveBounds(new double[]{2.8,2.8}, new double []{4.1,4.1});
//		p.setObjectiveBounds(new double[]{2.8,2.8}, new double []{33,33});
//		p.splitObservations();
//		p.spiltStates(null);
//		p.splitObservations();
//		p.computeMultiStrategyMILP(0);
//		p.displayStates();
//		p.displayTransition();
    }    

}



