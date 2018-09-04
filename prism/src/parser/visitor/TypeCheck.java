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

package parser.visitor;

import parser.ast.*;
import parser.type.*;
import prism.PrismLangException;

/**
 * Check for type-correctness and compute type.
 * Optionally pass in a PropertiesFile in order to find types of referenced properties.
 */
public class TypeCheck extends ASTTraverse
{
	private PropertiesFile propertiesFile = null;

	public TypeCheck()
	{
		this.propertiesFile = null;
	}

	public TypeCheck(PropertiesFile propertiesFile)
	{
		this.propertiesFile = propertiesFile;
	}

	public void visitPost(ModulesFile e) throws PrismLangException
	{
		if (e.getInitialStates() != null && !(e.getInitialStates().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: Initial states definition must be Boolean", e.getInitialStates());
		}
	}

	public void visitPost(Property e) throws PrismLangException
	{
		e.setType(e.getExpression().getType());
	}

	public void visitPost(FormulaList e) throws PrismLangException
	{
		// Formulas are defined at the text level and are type checked after
		// substitutions have been applied
	}

	public void visitPost(LabelList e) throws PrismLangException
	{
		int i, n;
		n = e.size();
		for (i = 0; i < n; i++) {
			if (!(e.getLabel(i).getType() instanceof TypeBool)) {
				throw new PrismLangException("Type error: Label \"" + e.getLabelName(i) + "\" is not Boolean", e.getLabel(i));
			}
		}
	}

	public void visitPost(ConstantList e) throws PrismLangException
	{
		int i, n;
		n = e.size();
		for (i = 0; i < n; i++) {
			if (e.getConstantType(i).contains(t -> t instanceof TypeClock)) {
				throw new PrismLangException("Constants of type clock are not allowed", e.getConstant(i));
			}
			if (e.getConstant(i) != null) {
				try {
					e.getConstantType(i).checkAssignAllowed(e.getConstant(i).getType());
				} catch (PrismLangException ex) {
					throw new PrismLangException("Type error in value for constant " + e.getConstantName(i) + ": " + ex.getMessage(), e.getConstant(i));
				}
			}
		}
	}

	public void visitPost(Declaration e) throws PrismLangException
	{
		// Check initial value if present
		if (e.getStart() != null) {
			try {
				e.getType().checkAssignAllowed(e.getStart().getType());
			} catch (PrismLangException ex) {
				throw new PrismLangException("Type error in initial value of " + e.getName() + ": " + ex.getMessage(), e.getStart());
			}
		}
	}

	public void visitPost(DeclarationInt e) throws PrismLangException
	{
		if (e.getLow() != null && !TypeInt.getInstance().canCastTypeTo(e.getLow().getType())) {
			throw new PrismLangException("Type error: Integer range lower bound \"" + e.getLow() + "\" is not an integer", e.getLow());
		}
		if (e.getHigh() != null && !TypeInt.getInstance().canCastTypeTo(e.getHigh().getType())) {
			throw new PrismLangException("Type error: Integer range upper bound \"" + e.getHigh() + "\" is not an integer", e.getHigh());
		}
	}

	public void visitPost(DeclarationArray e) throws PrismLangException
	{
		if (e.getLength() != null && !TypeInt.getInstance().canCastTypeTo(e.getLength().getType())) {
			throw new PrismLangException("Type error: Array length \"" + e.getLength() + "\" is not an integer", e.getLength());
		}
	}

	public void visitPost(Command e) throws PrismLangException
	{
		if (!(e.getGuard().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: Guard is not Boolean", e.getGuard());
		}
	}

	public void visitPost(Updates e) throws PrismLangException
	{
		int i, n;
		n = e.getNumUpdates();
		for (i = 0; i < n; i++) {
			if (e.getProbability(i) != null) {
				Type typeProb = e.getProbability(i).getType();
				boolean typeOK = TypeDouble.getInstance().canCastTypeTo(typeProb);
				typeOK |= typeProb instanceof TypeArray && TypeDouble.getInstance().canCastTypeTo(((TypeArray) typeProb).getSubType());
				if (!typeOK) {
					throw new PrismLangException("Type error: Update probability/rate cannot have type " + typeProb, e.getProbability(i));
				}
			}
		}
	}

	public void visitPost(UpdateElement e) throws PrismLangException
	{
		// Check assignment is allowed
		try {
			e.getVarRef().getType().checkAssignAllowed(e.getExpression().getType());
		} catch (PrismLangException ex) {
			throw new PrismLangException("Type error in update to " + e.getVarRef() + ": " + ex.getMessage(), e.getExpression());
		}
	}

	public void visitPost(RewardStructItem e) throws PrismLangException
	{
		if (!(e.getStates().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error in reward struct item: guard must be Boolean", e.getStates());
		}
		if (!TypeDouble.getInstance().canCastTypeTo(e.getReward().getType())) {
			throw new PrismLangException("Type error in reward struct item: value must be an int or double", e.getReward());
		}
	}

	public void visitPost(ExpressionTemporal e) throws PrismLangException
	{
		Type type;
		if (e.getLowerBound() != null && !TypeDouble.getInstance().canCastTypeTo(e.getLowerBound().getType())) {
			throw new PrismLangException("Type error: Lower bound in " + e.getOperatorSymbol() + " operator must be an int or double", e.getLowerBound());
		}
		if (e.getUpperBound() != null && !TypeDouble.getInstance().canCastTypeTo(e.getUpperBound().getType())) {
			throw new PrismLangException("Type error: Upper bound in " + e.getOperatorSymbol() + " operator must be an int or double", e.getUpperBound());
		}
		switch (e.getOperator()) {
		case ExpressionTemporal.P_X:
		case ExpressionTemporal.P_U:
		case ExpressionTemporal.P_F:
		case ExpressionTemporal.P_G:
		case ExpressionTemporal.P_W:
		case ExpressionTemporal.P_R:
			if (e.getOperand1() != null) {
				type = e.getOperand1().getType();
				if (!(type instanceof TypeBool) && !(type instanceof TypePathBool))
					throw new PrismLangException("Type error: Argument of " + e.getOperatorSymbol() + " operator is not Boolean", e.getOperand1());
			}
			if (e.getOperand2() != null) {
				type = e.getOperand2().getType();
				if (!(type instanceof TypeBool) && !(type instanceof TypePathBool))
					throw new PrismLangException("Type error: Argument of " + e.getOperatorSymbol() + " operator is not Boolean", e.getOperand2());
			}
			e.setType(TypePathBool.getInstance());
			break;
		case ExpressionTemporal.R_C:
		case ExpressionTemporal.R_I:
		case ExpressionTemporal.R_S:
			e.setType(TypePathDouble.getInstance());
			break;
		}
	}

	public void visitPost(ExpressionITE e) throws PrismLangException
	{
		Type t1 = e.getOperand1().getType();
		Type t2 = e.getOperand2().getType();
		Type t3 = e.getOperand3().getType();

		if (!(t1 instanceof TypeBool)) {
			throw new PrismLangException("Type error: condition of ? operator is not Boolean", e.getOperand1());
		}
		if (!(t2.canCastTypeTo(t3) || t3.canCastTypeTo(t2))) {
			throw new PrismLangException("Type error: types for then/else operands of ? operator must match", e);
		}

		if (t2.canCastTypeTo(t3)) {
			e.setType(t2);
		} else {
			e.setType(t3);
		}
	}

	public void visitPost(ExpressionBinaryOp e) throws PrismLangException
	{
		Type t1 = e.getOperand1().getType();
		if (t1 == null)
			e.getOperand1().getType();
		Type t2 = e.getOperand2().getType();
		boolean ok;

		switch (e.getOperator()) {
		case ExpressionBinaryOp.IMPLIES:
		case ExpressionBinaryOp.IFF:
		case ExpressionBinaryOp.OR:
		case ExpressionBinaryOp.AND:
			if (!(t1 instanceof TypeBool) && !(t1 instanceof TypePathBool)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " applied to non-Boolean expression", e.getOperand1());
			}
			if (!(t2 instanceof TypeBool) && !(t2 instanceof TypePathBool)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " applied to non-Boolean expression", e.getOperand2());
			}
			e.setType(t1 instanceof TypePathBool || t2 instanceof TypePathBool ? TypePathBool.getInstance() : TypeBool.getInstance());
			break;
		case ExpressionBinaryOp.EQ:
		case ExpressionBinaryOp.NE:
			if (!(t1.canCastTypeTo(t2) || t2.canCastTypeTo(t1))) {
				if (t1.equals(t2)) {
					throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " cannot compare " + t1 + "s", e);
				} else {
					throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " cannot compare " + t1 + " and " + t2, e);
				}
			}
			// Additional checks for clocks
			if (t1 instanceof TypeClock && !(t2 instanceof TypeClock || TypeInt.getInstance().canCastTypeTo(t2))) {
				throw new PrismLangException("Clocks can only be compared to integers or other clocks", e);
			}
			if (t2 instanceof TypeClock && !(t1 instanceof TypeClock || TypeInt.getInstance().canCastTypeTo(t1))) {
				throw new PrismLangException("Clocks can only be compared to integers or other clocks", e);
			}
			e.setType(TypeBool.getInstance());
			break;
		case ExpressionBinaryOp.GT:
		case ExpressionBinaryOp.GE:
		case ExpressionBinaryOp.LT:
		case ExpressionBinaryOp.LE:
			ok = false;
			// comparison of ints/doubles
			if ((t1 instanceof TypeInt || t1 instanceof TypeDouble) && (t2 instanceof TypeInt || t2 instanceof TypeDouble)) {
				ok = true;
			}
			// equality of clocks against clocks/integers
			// (and int/int - but this is already covered above)
			else if ((t1 instanceof TypeInt || t1 instanceof TypeClock) && (t2 instanceof TypeInt || t2 instanceof TypeClock)) {
				ok = true;
			}
			if (!ok) {
				if (t1.equals(t2))
					throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " cannot compare " + t1 + "s", e);
				else
					throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " cannot compare " + t1 + " and " + t2, e);
			}
			e.setType(TypeBool.getInstance());
			break;
		case ExpressionBinaryOp.PLUS:
		case ExpressionBinaryOp.MINUS:
		case ExpressionBinaryOp.TIMES:
			if (!(t1 instanceof TypeInt || t1 instanceof TypeDouble)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " can only be applied to ints or doubles", e.getOperand1());
			}
			if (!(t2 instanceof TypeInt || t2 instanceof TypeDouble)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " can only be applied to ints or doubles", e.getOperand2());
			}
			e.setType(t1 instanceof TypeDouble || t2 instanceof TypeDouble ? TypeDouble.getInstance() : TypeInt.getInstance());
			break;
		case ExpressionBinaryOp.DIVIDE:
			if (!(t1 instanceof TypeInt || t1 instanceof TypeDouble)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " can only be applied to ints or doubles", e.getOperand1());
			}
			if (!(t2 instanceof TypeInt || t2 instanceof TypeDouble)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " can only be applied to ints or doubles", e.getOperand2());
			}
			e.setType(TypeDouble.getInstance());
			break;
		}
	}

	public void visitPost(ExpressionUnaryOp e) throws PrismLangException
	{
		Type t = e.getOperand().getType();

		switch (e.getOperator()) {
		case ExpressionUnaryOp.NOT:
			if (!(t instanceof TypeBool) && !(t instanceof TypePathBool)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " applied to non-Boolean expression", e.getOperand());
			}
			e.setType(t);
			break;
		case ExpressionUnaryOp.MINUS:
			if (!(t instanceof TypeInt || t instanceof TypeDouble)) {
				throw new PrismLangException("Type error: " + e.getOperatorSymbol() + " can only be applied to ints or doubles", e.getOperand());
			}
			e.setType(t);
			break;
		case ExpressionUnaryOp.PARENTH:
			e.setType(t);
			break;
		}
	}

	public void visitPost(ExpressionArrayAccess e) throws PrismLangException
	{
		Type tArray = e.getArray().getType();
		Type tIndex = e.getIndex().getType();
		if (!(tArray instanceof TypeArray)) {
			throw new PrismLangException("Type error: " + e.getArray() + " is not an array", e);
		}
		if (!(tIndex instanceof TypeInt)) {
			throw new PrismLangException("Type error: array index " + e.getIndex() + " is not an integer", e.getIndex());
		}
		e.setType(((TypeArray) tArray).getSubType());
	}

	public void visitPost(ExpressionStructAccess e) throws PrismLangException
	{
		Type tStruct = e.getStruct().getType();
		if (!(tStruct instanceof TypeStruct)) {
			throw new PrismLangException("Type error: " + e.getStruct() + " is not a struct", e);
		}
		int fieldIndex = ((TypeStruct) tStruct).getFieldIndex(e.getFieldName());
		if (fieldIndex == -1) {
			throw new PrismLangException("Type error: struct has no field " + e.getFieldName(), e);
		}
		e.setType(((TypeStruct) tStruct).getFieldType(fieldIndex));
	}

	public void visitPost(ExpressionFunc e) throws PrismLangException
	{
		int i, n;
		Type types[];

		// Get types of operands
		n = e.getNumOperands();
		types = new Type[n];
		for (i = 0; i < n; i++) {
			types[i] = e.getOperand(i).getType();
		}

		// Check types of operands are ok
		switch (e.getNameCode()) {
		case ExpressionFunc.MIN:
		case ExpressionFunc.MAX:
		case ExpressionFunc.FLOOR:
		case ExpressionFunc.CEIL:
		case ExpressionFunc.ROUND:
		case ExpressionFunc.POW:
		case ExpressionFunc.LOG:
			// All operands must be ints or doubles
			for (i = 0; i < n; i++) {
				if (types[i] instanceof TypeBool) {
					throw new PrismLangException("Type error: Boolean argument not allowed as argument to function \"" + e.getName() + "\"", e.getOperand(i));
				}
			}
			break;
		case ExpressionFunc.MOD:
			// All operands must be ints
			for (i = 0; i < n; i++) {
				if (!(types[i] instanceof TypeInt)) {
					throw new PrismLangException("Type error: non-integer argument to  function \"" + e.getName() + "\"", e.getOperand(i));
				}
			}
			break;
		case ExpressionFunc.MULTI:
			// All operands must be booleans or doubles, and doubles must come first.
			boolean seenBoolean = false;
			for (i = 0; i < n; i++) {
				if (!(types[i] instanceof TypeBool || types[i] instanceof TypeDouble)) {
					throw new PrismLangException("Type error: non-Boolean/Double argument to  function \"" + e.getName()
							+ "\"", e.getOperand(i));
				}
				if (seenBoolean && types[i] instanceof TypeDouble) {
					throw new PrismLangException("Type error: in the function \"" + e.getName() + "\", any Double arguments must come before any Boolean arguments.");
				}
				if (types[i] instanceof TypeBool) {
					seenBoolean = true;
				}
			}
			break;
		default:
			throw new PrismLangException("Cannot type check unknown function", e);
		}

		// Determine type of this function
		switch (e.getNameCode()) {
		case ExpressionFunc.MIN:
		case ExpressionFunc.MAX:
			// int if all ints, double otherwise
			e.setType(TypeInt.getInstance());
			for (i = 0; i < n; i++) {
				if (types[i] instanceof TypeDouble) {
					e.setType(TypeDouble.getInstance());
					break;
				}
			}
			break;
		case ExpressionFunc.FLOOR:
		case ExpressionFunc.CEIL:
		case ExpressionFunc.ROUND:
		case ExpressionFunc.MOD:
			// Resulting type is always int
			e.setType(TypeInt.getInstance());
			break;
		case ExpressionFunc.POW:
			// int if both ints, double otherwise
			e.setType(types[0] instanceof TypeDouble || types[1] instanceof TypeDouble ? TypeDouble.getInstance() : TypeInt.getInstance());
			break;
		case ExpressionFunc.LOG:
			// Resulting type is always double
			e.setType(TypeDouble.getInstance());
			break;
		case ExpressionFunc.MULTI:
			// Resulting type is always same as first arg
			if (types[0] instanceof TypeBool)
				e.setType(TypeBool.getInstance());
			else if (types.length == 1 || types[1] instanceof TypeBool) //in this case type[0] is TypeDouble
				e.setType(TypeDouble.getInstance());
			else
				e.setType(TypeVoid.getInstance());
			break;

		}
	}

	public void visitPost(ExpressionStruct e) throws PrismLangException
	{
		// Field types can be anything but are used to construct the type of this expression
		// (note that field names are ommitted since not provided)
		int n = e.getNumFields();
		TypeStruct tStruct = new TypeStruct();
		for (int i = 0; i < n; i++) {
			tStruct.addField(null, e.getField(i).getType());
		}
		e.setType(tStruct);
	}

	public void visitPost(ExpressionIdent e) throws PrismLangException
	{
		// Should never happpen
		throw new PrismLangException("Cannot determine type of unknown identifier", e);
	}

	public void visitPost(ExpressionLiteral e) throws PrismLangException
	{
		// Type already known
	}

	public void visitPost(ExpressionConstant e) throws PrismLangException
	{
		// Type already known
	}

	public void visitPost(ExpressionFormula e) throws PrismLangException
	{
		// This should have been defined or expanded by now.
		// If so, just set type to that of the definition; otherwise error
		if (e.getDefinition() != null)
			e.setType(e.getDefinition().getType());
		else
			throw new PrismLangException("Cannot determine type of formula", e);
	}

	public void visitPost(ExpressionVar e) throws PrismLangException
	{
		// Type already known
	}

	public void visitPost(ExpressionInterval e) throws PrismLangException
	{
		Type t1 = e.getOperand1().getType();
		Type t2 = e.getOperand2().getType();
		// Only intervals of intervals or doubles allowed
		if (!(t1 instanceof TypeInt || t1 instanceof TypeDouble)) {
			throw new PrismLangException("Type error: intervals can only of ints or doubles", e.getOperand1());
		}
		if (!(t2 instanceof TypeInt || t2 instanceof TypeDouble)) {
			throw new PrismLangException("Type error: intervals can only of ints or doubles", e.getOperand1());
		}
		// Interval of int only if both ints
		Type subType = t1 instanceof TypeDouble || t2 instanceof TypeDouble ? TypeDouble.getInstance() : TypeInt.getInstance();
		e.setType(TypeInterval.getInstance(subType));
	}

	public void visitPost(ExpressionArray e) throws PrismLangException
	{
		// Find a type that all array element can be cast to
		// (e.g. [2,2.0] would be array of double)
		Type subType = null;
		int numElements = e.getNumElements();
		for (int i = 0; i < numElements; i++) {
			Type elemType = e.getElement(i).getType();
			if (subType == null) {
				subType = elemType;
			} else if (subType.canCastTypeTo(elemType)) {
				// No change
			} else if (elemType.canCastTypeTo(subType)) {
				subType = elemType;
			} else {
				throw new PrismLangException("Type error: array elements must have matching types", e.getElement(i));
			}
		}
		e.setType(TypeArray.getInstance(subType));
	}

	public void visitPost(ExpressionProb e) throws PrismLangException
	{
		// Check prob bound
		if (e.getProb() != null && !TypeDouble.getInstance().canCastTypeTo(e.getProb().getType())) {
			throw new PrismLangException("Type error: P operator probability bound is not a double", e.getProb());
		}
		// Check filter
		if (e.getFilter() != null && !(e.getFilter().getExpression().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: P operator filter is not a Boolean", e.getFilter().getExpression());
		}
		// Need to to do this type check here because some info has been lost when converted to ExpressionFilter
		if (e.getProb() != null && e.getFilter() != null) {
			if (e.getFilter().minRequested() || e.getFilter().maxRequested()) {
				throw new PrismLangException("Type error: Cannot use min/max filters in Boolean-valued properties");
			}
		}
		// Check path operator
		// Note: need to allow (non-path) Boolean types too, since propositional formulae are also LTL
		if (!(e.getExpression().getType() instanceof TypePathBool || e.getExpression().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: Contents of P operator is not a path formula", e.getExpression());
		}
		// Set type
		e.setType(e.getProb() == null ? TypeDouble.getInstance() : TypeBool.getInstance());
	}

	public void visitPost(ExpressionReward e) throws PrismLangException
	{
		// Check reward struct ref(s)
		if (e.getRewardStructIndex() != null && e.getRewardStructIndex() instanceof Expression) {
			Expression rsi = (Expression) e.getRewardStructIndex();
			if (!(rsi.getType() instanceof TypeInt)) {
				throw new PrismLangException("Type error: Reward structure index must be string or integer", rsi);
			}
		}
		if (e.getRewardStructIndexDiv() != null && e.getRewardStructIndexDiv() instanceof Expression) {
			Expression rsi = (Expression) e.getRewardStructIndexDiv();
			if (!(rsi.getType() instanceof TypeInt)) {
				throw new PrismLangException("Type error: Reward structure index must be string or integer", rsi);
			}
		}
		// Check reward bound
		if (e.getReward() != null && !TypeDouble.getInstance().canCastTypeTo(e.getReward().getType())) {
			throw new PrismLangException("Type error: R operator reward bound is not a double", e.getReward());
		}
		// Check filter
		if (e.getFilter() != null && !(e.getFilter().getExpression().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: R operator filter is not a Boolean", e.getFilter().getExpression());
		}
		// Need to to do this type check here because some info has been lost when converted to ExpressionFilter
		if (e.getReward() != null && e.getFilter() != null) {
			if (e.getFilter().minRequested() || e.getFilter().maxRequested()) {
				throw new PrismLangException("Type error: Cannot use min/max filters in Boolean-valued properties");
			}
		}
		// Check argument
		Type typeArg = e.getExpression().getType();
		if (!(typeArg instanceof TypePathDouble || typeArg instanceof TypePathBool || typeArg instanceof TypeBool)) {
			throw new PrismLangException("Type error: Contents of R operator is invalid", e.getExpression());
		}
		// Set type
		e.setType(e.getReward() == null ? TypeDouble.getInstance() : TypeBool.getInstance());
	}

	public void visitPost(ExpressionSS e) throws PrismLangException
	{
		// Check probability bound
		if (e.getProb() != null && !TypeDouble.getInstance().canCastTypeTo(e.getProb().getType())) {
			throw new PrismLangException("Type error: S operator probability bound is not a double", e.getProb());
		}
		// Check filter
		if (e.getFilter() != null && !(e.getFilter().getExpression().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: S operator filter is not a Boolean", e.getFilter().getExpression());
		}
		// Need to to do this type check here because some info has been lost when converted to ExpressionFilter
		if (e.getProb() != null && e.getFilter() != null) {
			if (e.getFilter().minRequested() || e.getFilter().maxRequested()) {
				throw new PrismLangException("Type error: Cannot use min/max filters in Boolean-valued properties");
			}
		}
		// Check argument
		if (!(e.getExpression().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: Contents of S operator is not a Boolean-valued expression", e.getExpression());
		}
		// Set type
		e.setType(e.getProb() == null ? TypeDouble.getInstance() : TypeBool.getInstance());
	}

	public void visitPost(ExpressionExists e) throws PrismLangException
	{
		e.setType(TypeBool.getInstance());
	}

	public void visitPost(ExpressionForAll e) throws PrismLangException
	{
		e.setType(TypeBool.getInstance());
	}

	public void visitPost(ExpressionStrategy e) throws PrismLangException
	{
		// Get types of operands
		int n = e.getNumOperands();
		Type types[] = new Type[n];
		for (int i = 0; i < n; i++) {
			types[i] = e.getOperand(i).getType();
		}

		// Currently, resulting type is always same as first arg
		if (types[0] instanceof TypeBool)
			e.setType(TypeBool.getInstance());
		else if (types.length == 1 || types[1] instanceof TypeBool) //in this case type[0] is TypeDouble
			e.setType(TypeDouble.getInstance());
		else
			e.setType(TypeVoid.getInstance());
	}

	public void visitPost(ExpressionLabel e) throws PrismLangException
	{
		e.setType(TypeBool.getInstance());
	}

	public void visitPost(ExpressionObs e) throws PrismLangException
	{
		// Type should be already known
		if (e.getTypeIfDefined() == null) {
			throw new PrismLangException("Cannot determine type of observable", e);
		}
	}

	public void visitPost(ExpressionProp e) throws PrismLangException
	{
		// Recursively type check referenced property
		// (may not have been done yet, e.g. because that property appears later than the current one in a PropertiesFile)
		// (NB: recursive check not triggered in visit() method because PropertiesFile not available there)
		// However, if a PropertiesFile is not available *and* we have a type stored already, don't recompute
		// (in case typeCheck() is called by getType() later without passing a PropertieFile)
		if (propertiesFile == null) {
			if (e.getTypeIfDefined() == null)
				throw new PrismLangException("No properties file to look up type of property reference " + e);
		} else {
			Property prop = propertiesFile.lookUpPropertyObjectByName(e.getName());
			if (prop == null)
				throw new PrismLangException("Could not look up type of property reference " + e);
			// Notice we explicitly start a type check using the current TypeCheck class, rather than let
			// it be triggered by the subsequent call to getType() becase need to keep ref to PropertiesFile
			prop.accept(this);
			e.setType(prop.getType());
		}
	}

	public void visitPost(ExpressionFilter e) throws PrismLangException
	{
		// Get type of operand
		Type t = e.getOperand().getType();

		// Check filter is ok
		if (e.getFilter() != null && !(e.getFilter().getType() instanceof TypeBool)) {
			throw new PrismLangException("Type error: Second argument of filter is not a Boolean", e.getFilter());
		}

		// Check type of operands is ok
		switch (e.getOperatorType()) {
		case MIN:
		case MAX:
		case ARGMIN:
		case ARGMAX:
		case SUM:
		case AVG:
		case RANGE:
			if (t instanceof TypeBool) {
				throw new PrismLangException("Type error: Boolean argument not allowed as operand for filter of type \"" + e.getOperatorName() + "\"",
						e.getOperand());
			}
			if (t instanceof TypeVoid) {
				// e.g., complex results from multi-objective checking
				throw new PrismLangException("Type error: Void/complex arguments not allowed as operand for filter of type \"" + e.getOperatorName() + "\"",
						e.getOperand());
			}
			break;
		case COUNT:
		case FORALL:
		case EXISTS:
			if (!(t instanceof TypeBool)) {
				throw new PrismLangException("Type error: Operand for filter of type \"" + e.getOperatorName() + "\" must be Boolean", e.getOperand());
			}
			break;
		case FIRST:
		case PRINT:
		case PRINTALL:
		case STORE:
			if (t instanceof TypeVoid) {
				throw new PrismLangException("Type error: Void/complex arguments not allowed as operand for filter of type \"" + e.getOperatorName() + "\"",
						e.getOperand());
			}
			break;
		case STATE:
			// Anything goes, has special handling for TypeVoid (e.g., some multi-objective results)
			break;
		default:
			throw new PrismLangException("Cannot type check filter of unknown type", e);
		}

		// Determine type of this filter
		switch (e.getOperatorType()) {
		case MIN:
		case MAX:
		case SUM:
		case FIRST:
		case PRINT:
		case PRINTALL:
		case STORE:
		case STATE:
			e.setType(t);
			break;
		case RANGE:
			e.setType(TypeInterval.getInstance(t));
			break;
		case COUNT:
			e.setType(TypeInt.getInstance());
			break;
		case AVG:
			e.setType(TypeDouble.getInstance());
			break;
		case ARGMIN:
		case ARGMAX:
		case FORALL:
		case EXISTS:
			e.setType(TypeBool.getInstance());
			break;
		}
	}
}
