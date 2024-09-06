//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham)
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

package prism;

import java.util.Collections;
import java.util.List;

import explicit.rewards.Rewards;
import parser.State;
import parser.ast.RewardStruct;

/**
 * Interface for classes that provide details of rewards for a model.
 * This is a generic class where rewards are of type Value.
 * <br><br>
 * Firstly, this includes basic syntactic info: the number of reward structs and their names.
 * It is only necessary to implement {@link #getRewardStructNames()};
 * the others have default implementations that use this method as a basis.
 * <br><br>
 * Secondly it provides access to the rewards themselves.
 * Implementations of {@link RewardGenerator} can allow rewards to be queried in one or more ways:
 * by State object; by (integer) state index; syntactically by providing a RewardStruct;
 * or by directly providing a (explicit engine) Rewards object.
 * The method {@link RewardGenerator#isRewardLookupSupported(RewardLookup)} should return true or false accordingly.
 * <br><br>
 * Default implementations of all methods are provided which assume that rewards are
 * looked up by State object and there are no rewards defined (zero reward structs).
 */
public interface RewardGenerator<Value>
{
	/**
	 * Mechanisms for querying rewards
	 */
	public enum RewardLookup { BY_STATE, BY_STATE_INDEX, BY_REWARD_STRUCT, BY_REWARD_OBJECT };
	
	/**
	 * Get an Evaluator for the values generated by this RewardGenerator for rewards.
	 * This is needed, for example, to sum rewards or check if they are negative/NaN.
	 * A default implementation provides an evaluator for the (usual) case when Value is Double.
	 * The method name is distinct from {@link ModelGenerator#getEvaluator()} so that
	 * classes implementing both interfaces do not need to disambiguate.
	 */
	@SuppressWarnings("unchecked")
	public default Evaluator<Value> getRewardEvaluator()
	{
		return (Evaluator<Value>) Evaluator.forDouble();
	}
	
	/**
	 * Get a list of the names of the reward structures.
	 * Unnamed reward structures are allowed and should use name "".
	 * Reward names should be distinct (except if equal to "").
	 */
	public default List<String> getRewardStructNames()
	{
		// No reward structures by default
		return Collections.emptyList();
	}
	
	/**
	 * Get the number of reward structures.
	 */
	public default int getNumRewardStructs()
	{
		// Default implementation just extracts from getRewardStructNames() 
		return getRewardStructNames().size();
	}
	
	/**
	 * Look up the index of a reward structure by name.
	 * (indexed from 0, not from 1 like at the user (property language) level).
	 * Returns -1 if there is no such reward structure.
	 */
	public default int getRewardStructIndex(String name)
	{
		// Default implementation just extracts from getRewardStructNames() 
		return getRewardStructNames().indexOf(name);
	}

	/**
	 * Get the name of the {@code i}th reward structure.
	 * {@code i} should always be between 0 and getNumVars() - 1. 
	 */
	public default String getRewardStructName(int i)
	{
		// Default implementation just extracts from getRewardStructNames() 
		return getRewardStructNames().get(i);
	}
	
	/**
	 * Returns true if the {@code r}th reward structure defines state rewards.
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * If this returns false, the model checker is allowed to ignore them (which may be more efficient).
	 * If using an algorithm or implementation that does not support state rewards,
	 * you may need to return false here (as well as not defining state rewards).
	 */
	public default boolean rewardStructHasStateRewards(int r)
	{
		// By default, assume that any reward structures that do exist may have state rewards
		return true;
	}
	
	/**
	 * Returns true if the {@code r}th reward structure defines transition rewards.
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * If this returns false, the model checker is allowed to ignore them (which may be more efficient).
	 * If using an algorithm or implementation that does not support transition rewards,
	 * you may need to return false here (as well as not defining transition rewards).
	 */
	public default boolean rewardStructHasTransitionRewards(int r)
	{
		// By default, assume that any reward structures that do exist may have transition rewards
		return true;
	}
	
