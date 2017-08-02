package explicit.cex.cex;

import java.io.File;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import parser.State;
import parser.Values;
import parser.VarList;
import prism.ModelType;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLog;
import strat.MDStrategy;
import explicit.MDP;
import explicit.Model;
import explicit.PredecessorRelation;
import explicit.StateValues;
import explicit.SuccessorsIterator;
import explicit.cex.gens.MDPViaDTMCCexGenerator.DeterministicChoiceResolver;
import explicit.cex.util.CexParams;
import explicit.cex.util.CexStatistics;
import explicit.cex.util.ValuationSet;
import explicit.exporters.ExportType;
import explicit.exporters.ModelExporter;
import explicit.exporters.StateExporter;
import explicit.rewards.MCRewards;
import explicit.rewards.MDPRewards;

public class MDCriticalMDPSubsystem extends NondetCounterexample implements MDP
{
	
	private CriticalSubsystem cs;
	private DeterministicChoiceResolver resolver;
	private MDP mdp;

	public MDCriticalMDPSubsystem(CexParams params, MDP underlyingModel, CriticalSubsystem cs, DeterministicChoiceResolver resolver)
	{
		super(params);
		this.cs = cs;
		this.mdp = underlyingModel;
		this.resolver = resolver;
	}
	
	private boolean hasChoiceIndex(int s, int i)
	{
		return resolver.getChoiceIndexForState(s) == i;
	}

	@Override
	public double getProbabilityMass()
	{
		return cs.getProbabilityMass();
	}

	@Override
	public CexStatistics generateStats()
	{
		return cs.generateStats();
	}

	@Override
	public void export(PrismLog out, StateExporter exp) throws PrismException
	{
		ModelExporter modelExporter = ModelExporter.makeExporter(this, ExportType.EXPLICIT_TRA);
		if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
		modelExporter.setStateExporter(exp);
		modelExporter.export(out);
	}

	@Override
	public Set<Object> getActions()
	{
		// TODO: Super inefficient
		Set<Object> actions = new TreeSet<>();
		for (int s = 0; s < cs.getNumStates(); s++) {
			Object action = resolver.getChoiceActionForState(s);
			if (action != null) actions.add(action);
		}
		return actions;
	}

	@Override
	public int getNumChoices(int s)
	{
		return 1;
	}

	@Override
	public int getMaxNumChoices()
	{
		return 1;
	}

	@Override
	public int getNumChoices()
	{
		// TODO: Super inefficient
		return getActions().size();
	}

	@Override
	public Object getAction(int s, int i)
	{
		if (hasChoiceIndex(s, i))
			return resolver.getChoiceActionForState(s);
		else
			return null;
	}

	@Override
	public boolean areAllChoiceActionsUnique()
	{
		return true;
	}

	@Override
	public boolean allSuccessorsInSet(int s, int i, BitSet set)
	{
		if (hasChoiceIndex(s, i))
			return cs.allSuccessorsInSet(s, set);
		else
			return true;
	}

	@Override
	public boolean someSuccessorsInSet(int s, int i, BitSet set)
	{
		if (hasChoiceIndex(s, i))
			return cs.allSuccessorsInSet(s, set);
		else
			return false;
	}

	@Override
	public Model constructInducedModel(MDStrategy strat)
	{
		throw new UnsupportedOperationException();
	}

	//@Override
	public void exportWithStrat(PrismLog out, ExportType modelExportType, int[] strat) throws PrismException
	{
		ModelExporter modelExporter = ModelExporter.makeExporter(this, modelExportType);
		modelExporter.exportWithStrat(out, strat);
	}

	@Override
	public ModelType getModelType()
	{
		return ModelType.MDP;
	}

	@Override
	public int getNumStates()
	{
		return cs.getNumStates();
	}

	@Override
	public int getNumInitialStates()
	{
		return cs.getNumInitialStates();
	}

	@Override
	public Iterable<Integer> getInitialStates()
	{
		return cs.getInitialStates();
	}

	@Override
	public int getFirstInitialState()
	{
		return cs.getFirstInitialState();
	}

	@Override
	public boolean isInitialState(int i)
	{
		return cs.isInitialState(i);
	}

	@Override
	public int getNumDeadlockStates()
	{
		return cs.getNumDeadlockStates();
	}

	@Override
	public Iterable<Integer> getDeadlockStates()
	{
		return cs.getDeadlockStates();
	}

	@Override
	public StateValues getDeadlockStatesList()
	{
		return cs.getDeadlockStatesList();
	}

	@Override
	public int getFirstDeadlockState()
	{
		return cs.getFirstDeadlockState();
	}

	@Override
	public boolean isDeadlockState(int i)
	{
		return cs.isDeadlockState(i);
	}

	@Override
	public List<State> getStatesList()
	{
		return cs.getStatesList();
	}

	@Override
	public Values getConstantValues()
	{
		return cs.getConstantValues();
	}

	@Override
	public BitSet getLabelStates(String name)
	{
		return cs.getLabelStates(name);
	}

	@Override
	public int getNumTransitions()
	{
		return cs.getNumTransitions();
	}

	@Override
	public Iterator<Integer> getSuccessorsIterator(int s)
	{
		return cs.getSuccessorsIterator(s);
	}
	
	@Override
	public Iterator<Integer> getSuccessorsIterator(int s, int i)
	{
		if (hasChoiceIndex(s, i))
			return cs.getSuccessorsIterator(s);
		else
			return new LinkedList<Integer>().iterator();
	}

	@Override
	public SuccessorsIterator getSuccessors(int s)
	{
		return SuccessorsIterator.from(getSuccessorsIterator(s), true);
	}

