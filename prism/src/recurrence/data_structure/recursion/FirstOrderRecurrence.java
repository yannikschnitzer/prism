package recurrence.data_structure.recursion;

import java.util.ArrayList;
import java.util.List;

import param.BigRational;
import recurrence.data_structure.numbers.INumber;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.data_structure.numbers.Rational;

/**
 * 	This class that represents the first order recurrence relationship
 * 	@class FirstOrderRecurrence	
 */
public class FirstOrderRecurrence
{

	List<BigRational> coeffs; // coefficients
	double[] _coeffs; // coefficients as doubles
	BigRational baseVal; // base of the recurrence relationship
	int numVars; // number of variables in the relationship
	int subjectVarIndex; // index of the subject variable of the recurrence relationship

	// OGF function
	PolynomialFraction lhs; // constants
	PolynomialFraction[] rhs; // variables

	/**
	 * Constructs the first order recurrence relation
	 * @param coeffs coefficients of the variables in the recurrence relationship
	 * @param baseVal base condition
	 * @param varIndex index of the subject variable
	 */
	public FirstOrderRecurrence(List<BigRational> coeffs, BigRational baseVal, int varIndex)
	{
		this.coeffs = coeffs;
		_coeffs = new double[coeffs.size()];

		for (int i = 0; i < coeffs.size(); i++) {
			_coeffs[i] = coeffs.get(i).doubleValue();
		}

		this.baseVal = baseVal;
		subjectVarIndex = varIndex;
		numVars = coeffs.size() - 1;
		convertToGF();
	}

	/**
	 * @return true if the recurrence relationship only relies on its previous terms and not other variables
	 */
	public boolean isIndependent()
	{
		boolean isNotIndependent = false;
		for (int i = 0; i < coeffs.size() - 1; i++) {
			if (!coeffs.get(i).isZero())
				isNotIndependent = true;
		}
		return !isNotIndependent;
	}

	/**
	 * @return index of the subject variable
	 */
	public int getVarIndex()
	{
		return subjectVarIndex;
	}

	/**
	 * @return number of variables in the recurrence relation
	 */
	public int getNumVars()
	{
		return this.numVars;
	}

	/**
	 * @return the deep copy of the current recurrent relation
	 */
	public FirstOrderRecurrence deepCopy()
	{
		return new FirstOrderRecurrence(new ArrayList<BigRational>(coeffs), baseVal, subjectVarIndex);
	}

	/**
	 * Removes a specific variable out of the equation
	 * @param varIndex the index of the variable to be removed
	 * @return the values corresponding the index of the variable
	 */
	public BigRational removeCoeff(int varIndex)
	{
		if (subjectVarIndex > varIndex)
			subjectVarIndex--;
		numVars--;
		return coeffs.remove(varIndex);
	}

	/**
	 * Adds a value to the coefficient
	 * @param varIndex index of the coefficient of the variable
	 * @param val value
	 */
	public void addCoeff(int varIndex, BigRational val)
	{
		coeffs.set(varIndex, coeffs.get(varIndex).add(val));
	}

	/**
	 * Sets a value to the coefficient
	 * @param varIndex index of the coefficient of the variable
	 * @param val value
	 */
	public void setCoeff(int varIndex, BigRational val)
	{
		coeffs.set(varIndex, val);
	}

	/**
	 * Adds a value to the last coefficient (Special case of the addCoeff)
	 * @param val value
	 */
	public void addLastCoeff(BigRational val)
	{
		addCoeff(coeffs.size() - 1, val);
	}

	/**
	 * @return the value of the last coefficient
	 */
	public BigRational getLastCoeff()
	{
		return coeffs.get(coeffs.size() - 1);
	}

	/**
	 * @return the ordinary generating function for the current recurrence relation
	 */
	public OrdinaryGeneratingFunction getOGFForm()
	{
		convertToGF();
		return new OrdinaryGeneratingFunction(lhs, rhs);
	}

	/**
	 * @return the array of coefficients of the recurrence equation
	 */
	public double[] getCoeffs()
	{
		return this._coeffs;
	}

	/**
	 * Evaluates the recurrence relation based on the valuations of the variables
	 * @param values array of variable value
	 * @return the value after evaluation
	 */
	public double evaluate(double[] values)
	{
		double sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += (values[i] * _coeffs[i]);
		}
		return sum + _coeffs[_coeffs.length - 1];
	}

	/**
	 * Converts the recurrence relation into Ordinary generating function
	 */
	public void convertToGF()
	{
		rhs = new PolynomialFraction[numVars];
		// Initial Step => Transform L.H.S
		lhs = new PolynomialFraction(new Rational(baseVal.negate()));
		rhs[subjectVarIndex] = new PolynomialFraction(Rational.ONE.negate());

		for (int i = 0; i < coeffs.size(); i++) {
			if (i == coeffs.size() - 1) { // R.H.S constants
				if (!coeffs.get(i).isZero()) {
					PolynomialFraction rhsConstantGF = new PolynomialFraction(new INumber[] { Rational.ZERO, new Rational(coeffs.get(i)) }, new INumber[] {
							Rational.ONE, Rational.ONE.negate() }, false);
					lhs = lhs.minus(rhsConstantGF);
				}
			} else { // R.H.S variables
				PolynomialFraction rhsVarGF = new PolynomialFraction(new INumber[] { Rational.ZERO, new Rational(coeffs.get(i)) });
				if (i == subjectVarIndex) {
					rhs[i] = rhs[i].plus(rhsVarGF);
				} else {
					rhs[i] = rhsVarGF;
				}
			}
		}

	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder(lhs + " = ");
		for (int i = 0; i < rhs.length - 1; i++) {
			sb.append(rhs[i] + " + ");
		}
		sb.append(rhs[rhs.length - 1]);

		sb.append("\nCorresponding Recurrence Relations =>\n\t (baseVal=" + baseVal + ")  (subjectVarIndex=" + subjectVarIndex + ")\n\t");
		for (int i = 0; i < coeffs.size() - 1; i++) {
			sb.append(coeffs.get(i) + " + ");
		}
		sb.append(coeffs.get(coeffs.size() - 1) + "\n\n");

		return sb.toString();
	}

	/**
	 * @return the baseVal
	 */
	public BigRational getBaseVal()
	{
		return baseVal;
	}
}