	/**
	 * Check which mechanisms for looking up rewards are supported.
	 */
	public default boolean isRewardLookupSupported(RewardLookup lookup)
	{
		// By default, rewards are queried via State objects (only)
		return lookup == RewardLookup.BY_STATE;
	}
	
	/**
	 * Get the state reward of the {@code r}th reward structure for state {@code state}
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * Only available if {@link #isRewardLookupSupported(RewardLookup)} returns true for {@code RewardLookup.BY_STATE)}.
	 * If a reward structure has no state rewards, you can indicate this by implementing
	 * the method {@link #rewardStructHasStateRewards(int)}, which may improve efficiency
	 * and/or allow use of algorithms/implementations that do not support state rewards rewards.
	 * @param r The index of the reward structure to use
	 * @param state The state in which to evaluate the rewards
	 */
	public default Value getStateReward(int r, State state) throws PrismException
	{
		// Default implementation: error if not supported, or bad index
		if (!isRewardLookupSupported(RewardLookup.BY_STATE)) {
			throw new PrismException("Reward lookup by State not supported");
		}
		if (r < 0 || r >= getNumRewardStructs()) {
			throw new PrismException("Invalid reward index " + r);
		}
		// Otherwise, if reward is not defined, that's an error
		throw new PrismException("Reward has not been defined");
	}
	
	/**
	 * Get the state-action reward of the {@code r}th reward structure for state {@code state} and action {@code action}
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * Only available if {@link #isRewardLookupSupported(RewardLookup)} returns true for {@code RewardLookup.BY_STATE)}.
	 * If a reward structure has no transition rewards, you can indicate this by implementing
	 * the method {@link #rewardStructHasTransitionRewards(int)}, which may improve efficiency
	 * and/or allow use of algorithms/implementations that do not support transition rewards rewards.
	 * @param r The index of the reward structure to use
	 * @param state The state in which to evaluate the rewards
	 * @param action The outgoing action label
	 */
	public default Value getStateActionReward(int r, State state, Object action) throws PrismException
	{
		// Default implementation: error if not supported, or bad index
		if (!isRewardLookupSupported(RewardLookup.BY_STATE)) {
			throw new PrismException("Reward lookup by State not supported");
		}
		if (r < 0 || r >= getNumRewardStructs()) {
			throw new PrismException("Invalid reward index " + r);
		}
		// Otherwise, if reward is not defined, that's an error
		throw new PrismException("Reward has not been defined");
	}
	
	/**
	 * Get the state reward of the {@code r}th reward structure for state {@code s}
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * Only available if {@link #isRewardLookupSupported(RewardLookup)} returns true for {@code RewardLookup.BY_STATE_INDEX)}.
	 * If a reward structure has no state rewards, you can indicate this by implementing
	 * the method {@link #rewardStructHasStateRewards(int)}, which may improve efficiency
	 * and/or allow use of algorithms/implementations that do not support state rewards.
	 * @param r The index of the reward structure to use
	 * @param s The index of the state in which to evaluate the rewards
	 */
	public default Value getStateReward(int r, int s) throws PrismException
	{
		// Default implementation: error if not supported, or bad index
		if (!isRewardLookupSupported(RewardLookup.BY_STATE_INDEX)) {
			throw new PrismException("Reward lookup by state index not supported");
		}
		if (r < 0 || r >= getNumRewardStructs()) {
			throw new PrismException("Invalid reward index " + r);
		}
		// Otherwise, if reward is not defined, that's an error
		throw new PrismException("Reward has not been defined");
	}

