package recurrence.data_structure.numbers;

import java.math.BigInteger;

import param.BigRational;
import recurrence.data_structure.result.ComplexDecimalResult;
import recurrence.data_structure.result.ComplexDoubleResult;

/**
 * 	This class that represents the rational numbers
 * 	@class Rational	
 */
public class Rational implements INumber
{
	// Stores the current rational number
	private BigRational rational;
	// Represents the ONE as rational
	public static Rational ONE = new Rational(1);
	// Represents the ZERO as rational
	public static Rational ZERO = new Rational(0);

	/**
	 *  Constructor for rational numbers
	 *  @param num represents the numerator as long
	 *  @param den represents the denominator as long
	 */
	public Rational(long num, long den)
	{
		this(num, den, true);
	}

	/**
	 *  Constructor for rational numbers
	 *  @param num represents the numerator as long
	 *  @param den represents the denominator as long
	 *  @param cancel represents whether the given rational should be simplified or not
	 */
	public Rational(long num, long den, boolean cancel)
	{
		this.rational = new BigRational(BigInteger.valueOf(num), BigInteger.valueOf(den), true);
	}

	/**
	 *  Constructor for rational numbers that constructs the rational number from the given BigRational
	 *  @param rational represents the rational number to be constructed
	 */
	public Rational(BigRational rational)
	{
		this.rational = rational;
	}

	/**
	 *  Constructor for rational numbers that constructs the rational number with denominator as one
	 *  @param num represents the numerator
	 */
	public Rational(long num)
	{
		this(num, 1);
	}

	/**
	 *  Roundup the current rational number
	 */
	public void roundup()
	{
		// Decides for how many decimal places that the new rational should be equal to the current one
		long precision = 6;
		// Any number below the epsilon is considered as zero
		double epsilon = Math.pow(10, -5);
		// Double value of the current rational number
		double value = rational.doubleValue();

		if (Math.abs(value) < epsilon) {
			rational = BigRational.ZERO;
		} else if (getDigits(rational.getNum()) > precision || getDigits(rational.getDen()) > precision) {
			long updatedVal = Math.round(value * (long) Math.pow(10, precision));
			rational = new BigRational(updatedVal, (long) Math.pow(10, precision)).cancel();
		}
	}

	@Override
	public String toString()
	{
		return rational.toString();
	}

	/**
	 *  Converts the current rational number into a double
	 */
	public double toDouble()
	{
		return rational.doubleValue();
	}

	/**
	 * 	Checks whether current number is zero or not 
	 * 	@return true if the the current number is zero and false otherwise. 
	 */
	@Override
	public boolean isZero()
	{
		return rational.isZero();
	}

	/**
	 * 	Checks whether current number is one or not 
	 * 	@return true if the the current number is one and false otherwise. 
	 */
	@Override
	public boolean isOne()
	{
		return rational.isOne();
	}

	/**
	 * 	Subtracts the given number from the current number
	 * 	@param  a	the number to be subtracted from the current number
	 * 	@return the resulting number after subtracting a from the current number 
	 */
	@Override
	public INumber plus(INumber a)
	{
		if (a instanceof Rational) {
			return new Rational(((Rational) a).rational.add(this.rational));
		} else if (a instanceof Decimal) {
			return new Decimal(rational.doubleValue()).plus(a);
		} else
			return a.plus(this);
	}

	/**
	 * 	Subtracts the given number from the current number
	 * 	@param  a	the number to be subtracted from the current number
	 * 	@return the resulting number after subtracting a from the current number 
	 */
	@Override
	public INumber minus(INumber a)
	{
		if (a instanceof Rational) {
			return new Rational(this.rational.subtract(((Rational) a).rational));
		} else if (a instanceof Decimal) {
			return new Decimal(rational.doubleValue()).minus(a);
		} else
			return a.negate().plus(this);
	}

