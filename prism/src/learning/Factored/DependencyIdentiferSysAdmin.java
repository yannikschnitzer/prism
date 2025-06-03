package learning.Factored;

import parser.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DependencyIdentiferSysAdmin implements DependencyIdentifier{
    private final HashMap<KeyTriple, String> identifiers;

    public DependencyIdentiferSysAdmin() {
        this.identifiers = new HashMap<>();
    }

    @Override
    public String getIdentifier(State state, List<State> marginalStates, int state_index, int action, int module) {
        List<State> relevantStates = new ArrayList<>();

        int numModules = marginalStates.size();

        relevantStates.add(marginalStates.get((module + 1) % numModules));
        relevantStates.add(marginalStates.get(module == 0 ? numModules - 1 : module - 1));

        Object timer = marginalStates.getFirst().varValues[marginalStates.getFirst().varValues.length - 1];

        String id = computeIdentifier(marginalStates.get(module), relevantStates, timer, state_index, action, module).intern();
        identifiers.put(new KeyTriple(state_index, action, module), id);
        return id;
    }

    @Override
    public String getIdentifier(int state_index, int action, int module) {
        return identifiers.get(new KeyTriple(state_index, action, module));
    }

    private String computeIdentifier(State marginalState, Object relevantStates, Object timer,int state_index, int action, int module) {
        return marginalState.toString() + "-" + relevantStates + "-" + timer + "-" + action + "-" + module;
    }

    private record KeyTriple(int x, int y, int z) {
    }
}
