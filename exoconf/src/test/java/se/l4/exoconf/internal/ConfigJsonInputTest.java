package se.l4.exoconf.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.Token;
import se.l4.exoconf.internal.streaming.ConfigJsonInput;

/**
 * Test for {@link ConfigJsonInput}.
 */
public class ConfigJsonInputTest
{
	/**
	 * Test reading without any object braces.
	 *
	 * @throws Exception
	 */
	@Test
	public void testObjectValuesWithoutBraces()
		throws Exception
	{
		String v = "\"key1\": \"value1\", \"key2\": \"value2\"";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key1"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value1"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value2"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	@Test
	public void testObjectValuesWithNull()
		throws Exception
	{
		String v = "\"key1\": null, \"key2\": normal";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key1"));

		assertThat(in.peek(), is(Token.NULL));
		assertThat(in.next(), is(Token.NULL));
		assertThat(in.readDynamic(), nullValue());

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("normal"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test reading without any object braces or commas.
	 *
	 * @throws Exception
	 */
	@Test
	public void testObjectValuesWithoutBracesAndCommas()
		throws Exception
	{
		String v = "\"key1\": \"value1\" \"key2\": \"value2\"";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key1"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value1"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value2"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test reading without any object braces or commas. Instead values are
	 * separated by a new line.
	 *
	 * @throws Exception
	 */
	@Test
	public void testKeyValueWithLinebreak()
		throws Exception
	{
		String v = "\"key1\": 22.0\n\"key2\": \"value2\"";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key1"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readFloat(), is(22.0f));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value2"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	@Test
	public void testObjectValuesWithNullKey()
		throws Exception
	{
		String v = "null: normal";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("null"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("normal"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test reading a string value that does not use quotes.
	 *
	 * @throws Exception
	 */
	@Test
	public void testValueNoQuotes()
		throws Exception
	{
		String v = "key: value with spaces";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value with spaces"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test reading where keys and values do not have any quotes.
	 *
	 * @throws Exception
	 */
	@Test
	public void testMultipleValuesNoQuotes()
		throws Exception
	{
		String v = "key: value with spaces\nkey2: another value 21.0";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value with spaces"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("another value 21.0"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test reading a key and value separated with equals.
	 *
	 * @throws Exception
	 */
	@Test
	public void testKeyWithEquals()
		throws Exception
	{
		String v = "key = value with spaces";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value with spaces"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test reading with a comment.
	 *
	 * @throws Exception
	 */
	@Test
	public void testMultipleValuesWithComment()
		throws Exception
	{
		String v = "key: value with spaces\n# This is a comment\nkey2: another value 21.0";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value with spaces"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("another value 21.0"));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test reading a string value that does not use quotes.
	 *
	 * @throws Exception
	 */
	@Test
	public void testOnlyComment()
		throws Exception
	{
		String v = "# comment";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test key without equal or colon.
	 *
	 * @throws Exception
	 */
	@Test
	public void testKeyThenObject()
		throws Exception
	{
		String v = "key { key2: value }";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value"));

		assertThat(in.peek(), is(Token.OBJECT_END));
		assertThat(in.next(), is(Token.OBJECT_END));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	/**
	 * Test key without equal or colon.
	 *
	 * @throws Exception
	 */
	@Test
	public void testKeyThenObjectWithNewline()
		throws Exception
	{
		String v = "key\n{ key2: value }";
		StreamingInput in = createInput(v);

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key"));

		assertThat(in.peek(), is(Token.OBJECT_START));
		assertThat(in.next(), is(Token.OBJECT_START));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("key2"));

		assertThat(in.peek(), is(Token.VALUE));
		assertThat(in.next(), is(Token.VALUE));
		assertThat(in.readString(), is("value"));

		assertThat(in.peek(), is(Token.OBJECT_END));
		assertThat(in.next(), is(Token.OBJECT_END));

		assertThat(in.peek(), is(Token.END_OF_STREAM));
		assertThat(in.next(), is(Token.END_OF_STREAM));
	}

	protected StreamingInput createInput(String in)
	{
		return new ConfigJsonInput(new StringReader(in));
	}
}
