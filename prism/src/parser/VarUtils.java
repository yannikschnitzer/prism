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

package parser;

import java.util.ArrayList;
import java.util.List;

import parser.ast.Expression;
import parser.ast.ExpressionArrayAccess;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionVar;
import parser.type.Type;
import parser.visitor.ASTTraverseModify;
import prism.PrismLangException;

/**
 * Utility methods relating to variable manipulation
 */
public class VarUtils
{
	/**
	 * Is an Expression a variable reference,
	 * i.e., an ExpressionVar, plus some combination of array accesses
	 */
	public static boolean isVarRef(Expression expr)
	{
		if (expr instanceof ExpressionVar) {
			return true;
		} else if (expr instanceof ExpressionArrayAccess) {
			return isVarRef(((ExpressionArrayAccess) expr).getArray());
		} else {
			return false;
		}
	}
	
	/**
	 * Get the name of the top-level variable for a variable reference,
	 * e.g. a[i][0], encoded as an Expression, returns "a".
	 */
	public static String getVarNameFromVarRef(Expression varRef)
	{
		if (varRef instanceof ExpressionVar) {
			return ((ExpressionVar) varRef).getName();
		} else if (varRef instanceof ExpressionArrayAccess) {
			return getVarNameFromVarRef(((ExpressionArrayAccess) varRef).getArray());
		}
		return null;
	}
	
	/**
	 * Get the top-level variable part of a variable reference,
	 * e.g. a[i][0], encoded as an Expression, returns the ExpressionVar for "a".
	 */
	public static Expression getVarFromVarRef(Expression varRef)
	{
		if (varRef instanceof ExpressionVar) {
			return varRef;
		} else if (varRef instanceof ExpressionArrayAccess) {
			return getVarFromVarRef(((ExpressionArrayAccess) varRef).getArray());
		}
		return null;
	}
	
	/**
	 * Convert a variable reference to a string in the context of a state.
	 * e.g. a[i+1][0], in state i=1, returns "a[2][0]".
	 */
	public static String varRefToString(Expression varRef, EvaluateContext ec) throws PrismLangException
	{
		return evaluateVarRef(varRef, ec).toString();
	}
	
	/**
	 * Evaluate a variable reference in the context of a state.
	 * The "evaluated" reference is returned as a new Expression, but with indices/etc. evaluated.
	 * e.g. a[i+1][0], in state i=1, returns a variable reference for a[2][0].
	 */
	public static Expression evaluateVarRef(Expression varRef, EvaluateContext ec) throws PrismLangException
	{
		if (varRef instanceof ExpressionVar) {
			return varRef;
		} else if (varRef instanceof ExpressionArrayAccess) {
			Expression array = evaluateVarRef(((ExpressionArrayAccess) varRef).getArray(), ec);
			Object index = ((ExpressionArrayAccess) varRef).getIndex().evaluate(ec);
			return new ExpressionArrayAccess(array, Expression.Literal(index));
		} else {
			throw new PrismLangException("Invalid variable reference", varRef);
		}
	}
	
	/**
	 * Read the value of a variable (or subvariable), specified by a variable reference,
	 * as it appears in a State object. This uses the indexing information cached in Expressions.
	 * 
	 * Note: this is similar to {@code varRef.evaluate(state)}, but will not complain
	 * about any missing values (just return null), nor do any type casting.
	 * 
	 * Also, any array indices are assumed to be constant, rather than evaluated
	 * using the State, or any other information. In fact, it also assumes that
	 * indices are literals and do not use any constant values.
	 */
	public static Object readVarValueFromState(Expression varRef, State state) throws PrismLangException
	{
		return readVarValueFromState(varRef, new EvaluateContextConstants(null), state);
	}
	
