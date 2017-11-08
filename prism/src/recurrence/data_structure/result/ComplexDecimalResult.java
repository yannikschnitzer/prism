package recurrence.data_structure.result;

import recurrence.data_structure.numbers.Decimal;

/**
 * 	This class stores the complex numbers as decimal
 * 	@class ComplexDecimalResult	
 */
public class ComplexDecimalResult
{
	// Store both the real and imaginary number
	private Decimal real;
	private Decimal imaginary;

	/**
	 *  Constructor for non-complex numbers
	 *  @param real represents the real number
	 */
	public ComplexDecimalResult(Decimal real)
	{
		this(real, Decimal.zero());
	}

	/**
	 *  Constructor for complex numbers
	 *  @param real represents the real number
	 *  @param imaginary represents the imaginary number
	 */
	public ComplexDecimalResult(Decimal real, Decimal imaginary)
	{
		this.real = real;
		this.imaginary = imaginary;
	}

	/**
	 *  Returns the real part of the complex number
	 *  @return the real number
	 */
	public Decimal getReal()
	{
		return real;
	}

	/**
	 *  Sets the real part of the complex number
	 *  @param real represents the real number
	 */
	public void setReal(Decimal real)
	{
		this.real = real;
	}

	/**
	 *  Returns the imaginary part of the complex number
	 *  @return the imaginary number
	 */
	public Decimal getImaginary()
	{
		return imaginary;
	}

	/**
	 *  Sets the imaginary part of the complex number
	 *  @param the imaginary number
	 */
	public void setImaginary(Decimal imaginary)
	{
		this.imaginary = imaginary;
	}

	/**
	 *  Returns whether the current number is complex or not
	 *  @return true if the current number is a complex number
	 */
	public boolean isComplex()
	{
		return !imaginary.isZero();
	}
}
