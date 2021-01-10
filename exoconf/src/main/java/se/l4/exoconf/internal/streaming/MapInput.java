package se.l4.exoconf.internal.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.OptionalInt;

import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import se.l4.exobytes.streaming.AbstractStreamingInput;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.Token;
import se.l4.exoconf.ConfigKey;
import se.l4.exoconf.sources.ConfigKeys;
import se.l4.exoconf.sources.ConfigSource;

/**
 * Implementation of {@link StreamingInput} that works on a objects.
 */
public class MapInput
	extends AbstractStreamingInput
{
	private enum State
	{
		START,
		KEY,
		VALUE,
		END,
		DONE
	}

	private final String key;
	private final ConfigSource source;
	private final Iterator<String> iterator;

	private String currentKey;

	private State state;
	private State previousState;

	private StreamingInput subInput;

	public MapInput(ConfigSource source, String key)
	{
		this.key = key;
		state = State.START;

		this.source = source;

		// Combine current key name with sub keys
		MutableList<String> list = Lists.mutable.empty();
		list.add(ConfigKey.NAME);
		list.withAll(source.getKeys(key));
		this.iterator = list.iterator();
	}

	@Override
	public void close()
		throws IOException
	{
		// Nothing to close
	}

	public static StreamingInput resolveInput(ConfigSource source, String key)
	{
		RichIterable<String> keys = source.getKeys(key);
		if(keys.size() > 0)
		{
			IntIterable indexes = ConfigKeys.toList(keys);
			if(indexes.size() > 0)
			{
				return new ListInput(source, key, indexes);
			}

			return new MapInput(source, key);
		}

		Object value = source.getValue(key);
		if(value == null)
		{
			return new NullInput(key);
		}
		else
		{
			return new ValueInput(key, value);
		}
	}

	private StreamingInput resolveInput()
	{
		String newKey = key.isEmpty() ? currentKey : key + ConfigKeys.PATH_DELIMITER + currentKey;
		if(ConfigKey.NAME.equals(currentKey))
		{
			return new ValueInput(newKey, key);
		}

		return resolveInput(source, newKey);
	}

	@Override
	protected IOException raiseException(String message)
	{
		return new IOException(key + ": " + message);
	}

	@Override
	public Token peek0()
		throws IOException
	{
		switch(state)
		{
			case START:
				return Token.OBJECT_START;
			case KEY:
				return Token.VALUE;
			case VALUE:
				Token peeked = subInput.peek();
				if(peeked != Token.END_OF_STREAM)
				{
					return peeked;
				}
				else
				{
					advancePosition();
					return peek();
				}
			case END:
				return Token.OBJECT_END;
		}

		return Token.END_OF_STREAM;
	}

	@Override
	public Token next0()
		throws IOException
	{
		switch(state)
		{
			case START:
				// Check what the next state should be
				advancePosition();
				return Token.OBJECT_START;
			case KEY:
				setState(State.VALUE);
				subInput = resolveInput();
				return Token.VALUE;
			case VALUE:
				/*
				 * Value state, check the sub input until it returns null
				 */
				Token t = subInput.next();
				if(t == Token.END_OF_STREAM)
				{
					// Nothing left in the value, advance and check again
					advancePosition();
					return next();
				}

				setState(State.VALUE);
				return t;
			case END:
				setState(State.DONE);
				return Token.OBJECT_END;
		}

		return Token.END_OF_STREAM;
	}

	private void setState(State state)
	{
		previousState = this.state;
		this.state = state;
	}

	private void advancePosition()
	{
		if(iterator.hasNext())
		{
			currentKey = iterator.next();
			setState(State.KEY);
		}
		else
		{
			setState(State.END);
		}
	}

	@Override
	public Token current()
	{
		return subInput == null ? super.current() : subInput.current();
	}

	@Override
	public OptionalInt getLength()
	{
		return subInput == null ? OptionalInt.empty() : subInput.getLength();
	}

	@Override
	protected void skipValue()
		throws IOException
	{
		if(subInput != null)
		{
			subInput.skip();
			markValueRead();
		}
	}

	@Override
	public Object readDynamic0()
		throws IOException
	{
		switch(previousState)
		{
			case KEY:
				markValueRead();
				return currentKey;
			case VALUE:
				markValueRead();
				return subInput.readDynamic();
		}

		return null;
	}

	@Override
	public String readString()
		throws IOException
	{
		switch(previousState)
		{
			case KEY:
				markValueRead();
				return currentKey;
			case VALUE:
				markValueRead();
				return subInput.readString();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public boolean readBoolean()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readBoolean();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public double readDouble()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readDouble();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public float readFloat()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readFloat();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public long readLong()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readLong();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public int readInt()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
			markValueRead();
				return subInput.readInt();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public short readShort()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readShort();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public byte readByte()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readByte();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public char readChar()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readChar();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public byte[] readByteArray()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readByteArray();
			default:
				throw raiseException("Not reading a value");
		}
	}

	@Override
	public InputStream readByteStream()
		throws IOException
	{
		switch(previousState)
		{
			case VALUE:
				markValueRead();
				return subInput.readByteStream();
			default:
				throw raiseException("Not reading a value");
		}
	}
}
