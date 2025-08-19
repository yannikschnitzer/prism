package learning.Simulation;


import common.Interval;
import explicit.*;
import explicit.rewards.MDPRewardsSimple;
import parser.State;
import parser.ast.Expression;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.Result;
import simulator.ModulesFileModelGenerator;
import simulator.PathOnTheFly;
import simulator.SimulatorEngine;
import strat.MDStrategy;
import strat.Strategy;
import strat.StrategyGenerator;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static imdpcomp.Experiment.ParameterTying;

public class ObservationSampler {

    private final Prism prism;
	private final MDP<Double> sul;

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
		this.accumulatedSamples = new HashMap<>();
		this.transitionsOfInterest = new HashSet<>();
		this.terminatingStates = terminatingStates;

		this.prism = prism;
		this.sul = sul;

		//load model into simulator
		this.prism.loadModelIntoSimulator();
		this.sim = this.prism.getSimulator();
		this.sim.setTerminatingStates(state -> terminatingStates.contains(getIndexFromState(state)));
    }

	public void setModulesFiles( ModulesFile modulesFileMDP, ModulesFile modulesFileIMDP) {
		this.modulesFileIMDP = modulesFileIMDP;
		this.modulesFileMDP = modulesFileMDP;
	}

	public void setTerminatingStates(HashSet<Integer> set) {
		this.terminatingStates = set;
	}

	public void setTransitionsOfInterest(HashSet<TransitionTriple> set) {
		this.transitionsOfInterest = set;
	}

	public int getIndexFromState(State s) {
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
		//System.out.println("State: " + s + a );
		if (a.equals("process1") || a.equals("process2")) {
			a = "[_empty]";
		}
		State successor = path.getCurrentState();
		parseStep(s, a, successor);
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
		String action = a.replace("[", "");
		action = action.replace("]", "");

		//System.out.println("("+currentState+", " + action + ", " + successorState + ")");

		StateActionPair sa = new StateActionPair(currentState, action);
		TransitionTriple t = new TransitionTriple(currentState, action, successorState);
		//only process t if it is a transition of 0 < p < 1
		if (transitionsOfInterest.contains(t)) {
			if (this.samplesMap.containsKey(t)) {
				this.samplesMap.put(t, this.samplesMap.get(t)+1);
			}
			else {
				this.samplesMap.put(t, 1);
			}

			if (this.sampleSizeMap.containsKey(sa)) {
				this.sampleSizeMap.put(sa, this.sampleSizeMap.get(sa)+1);
			}
			else {
				this.sampleSizeMap.put(sa, 1);
			}
		}
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