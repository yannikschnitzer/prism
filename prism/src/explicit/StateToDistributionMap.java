package explicit;


public interface StateToDistributionMap
{

	public int getNumStates();
	
	public int getNumTransitions();

	public Distribution getTransitions(int i);

}
