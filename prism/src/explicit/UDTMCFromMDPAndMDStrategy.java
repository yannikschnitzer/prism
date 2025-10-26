package explicit;

import parser.State;
import parser.Values;
import prism.PrismException;
import strat.MDStrategy;

import java.util.*;

public class UDTMCFromMDPAndMDStrategy<Value> extends ModelExplicit<Value> implements UDTMC<Value> {

    // Parent MDP
    protected UMDP<Value> umdp;
    // MD strategy
    protected MDStrategy strat;

    /**
     * Constructor: create from MDP and memoryless adversary.
     */
    public UDTMCFromMDPAndMDStrategy(UMDP<Value> umdp, MDStrategy strat)
    {
        this.umdp = umdp;
        this.numStates = umdp.getNumStates();
        this.strat = strat;
    }

    // Accessors (for Model)

    public int getNumStates()
    {
        return umdp.getNumStates();
    }

    public int getNumInitialStates()
    {
        return umdp.getNumInitialStates();
    }

    public Iterable<Integer> getInitialStates()
    {
        return umdp.getInitialStates();
    }

    public int getFirstInitialState()
    {
        return umdp.getFirstInitialState();
    }

    public boolean isInitialState(int i)
    {
        return umdp.isInitialState(i);
    }

    public boolean isDeadlockState(int i)
    {
        return umdp.isDeadlockState(i);
    }

    public List<State> getStatesList()
    {
        return umdp.getStatesList();
    }

    public Values getConstantValues()
    {
        return umdp.getConstantValues();
    }

    public Set<String> getLabels() { return umdp.getLabels(); }

    @Override
    public BitSet getLabelStates(String name) {
        return umdp.getLabelStates(name);
    }

    @Override
    public Map<String, BitSet> getLabelToStatesMap() {
        return umdp.getLabelToStatesMap();
    }

    public int getNumTransitions(int s)
    {
        return strat.isChoiceDefined(s) ? umdp.getNumTransitions(s, strat.getChoiceIndex(s)) : 0;
    }

    @Override
    public SuccessorsIterator getSuccessors(final int s)
    {
        if (strat.isChoiceDefined(s)) {
            return umdp.getSuccessors(s, strat.getChoiceIndex(s));
        } else {
            return SuccessorsIterator.empty();
        }
    }

    public int getNumChoices(int s)
    {
        // Always 1 for a DTMC
        return 1;
    }

    public void findDeadlocks(boolean fix) throws PrismException
    {
        // No deadlocks by definition
    }

    public void checkForDeadlocks() throws PrismException
    {
        // No deadlocks by definition
    }

    public void checkForDeadlocks(BitSet except) throws PrismException
    {
        // No deadlocks by definition
    }

    // Accessors (for DTMC)
    public Iterator<Object> getActionsIterator(int s)
    {
        if (strat.isChoiceDefined(s)) {
            return Collections.nCopies(getNumTransitions(s), umdp.getAction(s, strat.getChoiceIndex(s))).iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    @Override
    public double mvMultUncSingle(int s, double vect[], MinMax minMax)
    {
        return strat.isChoiceDefined(s) ? umdp.mvMultUncSingle(s, strat.getChoiceIndex(s), vect, minMax) : 0;
    }

    @Override
    public void exportToPrismLanguage(String filename, int precision) throws PrismException {

    }

    @Override
    public List<Object> findActionsUsed() {
        return List.of();
    }

    @Override
    public String toString()
    {
        String s = "";
        s = "[ ";

        for (int i = 0; i < getNumStates(); i++) {
            if (i > 0) {
                s += ", ";
            }
            s += i + ": ";
            s += "[";
            if (strat.isChoiceDefined(i)) {
                s += umdp.getUncertainDistribution(i, strat.getChoiceIndex(i)).toString();
            }
            s += "]";
        }
        s += " ]\n";
        return s;
    }
}
