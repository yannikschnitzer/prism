package explicit.cex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Map;
import java.util.Arrays;

import parser.ast.ModulesFile;
import prism.ModelType;
import prism.Prism;
import prism.PrismCL;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.PrismPrintStreamLog;
import prism.UndefinedConstants;
import simulator.ModulesFileModelGenerator;
import explicit.ConstructModel;
import explicit.DTMC;
import explicit.DTMCModelChecker;
import explicit.DTMCSimple;
import explicit.DTMCSparse;
import explicit.Distribution;
import explicit.MDPSimple;
import explicit.MDPSparse;
import explicit.Model;
import explicit.ModelExplicit;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.gens.CexGenerator;
import explicit.cex.util.CexParams;
import explicit.cex.util.ValuationSet;
import explicit.exporters.StateExporter;

/**
 * Simple command line interface for performing counterexample generation. 
 * This CLI is to be used by the developers, not the end users of PRISM.
 * Note: This class is now largely obsolete, as counterexample generation has now
 * been integrated into {@link prism.PrismCL}.
 */
public class CexGenCLI
{

	private static final int TIMEOUT_IN_SEC = 100;
	private static final int SHORT_RESULT = 0;
	private static final int PARTIAL_RESULT = 1;
	private static final int FULL_RESULT = 2;

	final int methodArg = 0;
	final int thresholdArg = 1;
	final int targetLabelArg = 2;
	final int longResultArg = 3;
	int firstPrismArg;
	int outputFormat;

	final String TRA_FILE_NAME = "tmp.tra";
	final String LAB_FILE_NAME = "tmp.lab";
	final String RESULT_FILE_NAME = "result.tra";

	final boolean constructModelDirectly = true;

	private PrismLog log = new PrismPrintStreamLog(System.out, true);

	private ModulesFile modulesFile = null;

	public static void main(String[] args) throws PrismException
	{	
		/*
		 * Example parameters:
		 * --local 0.95 elected ../prism-examples/leader/synchronous/leader5_2.pm ../prism-examples/leader/synchronous/leader.pctl -const L=1 -prop 1
		 * 
		 * Currently we only support properties of the form P<=p [F label]
		 * Above example: P<=0.95 [F elected]
		 */

		/*
		 * To avoid being forced to copy significant parts of the main PrismCL, we invoke it here explicitly,
		 * have it generate .tra/.lab files for us and then read those back in to get the model for counterexample generation
		 */

		if (args.length < 3 || args[0].equals("-h") || args[0].equals("--help")) {
			System.out.println("Use with arguments: [method] [prob-threshold] [target-label] [output format] model.pm model.prop -prop [index] -const [const values] ");
			System.out.println("Currently supported methods:");
			System.out.println("--kpath          DTMC: Explicit Path set via the recursive enumeration algorithm");
			System.out.println("--local          DTMC: Critical subsystems via Augmenting path fragments");
			System.out.println("--dtmc-smt       DTMC: Critical subsystems via SMT-encoding of the DTMC");
			System.out.println("--mdp-via-kpath  MDP: --local on DTMC induced by max. reachability strategy");
			System.out.println("--mdp-via-local  MDP: --local on DTMC induced by max. reachability strategy");
			System.out.println("--mdp-smt        MDP: Critical subsystems via SMT-encoding of the MDP");
			System.out.println("Optional output format:");
			System.out.println("--full-result    The full list of variable valuations is printed for each state");
			System.out.println("--print-vars var1,var2... Those variables in the comma-separated list are printed for each state");  
			return;
		}

		new CexGenCLI(args);
	}

