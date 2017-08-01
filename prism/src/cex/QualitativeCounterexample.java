package cex;

import prism.PrismException;
import prism.PrismLog;
import explicit.exporters.StateExporter;

/**
 * Base class for all qualitative counterexamples
 */
public class QualitativeCounterexample implements Counterexample
{

	@Override
	public void export(PrismLog out, StateExporter exp) throws PrismException
	{
		out.println(toString());
	}

	@Override
	public String getSummaryString()
	{
		throw new UnsupportedOperationException("Summary must be provided by overriding classes");
	}

}
