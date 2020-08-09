package se.l4.exoconf.internal;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import se.l4.exobytes.Serializer;
import se.l4.exobytes.Serializers;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exoconf.Config;
import se.l4.exoconf.ConfigException;
import se.l4.exoconf.ConfigKey;
import se.l4.exoconf.internal.streaming.MapInput;
import se.l4.exoconf.internal.streaming.NullInput;
import se.l4.exoconf.sources.ConfigSource;

/**
 * Default implementation of {@link Config}.
 */
public class DefaultConfig
	implements Config
{
	private final Serializers collection;
	private final ValidatorFactory validatorFactory;
	private final ConfigSource source;

	DefaultConfig(
		Serializers collection,
		ValidatorFactory validatorFactory,
		ConfigSource source,
		File root
	)
	{
		this.collection = collection;
		this.validatorFactory = validatorFactory;
		this.source = source;

		collection.register(File.class, new FileSerializer(root));
		collection.register(ConfigKey.class, new ConfigKey.ConfigKeySerializer(this));
	}

	@Override
	public <T> Optional<T> get(String path, Class<T> type)
	{
		Serializer<T> serializer = collection.get(type);
		return get(path, serializer);
	}

	@Override
	public <T> Optional<T> get(String path, Serializer<T> serializer)
	{
		Objects.requireNonNull(path);
		Objects.requireNonNull(serializer);

		StreamingInput input = MapInput.resolveInput(source, path);
		if(input instanceof NullInput)
		{
			return Optional.empty();
		}

		try
		{
			T instance = serializer.read(input);

			validateInstance(path, instance);

			return Optional.ofNullable(instance);
		}
		catch(ConfigException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new ConfigException("Unable to get config data at `" + path + "`; " + e.getMessage(), e);
		}
	}

	private void validateInstance(String path, Object object)
	{
		if(validatorFactory == null) return;

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(object);

		if(violations.isEmpty())
		{
			// No violations
			return;
		}

		StringBuilder builder = new StringBuilder("Validation failed for `" + path + "`:\n");

		for(ConstraintViolation<Object> violation : violations)
		{
			builder
				.append("* ")
				.append(join(violation.getPropertyPath()))
				.append(violation.getMessage())
				.append("\n");
		}

		throw new ConfigException(builder.toString());
	}

	private String join(Path path)
	{
		StringBuilder builder = new StringBuilder();
		for(Node node : path)
		{
			if(builder.length() > 0)
			{
				builder.append(".");
			}

			builder.append(node.getName());
		}

		if(builder.length() > 0)
		{
			builder.append(": ");
		}

		return builder.toString();
	}
}
