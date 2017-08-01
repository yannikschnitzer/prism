package explicit.cex.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class CexTestRunner
{

	public static void main(String[] args)
	{
		System.out.println("Will run test suite...");

		Result result = JUnitCore.runClasses(CexTestSuite.class);

		if (result.wasSuccessful()) {
			System.out.println("Passed all tests");
		} else {
			System.out.println(result.getFailureCount() + " test(s) failed:");
			for (Failure failure : result.getFailures()) {
				System.out.println(failure.toString());
			}
		}
	}

}
