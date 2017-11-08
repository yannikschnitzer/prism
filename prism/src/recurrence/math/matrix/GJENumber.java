package recurrence.math.matrix;

import recurrence.data_structure.numbers.Decimal;
import recurrence.data_structure.numbers.INumber;
import recurrence.log.Level;
import recurrence.log.Log;

public class GJENumber
{
	private final int N; // N-by-N system
	private INumber[][] a; // N-by-N+1 augmented matrix

	public GJENumber(INumber[][] A, INumber[] B)
	{
		// Assign the size of the square matrix
		N = B.length;
		// build augmented matrix
		a = new INumber[N][N + 1];

		// Copying A
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				a[i][j] = A[i][j];

		// Copying B
		for (int i = 0; i < N; i++)
			a[i][N] = B[i];

		solve();
		for (INumber[] row : a) {
			for (INumber col : row) {
				col.simplify();
			}
		}
	}

	private void solve()
	{
		// Choose a pivot and make the value that lies in the diagonal as 1
		for (int diag = 0; diag < N; diag++) {
			boolean isOneFound = false;
			if (!(isOneFound = a[diag][diag].isOne())) {
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
					a[diag][col] = a[diag][col].dividedby(a[diag][diag]);
				a[diag][diag] = Decimal.one();
			}
			eliminateL(diag);
		}

		// Make everything zero above diagonal
		for (int p = N - 1; p >= 0; p--) {
			for (int q = p - 1; q >= 0; q--) {
				a[q][N] = a[q][N].minus(a[q][p].timesby(a[p][N]));
				a[q][p] = Decimal.zero();
			}
		}
	}

	// Make everything zero below diagonal
	private void eliminateL(int diag)
	{
		for (int i = diag + 1; i < N; i++) {
			INumber x = a[i][diag];
			if (!x.isZero()) {
				for (int j = diag + 1; j <= N; j++) {
					a[i][j] = a[i][j].minus(x.timesby(a[diag][j]));
				}
				a[i][diag] = Decimal.zero();
			}
		}
	}

	public void swap(int row1, int row2)
	{
		INumber[] temp = a[row1];
		a[row1] = a[row2];
		a[row2] = temp;
	}

	public INumber[] result()
	{
		INumber[] result = new INumber[N];
		for (int i = 0; i < N; i++) {
			result[i] = a[i][N];
			result[i].simplify();
			result[i].firstScale();
		}
		return result;
	}

	public void show()
	{
		String str = ("=======================================\n");
		int max = 5;
		for (INumber[] i : a) {
			for (INumber j : i) {
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
