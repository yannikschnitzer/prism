package recurrence.math;

import java.util.HashMap;
import java.util.Map;

import prism.PrismException;
import recurrence.data_structure.numbers.INumber;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.math.matrix.EigenValueDecomposition;

public class RootFinder
{
	public Map<PolynomialFraction, EigenValueDecomposition> lsSolution;
	PolynomialFraction currentPolynomialFraction;

	public static final int ROOTS = 0;
	public static final int RECIPROCAL_ROOTS = 1;

	public RootFinder()
	{
		this.lsSolution = new HashMap<PolynomialFraction, EigenValueDecomposition>();
	}

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

	public double[] getAsDoubles(INumber[] val)
	{
		double[] dblVal = new double[val.length];
		for (int i = 0; i < val.length; i++)
			dblVal[i] = val[i].doubleValue().getReal();
		return dblVal;
	}
}
