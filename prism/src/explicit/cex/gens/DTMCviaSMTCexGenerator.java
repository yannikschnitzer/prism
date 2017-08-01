package explicit.cex.gens;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.CriticalSubsystem;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.util.CexParams;
import explicit.exporters.ExportType;
import prism.PrismFileLog;
import prism.PrismLog;

/**
 * Computes critical subsystem counterexamples via an SMT solver.
 * Note: This assumes that z3 is in the path.
 */
public class DTMCviaSMTCexGenerator extends DTMCCexGenerator
{
	
	private static final double EPS = 1e-8;
	private static final String SOLVER_PATH = "z3";
	private static final String SMT_FILE_NAME = "dtmc.smt2"; 
	
	public DTMCviaSMTCexGenerator(NormalizedDTMC dtmc, CexParams params, PrismLog log) {
		super(dtmc, params, log);
	}

	@Override
	public ProbabilisticCounterexample call() throws Exception
	{
		assert(params.hasExplicitInitialState());
		
		log.println("Will export model to " + SMT_FILE_NAME + " ...");
		long startTime = System.currentTimeMillis();
		PrismFileLog smtFile = PrismFileLog.create(SMT_FILE_NAME);		
		smtFile.println("(set-option :produce-unsat-cores true)");
		
		dtmc.export(smtFile, ExportType.SMT);
		
		// Counterexample threshold
		smtFile.println("(assert (! (<= " + makeStateVarName(params.getInitialState()) + " " + makeProbTerm(params.getThreshold()) + ")");
		smtFile.println(" :named reachability))");
		smtFile.println("(check-sat)");
		
		//smtFile.println();
		//smtFile.println("(get-model)");

		smtFile.println();
		smtFile.println("(get-unsat-core)");
		smtFile.close();
		
		long endTime = System.currentTimeMillis();
		log.println("Export complete. (took " + (endTime-startTime) + "ms)");
		
		log.println("Will now run SMT solver");
		startTime = endTime;
		
		String result = runSmt();
		ArrayList<Integer> statesInUnsatCore = null;
		if (result.equals(""))
			return null;
		else
			statesInUnsatCore = parseResult(result.substring(1, result.length() - 1));
		
		if (statesInUnsatCore != null) {
			 endTime = System.currentTimeMillis();
			 log.println("Finished SMT Solver (took " + (endTime-startTime) + "ms)");
		}
		
		CriticalSubsystem cs = new CriticalSubsystem(params, dtmc, smtFile, statesInUnsatCore);
		cs.triggerClosureRecomputation();
		cs.setComputationTime(System.currentTimeMillis() - startTime);
		return cs;
	}
	
	private ArrayList<Integer> parseResult(String result)
	{
		ArrayList<Integer> statesInUnsatCore = new ArrayList<>();
	
		String[] transitions = result.split(" ");
		log.println("Found critical subsystem with " + (transitions.length - 1) + " states");
		
		for (String transition : transitions) {
			if (transition.equals("reachability")) continue;
			statesInUnsatCore.add(Integer.parseInt(transition.substring(11)));
		}
		
		log.println("Counterexample consists of: ", PrismLog.VL_ALL);
		for (int i : statesInUnsatCore) {
			log.print(i + " ", PrismLog.VL_ALL);
		}
		log.println("", PrismLog.VL_ALL);
		
		return statesInUnsatCore;
	}

	private String runSmt()
	{
	      try
	      {
	         Runtime rt = Runtime.getRuntime();
	         String cmdString = SOLVER_PATH + " " + SMT_FILE_NAME;

	         System.out.println(cmdString);
	         Process pr = rt.exec(cmdString);

	         BufferedReader input = new BufferedReader(new InputStreamReader(
	                                                   pr.getInputStream()));

	         ArrayList<String> lines = new ArrayList<>();
	         String line;

	         while ((line = input.readLine()) != null)
	         {
	            lines.add(line);
	         }

	         int exitVal = pr.waitFor();
	         if (exitVal != 0) {
	        	 log.println(SOLVER_PATH + " exited with error code " + exitVal + " --> will abort");
	        	 return "";
	         }
	         
	         if (lines.get(0).equals("sat")) {
	        	 log.println("There is no counterexample for " + params + " --> will abort");
	        	 return "";
	         }
	         
	         assert(lines.get(0).equals("unsat"));
	         return lines.get(1);
	      }
	      catch (Exception e)
	      {
	         log.println("An execption occurred while executing " + SOLVER_PATH + ": " + e.toString() + " --> will abort");
	         e.printStackTrace();
	         return "";
	      }
	}

	public static String makeTransitionVarName(int i)
	{
		return "transition_" + i;
	}

	public static String makeStateVarName(int i)
	{
		return "s_" + i;
	}
	
	public static String makeProbTerm(double value)
	{
		assert(value > 0);
		if (value == 1.0) return "1";
		
		long nom = 0;
		long denom = 1;
		
		while ( Math.abs((double)nom / (double)(denom) - value) > EPS) {
			denom *= 10;
			nom = new Double(value * (double)denom).longValue();
		}
		
		return "(/ " + nom + " " + denom + ")";
	}
	
	@Override
	public String toString() {
		return "Subsystem via SMT Generator";
	}

}
