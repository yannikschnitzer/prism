package recurrence.data_structure.recursion;

import recurrence.data_structure.numbers.Decimal;
import recurrence.data_structure.numbers.INumber;
import cern.colt.Arrays;

/// WHAT IS THIS CLASS?????
public class ReducedRecursion
{

	INumber[] rs;
	INumber[] coeffs;
	String str_eqn;

	public ReducedRecursion(INumber[] rs, INumber[] coeffs)
	{
		this.rs = rs;
		this.coeffs = coeffs;
		// Rounding up to the give precision
		for (INumber i : rs)
			i.firstScale();
		//		for (INumber i : coeffs)
		//			i.actual();
		
		str_eqn = "[ " + coeffs[0] + " * (" + rs[0] + ")^n ]";
		for (int i = 1; i < rs.length; i++) {
			str_eqn += "+ [ " + coeffs[i] + " * (" + rs[i] + ")^n ]";
		}
		
	}

	public INumber getValue(int timeStep)
	{
		INumber solution = new Decimal(0);
		for (int i = 0; i < rs.length; i++) {
			solution = solution.plus((coeffs[i].timesby(rs[i]._pow(timeStep))));
		}
		return solution;
	}

	public String getEqnString()
	{
		return str_eqn;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder("\tValues of r (in the denomitaor) : " + Arrays.toString(rs) + "\n");
		sb.append("\tValues of the numerators : " + Arrays.toString(coeffs) + "\n");
		sb.append("\tEquation : [ " + coeffs[0] + " * (" + rs[0] + ")^n ]");
		for (int i = 1; i < rs.length; i++) {
			sb.append(" + [ " + coeffs[i] + " * (" + rs[i] + ")^n ]");
		}
		return sb.toString();
	}
}
