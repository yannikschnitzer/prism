//==============================================================================
//	
//	Copyright (c) 2018-
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

import java.util.List;

import parser.EvaluateContext;
import parser.type.TypeInt;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionArrayAccess extends Expression
{
	/** Array being accessed */
	protected Expression array = null;
	/** Index into the array */
	protected Expression index = null;

	// Constructors

	public ExpressionArrayAccess(Expression array, Expression index)
	{
		this.array = array;
		this.index = index;
	}

	// Set methods

	/**
	 * Set the array part of this array access, i.e., the "a" in "a[i]".
	 */
	public void setArray(Expression array)
	{
		this.array = array;
	}

	/**
	 * Set the index part of this array access, i.e., the "i" in "a[i]".
	 */
	public void setIndex(Expression index)
	{
		this.index = index;
	}

	// Get methods

	/**
	 * Get the array part of this array access, i.e., the "a" in "a[i]".
	 */
	public Expression getArray()
	{
		return array;
	}

	/**
	 * Get the index part of this array access, i.e., the "i" in "a[i]".
	 */
	public Expression getIndex()
	{
		return index;
	}

	/**
	 * Evaluate the index part of this array access, i.e., the "i" in "a[i]",
	 * using the specified evaluation context, and return it as an integer.
	 * The index is also checked against the supplied array length and an
	 * exception is thrown if the index is out of bounds.
	 */
	public int evaluateIndex(EvaluateContext ec, int length) throws PrismLangException
	{
		int iIndex = getIndex().evaluateInt(ec);
		if (iIndex < 0 || iIndex >= length) {
			throw new PrismLangException("Array index out of bounds (index=" + iIndex + ", length=" + length + ")", this);
		}
		return iIndex;
	}

	// Methods required for Expression:

	@Override
	public boolean isConstant()
	{
		return array.isConstant() && index.isConstant();
	}

	@Override
	public boolean isProposition()
	{
		return array.isProposition() && index.isProposition();
	}

	@Override
	public Object evaluate(EvaluateContext ec) throws PrismLangException
	{
		try {
			return applyAccess(array.evaluate(ec), index.evaluate(ec));
		} catch (PrismLangException e) {
			throw new PrismLangException(e.getMessage(), this);
		}
	}
	
	/**
	 * Apply an "array access" operation, where the array and its index have been evaluated
	 * to Objects (which should be of (or castable to) type List<?> and Integer, respectively).
	 * Returns the value of the required array element as an Object.
	 * Throw an exception in case of type errors or index-out-of-bounds errors.
	 */
	@SuppressWarnings("unchecked")
	public static Object applyAccess(Object array, Object index) throws PrismLangException
	{
		int iIndex = (int) TypeInt.getInstance().castValueTo(index);
		List<Object> list = ((List<Object>) array);
		if (iIndex < 0 || iIndex >= list.size()) {
			throw new PrismLangException("Array index out of bounds (index=" + iIndex + ", length=" + list.size() + ")");
		}
		return list.get(iIndex);
	}
	
	@Override
	public boolean returnsSingleValue()
	{
		return array.returnsSingleValue() && index.returnsSingleValue();
	}

	// Methods required for ASTElement:

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	@Override
	public ExpressionArrayAccess deepCopy(DeepCopy copier) throws PrismLangException
	{
		array = copier.copy(array);
		index = copier.copy(index);

		return this;
	}

	@Override
	public ExpressionArrayAccess clone()
	{
		return (ExpressionArrayAccess) super.clone();
	}
	
	// Standard methods
	
	@Override
	public String toString()
	{
		return array + "[" + index + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((array == null) ? 0 : array.hashCode());
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionArrayAccess other = (ExpressionArrayAccess) obj;
		if (array == null) {
			if (other.array != null)
				return false;
		} else if (!array.equals(other.array))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		return true;
	}
}

// ------------------------------------------------------------------------------
