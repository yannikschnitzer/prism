package recurrence.math;

import java.util.HashMap;
import java.util.Map;

import prism.PrismException;
import recurrence.data_structure.numbers.INumber;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.math.matrix.EigenValueDecomposition;

/**
 * 	This class is created to identify the roots of a polynomial
 * 	@class Decimal	
 */
public class RootFinder
{
	// storage to store the roots for every unique polynomial fraction
	public Map<PolynomialFraction, EigenValueDecomposition> lsSolution; 
	PolynomialFraction currentPolynomialFraction;

	public static final int ROOTS = 0;
	public static final int RECIPROCAL_ROOTS = 1;

	/**
	 *  Constructor that initializes the list of solutions 
	 */
	public RootFinder()
	{
		this.lsSolution = new HashMap<PolynomialFraction, EigenValueDecomposition>();
	}

	/**
	 * Finds the roots of a polynomial using Eigen value decomposition then returns the results. 
	 * @param polyfrac polynomial fraction to be solved
	 * @param option find the roots or reciprocal roots
	 * @return returns array of roots or reciprocal of roots based on the option
	 * @throws PrismException
	 */
	public INumber[] findRoots(PolynomialFraction polyfrac, int option) throws PrismException
	{
		boolean rootExists = lsSolution.containsKey(polyfrac);
		if (!rootExists) {
			EigenValueDecomposition evd = new EigenValueDecomposition(getAsDoubles(polyfrac.numerator));
			lsSolution.put(polyfrac, evd);
		}
		if (option == ROOTS)
			return lsSolution.get(polyfrac).getRoots();
		else if (option == RECIPROCAL_ROOTS)
			return lsSolution.get(polyfrac).getReciprocalRoots();
		else
			throw new PrismException("Not a valid option");
	}

	/**
	 * Convert the numbers into doubles
	 * @param val list of numbers
	 * @return array of converted numbers into double
	 */
	public static double[] getAsDoubles(INumber[] val)
	{
		double[] dblVal = new double[val.length];
		for (int i = 0; i < val.length; i++)
			dblVal[i] = val[i].doubleValue().getReal();
		return dblVal;
	}
}
