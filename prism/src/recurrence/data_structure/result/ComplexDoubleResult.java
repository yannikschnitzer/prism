package recurrence.data_structure.result;

/**
 * 	This class stores the complex numbers as double
 * 	@class ComplexDoubleResult	
 */
public class ComplexDoubleResult
{
	// Store both the real and imaginary number
	private double real;
	private double imaginary;

	/**
	 *  Constructor for non-complex numbers
	 *  @param real represents the real number
	 */
	public ComplexDoubleResult(double real)
	{
		this(real, 0);
	}

	/**
	 *  Constructor for complex numbers
	 *  @param real represents the real number
	 *  @param imaginary represents the imaginary number
	 */
	public ComplexDoubleResult(double real, double imaginary)
	{
		this.real = real;
		this.imaginary = imaginary;
	}

	/**
	 *  Returns the real part of the complex number
	 *  @return the real number
	 */
	public double getReal()
	{
		return real;
	}

	/**
	 *  Sets the real part of the complex number
	 *  @param real represents the real number
	 */
	public void setReal(double real)
	{
		this.real = real;
	}

	/**
	 *  Returns the imaginary part of the complex number
	 *  @return the imaginary number
	 */
	public double getImaginary()
	{
		return imaginary;
	}

	/**
	 *  Sets the imaginary part of the complex number
	 *  @param the imaginary number
	 */
	public void setImaginary(double imaginary)
	{
		this.imaginary = imaginary;
	}

	/**
	 *  Returns whether the current number is complex or not
	 *  @return true if the current number is a complex number
	 */
	public boolean isComplex()
	{
		return imaginary != 0;
	}
}
