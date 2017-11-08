package recurrence.data_structure.recursion;

import recurrence.data_structure.numbers.PolynomialFraction;

public class OrdinaryGeneratingFunction
{
	public PolynomialFraction lhs;
	public PolynomialFraction[] rhs;

	public OrdinaryGeneratingFunction(PolynomialFraction lhs, PolynomialFraction[] rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public PolynomialFraction getB()
	{
		return lhs;
	}

	public PolynomialFraction GetA(int index)
	{
		return rhs[index];
	}

	public PolynomialFraction[] getA()
	{
		return rhs;
	}
}
