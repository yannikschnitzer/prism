package recurrence.data_structure.numbers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import recurrence.data_structure.result.ComplexDecimalResult;
import recurrence.data_structure.result.ComplexDoubleResult;
import recurrence.utils.Helper;

/**
 * 	This class that represents the decimal numbers
 * 	@class Decimal	
 */
public class Decimal implements INumber
{
	// Stores the current decimal number
	public BigDecimal decimal;
	// Various scales has been used to maintain the precision
	public static final int FIRST_SCALE = 10;
	public static final int SECOND_SCALE = FIRST_SCALE + 10;
	public static final int THIRD_SCALE = SECOND_SCALE + 20;
	public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

	// Represents the number TWO as big decimal
	public static final BigDecimal TWO = BigDecimal.valueOf(2);

	/**
	 *  Constructor that constructs the decimal numbers from the given string 
	 *  @param decimal represents the current decimal as string 
	 */
	public Decimal(String decimal)
	{
		this(new BigDecimal(decimal));
	}

	/**
	 *  Constructor that constructs the decimal numbers from the given integer 
	 *  @param decimal represents the current decimal as integer 
	 */
	public Decimal(int decimal)
	{
		this((double) decimal);
	}

	/**
	 *  Constructor that constructs the decimal numbers from the given double 
	 *  @param decimal represents the current decimal as double 
	 */
	public Decimal(double decimal)
	{
		this(decimal, false);
	}

	/**
	 *  Constructor that constructs the decimal numbers from the given BigDecimal 
	 *  @param decimal represents the current decimal as BigDecimal 
	 */
	public Decimal(BigDecimal decimal)
	{
		this.decimal = decimal;
		simplify();
	}

	/**
	 *  Constructor that constructs the decimal numbers from the given double later rounds it up to the firstScale 
	 *  @param decimal represents the current decimal as double
	 *  @param isFirst represents whether the given number should be roundup to the first scale or not.  
	 */
	public Decimal(double decimal, boolean isFirst)
	{
		this(BigDecimal.valueOf(decimal));
		if (isFirst)
			firstScale();
	}

	/**
	 * 	Checks whether current number is zero or not 
	 * 	@return true if the the current number is zero and false otherwise. 
	 */
	@Override
	public boolean isZero()
	{
		return Helper.setScale(decimal).compareTo(BigDecimal.ZERO) == 0;
	}

	/**
	 * 	Checks whether current number is one or not 
	 * 	@return true if the the current number is one and false otherwise. 
	 */
	@Override
	public boolean isOne()
	{
		return Helper.setScale(decimal).compareTo(BigDecimal.ONE) == 0;
	}

	/**
	 * 	Adds the current number with the given number
	 * 	@param  a	the number to be added with the current number
	 * 	@return addition of the current number and the a 
	 */
	@Override
	public INumber plus(INumber a)
	{
		if (a.getType() == INumber.DECIMAL) {
			Decimal _a = (Decimal) a;
			BigDecimal _num = decimal.add(_a.decimal);
			return new Decimal(_num);
		} else {
			return a.plus(this);
		}
	}

	/**
	 * 	Subtracts the given number from the current number
	 * 	@param  a	the number to be subtracted from the current number
	 * 	@return the resulting number after subtracting a from the current number 
	 */
	@Override
	public INumber minus(INumber a)
	{
		if (a.getType() == INumber.DECIMAL) {
			Decimal _a = (Decimal) a;
			BigDecimal _num = decimal.subtract(_a.decimal);
			return new Decimal(_num);
		} else {
			return a.negate().plus(this);
		}
	}

	/**
	 * 	Multiplies the given number with the current number
	 * 	@param  a	the number to be multiplied with the current number
	 * 	@return the resulting number after multiplying the current number by a 
	 */
	@Override
	public INumber timesby(INumber a)
	{
		if (a.getType() == INumber.DECIMAL) {
			Decimal _a = (Decimal) a;
			BigDecimal _num = _a.decimal.multiply(decimal);
			return new Decimal(_num);
		} else {
			return a.timesby(this);
		}
	}

	/**
	 * 	Divides the current number by the give number
	 * 	@param  a	the number that divides the current number
	 * 	@return the resulting number after dividing the current number by a 
	 */
	@Override
	public INumber dividedby(INumber a)
	{
		if (a.getType() == INumber.DECIMAL) {
			Decimal _a = (Decimal) a;
			BigDecimal _num = Helper.divide(decimal, _a.decimal);
			return new Decimal(_num);
		} else {
			return a.reciprocal().timesby(this);
		}
	}

