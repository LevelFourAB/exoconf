package se.l4.exoconf.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Maps;
import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.Token;
import se.l4.exoconf.ConfigKey;
import se.l4.exoconf.internal.streaming.MapInput;
import se.l4.exoconf.sources.MapBasedConfigSource;

/**
 * Tests for {@link MapInput}.
 */
public class ObjectStreamsTest
{
	protected StreamingInput resolve(Map<String, Object> map)
	{
		return MapInput.resolveInput(new MapBasedConfigSource(Maps.mutable.ofMap(map)), "");
	}

	protected Map<String, Object> createMap()
	{
		return new LinkedHashMap<String, Object>();
	}

	@Test
	public void testSingleString()
		throws Exception
	{
		Map<String, Object> data = createMap();
		data.put("key", "value");

		StreamingInput in = resolve(data);

		assertThat(in.peek(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is(ConfigKey.NAME));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is(""));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value"));

		assertThat(in.peek(), is(Token.OBJECT_END));
		assertThat(in.next(), is(Token.OBJECT_END));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	@Test
	public void testSingleNumber()
		throws Exception
	{
		Map<String, Object> data = createMap();
		data.put("key", 12.0);

		StreamingInput in = resolve(data);

		assertThat(in.peek(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is(ConfigKey.NAME));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is(""));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readFloat(), is(12.0f));

		assertThat(in.peek(), is(Token.OBJECT_END));
		assertThat(in.next(), is(Token.OBJECT_END));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	@Test
	public void testSubObject()
		throws Exception
	{
		Map<String, Object> data = createMap();
		data.put("key.sub", "value1");

		StreamingInput in = resolve(data);

		assertThat(in.peek(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is(ConfigKey.NAME));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is(""));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is(ConfigKey.NAME));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is("sub"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value1"));

		assertThat(in.peek(), is(Token.OBJECT_END));
		assertThat(in.next(), is(Token.OBJECT_END));

		assertThat(in.peek(), is(Token.OBJECT_END));
		assertThat(in.next(), is(Token.OBJECT_END));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	@Test
	public void testList()
		throws Exception
	{
		Map<String, Object> data = createMap();
		data.put("sub.0", "value1");
		data.put("sub.1", "value2");

		StreamingInput in = resolve(data);

		assertThat(in.peek(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is(ConfigKey.NAME));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is(""));

		assertThat(in.peek(), is(Token.KEY));
		assertThat(in.next(), is(Token.KEY));
		assertThat(in.readString(), is("sub"));

		assertThat(in.peek(), is(Token.LIST_START));
		assertThat(in.next(), is(Token.LIST_START));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value1"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value2"));

		assertThat(in.peek(), is(Token.LIST_END));
		assertThat(in.next(), is(Token.LIST_END));

		assertThat(in.peek(), is(Token.OBJECT_END));
		assertThat(in.next(), is(Token.OBJECT_END));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

}
