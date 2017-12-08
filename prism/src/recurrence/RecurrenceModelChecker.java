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
import parser.ast.LabelList;
import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Property;
import parser.ast.Update;
import parser.type.TypeInt;
import parser.visitor.ASTTraverseModify;
import prism.ModelGenerator;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismSettings;
import prism.Result;
import recurrence.data_structure.Pair;
import recurrence.model_checking.AbstractModelChecker;
import recurrence.model_checking.backward_approach.BackwardModelChecker;
import recurrence.log.*;

/**
 * CTMC model checker based on recurrence relations 
 */
public class RecurrenceModelChecker extends PrismComponent
{
	// Model file
	private ModulesFile modulesFile;
	// Properties file
	private PropertiesFile propertiesFile;
	// Constants from model
	private Values constantValues;
	// Labels from the model
	private LabelList labelListModel;
	// Labels from the property file
	private LabelList labelListProp;
	// Model generator for Modules file
	private ModelGenerator modelgen;

	private PrismComponent parent;

	// the time variable
	public static final String timeVarName = "timeVar";

	/**
	 * Constructor.
	 */
	public RecurrenceModelChecker(PrismComponent parent, ModulesFile modulesFile, PropertiesFile propertiesFile) throws PrismException
	{
		super(parent);
		this.parent = parent;
		this.modulesFile = modulesFile;
		this.propertiesFile = propertiesFile;

		// Get combined constant values from model/properties
		constantValues = new Values();
		constantValues.addValues(modulesFile.getConstantValues());
		if (propertiesFile != null)
			constantValues.addValues(propertiesFile.getConstantValues());
		this.labelListModel = modulesFile.getLabelList();
		this.labelListProp = propertiesFile.getLabelList();
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
		Result result;

		String recur_input = settings.getString(PrismSettings.PRISM_RECUR_VAR);
		String recur_const = recur_input.substring(0, recur_input.indexOf("="));
		Integer recur_val = new Integer(recur_input.substring(recur_input.indexOf("=") + 1, recur_input.length()));
		System.out.println("Constant: " + recur_const);
		System.out.println("Constant Value: " + recur_val);

		 
		int num_vars = modulesFile.getNumVars();
		List<String> candidate_vars = new ArrayList<String>();
		
		// Identify all other variables depends on the recurrence parameter
		for (int i = 0; i < num_vars; i++) {
			Declaration decl = modulesFile.getVarDeclaration(i);
			Vector<String> consts = decl.getAllUndefinedConstantsRecursively(modulesFile.getConstantList(), null, null);
			if (consts.contains(recur_const)) {
				candidate_vars.add(decl.getName());
			}
		}
		
		String recur_var;
		switch (candidate_vars.size()) {
		// No other variable depends on the recurrence parameter
		case 0:
			preprocessDTMC(recur_const, recur_val);
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

		System.out.println("Variable: " + recur_var);
		// Log.p(Level.INFO, modulesFile, this.getClass());
		// Log.p(Level.INFO, propertiesFile, this.getClass());
		
		// boolean isTimedDTMC = rec_var.equals(timeVarName);

		// Create the backward model checker
		AbstractModelChecker amc = new BackwardModelChecker(parent, modulesFile, propertiesFile, recur_var, expr);

		// Analyze the modules_file and find the recurrent interval 
		amc.computeRegion();
		// Proceed with the model checking and return the result
		amc.process();
		return new Result(amc.result);
	}

	public void preprocessDTMC(String recConst, Integer recVal) throws PrismLangException
	{
		// Take a copy of the whole model/properties file before translation
		ModulesFile mf = (ModulesFile) modulesFile.deepCopy();
		PropertiesFile pf = (propertiesFile == null) ? null : (PropertiesFile) propertiesFile.deepCopy();

		// Create the expression identity for the recur constant
		ExpressionIdent ecRecurParam = new ExpressionIdent(recConst);
		// Add constant to the modulesFile
		mf.getConstantList().addConstant(ecRecurParam, null, TypeInt.getInstance());
		mf.getConstantValues().addValue(recConst, recVal);

		// Give a name to the time variable
		String timeVarName = "timeVar";

		// Create and add the global declaration of the time variable
		DeclarationType declType = new DeclarationInt(Expression.Int(0), ecRecurParam);
		Declaration decl = new Declaration(timeVarName, declType);
		mf.addGlobal(decl);
		// Create the time variable
		ExpressionVar timeVar = new ExpressionVar(timeVarName, decl.getType());

		// Create guard condition expression for the time variable
		ExpressionBinaryOp timeCondition = new ExpressionBinaryOp(ExpressionBinaryOp.LT, timeVar, ecRecurParam);
		// Create Expression Identity for the time variable
		ExpressionIdent timeIdent = new ExpressionIdent(timeVarName);
		// Create update expression for the time variable
		Expression timeUpdate = Expression.Plus(timeVar, Expression.Int(1));

		mf = (ModulesFile) mf.accept(new ASTTraverseModify()
		{
			public Object visit(Module e) throws PrismLangException
			{
				for (Command cmd : e.getCommands()) {
					// update the guard of the command
					cmd.setGuard(ExpressionBinaryOp.And(cmd.getGuard(), timeCondition));
					// add time update element for the each update in the command
					for (Update up : cmd.getUpdates().getUpdates()) {
						up.addElement(timeIdent, timeUpdate);
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
				ExpressionProb exProb = (ExpressionProb) p.getExpression();
				ExpressionTemporal et = (ExpressionTemporal) exProb.getExpression();

				ExpressionBinaryOp exp = null;
				if (et.getUpperBound() != null) {
					int operator = et.upperBoundIsStrict() ? ExpressionBinaryOp.LT : ExpressionBinaryOp.LE;
					exp = new ExpressionBinaryOp(operator, timeVar, ecRecurParam);
					et.setUpperBound(null);
				}

				if (et.getLowerBound() != null) {
					int operator = et.lowerBoundIsStrict() ? ExpressionBinaryOp.GT : ExpressionBinaryOp.GE;
					ExpressionBinaryOp tmp = new ExpressionBinaryOp(operator, timeVar, ecRecurParam);
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
		modulesFile = mf;
		propertiesFile = pf;
	}
}
