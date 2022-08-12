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

import java.io.*;
import java.math.BigInteger;

 class POMDPShield{
	// winning region =  obs to winning belief support. Belief support is represented by a list of large integers.
	// ... This integer, in is binary format, represents a set of states
	// ... The observation is based on Stompy convention 
	private Map<Integer, ArrayList<ArrayList<BigInteger>>> winningRegion;
	private POMDP pomdp;
//	private ArrayList<ArrayList<Integer>> PrismObsToPrismStates ;
	//private ArrayList<ArrayList<Integer>> StompyObsToStompyStates;
	private Map<Integer, ArrayList<Integer>> StompyObsToStompyStates;
	private Map<Integer, Integer> StompyStateToStompyObsIndex ; // get index
//	private int[] PrismObsToStompyObs;
//	private int[] StompyObsToPrismObs;
	private int[] PrismStateToStompyState;
//	private String [] stompyActions;
	private ArrayList<Integer> StompyStateToObs;
	private List<String> mainVarNames;
	private int numLocalObservations;
	private ArrayList<Integer> endStates;
	private int xMIN;
	private int yMIN;
	private int xMAX;
	private int yMAX;
	private List<String> localVarNames;
	public POMDPShield(POMDP pomdp, String winningFile, String translateFile, List<String> varNames, ArrayList<Integer> concreteModelEndStates, int [] primaryStates) {
		winningRegion = new HashMap<Integer, ArrayList<ArrayList<BigInteger>>> ();
		this.pomdp = pomdp;
		this.mainVarNames = varNames;
		StompyStateToStompyObsIndex = new HashMap<Integer, Integer>();
		StompyStateToObs = new ArrayList<Integer> ();
		
		
		loadWinningRegion(winningFile);
		//loadWinningRegionFromFile(winningFile);
//		loadTranslationFromFile(translateFile);
		
		this.endStates = concreteModelEndStates;
		xMIN = primaryStates[0];
		yMIN= primaryStates[1];
		xMAX = primaryStates[2];
		yMAX = primaryStates[3];

	}
	public int getStompyState(int PrismState) {
		int stompyState = -1;
		stompyState = PrismStateToStompyState[PrismState];
		return stompyState;
	}
//	public void loadTranslationFromFile(String translateFrile) 
//	{
//		HashMap<String, Integer> StompyMeaning2State = new HashMap<String, Integer>();
//		// get StompyStateToObs and StompyMeaningToState
//		// get StompyObsToStompyStates
//		try {
////			List<String> varNames = getVarNames();
//			BufferedReader in = new BufferedReader(new FileReader(translateFrile));
//			String str;
//			while((str = in.readLine()) != null) {
////				System.out.println(str + translateFrile);
//
//				str = str.replace("\"", "").replace(":", "").replace("}", ",");
//				str = str.replace("=", "").replace("[", "").replace("]", "");
//				int state = Integer.parseInt(getValueFromLine(str, " state"));
//				int obs= Integer.parseInt(getValueFromLine(str, " obs"));
//				StompyStateToObs.add(obs);
//				String meaning = "";
//				int numberVariables = varNames.size();
//				for (int i = 0; i < numberVariables; i++) {
//					String varName = varNames.get(i);
//					String value = getValueFromLine(str, varName);
//					//meaning += varName + "=" + value + ",";
//					if (i > 0) {
//						meaning += ",";
//					}
//					meaning += value;
//				}
//				//meaning = "(" + meaning.substring(0, meaning.length()-1) + ")" ;
//				StompyMeaning2State.put(meaning, state);
//			}
//			in.close();
//			
////			// build stommpyObsToStompyStates;
////			StompyObsToStompyStates = new ArrayList<ArrayList<Integer>> ();
////			int numStompyStates = StompyStateToObs.size();
////			for (int s = 0; s < numStompyStates; s++) {
////				ArrayList<Integer> StompyStatesPerObservation = new ArrayList<Integer> ();
////				StompyObsToStompyStates.add(StompyStatesPerObservation);
////			}
////			for (int StompyState = 0; StompyState < numStompyStates; StompyState++) {
////				int StompyObs = StompyStateToObs.get(StompyState); 
////				StompyObsToStompyStates.get(StompyObs).add(StompyState); 
////			}
//			translate(StompyMeaning2State);
//		} catch(IOException e) {
//		}
//	}
	public String getValueFromLine(String str, String feature) 
	{	
		System.out.println(str + "|" + feature);
		if (!str.contains(feature)) {
			return "feature not found";
		}

		if (str.contains("!"+feature)) {
			return "false";
		}
		String[] items = str.split("&");
		for (String item: items) {
			if (!item.contains(feature)) {
				continue;
			}
			if (!item.contains("=")) {
				return "true";
			} else {
				String[] values = item.split("=");
				return values[1];
			}
		}
		return "error";
	}	
//		
//		String value = "";
//		int startIndex = str.indexOf(feature) ;
////		System.out
//		if (startIndex + feature.length() < str.length() && str.charAt( startIndex + feature.length()) != '	' ){
//			// int type
//			int endIndex = startIndex + feature.length()  + 1;
//			while (endIndex < str.length()) {
//				char end = str.charAt(endIndex);
//				if (end == ',' || Integer.valueOf(end) == 32 || Integer.valueOf(end) == 9)  { //32: space; 9: horizontal tab
//					break;
//				}
//				endIndex++;
//			}
//			value = str.substring(startIndex + feature.length() , endIndex);
//		}
//		else {
//			//boolean type:
//			if (startIndex > 0 && str.charAt(startIndex - 1) == '!') {
//				value = "false";
//			}
//			value =  "true";
//		}
//		return value;
		
	
	public void translate(HashMap<String, Integer> StompyMeaning2State) 
	{
//		StompyObsToStompyStates = new ArrayList<ArrayList<Integer>> ();
		StompyObsToStompyStates = new HashMap<Integer, ArrayList<Integer>> ();
		int numStompyStates = StompyStateToObs.size();
		for (int state = 0; state < numStompyStates; state++) {
			int StompyObs = StompyStateToObs.get(state);
			if (!StompyObsToStompyStates.containsKey(StompyObs)) {
				StompyObsToStompyStates.put(StompyObs, new ArrayList<Integer> ());
			}
			StompyObsToStompyStates.get(StompyObs).add(state);
		}
		
		// for a state, the index in the list of states which have the same observation
		for (int StompyObs: StompyObsToStompyStates.keySet()) {
			ArrayList<Integer> stompyStates = StompyObsToStompyStates.get(StompyObs);
			for (int index = 0; index < stompyStates.size(); index++) {
				int StompyState = stompyStates.get(index);
				StompyStateToStompyObsIndex.put(StompyState, index);
			}
		}
		
		int nStompyStates = StompyStateToObs.size();
		//		int StompyState = PrismStateToStompyState[sPrime];
		//		int StompyObs = StompyStateToStompyObs[StompyState];
		PrismStateToStompyState = new int[pomdp.getNumStates()];
		HashMap<Integer, ArrayList<Integer>> StompyStateToPrismState = new HashMap<Integer, ArrayList<Integer>>();
		
		for (int s = 0; s < nStompyStates; s++) {
			StompyStateToPrismState.put(s, new ArrayList<Integer>());
		}

		for (int PrismState = 0; PrismState < pomdp.getNumStates(); PrismState++) {
//		for (int PrismState = 30321; PrismState < 33103; PrismState++) {

			String fullMeaning = pomdp.getStatesList().get(PrismState).toStringNoParentheses();
			String meaning = pomdp.getStatesList().get(PrismState).toStringNoParentheses(mainVarNames, localVarNames); //TODO
			System.out.println("Prism state= " + PrismState + " {" + meaning+ "}" + StompyMeaning2State.containsKey(meaning) );	

//			if(StompyMeaning2State.containsKey(meaning) ) {
//				System.out.println("Prism state= " + PrismState + " " + meaning + StompyMeaning2State.containsKey(meaning) );	
//			}
//			
			int StompyState = -1;
			if (StompyMeaning2State.containsKey(meaning)){
				StompyState = StompyMeaning2State.get(meaning);
				System.out.println("Prism state= " + PrismState + " " + getStateMeaning(PrismState)+ " to StompyState => "+ StompyState + " " + meaning);
			} else {
				continue; // prism state has not couterpart in stompystate
			}
			
			PrismStateToStompyState[PrismState] =  StompyState; //key
			
//			System.out.println("Prism state= " + PrismState + " " + getStateMeaning(PrismState)+ " to StompyState => "+ StompyState + " " + meaning);
			
			ArrayList<Integer> tp = StompyStateToPrismState.get(StompyState);
			tp.add(PrismState);
			StompyStateToPrismState.put(StompyState, tp);
			
//			PrismObsToStompyObs[pomdp.getObservation(PrismState)] = StompyStateToObs.get(StompyState);// StompyStateToObs[StompyState];
//			System.out.println(pomdp.getObservation(PrismState) + "xxx" + StompyStateToObs.get(StompyState));
//			StompyObsToPrismObs[StompyStateToObs.get(StompyState)] = pomdp.getObservation(PrismState);
		}			
		
//		for (int s = 0; s < nPrismStates; s++) {
//			System.out.println("Prism State " + s + getStateMeaning(s) + " Prism Obs " + pomdp.getObservation(s) + 
//					" Stompy State " + PrismStateToStompyState[s] 
//							+ " Stompy Obs "+ PrismObsToStompyObs[pomdp.getObservation(s)]  +"alt " + StompyStateToObs.get(PrismStateToStompyState[s]) 
//							+ getStateMeaningAbstract(s));
//		}
//		
		
//		for (int i = 0; i < pomdp.getNumObservations(); i++) {
//			ArrayList<Integer> StompyStatesPerObservation = new ArrayList<Integer> (); // dummy
////			StompyObsToStompyStates.add(StompyStatesPerObservation); // dummy
////
////			ArrayList<Integer> PrismStatesPerObservation = new ArrayList<Integer> ();
////			PrismObsToPrismStates.add(PrismStatesPerObservation);
//		}
//		
//		for (int StompyState = 0; StompyState < nStompyStates; StompyState++) {
//			int StompyObs = StompyStateToObs.get(StompyState); // dummy
//			StompyObsToStompyStates.get(StompyObs).add(StompyState); // dummy

//			if (nStompyStates == nPrismStates) {
//				int PrismState = StompyStateToPrismState.get(StompyState).get(0);
//				int PrismObs = pomdp.getObservation(PrismState);
//				PrismObsToPrismStates.get(PrismObs).add(PrismState);
//			}else {
////				asddddddd
//			}
//		}

	}

	public void loadWinningRegion(String winngingFile) 
	{
		try {
			StompyStateToObs = new ArrayList<Integer> ();
			HashMap<String, Integer> StompyMeaning2State = new HashMap<String, Integer>();

			BufferedReader in = new BufferedReader(new FileReader(winngingFile));
			String str;
//			long[] observationSizes = new long[pomdp.getNumObservations()];
			long loadingState = 0;
			int observation = 0;
			String preamblestream;
			//winningregion
			while((str = in.readLine()) != null) {

				if(str.length()>0 && str.charAt(0)=='#') {
					continue;
				}
				if(loadingState == 0) {
					//reading preamble
					loadingState = 1;
				} else if (loadingState == 1) {
					if (str.indexOf(":winningregion") >= 0) {
						loadingState = 2;
					}else{
						preamblestream = str;
					}
				} else if (loadingState == 2) {
					String[] entries = str.split(" ");
					numLocalObservations = entries.length;
//					for (int ob = 0; ob < entries.length; ob++) {
//						observationSizes[ob] = Long.parseLong(entries[ob]);
//					}
					// wr = winningRegion(observationSizes);
					loadingState = 3;
				} else if (loadingState == 3) { //eg. 84 1154891893868338944 139664365006618624;84 1442277793550311168 139664365006618624;
					
					if (str.indexOf(":state information") >=0){
						loadingState = 4;
						continue;
					}
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
				} else if (loadingState == 4) {
//					str = str.replace("\"", "").replace(":", "").replace("}", ",");
//					str = str.replace("[", "").replace("]", "").replace(9, "");
					str = str.replaceAll("[ \t]", "").replace("[", "").replace("]", "").replace("obs=", "observation=");
					int state = Integer.parseInt(getValueFromLine(str, "state"));
					int obs= Integer.parseInt(getValueFromLine(str, "observation"));
					StompyStateToObs.add(obs);
					String meaning = "";
					int numberVariables = mainVarNames.size();
					ArrayList<String> tempLocalVarNames = new ArrayList<String> (); 
					for (int i = 0; i < numberVariables; i++) {
						String varName = mainVarNames.get(i);
						if(!str.contains(varName)) {
							continue;
						}
						if (localVarNames == null) {
							tempLocalVarNames.add(varName);
						}
						String value = getValueFromLine(str, varName);
						//meaning += varName + "=" + value + ",";
						if (i > 0) {
							meaning += ",";
						}
						meaning += value;
					}
					if (localVarNames == null) {
						localVarNames = new ArrayList<String>(tempLocalVarNames);
					}
					//meaning = "(" + meaning.substring(0, meaning.length()-1) + ")" ;
					System.out.println("Stompy " + state + " stomoy obs "+ obs + "{" + meaning + "}");
					StompyMeaning2State.put(meaning, state);
				}
			}
			if (StompyMeaning2State.size() > 0) {
				System.out.println("load translation");
				translate(StompyMeaning2State);
			}else {
				System.out.println("Fail to load translation");
			}
			if (winningRegion.size() == 0) {
				System.out.println("\n\n\n\nFail to load winning region\n\n\n\n");
			}else{
				System.out.println("Winning region loaded.");
			}
//			displayWinningRegion();
//			displayTranslation();
			System.out.println("main varNames" + mainVarNames.toString());
			System.out.println("local varNames" + localVarNames.toString());
			in.close();
		} catch(IOException e) {
		}
	}
	
//	public void loadWinningRegionFromFile(String winngingFile) 
//	{
////		System.out.println("loadWinningRegionFromFile");
//		try {
//			BufferedReader in = new BufferedReader(new FileReader(winngingFile));
//			String str;
////			long[] observationSizes = new long[pomdp.getNumObservations()];
//			long state = 0;
//			int observation = 0;
//			String preamblestream;
//			//winningregion
//			while((str = in.readLine()) != null) {
//
//				if(str.length()>0 && str.charAt(0)=='#') {
//					continue;
//				}
//				if(state == 0) {
//					//reading preamble
//					state = 1;
//				} else if (state == 1) {
//					if (str.indexOf(":winningregion") >= 0) {
//						state = 2;
//					}else{
//						preamblestream = str;
//					}
//				} else if (state == 2) {
//					String[] entries = str.split(" ");
//					numLocalObservations = entries.length;
////					for (int ob = 0; ob < entries.length; ob++) {
////						observationSizes[ob] = Long.parseLong(entries[ob]);
////					}
//					// wr = winningRegion(observationSizes);
//					state = 3;
//				} else if (state == 3) { //eg. 84 1154891893868338944 139664365006618624;84 1442277793550311168 139664365006618624;
//					if (str.length() == 0) {
//						++observation;
//						continue;
//					}
//					String[] entries = str.split(";");
//					for (int i = 0; i < entries.length; i++) {
//						String[] subEntries = entries[i].split(" "); //eg. 84 1154891893868338944 139664365006618624;
//						ArrayList<BigInteger> bv = new ArrayList<BigInteger> ();
//						long obsSize = Long.parseLong(subEntries[0]); // eg. 84
//						for (int j = 1; j < subEntries.length; j++) {
//							BigInteger beliefSupport = new BigInteger(subEntries[j]);
//							//String beliefSupport = subEntries[j]; //eg. 1154891893868338944
//							bv.add(beliefSupport);
//						}
//						updateWinningRegion(winningRegion, observation, bv);
//						//updateWinningRegion(winningRegion,  StompyObsToPrismObs[observation], bv);
//					}
//					++observation;
//				}
//			}
//			System.out.println("Winning region loaded new.");
//			in.close();
//		} catch(IOException e) {
//		}
//	}
	public void displayWinningRegion() {
		//for (int observation = 0; observation < winningRegion.size(); observation++) {
		System.out.println("winning region:");
		for (int observation: winningRegion.keySet()) {
			ArrayList<ArrayList<BigInteger>> existingWinningSupports = winningRegion.get(observation);
			if (existingWinningSupports == null) {
				System.out.println("Obs = " + observation + " NULL");
			} else {
				System.out.print ("Obs = " + observation + " ");
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
	public void displayStompyMeanin() {
		;
	}
	public void displayTranslation() {
		System.out.println("state information");
		for (int state: StompyStateToStompyObsIndex.keySet()) {
			System.out.println("stompy state" + state+ " obs"+ StompyStateToObs.get(state) + "stompy obs index" + StompyStateToStompyObsIndex.get(state));
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
	
	public HashSet<Integer> filterPrimaryStates(HashSet<Integer> PrismStates) {
		HashSet<Integer> primaryStates = new HashSet<Integer>();
		for (int s : PrismStates) {
			
			int x = getAX(s);
			int y = getAY(s);
//			System.out.println("state"+ s + " " + xMIN + " x=" +  x  + " "  + xMAX  + " " + yMIN + " y="+ y+" " + yMAX  + (xMIN <= x && x <= xMAX && yMIN <= y && y <= yMAX ));
//			System.out.println(getStateMeaning(s));
			if ((xMIN <= x && x <= xMAX && yMIN <= y && y <= yMAX )) {
				primaryStates.add(s);
			}
		}
		return primaryStates;
	}
	public int getIndex(int StompyState, int StompyObs) {
		return StompyStateToStompyObsIndex.get(StompyState);
//
//		if (StompyStateToStompyObsIndex.containsKey(StompyState)) {
//			return StompyStateToStompyObsIndex.get(StompyState);
//		}
//		else {
//			ArrayList<Integer> stompyStates = StompyObsToStompyStates.get(StompyObs);
//			int index = stompyStates.indexOf(StompyState);
//			StompyStateToStompyObsIndex.put(StompyState, index);
//			return index;
//		}
	}
	
	public boolean isSetOfStatesWinning(HashSet<Integer> PrismStates) 
	{
//		System.out.println("why is this" + PrismStates);
		PrismStates = filterPrimaryStates(PrismStates);
//		System.out.println("and why is this" + PrismStates);

		// convert to Stompy obs 
		HashMap<Integer, HashSet<Integer>> StompyObs2StompyStates = new HashMap<Integer, HashSet<Integer>> ();
		for (int state : PrismStates) {
//			System.out.println( "State" + state + " " + getStateMeaning(state)+ "");

			int StompyState = PrismStateToStompyState[state];
			if(StompyState < 0) {
				// this state is not modeled in this shield
				System.out.println("? this state is not model");
				continue;
			}
			int StompyObs = StompyStateToObs.get(StompyState);
//			System.out.println( "State" + state + " " + getStateMeaning(state) + "stompy State" + StompyState+ " stompy obs" +  StompyObs);

			if (winningRegion.get(StompyObs) == null) { // if not winning support for this obs
				if (!endStates.contains(state)) { // logic to do
//					System.out.println("!endStates.contains(state");
					return false;
				}
			}
			
			//int index = StompyObsToStompyStates.get(StompyObs).indexOf(StompyState);
			int index = getIndex(StompyState, StompyObs);
//			System.out.println("state " + state + " stompy state " + StompyState + " stompy obs" + StompyObs + " index " +index);
//			displayState(state)
			if (StompyObs2StompyStates.get(StompyObs) == null) {
				StompyObs2StompyStates.put(StompyObs, new HashSet<Integer> ());
			}
			//HashSet<Integer> StompyStates = new HashSet<Integer> ();
			HashSet<Integer> StompyStates = StompyObs2StompyStates.get(StompyObs); //wrong 2 opened an new one every time
			StompyStates.add(index); 
			StompyObs2StompyStates.put(StompyObs, StompyStates);
		}

		for (int StompyObs : StompyObs2StompyStates.keySet()) {
			if (winningRegion.get(StompyObs) == null) { // if not winning support for this obs
				return true; // all states are in end states; other wise it would return false above
			}
			ArrayList<BigInteger> beliefSupportInBigInteger = getBigIntegerFromStateIndices(StompyObs2StompyStates.get(StompyObs), StompyObs);
//			System.out.println("Big Integer = "+ beliefSupportInBigInteger.toString() + "obs" + StompyObs + " IsWinning" + isSupportWinning(beliefSupportInBigInteger, StompyObs));
			if (!isSupportWinning(beliefSupportInBigInteger, StompyObs)) {
				return false;
			}
		}
		
		return true;
	}
	public ArrayList<BigInteger> getBigIntegerFromStateIndices( HashSet<Integer> stateIndices, int StompyObs)
	{
//		System.out.println("??? candidate index" + stateIndices);
		ArrayList<BigInteger> nextSupport = new ArrayList<BigInteger> ();
		ArrayList<ArrayList<BigInteger>> beliefSupports = winningRegion.get(StompyObs);
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
	public boolean isSupportWinning(ArrayList<BigInteger> nextSupport, int StompyObs) 
	{
		boolean isThisObsWinning = false;
		ArrayList<ArrayList<BigInteger>> beliefSupports = winningRegion.get(StompyObs);
		for (ArrayList<BigInteger> winning : beliefSupports) {
//			System.out.println("next Support " + nextSupport +   " winning" + winning + " is first belong to second " + isFirstBeliefSupportSubsetOfSecond(nextSupport, winning));
			if (isFirstBeliefSupportSubsetOfSecond(nextSupport, winning)) {
				isThisObsWinning = true;
				break;
			}
		}
		return isThisObsWinning;
	}
	
	public String getStateMeaning(int state) 
	{
		return pomdp.getStatesList().get(state).toString(mainVarNames);
	}
	public int getAX(int state) {
		int ax = -1;
		String meaning = getStateMeaning(state);
		meaning.replace("ax", "x");
		String [] meanings = meaning.split(",");
		for (int i = 0; i < meanings.length; i++) {
			if(meanings[i].contains("x")){
				String [] values = meanings[i].split("=");
				ax  = Integer.parseInt(values[1]);
				break;
			}
		}
		return ax;
	}
	public int getAY(int state) {
		int ay = -1;
		String meaning = getStateMeaning(state).replace(")", "").replace("ay", "y");
		String [] meanings = meaning.split(",");
		for (int i = 0; i < meanings.length; i++) {
			if(meanings[i].contains("y")){
				String [] values = meanings[i].split("=");
				ay  = Integer.parseInt(values[1]);
				break;
			}
		}
		return ay;
	}
}

 class POMCPNode{
	private int id;
	private boolean isONode;
	private POMCPNode parent;
	private int h;
	private Object hAction;
	private double v;
	private double n;
	private POMCPBelief belief;
	private HashMap<Integer, POMCPNode> children;
	private HashSet<Object> illegalActions; 
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
		//this.illegalActions = null;
	}
	public void addIllegalActions(Object action) {
		if (illegalActions == null) {
			illegalActions = new HashSet<Object>();
		}
		illegalActions.add(action);
	}
	public boolean isActionIllegal(Object action) {
		if (illegalActions == null) {
			return false;
		}
		return illegalActions.contains(action);
	}
	public HashSet<Object> getIllegalActions(){
		return illegalActions;
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
	public HashMap<Integer, POMCPNode> getChildren(){
		return children;
	}
	public void addChild(POMCPNode node, int index) {
		if (children == null) {
			children = new HashMap<Integer, POMCPNode> (); 
		}
		children.put(index, node);
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
	public POMCPNode getChildByActionIndex(int index) {
		if (children != null && children.containsKey(index)) {
			return children.get(index);
		}else {
			POMCPNode child = new POMCPNode();
			child.setH(index);
			child.setParent(this);
			this.addChild(child, index);
			return child;
		}
	}
	public boolean checkChildByObservationIndex(int index) 
	{
		if (children == null ) {
			return false;
		}
		return children.containsKey(index);
	}
	public POMCPNode getChildByObservationIndex(int index) 
	{
		if (children == null || !children.containsKey(index)) {
			POMCPNode child = new POMCPNode();
			child.setH(index);
			child.setParent(this);
			this.addChild(child, index);
			return child;	
		}
		return children.get(index);
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
	 public void copy(POMCPBelief orginal) {
		 for (int state: orginal.getParticles()) {
			 addParticle(state);
		 }
	 }
	 public boolean empty() {
		 return particles.size() == 0;
	 }
	 public ArrayList<Integer> getParticles(){
		 return particles;
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
  class stepReturn{
	 private int state;
	 private int observation;
	 private double reward;
	 private boolean terminal;
	 stepReturn(int state, int observation, double reward, boolean termial)
	 {
		 this.state = state;
		 this.observation = observation;
		 this.reward = reward;
		 this.terminal = termial;
	 }
	 public int getState() 
	 {
		 return state;
	 }
	 public int getObservation() 
	 {
		 return observation;
	 }
	 public double getReward() 
	 {
		 return reward;
	 }
	 public boolean getTerminal() 
	 {
		 return terminal;
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
	private int maxDepth;
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
	private Map<Integer, HashSet<Integer>> stateSuccessors;
	private List<String> varNames;
	private ArrayList<POMDPShield> localShields;
	private POMDPShield mainShield;
	private boolean useLocalShields;
	public PartiallyObservableMonteCarloPlanning(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest, ArrayList<Integer> endStates,
			 double constant, int maxDepth) 
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
		this.statesOfInterest = statesOfInterest;
		this.target = target;
		this.min = min;
		this.endStates = endStates;
		this.c = constant; //constant
		this.numSimulations =  Math.pow(2, 15);
		this.verbose = 0;
		this.gamma = 0.95;
		this.timeout = 10000;
		this.K = 10000;
		this.maxDepth = maxDepth;
		initializePOMCP();
	}
	public void initializePOMCP() {
		this.nodeCount = 0;
		this.TreeDepth = 0;
		this.PeakTreeDepth = 0;
		initializeVarNames();
		getAllActions();
		setActionToIndex();
		//root.getBelief().disaplyParticles();
		initialUCB(10000, 100);
		this.shieldLevel = 0; 
		this.useLocalShields = false;
		stateSuccessors = new HashMap<Integer, HashSet<Integer>> ();
	}
	public void resetRoot() {
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
	}
	
	public void loadMainShield(String shieldDir) {
		File files = new File(shieldDir);
		File[] array = files.listFiles();
		for (int i = 0; i < array.length; i++) {
			if (!array[i].isFile() ) {
				continue;
			}
			File file = array[i];
			String fileName = array[i].getName(); 
			if (!fileName.contains("centralized")) {
				continue;
			}
			System.out.println("++++Initialize main shield " );
			System.out.println(fileName);
			String[] parameters = fileName.split("_");
			int[] pStates = {0, 0, Integer.parseInt(parameters[1]), Integer.parseInt(parameters[1])}; 
			String winning = file.toString();
			mainShield = new POMDPShield(pomdp, winning, winning, varNames, endStates, pStates);
		}
	}
	
	public void loadShiled() 
	{
		File files = new File(".");
		File[] array = files.listFiles();
		for (int i = 0; i < array.length; i++) {
			String path = array[i].getPath();
			if (path.contains("winningregion")){
				loadMainShield(path);
				loadLocalShield(path);
				return;
			}
		}
		System.out.println("Fail to find directory winningregion");
	}
	
	public void setUseLocalShields(boolean use) 
	{
		useLocalShields = use;
	}
	public void setShieldLevel(int level) 
	{
		shieldLevel = level;
	}
	public void loadLocalShield(String shieldDir) {
		localShields = new ArrayList<POMDPShield> ();
		File files = new File(shieldDir);
		File[] array = files.listFiles();
		for (int i = 0; i < array.length; i++) {
			if (!array[i].isFile() ) {
				continue;
			}
			File file = array[i];
			String fileName = array[i].getName(); 
			if (!fileName.contains("factor")) {
				continue;
			}
			System.out.println("++++Initialize shield " + localShields.size());
			System.out.println(fileName);
			String[] parameters = fileName.split("-");
			int[] pStates = {Integer.parseInt(parameters[2]), Integer.parseInt(parameters[3]), Integer.parseInt(parameters[4]), Integer.parseInt(parameters[5])}; 
			String winning = file.toString();
			POMDPShield localShield = new POMDPShield(pomdp, winning, winning, varNames, endStates, pStates);
			localShields.add(localShield);
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
		for (int s = 0; s < pomdp.getNumStates();s++) {
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
	public POMCPNode getRoot() 
	{
		return root;
	}
	
	public void update(Object action, int obs) 
	{
		// update/ prune tree given real action and observation
		int actionIndex = getActionIndex(action);
		POMCPNode qnode = root.getChildByActionIndex(actionIndex);
		POMCPNode vnode = qnode.getChildByObservationIndex(obs);
		invigorateBelief(root, vnode, action, obs);
		vnode.clear();
		root = vnode;
	}
//	
	public boolean Update(Object action, int obs) {
		
		POMCPBelief beliefs = new POMCPBelief();
		int actionIndex = getActionIndex(action);
		POMCPNode qnode = root.getChildByActionIndex(actionIndex);
		boolean isVnode = qnode.checkChildByObservationIndex(obs);
		if (isVnode) {
			POMCPNode vnode = qnode.getChildByObservationIndex(obs);
			if (verbose > 1) {
				System.out.println("Matched " + vnode.getBelief().size() + " states");
			}
			beliefs.copy(vnode.getBelief()) ;
		}
		else {
			System.out.println("No matching node found");
		}
		
		if(beliefs.empty() && (!isVnode || qnode.getChildByObservationIndex(obs).getBelief().empty()) ) {
			return false;
		}
		
		int state = 0;
		if (isVnode && ! qnode.getChildByObservationIndex(obs).getBelief().empty() ) {
			POMCPNode vnode = qnode.getChildByObservationIndex(obs);
			if (! vnode.getBelief().empty()) {
				state = vnode.getBelief().getParticles().get(0);
			}
		} else {
			state = beliefs.getParticles().get(0);
		}
		
		POMCPNode newRoot = new POMCPNode();
		expand(newRoot, state);
		newRoot.setBelief(beliefs);
		invigorateBelief(root, newRoot, action, obs);
		setRoot(newRoot);
		return true;
	}
	
	public void invigorateBelief(POMCPNode parent, POMCPNode child, Object action, int obs) 
	{
		// fill child belief with particles
		int childBeliefSize = child.getBelief().size();
		while (childBeliefSize < K) {
			int s = parent.getBelief().sample();
			stepReturn sord = step(s, action);
			int nextState = sord.getState();
			int obsSample = sord.getObservation();
			if (obsSample == obs) {
				if (!child.getBelief().getUniqueStatesInt().contains(nextState)) {
				}
				child.addBeliefParticle(nextState);
				childBeliefSize += 1;
			}
		}
	}
	
	public stepReturn step(int state, Object action)
	{
		if(!pomdp.getAvailableActions(state).contains(action)) {
			System.out.print("error step " + action + state);
			return new stepReturn(0, -1, -1, true);
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
		double reward = mdpRewards.getTransitionReward(state, choice) + mdpRewards.getStateReward(state); // to check if no shield what is the cost function
		if (min) {
			reward *= -1;
		}
		boolean done = false; // whether next state is terminal
		if (endStates.contains(nextState)){
			done = true;
			reward += mdpRewards.getStateReward(nextState) * gamma;
		}
		return new stepReturn(nextState, obs, reward, done);
	}
	public Object getDefaultAction() {
		int state = root.getBelief().sample();
		List<Object> availableActions = pomdp.getAvailableActions(state);
//		System.out.println("state" + state + "default action" + availableActions.get(0) + ( availableActions.size() == 1));
		return availableActions.get(0);
	}
	public Object selectAction() 
	{
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
		for (int n = 0; n < numSimulations; n++) {
			int state = root.getBelief().sample();
			if(!root.getBelief().getUniqueStates().get(state)){
				System.out.println("err" + state);
				root.getBelief().displayUniqueStates();
			}
			if (verbose >= 2 ) {
				System.out.println("=============Start UCT search  sample state" + state + " num Seracrh" + n);
			}
			TreeDepth = 0;
			PeakTreeDepth =0;
			double reward = simulateV(state, root);
			if (verbose >= 2 ) {
				System.out.println("==MCTSMCT after Num Simulation = " + n);
				System.out.println("MCTSMCTS");
				displayValue(1);
				System.out.println("===");
			}
		}
		if (verbose >=1) {
			System.out.println("finishing all " + numSimulations + "simulations");
			displayValue(2);
		}
	}
	public double simulateV(int state, POMCPNode vnode)
	{
		// original code would do actionSelectionFirst, and then do the add particle for only the first layer
		PeakTreeDepth = TreeDepth;
		if (vnode.getChildren() == null) {
			expand(vnode, state);
		}
		// TODO check later for shielding logic
		// For shield 1; add sample for only first layer
		if (TreeDepth == 1 && shieldLevel < 4) {
			vnode.getBelief().addParticle(state);
		}
		int actionIndex = GreedyUCB(vnode, true);

		// For shield 4; add sample for every layer		
		if (shieldLevel == 4) {
			vnode.getBelief().addParticle(state);
			if (shieldLevel == 4 && !isSetOfStatesWinning(vnode.getBelief().getUniqueStatesInt())) {
//				System.out.println(vnode.getBelief().getUniqueStatesInt() + "is not winning" + TreeDepth);
				POMCPNode qparent = vnode.getParent();
				Object parentAction = allActions.get(qparent.getH());
				POMCPNode vparent = qparent.getParent();
				vparent.addIllegalActions(parentAction);
				if (verbose >= 5) {
					System.out.println("Currnet Node=" + vnode.getID() + " Current belief support" + vnode.getBelief().getUniqueStatesInt()  );
					System.out.println("Currenting belief support is not winning. ");
					System.out.println("Action lead to this node= " + parentAction);
					System.out.println("shield level" + shieldLevel +" shielded action: " + parentAction 
							+ "\n adding to illegal actions for it parent node " + vparent.getID() 
							+ " parent belief support" +  vparent.getBelief().getUniqueStatesInt());
				}
//				System.out.println("after" + vparent.getIllegalActions());
			} else {
				if (verbose >= 5) {
					System.out.println("safe. Current Belief Support" + vnode.getBelief().getUniqueStatesInt() + " vnode ID="+ vnode.getID() );
				}
			}
		}
		POMCPNode qnode = vnode.getChildByActionIndex(actionIndex);
		double totalReward = simulateQ(state, qnode, actionIndex);
		vnode.increaseV(totalReward);
		vnode.increaseN(1);
		return totalReward;
	}
	public double simulateQ(int state, POMCPNode qnode, int actionIndex) 
	{
		double delayedReward = 0;
		Object action = allActions.get(actionIndex);
		stepReturn sord = step(state, action);
		int nextState = sord.getState();
		int observation = sord.getObservation();
		double immediateReward = sord.getReward();
		boolean done = sord.getTerminal();
		
		if (verbose >= 3) {
			System.out.println("uct action=" + action + " reward=" + immediateReward );
			displayState(nextState);
		}
		
		state = nextState; 
		POMCPNode vnode = null;
		if (qnode.checkChildByObservationIndex(observation)){
			vnode = qnode.getChildByObservationIndex(observation);
		}

		int paraExpandCount = 1;
		if (vnode == null && !done && qnode.getN() >= paraExpandCount) {
			vnode = new POMCPNode();
//			expand(vnode, state);
			vnode = expandNode(state);			
			vnode.setH(observation);
			updateNodeCount();
			vnode.setParent(qnode);
			qnode.addChild(vnode, observation);
		}
		
		if(!done) { // 0: ! terminal
			TreeDepth++;
			if(vnode != null) {
				delayedReward = simulateV(state, vnode);
			}
			else {
				delayedReward = rollout(state);	
			}
			TreeDepth--;
		}
		
		double totalReward = immediateReward + gamma * delayedReward;
		qnode.increaseN(1);
		qnode.increaseV(totalReward);
		return totalReward;
	
	}
//	
	public POMCPNode expandNode(int state)
	{
		POMCPNode vnode = new POMCPNode ();
		vnode.addBeliefParticle(state);
		List <Object> availableActions = getLegalActions(state);
		for (Object action : availableActions) {
			int actionIndex = getActionIndex(action);
			POMCPNode qnode = new POMCPNode();
			qnode.setH(actionIndex);
			qnode.setHAction(action, true);
			qnode.setParent(vnode);
			qnode.setID(nodeCount);
			nodeCount += 1;
			vnode.addChild(qnode, actionIndex);
		}
		return vnode;
	}
	
	public void expand(POMCPNode parent, int state)
	{	
		List <Object> availableActions = getLegalActions(state);
		for (Object action : availableActions) {
			POMCPNode newChild = new POMCPNode ();
//			if (shieldLevel == 2 && TreeDepth == 0 && parent.isActionIllegal(action)) {
//				 continue;
//			}
			if (shieldLevel == 2 && TreeDepth == 0 && isActionShieldedForNode(parent, action)) {
				parent.addIllegalActions(action);
				continue;
			}
			int actionIndex = actionToIndex.get(action);
			newChild.setH(actionIndex);
			newChild.setHAction(action, true);
			newChild.setParent(parent);;
			newChild.setID(nodeCount);
			updateNodeCount();
			parent.addChild(newChild, actionIndex);
		}
		if (parent.getChildren() == null) {
			System.out.println("May need to add default action");
		}

	}
	
	public double rollout(int state)
	{
		double totalReward = 0;
		double discount = 1;
		boolean done = false;
		if (verbose >= 3) {
			System.out.println("starting rollout");
		}
		int numStep = 0;
		while (!done && numStep + TreeDepth < maxDepth  ) {
			if(discount < e) {
				break;
			}
			List <Object> availableActions = getLegalActions(state);
			if (availableActions.size() <= 0) {
				return 0;
			}
			Random rnd = new Random();
			Object randomAction = availableActions.get(rnd.nextInt(availableActions.size()));
			stepReturn sord = step(state, randomAction);
			int nextState = sord.getState();
			double reward = sord.getReward();
			done = sord.getTerminal();

			if (verbose >= 4) {
				System.out.println("verbose= "+ verbose + " state= " + state + " rollout action=" + randomAction + " reward=" + reward + " discountR=" + reward*discount + " depth=" + numStep + " totalR=" + totalReward);
				displayState(nextState);
			}
			
			totalReward += reward * discount;
			discount *= gamma;
			numStep++;
			state = nextState;
		}
		if (verbose >= 3) {
			System.out.println("Ending rollout after " + numStep + "steps, with total reward" + totalReward );
		}
		return totalReward;
	}
	
	public List<Object> getLegalActions(int state)
	{
		List <Object> availableActions = pomdp.getAvailableActions(state);
		for (int a = availableActions.size() -1 ; a >= 0; a--) {
			if (step(state,  availableActions.get(a)).getReward() < -200 ) {
//				System.out.println("========\nstate " + state + "action" + availableActions.get(a));
//				displayState(state);
				availableActions.remove(a);
			}
		}
		return availableActions;
	}
//	public List<Object> getAllowedActions(POMCPNode node){
//		List<Object> allowedActions = getLegalActions();
//	}
	
	public int GreedyUCB(POMCPNode vnode, boolean ucb) 
	{
		ArrayList<Integer> besta = new ArrayList<Integer> ();
		double bestq = Double.NEGATIVE_INFINITY;
		
//		ArrayList<POMCPNode> children = vnode.getChildren();
		HashMap<Integer, POMCPNode> children = vnode.getChildren();
		
		ArrayList<Integer> actionIndexCandidates = new ArrayList<Integer>(); // if all actions are shielded, randomly pick an action
		for (int i: children.keySet()) {
			actionIndexCandidates.add(i);
			POMCPNode child = children.get(i);
			int actionIndex = child.getH();
			Object action = allActions.get(actionIndex);
			
			if ( !ucb  && shieldLevel == 1  && isActionShieldedForNode(vnode, action) ) { // shiled only apply to the most up level
//				System.out.println("shield Level = "+shieldLevel+ " Shielded Action = "  + action);
				continue;
			}
			if (shieldLevel == 3 && vnode.isActionIllegal(action)) {
//				System.out.println("shield level" + shieldLevel + " known illegal action" 
//									+ action +" for node " + vnode.getID() + " belief support" 
//									+ vnode.getBelief().getUniqueStatesInt());
				continue;
			}
			if (shieldLevel == 3 && isActionShieldedForNode(vnode, action)) {
//				System.out.println("shield level" + shieldLevel +" shielded action: " + action 
//									+ "\n adding to illegal actions for node " + vnode.getID() 
//									+ " belief support" +  vnode.getBelief().getUniqueStatesInt());
				vnode.addIllegalActions(action);
				continue;
			}
			
			if (shieldLevel == 4 && vnode.isActionIllegal(action)) {
//				System.out.println("shield level" + shieldLevel + " known illegal action" 
//						+ action +" for node " + vnode.getID() + " belief support" 
//						+ vnode.getBelief().getUniqueStatesInt());
				continue;
			}
			
			if (child.getN() == 0) {
				return child.getH();
			}
			
			double child_UCT_V = child.getV() / child.getN();
			if(ucb) {
				int N = (int) vnode.getN();
				int n = (int) child.getN();
				double ucb_value = fastUCB(N, n);
				child_UCT_V += ucb_value;
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
//		if (besta.size() == 0) {
//			System.out.println("b"+besta.size());
//			System.out.println("D"+ actionIndexCandidates.size());
//			 HashSet<Integer> atp = vnode.getBelief().getUniqueStatesInt();
////			 System.out.println("is winning= " + isSetOfStatesWinning(atp));
//			 for (int a : atp) {
//				 System.out.println(a + getStateMeaning(a) + getStompyState(a));
//			 }
//			 POMCPNode qParent = vnode.getParent();
//			 Object qAction = qParent.getHAction();
//			 POMCPNode vParent = qParent.getParent();
////			 verbose = 9;
////			 setVerbose(9);
////			 System.out.println(vParent.getBelief().getUniqueStatesInt() + "is vparent winning= " + isSetOfStatesWinning(vParent.getBelief().getUniqueStatesInt()));
////			 System.out.println(qAction + "is action shielded" + isActionShieldedForNode(vParent, qAction));
//		}
		if (besta.size() > 0) {
			int actionIndex = besta.get(rnd.nextInt(besta.size()));
			return actionIndex;
		}
		else {
			int actionIndex = actionIndexCandidates.get(rnd.nextInt(actionIndexCandidates.size()));
//			System.out.println("states" + vnode.getBelief().getUniqueStates());
			return actionIndex;
		}
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
				HashMap<Integer, POMCPNode> children = cur.getChildren();
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
		System.out.println("Belief ");
		for (int s = 0; s < numStates; s++) {
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
	public void initializeVarNames() 
	{
		varNames = new ArrayList<String>();
		for (int i =0; i < pomdp.getVarList().getNumVars(); i++) {
			varNames.add(pomdp.getVarList().getName(i));
		}
	}
	public List<String> getVarNames()
	{
		return varNames;
	}
	public String getStateMeaning(int state) 
	{	
		return pomdp.getStatesList().get(state).toString(varNames);
	}
	public void displayState(int state) 
	{
		List<String> varNames = getVarNames();
		System.out.println("s=" + state + pomdp.getStatesList().get(state).toString( varNames));
		List<Object> availableActions = pomdp.getAvailableActions(state);
//		System.out.println("Available actions");
//		for (Object a: availableActions) {
//			System.out.print(a + " ");
//		}
//		System.out.println();
	}
	public int getStompyState(int PrismState) {
		return mainShield.getStompyState(PrismState);
	}
	public HashSet<Integer> getStompyBeliefSupport(HashSet<Integer> PrismBeliefSupport){
		HashSet<Integer> StompyBeliefSupport = new HashSet<Integer> ();
		for (int PrismState : PrismBeliefSupport) {
			StompyBeliefSupport.add(mainShield.getStompyState(PrismState));
		}
		return StompyBeliefSupport;
	}
	public HashSet<Integer> getRootBeliefSupportPrism() {
		return root.getBelief().getUniqueStatesInt();
	}
	public HashSet<Integer> getRootBeliefSupportStompy(){
		return getStompyBeliefSupport(getRootBeliefSupportPrism());
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
//			if ( d % 2 == 0) {
				System.out.println("MCTS layer" + d);
//			}
			for (int i =0; i < size; i++) {
				POMCPNode node = queue.poll();
				displayNode(node, d);
				displayNodeActions(node, d);
				System.out.println("");
				HashMap<Integer, POMCPNode> children = node.getChildren();
				if (children != null) {
					for(POMCPNode child : children.values()) {
						if (child.getN() > 0) {
							queue.offer(child);
						}
					}
				}
			}
		}
	}
	public void displayNode(POMCPNode node, int depth)
	{
		String info = "";
		if (node.isONode()){
			info +="Id=" + node.getID()+ "depth" + depth  + " o=" + node.getH() + " vmean=" + (node.getV()/node.getN()) + " v=" + node.getV() + " n=" + node.getN() +" Belief Support=" + node.getBelief().getUniqueStatesInt();
		}
		else {
			info +="Id=" + node.getID() + "depth" + depth + " a=" + node.getHAction() + " vmean=" + (node.getV()/node.getN()) + " v=" + node.getV() + " n=" + node.getN() ;
		}
		POMCPNode parent = node.getParent();
		if (parent == null) {
			System.out.println(info);
		}
		else {
			System.out.print(info + " | ");			
			displayNode(parent, depth - 1);
		}
	}
	public void displayNodeActions(POMCPNode node, int depth)
	{
		String info = "";
		if (node.isONode()){
//			info +="depth" + depth + " obs=" + node.getH()  ;
		}
		else {
			info +="depth" + depth + " a=" + node.getHAction()  ;
		}
		POMCPNode parent = node.getParent();
		if (parent == null) {
			System.out.println(info);
		}
		else {
			System.out.print(info + " | ");			
			displayNodeActions(parent, depth - 1);
		}
	}

	
	public HashSet<Integer> getNextBeliefSupport(HashSet<Integer> beliefSupport, Object action)
	{
		HashSet<Integer> nextBeliefSupport = new HashSet<Integer> ();
		for (int state: beliefSupport) { // for every state in current belief support
			HashSet<Integer> nextStates = getNextStates(state, action);			// get its successor states
			nextBeliefSupport.addAll(nextStates);			// add these states into next belief support
		}
		return nextBeliefSupport;
	}
	
	public HashSet<Integer> getNextStates(int state, Object action)
	{
		int key = state * 100 + getActionIndex(action);
		if (stateSuccessors.get(key) == null) {
			HashSet<Integer> nextStates = new HashSet<Integer>();
			int choice = pomdp.getChoiceByAction(state, action);
			Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(state, choice);
			while (iter.hasNext()) {
				Map.Entry<Integer, Double> trans = iter.next();
				int nextState = trans.getKey();
				nextStates.add(nextState);
			}
			stateSuccessors.put(key, nextStates);
		}
		return stateSuccessors.get(key);
	}
	
	public boolean isActionShieldedForNode(POMCPNode node, Object action) // Main interface checking if action should be shielded
	{
		HashSet<Integer> currentBeliefSupport = node.getBelief().getUniqueStatesInt();
		return isActionShieldedForStates(currentBeliefSupport, action);
	}
	
	public boolean isActionShieldedForStates(HashSet<Integer> beliefSupport, Object action) 
	{
		if (useLocalShields) {
			return isActionShieldedForStatesByLocalShileds(beliefSupport, action);
		} else {
			return isActionShieldedForStatesByMainShield(beliefSupport, action);
		}
	}
	public boolean isActionShieldedForStatesByLocalShileds(HashSet<Integer>beliefSupport, Object action) 
	{
		if (localShields == null) {
			return false; // no local shields available
		}
		HashSet<Integer> nextBeliefSupport = getNextBeliefSupport(beliefSupport, action);
		System.out.println("considering if to shield action "  + action + " next suport" + nextBeliefSupport);
		int numLocalShields = localShields.size();
		for (int i = 0; i < numLocalShields; i++) {
			POMDPShield localShield = localShields.get(i);
			System.out.println("shield = " + i );
			if(!localShield.isSetOfStatesWinning(nextBeliefSupport)) {
				System.out.println("shield = " + i + " action = " + action + " action shield for current support " + beliefSupport + " because next" + nextBeliefSupport);
				return true;
			}
		}
		System.out.println("Not shielded: action "  + action);
		return false;
	}
	
	public boolean isActionShieldedForStatesByMainShield(HashSet<Integer> beliefSupport, Object action) 
	{
		if (mainShield == null) {
			return false; // no shield available
		}
		HashSet<Integer> nextBeliefSupport = getNextBeliefSupport(beliefSupport, action);
//		System.out.println("Use concetralized shield");
		if (!mainShield.isSetOfStatesWinning(nextBeliefSupport)) {
//			System.out.print("action shield for " + beliefSupport);
			return true; // action should be shielded because next belief support is not winning
		}
		return false;
	}

	public boolean isSetOfStatesWinning(HashSet<Integer> beliefSupport) {
		if (!useLocalShields) {
			return isSetOfStatesWinningByMainShield(beliefSupport);
		}else {
			return isSetOfStatesWinningByLocalShields(beliefSupport);
		}
	}
	public boolean isSetOfStatesWinningByMainShield(HashSet<Integer> beliefSupport) {
		if (mainShield == null) {
			return true; // no shield available
		}
		return mainShield.isSetOfStatesWinning(beliefSupport);
	}
	public boolean isSetOfStatesWinningByLocalShields(HashSet<Integer> beliefSupport) {
		if (localShields == null) {
			return true; // no shield available
		}
		for (int i = 0; i < localShields.size(); i++) {
			POMDPShield localShield = localShields.get(i);
			if (!localShield.isSetOfStatesWinning(beliefSupport)) {
//				System.out.println("shield" + i);
				return false;
			}
		}
		return true;
	}
} 
