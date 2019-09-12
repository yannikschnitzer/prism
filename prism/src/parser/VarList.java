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

package parser;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import parser.EvaluateContext.EvalMode;
import parser.ast.ASTElement;
import parser.ast.Declaration;
import parser.ast.DeclarationArray;
import parser.ast.DeclarationBool;
import parser.ast.DeclarationClock;
import parser.ast.DeclarationDoubleUnbounded;
import parser.ast.DeclarationEnum;
import parser.ast.DeclarationInt;
import parser.ast.DeclarationIntUnbounded;
import parser.ast.DeclarationStruct;
import parser.ast.DeclarationType;
import parser.ast.Expression;
import parser.ast.ExpressionArrayAccess;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionStructAccess;
import parser.ast.ExpressionVar;
import parser.type.Type;
import parser.type.TypeArray;
import parser.type.TypeBool;
import parser.type.TypeClock;
import parser.type.TypeEnum;
import parser.type.TypeInt;
import parser.type.TypeStruct;
import parser.visitor.ASTTraverse;
import prism.ModelInfo;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismUtils;

/**
 * Class to store information about the set of variables in a model.
 * Assumes that any constants in the model have been given fixed values.
 * Thus, ranges for all variables, array lengths, etc. are known.
 *
 * Primarily for symbolic model checking (i.e. (MT)BDD representations),
 * VarList also takes care of how each variable will be encoded to an integer
 * and how (sub)variables map to a flattened list of primitive variables.
 */
public class VarList
{
	// List of (top-level) variables
	private List<Var> vars;

	// Mapping from (top-level) variable names to index in "vars" list
	private Map<String, Integer> nameMap;

	// List of all (primitive) variables, expanding arrays etc.
	private List<VarPrimitive> varsPrimitive;

	// Total number of bits needed to encode variables
	private int totalNumBits;
	
	// Evaluation context (constant values + evaluation mode)
	private EvaluateContext ec;

	/**
	 * Construct empty variable list.
	 */
	public VarList()
	{
		initialise();
	}

	/**
	* Construct variable list for a ModelInfo object.
	 * @param modelInfo The model info
	*/
	public VarList(ModelInfo modelInfo) throws PrismException
	{
		initialise(modelInfo.getEvaluateContext());
		int numVars = modelInfo.getNumVars();
		for (int i = 0; i < numVars; i++) {
			DeclarationType declType = modelInfo.getVarDeclarationType(i);
			int module = modelInfo.getVarModuleIndex(i);
			addVar(modelInfo.getVarName(i), declType, module);
		}
	}

	/**
	 * Set up data structures
	 */
	private void initialise()
	{
		initialise(EvaluateContext.create());
	}

	/**
	 * Set up data structures
	 */
	private void initialise(EvaluateContext ec)
	{
		setEvaluateContext(ec);
		vars = new ArrayList<Var>();
		nameMap = new HashMap<String, Integer>();
		varsPrimitive = new ArrayList<VarPrimitive>();
		totalNumBits = 0;
	}

	/**
	 * Set the evaluation context used to evaluate any expressions,
	 * e.g. for variable range bounds. This supplies constant values
	 * and determines the evaluation mode used.
	 */
	public void setEvaluateContext(EvaluateContext ec)
	{
		this.ec = ec;
	}

	/**
	 * Add a new variable to the end of the VarList.
	 * @param decl Declaration defining the variable
	 * @param module Index of module containing variable
	 */
	public void addVar(Declaration decl, int module) throws PrismLangException
	{
		addVar(decl.getName(), decl.getDeclType(), module);
	}

	/**
	 * Add a new variable to the start of the VarList.
	 * Warning: this will result in some data being recreated in the list.
	 * @param decl Declaration defining the variable
	 * @param module Index of module containing variable
	 */
	public void addVarAtStart(Declaration decl, int module) throws PrismLangException
	{
		// Just recreate the data - easier and safer
		int numVarsOld = vars.size();
		List<Var> varsOld = vars;
		initialise(this.ec);
		addVar(decl.getName(), decl.getDeclType(), module);
		for (int j = 0; j < numVarsOld; j++) {
			Var varOld = varsOld.get(j);
			addVar(varOld.name, varOld.declType, varOld.module);
		}
	}

	/**
	 * Add a new variable at position i in the VarList.
	 * Index i refers to the indexing of primitive variables,
	 * for backwards compatibility, which is a bit odd. Deprecated.
	 * @param decl Declaration defining the variable
	 * @param module Index of module containing variable
	 * @deprecated If possible, use {@link #addVar(Declaration, int)} or {@link #addVarAtStart(Declaration, int)}
	 */
	public void addVar(int i, Declaration decl, int module) throws PrismLangException
	{
		if (i == 0) {
			addVarAtStart(decl, module);
		} else if (i == varsPrimitive.size()) {
			addVar(decl, module);
		} else {
			throw new PrismLangException("Cannot add variable to position " + i + " of VarList");
		}
	}

	/**
	 * Add a new variable to the end of the VarList.
	 * @param name Variable name
	 * @param declType Type declaration defining the variable
	 * @param module Index of module containing variable
	 */
	public void addVar(String name, DeclarationType declType, int module) throws PrismLangException
	{
		// Note: this is the "main" addVar method: all others call it
		// Update name map first - needed for var indexing during creation
		nameMap.put(name, vars.size());
		Var var = createVar(name, declType, module);
		vars.add(var);
		totalNumBits += getPrimitiveRangeLogTwo(vars.size() - 1);
	}

