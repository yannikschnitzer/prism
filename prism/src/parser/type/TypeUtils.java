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

package parser.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import param.BigRational;
import prism.PrismLangException;

/**
 * Utility methods relating to types
 */
public class TypeUtils
{
	/**
	 * Return a type to which both types can be cast.
	 * If one type is null, the other is returned.
	 * Throws an exception if no such common type exists.
	 */
	public static Type findCommonType(Type type1, Type type2) throws PrismLangException
	{
		if (type1 == null) {
			return type2;
		} else if (type2 == null) {
			return type1;
		} else if (type1.canCastTypeTo(type2)) {
			return type1;
		} else if (type2.canCastTypeTo(type1)) {
			return type2;
		} else {
			throw new PrismLangException("Types do not match: " + type1 + " and " + type2);
		}
	}
	
	/**
	 * Guess the type for a value stored as an Object,
	 * i.e., return a type which returns value of the same type of Object when evaluated. 
	 * Throws an exception if no type can be guessed. 
	 */
	public static Type guessTypeForValue(Object value) throws PrismLangException
	{
		if (value instanceof Boolean) {
			return TypeBool.getInstance();
		}
		if (value instanceof Integer || value instanceof BigInteger) {
			return TypeInt.getInstance();
		}
		if (value instanceof Double || value instanceof BigRational) {
			return TypeDouble.getInstance();
		}
		if (value instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) value;
			int size = list.size();
			Type subType = null;
			for (int i = 0; i < size; i++) {
				subType = findCommonType(subType, guessTypeForValue(list.get(i)));
			}
			return TypeArray.getInstance(subType);
		}
		throw new PrismLangException("Cannot guess type for value " + value);
	}
	
	/**
	 * Guess the types for a list of values stored as Objects,
	 * by applying {@link #guessTypeForValue(Object)} to each one.
	 * Throws an exception if any type cannot be guessed. 
	 */
	public static List<Type> guessTypesForValues(List<Object> values) throws PrismLangException
	{
		List<Type> types = new ArrayList<>();
		for (Object value : values) {
			types.add(guessTypeForValue(value));
		}
		return types;
	}
}

