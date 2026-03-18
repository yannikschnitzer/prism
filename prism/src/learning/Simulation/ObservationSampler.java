package learning.Simulation;


import explicit.MDP;
import parser.State;
import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismException;
import simulator.PathOnTheFly;
import simulator.SimulatorEngine;
import strat.Strategy;
import strat.StrategyGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static imdpcomp.Experiment.ParameterTying;

public class ObservationSampler {

	private final Prism prism;
	private final MDP<Double> sul;
	private final HashMap<State, Integer> stateToIndex;
	private final TransitionTriple transitionProbe;

	private final HashMap<TransitionTriple, Integer> samplesMap;
	private final HashMap<StateActionPair, Integer> sampleSizeMap;
	private HashMap<StateActionPair, Integer> accumulatedSamples;

	private HashSet<Integer> terminatingStates;
	private final SimulatorEngine sim;

	private HashSet<TransitionTriple> transitionsOfInterest;

	private final boolean DEBUG = false;

	private ModulesFile modulesFileIMDP;
    private ModulesFile modulesFileMDP;

	private ParameterTying tiedParameters;

	private int multiplier;

    public ObservationSampler(Prism prism, MDP<Double> sul, HashSet<Integer> terminatingStates) throws PrismException{
		this.sampleSizeMap = new HashMap<>();
		this.samplesMap = new HashMap<>();
		this.accumulatedSamples = new HashMap<>();
		this.transitionsOfInterest = new HashSet<>();
		this.terminatingStates = terminatingStates;
		this.stateToIndex = buildStateIndexMap(sul);
		this.transitionProbe = new TransitionTriple(0, "_empty", 0);

		this.prism = prism;
		this.sul = sul;

		//load model into simulator
		this.prism.loadModelIntoSimulator();
		this.sim = this.prism.getSimulator();
		this.sim.setTerminatingStates(state -> this.terminatingStates.contains(getIndexFromState(state)));
    }

	private static HashMap<State, Integer> buildStateIndexMap(MDP<Double> sul)
	{
		List<State> statesList = sul.getStatesList();
		HashMap<State, Integer> indexMap = new HashMap<>(Math.max(16, statesList.size() * 2));
		for (int i = 0; i < statesList.size(); i++) {
			indexMap.put(statesList.get(i), i);
		}
		return indexMap;
	}

	public void setModulesFiles( ModulesFile modulesFileMDP, ModulesFile modulesFileIMDP) {
		this.modulesFileIMDP = modulesFileIMDP;
		this.modulesFileMDP = modulesFileMDP;
	}

	public void setTerminatingStates(HashSet<Integer> set) {
		this.terminatingStates = set;
		this.sim.setTerminatingStates(state -> this.terminatingStates.contains(getIndexFromState(state)));
	}

	public void setTransitionsOfInterest(HashSet<TransitionTriple> set) {
		this.transitionsOfInterest = set;
	}

	public int getIndexFromState(State s) {
		Integer index = stateToIndex.get(s);
		if (index != null) {
			return index;
		}
		// Conservative fallback to preserve existing behaviour in case of mismatched state encoding.
		return sul.getStatesList().indexOf(s);
	}

	public String getActionString(MDP<Double> mdp, int s, int i) {
		String action = (String) mdp.getAction(s,i);
		if (action == null) {
			action = "_empty";
		}
		return action;
	}

	public HashMap<TransitionTriple, Integer> getSamplesMap() {
		return this.samplesMap;
	}

	public HashMap<StateActionPair, Integer> getSampleSizeMap() {
		return this.sampleSizeMap;
	}

	public int simulate(long size, Strategy strat) throws PrismException
	{
		int samples = 0;
		resetObservationSequence();
		sim.createNewOnTheFlyPath();
		sim.loadStrategy((StrategyGenerator<Double>) strat);
		sim.initialisePath(null);
		while (samples <= size) {
			boolean step = sim.automaticTransition();
			if (step) {
				PathOnTheFly path = (PathOnTheFly) sim.getPath();
				parseLastStep(path);
				samples += 1;
			}
			else {
				sim.createNewOnTheFlyPath();
				sim.loadStrategy((StrategyGenerator<Double>) strat);
				sim.initialisePath(null);
			}

		}
		return samples;
	}

