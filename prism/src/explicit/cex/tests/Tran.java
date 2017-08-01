package explicit.cex.tests;

public class Tran {
	
	private static final double EPS = 1e-16;
	
	public final int trg;
	public final double prob;
	
	public Tran(int trg, double prob)
	{
		this.trg = trg;
		this.prob = prob;
	}
	
	public Tran(Tran other) {
		this(other, 0);
	}

	public Tran(Tran other, int offset)
	{
		this.trg = other.trg + offset;
		this.prob = other.prob;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tran)) return false;
		Tran otherTran = (Tran)other;
		return trg == otherTran.trg && Math.abs(prob - otherTran.prob) < EPS;
	}
	
	@Override
	public String toString() {
		return String.format("%d : %.4f", trg, prob);
	}
}