	/**
	 * Create and return a new variable object to store in the list.
	 * Variable objects for all primitive variables are also created
	 * and stored in the varsPrimitive list along the way.
	 * @param name Variable name
	 * @param declType Type declaration defining the variable
	 * @param module Index of module containing variable
	 */
	private Var createVar(String name, DeclarationType declType, int module) throws PrismLangException
	{
		return createVarRec(name, declType.getType(), declType, module, "", null);
	}

	/**
	 * Recursive helper function for {@link #createVar(String, DeclarationType, int)}.
	 * @param nameTop Name of the (top-level) variable
	 * @param typeTop Type of the (top-level) variable
	 * @param declType Type declaration defining the variable
	 * @param module Index of module containing variable
	 * @param nameSuffix Suffix to be added to the name of new variables created
	 * @param varAccess Info about array (etc.) accesses that will define the variable references for primitives
	 */
	private Var createVarRec(String nameTop, Type typeTop, DeclarationType declType, int module, String nameSuffix, VarAccess varAccess) throws PrismLangException
	{
		int startIndex = varsPrimitive.size();
		Var var = null;
		
		// Primitive variables
		if (declType.getType().isPrimitive()) {

			// Variable is a bounded integer
			if (declType instanceof DeclarationInt) {

				DeclarationInt intdecl = (DeclarationInt) declType;
				int low = intdecl.getLow().evaluateInt(ec);
				int high = intdecl.getHigh().evaluateInt(ec);
				// Check range is valid
				if (high - low <= 0) {
					String s = "Invalid range (" + low + "-" + high + ") for variable \"" + nameTop + "\"";
					throw new PrismLangException(s, declType);
				}
				if ((long) high - (long) low >= Integer.MAX_VALUE) {
					String s = "Range for variable \"" + nameTop + "\" (" + low + "-" + high + ") is too big";
					throw new PrismLangException(s, declType);
				}
				var = new VarPrimitive(nameTop + nameSuffix, declType.getType(), low, high);
			}

			// Variable is a Boolean
			else if (declType instanceof DeclarationBool) {
				var = new VarPrimitive(nameTop + nameSuffix, declType.getType(), 0, 1);
			}

			// Variable is an enum
			else if (declType instanceof DeclarationEnum) {
				int size = ((DeclarationEnum) declType).getNumConstants();
				var = new VarPrimitive(nameTop + nameSuffix, declType.getType(), 0, size - 1);
			}

			// Variable is a clock
			else if (declType instanceof DeclarationClock) {
				// Just use dummy info
				var = new VarPrimitive(nameTop + nameSuffix, declType.getType(), 0, 1);
			}

			// Variable is an (unbounded) integer
			else if (declType instanceof DeclarationIntUnbounded) {
				// Just use dummy range info
				var = new VarPrimitive(nameTop + nameSuffix, declType.getType(), 0, 1);
			}

			// Variable is an (unbounded) double (only used for constants)
			else if (declType instanceof DeclarationDoubleUnbounded) {
				// Just use dummy range info
				var = new VarPrimitive(nameTop + nameSuffix, declType.getType(), 0, 1);
			}

			else {
				throw new PrismLangException("Unknown primitive variable type \"" + declType + "\" in declaration", declType);
			}
			// Add to list of all primitives
			varsPrimitive.add((VarPrimitive) var);
		}
		// Variable is an array
		else if (declType instanceof DeclarationArray) {
			int length = ((DeclarationArray) declType).getLength().evaluateInt(ec);
			if (length < 0) {
				String s = "Invalid size (" + length + ") for array \"" + nameTop + "\"";
				throw new PrismLangException(s, declType);
			}
			VarArray varArray = new VarArray(nameTop + nameSuffix, declType.getType());
			for (int i = 0; i < length; i++) {
				Var varElement = createVarRec(nameTop, typeTop, ((DeclarationArray) declType).getSubtype(), module, nameSuffix + "[" + i + "]", new VarAccessArray(varAccess, i));
				varArray.elements.add(varElement);
			}
			varArray.elementSize = varArray.elements.get(0).numPrimitives;
			var = varArray;
		}
		// Variable is a struct
		else if (declType instanceof DeclarationStruct) {
			DeclarationStruct declStruct = (DeclarationStruct) declType;
			int numFields = declStruct.getNumFields();
			VarStruct varStruct = new VarStruct(nameTop + nameSuffix, declType.getType());
//			if (!(exprInit instanceof ExpressionStruct)) {
//				throw new PrismLangException("Struct variable can only be initialised to a struct", decl);
//			}
			int fieldOffset = 0;
			for (int i = 0; i < numFields; i++) {
				String fieldName = declStruct.getFieldName(i);
				Var varField = createVarRec(nameTop, typeTop, declStruct.getFieldType(i), module, nameSuffix + "." + fieldName, new VarAccessStruct(varAccess, i, fieldName));
				varStruct.fields.add(varField);
				varStruct.fieldNames.add(fieldName);
				varStruct.fieldOffsets.add(fieldOffset);
				fieldOffset += varField.numPrimitives;
			}
			var = varStruct;
		}
		// Unknown variable type
		else {
			throw new PrismLangException("Unknown variable type \"" + declType + "\" in declaration", declType);
		}

		// Attach variable reference, and do indexing on it
		var.ref = new ExpressionVar(nameTop, typeTop);
		if (varAccess != null) {
			var.ref = varAccess.createVarRef((ExpressionVar) var.ref);
		}
		addVarIndexing(var.ref);
		// Store declaration/module
		var.declType = declType;
		var.module = module;
		// Store indexing info
		var.startIndex = startIndex;
		var.numPrimitives = varsPrimitive.size() - var.startIndex;

		return var;
	}