	public int simulateEpisode(int horizon, Strategy strat) throws PrismException {
		int number_of_samples = 0;
		sim.createNewOnTheFlyPath();
		sim.loadStrategy((StrategyGenerator<Double>) strat);
		sim.initialisePath(null);
		while (number_of_samples <= horizon) {
			boolean step = sim.automaticTransition();
			if (! step) {
				// could not execute a new action
				break;
			}
			PathOnTheFly path = (PathOnTheFly) sim.getPath();
			parseLastStep(path);
			number_of_samples += 1;
		}
		return number_of_samples;
	}

	private void parseLastStep(PathOnTheFly path) {
		State s = path.getPreviousState();
		String a = path.getPreviousActionString();
		State successor = path.getCurrentState();
		parseStep(s, normalizeAction(a), successor);
	}

	public boolean collectedEnoughSamples() {
		return this.collectedEnoughSamples(multiplier);
	}

	public boolean collectedEnoughSamples(float ratio) {
		for (Map.Entry<StateActionPair, Integer> entry: this.sampleSizeMap.entrySet()){
			if (tiedParameters == ParameterTying.FULL_TYING || tiedParameters == ParameterTying.DEPENDENCY_TYING) {
				if (entry.getValue() - this.accumulatedSamples.getOrDefault(entry.getKey(), 1) >= ratio * this.accumulatedSamples.getOrDefault(entry.getKey(), 1)) {
					return true;
				}
			} else {
				if (entry.getValue() >= ratio * this.accumulatedSamples.getOrDefault(entry.getKey(), 1)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getTotalSamples() {
		int total = 0;
		for (Map.Entry<StateActionPair, Integer> entry: this.accumulatedSamples.entrySet()){
			total += entry.getValue();
		}
		return total;
	}

	public void parseStep(State s, String a, State sprime) {
		int currentState = getIndexFromState(s);
		int successorState = getIndexFromState(sprime);
		if (currentState < 0 || successorState < 0) {
			return;
		}
		String action = normalizeAction(a);

		this.transitionProbe.setAll(currentState, action, successorState);
		// Only process transitions of interest, avoid creating objects for all other transitions.
		if (!transitionsOfInterest.contains(this.transitionProbe)) {
			return;
		}

		StateActionPair sa = new StateActionPair(currentState, action);
		Integer transitionCount = this.samplesMap.get(this.transitionProbe);
		if (transitionCount == null) {
			this.samplesMap.put(new TransitionTriple(currentState, action, successorState), 1);
		} else {
			this.samplesMap.put(this.transitionProbe, transitionCount + 1);
		}

		Integer stateActionCount = this.sampleSizeMap.get(sa);
		if (stateActionCount == null) {
			this.sampleSizeMap.put(sa, 1);
		} else {
			this.sampleSizeMap.put(sa, stateActionCount + 1);
		}
	}

	private static String normalizeAction(String action)
	{
		if (action == null || action.isEmpty() || action.equals("process1") || action.equals("process2")) {
			return "_empty";
		}
		if (action.length() >= 2 && action.charAt(0) == '[' && action.charAt(action.length() - 1) == ']') {
			return action.substring(1, action.length() - 1);
		}
		return action;
	}

	/**
	 * Reset observation sequence
	 */
    public void resetObservationSequence() {
		incrementAccumulatedSamples();
		this.sampleSizeMap.clear();
		this.samplesMap.clear();
    }

	public void incrementAccumulatedSamples() {
		this.sampleSizeMap.forEach((sa, counter) -> {
			this.accumulatedSamples.put(sa, this.accumulatedSamples.getOrDefault(sa, 0) + counter);
		});
	}

	public void setTiedParameters(ParameterTying tiedParameters) {
		this.tiedParameters = tiedParameters;
	}

	public int getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(int multiplier) {
		this.multiplier = multiplier;
	}
}
