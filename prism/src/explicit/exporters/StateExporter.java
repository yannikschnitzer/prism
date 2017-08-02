package explicit.exporters;

import java.util.LinkedList;
import java.util.List;

import parser.State;
import parser.ast.ModulesFile;
import explicit.Model;

/**
 * A class for configuring the way that states are formatted in exports.
 * See {@link cex.Counterexample#export(prism.PrismLog, StateExporter)}.
 */
public class StateExporter
{

	private final ModulesFile mf;
	private final Model model;
	private final List<String> varNames = new LinkedList<>();
	private final List<Integer> varIndices = new LinkedList<>();
	
	/**
	 * Constructs a default state exporter that only prints state indices
	 */
	public StateExporter() {
		this(null, null);
	}

	/** 
	 * Constructs a state exporter that uses the information in the given model and modules file
	 * to include variable valuations in the state export.
	 * The variables to include in the export have to be added via subsequent calls to {@link #addVarToOutput(String)}
	 * @param model Model with state information
	 * @param mf Modules file with variable information
	 */
	public StateExporter(Model model, ModulesFile mf) {
		this.model = model;
		this.mf = mf;
	}
	
	public void addVarToOutput(String varName) {
		if (mf == null) throw new IllegalStateException("Cannot add variable: Modules info unknown");
		assert(mf.getVarIndex(varName) >= 0);
		
		varNames.add(varName);
		varIndices.add(mf.getVarIndex(varName));
	}
	
	public String stateToString(int stateIndex) {
		if (mf == null || varIndices.size() == 0)
			return ""+stateIndex;
		else {
			List<State> states = model.getStatesList();
			if (states.size() > stateIndex && states.get(stateIndex) != null)
				return stateIndex + ":" + model.getStatesList().get(stateIndex).toPartialString(varIndices, varNames);
			else
				return stateIndex + " (introduced in preprocessing)";
		}
	}
	
}
