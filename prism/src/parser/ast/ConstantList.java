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

package parser.ast;

import java.util.Vector;

import parser.EvaluateContext;
import parser.Values;
import parser.type.Type;
import parser.type.TypeBool;
import parser.type.TypeDouble;
import parser.type.TypeInt;
import parser.visitor.ASTVisitor;
import prism.PrismLangException;
import prism.PrismUtils;

/**
 * Class to store list of (defined and undefined) constants
 */
public class ConstantList extends ASTElement
{
	// Name/expression/type triples to define constants
	private Vector<String> names = new Vector<String>();
	private Vector<Expression> constants = new Vector<Expression>(); // these can be null, i.e. undefined
	private Vector<Type> types = new Vector<Type>();
	// We also store an ExpressionIdent to match each name.
	// This is to just to provide positional info.
	private Vector<ExpressionIdent> nameIdents = new Vector<ExpressionIdent>();
	
	/** Constructor */
	public ConstantList()
	{
	}

	/** Constructor from a Values object, i.e., a list of name=value tuples */
	public ConstantList(Values constValues) throws PrismLangException
	{
		for (int i = 0; i < constValues.getNumValues(); i++) {
			Type type = constValues.getType(i);
			if (type.equals(TypeBool.getInstance()) ||
			    type.equals(TypeInt.getInstance()) ||
			    type.equals(TypeDouble.getInstance())) {
				addConstant(new ExpressionIdent(constValues.getName(i)),
				            new ExpressionLiteral(type, constValues.getValue(i)),
				            type);
			} else {
				throw new PrismLangException("Unsupported type for constant " + constValues.getName(i));
			}
		}
	}

	// Set methods
	
	public void addConstant(ExpressionIdent n, Expression c, Type t)
	{
		names.addElement(n.getName());
		constants.addElement(c);
		types.addElement(t);
		nameIdents.addElement(n);
	}
	
	public void setConstant(int i, Expression c)
	{
		constants.setElementAt(c, i);
	}
	
	// Get methods

	public int size()
	{
		return constants.size();
	}

	public String getConstantName(int i)
	{
		return names.elementAt(i);
	}
	
	public Expression getConstant(int i)
	{
		return constants.elementAt(i);
	}
	
	public Type getConstantType(int i)
	{
		return types.elementAt(i);
	}
	
	public ExpressionIdent getConstantNameIdent(int i)
	{
		return nameIdents.elementAt(i);
	}

	/**
	 * Get the index of a constant by its name (returns -1 if it does not exist).
	 */
	public int getConstantIndex(String s)
	{
		return names.indexOf(s);
	}

	/**
	 * Remove the constant with the given name.
	 * @param name the name of the constant
	 * @param ignoreNonexistent if true, don't throw an exception if the constant does not exist
	 * @throws PrismLangException if the constant does not exist (if not ignoreNonexistent)
	 */
	public void removeConstant(String name, boolean ignoreNonexistent) throws PrismLangException
	{
		int constantIndex = getConstantIndex(name);
		if (constantIndex == -1) {
			if (ignoreNonexistent) {
				return;
			}
			throw new PrismLangException("Can not remove nonexistent constant: " + name);
		}
		removeConstant(constantIndex);
	}

	/**
	 * Remove the constant with the given index.
	 * @param i the index
	 */
	public void removeConstant(int i)
	{
		names.remove(i);
		constants.remove(i);
		types.remove(i);
		nameIdents.remove(i);
	}

