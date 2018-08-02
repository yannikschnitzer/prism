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

import parser.ast.Expression;
import parser.ast.ExpressionArrayAccess;
import parser.ast.ExpressionVar;
import prism.PrismLangException;

/**
 * Get all maximal variable references (i.e., Expression objects referring
 * to variables, but not their sub-references), and store them in a list.
 * So, if a[i][j] appears, then only a[i][j] will be included, not a or a[i].
 */
public class GetAllMaximalVarRefs extends ASTTraverse
{
	private List<Expression> list;
	
	public GetAllMaximalVarRefs(List<Expression> list)
	{
		this.list = list;
	}
	
	public Object visit(ExpressionArrayAccess e) throws PrismLangException
	{
		if (!list.contains(e)) {
			list.add(e);
		}
		return null;
	}
	
	public Object visit(ExpressionVar e) throws PrismLangException
	{
		if (!list.contains(e)) {
			list.add(e);
		}
		return null;
	}
}

