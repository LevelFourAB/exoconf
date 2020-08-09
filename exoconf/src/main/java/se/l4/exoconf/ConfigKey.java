package se.l4.exoconf;

import java.io.IOException;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;
import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;
import se.l4.exoconf.sources.ConfigKeys;

/**
 * A configuration key, represents the path of the config object that has been
 * deserialized. Used to resolve further configuration values.
 *
 * <p>
 * Example use with {@link Expose} and {@link AnnotationSerialization}:
 * <pre>
 * @Expose(ConfigKey.NAME)
 * private ConfigKey configKey;
 * </pre>
 */
public class ConfigKey
{
	public static final String NAME = "exoconf:configKey";

	private final Config config;
	private final String key;

	private ConfigKey(Config config, String key)
	{
		this.config = config;
		this.key = key;
	}

	/**
	 * Get the value of a sub path to this key.
	 *
	 * @param subPath
	 * @param type
	 * @return
	 */
	@NonNull
	public <T> Optional<T> asObject(String subPath, Class<T> type)
	{
		return config.asObject(key + ConfigKeys.PATH_DELIMITER + subPath, type);
	}

	/**
	 * Get this object as another type.
	 *
	 * @param type
	 * @return
	 */
	public <T> T asObject(Class<T> type)
	{
		return config.asObject(key, type).get();
	}

	public static class ConfigKeySerializer
		implements Serializer<ConfigKey>
	{
		private final Config config;

		public ConfigKeySerializer(Config config)
		{
			this.config = config;
		}

		@Override
		public ConfigKey read(StreamingInput in)
			throws IOException
		{
			in.next(Token.VALUE);
			return new ConfigKey(config, in.readString());
		}

		@Override
		public void write(ConfigKey object, StreamingOutput stream)
			throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}
}
