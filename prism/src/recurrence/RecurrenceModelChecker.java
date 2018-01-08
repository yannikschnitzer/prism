//==============================================================================
//	
//	Copyright (c) 2013-
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

package recurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import parser.Values;
import parser.ast.Command;
import parser.ast.ConstantList;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.DeclarationType;
import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionTemporal;
import parser.ast.ExpressionUnaryOp;
import parser.ast.ExpressionVar;
import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Property;
import parser.ast.Update;
import parser.type.TypeInt;
import parser.visitor.ASTTraverseModify;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismSettings;
import prism.Result;
import recurrence.model_checking.AbstractModelChecker;
import recurrence.model_checking.backward_approach.BackwardModelChecker;
import simulator.ModulesFileModelGeneratorSymbolic;

/**
 * CTMC model checker based on recurrence relations 
 */
public class RecurrenceModelChecker extends PrismComponent
{
	// Model file
	private ModulesFile modules_file;
	// Properties file
	private PropertiesFile properties_file;
	// Constants from model
	private Values constantValues;

	private PrismComponent parent;

	// the time variable
	public static final String timeVarName = "timeVar";

	// Quantile property of interest
	public static boolean IS_QUANTILE = false;
	
	// Require a function for the property of interest wrt. recur_param
	public static boolean IS_FUNCTION = false;
	
	// Do not print the result when no value is assigned for the recur_param
	public static boolean PRINT_RESULT = false;
	
	// Represents the quantile probability
	public static double QUANT_PROB = 0.0;

