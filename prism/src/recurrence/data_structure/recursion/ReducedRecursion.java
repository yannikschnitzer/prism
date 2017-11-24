package recurrence.data_structure.recursion;

import recurrence.data_structure.numbers.Decimal;
import recurrence.data_structure.numbers.INumber;
import cern.colt.Arrays;

/**
* 	This class that represents the decimal numbers
* 	@class ReducedRecursion	
*/
/**
 * @author Nishan
 *
 */
public class ReducedRecursion
{

	INumber[] rs;
	INumber[] coeffs;
	String str_eqn;

	/**
	 * Constructor that stores the closed form a recurrence relation
	 * @param rs terms of the closed function
	 * @param coeffs coefficients of the terms
	 */
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

	/**
	 * Evaluates the closed form for given value of n recursions
	 * @param n recurring times
	 * @return value after n time recurring.
	 */
	public INumber getValue(int n)
	{
		INumber solution = new Decimal(0);
		for (int i = 0; i < rs.length; i++) {
			solution = solution.plus((coeffs[i].timesby(rs[i]._pow(n))));
		}
		return solution;
	}

	/**
	 * @return the string representation of the closed function
	 */
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
