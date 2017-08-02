package explicit.exporters;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import parser.State;
import prism.ModelType;
import prism.PrismException;
import prism.PrismLog;
import explicit.CTMC;
import explicit.DTMC;
import explicit.MDP;
import explicit.Model;

/**
 * Class to export explicit-state models to Dot files.
 */
public class DotModelExporter extends ModelExporter
{
	public DotModelExporter(Model model)
	{
		super(model);
	}
	
	@Override
	public void export(PrismLog out) throws PrismException
	{
		export(out, null, false);
	}

	/**
	 * Export the model in Dot format.
	 * 
	 * @param out PrismLog to send output to
	 * @param mark Subset of states to highlight (optional, can be null)
	 */
	public void export(PrismLog out, BitSet mark) throws PrismException
	{
		export(out, mark, false);
	}
	
	/**
	 * Export the model in Dot format.
	 * 
	 * @param out PrismLog to send output to
	 * @param mark Subset of states to highlight (optional, can be null)
	 * @param showStates Label states with variable values (if available)?
	 */
	public void export(PrismLog out, BitSet mark, boolean showStates) throws PrismException
	{
		// Header
		out.print("digraph " + model.getModelType() + " {\nsize=\"8,5\"\nnode [shape=box];\n");
		int n = model.getNumStates();
		int numChoices;
		for (int i = 0; i < n; i++) {
			// Style for each state
			if (mark != null && mark.get(i))
				out.print(i + " [style=filled  fillcolor=\"#cccccc\"]\n");
			// Transitions for state i
			Iterator<Map.Entry<Integer, Double>> iter;
			switch (model.getModelType()) {
			case DTMC:
				iter = ((DTMC) model).getTransitionsIterator(i);
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> e = iter.next();
					out.print(i + " -> " + e.getKey() + " [ label=\"");
					out.print(e.getValue() + "\" ];\n");
				}
			case CTMC:
				iter = ((CTMC) model).getTransitionsIterator(i);
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> e = iter.next();
					out.print(i + " -> " + e.getKey() + " [ label=\"");
					out.print(e.getValue() + "\" ];\n");
				}
				break;
			case CTMDP:
				break;
			case MDP:
				MDP mdp = (MDP) model;
				numChoices = mdp.getNumChoices(i);
				for (int j = 0; j < numChoices; j++) {
					Object action = mdp.getAction(i, j);
					String nij = "n" + i + "_" + j;
					out.print(i + " -> " + nij + " [ arrowhead=none,label=\"" + j);
					if (action != null)
						out.print(":" + action);
					out.print("\" ];\n");
					out.print(nij + " [ shape=point,width=0.1,height=0.1,label=\"\" ];\n");
					iter = mdp.getTransitionsIterator(i, j);
					while (iter.hasNext()) {
						Map.Entry<Integer, Double> e = iter.next();
						out.print(nij + " -> " + e.getKey() + " [ label=\"" + e.getValue() + "\" ];\n");
					}
				}
				break;
			case PTA:
				break;
			case SMG:
				break;
			case STPG:
				break;
			default:
				throw new PrismException("Cannot export " + model.getModelType() + "s in Dot format");
			}
		}
		// Append state info (if required)
		if (showStates) {
			List<State> states = model.getStatesList();
			if (states != null) {
				for (int i = 0; i < n; i++) {
					out.print(i + " [label=\"" + i + "\\n" + states.get(i) + "\"]\n");
				}
			}
		}
		// Footer
		out.print("}\n");
	}

	@Override
	public void exportWithStrat(PrismLog out, int strat[]) throws PrismException
	{
		if (model.getModelType() == ModelType.MDP) {
			exportMDPWithStrat((MDP) model, out, null, strat);
		} else {
			super.exportWithStrat(out, strat);
		}
	}

	/**
	 * Export the model in Dot format, highlighting a strategy.
	 * 
	 * @param out PrismLog to send output to
	 * @param mark Subset of states to highlight (optional, can be null)
	 * @param strat The strategy to highlight
	 */
	public void exportMDPWithStrat(MDP mdp, PrismLog out, BitSet mark, int strat[])
	{
		int i, j, numChoices;
		String nij;
		Object action;
		String style;
		out.print("digraph " + mdp.getModelType() + " {\nsize=\"8,5\"\nnode [shape=box];\n");
		for (i = 0; i < mdp.getNumStates(); i++) {
			if (mark != null && mark.get(i))
				out.print(i + " [style=filled  fillcolor=\"#cccccc\"]\n");
			numChoices = mdp.getNumChoices(i);
			for (j = 0; j < numChoices; j++) {
				style = (strat[i] == j) ? ",color=\"#ff0000\",fontcolor=\"#ff0000\"" : "";
				action = mdp.getAction(i, j);
				nij = "n" + i + "_" + j;
				out.print(i + " -> " + nij + " [ arrowhead=none,label=\"" + j);
				if (action != null)
					out.print(":" + action);
				out.print("\"" + style + " ];\n");
				out.print(nij + " [ shape=point,height=0.1,label=\"\"" + style + " ];\n");
				Iterator<Map.Entry<Integer, Double>> iter = mdp.getTransitionsIterator(i, j);
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> e = iter.next();
					out.print(nij + " -> " + e.getKey() + " [ label=\"" + e.getValue() + "\"" + style + " ];\n");
				}
			}
		}
		out.print("}\n");
	}
}