	/**
	 * Find cyclic dependencies.
	 */
	public void findCycles() throws PrismLangException
	{
		// Create boolean matrix of dependencies
		// (matrix[i][j] is true if constant i contains constant j)
		int n = constants.size();
		boolean matrix[][] = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			Expression e = getConstant(i);
			if (e != null) {
				Vector<String> v = e.getAllConstants();
				for (int j = 0; j < v.size(); j++) {
					int k = getConstantIndex(v.elementAt(j));
					if (k != -1) {
						matrix[i][k] = true;
					}
				}
			}
		}
		// Check for and report dependencies
		int firstCycle = PrismUtils.findCycle(matrix);
		if (firstCycle != -1) {
			String s = "Cyclic dependency in definition of constant \"" + getConstantName(firstCycle) + "\"";
			throw new PrismLangException(s, getConstant(firstCycle));
		}
	}
	
	/**
	 * Get the number of undefined constants in the list.
	 */
	public int getNumUndefined()
	{
		int i, n, res;
		Expression e;
		
		res = 0;
		n = constants.size();
		for (i = 0; i < n; i++) {
			e = getConstant(i);
			if (e == null) {
				res++;
			}
		}
		
		return res;
	}
	
	/**
	 * Get a list of the undefined constants in the list.
	 */
	public Vector<String> getUndefinedConstants()
	{
		int i, n;
		Expression e;
		Vector<String> v;
		
		v = new Vector<String>();
		n = constants.size();
		for (i = 0; i < n; i++) {
			e = getConstant(i);
			if (e == null) {
				v.addElement(getConstantName(i));
			}
		}
		
		return v;
	}
	
	/**
	 * Check if {@code name} is a *defined* constants in the list,
	 * i.e. a constant whose value was *not* left unspecified in the model/property.
	 */
	public boolean isDefinedConstant(String name)
	{
		int i = getConstantIndex(name);
		if (i == -1)
			return false;
		return (getConstant(i) != null);
	}

	/**
	 * Set values for *all* undefined constants, evaluate values for *all* constants
	 * and return a Values object with values for *all* constants.
	 * The values being provided for these constants, as well as any other constants needed,
	 * are provided in an EvaluateContext object. This also determines the evaluation mode.
	 */
	public Values evaluateConstants(EvaluateContext ec) throws PrismLangException
	{
		return evaluateSomeOrAllConstants(ec, true);
	}

	/**
	 * Set values for *some* undefined constants, evaluate values for constants where possible
	 * and return a Values object with values for all constants that could be evaluated.
	 * The values being provided for these constants, as well as any other constants needed,
	 * are provided in an EvaluateContext object. This also determines the evaluation mode.
	 */
	public Values evaluateSomeConstants(EvaluateContext ec) throws PrismLangException
	{
		return evaluateSomeOrAllConstants(ec, false);
	}

	/**
	 * Set values for *some* or *all* undefined constants, evaluate values for constants where possible
	 * and return a Values object with values for all constants that could be evaluated.
	 * The values being provided for these constants, as well as any other constants needed,
	 * are provided in an EvaluateContext object. This also determines the evaluation mode.
	 * If argument 'all' is true, an exception is thrown if any undefined constant is not defined.
	 */
	private Values evaluateSomeOrAllConstants(EvaluateContext ec, boolean all) throws PrismLangException
	{
		// Take a copy of this constant list,
		// and replace all (defined) constant references with their definitions)
		ConstantList cl = (ConstantList) deepCopy();
		int n = size();
		for (int i = 0; i < n; i++) {
			Expression constant = cl.getConstant(i);
			cl.setConstant(i, constant == null ? null : (Expression) constant.expandConstants(cl, false));
		}
		
		// Evaluate all constants and store in a new Values object
		Values allValues = new Values();
		for (int i = 0; i < n; i++) {
			Expression constant = cl.getConstant(i);
			if (constant == null) {
				constant = new ExpressionConstant(cl.getConstantName(i), cl.getConstantType(i));
			}
			try {
				Object val = constant.evaluate(ec);
				val = cl.getConstantType(i).castValueTo(val, ec.getEvaluationMode());
				allValues.addValue(cl.getConstantName(i), val);
			} catch (PrismLangException ex) {
				if (all) {
					throw ex;
				}
			}
		}
		
		return allValues;
	}

	// Methods required for ASTElement:
	
	/**
	 * Visitor method.
	 */
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}
	
	/**
	 * Convert to string.
	 */
	public String toString()
	{
		String s = "";
		int i, n;
		Expression e;
		
		n = constants.size();
		for (i = 0; i < n; i++) {
			s += "const ";
			s += getConstantType(i).getTypeString() + " ";
			s += getConstantName(i);
			e = getConstant(i);
			if (e != null) {
				s += " = " + e;
			}
			s += ";\n";
		}
		
		return s;
	}
	
	/**
	 * Perform a deep copy.
	 */
	public ASTElement deepCopy()
	{
		int i, n;
		ConstantList ret = new ConstantList();
		n = size();
		for (i = 0; i < n; i++) {
			Expression constantNew = (getConstant(i) == null) ? null : getConstant(i).deepCopy();
			ret.addConstant((ExpressionIdent)getConstantNameIdent(i).deepCopy(), constantNew, getConstantType(i));
		}
		ret.setPosition(this);
		return ret;
	}
}

//------------------------------------------------------------------------------
