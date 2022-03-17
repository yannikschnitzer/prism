//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
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

import java.util.*;

import parser.ast.*;
import prism.PrismLangException;

public class ParserUtils
{
	/**
	 * Split a conjunction into a list of its component expressions, removing any parentheses.
	 * Note: this is purely syntactic, e.g. both "true" and "false" just result in a singleton list.  
	 * @param expr Expression to split.
	 */
	public static List<Expression> splitConjunction(Expression expr)
	{
		ArrayList<Expression> list = new ArrayList<Expression>();
		splitOnBinaryOp(expr, ExpressionBinaryOp.AND, list);
		return list;
	}
	
	/**
	 * Split a disjunction into a list of its component expressions, removing any parentheses.
	 * Note: this is purely syntactic, e.g. both "true" and "false" just result in a singleton list.  
	 * @param expr Expression to split.
	 */
	public static List<Expression> splitDisjunction(Expression expr)
	{
		ArrayList<Expression> list = new ArrayList<Expression>();
		splitOnBinaryOp(expr, ExpressionBinaryOp.OR, list);
		return list;
	}
	
	/**
	 * Split an expression into a list of its component expressions, based on a binary operator.
	 * Also remove any parentheses.
	 * @param expr Expression to split.
	 * @param op Code of operator to split on (from ExpressionBinaryOp).
	 * @param list Expression list in which to append resulting expressions.
	 */
	public static void splitOnBinaryOp(Expression expr, int op, List<Expression> list)
	{
		// Recursive case 1: brackets
		if (Expression.isParenth(expr)) {
			splitOnBinaryOp(((ExpressionUnaryOp)expr).getOperand(), op, list);				
			return;
		}
		// Recursive case 2: binary operator
		if (expr instanceof ExpressionBinaryOp) {
			if (((ExpressionBinaryOp)expr).getOperator() == op) {
				splitOnBinaryOp(((ExpressionBinaryOp)expr).getOperand1(), op, list);				
				splitOnBinaryOp(((ExpressionBinaryOp)expr).getOperand2(), op, list);				
				return;
			}
		}
		// Base case: anything else
		list.add(expr);
	}
	
	/**
	 * Attempt to repair a mis-parsed update expression.
	 * In particular, updates to Boolean variables such as b1'=b2&b3
	 * are parsed as a conjunction of two updates, b1'=b2 and b3,
	 * which is invalid since b3 is not an update.
	 * If the expression {@code update} is valid or contains cases such as the above
	 * that can be repaired, a valid update expression is thrown.
	 * Otherwise an exception is thrown.   
	 */
	public static Expression repairBooleanUpdate(Expression update) throws PrismLangException
	{
		// Distribution: recursively check/repair operands
		if (update instanceof ExpressionDistr) {
			int n = ((ExpressionDistr) update).size();
			for (int i = 0; i < n; i++) {
				Expression expri = repairBooleanUpdate(((ExpressionDistr) update).getExpression(i));
				((ExpressionDistr) update).setExpression(i, expri);
			}
			return update;
		}
		// Conjunction
		else if (Expression.isAnd(update)) {
			// Recursively check/repair operands
			Expression op1 = repairBooleanUpdate(((ExpressionBinaryOp) update).getOperand1()); 
			((ExpressionBinaryOp) update).setOperand1(op1);
			try {
				Expression op2 = repairBooleanUpdate(((ExpressionBinaryOp) update).getOperand2()); 
				((ExpressionBinaryOp) update).setOperand2(op2);
				// Valid conjunction of updates: return
				return update;
			} catch (PrismLangException e) {
				// If update is of form (x'=rhs)&op2
				if (Expression.isAssignment(op1)) {
					Expression rhs = ((ExpressionBinaryOp) op1).getOperand2();
					// Then we repair as x'=(rhs&op)
					((ExpressionBinaryOp) update).setOperand1(rhs);
					((ExpressionBinaryOp) op1).setOperand2(update);
					// TD
					return op1;
				} else {
					// Otherwise it's invalid
					throw e;
				}
			}
		}
		// Disjunction: not an update but maybe reparable
		else if (Expression.isOr(update)) {
			// Recursively check/repair operands
			Expression op1 = repairBooleanUpdate(((ExpressionBinaryOp) update).getOperand1());
			// If update is of form (x'=rhs)|op2
			if (Expression.isAssignment(op1)) {
				Expression rhs = ((ExpressionBinaryOp) op1).getOperand2();
				// Then we recreate as x'=(rhs|op)
				((ExpressionBinaryOp) update).setOperand1(rhs);
				((ExpressionBinaryOp) op1).setOperand2(update);
				return op1;
			} else {
				// Otherwise it's invalid
				throw new PrismLangException("Invalid update", update);
			}
		}
		// ITE: not an update but maybe reparable
		else if (update instanceof ExpressionITE) {
			// Recursively check/repair operands
			Expression op1 = repairBooleanUpdate(((ExpressionITE) update).getOperand1());
			// If update is of form (x'=rhs)?op2:op3
			if (Expression.isAssignment(op1)) {
				Expression rhs = ((ExpressionBinaryOp) op1).getOperand2();
				// Then we recreate as x'=(rhs?op2:op3)
				((ExpressionITE) update).setOperand1(rhs);
				((ExpressionBinaryOp) op1).setOperand2(update);
				return op1;
			} else {
				// Otherwise it's invalid
				throw new PrismLangException("Invalid update", update);
			}
		}
		// Assignment: valid
		else if (Expression.isAssignment(update)) {
			return update;
		}
		// "true": valid
		else if (Expression.isTrue(update)) {
			return update;
		}
		// Parentheses: recursively check/repair operand
		else if (Expression.isParenth(update)) {
			Expression op = repairBooleanUpdate(((ExpressionUnaryOp) update).getOperand());
			((ExpressionUnaryOp) update).setOperand(op);
			return update;
		}
		throw new PrismLangException("Invalid update", update);
	}
	
