# Exoconf

Exoconf is a small library to manage configuration for Java 9+. Exoconf 
supports turning configuration into objects using [Exobytes](https://github.com/LevelFourAB/exobytes)
by pulling configuration from different sources, like files in an easy to write
format and environment variables.

```java
Config config = Config.create()
  .addFile("example.conf")
  .build();

Optional<String> value = config.get("path.to.value", String.class);
```

## License

This project is licensed under the [MIT license](https://opensource.org/licenses/MIT),
see the file `LICENSE.md` for details.

## Usage via Maven

```xml
<dependency>
  <groupId>se.l4.exoconf</groupId>
  <artifactId>exoconf</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Configuration format

The format of configuration files is similar to JSON but more lenient in how
things are specified, such as not having to use quotes, colons (`:`) can be
excluded at times and equals can replaced `:`.

Example:

```
thumbnails {
  medium {
    width: 200
    height: 200
  }
}
```

If the above file is used the value can be fetched from the `Config`:

```java
Optional<Integer> width = config.get("thumbnails.medium.width", Integer.class);
```

## Environment overrides

Exoconf supports environment variables as a source, in which case they can be
used to provide new properties or override existing properties. When Exoconf
is asked for a property it will try to get a an environment variable according
to these rules:

* First the exact name will be tried, such as asking for `get("MEDIUM_WIDTH")`
  will look for `MEDIUM_WIDTH`
* Second all non-ASCII characters will be replaced with `_`, so that asking for
  `get("medium.width")` would match `medium_width`
* Third everything is uppercased, so that `get("medium.width")` would match
  `MEDIUM_WIDTH`.

For example, something like this can be used override the
`thumbnails.medium.width` value:

```
$ THUMBNAILS_MEDIUM_WIDTH=300 java ...
```

## Using serialization

Exoconf can turn configuration properties into an object by specifying a call
that uses serialization:

```java
@AnnotationSerialization
public class ThumbnailSize {
  @Expose
  public int width;

  @Expose
  public int height;
}
```

The class can then be specified when using `get`:

```java
Optional<ThumbnailSize> mediumSize = config.get("thumbnails.medium", ThumbnailSize.class);
```

## Validation of config properties

Exoconf supports validating objects using [Bean Validation](https://beanvalidation.org/).
To activate this set a `ValidatorFactory` when building the `Config` instance:

```java
Config.create()
  .withValidatorFactory(validatorFactory)
  .build();
```

When this factory is present it's possible to annotate fields in classes
with limits:

```java
@AnnotationSerialization
public class ThumbnailSize {
  @Expose
  @Min(100) @Max(2000)
  public int width;

  @Expose
  @Min(100) @Max(2000)
  public int height;
}
```

These limits will then be checked when `get` is called:

```java
Optional<ThumbnailSize> mediumSize = config.get("thumbnails.medium", ThumbnailSize.class);
```
