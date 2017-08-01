package explicit.cex;

import java.util.LinkedList;
import java.util.List;

import parser.ast.ModulesFile;
import explicit.Model;
import explicit.exporters.StateExporter;

public class CexSettings
{
	private static final String DEFAULT_METHOD = "kpath";
	private static final int DEFAULT_TIMEOUT_IN_SECS = 100;
	
	/** File to write counterexample to */
	public String counterexampleResultFilename = null;
	/** Counterexample method as provided in the command line switch */
	public String methodString = DEFAULT_METHOD;
	/** Should the valuation of all variables be included in the result file? */
	public boolean printAllVars = false;
	/** Include the valuation of these vars in the result file.
	 * Only applies if !printAllVars */
	public List<String> varsToPrint = new LinkedList<>();
	/** Abort computation (in a controlled fashion) after this many seconds */
	public int timeoutInSecs = DEFAULT_TIMEOUT_IN_SECS;
	/** K-Path: Stop after at most this many paths */ 
	public int maxNumPaths = Integer.MAX_VALUE;
	/** Subsystems: Export model including sink state, i.e. a legal DTMC/MDP model */
	public boolean doExportFullProcessedModel = false;
	/** Should we repeatedly print progress during counterexample computation? */
	public boolean printProgress = false;

	/**
	 * Returns the applicable counterexample method based on the String parameter passed on the command line
	 * and the type of the given model 
	 * @param model Model on which the counterexample computation will be performed
	 * @return Appropriate counterexample method enum
	 */
	public CounterexampleMethod getMethod(Model model) {
		switch (model.getModelType()) {
		case DTMC:
			switch (methodString) {
			case "kpath":
				return CounterexampleMethod.DTMC_EXPLICIT_KPATH;
			case "local":
				return CounterexampleMethod.DTMC_EXPLICIT_LOCAL;
			case "smt":
				return CounterexampleMethod.DTMC_EXPLICIT_SMT;
			default:
				return CounterexampleMethod.UNKNOWN;
			}
		case MDP:
			switch (methodString) {
			case "kpath":
				return CounterexampleMethod.MDP_VIA_DTMC_KPATH;
			case "local":
				return CounterexampleMethod.MDP_VIA_DTMC_LOCAL;
			case "smt":
				return CounterexampleMethod.MDP_EXPLICIT_SMT;
			default:
				return CounterexampleMethod.UNKNOWN;
			}
		default:
			return CounterexampleMethod.UNKNOWN;
		}
	}
	
	/**
	 * Constructs a StateExporter instance for counterexample export
	 * @param model
	 * @param modulesFile
	 * @return Formatter for states to be used in counterexample export
	 */
	public StateExporter makeStateExporter(Model model, ModulesFile modulesFile)
	{
		StateExporter exp = new StateExporter();
		
		if (printAllVars || !varsToPrint.isEmpty()) {
			// Verbose format based on settings
			exp = new StateExporter(model, modulesFile);
			if (printAllVars) {
				for (String varName : modulesFile.getVarNames())
					exp.addVarToOutput(varName);
			} else {
				for (String varName : varsToPrint) {
					exp.addVarToOutput(varName);
				}
			}
		}
		
		return exp;
	}
}
