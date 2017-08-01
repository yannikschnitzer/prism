package explicit.cex.gens;

import prism.PrismLog;
import explicit.cex.NormalizedDTMC;
import explicit.cex.cex.DummyCounterexample;
import explicit.cex.cex.ProbabilisticCounterexample;
import explicit.cex.util.CexParams;

public class DTMCCexGenerator extends CexGenerator
{
	
	/** The model to generate a counterexample for */
	protected final NormalizedDTMC dtmc;
	
	public DTMCCexGenerator(NormalizedDTMC dtmc, CexParams params, PrismLog log)
	{
		super(params, log);
		this.dtmc = dtmc;
		partialResult = new DummyCounterexample(params, this); // To guarantee non-null value
	}

	@Override
	public ProbabilisticCounterexample call() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

}
