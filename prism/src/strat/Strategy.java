//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham/Oxford)
//	* Aistis Simaitis <aistis.aimaitis@cs.ox.ac.uk> (University of Oxford)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package strat;

import static prism.PrismSettings.DEFAULT_EXPORT_MODEL_PRECISION;

import prism.PrismException;
import prism.PrismLog;
import simulator.RandomNumberGenerator;

/**
 * Interface for classes to represent strategies,
 * as built in the context of a constructed model.
 * This means that the information is queried by state index.
 * This is for a general class of strategies that may use memory,
 * but this can simply be ignored (e.g., set to -1) if not applicable.
 */
public interface Strategy extends StrategyInfo
{
	/**
	 * Get the action chosen by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m.
	 * Returns {@link #StrategyInfo.UNDEFINED} if undefined.
	 * For a randomised strategy (and if defined), this method returns
	 * an instance of DistributionOver&lt;Object&gt; instead of Object.
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public Object getChoiceAction(int s, int m);
	
	/**
	 * Get the probability with which an action is chosen by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m.
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public default double getChoiceActionProbability(int s, int m, Object act)
	{
		return getChoiceActionProbability(getChoiceAction(s, m), act);
	}
	
	/**
	 * Is an action chosen by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m?
	 * For a randomised strategy: is the action chosen with positive probability?
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public default boolean isActionChosen(int s, int m, Object act)
	{
		return isActionChosen(getChoiceAction(s, m), act);
	}
	
	/**
	 * Sample an action chosen by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m.
	 * For a deterministic strategy, this returns the (unique) chosen action;
	 * for a randomised strategy, an action is sampled according to the strategy's distribution.
	 * Returns {@link #StrategyInfo.UNDEFINED} if undefined.
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public default Object sampleChoiceAction(int s, int m, RandomNumberGenerator rng)
	{
		return sampleChoiceAction(getChoiceAction(s, m), rng);
	}
	
	/**
	 * Get the index of the choice picked by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m
	 * (assuming it is deterministic).
	 * The index is defined with respect to a particular model, stored locally.
	 * Returns a negative value (not necessarily -1) if undefined.
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public int getChoiceIndex(int s, int m);

	/**
	 * Is a choice defined by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m.
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public default boolean isChoiceDefined(int s, int m)
	{
		return getChoiceAction(s, m) != UNDEFINED;
	}
	
	/**
	 * Optionally, return reason why a choice is undefined by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m.
	 * Returns null if there is no reason.
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public default UndefinedReason whyUndefined(int s, int m)
	{
		// No reason provided by default
		return null;
	}
	
	/**
	 * Get a string representing the choice made by the strategy in the state index s
	 * and where the current memory of the strategy (if applicable) is m.
	 * This may also indicate the reason why it is undefined, if it is.
	 * Pass an arbitrary value (e.g. -1) for m if memory is not relevant.
	 */
	public default String getChoiceActionString(int s, int m)
	{
		Object action = getChoiceAction(s, m);
		if (action != UNDEFINED) {
			return action.toString();
		} else {
			UndefinedReason why = whyUndefined(s, m);
			if (why == null) {
				return "?";
			}
			switch (why) {
			case UNKNOWN:
				return "?";
			case ARBITRARY:
				return "";
			case UNREACHABLE:
				return "-";
			default:
				return "?";
			}
		}
	}
	
	/**
	 * For a strategy with memory, get the size of the memory
	 * (memory values are then assumed to be 0...memSize-1.
	 * A memoryless strategy should return 0. 
	 */
    public default int getMemorySize()
    {
    	// No memory by default
    	return 0;
    }
    
	/**
	 * For a strategy with memory, get the initial value of the memory
	 * based on the initial state of the model.
	 * @param sInit Index of initial model state
	 */
    public default int getInitialMemory(int sInit)
    {
    	// No memory by default
    	return -1;
    }
    
	/**
	 * For a strategy with memory, get the updated value of the memory
	 * based on its current value and the new current state of the model
	 * (plus the action that was taken to get there).
	 * @param m Current strategy memory value
	 * @param action Last action taken in model
	 * @param sInit Index of new model state
	 */
    public default int getUpdatedMemory(int m, Object action, int sNext)
    {
    	// No memory by default
    	return -1;
    }
    
	/**
	 * Get the number of states of the model associated with this strategy.
	 */
	public int getNumStates();

	/**
	 * Export the strategy to a PrismLog, displaying strategy choices as action names.
	 */
	public void exportActions(PrismLog out) throws PrismException;
	
	/**
	 * Export the strategy to a PrismLog, displaying strategy choices as indices.
	 */
	public void exportIndices(PrismLog out) throws PrismException;

	/**
	 * Export the model induced by this strategy to a PrismLog.
	 */
	default void exportInducedModel(PrismLog out) throws PrismException
	{
		exportInducedModel(out, DEFAULT_EXPORT_MODEL_PRECISION);
	}

	/**
	 * Export the model induced by this strategy to a PrismLog.
	 * @param precision number of significant digits >= 1
	 */
	public void exportInducedModel(PrismLog out, int precision) throws PrismException;

	/**
	 * Export the strategy to a dot file (of the model showing the strategy).
	 */
	default void exportDotFile(PrismLog out) throws PrismException
	{
		exportDotFile(out, DEFAULT_EXPORT_MODEL_PRECISION);
	}

	/**
	 * Export the strategy to a dot file (of the model showing the strategy).
	 * @param precision number of significant digits >= 1
	 */
	public void exportDotFile(PrismLog out, int precision) throws PrismException;
	
	/**
	 * Clear storage of the strategy.
	 */
	public void clear();
}
