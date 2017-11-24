package recurrence.data_structure.numbers;

import recurrence.data_structure.result.ComplexDecimalResult;
import recurrence.data_structure.result.ComplexDoubleResult;

/**
 * 	This class that represents the complex numbers
 * 	@class Decimal	
 */
public class Complex implements INumber
{	
	public Decimal re; // Real number
	public Decimal img; // Imaginary number

	/**
	 *  Constructor that constructs the complex numbers 
	 *  @param re  the real number
	 *  @param img the imaginary number 
	 */
	public Complex(INumber re, INumber img)
	{
		if (re.getType() == INumber.DECIMAL && img.getType() == INumber.DECIMAL) {
			this.re = (Decimal) re;
			this.img = (Decimal) img;
		} else {
			throw new NumberFormatException("Invalid Decimal numbers");
		}
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#isZero()
	 */
	@Override
	public boolean isZero()
	{
		return re.isZero() && img.isZero();
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#isOne()
	 */
	@Override
	public boolean isOne()
	{
		return re.isOne() && img.isZero();
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#plus(recurrence.data_structure.numbers.INumber)
	 */
	@Override
	public INumber plus(INumber a)
	{
		if (a.getType() == INumber.COMPLEX) {
			Complex _a = (Complex) a;
			return getNumber(re.plus(_a.re), img.plus(_a.img));
		} else {
			return new Complex(re.plus(a), img);
		}
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#minus(recurrence.data_structure.numbers.INumber)
	 */
	@Override
	public INumber minus(INumber a)
	{
		if (a.getType() == INumber.COMPLEX) {
			Complex _a = (Complex) a;
			return getNumber(re.minus(_a.re), img.minus(_a.img));
		} else {
			return new Complex(re.minus(a), img);
		}
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#timesby(recurrence.data_structure.numbers.INumber)
	 */
	@Override
	public INumber timesby(INumber a)
	{
		INumber real;
		INumber imag;
		if (a.getType() == INumber.COMPLEX) {
			Complex _a = (Complex) a;
			real = re.timesby(_a.re).minus(img.timesby(_a.img));
			imag = re.timesby(_a.img).plus(img.timesby(_a.re));
		} else {
			real = re.timesby(a);
			imag = img.timesby(a);
		}
		return getNumber(real, imag);
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#dividedby(recurrence.data_structure.numbers.INumber)
	 */
	@Override
	public INumber dividedby(INumber a)
	{
		if (a.getType() == INumber.COMPLEX) {
			Complex _a = (Complex) a;
			return timesby(_a.reciprocal());
		} else {
			return getNumber(re.dividedby(a), img.dividedby(a));
		}
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#negate()
	 */
	@Override
	public INumber negate()
	{
		return getNumber(re.negate(), img.negate());
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#reciprocal()
	 */
	@Override
	public INumber reciprocal()
	{
		Decimal scale = (Decimal) re._pow(2).plus(img._pow(2));
		if (!scale.isZero())
			return getNumber(re.dividedby(scale), img.negate().dividedby(scale));
		else
			return scale;
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#doubleValue()
	 */
	@Override
	public ComplexDoubleResult doubleValue()
	{
		return new ComplexDoubleResult(re.doubleValue().getReal(), img.doubleValue().getReal());
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#doubleValue(int)
	 */
	@Override
	public ComplexDoubleResult doubleValue(int precision)
	{
		return new ComplexDoubleResult(re.doubleValue(precision).getReal(), img.doubleValue(precision).getReal());
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#getType()
	 */
	@Override
	public int getType()
	{
		return 2;
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#value()
	 */
	@Override
	public ComplexDecimalResult value()
	{
		return new ComplexDecimalResult(re, img);
	}

	public String toString()
	{
		return "(" + re.toString() + " + " + img.toString() + "i)";
	}

	public INumber zero()
	{
		return Decimal.zero();
	}

	public INumber one()
	{
		return Decimal.one();
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#simplify()
	 */
	@Override
	public void simplify()
	{
		re.simplify();
		img.simplify();
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#_simplify()
	 */
	@Override
	public INumber _simplify()
	{
		return new Complex(re._simplify(), img._simplify());
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#firstScale()
	 */
	@Override
	public void firstScale()
	{
		re.firstScale();
		img.firstScale();
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#_firstScale()
	 */
	@Override
	public INumber _firstScale()
	{
		return new Complex(re._firstScale(), img._firstScale());
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#secondScale()
	 */
	@Override
	public void secondScale()
	{
		re.secondScale();
		img.secondScale();
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#_secondScale()
	 */
	@Override
	public INumber _secondScale()
	{
		return new Complex(re._secondScale(), img._secondScale());
	}

	/* (non-Javadoc)
	 * @see recurrence.data_structure.numbers.INumber#_pow(int)
	 */
	@Override
	public Complex _pow(int n)
	{
		double _re = re.doubleValue().getReal();
		double _img = img.doubleValue().getReal();

		if (_img == 0) {
			return new Complex(new Decimal(_re)._pow(n), Decimal.zero());
		}
		// find r and theta
		double r = Math.sqrt((_re * _re) + (_img * _img));
		double theta = arctan(_re, _img);
		// find r^n and n*theta
		double r_n = Math.pow(r, n);
		double ntheta = n * theta;
		double __re = r_n * Math.cos(ntheta);
		double __img = r_n * Math.sin(ntheta);
		Complex result = new Complex(new Decimal(__re), new Decimal(__img));
		result.firstScale();
		return result;
	}

	/**
	 * Find the actan of the give two sides with length x and y
	 * @param x the first side
	 * @param y the second side
	 * @return arctan of x and y
	 */
	public double arctan(double x, double y)
	{
		double result = Math.atan((y / x));

		if (x > 0) {
			return result;
		} else if (x < 0) {
			if (y >= 0) {
				return result + Math.PI;
			} else {
				return result - Math.PI;
			}
		} else {
			if (y > 0) {
				return Math.PI / 2;
			} else if (y < 0) {
				return -Math.PI / 2;
			} else {
				System.err.println("Indeterminate arctan function");
				System.exit(0);
				return 0;
			}

		}
	}

	/**
	 * Generates an INumber based on the inputs
	 * @param real the real part
	 * @param imag the imaginary part
	 * @return returns an INumber
	 */
	public INumber getNumber(INumber real, INumber imag)
	{
		if (!imag.isZero())
			return new Complex(real, imag);
		else
			return real;
	}

	/**
	 * Compares if two complex numbers are equals 
	 */
	public boolean equals(Object a)
	{
		if (!(a instanceof Complex))
			return false;

		Complex _a = (Complex) a;
		return this.re.equals(_a.re) && this.img.equals(_a.img);
	}
}
