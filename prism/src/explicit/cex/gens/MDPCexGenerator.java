package explicit.cex.gens;

import prism.PrismLog;
import explicit.MDP;
import explicit.cex.util.CexParams;

public abstract class MDPCexGenerator extends CexGenerator
{
	
	/** The model to generate a counterexample for */
	protected final MDP mdp;
	
	public MDPCexGenerator(MDP mdp, CexParams params, PrismLog log)
	{
		super(params, log);
		this.mdp = mdp;
	}

}
