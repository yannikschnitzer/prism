package recurrence.log;

/**
 * This enum represents the levels of log
 */
public enum Level {
	DEBUG(1), INFO(2), FINE(3);

	private int level;

	Level(int level)
	{
		this.level = level;
	}

	/**
	 * @return the current log level
	 */
	public int val()
	{
		return level;
	}
}