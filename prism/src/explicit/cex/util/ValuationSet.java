package explicit.cex.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import explicit.Model;
import parser.State;
import parser.ast.ModulesFile;

public class ValuationSet
{

	private final ModulesFile mf;
	private List<State> stateList;
	
	private final Map<Integer,Set<Object>> varIndexToValues = new TreeMap<>();
	
	public ValuationSet(Model model, ModulesFile mf) {
		if (model == null || mf == null)
			throw new IllegalArgumentException();
		//this.model = model;
		this.mf = mf;
		this.stateList = model.getStatesList();
		for (int i = 0; i < mf.getNumVars(); i++) {
			varIndexToValues.put(i, new TreeSet<Object>());
		}
	}
	
	public void addValuationForStateIndex(int i) {
		if (stateList.size() > i)
			addState(stateList.get(i));
	}
	
	public void addState(State state)
	{
		for (int i = 0; i < state.varValues.length; i++) {
			varIndexToValues.get(i).add(state.varValues[i]);
		}
	}
	
	public String valuationsToString() {
		StringBuilder sb = new StringBuilder();
		boolean newLine = false;
		for (int i : varIndexToValues.keySet()) {
			if (newLine) sb.append(System.getProperty("line.separator"));
			sb.append(mf.getVarName(i) + " \u2208 {");
			
			boolean comma = false;
			for (Object v : varIndexToValues.get(i)) {
				if (comma) sb.append(",");
				sb.append(v);
				comma = true;
			}
			
			sb.append("}");
			newLine = true;
		}
		return sb.toString();
	}
	
}
