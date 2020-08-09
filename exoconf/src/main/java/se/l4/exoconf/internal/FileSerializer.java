package se.l4.exoconf.internal;

import java.io.File;
import java.io.IOException;

import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Serializer for {@link File}.
 */
public class FileSerializer
	implements Serializer<File>
{
	private final File root;

	public FileSerializer(File root)
	{
		this.root = root;
	}

	@Override
	public File read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);

		String file = in.readString();
		if(file == null) return null;

		File temp = new File(file);
		if(temp.isAbsolute())
		{
			return temp;
		}
		else
		{
			return new File(root, file);
		}
	}

	@Override
	public void write(File object, StreamingOutput stream)
		throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