	public CexGenCLI(String[] args) throws PrismException
	{
		log.setVerbosityLevel(PrismLog.VL_HIGH);

		// Set long result flag if applicable
		switch (args[longResultArg]) {
		case "--full-result":
			outputFormat = FULL_RESULT;
			firstPrismArg = longResultArg+1;
			break;
		case "--print-vars":
			outputFormat = PARTIAL_RESULT;
			firstPrismArg = longResultArg+2;
			break;
		default:
			outputFormat = SHORT_RESULT;
			firstPrismArg = longResultArg;
		}

		// Read counterexample generation method
		CounterexampleMethod method = CounterexampleMethod.UNKNOWN;
		switch (args[methodArg]) {
		case "--kpath":
			method = CounterexampleMethod.DTMC_EXPLICIT_KPATH;
			break;
		case "--local":
			method = CounterexampleMethod.DTMC_EXPLICIT_LOCAL;
			break;
		case "--dtmc-smt":
			method = CounterexampleMethod.DTMC_EXPLICIT_SMT;
			break;
		case "--mdp-via-kpath":
			method = CounterexampleMethod.MDP_VIA_DTMC_KPATH;
			break;
		case "--mdp-via-local":
			method = CounterexampleMethod.MDP_VIA_DTMC_LOCAL;
			break;
		case "--mdp-smt":
			method = CounterexampleMethod.MDP_EXPLICIT_SMT;
		default:
			System.out.println("Error: Unknown counterexample generation method " + args[methodArg]);
			return;
		}

		// Read probability threshold
		double threshold = 0;
		try {
			threshold = Double.parseDouble(args[thresholdArg]);
		} catch (NumberFormatException e) {
			throw new PrismException("Not a valid probability threshold: \"" + args[thresholdArg] + "\"");
		}
		
		// Parse var names to be included in output (if applicable)
		String[] parsedVarNames = null;
		if (outputFormat == PARTIAL_RESULT) {
			parsedVarNames = args[longResultArg+1].split(",");
		}

		generateExplicitModelFiles(Arrays.copyOfRange(args, firstPrismArg, args.length), TRA_FILE_NAME, LAB_FILE_NAME);

		// At this point, we have .tra/.lab input that we will now parse again to get the model for counterexample generation.
		// But we can also generate the model directly, obtaining the ModulesFile as well, if desired
		// This is now the default, we keep the readModel method just to have a method for reading .tra/.lab directly
		Model model = null;
		if (constructModelDirectly) {
			model = generateModelAndModulesFile(Arrays.copyOfRange(args, firstPrismArg, args.length));
		} else {
			model = readModel(TRA_FILE_NAME, method.getModelType());
		}

		ModelExplicit explicitModel = null;
		if (model instanceof ModelExplicit) {
			explicitModel = (ModelExplicit)model;
		} 

		if (explicitModel == null) {
			System.out.println("Wasn't able to generate model, will return");
		}

		final String targetLabel = args[targetLabelArg];
		// Right now we assume that the target set just corresponds to the set of states with a specific label
		BitSet target = getLabelSet(targetLabel, LAB_FILE_NAME);

		// Add initial states to model, unless we invoke Prism directly, which already does that for us
		if (!constructModelDirectly) {
			addInitialStates(explicitModel, LAB_FILE_NAME);
		}

		// ...which we then invoke
		CexParams params = makeParams(explicitModel, threshold, targetLabel, target);
		Model preprocessedModel = preprocessModel(method, explicitModel, params);

		ProbabilisticCounterexample cex = generateCounterexample(method, preprocessedModel, params);

		if (cex != null) {
			log.println("Will write result to " + RESULT_FILE_NAME);
			PrismFileLog out = PrismFileLog.create(RESULT_FILE_NAME);
			
			// Make exporter
			StateExporter exp = new StateExporter(explicitModel, modulesFile);
			if (outputFormat == FULL_RESULT) {
				for (String varName : modulesFile.getVarNames())
					exp.addVarToOutput(varName);
			} else if (outputFormat == PARTIAL_RESULT) {
				for (String varName : parsedVarNames) {
					exp.addVarToOutput(varName);
				}
			}
			
			cex.export(out, exp);
			
			ValuationSet vs = new ValuationSet(explicitModel, modulesFile);
			cex.fillValuationSet(vs);
			System.out.println(vs.valuationsToString());
		}
	}

	/**
	 * Compares two DTMCs for equality w.r.t transition relation and initial states.
	 * Might move this somewhere, was used for testing 
	 */
	@SuppressWarnings("unused")
	private void compareModels(DTMC m1, DTMC m2)
	{
		if (m1.getNumStates() != m2.getNumStates()) {
			System.out.println("Different number of states: " + m1.getNumStates() + " / " + m2.getNumStates());
			assert(false);
		}

		if (m1.getNumInitialStates() != m2.getNumInitialStates()) {
			System.out.println("Different number of initial states " + m1.getNumInitialStates() + " / " + m2.getNumInitialStates());
			assert(false);
		}

		for (int s1 : m1.getInitialStates()) {
			boolean found = false;
			for (int s2 : m2.getInitialStates()) {
				if (s1 == s2) found = true;
				break;
			}
			if (!found) {
				System.out.println("Initial state " + s1 + " is no initial state in the second model");
				assert(false);
			}
		}

		for (int i = 0; i < m1.getNumStates(); i++) {
			Distribution d1 = m1.getDistribution(i);
			Distribution d2 = m2.getDistribution(i);
			if (!d1.getSupport().equals(d2.getSupport())) {
				System.out.println("Different succs of state " + i + ": " + d1.getSupport() + " / " + d2.getSupport());
				assert(false);
			}

			for (int j : d1.getSupport()) {
				if (d1.getEntry(j).getValue().doubleValue() != d2.getEntry(j).getValue().doubleValue()) {
					System.out.println("Different probabilities: " + d1.getEntry(j).getValue() + " / " + d2.getEntry(j).getValue());
					assert(false);
				}
			}
		}
	}

	private CexParams makeParams(ModelExplicit model, double threshold, String targetLabel, BitSet targets)
	{
		assert (targets.cardinality() > 0);

		CexParams params = null;
		if (model.getNumInitialStates() == 1) {
			log.println("Will search for counterexample from " + model.getFirstInitialState());
			params = new CexParams(threshold, CexParams.UNBOUNDED, model.getFirstInitialState(), targetLabel, targets);
		}
		else {
			// TODO: Add parameter for specifying that no normalization is desired. In that case, null would be passed as distribution & Cex generation be performed separately from each state
			log.print("Will make uniform distribution over initial states " );
			for (int s : model.getInitialStates()) {
				log.print(s + " ");
			}
			log.println();
			params = new CexParams(threshold, CexParams.UNBOUNDED, CexParams.makeUniformDistribution(model), targetLabel, targets);
		}
		return params;
	}

