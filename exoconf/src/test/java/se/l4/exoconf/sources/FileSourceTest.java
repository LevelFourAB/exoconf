package se.l4.exoconf.sources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class FileSourceTest
{
	@Test
	public void testDirectKey()
		throws IOException
	{
		FileConfigSource source = FileConfigSource.readString("medium.width: 100");
		assertThat(source.getValue("medium.width"), is(100l));
		assertThat(source.getKeys("medium"), containsInAnyOrder("width"));
		assertThat(source.getKeys(""), containsInAnyOrder("medium"));
	}

	@Test
	public void testObject()
		throws IOException
	{
		FileConfigSource source = FileConfigSource.readString("medium: { width: 100, height: 100 }");
		assertThat(source.getValue("medium.width"), is(100l));
		assertThat(source.getKeys("medium"), containsInAnyOrder("width", "height"));
		assertThat(source.getKeys(""), containsInAnyOrder("medium"));
	}

	@Test
	public void testObjectOverridden()
		throws IOException
	{
		FileConfigSource source = FileConfigSource.readString("medium: { width: 100, height: 100 }\nmedium.width: 200");
		assertThat(source.getValue("medium.width"), is(200l));
		assertThat(source.getKeys("medium"), containsInAnyOrder("width", "height"));
		assertThat(source.getKeys(""), containsInAnyOrder("medium"));
	}

	@Test
	public void testObjectSub()
		throws IOException
	{
		FileConfigSource source = FileConfigSource.readString("thumbnails: { \n medium: { width: 100, height: 100 }\n }");
		assertThat(source.getValue("thumbnails.medium.width"), is(100l));
		assertThat(source.getKeys("thumbnails.medium"), containsInAnyOrder("width", "height"));
		assertThat(source.getKeys(""), containsInAnyOrder("thumbnails"));
	}

	@Test
	public void testList()
		throws IOException
	{
		FileConfigSource source = FileConfigSource.readString("list: [ one, two ]");
		assertThat(source.getValue("list.0"), is("one"));
		assertThat(source.getValue("list.1"), is("two"));
		assertThat(source.getKeys(""), containsInAnyOrder("list"));
	}

}
