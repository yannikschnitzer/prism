package cex;

import prism.PrismException;
import prism.PrismLog;
import explicit.exporters.StateExporter;

/**
 * Interface for all kinds of counterexamples.
 */
public interface Counterexample
{

	/**
	 * Writes a string representation of the counterexample to the provided output.
	 * If either model or modulesFile are null, a short representation is printed which
	 * contains only the info explicit in the counterexample.
	 * Otherwise, a longer representation that includes the variable valuations is provided 
	 * @param out Output to print to
	 * @param doExportProcessedModel 
	 * @param model The model the correctness of which this counterexample refutes
	 * @param modulesFile Underlying modules file of the model
	 * @throws PrismException
	 */
	public abstract void export(PrismLog out, StateExporter exp) throws PrismException;
	
	/**
	 * Returns a one line summary of the counterexample
	 * @return One line summary of the counterexample
	 */
	public String getSummaryString();
	
}
