package recurrence.utils.expression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parser.State;
import parser.VarList;

public class ExpressionChecker
{
	private IChecker checker;
	public static VarList varList;

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

	public boolean isValid(State state)
	{
		return checker.validate(state.varValues);
	}

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
