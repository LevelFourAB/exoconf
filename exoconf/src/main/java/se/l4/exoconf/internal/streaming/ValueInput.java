package se.l4.exoconf.internal.streaming;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.OptionalInt;

import se.l4.exobytes.SerializationException;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.Token;

public class ValueInput
	implements StreamingInput
{
	private final Object value;
	private boolean used;
	private String key;

	public ValueInput(String key, Object value)
	{
		this.key = key;
		this.value = value;
	}

	@Override
	public void close()
		throws IOException
	{
		// Nothing to close
	}

	@Override
	public Token peek()
		throws IOException
	{
		return used ? Token.END_OF_STREAM : Token.VALUE;
	}

	@Override
	public Token next()
		throws IOException
	{
		if(used)
		{
			return Token.END_OF_STREAM;
		}
		else
		{
			used = true;
			return Token.VALUE;
		}
	}

	@Override
	public Token next(Token expected)
		throws IOException
	{
		Token token = next();
		if(expected != Token.VALUE)
		{
			throw new IOException(key + ": Expected "+ expected + " but got " + token);
		}

		return token;
	}

	@Override
	public void skip() throws IOException
	{
	}

	@Override
	public Token current()
	{
		return Token.VALUE;
	}

	@Override
	public void current(Token token)
	{
		if(current() != token)
		{
			throw new SerializationException("Expected " + token + " but is VALUE");
		}
	}

	@Override
	public OptionalInt getLength()
	{
		return OptionalInt.empty();
	}

	@Override
	public Object readDynamic()
	{
		return value;
	}

	@Override
	public String readString()
	{
		return String.valueOf(value);
	}

	@Override
	public boolean readBoolean()
	{
		return value instanceof Boolean
			? (Boolean) value
			: Boolean.parseBoolean(readString());
	}

	@Override
	public double readDouble()
	{
		return ((Number) value).doubleValue();
	}

	@Override
	public float readFloat()
	{
		return ((Number) value).floatValue();
	}

	@Override
	public long readLong()
	{
		return ((Number) value).longValue();
	}

	@Override
	public int readInt()
	{
		return ((Number) value).intValue();
	}

	@Override
	public short readShort()
	{
		return ((Number) value).shortValue();
	}

	@Override
	public byte readByte()
	{
		return ((Number) value).byteValue();
	}

	@Override
	public char readChar()
	{
		return (char) readShort();
	}

	@Override
	public byte[] readByteArray()
	{
		return (byte[]) value;
	}

	@Override
	public InputStream readByteStream()
	{
		return new ByteArrayInputStream(readByteArray());
	}
}
