package explicit.cex.util;

public class StatisticsContextWrapper extends CexStatistics
{

	private CexStatistics wrapped;
	private String statsPrefix;

	public StatisticsContextWrapper(CexStatistics wrapped, String statsPrefix)
	{
		super(wrapped.params, wrapped.failReason, wrapped.prob, wrapped.computationTimeInMs);
		this.wrapped = wrapped;
		this.statsPrefix = statsPrefix;
	}
	
	@Override
	public String toString() {
		return statsPrefix + ": " + wrapped;
	}

}
