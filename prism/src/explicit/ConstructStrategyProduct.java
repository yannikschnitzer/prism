//==============================================================================
//	
//	Copyright (c) 2022-
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

package explicit;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import parser.State;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import prism.ModelType;
import prism.PrismException;
import prism.PrismNotSupportedException;
import strat.Strategy;
import strat.StrategyInfo;

public class ConstructStrategyProduct
{
	/**
	 * Construct the model induced by a finite-memory strategy on a nondeterministic model
	 */
	public Model constructProductModel(NondetModel model, Strategy strat) throws PrismException
	{
		ModelType modelType = model.getModelType();
		int modelNumStates = model.getNumStates();
		int memSize = strat.getMemorySize();
		int prodNumStates;
		int s_1, s_2, q_1, q_2;
		List<State> prodStatesList = null, memStatesList = null;
		double stratChoiceProb = 1.0;

		// This is for finite-memory strategies
		if (!strat.hasMemory()) {
			throw new PrismException("Product construction is for finite-memory models");
		}
		
		// Check size limits for this product construction approach 
		try {
			prodNumStates = Math.multiplyExact(modelNumStates, memSize);
		} catch (ArithmeticException e) {
			throw new PrismException("Size of product state space of model and strategy is too large for explicit engine");
		}

		// If the model has a VarList, we will create a new one 
		VarList newVarList = null;
		if (model.getVarList() != null) {
			VarList varList = model.getVarList();
			// Create a (new, unique) name for the variable that will represent memory states
			String memVar = "_mem";
			while (varList.getIndex(memVar) != -1) {
				memVar = "_" + memVar;
			}
			newVarList = (VarList) varList.clone();
			Declaration decl = new Declaration(memVar, new DeclarationInt(Expression.Int(0), Expression.Int(memSize)));
			newVarList.addVar(0, decl, 1, model.getConstantValues());
		}

		// Determine type of induced model
		// (everything reduces to a DTMC for now)
		ModelType productModelType = null;
		switch (modelType) {
		case MDP:
		case STPG:
			productModelType = ModelType.DTMC;
			break;
		default:
			throw new PrismNotSupportedException("Product construction not supported for " + modelType + "s");
		}
		
		// Create a (simple, mutable) model of the appropriate type
		ModelSimple prodModel = null;
		switch (productModelType) {
		case DTMC: {
			DTMCSimple dtmcProd = new DTMCSimple();
			dtmcProd.setVarList(newVarList);
			prodModel = dtmcProd;
			break;
		}
		case MDP: {
			MDPSimple mdpProd = new MDPSimple();
			mdpProd.setVarList(newVarList);
			prodModel = mdpProd;
			break;
		}
		case STPG: {
			STPGExplicit stpgProd = new STPGExplicit();
			stpgProd.setVarList(newVarList);
			prodModel = stpgProd;
			break;
		}
		default:
			throw new PrismNotSupportedException("Product construction not supported for " + modelType + "s");
		}

		// Encoding: 
		// each state s' = <s, q> = s * memSize + q
		// s(s') = s' / memSize
		// q(s') = s' % memSize

		// Initialise state info storage
		LinkedList<Point> queue = new LinkedList<Point>();
		int map[] = new int[prodNumStates];
		Arrays.fill(map, -1);
		if (model.getStatesList() != null) {
			prodStatesList = new ArrayList<State>();
			memStatesList = new ArrayList<State>(memSize);
			for (int i = 0; i < memSize; i++) {
				memStatesList.add(new State(1).setValue(0, i));
			}
		}

		// Get initial states
		for (int s_0 : model.getInitialStates()) {
			// Find corresponding initial memory
			int q_0 = strat.getInitialMemory(s_0);
			if (q_0 < 0) {
				throw new PrismException("The memory status is unknown (state " + s_0 + ")");
			}
			// Add (initial) state to product
			queue.add(new Point(s_0, q_0));
			switch (productModelType) {
			case STPG:
				((STPGExplicit) prodModel).addState(((STPG) model).getPlayer(s_0));
				break;
			default:
				prodModel.addState();
			break;
			}
			prodModel.addInitialState(prodModel.getNumStates() - 1);
			map[s_0 * memSize + q_0] = prodModel.getNumStates() - 1;
			if (prodStatesList != null) {
				// Store state information for the product
				prodStatesList.add(new State(model.getStatesList().get(s_0), memStatesList.get(q_0)));
			}
		}

		// Product states
		BitSet visited = new BitSet(prodNumStates);
		while (!queue.isEmpty()) {
			Point p = queue.pop();
			s_1 = p.x;
			q_1 = p.y;
			visited.set(s_1 * memSize + q_1);
			int numChoices =  model.getNumChoices(s_1);
			// Extract strategy decision
			Object decision = strat.getChoiceAction(s_1, q_1);
			// If it is undefined, just pick the first one
			if (decision == StrategyInfo.UNDEFINED && numChoices > 0) {
				decision = model.getAction(s_1, 0);
			}
			// Go through transitions from state s_1 in original model
			for (int j = 0; j < numChoices; j++) {
				Object act = model.getAction(s_1, j);
				// Skip choices not picked by the strategy
				if (!strat.isActionChosen(decision, act)) {
					continue;
				}
				if (strat.isRandomised()) {
					stratChoiceProb = strat.getChoiceActionProbability(decision, act);
				}
				Iterator<Map.Entry<Integer, Double>> iter;
				switch (modelType) {
				case DTMC:
					iter = ((DTMC) model).getTransitionsIterator(s_1);
					break;
				case MDP:
					iter = ((MDP) model).getTransitionsIterator(s_1, j);
					break;
				case STPG:
					iter = ((STPG) model).getTransitionsIterator(s_1, j);
					break;
				default:
					throw new PrismNotSupportedException("Product construction not implemented for " + modelType + "s");
				}
				Distribution prodDistr = null;
				if (productModelType.nondeterministic()) {
					prodDistr = new Distribution();
				}
				while (iter.hasNext()) {
					Map.Entry<Integer, Double> e = iter.next();
					s_2 = e.getKey();
					double prob = e.getValue();
					if (strat.isRandomised()) {
						prob *= stratChoiceProb;
					}
					// Find corresponding memory update
					q_2 = strat.getUpdatedMemory(q_1, model.getAction(s_1, j), s_2);
					if (q_2 < 0) {
						throw new PrismException("The memory status is unknown (state " + s_2 + ")");
					}
					// Add state/transition to model
					if (!visited.get(s_2 * memSize + q_2) && map[s_2 * memSize + q_2] == -1) {
						queue.add(new Point(s_2, q_2));
						switch (productModelType) {
						case STPG:
							((STPGExplicit) prodModel).addState(((STPG) model).getPlayer(s_2));
							break;
						default:
							prodModel.addState();
							break;
						}
						map[s_2 * memSize + q_2] = prodModel.getNumStates() - 1;
						if (prodStatesList != null) {
							// Store state information for the product
							prodStatesList.add(new State(model.getStatesList().get(s_2), memStatesList.get(q_2)));
						}
					}
					switch (productModelType) {
					case DTMC:
						((DTMCSimple) prodModel).setProbability(map[s_1 * memSize + q_1], map[s_2 * memSize + q_2], prob);
						break;
					case MDP:
					case STPG:
						prodDistr.set(map[s_2 * memSize + q_2], prob);
						break;
					default:
						throw new PrismNotSupportedException("Product construction not implemented for " + modelType + "s");
					}
				}
				switch (productModelType) {
				case MDP:
					((MDPSimple) prodModel).addActionLabelledChoice(map[s_1 * memSize + q_1], prodDistr, ((MDP) model).getAction(s_1, j));
					break;
				case STPG:
					((STPGExplicit) prodModel).addActionLabelledChoice(map[s_1 * memSize + q_1], prodDistr, ((STPG) model).getAction(s_1, j));
					break;
				default:
					break;
				}
			}
		}
		
		prodModel.findDeadlocks(false);

		if (prodStatesList != null) {
			prodModel.setStatesList(prodStatesList);
		}
		
		return prodModel;
	}
}
