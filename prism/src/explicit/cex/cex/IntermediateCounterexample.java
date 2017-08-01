package explicit.cex.cex;

import prism.PrismException;
import prism.PrismLog;
import explicit.cex.util.CexStatistics;
import explicit.cex.util.StatisticsContextWrapper;
import explicit.cex.util.ValuationSet;
import explicit.exporters.StateExporter;

public class IntermediateCounterexample extends ProbabilisticCounterexample
{

	private final ProbabilisticCounterexample wrappedCex;
	private final String reason;
	private final String statsPrefix;

	public IntermediateCounterexample(ProbabilisticCounterexample wrappedCex, String reason, String statsPrefix)
	{
		super(wrappedCex.getParams());
		this.wrappedCex = wrappedCex;
		this.reason = reason;
		this.statsPrefix = statsPrefix;
	}

	@Override
	public double getProbabilityMass()
	{
		return wrappedCex.getProbabilityMass();
	}

	@Override
	public CexStatistics generateStats()
	{
		return new StatisticsContextWrapper(wrappedCex.generateStats(), statsPrefix);
	}

	@Override
	public void export(PrismLog out, StateExporter exp) throws PrismException
	{
		String delimLine = "-------------------------------------------------------------";
		
		out.println(delimLine);
		out.println("WARNING: Printing intermediate counterexample representation:");
		out.println(reason);
		out.println(delimLine);
		wrappedCex.export(out, exp);
	}

	@Override
	public String getTypeString()
	{
		return "Intermediate " + wrappedCex.getTypeString();
	}

	@Override
	public void fillValuationSet(ValuationSet vs) throws PrismException
	{
		wrappedCex.fillValuationSet(vs);
	}

	@Override
	public String getSummaryString()
	{
		return "Intermediate counterxample representation: " + wrappedCex.getSummaryString();
	}

}
