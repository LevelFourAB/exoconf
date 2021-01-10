package se.l4.exoconf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.junit.jupiter.api.Test;

import se.l4.exobytes.AnnotationSerialization;
import se.l4.exobytes.Expose;

public class DefaultConfigTest
{
	@Test
	public void testSizeObject()
	{
		Config config = Config.create()
			.addStream(stream("medium: { width: 100, height: 100 }"))
			.build();

		Optional<Size> size = config.get("medium", Size.class);
		assertThat(size, notNullValue());

		Size actual = size.get();
		assertThat(actual, notNullValue());

		assertThat(actual.width, is(100));
		assertThat(actual.height, is(100));
	}

	@Test
	public void testSizeObjectViaKeys()
	{
		Config config = Config.create()
			.addProperty("medium.width", 100)
			.addProperty("medium.height", 100)
			.build();

		Optional<Size> size = config.get("medium", Size.class);
		assertThat(size, notNullValue());

		Size actual = size.get();
		assertThat(actual, notNullValue());

		assertThat(actual.width, is(100));
		assertThat(actual.height, is(100));
	}

	@Test
	public void testSizeObjectViaConfigWithPeriods()
	{
		Config config = Config.create()
			.addStream(stream("medium.width: 100\nmedium.height: 100"))
			.build();

		Optional<Size> size = config.get("medium", Size.class);
		assertThat(size, notNullValue());

		Size actual = size.get();
		assertThat(actual, notNullValue());

		assertThat(actual.width, is(100));
		assertThat(actual.height, is(100));
	}

	@Test
	public void testThumbnailsObject()
	{
		Config config = Config.create()
			.addStream(stream("thumbnails: { \n medium: { width: 100, height: 100 }\n }"))
			.build();

		Optional<Thumbnails> value = config.get("thumbnails", Thumbnails.class);
		assertThat(value, notNullValue());

		Thumbnails thumbs = value.get();
		assertThat(thumbs, notNullValue());

		assertThat(thumbs.medium, notNullValue());
	}

	@Test
	public void testInvalidSize()
	{
		Config config = Config.create()
			.withValidatorFactory(Validation.buildDefaultValidatorFactory())
			.addStream(stream("medium: { width: 100 }\n }"))
			.build();

		assertThrows(ConfigException.class, () -> {
			config.get("medium", Size.class);
		});
	}

	@Test
	public void testInvalidThumbnailsSize()
	{
		Config config = Config.create()
			.withValidatorFactory(Validation.buildDefaultValidatorFactory())
			.addStream(stream("thumbnails: { \n medium: { width: 100, height: 4000 }\n }"))
			.build();

		assertThrows(ConfigException.class, () -> {
			config.get("thumbnails", Thumbnails.class);
		});
	}

	@Test
	public void testListAccessor()
	{
		Config config = Config.create()
			.addStream(stream("values: [ \n \"one\", \n \"two\" \n ]"))
			.build();

		String value = config.get("values.0", String.class).get();
		assertThat(value, is("one"));

		value = config.get("values.1", String.class).get();
		assertThat(value, is("two"));
	}

	@Test
	public void testListAccessorWithSizes()
	{
		Config config = Config.create()
			.addStream(stream("values: [ \n { width: 100, height: 100 }, \n { width: 200, height: 200 } \n ]"))
			.build();

		Size value = config.get("values.0", Size.class).get();
		assertThat(value, notNullValue());
		assertThat(value.width, is(100));
		assertThat(value.height, is(100));

		value = config.get("values.1", Size.class).get();
		assertThat(value, notNullValue());
		assertThat(value.width, is(200));
		assertThat(value.height, is(200));
	}

	@Test
	public void testListAccessorWithSubPath()
	{
		Config config = Config.create()
			.addStream(stream("values: [ \n { width: 100, height: 100 } \n ]"))
			.build();

		Integer value = config.get("values.0.width", Integer.class).get();
		assertThat(value, is(100));
	}

	private InputStream stream(String in)
	{
		return new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8));
	}

	@AnnotationSerialization
	public static class Thumbnails
	{
		@Expose @Valid
		public Size medium;
		@Expose @Valid
		public Size large;
	}

	@AnnotationSerialization
	public static class Size
	{
		@Min(1) @Max(1000)
		@Expose
		public int width;

		@Min(1) @Max(1000)
		@Expose
		public int height;
	}
}
