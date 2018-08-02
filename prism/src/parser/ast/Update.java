//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham/Oxford)
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

package parser.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

import parser.EvaluateContext;
import parser.EvaluateContext.EvalMode;
import parser.EvaluateContextState;
import parser.EvaluateContextSubstate;
import parser.State;
import parser.VarList;
import parser.type.Type;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

/**
 * Class to store a single update, i.e. a list of assignments of variables to expressions, e.g. (s'=1)&amp;(x'=x+1)
 */
public class Update extends ASTElement implements Iterable<UpdateElement>
{
	// Individual elements of update
	private ArrayList<UpdateElement> elements;

	// Parent Updates object
	private Updates parent;

	/**
	 * Create an empty update.
	 */
	public Update()
	{
		elements = new ArrayList<>();
	}

	// Set methods

	/**
	 * Add a variable assignment ({@code v}'={@code e}) to this update.
	 * @param v The AST element corresponding to the variable being updated
	 * @param e The expression which will be assigned to the variable
	 */
	public void addElement(Expression v, Expression e)
	{
		elements.add(new UpdateElement(v, e));
	}

	/**
	 * Add a variable assignment (encoded as an UpdateElement) to this update.
	 */
	public void addElement(UpdateElement e)
	{
		elements.add(e);
	}

	/**
	 * Set the ith variable assignment (encoded as an UpdateElement) to this update.
	 */
	public void setElement(int i, UpdateElement e)
	{
		elements.set(i, e);
	}

	/**
	 * Set the variable {@code v} for the {@code i}th variable assignment of this update.
	 * @param i The index of the variable assignment within the update
	 * @param v The AST element corresponding to the variable being updated
	 */
	public void setVarRef(int i, Expression v)
	{
		elements.get(i).setVarRef(v);
	}

	/**
	 * Set the expression {@code e} for the {@code i}th variable assignment of this update.
	 * @param i The index of the variable assignment within the update
	 * @param e The expression which will be assigned to the variable
	 */
	public void setExpression(int i, Expression e)
	{
		elements.get(i).setExpression(e);
	}

	/**
	 * Set the {@link parser.ast.Updates} object containing this update.
	 */
	public void setParent(Updates u)
	{
		parent = u;
	}

	// Get methods

	/**
	 * Get the number of variables assigned values by this update.
	 */
	public int getNumElements()
	{
		return elements.size();
	}

	/** Get the update element (individual assignment) with the given index. */
	public UpdateElement getElement(int index)
	{
		return elements.get(index);
	}
	
	/**
	 * Get the {@code i}th variable reference in this update.
	 * (ExpressionVar for variable, ExpressionArray for array, etc.)
	 */
	public Expression getVarRef(int i)
	{
		return elements.get(i).getVarRef();
	}

	/**
	 * Get the expression used to update the {@code i}th variable in this update.
	 */
	public Expression getExpression(int i)
	{
		return elements.get(i).getExpression();
	}

	/**
	 * Get the type of the {@code i}th variable in this update.
	 */
	public Type getType(int i)
	{
		return elements.get(i).getType();
	}

	/**
	 * Get the {@link parser.ast.Updates} object containing this update.
	 */
	public Updates getParent()
	{
		return parent;
	}

	/**
	 * Execute this update, based on variable values specified as a State object.
	 * Apply changes in variables to a provided copy of the State object.
	 * (i.e. oldState and newState should be equal when passed in)
	 * It is assumed that any constants have already been defined.
	 * @param oldState Variable values in current state
	 * @param newState State object to apply changes to
	 * @param exact evaluate arithmetic expressions exactly?
	 * @param varList VarList for info about state variables
	 */
	public void update(State oldState, State newState, boolean exact, VarList varList) throws PrismLangException
	{
		EvaluateContext ec = new EvaluateContextState(oldState);
		ec.setEvaluationMode(exact ? EvalMode.EXACT : EvalMode.FP);
		for (UpdateElement e : this) {
			e.update(ec, newState, varList);
		}
	}

	/**
	 * Execute this update, based on variable values specified as a State object.
	 * Apply changes in variables to a provided copy of the State object.
	 * (i.e. oldState and newState should be equal when passed in.) 
	 * Both State objects represent only a subset of the total set of variables,
	 * with this subset being defined by the mapping varMap.
	 * Only variables in this subset are updated.
	 * But if doing so requires old values for variables outside the subset, this will cause an exception. 
	 * It is assumed that any constants have already been defined.
	 * @param oldState Variable values in current state
	 * @param newState State object to apply changes to
	 * @param varMap A mapping from indices (over all variables) to the subset (-1 if not in subset). 
	 */
	@Deprecated
	public void updatePartially(State oldState, State newState, int[] varMap) throws PrismLangException
	{
		// Deprecated because doesn't work for arbitrary variable references,
		// and is not used now anyway, since PTA construction was refactored
		// (and also no support for exact evaluation)
		int n = getNumElements();
		for (int i = 0; i < n; i++) {
			if (getVarRef(i) instanceof ExpressionVar) {
				int j = varMap[((ExpressionVar) getVarRef(i)).getVarIndex()];
				if (j != -1) {
					newState.setValue(j, getExpression(i).evaluate(new EvaluateContextSubstate(oldState, varMap)));
				}
			} else {
				throw new PrismLangException("Can only update partially for a top-level variable");
			}
		}
	}

	/**
	 * Check whether this update (from a particular state) would cause any errors, mainly variable overflows.
	 * Variable ranges are specified in the passed in VarList.
	 * Throws an exception if such an error occurs.
	 */
	public State checkUpdate(State oldState, VarList varList) throws PrismLangException
	{
		State res = new State(oldState);
		for (UpdateElement e : this) {
			e.checkUpdate(oldState, varList);
		}
		return res;
	}

	// Methods required for ASTElement:

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	@Override
	public Update deepCopy(DeepCopy copier) throws PrismLangException
	{
		copier.copyAll(elements);

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Update clone()
	{
		Update clone = (Update) super.clone();

		clone.elements = (ArrayList<UpdateElement>) elements.clone();

		return clone;
	}
	
	// Other methods:
	
	@Override
	public Iterator<UpdateElement> iterator()
	{
		return elements.iterator();
	}
	
	@Override
	public String toString()
	{
		// Normal case
		if (elements.size() > 0) {
			return elements.stream().map(UpdateElement::toString).collect(Collectors.joining(" & "));
		}
		// Special (empty) case
		else {
			return "true";
		}
	}
	
	/**
	 * String representation of the update, with assignment values and array indices/etc. evaluated.
	 * @param state The state in which to evaluate
	 * @param sep String used to separate elements, e.g. " & " or ", "
	 * @param emptyIsTrue If true, format an empty list as "true"; otherwise use ""
	 */
	public String toString(State state, String sep, boolean emptyIsTrue) throws PrismLangException
	{
		// Normal case
		if (elements.size() > 0) {
			boolean first = true;
			String s = "";
			for (UpdateElement ue : elements) {
				if (first)
					first = false;
				else
					s += sep;
				s += ue.toString(state, false);
			}
			return s;
		}
		// Special (empty) case
		else {
			return emptyIsTrue ? "true" : "";
		}
	}
}

//------------------------------------------------------------------------------
