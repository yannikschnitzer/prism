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
	 * Find the minimum value of an integer valued-expression
	 * with respect to a variable list and some values for constants.
	 */
	public static int findMinForIntExpression(Expression expr, VarList varList, Values constantValues) throws PrismLangException
	{
		// For constant expressions, this is easy
		if (expr.isConstant()) {
			return expr.evaluateInt(constantValues);
		}
		// Otherwise find all possible values and compute min
		List<Integer> allValues = varList.getAllValuesForIntExpression(expr, constantValues);
		int min = Integer.MAX_VALUE;
		for (int i : allValues) {
			if (i < min) {
				min = i;
			}
		}
		return min;
	}
	
	/**
	 * Find the maximum value of an integer valued-expression
	 * with respect to a variable list and some values for constants.
	 */
	public static int findMaxForIntExpression(Expression expr, VarList varList, Values constantValues) throws PrismLangException
	{
		// For constant expressions, this is easy
		if (expr.isConstant()) {
			return expr.evaluateInt(constantValues);
		}
		// Otherwise find all possible values and compute max
		List<Integer> allValues = varList.getAllValuesForIntExpression(expr, constantValues);
		int max = Integer.MIN_VALUE;
		for (int i : allValues) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}
	
	/**
	 * Find all possible values of an integer valued-expression
	 * with respect to a variable list and some values for constants.
	 */
	public static Collection<Integer> findAllValsForIntExpression(Expression expr, VarList varList, Values constantValues) throws PrismLangException
	{
		// For constant expressions, this is easy
		if (expr.isConstant()) {
			List<Integer> res = new ArrayList<Integer>();
			res.add(expr.evaluateInt(constantValues));
			return res;
		}
		// Otherwise find all possible values
		return varList.getAllValuesForIntExpression(expr, constantValues);
	}
}
