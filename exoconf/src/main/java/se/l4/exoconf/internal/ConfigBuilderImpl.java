package se.l4.exoconf.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.validation.ValidatorFactory;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;

import se.l4.exobytes.Serializers;
import se.l4.exoconf.Config;
import se.l4.exoconf.ConfigException;
import se.l4.exoconf.sources.ConfigSource;
import se.l4.exoconf.sources.EnvironmentConfigSource;
import se.l4.exoconf.sources.FileConfigSource;
import se.l4.exoconf.sources.MapBasedConfigSource;
import se.l4.exoconf.sources.MergingConfigSource;
import se.l4.ylem.io.IOSupplier;

/**
 * Builder for configuration instances.
 */
public class ConfigBuilderImpl
	implements Config.Builder
{
	private final ImmutableMap<String, Object> keys;
	private final ImmutableList<IOSupplier<ConfigSource>> suppliers;

	private final Serializers serializers;
	private final ValidatorFactory validatorFactory;

	private final File root;

	public ConfigBuilderImpl(
		Serializers serializers,
		ValidatorFactory validatorFactory,
		File root,
		ImmutableList<IOSupplier<ConfigSource>> suppliers,
		ImmutableMap<String, Object> keys
	)
	{
		this.serializers = serializers;
		this.validatorFactory = validatorFactory;
		this.root = root;
		this.keys = keys;
		this.suppliers = suppliers;
	}

	@Override
	public Config.Builder withSerializers(Serializers serializers)
	{
		return new ConfigBuilderImpl(
			serializers,
			validatorFactory,
			root,
			suppliers,
			keys
		);
	}

	@Override
	public Config.Builder withValidatorFactory(ValidatorFactory validatorFactory)
	{
		return new ConfigBuilderImpl(
			serializers,
			validatorFactory,
			root,
			suppliers,
			keys
		);
	}

	@Override
	public Config.Builder withRoot(String root)
	{
		return withRoot(new File(root));
	}

	@Override
	public Config.Builder withRoot(Path path)
	{
		return withRoot(path.toFile());
	}

	@Override
	public Config.Builder withRoot(File root)
	{
		return new ConfigBuilderImpl(
			serializers,
			validatorFactory,
			root,
			suppliers,
			keys
		);
	}

	@Override
	public Config.Builder addFile(String path)
	{
		return addFile(new File(path));
	}

	@Override
	public Config.Builder addFile(Path path)
	{
		return addFile(path.toFile());
	}

	@Override
	public Config.Builder addFile(File file)
	{
		File root = this.root;
		if(root == null)
		{
			root = file.getParentFile();
		}

		IOSupplier<ConfigSource> supplier = () -> {
			if(! file.exists())
			{
				throw new ConfigException("The file " + file + " does not exist");
			}
			else if(! file.isFile())
			{
				throw new ConfigException(file + " is not a file");
			}
			else if(! file.canRead())
			{
				throw new ConfigException("Can not read " + file + ", check permissions");
			}

			return FileConfigSource.read(file);
		};

		return new ConfigBuilderImpl(
			serializers,
			validatorFactory,
			root,
			suppliers.newWith(supplier),
			keys
		);
	}

	@Override
	public Config.Builder addStream(InputStream stream)
	{
		IOSupplier<ConfigSource> supplier = () -> FileConfigSource.read(stream);
		return new ConfigBuilderImpl(
			serializers,
			validatorFactory,
			root,
			suppliers.newWith(supplier),
			keys
		);
	}

	@Override
	public Config.Builder addSource(ConfigSource source)
	{
		return new ConfigBuilderImpl(
			serializers,
			validatorFactory,
			root,
			suppliers.newWith(() -> source),
			keys
		);
	}

	@Override
	public Config.Builder addProperty(String key, Object value)
	{
		return new ConfigBuilderImpl(
			serializers,
			validatorFactory,
			root,
			suppliers,
			keys.newWithKeyValue(key, value)
		);
	}

	@Override
	public Config build()
	{
		Serializers serializers = this.serializers;
		if(serializers == null)
		{
			serializers = Serializers.create()
				.build();
		}
		else
		{
			serializers = Serializers.create()
				.wrap(serializers)
				.build();
		}

		MutableList<ConfigSource> sources = Lists.mutable.empty();
		sources.add(new MapBasedConfigSource(keys.toImmutable()));

		for(IOSupplier<ConfigSource> supplier : suppliers)
		{
			try
			{
				sources.add(supplier.get());
			}
			catch(IOException e)
			{
				throw new ConfigException("Unable to read configuration; " + e.getMessage(), e);
			}
		}

		sources.add(new EnvironmentConfigSource());

		ConfigSource source = new MergingConfigSource(sources.toReversed());
		return new DefaultConfig(serializers, validatorFactory, source, root);
	}
}
