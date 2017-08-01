package explicit.cex.cex;

import java.util.Set;

import explicit.cex.util.CexParams;

public abstract class NondetCounterexample extends ProbabilisticCounterexample
{

	public NondetCounterexample(CexParams params)
	{
		super(params);
	}

	public abstract Set<Object> getActions(); 

}
