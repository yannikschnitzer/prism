package explicit.cex.cex;

import prism.PrismException;
import prism.PrismLog;
import explicit.cex.gens.CexGenerator;
import explicit.cex.util.CexParams;
import explicit.cex.util.CexStatistics;
import explicit.cex.util.DummyStats;
import explicit.cex.util.ValuationSet;
import explicit.exporters.StateExporter;

public class DummyCounterexample extends ProbabilisticCounterexample
{

	private CexGenerator gen;

	public DummyCounterexample(CexParams params, CexGenerator gen)
	{
		super(params);
		this.gen = gen;
	}

	@Override
	public double getProbabilityMass()
	{
		return 0;
	}

	@Override
	public CexStatistics generateStats()
	{
		return new DummyStats(getParams(), gen);
	}

	@Override
	public void export(PrismLog out, StateExporter exp) throws PrismException
	{
		throw new PrismException("Dummy counterexample can't be exported");
	}

	@Override
	public String getTypeString()
	{
		return "Dummy";
	}

	@Override
	public void fillValuationSet(ValuationSet vs) throws PrismException
	{
		throw new PrismException("Dummy counterexamples do not contain any valuations");
	}

	@Override
	public String getSummaryString()
	{
		return "Dummy";
	}

}
