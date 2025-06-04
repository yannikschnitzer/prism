package learning.Factored;

import parser.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DependencyIdentiferStockTrading implements DependencyIdentifier{
    private final HashMap<KeyTriple, String> identifiers;

    public DependencyIdentiferStockTrading() {
        this.identifiers = new HashMap<>();
    }

    @Override
    public String getIdentifier(State state, List<State> marginalStates, int state_index, int action, int module) {

        Object timer = marginalStates.getFirst().varValues[marginalStates.getFirst().varValues.length - 1];

        String id = computeIdentifier(marginalStates.get(module), timer, state_index, action, module).intern();
        identifiers.put(new KeyTriple(state_index, action, module), id);
        return id;
    }

    @Override
    public String getIdentifier(int state_index, int action, int module) {
        return identifiers.get(new KeyTriple(state_index, action, module));
    }

    private String computeIdentifier(State marginalState, Object timer, int state_index, int action, int module) {
        return marginalState.toString() + "-" + timer + "-" + action + "-" + module;
    }

    private record KeyTriple(int x, int y, int z) {
    }
}
