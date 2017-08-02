package explicit.exporters;

import static explicit.cex.gens.DTMCviaSMTCexGenerator.makeProbTerm;
import static explicit.cex.gens.DTMCviaSMTCexGenerator.makeStateVarName;
import static explicit.cex.gens.DTMCviaSMTCexGenerator.makeTransitionVarName;

import java.util.Iterator;
import java.util.Map.Entry;

import prism.PrismException;
import prism.PrismLog;
import explicit.DTMC;
import explicit.Model;
import explicit.cex.NormalizedDTMC;

/**
 * Class to export explicit-state models to smt2 format.
 */
public class Smt2ModelExporter extends ModelExporter
{
	public Smt2ModelExporter(Model model)
	{
		super(model);
	}

	@Override
	public void export(PrismLog out) throws PrismException
	{
		switch (model.getModelType()) {
		case DTMC:
			exportDTMC((DTMC) model, out);
			break;
		case CTMC:
			break;
		case CTMDP:
			break;
		case MDP:
			break;
		case PTA:
			break;
		case SMG:
			break;
		case STPG:
			break;
		default:
			throw new PrismException("Cannot export " + model.getModelType() + "s in smt2 format");
		}
	}

	public void exportDTMC(DTMC dtmc, PrismLog out)
	{
		// Add one variable per state
		for (int i = 0; i < dtmc.getNumStates(); i++) {
			out.println("(declare-fun " + makeStateVarName(i) + " () Real)");
		}

		// Encode that valuations must be from the interval [0..1]
		for (int i = 0; i < dtmc.getNumStates(); i++) {
			out.println("(assert (and (>= " + makeStateVarName(i) + " 0) (<= (+ " + makeStateVarName(i) + " (- 1)) 0)) )");
		}

		// Add one equation per transition
		for (int i = 0; i < dtmc.getNumStates(); i++) {
			boolean isTarget = false;
			if (dtmc instanceof NormalizedDTMC) {
				isTarget = i == ((NormalizedDTMC) dtmc).getTargetState();
			}

			if (isTarget) {
				addTargetEquation(out, i);
			} else {
				addTransitionEquation(out, i);
			}
		}
	}

	private void addTargetEquation(PrismLog out, int i)
	{
		out.println("(assert (! (= " + makeStateVarName(i) + " 1)");
		out.println(":named " + makeTransitionVarName(i) + "))");
	}

	private void addTransitionEquation(PrismLog out, int i)
	{
		// Format (assert (! (=(+ (* (- 1) s_0) s_1) 0)
		// :named transition_0))
		out.print("(assert (! ");

		// Encode flow invariant (exactly what flows into each node flows out through the transitions)
		out.print("(= ");
		out.print("(+ ");

		out.print("(* (- 1) " + makeStateVarName(i) + ")"); // In-flow

		Iterator<Entry<Integer, Double>> it = ((DTMC) model).getTransitionsIterator(i);
		while (it.hasNext()) {
			// Out-flow
			Entry<Integer, Double> entry = it.next();
			out.print("(* " + makeProbTerm(entry.getValue()) + " " + makeStateVarName(entry.getKey()) + ") ");
		}
		out.print(") 0");
		out.println(")");

		out.println(":named " + makeTransitionVarName(i) + "))");
	}
}
