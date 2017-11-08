package recurrence.data_structure.recursion;

import java.util.ArrayList;
import java.util.List;

import param.BigRational;
import recurrence.data_structure.numbers.INumber;
import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.data_structure.numbers.Rational;

public class FirstOrderRecurrence
{

	List<BigRational> coeffs;
	double[] _coeffs;
	BigRational baseVal;
	int numVars;
	int subjectVarIndex;

	// OGF function
	PolynomialFraction lhs; // constants
	PolynomialFraction[] rhs; // variables

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

	public boolean isIndependent()
	{
		boolean isNotIndependent = false;
		for (int i = 0; i < coeffs.size() - 1; i++) {
			if (!coeffs.get(i).isZero())
				isNotIndependent = true;
		}
		return !isNotIndependent;
	}

	public int getVarIndex()
	{
		return subjectVarIndex;
	}

	public int getNumVars()
	{
		return this.numVars;
	}

	public FirstOrderRecurrence deepCopy()
	{
		return new FirstOrderRecurrence(new ArrayList<BigRational>(coeffs), baseVal, subjectVarIndex);
	}

	public BigRational removeCoeff(int varIndex)
	{
		if (subjectVarIndex > varIndex)
			subjectVarIndex--;
		numVars--;
		return coeffs.remove(varIndex);
	}

	public void addCoeff(int varIndex, BigRational val)
	{
		coeffs.set(varIndex, coeffs.get(varIndex).add(val));
	}

	public void setCoeff(int varIndex, BigRational val)
	{
		coeffs.set(varIndex, val);
	}

	public void addLastCoeff(BigRational val)
	{
		addCoeff(coeffs.size() - 1, val);
	}

	public BigRational getLastCoeff()
	{
		return coeffs.get(coeffs.size() - 1);
	}

	public OrdinaryGeneratingFunction getOGFForm()
	{
		convertToGF();
		return new OrdinaryGeneratingFunction(lhs, rhs);
	}

	public double evaluate(double[] values)
	{
		double sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += (values[i] * _coeffs[i]);
		}
		return sum + _coeffs[_coeffs.length - 1];
	}

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
}
