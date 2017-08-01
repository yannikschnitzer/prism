package explicit.cex.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import prism.PrismLog;
import prism.PrismPrintStreamLog;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestDTMCTransformations.class, TestNormalizedDTMC.class, TestDTMCSparse.class, TestShortestPathFinder.class, TestPathSetCexGenerator.class, TestCriticalSubsystem.class, TestMDPviaDTMC.class })
public class CexTestSuite
{
	
	/** Accuracy to be used in assertEquals() for doubles */
	public static final double EPS = 1e-16;
	
	/** Max. log level to be used in the tests; set this to VL_DEFAULT to mute most test output */
	public static final int MAX_VERBOSITY = PrismLog.VL_ALL;
	
	public static int applyGlobalLoggingRestrictions(int level) {
		return Math.min(MAX_VERBOSITY, level);
	}
	
	public static final PrismLog testLog = new PrismPrintStreamLog(System.out);

	public static void setTestLogVerbosityLevel(int verbosity)
	{
		testLog.setVerbosityLevel(applyGlobalLoggingRestrictions(verbosity));
	}
	
}
