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

package parser.visitor;

import java.util.List;
import java.util.function.Predicate;

import parser.ast.Expression;
import parser.ast.ExpressionArrayAccess;
import parser.ast.ExpressionStructAccess;
import parser.ast.ExpressionVar;
import parser.type.Type;
import prism.PrismLangException;

/**
 * Get all variable references (i.e., Expression objects referring to a variable
 * or subvariable) whose type satisfies a predicate, and store them in a list.
 * This descends recursively, so if a[i][j] appears, then a, a[i] and a[i][j]
 * will all be tested against the predicate and potentially added.
 */
public class GetAllVarRefs extends ASTTraverse
{
	private Predicate<Type> pred;
	private List<Expression> list;
	
	public GetAllVarRefs(Predicate<Type> pred, List<Expression> list)
	{
		this.pred = pred;
		this.list = list;
	}
	
	public void visitPost(ExpressionArrayAccess e) throws PrismLangException
	{
		if (pred.test(e.getType()) && !list.contains(e)) {
			list.add(e);
		}
	}
	
	public void visitPost(ExpressionStructAccess e) throws PrismLangException
	{
		if (pred.test(e.getType()) && !list.contains(e)) {
			list.add(e);
		}
	}
	
	public void visitPost(ExpressionVar e) throws PrismLangException
	{
		if (pred.test(e.getType()) && !list.contains(e)) {
			list.add(e);
		}
	}
}

