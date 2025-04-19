package learning;

import explicit.MDP;
import learning.Simulation.TransitionTriple;
import param.Function;
import prism.Pair;

import java.util.*;

public class ParameterTyer {
    public static List<List<TransitionTriple>> getSimilarTransitions(MDP<Function> mdpParam) {
        Map<Set<Function>, List<Pair<Integer, Integer>>> similarStateMap = getSimilarStateActionMap(mdpParam);
        List<List<TransitionTriple>> similarTransitions = new ArrayList<>();

        for (List<Pair<Integer, Integer>> similarStateActions : similarStateMap.values()) {
            Map<Function, List<TransitionTriple>> transitions = new HashMap<>();

            for (Pair<Integer, Integer> sa : similarStateActions) {
                int s = sa.first;
                int i = sa.second;

                String action = getActionString(mdpParam, s, i);

                mdpParam.forEachTransition(s, i, (int sFrom, int sTo, Function p) -> {
                    if (!transitions.containsKey(p)) {
                        transitions.put(p, new ArrayList<>());
                    }
                    transitions.get(p).add(new TransitionTriple(sFrom, action, sTo));
                });

            }

            similarTransitions.addAll(transitions.values());
        }
        //System.out.println("Similar Transitions: " + similarTransitions);
        return similarTransitions;
    }

    public static Map<Set<Function>, List<Pair<Integer, Integer>>> getSimilarStateActionMap(MDP<Function> mdpParam) {
        HashMap<Set<Function>, List<Pair<Integer, Integer>>> similarStateActionMap = new HashMap<>();

        for (int s = 0; s < mdpParam.getNumStates(); s++) {
            int numChoices = mdpParam.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                Set<Function> transitionStructure = getTransitionStructure(mdpParam, s, i);
                if (!similarStateActionMap.containsKey(transitionStructure)) {
                    similarStateActionMap.put(transitionStructure, new ArrayList<>());
                }
                similarStateActionMap.get(transitionStructure).add(new Pair<>(s, i));
            }
        }

        //System.out.println("Similar state action map" + similarStateActionMap);
        return similarStateActionMap;
    }

    public static String getActionString(MDP<Function> mdp, int s, int i) {
        String action = (String) mdp.getAction(s, i);
        if (action == null) {
            action = "_empty";
        }
        return action;
    }

    public static Set<Function> getTransitionStructure(MDP<Function> mdpParam, int s) {
        HashSet<Function> transitions = new HashSet<>();
        int numChoices = mdpParam.getNumChoices(s);
        for (int i = 0; i < numChoices; i++) {
            mdpParam.forEachTransition(s, i, (int sFrom, int sTo, Function p) -> {
                transitions.add(p);
            });
        }
        return transitions;
    }

    public static Set<Function> getTransitionStructure(MDP<Function> mdpParam, int s, int a) {
        HashSet<Function> transitions = new HashSet<>();
        mdpParam.forEachTransition(s, a, (int sFrom, int sTo, Function p) -> {
            transitions.add(p);
        });
        return transitions;
    }

    public static Map<Function, List<TransitionTriple>> getFunctionMap(MDP<Function> mdpParam) {
        Map<Function, List<TransitionTriple>> functionMap = new HashMap<>();

        for (int s = 0; s < mdpParam.getNumStates(); s++) {
            int numChoices = mdpParam.getNumChoices(s);
            for (int i = 0; i < numChoices; i++) {
                //String action = (String) mdpParam.getAction(s, i);
                String action = getActionString(mdpParam, s, i);
                mdpParam.forEachTransition(s, i, (int sFrom, int sTo, Function p) -> {
                    if (functionMap.containsKey(p)) {
                        functionMap.get(p).add(new TransitionTriple(sFrom, action, sTo));
                    } else {
                        functionMap.put(p, new ArrayList<>());
                        functionMap.get(p).add(new TransitionTriple(sFrom, action, sTo));
                    }
                });
            }
        }

        return functionMap;
    }
}