	/**
	 * Get the Var object created locally to represent a (sub)variable,
	 * as specified by a variable reference {@code varRef} in the form of an Expression.
	 * The specified evaluation context {@code ec} is used to evaluate any array indices.
	 */
	public Var getVarForVarRef(Expression varRef, EvaluateContext ec) throws PrismLangException
	{
		if (varRef instanceof ExpressionVar) {
			return vars.get(((ExpressionVar) varRef).getVarIndex());
		} else if (varRef instanceof ExpressionArrayAccess) {
			VarArray varArray = (VarArray) getVarForVarRef(((ExpressionArrayAccess) varRef).getArray(), ec);
			int evalIndex = ((ExpressionArrayAccess) varRef).evaluateIndex(ec, varArray.elements.size());
			return varArray.elements.get(evalIndex);
		} else if (varRef instanceof ExpressionStructAccess) {
			int fieldIndex = ((ExpressionStructAccess) varRef).getFieldIndex();
			VarStruct varStruct = (VarStruct) getVarForVarRef(((ExpressionStructAccess) varRef).getStruct(), ec);
			return varStruct.fields.get(fieldIndex);
		} else {
			throw new PrismLangException("Invalid variable reference", varRef);
		}
	}

	/**
	 * Get one of the Var objects created locally to represent a (sub)variable,
	 * as specified by a variable reference {@code varRef} in the form of an Expression,
	 * and where array indices could potentially take any value.
	 */
	public Var getSomeVarForVarRef(Expression varRef) throws PrismLangException
	{
		if (varRef instanceof ExpressionVar) {
			return vars.get(((ExpressionVar) varRef).getVarIndex());
		} else if (varRef instanceof ExpressionArrayAccess) {
			VarArray varArray = (VarArray) getSomeVarForVarRef(((ExpressionArrayAccess) varRef).getArray());
			// Just use the first element (they are all the same type)
			return varArray.elements.get(0);
		} else if (varRef instanceof ExpressionStructAccess) {
			int fieldIndex = ((ExpressionStructAccess) varRef).getFieldIndex();
			VarStruct varStruct = (VarStruct) getSomeVarForVarRef(((ExpressionStructAccess) varRef).getStruct());
			return varStruct.fields.get(fieldIndex);
		} else {
			throw new PrismLangException("Invalid variable reference", varRef);
		}
	}

	public List<Var> getSomeVarPathForVarRef(Expression varRef) throws PrismLangException
	{
		List<Var> varPath = new ArrayList<>();
		getSomeVarPathForVarRefRec(varRef, varPath);
		return varPath;
	}

	public Var getSomeVarPathForVarRefRec(Expression varRef, List<Var> varPath) throws PrismLangException
	{
		if (varRef instanceof ExpressionVar) {
			Var var = vars.get(((ExpressionVar) varRef).getVarIndex());
			varPath.add(var);
			return var;
		} else if (varRef instanceof ExpressionArrayAccess) {

			VarArray varArray = (VarArray) getSomeVarPathForVarRefRec(((ExpressionArrayAccess) varRef).getArray(), varPath);
			varPath.add(varArray.elements.get(0));
			// Just use the first element (they are all the same type)
			return varArray.elements.get(0);
		} else if (varRef instanceof ExpressionStructAccess) {
			int fieldIndex = ((ExpressionStructAccess) varRef).getFieldIndex();
			VarStruct varStruct = (VarStruct) getSomeVarPathForVarRefRec(((ExpressionStructAccess) varRef).getStruct(), varPath);
			varPath.add(varStruct.fields.get(fieldIndex));
			return varStruct.fields.get(fieldIndex);
		} else {
			throw new PrismLangException("Invalid variable reference", varRef);
		}
	}

	/**
	 * Get the Var object created locally to represent the ith (top-level) variable.
	 */
	public Var getVar(int i) throws PrismLangException
	{
		return vars.get(i);
	}

	// Accessors for (normal, top-level) variable info

	/**
	 * Get the number of (top-level) variables stored in this list.
	 * E.g. this returns 3 for variable list { a, b[2], c[2][2] }
	 */
	public int getNumVars()
	{
		return vars.size();
	}

	/**
	 * Get the name of the ith (top-level) variable in this list.
	 */
	public String getName(int i)
	{
		return vars.get(i).name;
	}

	/**
	 * Get an Expression representing a reference to the ith (top-level) variable in this list.
	 */
	public Expression getRef(int i)
	{
		return vars.get(i).ref;
	}

	/**
	 * Get the type of the ith (top-level) variable in this list.
	 */
	public Type getType(int i)
	{
		return vars.get(i).type;
	}

	/**
	 * Look up the index of a (top-level) variable, as stored in this list, by name.
	 * Returns -1 if there is no such variable. 
	 */
	public int getIndex(String name)
	{
		Integer i = nameMap.get(name);
		return (i == null) ? -1 : i;
	}

