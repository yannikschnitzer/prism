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

package parser.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import parser.EvaluateContext.EvalMode;
import prism.PrismLangException;

public class TypeArray extends Type 
{
	private static Map<Type, TypeArray> singletons;
	
	static
	{
		singletons = new HashMap<Type, TypeArray>();
	}
	
	private Type subType;
	
	private TypeArray(Type subType)
	{
		this.subType = subType;
	}

	public static TypeArray getInstance(Type subType)
	{
		return singletons.computeIfAbsent(subType, TypeArray::new);
	}
	
	public Type getSubType() 
	{
		return subType;
	}

	public void setSubType(Type subType) 
	{
		this.subType = subType;
	}
	
	// Methods required for Type:
	
	@Override
	public String getTypeString()
	{
		return "array of " + subType.getTypeString();
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public Object defaultValue()
	{
		// The array size is unknown, but the default is to block assign
		// all array elements the default value for the subtype
		return subType.defaultValue();
	}
	
	@Override
	public boolean canCastTypeTo(Type type)
	{
		return (type instanceof TypeArray) && (subType.canCastTypeTo(((TypeArray) type).getSubType()));
	}
	
	@Override
	public List<?> castValueTo(Object value) throws PrismLangException
	{
		// Must be a list
		if (!(value instanceof List<?>)) {
			throw new PrismLangException("Can't convert " + value.getClass() + " to type " + getTypeString());
		}
		// Cast each element of list separately
		// TODO: wasteful to always create new list?
		List<Object> newList = new ArrayList<>();
		for (Object o : (List<?>) value) {
			newList.add(subType.castValueTo(o));
		}
		return newList;
	}

	@Override
	public List<?> castValueTo(Object value, EvalMode evalMode) throws PrismLangException
	{
		// Must be a list
		if (!(value instanceof List<?>)) {
			throw new PrismLangException("Can't convert " + value.getClass() + " to type " + getTypeString());
		}
		// Cast each element of list separately
		// TODO: wasteful to always create new list?
		List<Object> newList = new ArrayList<>();
		for (Object o : (List<?>) value) {
			newList.add(subType.castValueTo(o, evalMode));
		}
		return newList;
	}
	
	@Override
	public void checkAssignAllowed(Type rhs) throws PrismLangException
	{
		int dimThis = getArrayDimension(this);
		int dimRhs = getArrayDimension(rhs);
		// Dimensions match, check recursively
		if (dimThis == dimRhs) {
			getSubType().checkAssignAllowed(((TypeArray) rhs).getSubType());
		}
		// Could be a block assignment to the array, check recursively
		else if (dimThis > dimRhs) {
			getSubType().checkAssignAllowed(rhs);
		}
		// Dimension mismatch
		else {
			throw new PrismLangException("Cannot assign " + rhs + " to " + this);
		}
	}
	
	@Override
	public boolean contains(Predicate<Type> pred)
	{
		return pred.test(this) | subType.contains(pred);
	}
	
	@Override
	public Object accept(TypeTraverseModify v) throws PrismLangException
	{
		return v.visit(this);
	}

	/**
	 * Get the number if dimensions of an array type,
	 * i.e., the number of nested {@code TypeArray}s.
	 * Returns 0 if not an array.
	 */
	public static int getArrayDimension(Type type)
	{
		int arrayDim = 0;
		Type ptr = type;
		while (ptr instanceof TypeArray) {
			ptr = ((TypeArray) ptr).getSubType();
			arrayDim++;
		}
		return arrayDim;
	}
	
	// Standard methods:
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof TypeArray) {
			TypeArray oa = (TypeArray)o;
			return (subType.equals(oa.getSubType()));
		}
		
		return false;
	}
}
