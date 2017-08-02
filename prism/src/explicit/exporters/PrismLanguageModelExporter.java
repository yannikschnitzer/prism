package explicit.exporters;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import prism.PrismException;
import prism.PrismLog;
import prism.PrismUtils;
import explicit.DTMC;
import explicit.MDP;
import explicit.Model;

/**
 * Class to export explicit-state models to an equivalent PRISM language model description.
 */
public class PrismLanguageModelExporter extends ModelExporter
{
	public PrismLanguageModelExporter(Model model)
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
		boolean first;
		TreeMap<Integer, Double> sorted;
		// Output transitions to PRISM language file
		out.print(dtmc.getModelType().keyword() + "\n");
		out.print("module M\nx : [0.." + (dtmc.getNumStates() - 1) + "];\n");
		sorted = new TreeMap<Integer, Double>();
		for (i = 0; i < dtmc.getNumStates(); i++) {
			// Extract transitions and sort by destination state index (to match PRISM-exported files)
			Iterator<Map.Entry<Integer, Double>> iter = dtmc.getTransitionsIterator(i);
			while (iter.hasNext()) {
				Map.Entry<Integer, Double> e = iter.next();
				sorted.put(e.getKey(), e.getValue());
			}
			// Print out (sorted) transitions
			out.print("[]x=" + i + "->");
			first = true;
			for (Map.Entry<Integer, Double> e : sorted.entrySet()) {
				if (first)
					first = false;
				else
					out.print("+");
				// Note use of PrismUtils.formatDouble to match PRISM-exported files
				out.print(PrismUtils.formatDouble(e.getValue()) + ":(x'=" + e.getKey() + ")");
			}
			out.print(";\n");
			sorted.clear();
		}
		out.print("endmodule\n");
	}

	public void exportMDP(MDP mdp, PrismLog out)
	{
		int i, j, numChoices;
		boolean first;
		TreeMap<Integer, Double> sorted;
		Object action;
		// Output transitions to PRISM language file
		out.print(mdp.getModelType().keyword() + "\n");
		out.print("module M\nx : [0.." + (mdp.getNumStates() - 1) + "];\n");
		sorted = new TreeMap<Integer, Double>();
		for (i = 0; i < mdp.getNumStates(); i++) {
			numChoices = mdp.getNumChoices(i);
			for (j = 0; j < numChoices; j++) {
				// Extract transitions and sort by destination state index (to match PRISM-exported files)
				Iterator<Map.Entry<Integer, Double>> iter = mdp.getTransitionsIterator(i, j);
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> e = iter.next();
					sorted.put(e.getKey(), e.getValue());
				}
				// Print out (sorted) transitions
				action = mdp.getAction(i, j);
				out.print(action != null ? ("[" + action + "]") : "[]");
				out.print("x=" + i + "->");
				first = true;
				for (Map.Entry<Integer, Double> e : sorted.entrySet()) {
					if (first)
						first = false;
					else
						out.print("+");
					// Note use of PrismUtils.formatDouble to match PRISM-exported files
					out.print(PrismUtils.formatDouble(e.getValue()) + ":(x'=" + e.getKey() + ")");
				}
				out.print(";\n");
				sorted.clear();
			}
		}
		out.print("endmodule\n");
	}
}