	/**
	 * Find the minimum value of an integer valued-expression
	 * with respect to a variable list and some values for constants.
	 */
	public static int findMinForIntExpression(Expression expr, VarList varList, Values constantValues) throws PrismLangException
	{
		List<String> vars;
		List<Values> allValues;
		int i, min;
		
		// For constant expressions, this is easy
		if (expr.isConstant())
			return expr.evaluateInt(constantValues);
		
		// Get all variables appearing in the expression and all values of them
		vars = expr.getAllVars();
		allValues = varList.getAllValues(vars);
		
		// Compute min over all values
		min = Integer.MAX_VALUE;
		for (Values varValues : allValues) {
			i = expr.evaluateInt(constantValues, varValues);
			if (i < min)
				min = i;
		}
		
		return min;
	}
	
	/**
	 * Find the maximum value of an integer valued-expression
	 * with respect to a variable list and some values for constants.
	 */
	public static int findMaxForIntExpression(Expression expr, VarList varList, Values constantValues) throws PrismLangException
	{
		List<String> vars;
		List<Values> allValues;
		int i, max;
		
		// For constant expressions, this is easy
		if (expr.isConstant())
			return expr.evaluateInt(constantValues);
		
		// Get all variables appearing in the expression and all values of them
		vars = expr.getAllVars();
		allValues = varList.getAllValues(vars);
		
		// Compute max over all values
		max = Integer.MIN_VALUE;
		for (Values varValues : allValues) {
			i = expr.evaluateInt(constantValues, varValues);
			if (i > max)
				max = i;
		}
		
		return max;
	}
	
	/**
	 * Find all possible values of an integer valued-expression
	 * with respect to a variable list and some values for constants.
	 */
	public static Collection<Integer> findAllValsForIntExpression(Expression expr, VarList varList, Values constantValues) throws PrismLangException
	{
		List<String> vars;
		List<Values> allValues;
		HashSet<Integer> res;
		
		// For constant expressions, this is easy
		if (expr.isConstant()) {
			res = new HashSet<Integer>();
			res.add(expr.evaluateInt(constantValues));
			return res;
		}
		
		// Get all variables appearing in the expression and all values of them
		vars = expr.getAllVars();
		allValues = varList.getAllValues(vars);
		
		// Compute set of all values
		res = new HashSet<Integer>();
		for (Values varValues : allValues) {
			res.add(expr.evaluateInt(constantValues, varValues));
		}
		
		return res;
	}
}
