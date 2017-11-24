package recurrence.data_structure.numbers;

import java.util.Arrays;

import recurrence.data_structure.result.DivisionResult;
import recurrence.utils.Helper;

/**
 * @author Nishan
 *
 */
/**
 * @author Nishan
 *
 */
/**
 * @author Nishan
 *
 */
public class PolynomialFraction
{

	// Arrays that stores the coefficients of the given polynomial in the ascending order of the power
	public INumber[] numerator;
	public INumber[] denominator;
	// Degree of this polynomial (0 for the zero() polynomial)
	public int deg;

	/** Generates a polynomial fraction of the form  [ a * x^b / 1]
	 *  @param INumber the 
	 */
	public PolynomialFraction(INumber a, int b)
	{
		numerator = fillZeroes(b + 1); // fills with 0 
		denominator = new INumber[] { Rational.ONE };
		numerator[b] = a;
		deg = degree(false);
	}

	/**
	 * Generates a polynomial fraction in the form of a * x^0 / 1
	 * @param a coefficient of x^0
	 */
	public PolynomialFraction(INumber a)
	{
		this(a, 0);
	}

	/**
	 * Generates a polynomial fraction in the form of a * x^0 / 1
	 * @param a coefficient of x^0
	 */
	public PolynomialFraction(int a)
	{
		this(new Rational(a));
	}

	/**
	 * Generates a polynomial fraction in the form of a * x^0 + b * x^1 + ....... / 1
	 * @param a array of coefficients
	 */
	public PolynomialFraction(INumber... a)
	{
		this(a, false);
	}

	/**
	 * Generates a polynomial fraction in the form of a * x^0 + b * x^1 + ....... and simplifies it
	 * @param a array of coefficients
	 */
	public PolynomialFraction(INumber[] a, boolean isCancel)
	{
		numerator = new INumber[a.length];
		denominator = new INumber[] { Rational.ONE };
		for (int i = 0; i < a.length; i++)
			numerator[i] = a[i];
		deg = degree(isCancel);
	}

	/**
	 * Genrates a poly nomial fraction in the form of 
	 * 
	 * a * x^0 + b * x^1 + ...
	 * -----------------------------
	 * c * x^0 + d * x^1 + ...
	 * 
	 * @param a coefficients of the numerator
	 * @param b coefficients of the denominator
	 * @param isCancel
	 */
	public PolynomialFraction(INumber[] a, INumber[] b, boolean isCancel)
	{
		numerator = new INumber[a.length];
		for (int i = 0; i < a.length; i++)
			numerator[i] = a[i];
		denominator = new INumber[b.length];
		for (int i = 0; i < b.length; i++)
			denominator[i] = b[i];
		deg = degree(isCancel);
	}

	
	/**
	 * Returns the degree of this polynomial (0 for the zero() polynomial)
	 * @param isCancel also simplifies the polynomial
	 * @return the degree of the current polynomial
	 */
	private int degree(boolean isCancel)
	{
		if (isCancel)
			simplify();
		int n = 0, d = 0;
		for (int i = numerator.length - 1; i >= 0; i--) {
			if (!numerator[i].isZero()) {
				n = i;
				break;
			}
		}

		if (denominator != null) {
			for (int i = denominator.length - 1; i >= 0; i--) {
				if (!denominator[i].isZero()) {
					d = i;
					break;
				}
			}
		}
		return n - d;
	}

	
	/**
	 * @return the degree of the polynomial
	 */
	public int getDegree()
	{
		return deg;
	}

	
	/**
	 * Returns the addition of the polynomials
	 * @param b the polynomial fraction to be added
	 * @return the sum of the fraction
	 */
	public PolynomialFraction plus(PolynomialFraction b)
	{
		PolynomialFraction a = this;
		PolynomialFraction c = new PolynomialFraction(Rational.ZERO);
		c.numerator = plus(times(a.numerator, b.denominator), times(b.numerator, a.denominator));
		c.denominator = times(a.denominator, b.denominator);
		c.deg = c.degree(false);
		return c;
	}

