package recurrence.utils.expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parser.State;
import parser.VarList;

public class ExpressionChecker
{
	private IChecker checker;
	public static VarList varList;

	/**
	 * Construct that constructs the expression checker based on the parameters
	 * @param constraint the condition for a state to be valid
	 * @param varList the list of variables makes a state
	 */
	public ExpressionChecker(String constraint, VarList varList)
	{
		ExpressionChecker.varList = varList;
		String indexedConstraint = getIndexedConstraint(constraint);
		try {
			ExpressionFactory ef = new ExpressionFactory(indexedConstraint);
			checker = ef.getChecker();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	 * @param state a state that needs to be checked for its validity
	 * @return true if the state is valid
	 */
	public boolean isValid(State state)
	{
		return checker.validate(state.var_values);
	}

	/**
	 * Generates a java boolean expression to verify the validity of a state
	 * @param constraint the constraint for a state to be valid
	 * @return valid java expression that validates the state 
	 */
	public String getIndexedConstraint(String constraint)
	{
		String tmpConstraint = constraint;
		if (constraint.contains(" "))
			tmpConstraint = tmpConstraint.replaceAll("\\s", "");
		if (constraint.contains("&"))
			tmpConstraint = tmpConstraint.replaceAll("&", "&&");
		if (constraint.contains("|"))
			tmpConstraint = tmpConstraint.replaceAll("|", "||");

		Pattern checkRegEx = Pattern.compile("[a-zA-Z_]{1,}[\\w]*|<=|>=|=");
		Matcher regexMatcher = checkRegEx.matcher(tmpConstraint);

		StringBuffer sb = new StringBuffer();

		while (regexMatcher.find()) {
			String result = regexMatcher.group();
			if (!(result.equals("<=") || result.equals(">="))) {
				if (result.equals("="))
					regexMatcher.appendReplacement(sb, "==");
				else {
					int index = varList.getIndex(regexMatcher.group());
					String type = varList.getType(index).getTypeString();
					String input;

					if (type.equals("int"))
						input = "((Integer)state[" + index + "]).intValue()";
					else
						input = "((Boolean)state[" + index + "]).booleanValue()";

					regexMatcher.appendReplacement(sb, input);
				}
			}
		}

		regexMatcher.appendTail(sb);
		return sb.toString();
	}
}
