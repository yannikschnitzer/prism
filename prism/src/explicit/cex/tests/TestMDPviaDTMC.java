package explicit.cex.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import prism.PrismLog;
import explicit.MDP;
import explicit.cex.CounterexampleMethod;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.gens.MDPViaDTMCCexGenerator;
import explicit.cex.util.CexParams;

@RunWith(JUnit4.class)
public class TestMDPviaDTMC
{
	
	private static final int VERBOSITY = CexTestSuite.applyGlobalLoggingRestrictions(PrismLog.VL_ALL);
	
	private static final MDPwithTargets[] testMDPs = new MDPwithTargets[]{
		ExampleMDPs.initialChoicePathSet(4, 10, 1),
		ExampleMDPs.binaryChoiceTree(3, 1)
	};

	@Test
	public void viaKPath() {
		viaMethod(CounterexampleMethod.DTMC_EXPLICIT_KPATH);
	}
	
	@Test
	public void viaLocalSearch() {
		viaMethod(CounterexampleMethod.DTMC_EXPLICIT_LOCAL);
	}
	
	private void viaMethod(CounterexampleMethod method)
	{
		CexTestSuite.setTestLogVerbosityLevel(VERBOSITY);
		
		for (MDPwithTargets triple : testMDPs) {
			MDP mdp = triple.mdp;
			CexParams params = new CexParams(1, CexParams.UNBOUNDED, mdp.getFirstInitialState(), "", triple.targets);
			MDPViaDTMCCexGenerator gen = new MDPViaDTMCCexGenerator(mdp, method, params, CexTestSuite.testLog);
			
			try {
				ProbabilisticCounterexample cex = gen.callFromCurrentThread();
				assertNotNull("Couldn't find counterexample", cex);
			} catch (Exception e) {
				assertTrue("An exception happened during counterxample generation", false);
				e.printStackTrace();
			}
		}
	}

}
