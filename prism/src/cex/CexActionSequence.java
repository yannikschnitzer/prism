package cex;

import java.util.Vector;

/**
 * @deprecated Use {@link CexPathStates} instead
 */
@Deprecated
public class CexActionSequence extends QualitativeCounterexample
{

	private Vector<String> cexActions;

	public CexActionSequence(Vector<String> cexActions)
	{
		this.cexActions = cexActions;
	}

	@Override
	public String toString()
	{
		return cexActions.toString();
	}
	
	@Override
	public String getSummaryString()
	{
		return cexActions.size() + " actions";
	}

}
