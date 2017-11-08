package recurrence.data_structure.numbers;

import recurrence.data_structure.result.ComplexDecimalResult;
import recurrence.data_structure.result.ComplexDoubleResult;

/**
 * 	This interface defines the methods for any type of numbers
 * 	@interface INumber	
 */
public interface INumber
{
	// All type of numbers that implements this interface
	public static final int DECIMAL = 1;
	public static final int COMPLEX = 2;
	public static final int RATIONAL = 3;

	/**
	 * 	Checks whether current number is zero or not 
	 * 	@return true if the the current number is zero and false otherwise. 
	 */
	public boolean isZero();

	/**
	 * 	Checks whether current number is one or not 
	 * 	@return true if the the current number is one and false otherwise. 
	 */
	public boolean isOne();

	/**
	 * 	Adds the current number with the given number
	 * 	@param  a	the number to be added with the current number
	 * 	@return addition of the current number and the a 
	 */
	public INumber plus(INumber a);

	/**
	 * 	Subtracts the given number from the current number
	 * 	@param  a	the number to be subtracted from the current number
	 * 	@return the resulting number after subtracting a from the current number 
	 */
	public INumber minus(INumber a);

	/**
	 * 	Multiplies the given number with the current number
	 * 	@param  a	the number to be multiplied with the current number
	 * 	@return the resulting number after multiplying the current number by a 
	 */
	public INumber timesby(INumber a);

	/**
	 * 	Divides the current number by the give number
	 * 	@param  a	the number that divides the current number
	 * 	@return the resulting number after dividing the current number by a 
	 */
	public INumber dividedby(INumber a);

	/**
	 * 	Negates the current number
	 * 	@return the resulting number after negating the current number 
	 */
	public INumber negate();

	/**
	 * 	Reciprocates the current number
	 * 	@return the resulting number after reciprocating the current number 
	 */
	public INumber reciprocal();

	/**
	 * 	Returns the current number in the form of ComplexDoubleResult with default precision of 2
	 * 	@return the complex double result of the current number with precision of 2 
	 */
	public ComplexDoubleResult doubleValue();

	/**
	 * 	Returns the current number in the form of ComplexDoubleResult with the given precision
	 *  @param precision represents the precision of the current number when converting it into double
	 * 	@return the complex double result of the current number with the given precision
	 */
	public ComplexDoubleResult doubleValue(int precision);

	/**
	 * 	Returns the type of the current number (they are defined in the top of this class)
	 * 	@return the type of the current number
	 */
	public int getType();

	/**
	 * 	Returns the current number as ComplexDecimalResult
	 * 	@return the ComplexDecimal form of the current number
	 */
	public ComplexDecimalResult value();

	/**
	 * 	Simplifies the current number (especially for the rational number)
	 */
	public void simplify();

	/**
	 * 	Returns the simplified form of the current number (especially for the rational number)
	 * 	@return returns the simplified current number 
	 */
	public INumber _simplify();

	/**
	 * 	Rounds up the current number up to the given firstScale
	 */
	public void firstScale();

	/**
	 * 	Returns the rounded up current number up to the given firstScale
	 * 	@return rounded up current number to the value of firstScale
	 */
	public INumber _firstScale();

	/**
	 * 	Rounds up the current number up to the given secondScale
	 */
	public void secondScale();

	/**
	 * 	Returns the rounded up current number up to the given secondScale
	 * 	@return rounded up current number to the value of secondScale
	 */
	public INumber _secondScale();

	/**
	 * 	Returns the power of the current number
	 * 	@param exponent represents the power of the current number to be raised
	 * 	@return return the current number ^ exponent
	 */
	public INumber _pow(int exponent);
}
