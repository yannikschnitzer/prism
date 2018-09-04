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
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class ExpressionStructAccess extends Expression
{
	/** Array being accessed */
	protected Expression struct = null;
	/** name of struct field being accessed */
	protected String fieldName = null;

	/** Cached index of the field within the struct being accessed */
	protected int fieldIndex;
	
	// Constructors

	public ExpressionStructAccess(Expression struct, String fieldName)
	{
		this.struct = struct;
		this.fieldName = fieldName;
	}

	// Set methods

	public void setStruct(Expression struct)
	{
		this.struct = struct;
	}

	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}

	/**
	 * Set the index of the field within the struct being accessed
	 */
	public void setFieldIndex(int fieldIndex)
	{
		this.fieldIndex = fieldIndex;
	}
	
	// Get methods

	public Expression getStruct()
	{
		return struct;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	/**
	 * Get the index of the field within the struct being accessed
	 */
	public int getFieldIndex()
	{
		return fieldIndex;
	}
	
	// Methods required for Expression:

	@Override
	public boolean isConstant()
	{
		return struct.isConstant();
	}

	@Override
	public boolean isProposition()
	{
		return struct.isProposition();
	}

	@Override
	public Object evaluate(EvaluateContext ec) throws PrismLangException
	{
		// Evaluate struct and return field
		return ((List<?>) struct.evaluate(ec)).get(fieldIndex);
	}
	
	@Override
	public boolean returnsSingleValue()
	{
		return struct.returnsSingleValue();
	}

	// Methods required for ASTElement:

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	@Override
	public ExpressionStructAccess deepCopy(DeepCopy copier) throws PrismLangException
	{
		struct = copier.copy(struct);

		return this;
	}

	@Override
	public ExpressionStructAccess clone()
	{
		return (ExpressionStructAccess) super.clone();
	}
	
	// Standard methods
	
	@Override
	public String toString()
	{
		return struct + "." + fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((struct == null) ? 0 : struct.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		ExpressionStructAccess other = (ExpressionStructAccess) obj;
		if (struct == null) {
			if (other.struct != null)
				return false;
		} else if (!struct.equals(other.struct))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}
}

// ------------------------------------------------------------------------------