	/**
	 * Check if there is a (top-level) variable of a given name in this list.
	 */
	public boolean exists(String name)
	{
		return getIndex(name) != -1;
	}

	// Accessors for lower-level variable info: primitives, encoding

	/**
	 * Get the number of (primitive) variables stored in this list.
	 * E.g. this returns 1+2+4=7 for variable list { a, b[2], c[2][2] }
	 */
	public int getNumPrimitiveVars()
	{
		return varsPrimitive.size();
	}

	/**
	 * Get the index of the first primitive variable
	 * corresponding to the ith (top-level) variable in this list.
	 */
	public int getIndexOfFirstPrimitive(int i)
	{
		return vars.get(i).startIndex;
	}

	/**
	 * Get the number of primitive variables
	 * corresponding to the ith (top-level) variable in this list.
	 */
	public int getNumberOfPrimitives(int i)
	{
		return vars.get(i).numPrimitives;
	}

	/**
	 * Get the number of primitive variables
	 * corresponding to a (sub)variable, specified as a variable reference.
	 * Array index values are ignored since all elements have the same type,
	 * e.g. the variable reference can be x[i] without specifying a value for i.
	 */
	public int getNumberOfPrimitives(Expression varRef) throws PrismLangException
	{
		return getSomeVarForVarRef(varRef).numPrimitives;
	}

	/**
	 * Get the name of the ith (primitive) variable in this list.
	 */
	public String getPrimitiveName(int i)
	{
		return varsPrimitive.get(i).name;
	}

	/**
	 * Get the type of the ith (primitive) variable in this list.
	 */
	public Type getPrimitiveType(int i)
	{
		return varsPrimitive.get(i).type;
	}

	/**
	 * Get the declaration type of the ith (primitive) variable in this list.
	 */
	public DeclarationType getPrimitiveDeclarationType(int i)
	{
		return vars.get(i).declType;
	}

	/**
	 * Get the index of the module of the ith (primitive) variable in this list (-1 denotes global variable).
	 */
	public int getPrimitiveModule(int i)
	{
		return varsPrimitive.get(i).module;
	}

	/**
	 * Get the low value of the ith (primitive) variable in this list (when encoded as an integer).
	 */
	public int getPrimitiveLow(int i)
	{
		return varsPrimitive.get(i).low;
	}

	/**
	 * Get the high value of the ith (primitive) variable in this list (when encoded as an integer).
	 */
	public int getPrimitiveHigh(int i)
	{
		return varsPrimitive.get(i).high;
	}

	/**
	 * Get the range of the ith (primitive) variable in this list (when encoded as an integer).
	 */
	public int getPrimitiveRange(int i)
	{
		return getPrimitiveHigh(i) - getPrimitiveLow(i) + 1;
	}

	/**
	 * Get the number of bits required to store the ith (primitive) variable in this list (when encoded as an integer).
	 */
	public int getPrimitiveRangeLogTwo(int i)
	{
		return (int) Math.ceil(PrismUtils.log2(getPrimitiveRange(i)));
	}

	/**
	 * Get the total number of bits required to store all (primitive) variables in this list (when encoded as integers).
	 */
	public int getTotalNumBits()
	{
		return totalNumBits;
	}

	/**
	 * Get variable references (i.e., Expression objects referring to a variable or subvariable)
	 * for all primitive variables whose type satisfies a predicate, and return them in a list.
	 * Since these are primitive, variable references are all constant/literal.
	 */
	public List<Expression> getAllPrimitiveVarRefs(Predicate<Type> pred)
	{
		List<Expression> list = new ArrayList<>();
		for (Var var : varsPrimitive) {
			if (pred.test(var.type) && !list.contains(var.ref)) {
				list.add(var.ref);
			}
		}
		return list;
	}

	/**
	 * Get variable references (i.e., Expression objects referring to a variable or subvariable)
	 * for all primitive variables of a module whose type satisfies a predicate, and return them in a list.
	 * Since these are primitive, variable references are all constant/literal.
	 */
	public List<Expression> getAllPrimitiveVarRefs(int module, Predicate<Type> pred)
	{
		List<Expression> list = new ArrayList<>();
		for (Var var : varsPrimitive) {
			if (var.module == module && pred.test(var.type) && !list.contains(var.ref)) {
				list.add(var.ref);
			}
		}
		return list;
	}

	/**
	 * Get variable references (i.e., Expression objects referring to a variable or subvariable)
	 * for all primitive variables whose type satisfies a predicate, and return them in a list.
	 * Since these are primitive, variable references are all constant/literal.
	 */
	public List<Expression> getAllClockVarRefs()
	{
		return getAllPrimitiveVarRefs(t -> (t instanceof TypeClock));
	}

	/**
	 * Get variable references (i.e., Expression objects referring to a variable or subvariable)
	 * for all primitive variables of a module whose type satisfies a predicate, and return them in a list.
	 * Since these are primitive, variable references are all constant/literal.
	 */
	public List<Expression> getAllClockVarRefs(int module)
	{
		return getAllPrimitiveVarRefs(module, t -> (t instanceof TypeClock));
	}

	// Utility methods for encoding/decoding values to/from integers

	/**
	 * Convert a value for the ith (top-level) variable in this list
	 * to an array of values encoding its corresponding primitive variables as integers.
	 */
	public int[] encodeVarValueToInts(int i, Object val) throws PrismLangException
	{
		int startIndex = vars.get(i).startIndex;
		int numPrimitiveVars = vars.get(i).numPrimitives;
		int enc[] = new int[numPrimitiveVars];
		encodeVarValueToIntsRec(vars.get(i), startIndex, val, enc);
		return enc;
	}

