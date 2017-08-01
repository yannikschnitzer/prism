package explicit.cex.util;

import java.util.ArrayList;

import prism.PrismLog;
import explicit.cex.BidirectionalDTMCWrapper;
import explicit.cex.cex.ProbabilisticPath;
import explicit.cex.gens.ShortestPathFinder;

/**
 * A wrapper around the {@link ShortestPathFinder} that provides a bidirectional iterator interface.
 * TODO: Generalize shortest path finder to directly support MDPs?!
 */
public class ShortestPathBuffer implements ProbablePathIterator
{
	
	final ShortestPathFinder spf; 
	
	int nextIndex = 0;
	ArrayList<ProbabilisticPath> generatedPaths = new ArrayList<>();

	private boolean currentIsBuffered() {
		return nextIndex < generatedPaths.size();
	}
	
	public ShortestPathBuffer(BidirectionalDTMCWrapper model, int initialState, PrismLog log) {
		this.spf = new ShortestPathFinder(model, initialState, log);
	}
	
	@Override
	public void add(ProbabilisticPath e)
	{
		throw new UnsupportedOperationException("Cannot add paths -- paths are generated in the underlying shortest path finder");
	}

	@Override
	public boolean hasNext()
	{
		return currentIsBuffered() || spf.hasNext();
	}

	@Override
	public boolean hasPrevious()
	{
		return nextIndex > 1;
	}

	@Override
	public ProbabilisticPath next()
	{
		if (!currentIsBuffered()) {
			generatedPaths.add(spf.next());
		}
		
		return generatedPaths.get(nextIndex++);
	}

	@Override
	public int nextIndex()
	{
		return nextIndex;
	}

	@Override
	public ProbabilisticPath previous()
	{
		nextIndex--;
		return generatedPaths.get(nextIndex-1);
	}

	@Override
	public int previousIndex()
	{
		return nextIndex - 2;
	}

	@Override
	public void remove()
	{
		new UnsupportedOperationException("Cannot remove paths");
	}

	@Override
	public void set(ProbabilisticPath e)
	{
		new UnsupportedOperationException("Cannot replace paths");
	}

	@Override
	public void reset()
	{
		nextIndex = 0;
	}

}
