package recurrence.data_structure.numbers;

import recurrence.data_structure.result.ComplexDecimalResult;
import recurrence.data_structure.result.ComplexDoubleResult;

public class Complex implements INumber
{
	public Decimal re;
	public Decimal img;

	public Complex(INumber re, INumber img)
	{
		if (re.getType() == INumber.DECIMAL && img.getType() == INumber.DECIMAL) {
			this.re = (Decimal) re;
			this.img = (Decimal) img;
		} else {
			throw new NumberFormatException("Invalid Decimal numbers");
		}
	}

	@Override
	public boolean isZero()
	{
		return re.isZero() && img.isZero();
	}

	@Override
	public boolean isOne()
	{
		return re.isOne() && img.isZero();
	}

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

	@Override
	public INumber negate()
	{
		return getNumber(re.negate(), img.negate());
	}

	@Override
	public INumber reciprocal()
	{
		Decimal scale = (Decimal) re._pow(2).plus(img._pow(2));
		if (!scale.isZero())
			return getNumber(re.dividedby(scale), img.negate().dividedby(scale));
		else
			return scale;
	}

	@Override
	public ComplexDoubleResult doubleValue()
	{
		return new ComplexDoubleResult(re.doubleValue().getReal(), img.doubleValue().getReal());
	}

	@Override
	public ComplexDoubleResult doubleValue(int precision)
	{
		return new ComplexDoubleResult(re.doubleValue(precision).getReal(), img.doubleValue(precision).getReal());
	}

	@Override
	public int getType()
	{
		return 2;
	}

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

	@Override
	public void simplify()
	{
		re.simplify();
		img.simplify();
	}

	@Override
	public INumber _simplify()
	{
		return new Complex(re._simplify(), img._simplify());
	}

	@Override
	public void firstScale()
	{
		re.firstScale();
		img.firstScale();
	}

	@Override
	public INumber _firstScale()
	{
		return new Complex(re._firstScale(), img._firstScale());
	}

	@Override
	public void secondScale()
	{
		re.secondScale();
		img.secondScale();
	}

	@Override
	public INumber _secondScale()
	{
		return new Complex(re._secondScale(), img._secondScale());
	}

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

	public INumber getNumber(INumber real, INumber imag)
	{
		if (!imag.isZero())
			return new Complex(real, imag);
		else
			return real;
	}

	public boolean equals(Object a)
	{
		if (!(a instanceof Complex))
			return false;

		Complex _a = (Complex) a;
		return this.re.equals(_a.re) && this.img.equals(_a.img);
	}
}