	/**
	 * Returns the result after subtraction
	 * @param b the polynomial fraction to be subtracted
	 * @return the result after subtracting b from the current polynomial fraction
	 */
	public PolynomialFraction minus(PolynomialFraction b)
	{
		PolynomialFraction a = this;
		PolynomialFraction c = new PolynomialFraction(Rational.ZERO);
		c.numerator = minus(times(a.numerator, b.denominator), times(b.numerator, a.denominator));
		c.denominator = times(a.denominator, b.denominator);
		c.deg = c.degree(false);
		return c;
	}

	/**
	 * Returns the multiplication of the polynomial fractions
	 * @param b the polynomial fraction to be multiplied with current one
	 * @return the multiplication of the polynomial fractions
	 */
	public PolynomialFraction times(PolynomialFraction b)
	{
		PolynomialFraction a = this;
		PolynomialFraction c = new PolynomialFraction(Rational.ZERO);
		c.numerator = times(a.numerator, b.numerator);
		c.denominator = times(a.denominator, b.denominator);
		c.deg = c.degree(true);
		return c;
	}

	/**
	 * Returns the division of the current one by b
	 * @param b the polynomial fraction that divides the current one
	 * @return the result after division
	 */
	public PolynomialFraction divides(PolynomialFraction b)
	{
		PolynomialFraction a = this;
		PolynomialFraction c = new PolynomialFraction(Rational.ZERO);
		c.numerator = times(a.numerator, b.denominator);
		c.denominator = times(a.denominator, b.numerator);
		c.deg = c.degree(true);
		return c;
	}

	/**
	 * @return the negation of the current polynomial fraction
	 */
	public PolynomialFraction negate()
	{
		PolynomialFraction a = this;
		PolynomialFraction c = new PolynomialFraction(Rational.ZERO);
		c.numerator = negate(a.numerator);
		c.denominator = new INumber[a.denominator.length];
		System.arraycopy(a.denominator, 0, c.denominator, 0, a.denominator.length);
		c.deg = c.degree(false);
		return c;
	}

	/**
	 * @param a coefficients of a polynomial
	 * @return array of the negation of the coefficients
	 */
	private static INumber[] negate(INumber[] a)
	{
		INumber[] _a = new INumber[a.length];
		for (int i = 0; i < a.length; i++)
			_a[i] = a[i].negate();
		return _a;
	}

	/**
	 * @param a first polynomial coefficients
	 * @param b second polynomial coefficients
	 * @return sum of the a and b
	 */
	public static INumber[] plus(INumber[] a, INumber[] b)
	{
		INumber[] result = fillZeroes(Math.max(a.length, b.length));
		for (int i = 0; i < a.length; i++)
			result[i] = result[i].plus(a[i]);
		for (int i = 0; i < b.length; i++)
			result[i] = result[i].plus(b[i]);
		return Helper.shrink(result);
	}

	/**
	 * @param a first polynomial coefficients
	 * @param b second polynomial coefficients
	 * @return the result after subtracting b from a
	 */
	public static INumber[] minus(INumber[] a, INumber[] b)
	{
		INumber[] result = fillZeroes(Math.max(a.length, b.length));
		for (int i = 0; i < a.length; i++)
			result[i] = result[i].plus(a[i]);
		for (int i = 0; i < b.length; i++)
			result[i] = result[i].minus(b[i]);
		return Helper.shrink(result);
	}