	/**
	 * Recursive helper function to implement {@link #encodeVarValueToInts(int, Object)}.
	 */
	private void encodeVarValueToIntsRec(Var var, int startIndex, Object val, int enc[]) throws PrismLangException
	{
		if (var instanceof VarPrimitive) {
			enc[var.startIndex - startIndex] = encodeVarValueToInt(var.startIndex, val);
		} else {
			if (var instanceof VarArray) {
				VarArray varArray = (VarArray) var;
				for (Var varChild : varArray.elements) {
					encodeVarValueToIntsRec(varChild, startIndex, val, enc);
				}
			} else if (var instanceof VarStruct) {
				VarStruct varStruct = (VarStruct) var;
				int numFields = varStruct.fields.size();
				for (int i = 0; i < numFields; i++) {
					List<?> valFields = ((TypeStruct) var.type).castValueTo(val);
					encodeVarValueToIntsRec(varStruct.fields.get(i), startIndex, valFields.get(i), enc);
				}
			} else {
				throw new PrismLangException("Can't encode variable of type " + var.getClass());
			}
		}
	}

	/**
	 * Get the integer encoding of a value for the ith (primitive) variable, specified as an Object.
	 * The Object is assumed to be of correct type (e.g. Integer, Boolean).
	 * Throws an exception if Object is of the wrong type.
	 * Also throws an exception if the value is out of range.
	 */
	public int encodeVarValueToInt(int i, Object val) throws PrismLangException
	{
		Type type = getPrimitiveType(i);
		try {
			// Integer type
			if (type instanceof TypeInt) {
				int intVal = ((TypeInt) type).castValueTo(val).intValue();
				if (intVal < getPrimitiveLow(i) || intVal > getPrimitiveHigh(i)) {
					throw new PrismLangException("Value " + val + " out of range for variable " + getPrimitiveName(i));
				}
				return intVal - getPrimitiveLow(i);
			}
			// Boolean type
			else if (type instanceof TypeBool) {
				return ((TypeBool) type).castValueTo(val).booleanValue() ? 1 : 0;
			}
			// Enum type
			else if (type instanceof TypeEnum) {
				return ((TypeEnum) type).castValueTo(val).getIndex();
			}
			// Anything else
			else {
				throw new PrismLangException("Unknown type " + type + " for variable " + getPrimitiveName(i));
			}
		} catch (ClassCastException e) {
			throw new PrismLangException("Value " + val + " is wrong type for variable " + getPrimitiveName(i));
		}
	}

	/**
	 * Get the integer encoding of a value for the ith (primitive) variable, specified as a string.
	 */
	public int encodeValueToIntFromString(int i, String s) throws PrismLangException
	{
		Type type = getPrimitiveType(i);
		// Integer type
		if (type instanceof TypeInt) {
			try {
				int iVal = Integer.parseInt(s);
				if (iVal < getPrimitiveLow(i) || iVal > getPrimitiveHigh(i)) {
					throw new PrismLangException("Value " + iVal + " out of range for variable " + getPrimitiveName(i));
				}
				return iVal - getPrimitiveLow(i);
			} catch (NumberFormatException e) {
				throw new PrismLangException("\"" + s + "\" is not a valid integer value");
			}
		}
		// Boolean type
		else if (type instanceof TypeBool) {
			if (s.equals("true"))
				return 1;
			else if (s.equals("false"))
				return 0;
			else
				throw new PrismLangException("\"" + s + "\" is not a valid Boolean value");

		}
		// Enum type
		else if (type instanceof TypeEnum) {
			return ((TypeEnum) type).getConstantByName(s).getIndex();
		}
		// Anything else
		else {
			throw new PrismLangException("Unknown type " + type + " for variable " + getPrimitiveName(i));
		}
	}

	/**
	 * Get the value (as an Object) for the ith (primitive) variable,
	 * from the value encoded as an integer.
	 */
	public Object decodeFromInt(int i, int val)
	{
		return decodeFromInt(varsPrimitive.get(i), val);
	}

	/**
	 * Get the value (as an Object) for a primitive variable,
	 * from the value encoded as an integer.
	 * In case of any problems, this will return null.
	 */
	private Object decodeFromInt(VarPrimitive var, int val)
	{
		try {
			Type type = var.type;
			// Integer type
			if (type instanceof TypeInt) {
				return type.castValueTo(val + var.low, ec.getEvaluationMode());
			}
			// Boolean type
			else if (type instanceof TypeBool) {
				return val != 0;
			}
			// Enum type
			else if (type instanceof TypeEnum) {
				return ((TypeEnum) type).getConstant(val);
			}
			// Unknown
			return null;
		} catch (PrismLangException e) {
			// In case of any error return null
			return null;
		}
	}

	/**
	 * Convert an array of integer-encoded values for all (primitive) variables
	 * to a corresponding State object.
	 */
	public State decodeStateFromInts(int enc[])
	{
		State state = new State(getNumVars());
		for (int i = 0; i < getNumVars(); i++) {
			Object val = decodeStateFromIntsRec(vars.get(i), enc);
			state.setValue(i, val);
		}
		return state;
	}

