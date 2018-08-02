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

import java.util.ArrayList;
import java.util.List;

import parser.EvaluateContext;
import parser.Values;
import parser.VarList;
import parser.type.Type;
import parser.visitor.ASTVisitor;
import parser.visitor.DeepCopy;
import prism.PrismLangException;
import prism.PrismUtils;

/**
 * Class to store list of (defined and undefined) constants
 */
public class ConstantList extends ASTElement
{
	// Name/expression/declaration type triples to define constants
	private ArrayList<String> names = new ArrayList<>();
	private ArrayList<Expression> constants = new ArrayList<>(); // these can be null, i.e. undefined
	private ArrayList<DeclarationType> declTypes = new ArrayList<>();
	// We also store an ExpressionIdent to match each name.
	// This is to just to provide positional info.
	private ArrayList<ExpressionIdent> nameIdents = new ArrayList<>();
	
	/**
	 * Construct an empty constants list.
	 */
	public ConstantList()
	{
	}

	/**
	 * Construct a constants list from a Values object, i.e., a list of name=value pairs.
	 * This is just for convenience and only works for constants of primitive type.
	 * In general, it is preferred to construct an empty list and then repeatedly call
	 * {@link #addConstant(ExpressionIdent, Expression, DeclarationType)}
	 * so that the type of each constant can be reliably determined.
	 */
	public ConstantList(Values constValues) throws PrismLangException
	{
		int numConsts = constValues.getNumValues();
		for (int i = 0; i < numConsts; i++) {
			ExpressionIdent nameIdent = new ExpressionIdent(constValues.getName(i));
			Expression constDefn = new ExpressionLiteral(type, constValues.getValue(i));
			addConstant(nameIdent, constDefn, constValues.getType(i));
		}
	}

	// Set methods
	
	/**
	 * Add a constant to this list.
	 * @param nameIdent The identifier for the name of the constant
	 * @param constDefn The expression defining the constant (or null, if undefined)
	 * @param declType The type of the constant, as a DeclarationType
	 */
	public void addConstant(ExpressionIdent nameIdent, Expression constDefn, DeclarationType declType)
	{
		names.add(nameIdent.getName());
		constants.add(constDefn);
		declTypes.add(declType);
		nameIdents.add(nameIdent);
	}

	/**
	 * Add a (primitive-typed) constant to this list.
	 * {@link #addConstant(ExpressionIdent, Expression, DeclarationType)} is preferred in general,
	 * and is required for non-primitive types (e.g., to find array sizes).
	 * @param nameIdent The identifier for the name of the constant
	 * @param constDefn The expression defining the constant (or null, if undefined)
	 * @param declType The type of the constant, as a Type
	 */
	public void addConstant(ExpressionIdent nameIdent, Expression constDefn, Type declType) throws PrismLangException
	{
		try {
			addConstant(nameIdent, constDefn, declType.defaultDeclarationType());
		} catch (PrismLangException e) {
			throw new PrismLangException("Cannot add constant of type " + declType);
		}
	}
	
	/**
	 * Set the type of the ith constant in the list.
	 */
	public void setConstantDeclarationType(int i, DeclarationType dt)
	{
		declTypes.set(i, dt);
	}

	/**
	 * Set the definition of the ith constant in the list (null if undefined).
	 */
	public void setConstant(int i, Expression c)
	{
		constants.set(i, c);
	}
	
	// Get methods

	/**
	 * Get the number of constants in the list.
	 */
	public int size()
	{
		return constants.size();
	}

	/**
	 * Get the name of the ith constant in the list.
	 */
	public String getConstantName(int i)
	{
		return names.get(i);
	}
	
	/**
	 * Get the identifier for the name of the ith constant in the list.
	 */
	public ExpressionIdent getConstantNameIdent(int i)
	{
		return nameIdents.get(i);
	}

	/**
	 * Get the definition of the ith constant in the list (null if undefined).
	 */
	public Expression getConstant(int i)
	{
		return constants.get(i);
	}
	
	/**
	 * Get the type of the ith constant in the list, as a DeclarationType.
	 */
	public DeclarationType getConstantDeclarationType(int i)
	{
		return declTypes.get(i);
	}

	/**
	 * Set the type of the ith constant in the list, as a Type.
	 */
	public Type getConstantType(int i)
	{
		return getConstantDeclarationType(i).getType();
	}

