package se.l4.exoconf.sources;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

public class EnvironmentSourceTest
{
	@Test
	public void testGet1()
	{
		EnvironmentConfigSource source = new EnvironmentConfigSource(Maps.mutable.<String, Object>empty()
			.withKeyValue("TEST", "v1")
		);

		assertThat(source.getValue("TEST"), is("v1"));
		assertThat(source.getValue("test"), is("v1"));
	}

	@Test
	public void testGet2()
	{
		EnvironmentConfigSource source = new EnvironmentConfigSource(Maps.mutable.<String, Object>empty()
			.withKeyValue("TEST_SUBKEY", "v1")
		);

		assertThat(source.getValue("TEST_SUBKEY"), is("v1"));
		assertThat(source.getValue("test_subkey"), is("v1"));
		assertThat(source.getValue("test.subkey"), is("v1"));
	}

	@Test
	public void testKeys()
	{
		EnvironmentConfigSource source = new EnvironmentConfigSource(Maps.mutable.<String, Object>empty()
			.withKeyValue("TEST_SUBKEY", "v1")
		);

		assertThat(source.getKeys("test"), containsInAnyOrder("subkey"));
	}

	@Test
	public void testKeys2()
	{
		EnvironmentConfigSource source = new EnvironmentConfigSource(Maps.mutable.<String, Object>empty()
			.withKeyValue("TEST_SUBKEY", "v1")
			.withKeyValue("TEST_key2", "v2")
		);

		assertThat(source.getKeys("test"), containsInAnyOrder("subkey", "key2"));
	}
}