	private Model preprocessModel(CounterexampleMethod method, ModelExplicit model, CexParams params)
	{
		switch (method.getModelType()) {
		case DTMC:
			return new NormalizedDTMC(new DTMCSparse((DTMCSimple) model), params);
		case MDP:
			return new MDPSparse((MDPSimple)model);
		default:
			return null;
		}
	}

	private ProbabilisticCounterexample generateCounterexample(CounterexampleMethod method, Model model, CexParams params)
	{
		CexGenRunner runner = new CexGenRunner(log);
		CexGenerator gen = null;
		try {
			gen = method.makeGenerator(model, params, log);
		} catch (Exception e) {
			System.out.println("Error: Unknown counterexample method selected. Will exit.");
			return null;
		}

		ProbabilisticCounterexample cex = runner.generateCex(gen, TIMEOUT_IN_SEC * 1000, true);
		if (cex.probabilityMassExceedsThreshold()) {
			log.println("Finished counterexample computation:");
		} else {
			log.println("Counterexample generation failed. Best Partial result computed within " + TIMEOUT_IN_SEC + "sec:");
		}
		cex.generateStats().print(log);
		return cex;
	}

	private Model generateModelAndModulesFile(String[] args) {
		try {
			// Simple example: parse a PRISM file from a file, construct the model and export to a .tra file
			PrismLog mainLog = new PrismPrintStreamLog(System.out);
			Prism prism = new Prism(mainLog);
			ModulesFile modulesFile = prism.parseModelFile(new File(args[0]));
			UndefinedConstants undefinedConstants = new UndefinedConstants(modulesFile, null);

			String constSwitch = null;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-const")) {
					constSwitch = args[i+1];
					break;
				}
			}

			if (constSwitch != null)
				undefinedConstants.defineUsingConstSwitch(constSwitch);
			modulesFile.setUndefinedConstants(undefinedConstants.getMFConstantValues());
			this.modulesFile = modulesFile;
			ConstructModel constructModel = new ConstructModel(prism);
			return constructModel.constructModel(new ModulesFileModelGenerator(modulesFile, prism));
		} catch (FileNotFoundException | PrismException e) {
			System.out.println("Error: " + e.getMessage());
			return null;
		}
	}

	private void generateExplicitModelFiles(String[] args, String traFile, String labFile)
	{
		// Note: The following now also works on my machine, but only because when building the model
		// from a .tra file, we now also allow proabilities in the format x,yz rather than x.yz
		String[] newArgs = Arrays.copyOf(args, args.length + 5);

		newArgs[newArgs.length - 5] = "-exportmodel";
		newArgs[newArgs.length - 4] = traFile;
		newArgs[newArgs.length - 3] = "-exportlabels";
		newArgs[newArgs.length - 2] = labFile;
		newArgs[newArgs.length - 1] = "-hybrid";
		PrismCL.main(newArgs);
		System.out.flush();

		/*// We don't verify the args but instead assume that they are correct PRISM args
		String[] newArgs = Arrays.copyOf(args, args.length + 3);

		// We add export flags to generate .tra/.lab files
		// Unfortunately, we have to get model and labs separately, because the explicit engine does
		// not support label export yet, while the other engines return doubles formatted in the
		// default locale, which for me means 0,xxx instead of the 0.xxx needed when reading the model back

		newArgs[newArgs.length - 3] = "-exportmodel";
		newArgs[newArgs.length - 2] = traFile;
		newArgs[newArgs.length - 1] = "-hybrid";
		PrismCL.main(newArgs);

		System.out.flush();

		newArgs[newArgs.length - 3] = "-exportlabels";
		newArgs[newArgs.length - 2] = labFile;
		newArgs[newArgs.length - 1] = "-hybrid";
		PrismCL.main(newArgs);

		System.out.flush();*/
	}

	public static final ModelExplicit readModel(String traFile, ModelType type) throws PrismException
	{
		ModelExplicit result = null;
		switch (type) {
		case DTMC:
			result = new DTMCSimple();
			break;
		case MDP:
			result = new MDPSimple();
		default:
			throw new IllegalArgumentException();
		}
		assert (new File(traFile).exists());
		result.buildFromPrismExplicit(traFile);
		return result;
	}

	public static final BitSet getLabelSet(String label, String labFile) throws PrismException
	{
		DTMCModelChecker mc = new DTMCModelChecker(null);
		Map<String, BitSet> labels = mc.loadLabelsFile(labFile);
		BitSet target = labels.get(label);
		if (target == null)
			throw new PrismException("Unknown label \"" + label + "\"");
		else
			return target;
	}

	public static void addInitialStates(ModelExplicit dtmc, String labFile) throws PrismException
	{
		BitSet init = getLabelSet("init", labFile);
		assert (init.cardinality() > 0);
		for (int s = init.nextSetBit(0); s != -1; s = init.nextSetBit(s + 1)) {
			dtmc.addInitialState(s);
		}
	}
}