	/**
	 * Recursive helper function to implement {@link #decodeStateFromInts(int[])}.
	 */
	private Object decodeStateFromIntsRec(Var var, int enc[])
	{
		if (var instanceof VarPrimitive) {
			return decodeFromInt(var.startIndex, enc[var.startIndex]);
		} else {
			if (var instanceof VarArray) {
				VarArray varArray = (VarArray) var;
				List<Object> list = new ArrayList<>(varArray.elements.size());
				for (Var varChild : varArray.elements) {
					list.add(decodeStateFromIntsRec(varChild, enc));
				}
				return list;
			} else if (var instanceof VarStruct) {
				VarStruct varStruct = (VarStruct) var;
				int numFields = varStruct.fields.size();
				List<Object> list = new ArrayList<>(numFields);
				for (int i = 0; i < numFields; i++) {
					list.add(decodeStateFromIntsRec(varStruct.fields.get(i), enc));
				}
				return list;
			}
		}
		return null;
	}

	/**
	 * Convert a bit vector representing a single state to a State object.
	 * The bit vector contains the binary encoding of all (primitive) variable values.
	 */
	public State decodeStateFromBitSet(BitSet bits)
	{
		int var = 0, val = 0, j = 0;
		int enc[] = new int[getNumPrimitiveVars()];
		for (int i = 0; i < totalNumBits; i++) {
			if (bits.get(i)) {
				val += (1 << (getPrimitiveRangeLogTwo(var) - j - 1));
			}
			if (j >= getPrimitiveRangeLogTwo(var) - 1) {
				enc[var] = val;
				var++;
				val = 0;
				j = 0;
			} else {
				j++;
			}
		}
		return decodeStateFromInts(enc);
	}

	// Utility methods

	/**
	 * Cache variable indexing info recursively within an ASTElement tree.
	 * In particular, set the variable indices on ExpressionVar objects,
	 * the array length and element size for ExpressionArrayAccess objects,
	 * and the field info for ExpressionStructAccess objects.
	 * This info is primarily to assist reading (e.g., Expression evaluation)
	 * rather than writing (e.g., variable updates), since latter requires
	 * index/range info attached to variables, in order to e.g. check for
	 * out-of-range errors, or to block-assign to arrays.
	 */
	public void addVarIndexing(ASTElement e) throws PrismLangException
	{
		// Do this is using a visitor template since we want to traverse the whole AST element.
		// Can just use ASTTraverse, not ASTTraverseModify, even though we do make changes to
		// the ASTElements, because we just call set methods, never actually replace whole elements.

		e.accept(new ASTTraverse()
		{
			public void visitPost(ExpressionStructAccess varRef) throws PrismLangException
			{
				// Look up field in associated type and store
				TypeStruct typeStruct = (TypeStruct) varRef.getStruct().getType();
				int fieldIndex = typeStruct.getFieldIndex(varRef.getFieldName());
				if (fieldIndex == -1) {
					throw new PrismLangException("Unknown field " + varRef.getFieldName(), varRef);
				}
				varRef.setFieldIndex(fieldIndex);
			}

			public void visitPost(ExpressionVar varRef) throws PrismLangException
			{
				// Look up the variable name and store
				int i = nameMap.get(varRef.getName());
				if (i == -1) {
					throw new PrismLangException("Unknown variable " + ((ExpressionVar) varRef).getName(), varRef);
				}
				varRef.setVarIndex(i);
			}
		});
	}

	/**
	 * Get the correct value to be assigned to a variable, represented by an object.
	 * The variable is specified by providing a variable reference (as an Expression).
	 * Array index values are ignored since all elements have the same type,
	 * e.g. the variable reference can be x[i] without specifying a value for i.
	 *
	 * This takes care of two things. Firstly, it casts the resulting Object to the right
	 * type required for the variable, e.g., mapping Integer to Double.
	 * Secondly, it deals with block array assignments, e.g. b'=2 as shorthand for
	 * b[0]'=2 & ... & b[n]'=2. This would convert integer 2 to list [2,...,2].
	 * If the value is null, then null is returned (and the type is ignored).
	 *
	 * TODO: check array sizes too!
	 * TODO: check range too!
	 * TODO: for effic, lists maybe modified as well as returned
	 *
	 * @param varRef Variable to be updated (reference to)
	 * @param value Value to be assigned
	 * @param type Type of value to be assigned (from)
	 */
	public Object getVarAssignmentValue(Expression varRef, Object value, Type type) throws PrismLangException
	{
//		if (!varRef.getType().canAssign(type)) {
//			value = getVarAssignmentValueRec(getSomeVarForVarRef(varRef), value, type, evalMode);
//		}
//		return value == null ? null : varRef.getType().castValueTo(value, evalMode);
		return getVarAssignmentValueRec(getSomeVarForVarRef(varRef), value, type, ec.getEvaluationMode());
	}

	public static Object getAssignmentValue(Var var, Object value, Type type, EvalMode evalMode) throws PrismLangException
	{
		return getVarAssignmentValueRec(var, value, type, evalMode);
		// TODO: even need sep rec method?
	}

