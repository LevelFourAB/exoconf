package se.l4.exoconf.internal;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import se.l4.exobytes.Serializer;
import se.l4.exobytes.streaming.StreamingInput;
import se.l4.exobytes.streaming.StreamingOutput;
import se.l4.exobytes.streaming.Token;

/**
 * Serializer for {@link Path}.
 */
public class PathSerializer
	implements Serializer<Path>
{
	private final Path root;

	public PathSerializer(Path root)
	{
		this.root = root;
	}

	@Override
	public Path read(StreamingInput in)
		throws IOException
	{
		in.next(Token.VALUE);

		String file = in.readString();
		if(file == null) return null;

		Path temp = FileSystems.getDefault().getPath(file);
		if(temp.isAbsolute())
		{
			return temp;
		}
		else
		{
			return root.resolve(temp);
		}
	}

	@Override
	public void write(Path object, StreamingOutput stream)
		throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
