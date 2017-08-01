package cex;

import explicit.exporters.StateExporter;
import prism.PrismException;
import prism.PrismLog;

/**
 * A class representing that counterexample computation failed.
 * Stores error message indicating the reason of the failure.
 */
public class CexError implements Counterexample
{
	private PrismException e;

	public CexError(PrismException e) {
		this.e = e;
	}
	
	@Override
	public String toString()
	{
		return "Error: " + e.getMessage();
	}
	
	@Override
	public String getSummaryString()
	{
		return "Error";
	}

	@Override
	public void export(PrismLog out, StateExporter exp) throws PrismException
	{
		out.println(toString());
	}
}
