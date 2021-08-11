package se.l4.exoconf.sources;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.impl.tuple.Tuples;

/**
 * Source that provides access to environment variables. Added as the highest
 * priority source when using {@link Config.Builder}.
 */
public class EnvironmentConfigSource
	implements ConfigSource
{
	private final MapIterable<String, Object> properties;

	public EnvironmentConfigSource()
	{
		this(Maps.mutable.ofMap(System.getenv()));
	}

	EnvironmentConfigSource(MapIterable<String, Object> properties)
	{
		this.properties = properties.collect((k, v) -> Tuples.pair(k.toUpperCase(), v));
	}

	@Override
	public MapIterable<String, Object> getProperties()
	{
		return properties;
	}

	@Override
	public RichIterable<String> getKeys(String path)
	{
		String prefix = path.isEmpty() ? path : path + ConfigKeys.PATH_DELIMITER;
		int length = prefix.length();
		return properties.keysView().asLazy()
			.flatCollect(key -> {
				String trimmedKey = key.trim();
				return Lists.immutable.of(
					trimmedKey,
					replaceNonAscii(trimmedKey.toLowerCase()),
					trimmedKey.replace('_', ConfigKeys.PATH_DELIMITER),
					replaceNonAscii(trimmedKey).replace('_', ConfigKeys.PATH_DELIMITER).toLowerCase()
				);
			})
			.selectWith(String::startsWith, prefix)
			.collect(key -> {
				int idx = key.indexOf(ConfigKeys.PATH_DELIMITER, length);
				return idx >= 0 ? key.substring(length, idx) : key.substring(length);
			})
			.select(s -> ! s.isEmpty())
			.distinct();
	}

	@Override
	public Object getValue(String path)
	{
		String key = path.toUpperCase();
		Object value = properties.get(key);
		if(value != null)
		{
			return value;
		}

		return properties.get(replaceNonAscii(key));
	}

	private String replaceNonAscii(String path)
	{
		StringBuilder builder = new StringBuilder();
		for(int i=0, n=path.length(); i<n; i++)
		{
			char c = path.charAt(i);
			if((c >= '0' && c <= '9')
				|| (c >= 'a' && c <= 'z')
				|| (c >= 'A' && c <= 'Z'))
			{
				builder.append(c);
			}
			else
			{
				builder.append('_');
			}
		}

		return builder.toString();
	}
}
