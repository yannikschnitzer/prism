package recurrence.math.partialfraction;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import prism.PrismException;
import recurrence.data_structure.numbers.Decimal;
import recurrence.data_structure.numbers.INumber;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.data_structure.recursion.ReducedRecursion;
import recurrence.math.RootFinder;
import recurrence.math.matrix.GJENumber;

public class Decomposition
{
	RootFinder rootFinder;
	int numVars;

	INumber[][] A;
	INumber[] B;

	public Decomposition()
	{
		this.rootFinder = new RootFinder();
	}

	public ReducedRecursion decompose(PolynomialFraction solution) throws PrismException
	{
		INumber[] rs;
		INumber[] coeffs;
		//System.out.println(solution.getDenominator().getDegree());
		if (solution.getDenominator().getDegree() > 1) {
			rs = rootFinder.findRoots(solution.getDenominator(), RootFinder.RECIPROCAL_ROOTS);
			numVars = rs.length;
			computeAB(solution.getNumerator(), rs);
			GJENumber gjen = new GJENumber(A, B);
			coeffs = gjen.result();
		} else {
			rs = new INumber[] { solution.getDenominator().numerator[1].negate() };
			coeffs = solution.getNumerator().numerator;
		}
		ReducedRecursion rr = new ReducedRecursion(rs, coeffs);
		return rr;
	}

	public void computeAB(PolynomialFraction polyB, INumber[] rs)
	{
		Map<PolynomialFraction, Integer> mm = getPolynomialMultiplicity(rs);
		PolynomialFraction[] polyfracVars = new PolynomialFraction[numVars];

		Iterator<Map.Entry<PolynomialFraction, Integer>> it = mm.entrySet().iterator();

		int countPolyFracs = 0;

		while (it.hasNext()) {
			Entry<PolynomialFraction, Integer> entry = it.next();
			PolynomialFraction pf = entry.getKey();
			int multiplicity = entry.getValue();

			// before
			for (int i = 0; i < countPolyFracs; i++) {
				if (polyfracVars[i] != null)
					polyfracVars[i] = polyfracVars[i].times(pf.power(multiplicity));
				else
					polyfracVars[i] = pf.power(multiplicity);
			}
			// during
			for (int i = 0; i < multiplicity - 1; i++) {
				int j = i + countPolyFracs;
				if (polyfracVars[j] != null)
					polyfracVars[j] = polyfracVars[j].times(pf.power(multiplicity - i + 1));
				else
					polyfracVars[j] = pf.power(i + 1);
			}
			// after
			for (int i = countPolyFracs + multiplicity; i < polyfracVars.length; i++) {
				if (polyfracVars[i] != null)
					polyfracVars[i] = polyfracVars[i].times(pf.power(multiplicity));
				else
					polyfracVars[i] = pf.power(multiplicity);
			}

			countPolyFracs += multiplicity;
		}

		int maxRow = polyB.deg + 1;
		for (int i = 0; i < polyfracVars.length; i++) {
			if (polyfracVars[i].deg + 1 > maxRow)
				maxRow = polyfracVars[i].deg + 1;
		}

		A = new INumber[maxRow][numVars];

		for (int col = 0; col < numVars; col++) {
			INumber[] var = polyfracVars[col].numerator;

			for (int row = 0; row < var.length; row++)
				A[row][col] = var[row];

			for (int row = var.length; row < maxRow; row++)
				A[row][col] = Decimal.zero();

		}

		B = new INumber[maxRow];
		for (int row = 0; row < polyB.numerator.length; row++)
			B[row] = new Decimal(polyB.numerator[row].doubleValue().getReal());
		for (int row = polyB.numerator.length; row < maxRow; row++)
			B[row] = Decimal.zero();
	}

	public Map<PolynomialFraction, Integer> getPolynomialMultiplicity(INumber[] rs)
	{
		Map<INumber, Integer> in = new LinkedHashMap<INumber, Integer>();

		for (int i = 0; i < rs.length; i++) {
			Integer val;
			if ((val = in.get(rs[i])) != null)
				in.put(rs[i], val++);
			else
				in.put(rs[i], 1);
		}

		Map<PolynomialFraction, Integer> mm = new LinkedHashMap<PolynomialFraction, Integer>(in.size());

		for (int i = 0; i < in.size(); i++) {
			PolynomialFraction pf = new PolynomialFraction(Decimal.one(), rs[i].negate());
			mm.put(pf, in.get(rs[i]));
		}
		return mm;
	}
}
