package recurrence.data_structure.result;

import recurrence.data_structure.numbers.INumber;
import recurrence.utils.Helper;

/**
 * 	This class stores the result of a polynomial division
 * 	@class DivisionResult	
 */
public class DivisionResult
{
	// The Quotient and the remainder 
	public INumber[] quotient, remainder;
	// The degree of both the quotient and the remainder
	public int remainderDegree, quotientDegree;

	/**
	 *  Constructor that stores the result of the polynomial division
	 *  @param quotient resulted quotient from the polynomial division
	 *  @param remainder resulted remainder from the polynomial division
	 */
	public DivisionResult(INumber[] quotient, INumber[] remainder)
	{
		this.quotient = quotient;
		this.remainder = Helper.shrink(remainder);
		this.quotientDegree = this.quotient.length - 1;
		this.remainderDegree = this.remainder.length - 1;

	}
}
