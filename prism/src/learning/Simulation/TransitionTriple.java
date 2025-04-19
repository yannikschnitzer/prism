package learning.Simulation;

import java.util.Objects;

/**
 * Small class for transition triples (s,a,s')
 */
public class TransitionTriple {
    private int s;
    private String action;
    private int successor;

    public TransitionTriple(int s, String action, int successor) {
        this.s = s;
        this.action = action;
        this.successor = successor;
    }

    public TransitionTriple setAll(int s, String action, int successor) {
        this.s = s;
        this.action = action;
        this.successor = successor;
        return this;
    }

    public int getState() {
        return this.s;
    }

    public String getAction() {
        return this.action;
    }

    public StateActionPair getStateAction() {
        return new StateActionPair(this.s, this.action);
    }

    public int getSuccessor() {
        return this.successor;
    }

    @Override
    public int hashCode() {
        int h = s;
        h = 31*h + action.hashCode();
        h = 31*h + successor;
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (this.getClass() != o.getClass())
            return false;

        TransitionTriple other = (TransitionTriple) o;
        return (this.s == other.getState() && this.action.equals(other.getAction()) && this.successor == other.getSuccessor());
    }

    @Override
    public String toString() {
        return "(" + this.s + ", " + this.action + ", " + this.successor + ")";
    }
}