	/**
	 * Constructor.
	 */
	public RecurrenceModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile) throws PrismException
	{
		super(parent);
		this.parent = parent;
		this.modules_file = modulesFile;
		this.properties_file = propertiesFile;

		// Get combined constant values from model/properties
		constantValues = new Values();
		constantValues.addValues(modulesFile.getConstantValues());
		if (propertiesFile != null)
			constantValues.addValues(propertiesFile.getConstantValues());
	}

	/**
	 * Model check a property.
	 */
	public Result check(Expression expr) throws PrismException
	{
		Result res;
		String resultString;
		long timer;

		// Starting model checking
		timer = System.currentTimeMillis();

		// Do model checking
		res = checkExpression(expr);

		// Model checking complete
		timer = System.currentTimeMillis() - timer;
		mainLog.println("\nModel checking completed in " + (timer / 1000.0) + " secs.");

		// Print result to log
		resultString = "Result";
		if (!("Result".equals(expr.getResultName())))
			resultString += " (" + expr.getResultName().toLowerCase() + ")";
		resultString += ": " + res;
		mainLog.print("\n" + resultString + "\n");

		// Return result
		return res;
	}

	/**
	 * Model check an expression (used recursively).
	 */
	private Result checkExpression(Expression expr) throws PrismException
	{
		// Retrieve the recurrence expression passed as the argument
		String recur_expr = settings.getString(PrismSettings.PRISM_RECUR_VAR);
		// Check if recurrence variable is assigned with a value
		int index_of_equal = recur_expr.indexOf("=");
		// Check if the property of interest is quantile
		int index_of_comma = recur_expr.indexOf(",");
		String recur_param = "Undefined";
		// Parameter value can be either an integer or a range
		String recur_param_val = "Undefined";

		if (index_of_comma != -1) {
			String recur_props = recur_expr.substring(index_of_comma + 1);
			recur_expr = recur_expr.substring(0, index_of_comma);
			if(recur_props.contains("quantile")) {
				QUANT_PROB = Double.valueOf(recur_props.substring(recur_props.indexOf("=")+1, recur_props.indexOf(",")));
				IS_QUANTILE = true;
			}
			if(recur_props.contains("func"))
				IS_FUNCTION = true;
		}

		if (index_of_equal == -1) {
			recur_param = recur_expr.trim();
		} else {
			PRINT_RESULT = true;
			recur_param = recur_expr.substring(0, recur_expr.indexOf("="));
			recur_param_val = recur_expr.substring(recur_expr.indexOf("=") + 1).trim();
		}

		System.out.println("Recurrence Parameter: " + recur_param);
		System.out.println("Recurrence Parameter Value: " + recur_param_val);

		int num_vars = modules_file.getNumVars();
		List<String> candidate_vars = new ArrayList<String>();

		// Identify all other variables depends on the recurrence parameter
		for (int i = 0; i < num_vars; i++) {
			Declaration decl = modules_file.getVarDeclaration(i);
			Vector<String> consts = decl.getAllUndefinedConstantsRecursively(modules_file.getConstantList(), null, null);
			if (consts.contains(recur_param)) {
				candidate_vars.add(decl.getName());
			}
		}

		String recur_var;
		switch (candidate_vars.size()) {
		// No other variable depends on the recurrence parameter
		case 0:
			preprocessDTMC(recur_param, recur_param_val);
			recur_var = timeVarName;
			break;
		// Only one variable depends on the recurrence parameter
		case 1:
			recur_var = candidate_vars.get(0);
			break;
		// More than one variable depends on the recurrence parameter
		default:
			throw new PrismException("RMC does not handle the case where more than one variable depends on the recurrence parameter!!");
		}

		System.out.println("Recurrence Variable: " + recur_var);

		// [ Debug ]
		// Log.p(Level.INFO, modulesFile, this.getClass());
		// Log.p(Level.INFO, propertiesFile, this.getClass());

		/*
		  // NOTE: Currently uses ModulesFileModelGeneratorSymbolic with custom wrapper around, Thus always requires a 
		  // valid value for the recurrence parameter K
				// Remove the temporary assignment of the recurrence parameter and
				// all of the constants depends on it
				ConstantList constantList = modules_file.getConstantList();
				modules_file.getConstantValues().removeValue(recur_param);
				for (int i = 0; i < constantList.size(); i++) {
					Expression constantExpr = constantList.getConstant(i);
					if (constantExpr != null && constantExpr.getAllConstants().contains(recur_param))
						modules_file.getConstantValues().removeValue(constantList.getConstantName(i));
				}
		*/

		// Create the backward model checker
		AbstractModelChecker amc = new BackwardModelChecker(parent, modules_file, properties_file, recur_var, expr, recur_param, recur_param_val);

		// Analyze the modules_file and find the recurrent interval 
		amc.computeRegion();
		// Proceed with the model checking and return the result
		amc.process();

		return new Result(amc.result);
	}

	public void preprocessDTMC(String recur_const, String recur_val) throws PrismLangException
	{
		// Take a copy of the whole model/properties file before translation
		ModulesFile mf = (ModulesFile) modules_file.deepCopy();
		PropertiesFile pf = (properties_file == null) ? null : (PropertiesFile) properties_file.deepCopy();

		// Create the expression identity for the recur constant
		ExpressionIdent recur_param_ident = new ExpressionIdent(recur_const);
		// Add constant to the modulesFile
		mf.getConstantList().addConstant(recur_param_ident, null, TypeInt.getInstance());
		mf.getConstantValues().addValue(recur_const, recur_val);

		// Give a name to the time variable
		String time_var_name = "time_var";

		// Create and add the global declaration of the time variable
		DeclarationType decl_type = new DeclarationInt(Expression.Int(0), recur_param_ident);
		Declaration decl = new Declaration(time_var_name, decl_type);
		mf.addGlobal(decl);
		// Create the time variable
		ExpressionVar time_var = new ExpressionVar(time_var_name, decl.getType());

		// Create guard condition expression for the time variable
		ExpressionBinaryOp time_condition = new ExpressionBinaryOp(ExpressionBinaryOp.LT, time_var, recur_param_ident);
		// Create Expression Identity for the time variable
		ExpressionIdent time_ident = new ExpressionIdent(time_var_name);
		// Create update expression for the time variable
		Expression time_update = Expression.Plus(time_var, Expression.Int(1));

		mf = (ModulesFile) mf.accept(new ASTTraverseModify()
		{
			public Object visit(Module e) throws PrismLangException
			{
				for (Command cmd : e.getCommands()) {
					// update the guard of the command
					cmd.setGuard(ExpressionBinaryOp.And(cmd.getGuard(), time_condition));
					// add time update element for the each update in the command
					for (Update up : cmd.getUpdates().getUpdates()) {
						up.addElement(time_ident, time_update);
					}
				}
				return e;
			}
		});
		mf.tidyUp();

		//		/System.out.println(mf);
		pf = (PropertiesFile) pf.accept(new ASTTraverseModify()
		{
			public Object visit(Property p)
			{
				ExpressionProb prob_expr = (ExpressionProb) p.getExpression();
				ExpressionTemporal et = (ExpressionTemporal) prob_expr.getExpression();

				ExpressionBinaryOp exp = null;
				if (et.getUpperBound() != null) {
					int operator = et.upperBoundIsStrict() ? ExpressionBinaryOp.LT : ExpressionBinaryOp.LE;
					exp = new ExpressionBinaryOp(operator, time_var, recur_param_ident);
					et.setUpperBound(null);
				}

				if (et.getLowerBound() != null) {
					int operator = et.lowerBoundIsStrict() ? ExpressionBinaryOp.GT : ExpressionBinaryOp.GE;
					ExpressionBinaryOp tmp = new ExpressionBinaryOp(operator, time_var, recur_param_ident);
					exp = (exp != null) ? ExpressionBinaryOp.And(exp, tmp) : tmp;
					et.setLowerBound(null);
				}

				if (exp == null) {
					// Unbounded operator case
					System.exit(0);
				}

				et.setOperand2(ExpressionBinaryOp.And(et.getOperand2(), new ExpressionUnaryOp(ExpressionUnaryOp.PARENTH, exp)));
				return p;
			}
		});
		pf.setModelInfo(mf);
		pf.tidyUp();
		modules_file = mf;
		properties_file = pf;
	}
}