	/**
	 * Recursive helper method for {@link #getVarAssignmentValue(Expression, Object, Type)}.
	 * @param var Var object for variable whose value is to be returned
	 * @param value Value to be assigned
	 * @param type Type of value to be assigned (from)
	 */
	private static Object getVarAssignmentValueRec(Var var, Object value, Type type, EvalMode evalMode) throws PrismLangException
	{
		// Null: just return null
		if (value == null) {
			return null;
		}
		// Assignment to primitive: just casting possibly needing
		else if (var.type.isPrimitive()) {
			return var.type.castValueTo(value, evalMode);
		}
		// Might need to recurse into a struct (e.g. if it contains an array)
		// TODO CHECK THIS
		else if (var instanceof VarStruct) {
			int numFields = ((VarStruct) var).fields.size();
			if (!(value instanceof List && type instanceof TypeStruct)) {
				// Shouldn't happen: type checking would catch earlier
				throw new PrismLangException("Cannot assign value " + value + " of type " + type + " to a variable of type " + var.type, var.ref);
			}
			@SuppressWarnings("unchecked")
			List<Object> valueStruct = (List<Object>) value;
			TypeStruct typeStruct = (TypeStruct) type;
			List<Object> l = new ArrayList<>(numFields);
			if (valueStruct.size() != numFields) {
				// Shouldn't happen: type checking would catch earlier
				throw new PrismLangException("Value " + value + " is the wrong size for struct " + var.ref, var.ref);
			}
			for (int i = 0; i < numFields; i++) {
				l.add(getVarAssignmentValueRec(((VarStruct) var).fields.get(i), valueStruct.get(i), typeStruct.getFieldType(i), evalMode));
			}
			return l;
		}
		// Assignment to array
		else if (var instanceof VarArray) {
			VarArray varArray = (VarArray) var;
			int arraySize = varArray.elements.size();
			int dimVar = TypeArray.getArrayDimension(var.type);
			int dimValue = TypeArray.getArrayDimension(type);
			// Dimensions match: just need to check sizes
			if (dimVar == dimValue) {
				@SuppressWarnings("unchecked")
				List<Object> listValue = (List<Object>) value;
				if (arraySize != listValue.size()) {
					throw new PrismLangException("Array size mismatch in assignment: " + State.valueToString(value) + " should be of size " + varArray.elements.size(), var.ref);
				}
				// Recurse to check at lower levels too
				for (int i = 0; i < arraySize; i++) {
					listValue.set(i, getVarAssignmentValueRec(varArray.elements.get(i), listValue.get(i), ((TypeArray) type).getSubType(), evalMode));
				}
				return listValue;
			}
			// Block assignment to array
			else if (dimVar > dimValue) {
				// Recurse to get value
				// (just use the first element of the array - they all have the same type)
				Object subValue = getVarAssignmentValueRec(varArray.elements.get(0), value, type, evalMode);
				// Then create new list of duplicate values
				List<Object> listValue = new ArrayList<>();
				for (int i = 0; i < arraySize; i++) {
					listValue.add(subValue);
				}
				return listValue;
			}
		}
		throw new PrismLangException("Cannot assign a value of type " + type + " to a variable of type " + var.type, var.ref);
	}

	/**
	 * Get a list of all possible states over the variables in this list. Use with care!
	 */
	public List<State> getAllStates() throws PrismLangException
	{
		List<State> states = new ArrayList<>();
		int enc[] = new int[getNumPrimitiveVars()];
		getAllStatesRec(0, enc, states);
		return states;
	}

	/**
	 * Recursive helper method for {@link #getAllStates()}
	 */
	private void getAllStatesRec(int v, int enc[], List<State> states) throws PrismLangException
	{
		if (v == getNumPrimitiveVars()) {
			states.add(decodeStateFromInts(enc));
		} else {
			int low = getPrimitiveLow(v);
			int high = getPrimitiveHigh(v);
			for (int j = low; j <= high; j++) {
				enc[v] = j;
				getAllStatesRec(v + 1, enc, states);
			}
		}
	}

	/**
	 * Get a list of all possible values for an integer valued-expression
	 * with respect to this variable list and some values for constants.
	 */
	public List<Integer> getAllValuesForIntExpression(Expression expr, Values constantValues) throws PrismLangException
	{
		expr = (Expression) expr.deepCopy().replaceConstants(constantValues);
		List<Expression> varRefs = expr.getAllMaximalVarRefs();
		List<Integer> values = new ArrayList<>();
		Object varRefVals[] = new Object[varRefs.size()];
		getAllValuesRec(expr, varRefs, 0, varRefVals, values);
		return values;
	}

	/**
	 * Recursive helper method for {@link #getAllValuesForIntExpression(Expression, Values)}.
	 */
	private void getAllValuesRec(Expression expr, List<Expression> varRefs, int v, Object varRefVals[], List<Integer> values) throws PrismLangException
	{
		// Base case: evaluate expression and add to list
		if (v == varRefs.size()) {
			Integer value = VarUtils.replaceVarRefs(expr, varRefs, varRefVals).evaluateInt();
			if (!values.contains(value)) {
				values.add(value);
			}

		}
		// Recursion: iterate over all values for all maximal variable references
		else {
			Var var = getSomeVarForVarRef(varRefs.get(v));
			if (!(var instanceof VarPrimitive)) {
				throw new PrismLangException("Cannot evaluate all values", expr);
			}
			VarPrimitive varPrim = (VarPrimitive) var;
			for (int j = varPrim.low; j <= varPrim.high; j++) {
				varRefVals[v] = decodeFromInt(varPrim, j);
				getAllValuesRec(expr, varRefs, v + 1, varRefVals, values);
			}
		}
	}

