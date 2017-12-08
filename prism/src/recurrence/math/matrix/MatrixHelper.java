package recurrence.math.matrix;

public class MatrixHelper
{
	/**
	 * Solves the form a^power * b
	 * @param a 2x2 matrix
	 * @param b 2x1 vector
	 * @param power the power of a
	 * @return a^power times b
	 */
	public static double[] solve(double[][] a, double[] b, int power)
	{
		return multiply(matrixPower(a, power), b);
	}

	/**
	 * Compute the power n of Matrix
	 * @param base matrix to powered
	 * @param pow value of the power
	 * @return base^n
	 */
	static double[][] matrixPower(double[][] base, long pow)
	{
		int n = base.length;

		// Generate identity matrix
		double[][] ans = new double[n][n];
		for (int i = 0; i < n; i++)
			ans[i][i] = 1;

		while (pow != 0) {
			if ((pow & 1) != 0)
				ans = multiply(ans, base);
			base = multiply(base, base);
			pow >>= 1;
		}

		return ans;
	}

	/**
	 * Matrix times Matrix
	 * @param a 2x2 matrix
	 * @param b 2x2 matrix
	 * @return 2x2 matrix
	 */
	public static double[][] multiply(double[][] a, double[][] b)
	{
		double[][] c = new double[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			double[] arowi = a[i];
			double[] crowi = c[i];
			for (int k = 0; k < b.length; k++) {
				double[] browk = b[k];
				double aik = arowi[k];
				for (int j = 0; j < b[0].length; j++) {
					crowi[j] += aik * browk[j];
				}
			}
		}
		return c;
	}

	/**
	 * Matrix times Vector
	 * @param a 2x2 matrix
	 * @param b 2x1 vector
	 * @return 2x1 vector
	 */
	public static double[] multiply(double[][] a, double[] b)
	{
		int m = a.length;
		int n = a[0].length;

		double[] y = new double[m];
		double[] row;

		for (int i = 0; i < m; i++) {
			row = a[i];
			for (int j = 0; j < n; j++)
				y[i] += row[j] * b[j];
		}
		return y;
	}

}
