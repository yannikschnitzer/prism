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

import prism.PrismLangException;

/**
 * Performs a depth-first traversal of a Type object instance,
 * replacing each child node with the object returned by the recursive visit call.
 * 
 * Simplified version of ASTTraverseModify for ASTElement objects.
 */
public class TypeTraverseModify
{
	public void defaultVisitPre(Type t) throws PrismLangException {}
	public void defaultVisitPost(Type t) throws PrismLangException {}
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeBool t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeBool t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypeBool t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeInt t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeInt t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypeInt t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeDouble t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeDouble t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypeDouble t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeClock t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeClock t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypeClock t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeArray t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeArray t) throws PrismLangException
	{
		visitPre(t);
		if (t.getSubType() != null) t = TypeArray.getInstance((Type)(t.getSubType().accept(this)));
		visitPost(t);
		return t;
	}
	public void visitPost(TypeArray t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeStruct t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeStruct t) throws PrismLangException
	{
		visitPre(t);
		int numFields = t.getNumFields();
		for (int i = 0; i < numFields; i++) {
			if (t.getFieldType(i) != null) t.setFieldType(i, (Type) (t.getFieldType(i).accept(this)));
		}
		visitPost(t);
		return t;
	}
	public void visitPost(TypeStruct t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeInterval t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeInterval t) throws PrismLangException
	{
		visitPre(t);
		if (t.getSubType() != null) t = TypeInterval.getInstance((Type)(t.getSubType().accept(this)));
		visitPost(t);
		return t;
	}
	public void visitPost(TypeInterval t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypePathBool t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypePathBool t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypePathBool t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypePathInt t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypePathInt t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypePathInt t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypePathDouble t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypePathDouble t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypePathDouble t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
	public void visitPre(TypeVoid t) throws PrismLangException { defaultVisitPre(t); }
	public Object visit(TypeVoid t) throws PrismLangException
	{
		visitPre(t);
		visitPost(t);
		return t;
	}
	public void visitPost(TypeVoid t) throws PrismLangException { defaultVisitPost(t); }
	// -----------------------------------------------------------------------------------
}

