module se.l4.exoconf {
	requires com.github.spotbugs.annotations;

	requires transitive java.validation;
	requires org.eclipse.collections.api;

	requires transitive se.l4.exobytes;
	requires transitive se.l4.ylem.io;

	exports se.l4.exoconf;
	exports se.l4.exoconf.sources;
}