	/**
	 * 	Negates the current number
	 * 	@return the resulting number after negating the current number 
	 */
	@Override
	public INumber negate()
	{
		return new Decimal(decimal.negate());
	}

	/**
	 * 	Reciprocates the current number
	 * 	@return the resulting number after reciprocating the current number 
	 */
	@Override
	public INumber reciprocal()
	{
		BigDecimal _num = Helper.divide(BigDecimal.ONE, decimal);
		return new Decimal(_num);
	}

	/**
	 * 	Returns the current number in the form of ComplexDoubleResult with default precision of 2
	 * 	@return the complex double result of the current number with precision of 2 
	 */
	@Override
	public ComplexDoubleResult doubleValue()
	{
		return doubleValue(2);
	}

	/**
	 * 	Returns the current number in the form of ComplexDoubleResult with the given precision
	 *  @param precision represents the precision of the current number when converting it into double
	 * 	@return the complex double result of the current number with the given precision
	 */
	@Override
	public ComplexDoubleResult doubleValue(int precision)
	{
		simplify();
		return new ComplexDoubleResult(decimal.doubleValue());
	}

	/**
	 * 	Returns the type of the current number (they are defined in the top of this class)
	 * 	@return the type of the current number
	 */
	@Override
	public int getType()
	{
		return 1;
	}

	/**
	 * 	Returns the current number as ComplexDecimalResult
	 * 	@return the ComplexDecimal form of the current number
	 */
	@Override
	public ComplexDecimalResult value()
	{
		return new ComplexDecimalResult(this);
	}

	public String toString()
	{
		simplify();
		return isZero() ? "0" : decimal.toString();
	}

	/**
	 * 	Returns Decimal version of Zero
	 *  @return returns Decimal Zero
	 */
	public static Decimal zero()
	{
		return new Decimal(0);
	}

	/**
	 * 	Returns Decimal version of One
	 *  @return returns Decimal One
	 */
	public static Decimal one()
	{
		return new Decimal(1);
	}

	/**
	 * 	Simplifies the current number (especially for the rational number)
	 */
	@Override
	public void simplify()
	{
		if (decimal.compareTo(BigDecimal.ZERO) != 0) {
			decimal = Helper.setScale(decimal);
		}
	}

	/**
	 * 	Returns the simplified form of the current number (especially for the rational number)
	 * 	@return returns the simplified current number 
	 */
	@Override
	public INumber _simplify()
	{
		if (decimal.compareTo(BigDecimal.ZERO) != 0) {
			return new Decimal(Helper.setScale(decimal));
		} else
			return new Decimal(decimal);
	}

	/**
	 * 	Rounds up the current number up to the given firstScale
	 */
	@Override
	public void firstScale()
	{
		decimal = decimal.setScale(FIRST_SCALE, ROUNDING_MODE).stripTrailingZeros();
	}

	/**
	 * 	Returns the rounded up current number up to the given firstScale
	 * 	@return rounded up current number to the value of firstScale
	 */
	@Override
	public INumber _firstScale()
	{
		return new Decimal(decimal.setScale(FIRST_SCALE, ROUNDING_MODE).stripTrailingZeros());
	}

	/**
	 * 	Rounds up the current number up to the given secondScale
	 */
	@Override
	public void secondScale()
	{
		decimal = decimal.setScale(SECOND_SCALE, ROUNDING_MODE).stripTrailingZeros();
	}

	/**
	 * 	Returns the rounded up current number up to the given secondScale
	 * 	@return rounded up current number to the value of secondScale
	 */
	@Override
	public INumber _secondScale()
	{
		return new Decimal(decimal.setScale(SECOND_SCALE, ROUNDING_MODE).stripTrailingZeros());
	}

	@Override
	public boolean equals(Object a)
	{
		if (!(a instanceof Decimal))
			return false;

		Decimal _a = (Decimal) a;
		Decimal b = (Decimal) _a._simplify();
		Decimal c = (Decimal) this._simplify();

		return b.decimal.compareTo(c.decimal) == 0;
	}

	/**
	 * 	Returns the power of the current number
	 * 	@param exponent represents the power of the current number to be raised
	 * 	@return return the current number ^ exponent
	 */
	@Override
	public INumber _pow(int exponent)
	{
		return new Decimal(Math.pow(decimal.doubleValue(), exponent));
	}

	//  MIGHT BE NEEDED LATER
	//  =====================
	//	public Decimal abs()
	//	{
	//		if (decimal.compareTo(BigDecimal.ZERO) < 0)
	//			return (Decimal) this.negate();
	//		return new Decimal(this.decimal);
	//	}

}
