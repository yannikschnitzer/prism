//==============================================================================
//	
//	Copyright (c) 2018
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

package parser.ast;

import parser.EvaluateContext;
import parser.EvaluateContext.EvalMode;
import parser.EvaluateContextState;
import parser.State;
import parser.VarList;
import parser.VarUtils;
import parser.type.Type;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

/**
 * Class to store a single element of an Update, i.e. a single assignment (e.g. s'=1)
 */
public class UpdateElement extends ASTElement
{
	/** Reference to the variable to be updated
	 * (ExpressionVar for variable, ExpressionArray for array, etc.) */
	private Expression varRef;
	/** Name of the variable */
	private String varName;
	/** The expression for the assignment value, used to up */
	private Expression expr;

	/** Constructor */
	public UpdateElement(Expression varRef, Expression expr)
	{
		this.varRef = varRef;
		this.expr = expr;
	}

	/** Shallow copy constructor */
	public UpdateElement(UpdateElement other)
	{
		varRef = other.varRef;
		varName = other.varName;
		expr = other.expr;
	}

	// Setters
	
	/**
	 * Set the reference to the variable to be updated
	 * (ExpressionVar for variable, ExpressionArray for array, etc.)
	 * */
	public void setVarRef(Expression varRef)
	{
		this.varRef = varRef;
	}

	/**
	 * Set the expression that will be assigned to the variable.
	 */
	public void setExpression(Expression expr)
	{
		this.expr = expr;
	}

	// Getters

	/**
	 * Get the reference to the variable to be updated
	 * (ExpressionVar for variable, ExpressionArray for array, etc.)
	 */
	public Expression getVarRef()
	{
		return varRef;
	}

	/**
	 * Get the expression that will be assigned to the variable.
	 */
	public Expression getExpression()
	{
		return expr;
	}

	/**
	 * Get the name of the variable that is the assignment target.
	 * This is the "parent" variable, e.g. "a" for a[i][j].
	 */
	public String getVarName()
	{
		// Do this on demand, because need some info about variables
		// that is not available when this is first constructed by the parser 
		if (varName == null) {
			varName = VarUtils.getVarNameFromVarRef(varRef);
		}
		return varName;
	}

	/**
	 * Get the type of the update element (i.e. the type of the variable being updated). 
	 */
	public Type getType()
	{
		return varRef.getType();
	}

	/**
	 * Execute this update element, applying variable changes to a State object.  
	 * Current variable/constant values are specified as an EvaluateContext object.
	 * The evaluation mode and values for any undefined constants are also taken from this object.
	 * @param ec Context for evaluation of variables values etc.
	 * @param newState State object to apply changes to
	 * @param varList VarList for info about state variables
	 */
	public void update(EvaluateContext ec, State newState, VarList varList) throws PrismLangException
	{
		try {
			Object newValue = expr.evaluate(ec);
			VarUtils.assignVarValueInState(varRef, ec, newState, newValue, expr.getType(), varList);
		} catch (PrismLangException e) {
			e.setASTElement(expr);
			throw e;
		}
	}
	
	/**
	 * Check whether this update (from a particular state) would cause any errors, mainly variable overflows.
	 * Variable ranges are specified in the passed in VarList.
	 * Throws an exception if such an error occurs.
	 */
	public void checkUpdate(State oldState, VarList varList) throws PrismLangException
	{
		// TODO: primitive vs top-level
		/*int newValueInt = varList.encodeVarValueToInt(index, newValue);
		if (newValueInt < varList.getPrimitiveLow(index) || newValueInt > varList.getPrimitiveHigh(index)) {
			String errMsg = "Overflow when assigning value " + newValue + " to variable " + VarUtils.evaluateVarRef(getVarRef(i), oldState, false);
			throw new PrismLangException(errMsg, getElement(i));
		}*/
	}

	/**
	 * Convert this update element to a string in the context of a state.
	 * i.e. with assignment values and array indices/etc. evaluated.
	 * e.g. (a[i+1][0]'=i), in state i=1, returns "a[2][0]'=1".
	 * Note that this is *not* parenthesised, as in the PRISM language, or as from toString(). 
	 */
	public String toString(State state, boolean exact) throws PrismLangException
	{
		EvaluateContext ec = new EvaluateContextState(state);
		ec.setEvaluationMode(exact ? EvalMode.EXACT : EvalMode.FP);
		return VarUtils.varRefToString(getVarRef(), ec) + "'=" + getExpression().evaluate(ec);
	}
	
	// Methods required for ASTElement:

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	@Override
	public UpdateElement deepCopy(DeepCopy copier) throws PrismLangException
	{
		varRef = copier.copy(varRef);
		expr = copier.copy(expr);

		return this;
	}

	@Override
	public UpdateElement clone()
	{
		return (UpdateElement) super.clone();
	}
	
	// Other methods:
	
	@Override
	public String toString()
	{
		return "(" + getVarRef() + "'=" + getExpression() + ")";
	}
}
