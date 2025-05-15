package learning.Factored;

import parser.State;

import java.util.HashMap;

/**
 * General Dependency Identifier for dependencies that only depend on the individual module / factor state.
 */
public class DependencyIdentifierGeneral implements DependencyIdentifier {
    private record KeyTriple(int x, int y, int z) {}
    private final HashMap<KeyTriple, String> identifiers;

    public DependencyIdentifierGeneral() {
        this.identifiers = new HashMap<>();
    }

    @Override
    public String getIdentifier(State state, State marginalState, int state_index, int action, int module) {
        String id = computeIdentifier(marginalState, state_index, action, module).intern();
        identifiers.put(new KeyTriple(state_index, action, module), id);
        return id;
    }

    @Override
    public String getIdentifier(int state_index, int action, int module) {
        return identifiers.get(new KeyTriple(state_index, action, module));
    }

    private String computeIdentifier(State marginalState, int state_index, int action, int module) {
        return marginalState.toString() + "-" + action + "-" + module;
    }


}