	// Standard methods

	/**
	 * Perform a deep copy.
	 */
	public VarList deepCopy()
	{
		VarList ret = new VarList();
		ret.setEvaluateContext(ec);
		int n = vars.size();
		ret.vars = new ArrayList<Var>(n);
		ret.nameMap = new HashMap<String, Integer>(n);
		for (int i = 0; i < n; i++) {
			ret.vars.add(vars.get(i).deepCopy());
			ret.nameMap.put(getPrimitiveName(i), i);
		}
		n = varsPrimitive.size();
		ret.varsPrimitive = new ArrayList<VarPrimitive>(n);
		for (int i = 0; i < n; i++) {
			ret.varsPrimitive.add(varsPrimitive.get(i).deepCopy());
		}
		return ret;
	}

	@Override
	public Object clone()
	{
		return deepCopy();
	}

	// Classes to store information about a single variable, or subvariable (e.g. array element)

	public class Var
	{
		// Name
		public String name;
		// Ref
		public Expression ref;
		// Type
		public Type type;
		// Further type info from variable declaration
		public DeclarationType declType;
		// Index of containing module (-1 for a global)
		public int module;
		// Start index for this in the list of all (primitive) vars
		public int startIndex;
		// Num (primitve) vars this corresponds to when arrays etc. are expanded
		public int numPrimitives;

		/** Default constructor */
		public Var(String name, Type type)
		{
			this.name = name;
			this.type = type;
		}

		/** Copy constructor */
		public Var(Var var)
		{
			name = var.name;
			type = var.type;
			declType = (DeclarationType) var.declType.deepCopy();
			module = var.module;
			startIndex = var.startIndex;
			numPrimitives = var.numPrimitives;
		}

		@Override
		public String toString()
		{
			return name + ":" + startIndex + "-" + (startIndex + numPrimitives - 1);
		}

		/**
		 * Perform a deep copy.
		 */
		public Var deepCopy()
		{
			return new Var(this);
		}
	}

	public class VarPrimitive extends Var
	{
		// Info about how variable is encoded as an integer
		public int low;
		public int high;

		/** Default constructor */
		public VarPrimitive(String name, Type type, int low, int high)
		{
			super(name, type);
			this.low = low;
			this.high = high;
		}

		/** Copy constructor */
		public VarPrimitive(VarPrimitive var)
		{
			super(var);
			this.low = var.low;
			this.high = var.high;
		}

		@Override
		public VarPrimitive deepCopy()
		{
			return new VarPrimitive(this);
		}
	}

	public class VarArray extends Var
	{
		// Elements of the array
		public List<Var> elements = new ArrayList<>();

		// Size of each array element (in terms of number of primitive vars)
		public int elementSize;

		/** Default constructor */
		public VarArray(String name, Type type)
		{
			super(name, type);
		}

		/** Copy constructor */
		public VarArray(VarArray var)
		{
			super(var);
			for (Var element : var.elements) {
				elements.add(element.deepCopy());
			}
			elementSize = var.elementSize;
		}

		@Override
		public VarArray deepCopy()
		{
			return new VarArray(this);
		}
	}

	public class VarStruct extends Var
	{
		// Fields of the struct
		public List<Var> fields = new ArrayList<>();

		// Names of each struct field
		public List<String> fieldNames = new ArrayList<>();

		// Offsets of each struct field (in terms of number of primitive vars)
		public List<Integer> fieldOffsets = new ArrayList<>();

		/** Default constructor */
		public VarStruct(String name, Type type)
		{
			super(name, type);
		}

		/** Copy constructor */
		public VarStruct(VarStruct var)
		{
			super(var);
			for (Var field : var.fields) {
				fields.add(field.deepCopy());
			}
			for (String name : var.fieldNames) {
				fieldNames.add(name);
			}
			for (int offset : var.fieldOffsets) {
				fieldOffsets.add(offset);
			}
		}

		@Override
		public VarStruct deepCopy()
		{
			return new VarStruct(this);
		}
	}

	// Classes to store information about access to a variable, or subvariable

	static abstract class VarAccess
	{
		public abstract Expression createVarRef(ExpressionVar var);
		public abstract VarAccess getChild();
		public abstract int getIndex();
	}

	static class VarAccessArray extends VarAccess
	{
		public VarAccess array;
		public int index;

		/** Default constructor */
		public VarAccessArray(VarAccess array, int index)
		{
			this.array = array;
			this.index = index;
		}

		public VarAccess getChild()
		{
			return array;
		}

		public int getIndex()
		{
			return index;
		}

		public Expression createVarRef(ExpressionVar var)
		{
			return new ExpressionArrayAccess(array == null ? var : array.createVarRef(var), ExpressionLiteral.Int(index));
		}
	}

	static class VarAccessStruct extends VarAccess
	{
		public VarAccess struct;
		public int index;
		public String fieldName;

		/** Default constructor */
		public VarAccessStruct(VarAccess struct, int index, String fieldName)
		{
			this.struct = struct;
			this.index = index;
			this.fieldName = fieldName;
		}

		public VarAccess getChild()
		{
			return struct;
		}

		public int getIndex()
		{
			return index;
		}

		public String getFieldName()
		{
			return fieldName;
		}

		public Expression createVarRef(ExpressionVar var)
		{
			return new ExpressionStructAccess(struct == null ? var : struct.createVarRef(var), fieldName);
		}
	}
}
