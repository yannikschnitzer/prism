package recurrence.utils.expression;

public interface IChecker
{
	
	/**
	 * Checks whether state is valid according to the commands
	 * @param state valuations of variables of a state
	 * @return returns true if it is a valid state
	 */
	public boolean validate(Object[] state);
}
