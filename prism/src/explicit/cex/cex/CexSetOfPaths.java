package explicit.cex.cex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import parser.State;
import prism.PrismException;
import prism.PrismLog;
import explicit.Model;
import explicit.cex.util.CexParams;
import explicit.cex.util.CexPathSetStats;
import explicit.cex.util.CexStatistics;
import explicit.cex.util.ProbablePathIterator;
import explicit.cex.util.ValuationSet;
import explicit.exporters.StateExporter;

/**
 * A DTMC/MDP counterexample represented as a list of paths from the initial state(s) into the target set.
 */
public class CexSetOfPaths extends NondetCounterexample implements ProbablePathIterator
{

	/** Constituent paths */
	private final ArrayList<ProbabilisticPath> paths = new ArrayList<>();

	/** Cache of probability sum */
	private double cumulativeProbability = 0;
	/** Index of last path included in the cached probability sum */
	private int lastIndexInProbability = -1;
	
	/** State of the probable path iterator */
	private int nextIndexInIterator = 0;

	/** Optional reference to state info in the underlying model.
	 *  If set, this is propagated to the contained paths, making them usable as PathFullInfo objects */ 
	private List<State> statesList = null;

	/** Reference to the (original, unprocessed) model through which these paths lead */
	private final Model underlyingModel;

	public CexSetOfPaths(CexParams params, Model underlyingModel)
	{
		super(params);
		this.underlyingModel = underlyingModel;
	}

	/**
	 * Returns the number of paths in the counterexample
	 * @return Number of paths in counterexample
	 */
	public int size()
	{
		return paths.size();
	}

	/**
	 * Adds a new path to the counterexample
	 * @param p Path to add
	 */
	@Override
	public void add(ProbabilisticPath p)
	{
		if (statesList != null) p.setStateInfo(statesList);
		paths.add(p);
	}

	public Iterable<ProbabilisticPath> getPaths() {
		return paths;
	}

	@Override
	public double getProbabilityMass()
	{
		// Add probabilities of paths that have been added since the last call to this method
		for (int i = lastIndexInProbability + 1; i < paths.size(); i++) {
			cumulativeProbability += paths.get(i).getProbability();
		}
		lastIndexInProbability = paths.size() - 1;

		return cumulativeProbability;
	}

	@Override
	public CexStatistics generateStats()
	{
		double probOfLastPath = paths.size() > 0 ? paths.get(paths.size() - 1).getProbability() : 1;
		return new CexPathSetStats(getParams(), paths.size(), getProbabilityMass(), probOfLastPath, failReason, computationTimeInMs);
	}

	@Override
	public void export(PrismLog out, StateExporter exp) throws PrismException
	{
		for (ProbabilisticPath path : paths) {
			ProbabilisticPath restrictedPath = path;
			if (!fullExportModeEnabled) {
				restrictedPath = path.restrictTo(underlyingModel);
			}
			
			if (restrictedPath instanceof NondetPath)
				out.println(((NondetPath) restrictedPath).toStringWithActions(",", ",", ",", exp));
			else
				out.println(restrictedPath.toStringWithDelim(",", exp));
		}
	}

	@Override
	public Set<Object> getActions()
	{
		// TODO: This is of course very inefficient. If we end up needing this for more than stats / debugging, reimplementation will be necessary
		Set<Object> result = new TreeSet<>();
		for (ProbabilisticPath path : paths) {
			if (path instanceof NondetPath) {
				result.addAll(((NondetPath) path).getChoices());
			}
		}
		return result;
	}

	@Override
	public String getTypeString()
	{
		return "Path Set";
	}

	@Override
	public void fillValuationSet(ValuationSet vs) {
		for (ProbabilisticPath p : paths) {
			p.gatherValuations(vs);
		}
	}

	@Override
	public String toString() {
		StateExporter exp = new StateExporter();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < paths.size(); i++) {
			sb.append((i+1) + ": ");
			
			ProbabilisticPath path = paths.get(i);
			if (path instanceof NondetPath)
				sb.append((((NondetPath) path).toStringWithActions(",", ",", ",", exp)));
			else
				sb.append(path.toStringWithDelim(",", exp));
			
			sb.append(System.getProperty("line.separator"));
		}

		return sb.toString();
	}

	@Override
	public String getSummaryString()
	{
		return "Set of " + paths.size() + " paths";
	}

	public void setStateInfo(List<State> statesList)
	{
		for (ProbabilisticPath p : paths) p.setStateInfo(statesList);
		
		this.statesList = statesList; // For any future paths
	}

	@Override
	public boolean hasNext()
	{
		return nextIndexInIterator < paths.size();
	}

	@Override
	public boolean hasPrevious()
	{
		return nextIndexInIterator > 1 && !paths.isEmpty();
	}

	@Override
	public ProbabilisticPath next()
	{
		return paths.get(nextIndexInIterator++);
	}

	@Override
	public int nextIndex()
	{
		return nextIndexInIterator;
	}

	@Override
	public ProbabilisticPath previous()
	{
		nextIndexInIterator--;
		return paths.get(nextIndexInIterator-1);
	}

	@Override
	public int previousIndex()
	{
		return nextIndexInIterator - 2;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("Remvoing paths not supported");
	}

	@Override
	public void set(ProbabilisticPath arg0)
	{
		throw new UnsupportedOperationException("Replacing paths not supported");
	}

	@Override
	public void reset()
	{
		nextIndexInIterator = 0;
	}

}