	/**
	 * Get the state-action reward of the {@code r}th reward structure for state {@code s} and action {@code action}
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * Only available if {@link #isRewardLookupSupported(RewardLookup)} returns true for {@code RewardLookup.BY_STATE_INDEX)}.
	 * If a reward structure has no transition rewards, you can indicate this by implementing
	 * the method {@link #rewardStructHasTransitionRewards(int)}, which may improve efficiency
	 * and/or allow use of algorithms/implementations that do not support transition rewards.
	 * @param r The index of the reward structure to use
	 * @param s The index of the state in which to evaluate the rewards
	 * @param action The outgoing action label
	 */
	public default Value getStateActionReward(int r, int s, Object action) throws PrismException
	{
		// Default implementation: error if not supported, or bad index
		if (!isRewardLookupSupported(RewardLookup.BY_STATE_INDEX)) {
			throw new PrismException("Reward lookup by state index not supported");
		}
		if (r < 0 || r >= getNumRewardStructs()) {
			throw new PrismException("Invalid reward index " + r);
		}
		// Otherwise, if reward is not defined, that's an error
		throw new PrismException("Reward has not been defined");
	}
	
	/**
	 * Get a {@link RewardStruct} object representing the {@code r}th reward structure
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * Only available if {@link #isRewardLookupSupported(RewardLookup)} returns true for {@code RewardLookup.BY_REWARD_STRUCT)}.
	 * Throws an exception if {@code r} is out of range or the information is not available.
	 * If a reward structure has no transition rewards, you can indicate this by implementing
	 * the method {@link #rewardStructHasTransitionRewards(int)}, which may improve efficiency
	 * and/or allow use of algorithms/implementations that do not support transition rewards.
	 */
	public default RewardStruct getRewardStruct(int r) throws PrismException
	{
		// Default implementation: error if not supported, or bad index
		if (!isRewardLookupSupported(RewardLookup.BY_REWARD_STRUCT)) {
			throw new PrismException("Reward lookup by reward struct not supported");
		}
		if (r < 0 || r >= getNumRewardStructs()) {
			throw new PrismException("Invalid reward index " + r);
		}
		// Otherwise, if reward is not defined, that's an error
		throw new PrismException("Reward has not been defined");
	}

	/**
	 * Get a {@link Rewards} object representing the {@code r}th reward structure
	 * ({@code r} is indexed from 0, not from 1 like at the user (property language) level).
	 * Only available if {@link #isRewardLookupSupported(RewardLookup)} returns true for {@code RewardLookup.BY_REWARD_OBJECT)}.
	 * Throws an exception if {@code r} is out of range or the information is not available.
	 * If a reward structure has no transition rewards, you can indicate this by implementing
	 * the method {@link #rewardStructHasTransitionRewards(int)}, which may improve efficiency
	 * and/or allow use of algorithms/implementations that do not support transition rewards.
	 */
	public default Rewards<Value> getRewardObject(int r) throws PrismException
	{
		// Default implementation: error if not supported, or bad index
		if (!isRewardLookupSupported(RewardLookup.BY_REWARD_OBJECT)) {
			throw new PrismException("Reward lookup by reward object not supported");
		}
		if (r < 0 || r >= getNumRewardStructs()) {
			throw new PrismException("Invalid reward index " + r);
		}
		// Otherwise, if reward is not defined, that's an error
		throw new PrismException("Reward has not been defined");
	}

	/**
	 * Get the {@link Model} corresponding to the {@link Rewards} objects when rewards are provided in this way.
	 * Only available if {@link #isRewardLookupSupported(RewardLookup)} returns true for {@code RewardLookup.BY_REWARD_OBJECT)}.
	 * Throws an exception if the information is not available.
	 * If a reward structure has no transition rewards, you can indicate this by implementing
	 * the method {@link #rewardStructHasTransitionRewards(int)}, which may improve efficiency
	 * and/or allow use of algorithms/implementations that do not support transition rewards.
	 */
	public default explicit.Model<Value> getRewardObjectModel() throws PrismException
	{
		// Default implementation: error if not supported
		if (!isRewardLookupSupported(RewardLookup.BY_REWARD_OBJECT)) {
			throw new PrismException("Reward lookup by reward object not supported");
		}
		// Otherwise, if model is not defined, that's an error
		throw new PrismException("Model for rewards has not been defined");
	}
}
