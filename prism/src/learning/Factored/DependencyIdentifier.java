package learning.Factored;

import parser.State;

import java.util.List;

public interface DependencyIdentifier {

    /**
     * Compute and remember dependency identifier, this may depend on the actual state variables, but is cached on the index level.
     *
     * @param state         Actual state
     * @param marginalStates Substates containing only the variables of the respective modules
     * @param state_index   State index
     * @param action        Action index
     * @param module        Module / Factor index
     * @return Dependency identifier that was generated and cached for this state-action-module combination.
     */
    String getIdentifier(State state, List<State> marginalStates, int state_index, int action, int module);

    /**
     * Get the computed dependency identifier on the index level.
     *
     * @param state_index State Index
     * @param action      Action index
     * @param module      Module / Factor index
     * @return Depdency identifier that was cached for this state-action-module combination.
     */
    String getIdentifier(int state_index, int action, int module);
}
