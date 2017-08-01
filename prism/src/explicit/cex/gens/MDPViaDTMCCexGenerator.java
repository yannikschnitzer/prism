package explicit.cex.gens;

import prism.PrismException;
import prism.PrismLog;
import strat.MDStrategy;
import explicit.DTMC;
import explicit.DTMCFromMDPAndMDStrategy;
import explicit.DTMCFromMDPMemorylessAdversary;
import explicit.MDP;
import explicit.MDPModelChecker;
import explicit.ModelCheckerResult;
import explicit.cex.CounterexampleMethod;
import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.CexSetOfPaths;
import explicit.cex.cex.CriticalSubsystem;
import explicit.cex.cex.MDCriticalMDPSubsystem;
import explicit.cex.cex.IntermediateCounterexample;
import explicit.cex.cex.NondetCounterexample;
import explicit.cex.cex.NondetPath;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.util.CexParams;
import explicit.cex.util.Transition;

public class MDPViaDTMCCexGenerator extends MDPCexGenerator
{

	protected final CounterexampleMethod dtmcMethod;
	private MDStrategy strat = null;
    private int[] adv = null;
	
	public MDPViaDTMCCexGenerator(MDP mdp, MDStrategy strat, CounterexampleMethod dtmcMethod, CexParams params, PrismLog log) {
		this(mdp, dtmcMethod, params, log);
		this.strat = strat;
	}
	
	public MDPViaDTMCCexGenerator(MDP mdp, int[] adv, CounterexampleMethod dtmcMethod, CexParams params, PrismLog log)
	{
		this(mdp, dtmcMethod, params, log);
		this.adv = adv;
	}
	
	public MDPViaDTMCCexGenerator(MDP mdp, CounterexampleMethod dtmcMethod, CexParams params, PrismLog log)
	{
		super(mdp, params, log);
		if (!dtmcMethod.isApplicableToInducedDTMC()) throw new IllegalArgumentException("Expected counterexample method applicable to the induced DTMC, but received " + dtmcMethod);
		this.dtmcMethod = dtmcMethod;
	}

	protected DTMC getInducedDTMC() {
		if (strat == null && adv == null) {
			computeMaxReachabilityStrat();
		}
		
		if (strat != null) {
			return new DTMCFromMDPAndMDStrategy(mdp, (MDStrategy)strat);
		} else if (adv != null) {
			return new DTMCFromMDPMemorylessAdversary(mdp, adv);
		} else {
			return null;
		}
	}

	private void computeMaxReachabilityStrat()
	{
		try {
			int init = params.getInitialState();
			assert (init != CexParams.UNINITIALIZED_STATE);
			log.println("Will compute max reachability from initial state " + init + "...", PrismLog.VL_ALL);
			MDPModelChecker mc = new MDPModelChecker(null);
			mc.setGenStrat(true);

			// Perform modelchecking, restricted to the states that are currently in the subsystem
			ModelCheckerResult res = mc.computeReachProbs(mdp, params.getTargetSet(), false);
			log.println("Got back prob mass from modelchecking: " + res.soln[init], PrismLog.VL_ALL);

			assert (res.strat != null);			

			if (res.strat instanceof MDStrategy) {
				strat = (MDStrategy)res.strat;
			} else {
				log.println("Error: Expected memory-less deterministic strategy, received " + strat.getClass().getName());
			}

		} catch (PrismException e) {
			log.println("Model-checking the MDP failed: " + e.getMessage());
			e.printStackTrace();
		}
	}


	@Override
	public ProbabilisticCounterexample call() throws Exception
	{
		DTMC dtmc = getInducedDTMC();
		if (dtmc == null) throw new IllegalStateException();
		NormalizedDTMC ndtmc = new NormalizedDTMC(dtmc, params);
		
		CexGenerator gen = dtmcMethod.makeGenerator(ndtmc, params, log);
		log.println("Set up '" + gen + "'", PrismLog.VL_HIGH);
		
		ProbabilisticCounterexample dtmcCex = gen.partialResult;
		partialResult = new IntermediateCounterexample(dtmcCex, "Counterexample for the DTMC induced by strategy/adversary, not for the MDP", "Induced DTMC");

		try {
			dtmcCex = gen.callFromCurrentThread();
			partialResult = new IntermediateCounterexample(dtmcCex, "Counterexample for the DTMC induced by strategy/adversary, not for the MDP", "Induced DTMC");
		} catch (InterruptedException e) {
			log.println("Underlying DTMC Counterexample generator was interrupted. Will return", PrismLog.VL_HIGH);
		}

		if (dtmcCex.probabilityMassExceedsThreshold()) {
			log.println("Underlying DTMC Counterexample generator '"  + gen + "' was successful. Will inject non-determinism", PrismLog.VL_HIGH);
			partialResult = injectNondeterminism(dtmcCex);
			log.println("Counterexample computation complete, will return " + partialResult.getTypeString(), PrismLog.VL_HIGH);
			return partialResult;
		}
		else {
			return null;
		}
	}

	protected DeterministicChoiceResolver getChoiceResolver() {
		if (strat != null)
			return makeChoiceResolver(strat);
		else if (adv != null)
			return makeChoiceResolver(adv);
		else
			throw new IllegalStateException();
	}

	protected NondetCounterexample injectNondeterminism(ProbabilisticCounterexample cex) {
		switch (dtmcMethod) {
		case DTMC_EXPLICIT_KPATH:
			return addNondeterminismToPaths((CexSetOfPaths)cex);
		case DTMC_EXPLICIT_LOCAL:
			return addNondeterminismToSubsystem((CriticalSubsystem)cex);
		default:
			throw new IllegalStateException("Illegal counterexample method for underlying DTMC: " + dtmcMethod);
		}
	}

	private NondetCounterexample addNondeterminismToPaths(CexSetOfPaths cex)
	{
		DeterministicChoiceResolver resolver = getChoiceResolver();
		CexSetOfPaths cexWithNondet = new CexSetOfPaths(params, mdp);
		for (ProbabilisticPath path : cex.getPaths()) {
			NondetPath npath = new NondetPath();
			for (Transition t : path.getTransitions()) {
				npath.addTransitionAtBack(t, resolver.getChoiceActionForState(t.getSource()));
			}
			cexWithNondet.add(npath);
		}
		return cexWithNondet;
	}

	private NondetCounterexample addNondeterminismToSubsystem(CriticalSubsystem cs)
	{
		return new MDCriticalMDPSubsystem(params, mdp, cs, getChoiceResolver());
	}

	public interface DeterministicChoiceResolver {
		public int getChoiceIndexForState(int s);
		public Object getChoiceActionForState(int s);
	}

	private static DeterministicChoiceResolver makeChoiceResolver(final MDStrategy strat) {
		return new DeterministicChoiceResolver() {

			@Override
			public int getChoiceIndexForState(int s)
			{
				return s < strat.getNumStates() ? strat.getChoiceIndex(s) : 0;
			}

			@Override
			public Object getChoiceActionForState(int s)
			{
				return s < strat.getNumStates() ? strat.getChoice(s) : null;
			}
		};
	}
	
	private static DeterministicChoiceResolver makeChoiceResolver(final int[] adv)
	{
		return new DeterministicChoiceResolver() {

			@Override
			public int getChoiceIndexForState(int s)
			{
				return s < adv.length ? adv[s] : 0;
			}

			@Override
			public Object getChoiceActionForState(int s)
			{
				return s < adv.length ? (Integer)adv[s] : null;
			}
		};
	}
	
	@Override
	public String toString() {
		return "MDP Counterexample via DTMC Generator";
	}
}
