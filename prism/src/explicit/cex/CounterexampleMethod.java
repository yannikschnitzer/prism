package explicit.cex;

import explicit.DTMC;
import explicit.MDP;
import explicit.Model;
import explicit.cex.gens.CexGenerator;
import explicit.cex.gens.CritSysCexGenerator;
import explicit.cex.gens.MDPViaDTMCCexGenerator;
import explicit.cex.gens.MDPviaSMTCexGenerator;
import explicit.cex.gens.PathSetCexGenerator;
import explicit.cex.gens.DTMCviaSMTCexGenerator;
import explicit.cex.util.CexParams;
import prism.ModelType;
import prism.PrismLog;

public enum CounterexampleMethod {
	DTMC_EXPLICIT_KPATH, DTMC_EXPLICIT_LOCAL, DTMC_EXPLICIT_SMT,
	MDP_VIA_DTMC_KPATH, MDP_VIA_DTMC_LOCAL, MDP_EXPLICIT_SMT,
	UNKNOWN;

	@Override
	public String toString() {
		switch (this) {
		case DTMC_EXPLICIT_KPATH:
			return "K-Path";
		case DTMC_EXPLICIT_LOCAL:
			return "Local Search";
		case DTMC_EXPLICIT_SMT:
			return "SMT encoding of DTMC";
		case MDP_VIA_DTMC_KPATH:
			return ("MDP via DTMC K-Path");
		case MDP_VIA_DTMC_LOCAL:
			return ("MDP via DTMC Local Search");
		case MDP_EXPLICIT_SMT:
			return ("SMT encoding of MDP");
		case UNKNOWN:
			return "Unknown";
		default:
			throw new IllegalStateException();
		}
	}

	public CexGenerator makeGenerator(Model model, CexParams params, PrismLog log) {
		switch (getModelType()) {
		case DTMC:
			if (!(model instanceof NormalizedDTMC)) {
				if (!(model instanceof DTMC)) {
					throw new IllegalArgumentException("Cannot create counterexample generator '" + this + "': Expected DTMC, received " + model.getClass().getName());
				} else {
					model = new NormalizedDTMC((DTMC)model, params);
				}
				//throw new IllegalArgumentException("Cannot create counterexample generator '" + this + "': Expected NormalizedDTMC, received " + model.getClass().getName());
			}
			break;
		case MDP:
			if (!(model instanceof MDP)) throw new IllegalArgumentException("Cannot create counterexample generator '" + this + "': Expected MDP, received " + model.getClass().getName());
			break;
		default:
			break;
		} 

		switch (this) {
		case DTMC_EXPLICIT_KPATH:
			return new PathSetCexGenerator((NormalizedDTMC)model, params, log);
		case DTMC_EXPLICIT_LOCAL:
			return new CritSysCexGenerator((NormalizedDTMC)model, params, log);
		case DTMC_EXPLICIT_SMT:
			return new DTMCviaSMTCexGenerator((NormalizedDTMC)model, params, log);
		case MDP_VIA_DTMC_KPATH:
			return new MDPViaDTMCCexGenerator((MDP)model, CounterexampleMethod.DTMC_EXPLICIT_KPATH, params, log);
		case MDP_VIA_DTMC_LOCAL:
			return new MDPViaDTMCCexGenerator((MDP)model, CounterexampleMethod.DTMC_EXPLICIT_LOCAL, params, log);
		case MDP_EXPLICIT_SMT:
			return new MDPviaSMTCexGenerator((MDP)model, params, log);
		case UNKNOWN:
			throw new IllegalStateException("Unknown counterexample method");
		default:
			throw new IllegalStateException();
		}
	}

	public boolean isApplicableToInducedDTMC()
	{
		switch (this) {
		case DTMC_EXPLICIT_KPATH:
		case DTMC_EXPLICIT_LOCAL:
			return true;
		case DTMC_EXPLICIT_SMT:
			// Should solve SMT directly for MDP
			return false;
		default:
			return false;
		}
	}

	public ModelType getModelType()
	{
		switch (this) {
		case DTMC_EXPLICIT_KPATH:
		case DTMC_EXPLICIT_LOCAL:
		case DTMC_EXPLICIT_SMT:
			return ModelType.DTMC;
		case MDP_VIA_DTMC_KPATH:
		case MDP_VIA_DTMC_LOCAL:
		case MDP_EXPLICIT_SMT:
			return ModelType.MDP;
		default:
			return null;
		}
	}
}
