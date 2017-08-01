package explicit.cex.tests;

import java.util.BitSet;

import explicit.MDPSimple;

public class MDPwithTargets
{

	public final MDPSimple mdp;
	public final BitSet targets;
	public final String description;

	public MDPwithTargets(MDPSimple mdp, BitSet targets)
	{
		this(mdp, targets, "Default Name");
	}

	public MDPwithTargets(MDPSimple mdp, BitSet targets, String description)
	{
		this.mdp = mdp;
		this.targets = targets;
		this.description = description;
	}
	
}
