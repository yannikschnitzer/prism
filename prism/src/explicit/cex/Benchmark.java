package explicit.cex;

import java.util.BitSet;

import prism.ModelType;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.PrismPrintStreamLog;
import explicit.DTMC;
import explicit.DTMCSimple;
import explicit.DTMCSparse;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.gens.CexGenerator;
import explicit.cex.util.CexParams;

public class Benchmark implements Runnable
{

	public enum ModelImplementation {
		DTMCSimple, DTMCSparse, MDPSimple, MDPSparse;

		public ModelType getModelType() {
			switch (this) {
			case DTMCSimple:
			case DTMCSparse:
				return ModelType.DTMC;
			case MDPSimple:
			case MDPSparse:
				return ModelType.MDP;
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	// Constructor arguments
	public final String description;
	public final String traFile;
	public final double threshold;
	public final String label;
	public final String labFile;
	public final CounterexampleMethod method;
	public final ModelImplementation implementation;

	// Statistics
	public int runCount = 0;
	public int timeoutCount = 0;
	public int errorCount = 0;
	public long previousRuntime = 0;
	public long totalRuntime = 0;
	public long minRuntime = Long.MAX_VALUE;
	public long maxRuntime = 0;

	// Initialization
	private boolean initialized = false;
	/** Normalized DTMC created during initialization, cached for reuse across several runs */
	private NormalizedDTMC normalizedModel = null;
	/** Params created during initialization, cached for reuse across several runs */
	private CexParams params = null;
	
	private boolean isFinished = false;


	public Benchmark(String description, String traFile, double threshold, String label, String labFile, CounterexampleMethod method, ModelImplementation implementation) {
		this.description = description;
		this.traFile = traFile;
		this.threshold = threshold;
		this.label = label;
		this.labFile = labFile;
		this.method = method;
		this.implementation = implementation;
	}
	
	public boolean isFinished() {
		return isFinished;
	}

	public void initialize() throws PrismException {
		this.initialized = true;

		DTMCSimple simple = (DTMCSimple)CexGenCLI.readModel(traFile, ModelType.DTMC);
		BitSet targets = CexGenCLI.getLabelSet(label, labFile);
		CexGenCLI.addInitialStates(simple, labFile);

		DTMC model;
		switch (implementation) {
		case DTMCSimple:
			model = simple;
			break;
		case DTMCSparse:
			model = new DTMCSparse(simple);
			break;
		default:
			throw new UnsupportedOperationException("Model type not supported by benchmark suite");
		}

		params = new CexParams(threshold, 0, model, label, targets);
		normalizedModel = new NormalizedDTMC(model, params);
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public void run()
	{
		if (!initialized) throw new IllegalStateException("Benchmark " + this + " not initialized");
		isFinished = false;
		
		System.out.println("Starting benchmark " + this);
		PrismLog log = new PrismPrintStreamLog(System.out, true);
		log.setVerbosityLevel(PrismLog.VL_HIGH);
		CexGenerator gen = method.makeGenerator(normalizedModel, params, new PrismDevNullLog());

		long startTime = System.currentTimeMillis();

		ProbabilisticCounterexample cex = null;
		try {
			cex = gen.callFromCurrentThread();
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				this.timeoutCount++;
			}
			else {
				this.errorCount++;
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace();
			}}

		long endTime = System.currentTimeMillis();

		// Statistics
		if (cex != null) {
			cex.generateStats().print(log);
		}
		
		previousRuntime = endTime - startTime;
		this.runCount++;;
		this.totalRuntime += previousRuntime;
		this.minRuntime = Math.min(this.minRuntime, previousRuntime);
		this.maxRuntime = Math.max(this.maxRuntime, previousRuntime);
		this.isFinished = true;
	}

	public long getAverageRuntime() {
		return totalRuntime / runCount;
	}

}
