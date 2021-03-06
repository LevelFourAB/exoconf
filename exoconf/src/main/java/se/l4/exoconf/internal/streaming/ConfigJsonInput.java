package se.l4.exoconf.internal.streaming;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Base64;
import java.util.OptionalInt;

import se.l4.exobytes.streaming.AbstractStreamingInput;
import se.l4.exobytes.streaming.Token;

/**
 * Input for the custom JSON-like config format.
 */
public class ConfigJsonInput
	extends AbstractStreamingInput
{
	private static final char NULL = 0;

	private final Reader in;

	private final char[] buffer;
	private int position;
	private int limit;

	private final boolean[] lists;
	private int level;

	private Token token;
	private Object value;

	private boolean isKey;

	public ConfigJsonInput(Reader in)
	{
		this.in = in;

		lists = new boolean[20];
		buffer = new char[1024];
	}

	@Override
	public void close()
		throws IOException
	{
		in.close();
	}

	private void readWhitespace()
		throws IOException
	{
		while(true)
		{
			if(limit - position < 1)
			{
				if(! read(1))
				{
					return;
				}
			}

			char c = buffer[position];
			if(Character.isWhitespace(c) || c == ',')
			{
				position++;
			}
			else if(c == '#')
			{
				// Comment
				readUntilEndOfLine();
			}
			else
			{
				return;
			}
		}
	}

	private void readUntilEndOfLine()
		throws IOException
	{
		while(true)
		{
			if(limit - position < 1)
			{
				if(! read(1))
				{
					return;
				}
			}

			char c = buffer[position];
			if(c == '\n' || c == '\r')
			{
				return;
			}
			else
			{
				position++;
			}
		}
	}

	private char readNext()
		throws IOException
	{
		readWhitespace();

		return read();
	}

	private char read()
		throws IOException
	{
		if(limit - position < 1)
		{
			if(! read(1))
			{
				throw new EOFException();
			}
		}

		return buffer[position++];
	}

	private boolean read(int minChars)
		throws IOException
	{
		if(limit < 0)
		{
			return false;
		}
		else if(position + minChars < limit)
		{
			return true;
		}
		else if(limit >= position)
		{
			// If we have characters left we need to keep them in the buffer
			int stop = limit - position;
			System.arraycopy(buffer, position, buffer, 0, stop);
			limit = stop;
		}

		int read = in.read(buffer, limit, buffer.length - limit);
		position = 0;
		limit = read;

		if(read == -1)
		{
			return false;
		}

		if(read < minChars)
		{
			throw new IOException("Needed " + minChars + " but got " + read);
		}

		return true;
	}

	private Token toToken(int position)
		throws IOException
	{
		if(position > limit)
		{
			return Token.END_OF_STREAM;
		}

		char c = buffer[position];

		switch(c)
		{
			case '{':
				return Token.OBJECT_START;
			case '}':
				return Token.OBJECT_END;
			case '[':
				return Token.LIST_START;
			case ']':
				return Token.LIST_END;
		}

		if(c == 'n')
		{
			return checkString("null", false) ? Token.NULL : Token.VALUE;
		}

		return Token.VALUE;
	}

	private Object readNextValue()
		throws IOException
	{
		char c = readNext();
		if(c == '"')
		{
			// This is a string
			return readString(false);
		}
		else
		{
			StringBuilder value = new StringBuilder();
			_outer:
			while(true)
			{
				value.append(c);

				c = peekChar(false);
				switch(c)
				{
					case '}':
					case ']':
					case ',':
					case ':':
					case '=':
					case '\n':
					case '\r':
					case NULL: // EOF
						break _outer;
//					default:
//						if(Character.isWhitespace(c)) break _outer;
				}

				read();
			}

			return toObject(value.toString().trim());
		}
	}

	private Object toObject(String in)
	{
		if(in.equals("false"))
		{
			return false;
		}
		else if(in.equals("true"))
		{
			return true;
		}

		try
		{
			return Long.parseLong(in);
		}
		catch(NumberFormatException e)
		{
			try
			{
				return Double.parseDouble(in);
			}
			catch(NumberFormatException e2)
			{
			}
		}

		return in;
	}

	private String readString(boolean readStart)
		throws IOException
	{
		StringBuilder key = new StringBuilder();
		char c = read();
		if(readStart)
		{
			if(c != '"') throw new IOException("Expected \", but got " + c);
			c = read();
		}

		while(c != '"')
		{
			if(c == '\\')
			{
				readEscaped(key);
			}
			else
			{
				key.append(c);
			}

			c = read();
		}

		return key.toString();
	}

	private String readKey()
		throws IOException
	{
		StringBuilder key = new StringBuilder();
		char c = read();
		while(c != ':' && c != '=')
		{
			if(c == '\\')
			{
				readEscaped(key);
			}
			else if(! Character.isWhitespace(c))
			{
				key.append(c);
			}

			// Peek to see if we should end reading
			c = peekChar();
			if(c == '{' || c == '[')
			{
				// Next is object or list, break
				break;
			}

			// Read the actual character
			c = read();
		}

		return key.toString();
	}

	private void readEscaped(StringBuilder result)
		throws IOException
	{
		char c = read();
		switch(c)
		{
			case '\'':
				result.append('\'');
				break;
			case '"':
				result.append('"');
				break;
			case '\\':
				result.append('\\');
				break;
			case '/':
				result.append('/');
				break;
			case 'r':
				result.append('\r');
				break;
			case 'n':
				result.append('\n');
				break;
			case 't':
				result.append('\t');
				break;
			case 'b':
				result.append('\b');
				break;
			case 'f':
				result.append('\f');
				break;
			case 'u':
				// Unicode, read 4 chars and treat as hex
				read(4);
				String s = new String(buffer, position, 4);
				result.append((char) Integer.parseInt(s, 16));
				position += 4;
				break;
		}
	}

	@Override
	public Token next0()
		throws IOException
	{
		Token token = peek0();
		switch(token)
		{
			case OBJECT_END:
			case LIST_END:
				isKey = false;
				readNext();
				level--;
				return this.token = token;
			case OBJECT_START:
			case LIST_START:
				isKey = false;
				readNext();
				level++;
				lists[level] = token == Token.LIST_START;
				return this.token = token;
			case VALUE:
			{
				isKey = ! isKey && ! lists[level];

				if(isKey)
				{
					readWhitespace();
					if(peekChar() == '"')
					{
						value = readString(true);

						char next = peekChar();
						if(next == ':' || next == '=')
						{
							readNext();
						}
						else if(next == '{' || next == '[')
						{
							// Just skip
						}
						else
						{
							throw new IOException("Expected :, got " + next);
						}
					}
					else
					{
						// Case where keys do not include quotes
						value = readKey();
					}
				}
				else
				{
					value = readNextValue();

					// Check for trailing commas
					readWhitespace();
					char c = peekChar();
					if(c == ',') read();
				}

				return this.token = token;
			}
			case NULL:
			{
				isKey = false;

				value = null;
				Object s = readNextValue();
				if(! s.equals("null"))
				{
					throw new IOException("Invalid stream, encountered null value with trailing data");
				}

				// Check for trailing commas
				readWhitespace();
				char c = peekChar();
				if(c == ',') read();

				return this.token = token;
			}
		}

		return Token.END_OF_STREAM;
	}

	private char peekChar()
		throws IOException
	{
		return peekChar(true);
	}

	private char peekChar(boolean ws)
		throws IOException
	{
		if(ws) readWhitespace();

		if(limit - position < 1)
		{
			if(false == read(1))
			{
				return NULL;
			}
		}

		if(limit - position > 0)
		{
			return buffer[position];
		}

		return NULL;
	}

	private boolean checkString(String value, boolean ws)
		throws IOException
	{
		if(ws) readWhitespace();

		int length = value.length();

		if(limit - position < length)
		{
			if(false == read(length))
			{
				return false;
			}
		}

		if(limit - position < length)
		{
			// Still not enough data
			return false;
		}

		for(int i=0, n=length; i<n; i++)
		{
			if(buffer[position+i] != value.charAt(i))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public Token peek0()
		throws IOException
	{
		readWhitespace();

		if(limit - position < 1)
		{
			if(false == read(1)) return Token.END_OF_STREAM;
		}

		if(limit - position > 0)
		{
			Token token = toToken(position);
			if(token == Token.NULL && ! isKey && ! lists[level])
			{
				return Token.VALUE;
			}

			return token;
		}

		return Token.END_OF_STREAM;
	}

	@Override
	protected void skipValue()
		throws IOException
	{
		switch(peek())
		{
			case LIST_START:
			case LIST_END:
			case OBJECT_START:
			case OBJECT_END:
				next();
				skip();
				break;
			default:
				next();
		}
	}

	@Override
	public Token current()
	{
		return token;
	}

	@Override
	public OptionalInt getLength()
	{
		return OptionalInt.empty();
	}

	@Override
	public Object readDynamic0()
		throws IOException
	{
		markValueRead();
		return value;
	}

	@Override
	public String readString()
		throws IOException
	{
		markValueRead();
		return String.valueOf(value);
	}

	@Override
	public boolean readBoolean()
		throws IOException
	{
		markValueRead();
		return (Boolean) value;
	}

	@Override
	public double readDouble()
		throws IOException
	{
		markValueRead();
		return ((Number) value).doubleValue();
	}

	@Override
	public float readFloat()
		throws IOException
	{
		markValueRead();
		return ((Number) value).floatValue();
	}

	@Override
	public long readLong()
		throws IOException
	{
		markValueRead();
		return ((Number) value).longValue();
	}

	@Override
	public int readInt()
		throws IOException
	{
		markValueRead();
		return ((Number) value).intValue();
	}

	@Override
	public short readShort()
		throws IOException
	{
		markValueRead();
		return ((Number) value).shortValue();
	}

	@Override
	public byte readByte()
		throws IOException
	{
		markValueRead();
		return ((Number) value).byteValue();
	}

	@Override
	public char readChar()
		throws IOException
	{
		markValueRead();
		return (char) ((Number) value).shortValue();
	}

	@Override
	public byte[] readByteArray()
		throws IOException
	{
		/*
		 * JSON uses Base64 strings, so we need to decode on demand.
		 */
		String value = readString();
		return Base64.getDecoder().decode(value);
	}

	@Override
	public InputStream readByteStream()
		throws IOException
	{
		return new ByteArrayInputStream(readByteArray());
	}
}
