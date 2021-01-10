package se.l4.exoconf;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import javax.validation.ValidatorFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.exobytes.Serializer;
import se.l4.exobytes.Serializers;
import se.l4.exoconf.internal.ConfigBuilderImpl;
import se.l4.exoconf.sources.ConfigSource;

/**
 * Configuration as loaded from config files. Instances of this type can
 * be created via {@link #create()}.
 *
 * <p>
 * Example usage:
 * <pre>
 * Config config = Config.create()
 *	.addFile("/etc/app/normal.conf")
 *	.build();
 *
 * Optional<Thumbnails> thumbs = config.get("thumbs", Thumbnails.class);
 * Optional<Size> mediumSize = config.get("thumbs.medium", Size.class);
 * </pre>
 *
 * <h2>Configuration file format</h2>
 * <p>
 * The format is similar to JSON but is not as strict. For example it does not
 * require quotes around keys or string values and the initial braces can be
 * skipped.
 *
 * <p>
 * Example:
 * <pre>
 * thumbs: {
 *	medium: { width: 400, height: 400 }
 *	small: {
 *		width: 100
 *		height: 100
 *	}
 * }
 *
 * # Override the width
 * thumbs.small.width: 150
 * </pre>
 */
public interface Config
{
	/**
	 * Resolve values as the given path as an object. This is equivalent
	 * to call {@link #get(String, Class)} and then {@link Value#getOrDefault()}
	 * with the value {@code null}.
	 *
	 * @param path
	 * @param type
	 * @return
	 */
	@NonNull
	<T> Optional<T> get(@NonNull String path, @NonNull Class<T> type);

	/**
	 * Resolve values as the given path as an object. This is equivalent
	 * to call {@link #get(String, Class)} and then {@link Value#getOrDefault()}
	 * with the value {@code null}.
	 *
	 * @param path
	 * @param type
	 * @return
	 */
	@NonNull
	<T> Optional<T> get(@NonNull String path, @NonNull Serializer<T> serializer);

	/**
	 * Scope this configuration to the specified path.
	 *
	 * @param path
	 * @return
	 */
	Config scope(String path);

	/**
	 * Get a new {@link ConfigBuilder} to create a new configuration.
	 *
	 * @return
	 */
	@NonNull
	static Builder create()
	{
		return new ConfigBuilderImpl();
	}

	/**
	 * Builder for instances of {@link Config}. Makes it easy to create a
	 * configuration over several files. When creating a configuration one can opt
	 * to use a {@link #withSerializers(Serializers) custom serializer collection}
	 * and its possible to activate validation via {@link #withValidatorFactory(ValidatorFactory)}.
	 */
	interface Builder
	{
		/**
		 * Set the the {@link Serializers} to use when reading the
		 * configuration files.
		 *
		 * @param serializers
		 * @return
		 */
		@NonNull
		Builder withSerializers(@NonNull Serializers serializers);

		/**
		 * Set the {@link ValidatorFactory} to use when validating loaded
		 * configuration objects.
		 *
		 * @param validation
		 * @return
		 */
		@NonNull
		Builder withValidatorFactory(@NonNull ValidatorFactory validation);

		/**
		 * Set the root folder of the configuration.
		 *
		 * @param root
		 * @return
		 */
		@NonNull
		Builder withRoot(@NonNull String root);

		/**
		 * Set the root folder of the configuration.
		 *
		 * @param path
		 * @return
		 */
		@NonNull
		Builder withRoot(@NonNull Path path);

		/**
		 * Set the root folder of the configuration.
		 *
		 * @param root
		 * @return
		 */
		@NonNull
		Builder withRoot(@NonNull File root);

		/**
		 * Add a file that should be loaded.
		 *
		 * @param path
		 * @return
		 */
		@NonNull
		Builder addFile(@NonNull String path);

		/**
		 * Add a file that should be loaded.
		 *
		 * @param path
		 * @return
		 */
		@NonNull
		Builder addFile(@NonNull Path path);

		/**
		 * Add a file that should be loaded.
		 *
		 * @param file
		 * @return
		 */
		@NonNull
		Builder addFile(@NonNull File file);

		/**
		 * Add a stream that should be read.
		 *
		 * @param stream
		 * @return
		 */
		@NonNull
		Builder addStream(@NonNull InputStream stream);

		/**
		 * Add a source of config properties.
		 *
		 * @param source
		 * @return
		 */
		@NonNull
		Builder addSource(@NonNull ConfigSource source);

		/**
		 * Add a property to the current configuration.
		 *
		 * @param key
		 * @param value
		 * @return
		 */
		@NonNull
		Builder addProperty(@NonNull String key, Object value);

		/**
		 * Create the configuration object. This will load any declared input
		 * files.
		 *
		 * @return
		 */
		@NonNull
		Config build();
	}
}
