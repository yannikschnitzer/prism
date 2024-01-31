//==============================================================================
//	
//	Copyright (c) 2013-
//	Authors:
//	* Ernst Moritz Hahn <emhahn@cs.ox.ac.uk> (University of Oxford)
//	* Dave Parker <david.parker@cs.ox.ac.uk> (University of Birmingham/Oxford)
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

package param;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import explicit.IndexedSet;
import explicit.StateStorage;
import parser.State;
import parser.Values;
import parser.ast.Expression;
import prism.Evaluator;
import prism.ModelGenerator;
import prism.ModelType;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismNotSupportedException;
import prism.PrismSettings;
import prism.Evaluator.EvaluatorBigRational;

/**
 * Class to construct a parametric Markov model.
 */
public final class ModelBuilder extends PrismComponent
{
	/** mode (parametric / exact) */
	private final ParamMode mode;
	/** function factory used in the constructed parametric model */
	private FunctionFactory functionFactory;
	/** names of parameters */
	private String[] paramNames;
	/** name of function type to use, as read from PRISM settings */
	private String functionType;
	/** maximal error probability of DAG function representation */
	private double dagMaxError;

	/** local storage made static for use in anonymous class */
	private static Map<String,Expression> constExprs;
	
	/**
	 * Constructor
	 */
	public ModelBuilder(PrismComponent parent, ParamMode mode) throws PrismException
	{
		super(parent);
		this.mode = mode;
		// If present, initialise settings from PrismSettings
		if (settings != null) {
			functionType = settings.getString(PrismSettings.PRISM_PARAM_FUNCTION);
			dagMaxError = settings.getDouble(PrismSettings.PRISM_PARAM_DAG_MAX_ERROR);
		}
	}
	
	public FunctionFactory getFunctionFactory(String[] paramNames, String[] lowerStr, String[] upperStr)
	{
		this.paramNames = paramNames;
		BigRational[] lower = new BigRational[lowerStr.length];
		BigRational[] upper = new BigRational[upperStr.length];
		for (int param = 0; param < lowerStr.length; param++) {
			lower[param] = new BigRational(lowerStr[param]);
			upper[param] = new BigRational(upperStr[param]);
		}
		
		// Create function factory
		if (functionType.equals("JAS")) {
			functionFactory = new JasFunctionFactory(paramNames, lower, upper);
		} else if (functionType.equals("JAS-cached")) {
			functionFactory = new CachedFunctionFactory(new JasFunctionFactory(paramNames, lower, upper));
		} else if (functionType.equals("DAG")) {
			functionFactory = new DagFunctionFactory(paramNames, lower, upper, dagMaxError, false);
		}

		return functionFactory;
	}
}
