package explicit.cex.util;

import explicit.cex.gens.CexGenerator;

public class DummyStats extends CexStatistics
{

	private CexGenerator gen;

	public DummyStats(CexParams initialSetup, CexGenerator gen)
	{
		super(initialSetup, FailReason.NOT_STARTED, 0, 0);
		this.gen = gen;
	}
	
	@Override
	public String toString() {
		return "NOT STARTED: " + gen.getClass().getName() + " for " + params + " was setup but never executed"; 
	}

}
