package explicit.cex.util;

public class Transition
{

	private int source;
	private int target;
	private double probability;

	public Transition(int source, int target, double probability)
	{
		super();
		this.source = source;
		this.target = target;
		this.probability = probability;
	}

	public int getSource()
	{
		return source;
	}

	public int getTarget()
	{
		return target;
	}

	public double getProbability()
	{
		return probability;
	}

	@Override
	public String toString()
	{
		return source + "-[" + probability + "]->" + target;
	}
}