	/**
	 * Get the index of a constant by its name (returns -1 if it does not exist).
	 */
	public int getConstantIndex(String name)
	{
		return names.indexOf(name);
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
		declTypes.remove(i);
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
				List<String> v = e.getAllConstants();
				for (int j = 0; j < v.size(); j++) {
					int k = getConstantIndex(v.get(j));
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
	public List<String> getUndefinedConstants()
	{
		int i, n;
		Expression e;
		List<String> v = new ArrayList<>();

		n = constants.size();
		for (i = 0; i < n; i++) {
			e = getConstant(i);
			if (e == null) {
				v.add(getConstantName(i));
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
		// and replace all (defined) constant references with their definitions
		// (need to look in types too, e.g., for array sizes)
		ConstantList cl = (ConstantList) deepCopy();
		int n = size();
		for (int i = 0; i < n; i++) {
			Expression constant = cl.getConstant(i);
			cl.setConstant(i, constant == null ? null : (Expression) constant.expandConstants(cl, false));
			cl.setConstantDeclarationType(i, (DeclarationType) getConstantDeclarationType(i).expandConstants(cl, false));
		}
		
		// Evaluate all constants and store in a new Values object
		// We also create a VarList to help managing non-primitive constants
		Values allValues = new Values();
		VarList varListConsts = new VarList();
		varListConsts.setEvaluateContext(ec);
		for (int i = 0; i < n; i++) {
			varListConsts.addVar(cl.getConstantName(i), cl.getConstantDeclarationType(i), -1);
			// Try to evaluate the value to be assigned to the constant,
			// via its definition if present, or directly if not.
			// If this fails, only throw an exception if asked to.
			Expression constant = cl.getConstant(i);
			if (constant == null) {
				constant = new ExpressionConstant(cl.getConstantName(i), cl.getConstantType(i));
			}
			Object val = null;
			try {
				val = constant.evaluate(ec);
			} catch (PrismLangException ex) {
				if (all) {
					throw ex;
				}
			}
			// If evaluation was successful, assign the value to the constant
			if (val != null) {
				try {
					val = varListConsts.getVarAssignmentValue(varListConsts.getRef(i), val, constant.getType());
					allValues.addValue(cl.getConstantName(i), val);
				} catch (PrismLangException e) {
					// Attach an AST element for better error reporting
					e.setASTElement(constant);
					throw e;
				}
			}
		}
		
		return allValues;
	}

//	/**
//	 * Get the correct value to be assigned to the ith constant, represented by an object.
//	 * and an EvaluateContext which may be needed, e.g., to evaluate array lengths.
//	 *
//	 * This takes care of two things. Firstly, it casts the resulting Object to the right
//	 * type required for the constant, e.g., mapping Integer to Double.
//	 * Secondly, it deals with block array assignments, e.g. mapping 2 to [2,...,2].
//	 * If the value is null, then null is returned (and the type is ignored).
//	 *
//	 * @param i The index of the constant
//	 * @param ec Context to evaluate, e.g., array lengths (and supply eval mode)
//	 * @param value Value to be assigned
//	 * @param type Type of value to be assigned (from)
//	 */
//	public Object getConstAssignmentValue(int i, EvaluateContext ec, Object value, Type type) throws PrismLangException
//	{
//		if (!getConstantType(i).canAssign(type)) {
//			value = getConstAssignmentValueRec(getConstantDeclarationType(i), ec, value, type);
//		}
//		return value == null ? null : getConstantType(i).castValueTo(value, ec.getEvaluationMode());
//	}
//
//	/**
//	 * Recursive helper method for {@link #getConstAssignmentValue()}.
//	 * @param dt Declaration type for constant whose value is to be returned
//	 * @param ec Context to evaluate, e.g., array lengths
//	 * @param value Value to be assigned
//	 * @param type Type of value to be assigned (from)
//	 */
//	private static Object getConstAssignmentValueRec(DeclarationType dt, EvaluateContext ec, Object value, Type type) throws PrismLangException
//	{
//		// TODO: could create Var for each const and reuse var assm?
//		// Null: just return null
//		if (value == null) {
//			return null;
//		}
//		// Normal assignment: just casting possibly needing
//		else if (dt.getType().canAssign(type)) {
//			return value;
//		}
//		// Block assignment to array
//		else if (dt instanceof DeclarationArray) {
//			// Recurse to get value
//			Object subVal = getConstAssignmentValueRec(((DeclarationArray) dt).getSubtype(), ec, value, type);
//			// Then create new list of duplicate values
//			List<Object> l = new ArrayList<>();
//			int arraySize = ((DeclarationArray) dt).getLength().evaluateInt(ec);
//			for (int i = 0; i < arraySize; i++) {
//				l.add(subVal);
//			}
//			return l;
//		}
//		throw new PrismLangException("Cannot assign a value of type " + type + " to a constant of type " + dt.getType());
//	}

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
			s += getConstantDeclarationType(i) + " ";
			s += getConstantName(i);
			e = getConstant(i);
			if (e != null) {
				s += " = " + e;
			}
			s += ";\n";
		}
		
		return s;
	}
	
	@Override
	public ConstantList deepCopy(DeepCopy copier) throws PrismLangException
	{
		copier.copyAll(constants);
		copier.copyAll(declTypes);
		copier.copyAll(nameIdents);

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ConstantList clone()
	{
		ConstantList clone = (ConstantList) super.clone();

		clone.names      = (ArrayList<String>)          names.clone();
		clone.constants  = (ArrayList<Expression>)      constants.clone();
		clone.declTypes  = (ArrayList<DeclarationType>) declTypes.clone();
		clone.nameIdents = (ArrayList<ExpressionIdent>) nameIdents.clone();

		return clone;
	}
}

//------------------------------------------------------------------------------