	@Override
	public SuccessorsIterator getSuccessors(int s, int i)
	{
		return SuccessorsIterator.from(getSuccessorsIterator(s, i), true);
	}
	
	@Override
	public boolean isSuccessor(int s1, int s2)
	{
		return cs.isSuccessor(s1, s2);
	}

	@Override
	public boolean allSuccessorsInSet(int s, BitSet set)
	{
		return cs.allSuccessorsInSet(s, set);
	}

	@Override
	public boolean someSuccessorsInSet(int s, BitSet set)
	{
		return cs.someSuccessorsInSet(s, set);
	}

	@Override
	public void findDeadlocks(boolean fix) throws PrismException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkForDeadlocks() throws PrismException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkForDeadlocks(BitSet except) throws PrismException
	{
		throw new UnsupportedOperationException();
	}

	//@Override
	public void export(PrismLog out, ExportType modelExportType) throws PrismException
	{
		ModelExporter modelExporter = ModelExporter.makeExporter(this, modelExportType);
		if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
		modelExporter.export(out);
	}

	@Override
	public void exportStates(int exportType, VarList varList, PrismLog log) throws PrismException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String infoString()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String infoStringTable()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumTransitions(int s, int i)
	{
		if (hasChoiceIndex(s, i))
			return cs.getNumTransitions(s);
		else
			return 0;
	}

	@Override
	public Iterator<Entry<Integer, Double>> getTransitionsIterator(int s, int i)
	{
		if (hasChoiceIndex(s, i)) {
			return cs.getTransitionsIterator(s);
		} else {
			List<Entry<Integer, Double>> emptyList = new LinkedList<>();
			return emptyList.iterator();
		}
	}

	@Override
	public void prob0step(BitSet subset, BitSet u, boolean forall, BitSet result)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void prob1Astep(BitSet subset, BitSet u, BitSet v, BitSet result)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void prob1Estep(BitSet subset, BitSet u, BitSet v, BitSet result, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void prob1step(BitSet subset, BitSet u, BitSet v, boolean forall, BitSet result)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean prob1stepSingle(int s, int i, BitSet u, BitSet v)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void mvMultMinMax(double[] vect, boolean min, double[] result, BitSet subset, boolean complement, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultMinMaxSingle(int s, double[] vect, boolean min, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Integer> mvMultMinMaxSingleChoices(int s, double[] vect, boolean min, double val)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultSingle(int s, int i, double[] vect)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultGSMinMax(double[] vect, boolean min, BitSet subset, boolean complement, boolean absolute, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultJacMinMaxSingle(int s, double[] vect, boolean min, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultJacSingle(int s, int i, double[] vect)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void mvMultRewMinMax(double[] vect, MDPRewards mdpRewards, boolean min, double[] result, BitSet subset, boolean complement, int[] strat)
	{
		throw new UnsupportedOperationException();		
	}

	@Override
	public double mvMultRewMinMaxSingle(int s, double[] vect, MDPRewards mdpRewards, boolean min, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultRewSingle(int s, int i, double[] vect, MCRewards mcRewards)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultRewGSMinMax(double[] vect, MDPRewards mdpRewards, boolean min, BitSet subset, boolean complement, boolean absolute, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double mvMultRewJacMinMaxSingle(int s, double[] vect, MDPRewards mdpRewards, boolean min, int[] strat)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Integer> mvMultRewMinMaxSingleChoices(int s, double[] vect, MDPRewards mdpRewards, boolean min, double val)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void mvMultRight(int[] states, int[] strat, double[] source, double[] dest)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTypeString()
	{
		return "Critical MDP Subsystem";
	}

	@Override
	public void fillValuationSet(ValuationSet vs) throws PrismException
	{
		for (State s : getStatesList()) {
			vs.addState(s);
		}
	}

	@Override
	public String getSummaryString()
	{
		return "Critical MDP Subsystem of " + cs.getNumStates() + " states, " + cs.getNumTransitions() + " transitions";
	}
	
	
	public Model getUnderlyingModel() {
		return mdp;
	}

	@Override
	public Set<String> getLabels()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasStoredPredecessorRelation()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PredecessorRelation getPredecessorRelation(PrismComponent parent, boolean storeIfNew)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearPredecessorRelation()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public VarList getVarList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasLabel(String name)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void exportToPrismExplicit(String baseFilename) throws PrismException
	{
		exportToPrismExplicitTra(baseFilename + ".tra");
	}

	@Override
	public void exportToPrismExplicitTra(String filename) throws PrismException
	{
		try {
			ModelExporter modelExporter = ModelExporter.makeExporter(this, ExportType.EXPLICIT_TRA);
			if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
			modelExporter.export(filename);
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void exportToPrismExplicitTra(File file) throws PrismException
	{
		try {
			ModelExporter modelExporter = ModelExporter.makeExporter(this, ExportType.EXPLICIT_TRA);
			if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
			modelExporter.export(file);
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void exportToPrismExplicitTra(PrismLog log)
	{
		try {
			ModelExporter modelExporter = ModelExporter.makeExporter(this, ExportType.EXPLICIT_TRA);
			if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
			modelExporter.export(log);
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void exportToPrismLanguage(String filename) throws PrismException
	{
		try {
			ModelExporter modelExporter = ModelExporter.makeExporter(this, ExportType.PRISM_LANG);
			if (!fullExportModeEnabled) modelExporter.restrictExportTo(getUnderlyingModel());
			modelExporter.export(filename);
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void exportToDotFileWithStrat(PrismLog out, BitSet mark, int[] strat)
	{
		try {
			exportWithStrat(out, ExportType.EXPLICIT_TRA, strat);
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
