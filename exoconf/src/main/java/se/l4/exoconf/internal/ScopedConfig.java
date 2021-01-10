package se.l4.exoconf.internal;

import java.util.Optional;

import se.l4.exobytes.Serializer;
import se.l4.exoconf.Config;

/**
 * Configuration that scopes all the lookups.
 */
public class ScopedConfig
	implements Config
{
	private final Config other;
	private final String path;

	public ScopedConfig(Config other, String path)
	{
		this.other = other;
		this.path = path;
	}

	@Override
	public <T> Optional<T> get(String path, Class<T> type)
	{
		return other.get(this.path + '.' + path, type);
	}

	@Override
	public <T> Optional<T> get(String path, Serializer<T> serializer)
	{
		return other.get(this.path + '.' + path, serializer);
	}

	@Override
	public Config scope(String path)
	{
		return new ScopedConfig(other, this.path + '.' + path);
	}
}
