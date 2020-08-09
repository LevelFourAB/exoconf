package se.l4.exoconf;

/**
 * Exception thrown when something is wrong with the configuration.
 */
public class ConfigException
	extends RuntimeException
{
	public ConfigException()
	{
		super();
	}

	public ConfigException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConfigException(String message)
	{
		super(message);
	}

	public ConfigException(Throwable cause)
	{
		super(cause);
	}
}