	/**
	 * Read the value of a variable (or subvariable), specified by a variable reference,
	 * as it appears in a State object. This uses the indexing information cached in Expressions.
	 * 
	 * Note: this is similar to {@code varRef.evaluate(state)}, but will not complain
	 * about any missing values (just return null), nor do any type casting.
	 * 
	 * Any non-literal array indices are evaluated using the passed in EvaluateContext,
	 * not the State object.
	 */
	public static Object readVarValueFromState(Expression varRef, EvaluateContext ec, State state) throws PrismLangException
	{
		if (varRef instanceof ExpressionVar) {
			// For a variable, just get the index and access the State accordingly
			int varIndex = ((ExpressionVar) varRef).getVarIndex();
			return state.varValues[varIndex];
		} else if (varRef instanceof ExpressionArrayAccess) {
			// Get the values in the array recursively
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) readVarValueFromState(((ExpressionArrayAccess) varRef).getArray(), state);
			// Get array index (including bounds check) and extract required element
			int arrayIndex = ((ExpressionArrayAccess) varRef).evaluateIndex(ec, list.size());
			return list.get(arrayIndex);
		} else {
			throw new PrismLangException("Invalid variable reference", varRef);
		}
	}
	
	/**
	 * Update the value of a variable (or subvariable) as it appears in a State object.
	 * The variable is specified by providing a variable reference (as an Expression)
	 * and an EvaluateContext which may be needed, e.g., to evaluate array indices.
	 * This uses the indexing information cached in Expressions.
	 * 
	 * The correct value to be assigned to the variable is also determined.
	 * This takes care of two things. Firstly, it casts the resulting Object to the right
	 * type required for the variable, e.g., mapping Integer to Double.
	 * Secondly, it deals with block array assignments, e.g. b'=2 as shorthand for
	 * b[0]'=2 & ... & b[n]'=2. This would convert integer 2 to list [2,...,2].
	 * 
	 * Value can also be null (in which case the type is ignored).
	 * 
	 * @param varRef Variable to be updated (reference to)
	 * @param ec Context to evaluate variable references (e.g., array indices)
	 * @param value Value to be assigned
	 * @param type Type of value to be assigned (from)
	 * @param varList The VarList
	 */
	@SuppressWarnings("unchecked")
	public static void assignVarValueInState(Expression varRef, EvaluateContext ec, State state, Object value, Type type, VarList varList) throws PrismLangException
	{
		// First get the proper value to be assigned
		value = varList.getVarAssignmentValue(varRef, value, type);
		// If reference is directly to a variable, just update State array directly
		if (varRef instanceof ExpressionVar) {
			state.setValue(((ExpressionVar) varRef).getVarIndex(), value);
		}
		// Otherwise, we need to navigate a sequence of lists
		else {
			// Flatten var reference (because traversing both up and down is tricky)
			List<Expression> varRefFlat = flattenVarRef(varRef);
			// First list obtained from State
			int varIndex = ((ExpressionVar) varRefFlat.get(0)).getVarIndex();
			List<Object> list = (List<Object>) state.varValues[varIndex];
			for (int i = 1; i < varRefFlat.size(); i++) {
				// Get the index for accessing the next list
				Expression ptr = varRefFlat.get(i);
				int accessIndex = -1;
				if (ptr instanceof ExpressionArrayAccess) {
					accessIndex = ((ExpressionArrayAccess) ptr).evaluateIndex(ec, list.size());
				} else {
					throw new PrismLangException("Invalid variable reference", varRef);
				}
				// Either navigate to the next list
				if (i < varRefFlat.size() - 1) {
					list = (List<Object>) list.get(accessIndex);
				}
				// Or (for the final one) update the required element of the list
				else {
					list.set(accessIndex, value);
				}
			}
		}
	}
	
	/**
	 * Check whether a value, encoded as an Object is null, or contains null,
	 * usually indicating that a variable (or subvariable) is undefined.
	 * This basically recurses into lists within this Object checking for nulls. 
	 */
	public static boolean valueContainsNull(Object val)
	{
		if (val == null) {
			return true;
		} else if (val instanceof List<?>) {
			for (Object o : (List<?>) val) {
				if (valueContainsNull(o)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Return a copy of an expression with all (maximal) variable references
	 * from a list replaced with provided values.
	 */
	public static Expression replaceVarRefs(final Expression expr, final List<Expression> varRefs, final Object varRefVals[]) throws PrismLangException
	{
		return (Expression) expr.deepCopy().accept(new ASTTraverseModify() {
			public Object visit(ExpressionArrayAccess e) throws PrismLangException
			{
				return new ExpressionLiteral(e.getType(), varRefVals[varRefs.indexOf(e)]);
			}
			
			public Object visit(ExpressionVar e) throws PrismLangException
			{
				return new ExpressionLiteral(e.getType(), varRefVals[varRefs.indexOf(e)]);
			}
		});
	}
	
	// Private helper methods
	
	/**
	 * Flatten a variable reference into a list of its sub-references,
	 * e.g. a[i][0] becomes [a, a[i], a[i][0]
	 */
	public static List<Expression> flattenVarRef(Expression varRef)
	{
		List<Expression> varRefFlat = new ArrayList<>();
		flattenVarRefRec(varRef, varRefFlat);
		return varRefFlat;
	}
	
	/**
	 * Recursive helper method for {@link #flattenVarRef(Expression)}.
	 */
	private static void flattenVarRefRec(Expression varRef, List<Expression> varRefFlat)
	{
		if (varRef instanceof ExpressionArrayAccess) {
			flattenVarRefRec(((ExpressionArrayAccess) varRef).getArray(), varRefFlat);
		}
		varRefFlat.add(varRef);
	}
}
