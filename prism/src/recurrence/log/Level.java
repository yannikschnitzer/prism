package recurrence.log;

public enum Level {
	DEBUG(1), INFO(2), FINE(3);

	private int level;

	Level(int level)
	{
		this.level = level;
	}

	public int val()
	{
		return level;
	}
}