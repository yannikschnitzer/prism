package recurrence.data_structure.recursion;

import recurrence.data_structure.numbers.PolynomialFraction;

/**
 * 	This class represents the ordinary generating function
 * 	@class Decimal	
 */
public class OrdinaryGeneratingFunction
{
	public PolynomialFraction lhs; // left hand side of the ogf
	public PolynomialFraction[] rhs; // right hand side of the ogf

	/**
	 *  Constructor that constructs the ogf data structure 
	 *  @param lhs left hand side of the ogf
	 *  @param right hand side of the ogf
	 */
	public OrdinaryGeneratingFunction(PolynomialFraction lhs, PolynomialFraction[] rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}

	/**
	 * @return the lhs of the ogf
	 */
	public PolynomialFraction getB()
	{
		return lhs;
	}

	/**
	 * @param index represents the position of the variable
	 * @return the the polynomial fraction belongs the corresponding index in the rhs
	 */
	public PolynomialFraction GetA(int index)
	{
		return rhs[index];
	}

	/**
	 * @return the array of polynomial fractions in the rhs of the ogf
	 */
	public PolynomialFraction[] getA()
	{
		return rhs;
	}
}
