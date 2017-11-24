package recurrence.math.matrix;

import recurrence.data_structure.numbers.PolynomialFraction;
import recurrence.log.Level;
import recurrence.log.Log;

public class GJEPolyFraction
{
	private final int N; // N-by-N system
	private PolynomialFraction[][] a; // N-by-N+1 augmented matrix

	/**
	 * Solves the simultaneous equations of polynomial fractions
	 * @param A nxn matrix
	 * @param B nx1 matrix
	 */
	public GJEPolyFraction(PolynomialFraction[][] A, PolynomialFraction[] B)
	{
		// Assign the size of the square matrix
		N = B.length;
		// build augmented matrix
		a = new PolynomialFraction[N][N + 1];

		// Copying A
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				a[i][j] = A[i][j];

		// Copying B
		for (int i = 0; i < N; i++)
			a[i][N] = B[i];

		solve();
	}

	/**
	 * Applies the gaussion-jordan elimination
	 */
	private void solve()
	{
		// Choose a pivot and make the value that lies in the diagonal as 1
		for (int diag = 0; diag < N; diag++) {
			boolean isOneFound = false;
			if (!a[diag][diag].isOne()) {
				int nonZeroRow = diag;
				for (int row = diag + 1; row < N; row++) {
					if (a[row][diag].isOne()) {
						swap(row, diag);
						isOneFound = true;
						break;
					} else if (nonZeroRow == diag && !a[row][diag].isZero()) {
						nonZeroRow = row; // find a row that is non-zero
					}
				}

				if (a[diag][diag].isZero())
					swap(nonZeroRow, diag);
			}
			if (!isOneFound) {
				for (int col = diag + 1; col <= N; col++)
					a[diag][col] = a[diag][col].divides(a[diag][diag]);
				a[diag][diag] = PolynomialFraction.one();
			}
			eliminateL(diag);
			show();
		}

		// Make everything zero above diagonal
		for (int p = N - 1; p >= 0; p--) {
			for (int q = p - 1; q >= 0; q--) {
				PolynomialFraction s = a[q][p].times(a[p][N]);
				a[q][N] = a[q][N].minus(s);
				a[q][p] = PolynomialFraction.zero();
			}
		}
	}


	/**
	 * Make everything zero below diagonal
	 * @param diag index of the diagonal of the matrix
	 */
	private void eliminateL(int diag)
	{
		for (int i = diag + 1; i < N; i++) {
			PolynomialFraction x = a[i][diag];
			if (!x.isZero()) {
				for (int j = diag + 1; j <= N; j++) {
					a[i][j] = a[i][j].minus(x.times(a[diag][j]));
				}
				a[i][diag] = PolynomialFraction.zero();
			}
		}
	}

	/**
	 * Swaps the given rows in the matrix
	 * @param row1 a row of the matrix
	 * @param row2 a row of the matrix
	 */
	public void swap(int row1, int row2)
	{
		PolynomialFraction[] temp = a[row1];
		a[row1] = a[row2];
		a[row2] = temp;
	}

	/**
	 * @return result after applying gaussian-jordan elimination on polynomial fraction simultaneous equation
	 */
	public PolynomialFraction[] result()
	{
		PolynomialFraction[] result = new PolynomialFraction[N];
		for (int i = 0; i < N; i++) {
			result[i] = a[i][N];
			result[i].simplify();
			result[i].tmpScale();
		}
		return result;
	}

	/**
	 * Prints the results
	 */
	private void show()
	{
		String str = ("=======================================\n");
		int max = 5;
		for (PolynomialFraction[] i : a) {
			for (PolynomialFraction j : i) {
				int length = j.toString().length();
				if (max < length)
					max = length;
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				str += String.format("%" + max + "s ", a[i][j]);
			}
			str += String.format("| %" + max + "s\n", a[i][N]);
		}

		str += String.format("=======================================\n");
		Log.p(Level.INFO, str, this.getClass());
	}
}
