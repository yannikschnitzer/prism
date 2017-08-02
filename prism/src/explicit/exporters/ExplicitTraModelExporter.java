package explicit.exporters;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import prism.Pair;
import prism.PrismException;
import prism.PrismLog;
import prism.PrismUtils;
import explicit.DTMC;
import explicit.MDP;
import explicit.Model;

/**
 * Class to export explicit-state models to Dot files.
 */
public class ExplicitTraModelExporter extends ModelExporter
{
	public ExplicitTraModelExporter(Model model)
	{
		super(model);
	}

	@Override
	public void export(PrismLog out) throws PrismException
	{
		switch (model.getModelType()) {
		case DTMC:
		case CTMC:
			exportMC((DTMC) model, out);
			break;
		case CTMDP:
			break;
		case MDP:
			exportMDP((MDP) model, out);
			break;
		case PTA:
			break;
		case SMG:
			break;
		case STPG:
			break;
		default:
			throw new PrismException("Cannot export " + model.getModelType() + "s in tra format");
		}
	}

	public void exportMC(DTMC dtmc, PrismLog out)
	{
		int i;
		TreeMap<Integer, Pair<Double, Object>> sorted;
		// Output transitions to .tra file
		out.print(originalModel != null ? originalModel.getNumStates() : dtmc.getNumStates());
		out.println(" " + dtmc.getNumTransitions());
		sorted = new TreeMap<Integer, Pair<Double, Object>>();
		for (i = 0; i < dtmc.getNumStates(); i++) {
			if (originalModel != null && originalModel.getNumStates() <= i) {
				// Do not export states introduced through preprocessing
				break;
			}

			// Extract transitions and sort by destination state index (to match PRISM-exported files)
			Iterator<Map.Entry<Integer,Pair<Double, Object>>> iter = dtmc.getTransitionsAndActionsIterator(i);
			if (iter.hasNext()) {
				//System.out.println("Proc ts for " + i);
			}

			while (iter.hasNext()) {
				Map.Entry<Integer, Pair<Double, Object>> e = iter.next();
				sorted.put(e.getKey(), e.getValue());
			}
			// Print out (sorted) transitions
			for (Map.Entry<Integer, Pair<Double, Object>> e : sorted.entrySet()) {
				// If applicable: Only print a transition if the target wasn't introduced through preprocessing
				if (originalModel != null && originalModel.getNumStates() <= e.getKey())
					continue;

				// Note use of PrismUtils.formatDouble to match PRISM-exported files
				out.print(exp.stateToString(i) + " " + exp.stateToString(e.getKey()) + " " + PrismUtils.formatDouble(e.getValue().first));
				Object action = e.getValue().second; 
				if (action != null && !"".equals(action))
					out.print(" " + action);
				out.print("\n");
			}
			sorted.clear();
		}
	}

	public void exportMDP(MDP mdp, PrismLog out)
	{
		int i, j, numChoices;
		Object action;
		TreeMap<Integer, Double> sorted;
		// Output transitions to .tra file
		out.print(originalModel != null ? originalModel.getNumStates() : mdp.getNumStates());
		out.println(mdp.getNumChoices() + " " + mdp.getNumTransitions());
		sorted = new TreeMap<Integer, Double>();
		for (i = 0; i < mdp.getNumStates(); i++) {
			if (originalModel != null && originalModel.getNumStates() <= i) {
				// Do not export states introduced through preprocessing
				break;
			}

			numChoices = mdp.getNumChoices(i);
			for (j = 0; j < numChoices; j++) {
				// Extract transitions and sort by destination state index (to match PRISM-exported files)
				Iterator<Map.Entry<Integer, Double>> iter = mdp.getTransitionsIterator(i, j);
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> e = iter.next();
					sorted.put(e.getKey(), e.getValue());
				}
				// Print out (sorted) transitions
				for (Map.Entry<Integer, Double> e : sorted.entrySet()) {
					// If applicable: Only print a transition if the target wasn't introduced through preprocessing
					if (originalModel != null && originalModel.getNumStates() <= e.getKey())
						continue;

					// Note use of PrismUtils.formatDouble to match PRISM-exported files
					out.print(exp.stateToString(i) + " " + exp.stateToString(j) + " " + e.getKey() + " " + PrismUtils.formatDouble(e.getValue()));
					action = mdp.getAction(i, j);
					out.print(action == null ? "\n" : (" " + action + "\n"));
				}
				sorted.clear();
			}
		}
	}
}
