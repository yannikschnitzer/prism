package explicit.cex.cex;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import explicit.Model;
import explicit.cex.util.Transition;
import explicit.exporters.StateExporter;

public class NondetPath extends ProbabilisticPath
{

	/** The choice associated with each transition */
	protected ArrayDeque<Object> choices = new ArrayDeque<>();
	
	@Override
	public void addTransitionAtFront(Transition t) {
		addTransitionAtFront(t, null);
	}
	
	@Override
	public void addTransitionAtBack(Transition t) {
		addTransitionAtBack(t, null);
	}
	
	public void addTransitionAtFront(Transition t, Object action)
	{
		super.addTransitionAtFront(t);
		choices.addFirst(action);
	}

	public void addTransitionAtBack(Transition t, Object action)
	{
		super.addTransitionAtBack(t);
		choices.addLast(t);
	}

	@Override
	public void removeFirst()
	{
		super.removeFirst();
		choices.removeFirst();
	}

	@Override
	public void removeLast()
	{
		super.removeLast();
		choices.removeLast();
	}
	
	public Iterator<Object> getChoiceIterator()
	{
		assert(choices.size() == transitions.size());
		return choices.iterator();
	}
	
	/**
	 * Returns the set of all choices that occur in the path
	 * @return Set of all choices that occur in the path
	 */
	public Set<Object> getChoices() {
		Set<Object> choices = new TreeSet<Object>();
		for (Iterator<Object> it = getChoiceIterator(); it.hasNext(); ) {
			Object action = it.next();
			if (action != null) choices.add(action);
		}
		return choices;
	}
	
	public String toStringWithActions(String delimBefore, String delimAfter, String delimNull, StateExporter exp) {
		assert(choices.size() == transitions.size());
		
		StringBuilder result = new StringBuilder();
		if (!transitions.isEmpty()) {

			result.append(getFirst().getSource());
			Iterator<Transition> itT = getTransitionIterator();
			Iterator<Object> itC = getChoiceIterator();
			
			while (itT.hasNext()) {
				Object action = itC.next();
				String stateStr = exp.stateToString(itT.next().getTarget());
				if (action != null) {
					result.append(delimBefore + action + delimAfter + stateStr);
				} else {
					result.append(delimNull + stateStr);
				}
			}
		} else {
			result.append("empty path");
		}

		return result.toString();
	}
	
	@Override
	public String toString() {
		return toStringWithActions("-[", "]>", "->", new StateExporter());
	}
	
	@Override
	public int getChoice(int step)
	{
		// TODO: Figure out choice index
		return 0;
	}
	
	@Override
	public boolean hasChoiceInfo()
	{
		return true;
	}
	
	@Override
	public ProbabilisticPath restrictTo(Model underlyingModel)
	{
		NondetPath result = new NondetPath();
		result.probabilityOfInitialState = probabilityOfInitialState;
		result.states = states;
		
		Iterator<Transition> itT = getTransitionIterator();
		Iterator<Object> itC = getChoiceIterator();
		
		while (itT.hasNext()) {
			Transition t = itT.next();
			Object action = itC.next();
			if (t.getSource() < underlyingModel.getNumStates() && t.getTarget() < underlyingModel.getNumStates()) {
				result.addTransitionAtBack(t, action);
			}
		}
		
		return result;
	}
	
}