	/**
	 * @param a first polynomial coefficients
	 * @param b second polynomial coefficients
	 * @return multiplication of the a and b
	 */
	public static INumber[] times(INumber[] a, INumber[] b)
	{
		INumber[] result = fillZeroes(a.length + b.length - 1);
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < b.length; j++)
				result[i + j] = result[i + j].plus(a[i].timesby(b[j]));
		return Helper.shrink(result);
	}

	/**
	 * @param a first polynomial coefficients
	 * @param b factor
	 * @return multiplication of the polynomial a and factor b
	 */
	public static INumber[] singleTimes(INumber[] a, INumber factor)
	{
		INumber[] result = new INumber[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i].timesby(factor);
		}
		return Helper.shrink(result);
	}

	/**
	 * @param a first polynomial coefficients
	 * @param b factor
	 * @return the result after dividing the polynomial a by factor b
	 */
	public INumber[] singleDivides(INumber[] a, INumber factor)
	{
		INumber[] result = new INumber[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i].dividedby(factor);
		}
		return Helper.shrink(result);
	}

	
	/**
	 * @return true if the current polynomial is equivalent to zero
	 */
	public boolean isZero()
	{
		return isZero(numerator);
	}

	/**
	 * @return true if the given coefficients representing the polynomial equivalent to zero
	 */
	private static boolean isZero(INumber[] polyCoeff)
	{
		if (polyCoeff.length == 1 && polyCoeff[0].isZero())
			return true;
		return false;
	}

	/**
	 * @return true if the given coefficients representing the polynomial equivalent to one
	 */
	private static boolean isOne(INumber[] polyCoeff)
	{
		polyCoeff = Helper.shrink(polyCoeff);
		return polyCoeff.length == 1 && (polyCoeff[0].isOne());
	}

	/**
	 * @return true if the current polynomial is equivalent to one
	 */
	public boolean isOne()
	{
		if (numerator.length != denominator.length)
			return false;
		else {
			for (int i = 0; i < numerator.length; i++) {
				if (!numerator[i].equals(denominator[i]))
					return false;
			}
			return true;
		}
	}

	public boolean equals(PolynomialFraction a)
	{
		return Arrays.equals(a.numerator, this.numerator) && Arrays.equals(a.numerator, this.numerator);
	}

	
	/**
	 * @param numerator a polynomial
	 * @param denominator a polynomial
	 * @return simplified form of the polynomial fraction
	 */
	public DivisionResult euclidianDivision(INumber[] numerator, INumber[] denominator)
	{
		// Copy of the numerator
		INumber[] dividend = new INumber[numerator.length];
		System.arraycopy(numerator, 0, dividend, 0, numerator.length);
		// Denominator
		INumber[] divisor = denominator;
		// Degree of the quotient
		int degree = dividend.length - divisor.length;
		// Divisor length
		int divisor_size = divisor.length - 1;
		// Empty array to store the quotient
		INumber[] quotient = new INumber[degree + 1];

		for (int i = dividend.length - 1; i >= divisor_size; i--) {
			int quotientDeg = i - (divisor.length - 1);
			quotient[quotientDeg] = dividend[i].dividedby(divisor[divisor_size]);
			if (!quotient[quotientDeg].isZero())
				for (int j = divisor_size; j >= 0; j--)
					dividend[j + quotientDeg] = dividend[j + quotientDeg].minus(divisor[j].timesby(quotient[quotientDeg]));
		}

		// dividend = remainder
		return new DivisionResult(quotient, dividend);

	}

	public String toString()
	{
		String numerator = getString(this.numerator);
		String denominator = getString(this.denominator);
		if (denominator.equals("1") || numerator.equals("0"))
			return "(" + numerator + ")";
		return "(" + numerator + ")/(" + denominator + ")";

	}

	/**
	 * @param polynomial array of coefficients of a polynomial
	 * @return the string representation of the polynomial
	 */
	public static String getString(INumber[] polynomial)
	{
		int deg = polynomial.length - 1;
		if (deg == 0)
			return "" + polynomial[0];
		if (deg == 1)
			return polynomial[1] + "*x" + (polynomial[0].isZero() ? "" : " + " + polynomial[0]);
		String s = polynomial[deg] + "*x^" + deg;
		for (int i = deg - 1; i >= 0; i--) {
			if (polynomial[i].isZero())
				continue;
			else
				s = s + " + " + (polynomial[i]);
			if (i == 1)
				s = s + "*x";
			else if (i > 1)
				s = s + "*x^" + i;
		}
		return s;
	}

	/**
	 * rounds the polynomial fraction to the first_scale	
	 */
	public void round()
	{
		for (INumber _a : numerator)
			_a._firstScale();
		for (INumber _b : denominator)
			_b.firstScale();
	}

	/**
	 * Simplifies the polynomial fraction
	 */
	public void simplify()
	{
		round();

		INumber[] dividend = numerator;
		INumber[] divisor = denominator;

		if (isZero(numerator) || isOne(numerator) || isOne(denominator) || isZero(denominator))
			return;

		if (dividend.length < divisor.length) {
			INumber[] tmp = dividend;
			dividend = divisor;
			divisor = tmp;
		}

		if (denominator.length == 1) {
			if (!denominator[0].isOne()) {
				numerator = singleDivides(numerator, denominator[0]);
				denominator = new INumber[] { Rational.ONE };
			}
		} else {
			DivisionResult res = euclidianDivision(dividend, divisor);
			while (res.remainderDegree > 0 && dividend.length > divisor.length) {// && res.quotient.length < prevQuotientDeg) {
				dividend = divisor;
				divisor = res.remainder;
				res = euclidianDivision(dividend, divisor);
			}

			if (isZero(res.remainder)) {
				// there is a common factor we can remove
				numerator = euclidianDivision(numerator, divisor).quotient;
				denominator = euclidianDivision(denominator, divisor).quotient;
			}
		}

		// makes the coefficient of x^0 of the denominator equals to 1
		INumber val = this.denominator[0];
		if (!val.isOne()) {
			numerator = singleDivides(numerator, val);
			denominator = singleDivides(denominator, val);
		}
	}

	/**
	 * rounds the polynomial fraction to the first_scale	
	 */
	public void actualScale()
	{
		for (INumber val : numerator)
			val.firstScale();
		for (INumber val : denominator)
			val.firstScale();
	}

	/**
	 * rounds the polynomial fraction to the second_scale	
	 */
	public void tmpScale()
	{
		for (INumber val : numerator)
			val.secondScale();
		for (INumber val : denominator)
			val.secondScale();
	}

	
	/**
	 * @return polynomial fraction that represents one
	 */
	public static PolynomialFraction one()
	{
		return new PolynomialFraction(new INumber[] { Rational.ONE }, new INumber[] { Rational.ONE }, false);
	}

	/**
	 * @return polynomial fraction that represents zero
	 */
	public static PolynomialFraction zero()
	{
		return new PolynomialFraction(new INumber[] { Rational.ZERO }, new INumber[] { Rational.ONE }, false);
	}

	public boolean equals(Object a)
	{
		if (!(a instanceof PolynomialFraction))
			return false;
		PolynomialFraction _a = (PolynomialFraction) a;

		if (numerator.length != _a.numerator.length || denominator.length != _a.denominator.length)
			return false;

		for (int i = 0; i < numerator.length; i++) {
			if (!numerator[i].equals(_a.numerator[i]))
				return false;
		}

		for (int i = 0; i < denominator.length; i++) {
			if (!denominator[i].equals(_a.denominator[i]))
				return false;
		}

		return true;
	}

	/**
	 * @return the numerator of the polynomial fraction
	 */
	public PolynomialFraction getNumerator()
	{
		INumber[] _numerator = new INumber[numerator.length];
		System.arraycopy(numerator, 0, _numerator, 0, numerator.length);
		return new PolynomialFraction(_numerator);
	}

	/**
	 * @return the denominator of the polynomial fraction
	 */
	public PolynomialFraction getDenominator()
	{
		INumber[] _denominator = new INumber[denominator.length];
		System.arraycopy(denominator, 0, _denominator, 0, denominator.length);
		return new PolynomialFraction(_denominator);
	}

	/**
	 * @return the power of the polynomial fraction
	 */
	public PolynomialFraction power(int power)
	{
		PolynomialFraction pf = this;
		for (int i = 1; i < power; i++) {
			pf = pf.times(this);
		}
		return pf;
	}

	/**
	 * @param size length of the polynomial
	 * @return array of coefficients of the polynomial filled with zeroes
	 */
	public static INumber[] fillZeroes(int size)
	{
		INumber[] coef = new INumber[size];
		for (int i = 0; i < coef.length; i++)
			coef[i] = Rational.ZERO;
		return coef;
	}
}
