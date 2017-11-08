package recurrence.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import recurrence.data_structure.numbers.Decimal;
import recurrence.data_structure.numbers.INumber;

/**
 * 	This class contains helper methods
 * 	@class Helper	
 */
public class Helper
{
	/**
	 *  This method simplifies the given polynomial by removing unnecessary place holders
	 *  @param polynomial that needs to be simplified
	 *  @return INumber[] the simplified polynomial
	 */
	public static INumber[] shrink(INumber[] polynomial)
	{
		// Stores the degree of the polynomial
		int polyDeg;

		// Checks for the unnecessary place holders and computes the actual degree of the polynomial
		for (polyDeg = polynomial.length - 1; polyDeg > 0; polyDeg--) {
			if (!polynomial[polyDeg].isZero())
				break;
		}

		// Reforms the polynomial by eliminating the unnecessary place holders
		if (polyDeg < (polynomial.length - 1)) {
			INumber[] _polynomial = new INumber[polyDeg + 1];
			System.arraycopy(polynomial, 0, _polynomial, 0, _polynomial.length);
			return _polynomial;
		} else {
			return polynomial;
		}

	}

	/**
	 *  This method rounds up the given BigDecimal number to the predefined scale
	 *  @param decimal represents the given decimal number
	 *  @return return the rounded up decimal number
	 */
	public static BigDecimal setScale(BigDecimal decimal)
	{
		BigDecimal epsilon = new BigDecimal("1E-" + Decimal.FIRST_SCALE);
		if (epsilon.compareTo(decimal.abs()) == 1)
			return BigDecimal.ZERO;
		return decimal.setScale(Decimal.THIRD_SCALE, Decimal.ROUNDING_MODE).stripTrailingZeros();
	}

	/**
	 *  This method divides the given numerator by the denominator to the predefined scale
	 *  @param numerator the number to be divided
	 *  @param denominator the number that divides
	 *  @return return the result as the rounded up decimal number
	 */
	public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator)
	{
		return numerator.divide(denominator, Decimal.THIRD_SCALE, Decimal.ROUNDING_MODE);
	}

	/**
	 *  This method finds the square root of the given BigDecimal number
	 *  @param decimal the number to be square rooted
	 *  @return return the result as the rounded up decimal number
	 *  @author Borrowed from https://java.net/projects/jide-oss
	 */
	public static BigDecimal sqrt(BigDecimal decimal)
	{
		int digits; // final precision
		BigDecimal numberToBeSquareRooted;
		BigDecimal iteration1;
		BigDecimal iteration2;
		BigDecimal temp1 = null;
		BigDecimal temp2 = null; // tmp values

		int extraPrecision = decimal.precision();
		MathContext mc = new MathContext(extraPrecision, Decimal.ROUNDING_MODE);
		numberToBeSquareRooted = decimal; // bd global variable
		double dblNum = numberToBeSquareRooted.doubleValue(); // bd to double

		if (mc.getPrecision() == 0)
			throw new IllegalArgumentException("\nRoots need a MathContext precision > 0");
		if (dblNum < 0.)
			throw new ArithmeticException("\nCannot calculate the square root of a negative number");
		if (dblNum == 0.)
			return decimal.round(mc); // return sqrt(0) immediately

		if (mc.getPrecision() < 50) // small precision is buggy..
			extraPrecision += 10; // ..make more precise
		int startPrecision = 1; // default first precision

		if (dblNum == Double.POSITIVE_INFINITY) // d > 1.7E308
		{
			BigInteger bi = numberToBeSquareRooted.unscaledValue();
			int biLen = bi.bitLength();
			int biSqrtLen = biLen / 2; // floors it too

			bi = bi.shiftRight(biSqrtLen); // bad guess sqrt(d)
			iteration1 = new BigDecimal(bi); // x ~ sqrt(d)

			MathContext mm = new MathContext(Decimal.THIRD_SCALE, Decimal.ROUNDING_MODE); // minimal precision
			extraPrecision += 10; // make up for it later

			iteration2 = BigDecimal.ONE.divide(Decimal.TWO.multiply(iteration1, mm), mm); // v = 1/(2*x)
		} else // d < 1.7E10^308  (the usual numbers)
		{
			double s = Math.sqrt(dblNum);
			iteration1 = new BigDecimal(((Double) s).toString()); // x = sqrt(d)
			iteration2 = new BigDecimal(((Double) (1. / 2. / s)).toString()); // v = 1/2/x
			// works because Double.MIN_VALUE * Double.MAX_VALUE ~ 9E-16, so: v > 0

			startPrecision = 64;
		}

		digits = mc.getPrecision() + extraPrecision; // global limit for procedure

		// create initial MathContext(precision, RoundingMode)
		MathContext n = new MathContext(startPrecision, mc.getRoundingMode());

		BigDecimal result = sqrtProcedure(n, digits, numberToBeSquareRooted, iteration1, iteration2, temp1, temp2); // return square root using argument precision
		return result;
	}

	/**
	 *  This method processes the square root procedure
	 */
	private static BigDecimal sqrtProcedure(MathContext mc, int digits, BigDecimal numberToBeSquareRooted, BigDecimal iteration1, BigDecimal iteration2,
			BigDecimal temp1, BigDecimal temp2)
	{
		// next v                                         // g = 1 - 2*x*v
		temp1 = BigDecimal.ONE.subtract(Decimal.TWO.multiply(iteration1, mc).multiply(iteration2, mc), mc);
		iteration2 = iteration2.add(temp1.multiply(iteration2, mc), mc); // v += g*v        ~ 1/2/sqrt(d)

		// next x
		temp2 = numberToBeSquareRooted.subtract(iteration1.multiply(iteration1, mc), mc); // e = d - x^2
		iteration1 = iteration1.add(temp2.multiply(iteration2, mc), mc); // x += e*v        ~ sqrt(d)

		// increase precision
		int m = mc.getPrecision();
		if (m < 2)
			m++;
		else
			m = m * 2 - 1; // next Newton iteration supplies so many exact digits

		if (m < 2 * digits) // digits limit not yet reached?
		{
			mc = new MathContext(m, mc.getRoundingMode()); // apply new precision
			sqrtProcedure(mc, digits, numberToBeSquareRooted, iteration1, iteration2, temp1, temp2); // next iteration
		}
		return iteration1; // returns the iterated square roots
	}
}
