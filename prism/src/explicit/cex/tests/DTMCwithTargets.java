package explicit.cex.tests;

import java.util.BitSet;

import explicit.DTMCSimple;

public class DTMCwithTargets
{

	public final DTMCSimple dtmc;
	public final BitSet targets;
	public final String description;

	public DTMCwithTargets(DTMCSimple dtmc, BitSet targets)
	{
		this(dtmc, targets, "Default Name");
	}

	public DTMCwithTargets(DTMCSimple dtmc, BitSet targets, String description)
	{
		this.dtmc = dtmc;
		this.targets = targets;
		this.description = description;
	}
}