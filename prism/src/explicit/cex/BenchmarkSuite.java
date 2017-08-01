package explicit.cex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import explicit.cex.Benchmark.ModelImplementation;
import prism.PrismException;

public class BenchmarkSuite
{

	private List<Benchmark> benchmarks = new ArrayList<>();
	private long timeout;
	private int runCountPerBenchmark;

	public BenchmarkSuite(long timeoutInMs)
	{
		this(timeoutInMs, 1);
	}

	public BenchmarkSuite(long timeoutInMs, int runCountPerBenchmark)
	{
		this.timeout = timeoutInMs;
		this.runCountPerBenchmark = runCountPerBenchmark;
	}

	public void addBenchmark(Benchmark bm)
	{
		benchmarks.add(bm);
	}
	
	private void addBenchmarks(List<Benchmark> bms)
	{
		benchmarks.addAll(bms);
	}

	public void runBenchmarks()
	{
		System.out.println("Will now run " + benchmarks.size() + " benchmarks, " + runCountPerBenchmark + " times each");

		for (int i = 1; i <= runCountPerBenchmark; i++) {
			System.out.println("Starting benchmark round " + i);

			for (Benchmark bm : benchmarks) {
				try {
					runBenchmark(bm);
				} catch (PrismException e) {
					System.out.println("There was an error during benchmark execution. Will return");
					e.printStackTrace();
					return;
				}
			}

			if (i < runCountPerBenchmark)
				System.out.println("Intermediate result after round " + i);
			else
				System.out.println("Done with all benchmarks. Results: ");
			printStatistics();
		}

		System.out.println("Will exit now.");
		System.exit(0);
	}

	private void runBenchmark(Benchmark bm) throws PrismException
	{
		bm.initialize();

		long deadline = System.currentTimeMillis() + timeout;

		ExecutorService exec = null;
		FutureTask<?> bmTask = new FutureTask<Void>(bm, null);
		exec = Executors.newSingleThreadExecutor();
		exec.submit(bmTask);

		int wokeUp = 0;
		do {
			wokeUp++;
			//Thread.yield();
			sleep(200);
		} while (System.currentTimeMillis() < deadline && !bmTask.isDone());
		System.out.println("This thread was active " + wokeUp + " times");

		bmTask.cancel(true);
		if (bmTask.isCancelled()) {
			System.out.println("Cancelled execution of " + bm + " after timeout of " + (timeout / 1000.0d) + "sec");
		} else {
			System.out.println(bm + " finished after " + (bm.previousRuntime / 1000.0d) + "sec");
		}

		exec.shutdownNow();
		while (!bm.isFinished()) sleep(50);
	}

	private void sleep(long millis)
	{
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void printStatistics()
	{
		int maxLength = 0;
		for (Benchmark bm : benchmarks) {
			maxLength = Math.max(maxLength, bm.toString().length());
		}

		System.out.printf("%" + (maxLength + 2) + "s %15s %15s %11s %10s %10s", "Title", "Impl.", "Method", "Avg. of " + runCountPerBenchmark, "Min. Time",
				"Max. Time");
		System.out.println();
		for (Benchmark bm : benchmarks) {
			String formatString = "%" + (maxLength + 2) + "s %15s %15s %11.4f %10.4f %10.3f";
			System.out.printf(formatString, bm.toString(), bm.implementation.toString(), bm.method.toString(), bm.getAverageRuntime() / 1000.0d,
					bm.minRuntime / 1000.0d, bm.maxRuntime / 1000.0d);
			System.out.print(bm.timeoutCount > 0 ? "  " + bm.timeoutCount + " TIMEOUTS" : "");
			System.out.print(bm.errorCount > 0 ? "  " + bm.errorCount + " ERRORS" : "");
			System.out.println();
		}
	}

	public static void main(String[] args)
	{

		// Let's compare the DTMC implementations...
		BenchmarkSuite suite = new BenchmarkSuite(50 * 1000, 5);

		suite.addBenchmarks(crowds5_4);
		suite.addBenchmarks(leader4_8);
		suite.addBenchmarks(crowds5_10);

		suite.runBenchmarks();
	}

	public static List<Benchmark> crowds5_4 = Arrays.asList(new Benchmark("Crowds 5/4 0.11", "crowds_5_4.tra", 0.11, "pos", "crowds_5_4.lab",
			CounterexampleMethod.DTMC_EXPLICIT_KPATH, ModelImplementation.DTMCSimple), new Benchmark("Crowds 5/4 0.11", "crowds_5_4.tra", 0.11, "pos",
			"crowds_5_4.lab", CounterexampleMethod.DTMC_EXPLICIT_KPATH, ModelImplementation.DTMCSparse), new Benchmark("Crowds 5/4 0.23", "crowds_5_4.tra",
			0.23, "pos", "crowds_5_4.lab", CounterexampleMethod.DTMC_EXPLICIT_LOCAL, ModelImplementation.DTMCSimple), new Benchmark("Crowds 5/4 0.23",
			"crowds_5_4.tra", 0.23, "pos", "crowds_5_4.lab", CounterexampleMethod.DTMC_EXPLICIT_LOCAL, ModelImplementation.DTMCSparse));

	public static List<Benchmark> leader4_8 = Arrays.asList(new Benchmark("Leader 4/8 0.96", "leader_4_8.tra", 0.96, "elected", "leader_4_8.lab",
			CounterexampleMethod.DTMC_EXPLICIT_KPATH, ModelImplementation.DTMCSimple), new Benchmark("Leader 4/8 0.96", "leader_4_8.tra", 0.96, "elected",
			"leader_4_8.lab", CounterexampleMethod.DTMC_EXPLICIT_KPATH, ModelImplementation.DTMCSparse), new Benchmark("Leader 4/8 0.40", "leader_4_8.tra",
			0.40, "elected", "leader_4_8.lab", CounterexampleMethod.DTMC_EXPLICIT_LOCAL, ModelImplementation.DTMCSimple), new Benchmark("Leader 4/8 0.40",
			"leader_4_8.tra", 0.40, "elected", "leader_4_8.lab", CounterexampleMethod.DTMC_EXPLICIT_LOCAL, ModelImplementation.DTMCSparse));

	public static List<Benchmark> crowds5_10 = Arrays.asList(new Benchmark("Crowds 5/10 0.14", "crowds_5_10.tra", 0.14, "pos", "crowds_5_10.lab",
			CounterexampleMethod.DTMC_EXPLICIT_KPATH, ModelImplementation.DTMCSimple), new Benchmark("Crowds 5/10 0.14", "crowds_5_10.tra", 0.14, "pos",
			"crowds_5_10.lab", CounterexampleMethod.DTMC_EXPLICIT_KPATH, ModelImplementation.DTMCSparse), new Benchmark("Crowds 5/10 0.35", "crowds_5_10.tra",
			0.35, "pos", "crowds_5_10.lab", CounterexampleMethod.DTMC_EXPLICIT_LOCAL, ModelImplementation.DTMCSimple), new Benchmark("Crowds 5/10 0.35",
			"crowds_5_10.tra", 0.35, "pos", "crowds_5_10.lab", CounterexampleMethod.DTMC_EXPLICIT_LOCAL, ModelImplementation.DTMCSparse));
}
