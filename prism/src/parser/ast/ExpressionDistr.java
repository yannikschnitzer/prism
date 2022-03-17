//==============================================================================
//	
//	Copyright (c) 2022-
//	Authors:
//	* Dave Parker <d.a.parker@bham.ac.uk> (University of Birmingham)
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
import java.util.Objects;

import parser.EvaluateContext;
import parser.visitor.ASTVisitor;
import prism.PrismLangException;

public class ExpressionDistr extends Expression
{
	// Operands
	private ArrayList<Expression> probabilities;
	// Operands
	private ArrayList<Expression> expressions;

	// Constructors

	public ExpressionDistr()
	{
		probabilities = new ArrayList<Expression>();
		expressions = new ArrayList<Expression>();
	}

	// Set methods

	public void add(Expression probability, Expression expression)
	{
		probabilities.add(probability);
		expressions.add(expression);
	}

	public void setProbability(int i, Expression probability)
	{
		probabilities.set(i, probability);
	}

	public void setExpression(int i, Expression expression)
	{
		expressions.set(i, expression);
	}

	// Get methods

	public int size()
	{
		return probabilities.size();
	}

	public Expression getProbability(int i)
	{
		return probabilities.get(i);
	}

	public Expression getExpression(int i)
	{
		return expressions.get(i);
	}

	// Methods required for Expression:

	@Override
	public boolean isConstant()
	{
		int n = size();
		for (int i = 0; i < n; i++) {
			if (!getProbability(i).isConstant()) {
				return false;
			}
			if (!getExpression(i).isConstant()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isProposition()
	{
		int n = size();
		for (int i = 0; i < n; i++) {
			if (!getProbability(i).isProposition()) {
				return false;
			}
			if (!getExpression(i).isProposition()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Object evaluate(EvaluateContext ec) throws PrismLangException
	{
		throw new PrismLangException("Probabilistic evaluation not supported yet");
	}

	@Override
	public boolean returnsSingleValue()
	{
		int n = size();
		for (int i = 0; i < n; i++) {
			if (!getProbability(i).returnsSingleValue()) {
				return false;
			}
			if (!getExpression(i).returnsSingleValue()) {
				return false;
			}
		}
		return true;
	}

	// Methods required for ASTElement:

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	@Override
	public Expression deepCopy()
	{
		ExpressionDistr e = new ExpressionDistr();
		int n = size();
		for (int i = 0; i < n; i++) {
			e.add(getProbability(i).deepCopy(), getExpression(i).deepCopy());
		}
		e.setType(type);
		e.setPosition(this);
		return e;
	}
	
	// Standard methods
	
	@Override
	public String toString()
	{
		String s = "";
		int n = size();
		if (n > 0) {
			s += getProbability(0) + ":" + getExpression(0);
			for (int i = 1; i < n; i++) {
				s += " + " + getProbability(i) + ":" + getExpression(i);
			}
		}
		return s;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(expressions, probabilities);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionDistr other = (ExpressionDistr) obj;
		return Objects.equals(expressions, other.expressions) && Objects.equals(probabilities, other.probabilities);
	}
}

// ------------------------------------------------------------------------------
