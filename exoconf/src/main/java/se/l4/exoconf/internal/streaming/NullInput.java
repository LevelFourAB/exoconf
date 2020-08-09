package se.l4.exoconf.internal.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.OptionalInt;

import se.l4.exobytes.SerializationException;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.Token;

public class NullInput
	implements StreamingInput
{
	private boolean used;
	private String key;

	public NullInput(String key)
	{
		this.key = key;
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
		return used ? Token.END_OF_STREAM : Token.NULL;
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
			return Token.NULL;
		}
	}

	@Override
	public Token next(Token expected)
		throws IOException
	{
		Token token = next();
		if(expected != Token.NULL)
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
		return Token.NULL;
	}

	@Override
	public void current(Token token)
	{
		if(current() != token)
		{
			throw new SerializationException("Expected " + token + " but is NULL");
		}
	}

	@Override
	public OptionalInt getLength()
	{
		return OptionalInt.empty();
	}

	@Override
	public Object readDynamic()
		throws IOException
	{
		return null;
	}

	@Override
	public String readString()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public boolean readBoolean()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public double readDouble()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public float readFloat()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public long readLong()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public int readInt()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public short readShort()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public byte readByte()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public char readChar()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public byte[] readByteArray()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}

	@Override
	public InputStream readByteStream()
		throws IOException
	{
		throw new IOException("NULL value, can not read");
	}
}
