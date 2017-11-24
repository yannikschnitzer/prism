package recurrence.log;

import java.io.PrintStream;

public class Log
{
	public static final PrintStream LOGGER = System.out;
	private static Level defaultLevel = Level.DEBUG;
	private static boolean isSelectedLevelOnly = false;

	public static void setLevel(Level level)
	{
		defaultLevel = level;
	}

	public static void set(boolean _isSelectedLevelOnly)
	{
		isSelectedLevelOnly = _isSelectedLevelOnly;
	}

	public static void p(Level level, Object obj)
	{
		p(level, obj, null);
	}

	public static void p(Level level, Object obj, Class<?> _class)
	{
		if ((!isSelectedLevelOnly && defaultLevel.val() < level.val()) || defaultLevel.val() == level.val()) {
			String header = getHeader(level, _class);
			String border = new String(new char[header.length() + 3]).replace("\0", "=");
			//LOGGER.print("\n\n" + border + "\n " + getHeader(level, _class) + border + "\n\n" + obj + "\n\n");
		}
	}

	public static String getHeader(Level level, Class<?> _class)
	{
		String str = "";
		switch (level) {
		case DEBUG:
			str += "Level : DEBUG\t";
			break;
		case INFO:
			str += "Level : INFO\t";
			break;
		case FINE:
			str += "Level : FINE\t";
		}
		str += _class == null ? "\n" : "Class : " + _class.getCanonicalName() + "\n";
		return str;
	}

	public static void EXIT()
	{
		System.exit(0);
	}
}
