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

package parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import parser.EvaluateContext;
import parser.type.Type;
import parser.type.TypeArray;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionArray extends Expression
{
	// Elements
	private ArrayList<Expression> elements;
	
	
	// Constructor

	public ExpressionArray()
	{
		elements = new ArrayList<>();
	}

	// Set methods

	public void addElement(Expression e)
	{
		elements.add(e);
	}

	public void setElement(int i, Expression e)
	{
		elements.set(i, e);
	}

	// Get methods

	public int getNumElements()
	{
		return elements.size();
	}

	public Expression getElement(int i)
	{
		return elements.get(i);
	}

	// Methods required for Expression:

	@Override
	public boolean isConstant()
	{
		int n = getNumElements();
		for (int i = 0; i < n; i++) {
			if (!getElement(i).isConstant())
				return false;
		}
		return true;
	}

	@Override
	public boolean isProposition()
	{
		int n = getNumElements();
		for (int i = 0; i < n; i++) {
			if (!getElement(i).isProposition())
				return false;
		}
		return true;
	}
	
	@Override
	public Object evaluate(EvaluateContext ec) throws PrismLangException
	{
		try {
			Type subType = ((TypeArray) getType()).getSubType();
			int n = getNumElements();
			List<Object> eval = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				// NB: Cast each element to a common subtype
				eval.add(subType.castValueTo(getElement(i).evaluate(ec)));
			}
			return eval;
		} catch (PrismLangException e) {
			e.setASTElement(this);
			throw e;
		}
	}

	/**
	 * Apply an "add-to-array" operation, where the value has been evaluated to an Object
	 * and the array is a List&lt;Object&gt; in the process of being created.
	 * The value to be added is cast to the correct type (e.g., an integer being added
	 * to an array of doubles is cast to a double), and the list is returned.
	 */
	@SuppressWarnings("unchecked")
	public Object applyAdd(Object array, Object value) throws PrismLangException
	{
		Type subType = ((TypeArray) getType()).getSubType();
		List<Object> list = ((List<Object>) array);
		list.add(subType.castValueTo(value));
		return list;
	}
	
	@Override
	public boolean returnsSingleValue()
	{
		int n = getNumElements();
		for (int i = 0; i < n; i++) {
			if (!getElement(i).returnsSingleValue())
				return false;
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
	public ExpressionArray deepCopy(DeepCopy copier) throws PrismLangException
	{
		copier.copyAll(elements);

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExpressionArray clone()
	{
		ExpressionArray clone = (ExpressionArray) super.clone();

		clone.elements = (ArrayList<Expression>) elements.clone();

		return clone;
	}
	
	// Standard methods
	
	@Override
	public String toString()
	{
		return elements.stream().map(Expression::toString).collect(Collectors.joining(",", "[", "]"));
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(elements);
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
		ExpressionArray other = (ExpressionArray) obj;
		return Objects.equals(elements, other.elements);
	}
}