	/**
	 * 	Multiplies the given number with the current number
	 * 	@param  a	the number to be multiplied with the current number
	 * 	@return the resulting number after multiplying the current number by a 
	 */
	@Override
	public INumber timesby(INumber a)
	{
		if (a instanceof Rational) {
			return new Rational(this.rational.multiply(((Rational) a).rational));
		} else if (a instanceof Decimal) {
			return new Decimal(rational.doubleValue()).timesby(a);
		} else
			return a.timesby(this);
	}

	/**
	 * 	Divides the current number by the give number
	 * 	@param  a	the number that divides the current number
	 * 	@return the resulting number after dividing the current number by a 
	 */
	@Override
	public INumber dividedby(INumber a)
	{
		if (a instanceof Rational) {
			return new Rational(this.rational.divide(((Rational) a).rational));
		} else if (a instanceof Decimal) {
			return new Decimal(rational.doubleValue()).dividedby(a);
		} else
			return a.reciprocal().timesby(this);
	}

	/**
	 * 	Negates the current number
	 * 	@return the resulting number after negating the current number 
	 */
	@Override
	public INumber negate()
	{
		return new Rational(this.rational.negate());
	}

	/**
	 * 	Reciprocates the current number
	 * 	@return the resulting number after reciprocating the current number 
	 */
	@Override
	public INumber reciprocal()
	{
		return new Rational(BigRational.ONE.divide(this.rational));
	}

	/**
	 * 	Returns the current number in the form of ComplexDoubleResult with default precision of 2
	 * 	@return the complex double result of the current number with precision of 2 
	 */
	@Override
	public ComplexDoubleResult doubleValue()
	{
		return new ComplexDoubleResult(rational.doubleValue());
	}

	/**
	 * 	Returns the current number in the form of ComplexDoubleResult with the given precision
	 *  @param precision represents the precision of the current number when converting it into double
	 * 	@return the complex double result of the current number with the given precision
	 */
	@Override
	public ComplexDoubleResult doubleValue(int precision)
	{
		return doubleValue();
	}

	/**
	 * 	Returns the type of the current number (they are defined in the top of this class)
	 * 	@return the type of the current number
	 */
	@Override
	public int getType()
	{
		return INumber.RATIONAL;
	}

	/**
	 * 	Returns the current number as ComplexDecimalResult
	 * 	@return the ComplexDecimal form of the current number
	 */
	@Override
	public ComplexDecimalResult value()
	{
		return null; // This method is unused for this class
	}

	@Override
	public void simplify()
	{
		// This method is unused for this class
	}

	@Override
	public INumber _simplify()
	{
		return this; // This method is unused for this class
	}

	@Override
	public void firstScale()
	{
		// This method is unused for this class
	}

	@Override
	public INumber _firstScale()
	{
		return this; // This method is unused for this class
	}

	@Override
	public void secondScale()
	{
		// This method is unused for this class
	}

	@Override
	public INumber _secondScale()
	{
		return this; // This method is unused for this class
	}

	/**
	 * 	Returns the power of the current number
	 * 	@param exponent represents the power of the current number to be raised
	 * 	@return return the current number ^ exponent
	 */
	@Override
	public INumber _pow(int exponent)
	{
		return new Rational(rational.pow(exponent));
	}

	/**
	 * 	Returns the number of digits in the given BigInteger
	 * 	@param the number whose digits needs to be found
	 * 	@return returns the number of digits of the given number 
	 */
	private int getDigits(BigInteger huge)
	{
		int digits = 0;
		int bits = huge.bitLength();
		// Serious reductions.
		while (bits > 4) {
			// 4 > log[2](10) so we should not reduce it too far.
			int reduce = bits / 4;
			// Divide by 10^reduce
			huge = huge.divide(BigInteger.TEN.pow(reduce));
			// Removed that many decimal digits.
			digits += reduce;
			// Recalculate bitLength
			bits = huge.bitLength();
		}
		// Now 4 bits or less - add 1 if necessary.
		if (huge.intValue() > 9) {
			digits += 1;
		}
		return digits + 1;
	}
}
