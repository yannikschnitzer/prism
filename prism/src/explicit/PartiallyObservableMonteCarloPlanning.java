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
	private HashSet<Integer> endStates;
	private int xMIN;
	private int yMIN;
	private int xMAX;
	private int yMAX;
	private List<String> localVarNames;
	private int variableIndexX;
	private int variableIndexY;
	public POMDPShield(POMDP pomdp, String winningFile, List<String> varNames, HashSet<Integer> concreteModelEndStates, int [] primaryStates) {
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
		
		variableIndexX = varNames.indexOf("ax") > 0 ? varNames.indexOf("ax") : varNames.indexOf("x");
		variableIndexY = varNames.indexOf("ay") > 0 ? varNames.indexOf("ay") : varNames.indexOf("y");

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
//		System.out.println(str + "|" + feature);
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

		int numStates = pomdp.getNumStates();
		for (int PrismState = 0; PrismState < numStates; PrismState++) {
//		for (int PrismState = 30321; PrismState < 33103; PrismState++) {

			String fullMeaning = pomdp.getStatesList().get(PrismState).toStringNoParentheses();
			String meaning = pomdp.getStatesList().get(PrismState).toStringNoParentheses(mainVarNames, localVarNames); //TODO
//			System.out.println("Prism state= " + PrismState + " {" + meaning+ "}" + StompyMeaning2State.containsKey(meaning) );	

//			if(StompyMeaning2State.containsKey(meaning) ) {
//				System.out.println("Prism state= " + PrismState + " " + meaning + StompyMeaning2State.containsKey(meaning) );	
//			}
//			
			int StompyState ;
			if (StompyMeaning2State.containsKey(meaning)){
				StompyState = StompyMeaning2State.get(meaning);
//				System.out.println("Prism state= " + PrismState + " " + getStateMeaning(PrismState)+ " to StompyState => "+ StompyState + " " + meaning);
			} else {
//				continue; // prism state has not couterpart in stompystate
				StompyState = -1;
			}
//			System.out.println("Prism state= " + PrismState + " " + getStateMeaning(PrismState)+ " to StompyState => "+ StompyState + " " + meaning);

			PrismStateToStompyState[PrismState] =  StompyState; //key
			
//			System.out.println("Prism state= " + PrismState + " " + getStateMeaning(PrismState)+ " to StompyState => "+ StompyState + " " + meaning);
			if (StompyState != -1) {
				ArrayList<Integer> tp = StompyStateToPrismState.get(StompyState);
				tp.add(PrismState);
				StompyStateToPrismState.put(StompyState, tp); // TODO Is this necessary? 
			}
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
//					System.out.println("Stompy " + state + " stomoy obs "+ obs + "{" + meaning + "}");
					StompyMeaning2State.put(meaning, state);
				}
			}
			if (StompyMeaning2State.size() > 0) {
				System.out.println("State information translated");
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
//			System.out.println("main varNames" + mainVarNames.toString());
//			System.out.println("local varNames" + localVarNames.toString());
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
//		System.out.println("Prism states in all area" + PrismStates);
//		PrismStates = filterPrimaryStates(PrismStates);
//		System.out.println("Prism states in primary area" + PrismStates);

		// convert to Stompy obs 
		HashMap<Integer, HashSet<Integer>> StompyObs2StompyStates = new HashMap<Integer, HashSet<Integer>> ();
		for (int state : PrismStates) {

			int StompyState = PrismStateToStompyState[state];
			if(StompyState < 0) {
				// this state is not modeled in this shield
//				System.out.println("? this state is not model");
				continue;
			}
			int StompyObs = StompyStateToObs.get(StompyState);
//			System.out.println( "State" + state + " " + getStateMeaning(state) + "stompy State" + StompyState+ " stompy obs" +  StompyObs);

			if (winningRegion.get(StompyObs) == null) { // if not winning support for this obs
//				if (!endStates.contains(state)) { // logic to do // the question is, is the end state in the winning region? if so, no need to add this . // TODO
//					System.out.println("!endStates.contains(state");
					return false;
//				}
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
		
		if (PrismStates.size() > 0 && StompyObs2StompyStates.size() == 0) {
			
			System.out.println("These states are not modeled in local shield." + xMIN + " " + yMIN);
			for (int state: PrismStates) {
				int StompyState = PrismStateToStompyState[state];
//				int StompyObs = StompyStateToObs.get(StompyState);
				System.out.println( "State" + state + " " + getStateMeaning(state) + "stompy State" + StompyState);
			}
			return false;
		}
		
		for (int StompyObs : StompyObs2StompyStates.keySet()) {
//			if (winningRegion.get(StompyObs) == null) { // if not winning support for this obs //TODO, should it safe just because the action leads to end state // TODO
//				System.out.println("Hereee");
//				return true; // all states are in end states; other wise it would return false above
//			}
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
		return (int) pomdp.getStatesList().get(state).getValueByIndex(variableIndexX);
	}
	public int getAY(int state) {
		return (int) pomdp.getStatesList().get(state).getValueByIndex(variableIndexY);
	}
}

 class POMCPNode{
	private int id;
	private boolean isQNode;
	private POMCPNode parent;
	private int h;
	private double v;
	private double n;
	private POMCPBelief belief;
	private HashMap<Integer, POMCPNode> children;
	private HashSet<Integer> illegalActionIndexes;
//	private HashSet<Object> illegalActions; 
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
		this.isQNode = false;
		this.v = 0;
		this.n = 0;
		this.illegalActionIndexes = new HashSet<Integer> ();
		//this.illegalActions = null;
	}
	public void addIllegalActionIndex(int actionIndex)
	{
		removeChild(actionIndex);
		illegalActionIndexes.add(actionIndex);
	}
	public boolean isActionIndexIllegal(int actionIndex) {
		if (illegalActionIndexes == null) {
			return false;
		}
		return illegalActionIndexes.contains(actionIndex);
	}
	public HashSet<Integer> getIllegalActionIndexes() 
	{
		return illegalActionIndexes;
	}
	
//	
//	public void addIllegalActions(Object action) {
//		if (illegalActions == null) {
//			illegalActions = new HashSet<Object>();
//		}
//		illegalActions.add(action);
//	}
//	public boolean isActionIllegal(Object action) {
//		if (illegalActions == null) {
//			return false;
//		}
//		return illegalActions.contains(action);
//	}
//	public HashSet<Object> getIllegalActions(){
//		return illegalActions;
//	}
	public boolean isQNode()
	{
		return isQNode;
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
	public void setHAction(boolean isAction) 
	{
		this.isQNode = isAction;
	}
//	public Object getHAction() 
//	{
//		return hAction;
//	}
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
		return v / n;
	}
//	public void increaseN(double value) 
//	{
//		n += value;
//	}
	public void increaseV(double value) 
	{
		v += value;
		n += 1;
	}
	public void setParent(POMCPNode parent) 
	{
		this.parent = parent;
	}
	public POMCPNode getParent() 
	{
		return parent;
	}
	public HashMap<Integer, POMCPNode> getChildren()
	{
		return children;
	}
	public void addChild(POMCPNode node, int index) 
	{
		if (children == null) {
			children = new HashMap<Integer, POMCPNode> (); 
		}
		children.put(index, node);
	}
	public void removeChild(int index) {
		if (children != null) {
			children.remove(index);
			if (children.size() == 0 && this.getParent() != null) {
				POMCPNode qParent = this.getParent();
				POMCPNode vParent = qParent.getParent();
				vParent.removeChild(qParent.getH());
			}
		}
	}
	
	public void setBelief(POMCPBelief belief) 
	{
		this.belief = belief;
	}
	public POMCPBelief getBelief() 
	{
		return belief;
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
//	 private BitSet uniqueStates;
	 POMCPBelief()
	 {
		 this.particles = new ArrayList<Integer>();
//		 uniqueStates = new BitSet();
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
	 public int sample() 
	 {
		 Random rnd = new Random();
		 return particles.get(rnd.nextInt(particles.size()));
	 }
	 public void addParticle(Integer s) 
	 {
		 particles.add(s);
	 }
	 public boolean isStateInBeliefSupport(int state)
	 {
		 return uniqueStatesInt.contains(state);
	 }
	 public void updateBeliefSupport(int state) 
	 {
		 uniqueStatesInt.add(state);
	 }
	 public HashSet<Integer> getUniqueStatesInt()
	 {
		 return uniqueStatesInt;
	 }
	 public void displayUniqueStates() 
	 {
		if(particles.size()==0) 
		{
			System.out.println("No particles");
		}
		else {
			System.out.println("belief: " +  uniqueStatesInt);
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
	 private double reward;
	 stepReturn(int state, double reward)
	 {
		 this.state = state;
		 this.reward = reward;
	 }
	 public int getState() 
	 {
		 return state;
	 }
	 public double getReward() 
	 {
		 return reward;
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
	private double[] initialBeliefDistribution;
	private double [][] UCB;
	private POMCPBelief initialBelief;
	private int TreeDepth;
	private int PeakTreeDepth;

	private POMCPNode root;
	private POMDP pomdp;
	private ArrayList<Object> allActions; 
	private Map <Object, Integer> actionToIndex;
	private MDPRewards mdpRewards;
	private HashMap<Integer, Double> rewardFunction;
	private BitSet target;
	private boolean min;
	private BitSet statesOfInterest;
	private HashSet<Integer> endStates;
	private List<String> varNames;
	private int variableIndexX;
	private int variableIndexY;
	private HashMap<Integer, HashSet<Integer>> stateSuccessorsHashSet;
	private HashMap<Integer, ArrayList<Integer>> stateSuccessorArrayList;
	private HashMap<Integer, ArrayList<Double>> stateSuccessorCumProb;

	private String shieldLevel;
	private ArrayList<POMDPShield> localShields;
	private POMDPShield mainShield;
	private boolean useLocalShields;
	private boolean isMainShieldAvailable;
	private boolean isLocalShieldAvailable;
	private int shieldSize;
	private int gridSize;
	
	public static final String NO_SHIELD = "none";
	public static final String PRIOR_SHIELD = "prior";
	public static final String ON_THE_FLY_SHIELD = "onTheFly";
	public static final String CENTRALIZED_SHIELD = "centrailized";
	public static final String FACTORED_SHIELD = "factored";
	public PartiallyObservableMonteCarloPlanning(POMDP pomdp, MDPRewards mdpRewards, BitSet target, boolean min, BitSet statesOfInterest, HashSet<Integer> endStates,
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
		this.isMainShieldAvailable = false;
		this.isLocalShieldAvailable = false;
		initializePOMCP();
	}
	public void initializePOMCP() {
		this.TreeDepth = 0;
		this.PeakTreeDepth = 0;
		initialUCB(10000, 100);
		initializeVariables();
		getAllActions();
		setActionToIndex();
		this.shieldLevel = NO_SHIELD; 
		this.useLocalShields = false;
		this.rewardFunction = new HashMap<Integer, Double> (); 
		initializeStates();

	}
	public void resetRoot() {
		this.initialBeliefDistribution = pomdp.getInitialBeliefInDist();
		this.initialBelief = new POMCPBelief();
		//TODO
		for (int p = 0; p < K; p ++) {
			int s = drawStateFromBelief(this.initialBeliefDistribution);
			this.initialBelief.addParticle(s);
			this.initialBelief.updateBeliefSupport(s);
		}
		
		this.root = new POMCPNode();
		root.setBelief(this.initialBelief);
	}
	
	
	public double fastUCB(int N, int n, double logN) 
	{
		if (N < 10000 && n < 100) {
			return UCB[N][n];
		}
		if (n == 0)
			return Double.POSITIVE_INFINITY;
		else
//			System.out.println("N=" + N + ", n=" + n);
			return this.c * Math.sqrt(logN / n);
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
	public void setVerbose(int v) 
	{
		verbose = v;
	}
	
	public void setRoot(POMCPNode node) 
	{
		this.root = node;
	}
	public POMCPNode getRoot() 
	{
		return root;
	}
	
	public void update(int actionIndex, int obs) 
	{
		// update/ prune tree given real action and observation
		POMCPNode qnode = root.getChildByActionIndex(actionIndex);
		POMCPNode vnode = qnode.getChildByObservationIndex(obs);
		invigorateBelief(root, vnode, actionIndex, obs);
		vnode.clear();
		root = vnode;
	}
//	
	public boolean Update(int actionIndex, int obs) 
	{
		POMCPBelief beliefs = new POMCPBelief();
	
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
		invigorateBelief(root, newRoot, actionIndex, obs);
		setRoot(newRoot);
		return true;
	}
	
	public void invigorateBelief(POMCPNode parent, POMCPNode child, int actionIndex, int obs) 
	{
		// fill child belief with particles
		int childBeliefSize = child.getBelief().size();
		while (childBeliefSize < K) {
			int s = parent.getBelief().sample();
			int nextState = step(s, actionIndex);
			int obsSample = pomdp.getObservation(nextState);
			if (obsSample == obs) {
				if (!child.getBelief().getUniqueStatesInt().contains(nextState)) {
				}
				child.getBelief().addParticle(nextState);
				child.getBelief().updateBeliefSupport(nextState);
				childBeliefSize += 1;
			}
		}
	}
	
	public int drawStateFromDistr(ArrayList<Double> distr) // TODO can be improved?
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
	
	public int getDefaultAction() {
//		int state = root.getBelief().sample();
//		List<Object> availableActions = pomdp.getAvailableActions(state);
////		System.out.println("state" + state + "default action" + availableActions.get(0) + ( availableActions.size() == 1));
//		return availableActions.get(0);
		return 0;
	}
	public int selectAction() 
	{
		boolean distableTrue = false;
		if (distableTrue) {
			return -1;
		}
		else {
			UCTSearch();
		}
		int actionIndex = GreedyUCB(root, false);
		return actionIndex;
	}
	public void UCTSearch()
	{
		for (int n = 0; n < numSimulations; n++) {
			int state = root.getBelief().sample();
//			if(!root.getBelief().getUniqueStates().get(state)){
//				System.out.println("err" + state);
//				root.getBelief().displayUniqueStates();
//			}
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
		if (TreeDepth >= maxDepth) {
			return 0;
		}
		
		// TODO check later for shielding logic
		if (TreeDepth == 1 && shieldLevel != ON_THE_FLY_SHIELD) {
			vnode.getBelief().addParticle(state); //add sample for only first layer
			vnode.getBelief().updateBeliefSupport(state);

		}
		int actionIndex = GreedyUCB(vnode, true);

		if (shieldLevel == ON_THE_FLY_SHIELD) {
			vnode.getBelief().addParticle(state); //add sample for every layer
			if (!vnode.getBelief().isStateInBeliefSupport(state)) { // only check when a new unique particle is to be added
				vnode.getBelief().updateBeliefSupport(state);
				if (!isSetOfStatesWinning(vnode.getBelief().getUniqueStatesInt())) {
	//				System.out.println("\n" + vnode.getBelief().getUniqueStatesInt() + "is not winning. TreeDepth = " + TreeDepth);
					POMCPNode qparent = vnode.getParent();
					int parentActionIndex = qparent.getH();
					POMCPNode vparent = qparent.getParent();
					vparent.addIllegalActionIndex(parentActionIndex);
					if (verbose >= 5) {
						System.out.println("Currnet Node=" + vnode.getID() + " Current belief support" + vnode.getBelief().getUniqueStatesInt()  );
						System.out.println("Currenting belief support is not winning. ");
						System.out.println("shield level" + shieldLevel +" shielded action: " + allActions.get(parentActionIndex)
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
		}
		
		POMCPNode qnode = vnode.getChildByActionIndex(actionIndex);
		double totalReward = simulateQ(state, qnode, actionIndex);
		vnode.increaseV(totalReward);
//		vnode.increaseN(1);
		return totalReward;
	}
	public double simulateQ(int state, POMCPNode qnode, int actionIndex) 
	{
		double delayedReward = 0;
		int nextState = step(state, actionIndex);
		int observation = pomdp.getObservation(nextState);
		boolean done = endStates.contains(nextState);
		double immediateReward = stepReward(state, actionIndex);
		double totalReward = 0;
		
		if (verbose >= 3) {
			System.out.println("uct action=" +  allActions.get(actionIndex) + " reward=" + immediateReward );
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
			vnode.setParent(qnode);
			qnode.addChild(vnode, observation);
		}
		
		if(!done) {
			TreeDepth++;
			if(vnode != null) {
				delayedReward = simulateV(state, vnode);
			}
			else {
				delayedReward = rollout(state);	
			}
			TreeDepth--;
		} else {
			// add reward for reaching  last state
			totalReward += mdpRewards.getStateReward(state);
		}
		
		totalReward += immediateReward + gamma * delayedReward;
		qnode.increaseV(totalReward);
		return totalReward;
	
	}
	public POMCPNode expandNode(int state)
	{
		POMCPNode vnode = new POMCPNode ();
		vnode.getBelief().addParticle(state);
		List <Object> availableActions = getLegalActions(state);
		for (Object action : availableActions) {
			int actionIndex = getActionIndex(action);
			POMCPNode qnode = new POMCPNode();
			qnode.setH(actionIndex);
			qnode.setHAction(true);
			qnode.setParent(vnode);
			vnode.addChild(qnode, actionIndex);
		}
		return vnode;
	}
	
	public void expand(POMCPNode parent, int state)
	{	
		List <Object> availableActions = getLegalActions(state);
		for (Object action : availableActions) {
			POMCPNode newChild = new POMCPNode ();
			int actionIndex = actionToIndex.get(action);
//			if (shieldLevel == 2 && TreeDepth == 0 && parent.isActionIllegal(action)) {
//				 continue;
//			}
			if (shieldLevel == PRIOR_SHIELD && TreeDepth == 0 && isActionShieldedForNode(parent, action)) {
				parent.addIllegalActionIndex(actionIndex);
				continue;
			}
			newChild.setH(actionIndex);
			newChild.setHAction(true);
			newChild.setParent(parent);;
			parent.addChild(newChild, actionIndex);
		}
		
		if (parent.getChildren() == null) {
//			System.out.println("Add default available actions");
			for (Object action : availableActions) {
				POMCPNode newChild = new POMCPNode ();
				int actionIndex = actionToIndex.get(action);
				newChild.setH(actionIndex);
				newChild.setHAction(true);
				newChild.setParent(parent);;
				parent.addChild(newChild, actionIndex);
			}
		}

	}
	public double rollout(int state)
	{
		double totalReward = 0;
		double discount = 1; // 1 as in original code 
		boolean done = false;
		if (verbose >= 3) {
			System.out.println("starting rollout");
		}
		int numStep = 0;
		int remainTree = maxDepth - TreeDepth;
		while (!done && numStep < remainTree  ) {
			// get random action
			int numChoices = pomdp.getNumChoices(state);
			Random rnd = new Random();
			int randomChoice = rnd.nextInt(numChoices);
			if (numChoices <= 0) {
				break;
			}
			Object randomAction = pomdp.getAction(state, randomChoice);
			
			int actionIndex = actionToIndex.get(randomAction);
			int nextState = step(state, actionIndex);
			double reward = stepReward(state, actionIndex);
			done = endStates.contains(nextState);

			if (verbose >= 4) {
				System.out.println("verbose= "+ verbose + " state= " + state + " rollout action=" + randomAction + " reward=" + reward + " discountR=" + reward*discount + " depth=" + numStep + " totalR=" + totalReward);
				displayState(nextState);
			}
			
			totalReward += reward * discount;
			discount *= gamma;
			numStep++;
			state = nextState;
		}
		// add reward for reaching  last state
		totalReward += mdpRewards.getStateReward(state);
		
		if (verbose >= 3) {
			System.out.println("Ending rollout after " + numStep + "steps, with total reward" + totalReward );
		}
		return totalReward;
	}

	
	public int GreedyUCB(POMCPNode vnode, boolean ucb) 
	{
		ArrayList<Integer> besta = new ArrayList<Integer> ();
		double bestq = Double.NEGATIVE_INFINITY;
		int N = (int) vnode.getN();
		double logN = Math.log(N + 1);
		HashMap<Integer, POMCPNode> children = vnode.getChildren();
		ArrayList<Integer> actionIndexCandidates = new ArrayList<Integer>(); // if all actions are shielded, randomly pick an action
		for (int i: children.keySet()) {
			actionIndexCandidates.add(i);
			if (shieldLevel == ON_THE_FLY_SHIELD && vnode.isActionIndexIllegal(i)) {
//				System.out.println("shield level" + shieldLevel + " known illegal action " + allActions.get(i) +" for node " + vnode.getID() + " belief support" 	+ vnode.getBelief().getUniqueStatesInt());
				continue;
			}
			POMCPNode qnode = children.get(i);
			int n = (int) qnode.getN();
			double q = qnode.getV();
			
			if(n == 0) {
				return i;
			}
			if (ucb) {
				q += fastUCB(N, n, logN);
			}
			if (q >= bestq) {
				if (q > bestq) {
					besta.clear();
				}
				bestq = q;
				besta.add(i);
			}
//			if ( !ucb  && shieldLevel == 1  && isActionShieldedForNode(vnode, action) ) { // shiled only apply to the most up level
////				System.out.println("shield Level = "+shieldLevel+ " Shielded Action = "  + action);
//				continue;
//			}
//			if (shieldLevel == 3 && vnode.isActionIllegal(action)) {
////				System.out.println("shield level" + shieldLevel + " known illegal action" 
////									+ action +" for node " + vnode.getID() + " belief support" 
////									+ vnode.getBelief().getUniqueStatesInt());
//				continue;
//			}
//			if (shieldLevel == 3 && isActionShieldedForNode(vnode, action)) {
////				System.out.println("shield level" + shieldLevel +" shielded action: " + action 
////									+ "\n adding to illegal actions for node " + vnode.getID() 
////									+ " belief support" +  vnode.getBelief().getUniqueStatesInt());
//				vnode.addIllegalActions(action);
//				continue;
//			}
//			
		}
		
		Random rnd = new Random();
		if(besta.size() ==0) {
//			System.out.println("not action available");
		}
		if (besta.size() > 0) {
			int actionIndex = besta.get(rnd.nextInt(besta.size()));
			return actionIndex;
		}
		else {
			int actionIndex = actionIndexCandidates.get(rnd.nextInt(actionIndexCandidates.size()));
//			System.out.println("states" + vnode.getBelief().getUniqueStatesInt());
			POMCPNode qParent = vnode.getParent();
			POMCPNode vParent = qParent.getParent();
			vParent.addIllegalActionIndex(qParent.getH());
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
	public Object getActionByActionIndex(int index) {
		return allActions.get(index);
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
	
	public void initializeStates() 
	{
		stateSuccessorsHashSet = new HashMap<Integer, HashSet<Integer>>();
		stateSuccessorArrayList = new HashMap<Integer, ArrayList<Integer>> ();
		stateSuccessorCumProb= new HashMap<Integer, ArrayList<Double>> ();
		for (int state = 0; state < pomdp.getNumStates(); state++) {
			for (int actionIndex = 0; actionIndex < allActions.size(); actionIndex++) {
				int key = getKeyByStateActionIndex(state, actionIndex);
				Object action = allActions.get(actionIndex);
				if(!pomdp.getAvailableActions(state).contains(action)) {
					continue;
				}
				int choice = pomdp.getChoiceByAction(state, action);
				Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(state, choice);
				ArrayList<Integer> nextStates = new ArrayList<Integer> ();
				ArrayList<Double> nextStatesProbs = new ArrayList<Double> ();
				ArrayList<Double> nextStatesCumProbs = new ArrayList<Double> ();
				int count = 0;
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> trans = iter.next();
					nextStates.add(trans.getKey());
					nextStatesProbs.add(trans.getValue());
					if (count > 0) {
						nextStatesCumProbs.add(nextStatesCumProbs.get(count - 1) + trans.getValue());
					} else {
						nextStatesCumProbs.add(trans.getValue());
					}
					count += 1;
				}
				stateSuccessorsHashSet.put(key, new HashSet<> (nextStates));
				stateSuccessorArrayList.put(key, nextStates);
				stateSuccessorCumProb.put(key, nextStatesCumProbs);
				stepReward(state, actionIndex);
			}
		}
//		Collections.binarySearch(nextStatesProbs, 0.4);
	}
	
	public int getKeyByStateActionIndex(int state, int actionIndex) 
	{
		return state * allActions.size() + actionIndex;
	}
	
	public double stepReward(int state, int actionIndex) {
		int key = getKeyByStateActionIndex(state, actionIndex);
		double reward;
		if (rewardFunction.containsKey(key)) {
			reward = rewardFunction.get(key);
		}else {
			Object action = allActions.get(actionIndex);
			int choice = pomdp.getChoiceByAction(state, action);		
			if (!stateSuccessorArrayList.containsKey(key)) {
				System.out.println("state = "+ state + allActions.get(actionIndex));
			}
			reward = mdpRewards.getTransitionReward(state, choice) + mdpRewards.getStateReward(state); // to check if no shield what is the cost function
			if (min) {
				reward *= -1;
			}
			rewardFunction.put(key,reward);
		}
		return reward;
	}
	
	
	public int step(int state, int actionIndex) {
		int key = getKeyByStateActionIndex(state, actionIndex);

		if (!stateSuccessorArrayList.containsKey(key)) {
			System.out.println("state = "+ state + allActions.get(actionIndex));
			return -1;
		}
		
		double tp = Math.random();
		int index = Collections.binarySearch(stateSuccessorCumProb.get(key), tp);
		index = (index >= 0) ? index : (-index -1);
		int nextState = stateSuccessorArrayList.get(key).get(index);
		//TODO add reward for final state
//		if (endStates.contains(nextState)){
//			reward += mdpRewards.getStateReward(nextState) * gamma;
//		}
		return nextState;
	}
	
	public int stepC(int state, int actionIndex) //TODO can be deleted
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
		int nextState = nextStates.get(drawStateFromDistr(nextStatesProbs));
		//TODO add reward for final state
//		if (endStates.contains(nextState)){
//			reward += mdpRewards.getStateReward(nextState) * gamma;
//		}
//		return new stepReturn(nextState, reward );
		return nextState;
	}
	public List<Object> getLegalActions(int state)
	{
		List <Object> availableActions = pomdp.getAvailableActions(state);
//		for (int a = availableActions.size() -1 ; a >= 0; a--) {
//			if (stepReward(state,  a) < -200 ) {
////				System.out.println("========\nstate " + state + "action" + availableActions.get(a));
////				displayState(state);
//				availableActions.remove(a);
//			}
//		}
		return availableActions;
	}
	
	public void initializeVariables() 
	{
		varNames = new ArrayList<String>();
		for (int i = 0; i < pomdp.getVarList().getNumVars(); i++) {
			varNames.add(pomdp.getVarList().getName(i));
		}
		variableIndexX = varNames.indexOf("ax") > 0 ? varNames.indexOf("ax") : varNames.indexOf("x");
		variableIndexY = varNames.indexOf("ay") > 0 ? varNames.indexOf("ay") : varNames.indexOf("y");
	}
	
	public int getAX(int state) {
		return (int) pomdp.getStatesList().get(state).getValueByIndex(variableIndexX);
	}
	public int getAY(int state) {
		return (int) pomdp.getStatesList().get(state).getValueByIndex(variableIndexY);
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
//		List<Object> availableActions = pomdp.getAvailableActions(state);
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
	public HashSet<Object> getRootIllegaActions()
	{
		HashSet<Integer> illegalActionIndexes = root.getIllegalActionIndexes();
		HashSet<Object> illegalActions = new HashSet<Object> ();
		for (int index: illegalActionIndexes) {
			illegalActions.add(allActions.get(index));
		}
		return illegalActions;
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
		if (!node.isQNode()){
			info +="Id=" + node.getID()+ "depth" + depth  + " o=" + node.getH() + " vmean=" + (node.getV()) + " vall=" + (node.getV() * node.getN()) + " n=" + node.getN() +" Belief Support=" + node.getBelief().getUniqueStatesInt();
		}
		else {
			info +="Id=" + node.getID() + "depth" + depth + " a=" + allActions.get(node.getH() ) + " vmean=" + (node.getV()) + " vall=" + (node.getV() * node.getN()) + " n=" + node.getN() ;
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
		if (!node.isQNode()){
//			info +="depth" + depth + " obs=" + node.getH()  ;
		}
		else {
			info +="depth" + depth + " a=" + allActions.get(node.getH() ) ;
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
		int actionIndex = actionToIndex.get(action);
		int key = getKeyByStateActionIndex(state, actionIndex);
		
		if (stateSuccessorsHashSet.get(key) == null) {
			HashSet<Integer> nextStates = new HashSet<Integer>();
			int choice = pomdp.getChoiceByAction(state, action);
			Iterator<Entry<Integer, Double>> iter = pomdp.getTransitionsIterator(state, choice);
			while (iter.hasNext()) {
				Map.Entry<Integer, Double> trans = iter.next();
				int nextState = trans.getKey();
				nextStates.add(nextState);
			}
			stateSuccessorsHashSet.put(key, nextStates);
		}
		return stateSuccessorsHashSet.get(key);
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
	
	public int getShieldIndex(int state)
	{
		
		int x = getAX(state);
		int y = getAY(state);
		int shieldIndex = (x / shieldSize)* (gridSize / shieldSize) + (y / shieldSize) ;
//		System.out.println(state + " get " + getStateMeaning(state) + "x " + x + ", y= " + y + "sheild index" + shieldIndex);
		return shieldIndex; 
	}
	public boolean isActionShieldedForStatesByLocalShileds(HashSet<Integer> beliefSupport, Object action) 
	{
		if (localShields == null) {
			return false; // no local shields available
		}
		HashSet<Integer> nextBeliefSupport = getNextBeliefSupport(beliefSupport, action);
		if (verbose > 0) {
			System.out.println("considering if to shield action "  + action + " next suport" + nextBeliefSupport);
		}
		if (isSetOfStatesWinningByLocalShields(nextBeliefSupport)) {
			return false;
		} else {
			return true;
		}
	}
//	public boolean isActionShieldedForStatesByLocalShileds3(HashSet<Integer>beliefSupport, Object action) //TODO delete
//	{
//		if (localShields == null) {
//			return false; // no local shields available
//		}
//		HashSet<Integer> nextBeliefSupport = getNextBeliefSupport(beliefSupport, action);
//		if (verbose > 0) {
//			System.out.println("considering if to shield action "  + action + " next suport" + nextBeliefSupport);
//		}
//		
//		int numLocalShields = localShields.size();
//		for (int i = 0; i < numLocalShields; i++) {
//			POMDPShield localShield = localShields.get(i);
//			System.out.println("shield = " + i );
//			if(!localShield.isSetOfStatesWinning(nextBeliefSupport)) {
//				if (verbose > 0) {
//					System.out.println("Shielded = " + i + " action = " + action + " action shield for current support " + beliefSupport + " because next" + nextBeliefSupport);
//				}
//				return true;
//			}
//		}
//		if (verbose > 0) {
//			System.out.println("Not shielded: action "  + action);
//		}
//		return false;
//	}
	public boolean isActionShieldedForStatesByMainShield(HashSet<Integer> beliefSupport, Object action) 
	{
		if (mainShield == null) {
			return false; // no shield available
		}
		HashSet<Integer> nextBeliefSupport = getNextBeliefSupport(beliefSupport, action);
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
		HashMap<Integer, HashSet<Integer>> shieldIndexToBeliefSupport = new HashMap<Integer, HashSet<Integer>> ();
		for (int state: beliefSupport) {
			int shieldIndex = getShieldIndex(state);
			if (!shieldIndexToBeliefSupport.containsKey(shieldIndex)) {
				HashSet<Integer> belief = new HashSet<Integer>();
				shieldIndexToBeliefSupport.put(shieldIndex, belief);
			}
			shieldIndexToBeliefSupport.get(shieldIndex).add(state);
		}
		for (int shieldIndex: shieldIndexToBeliefSupport.keySet()) {
			POMDPShield localShield = localShields.get(shieldIndex);
			HashSet<Integer> beliefSupportToCheck = shieldIndexToBeliefSupport.get(shieldIndex);
			if(!localShield.isSetOfStatesWinning(beliefSupportToCheck)) {
				return false;
			}
		}
		return true;
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
			this.isMainShieldAvailable = true;
			String[] parameters = fileName.split("-");
			
//			gridSize = Integer.parseInt(parameters[1]);
//			shieldSize = gridSize;
			int[] pStates = {0, 0, Integer.parseInt(parameters[1]), Integer.parseInt(parameters[1])}; 
			
			String winning = file.toString();
			mainShield = new POMDPShield(pomdp, winning,  varNames, endStates, pStates);
			break;
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
	public void setShieldLevel(String level) 
	{
		shieldLevel = level;
	}
	public void loadLocalShield(String shieldDir) 
	{
		localShields = new ArrayList<POMDPShield> ();
		File files = new File(shieldDir);
		File[] array = files.listFiles();
		ArrayList<String> fileNames = new ArrayList<String> ();
		for (int i = 0; i < array.length; i++) {
			if (!array[i].isFile()) {
				continue;
			}
			String fileName = array[i].getName(); 
			if (!fileName.contains("factor")) {
				continue;
			}
			fileNames.add(fileName);
		}
		Collections.sort(fileNames);
		for (int i = 0; i < fileNames.size(); i++) {
			String fileName = fileNames.get(i);
			System.out.println("++++Initialize shield index = " + localShields.size() + ", shieldName = "+ fileName);
			String winning = shieldDir + System.getProperties().getProperty("file.separator") + fileName;
			this.isLocalShieldAvailable = true;
			String[] parameters = fileName.split("-");
			int[] pStates = {Integer.parseInt(parameters[2]), Integer.parseInt(parameters[3]), Integer.parseInt(parameters[4]), Integer.parseInt(parameters[5])}; 
			
			shieldSize = Integer.parseInt(parameters[1]);
			gridSize = Integer.parseInt(parameters[5]) + 1;
			
			POMDPShield localShield = new POMDPShield(pomdp, winning, varNames, endStates, pStates);
			localShields.add(localShield);
		}
	}
	public boolean hasMainShield() 
	{
		return this.isMainShieldAvailable;
	}
	public boolean hasLocalShield() 
	{
		return this.isLocalShieldAvailable;
	}	
	
//	public int getDistanceToEndState(int state) {
//		for(int endState: endStates) {
//			int ax = getAX(state);
//			int ay = getAY(state);
//			int x = getAX(endState);
//			int y = getAY(endState);
//			return Math.abs(ax -x) + Math.abs(ay - y);
//		} 
//		return -1;
//	}
} 
