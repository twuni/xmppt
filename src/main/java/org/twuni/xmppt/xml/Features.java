package org.twuni.xmppt.xml;

public class Features {

	public static final String DEFAULT_PREFIX = "stream";
	public static final String ELEMENT_NAME = "features";

	private final String prefix;
	private final Object [] content;

	public Features( Object... content ) {
		this( DEFAULT_PREFIX, content );
	}

	public Features( String prefix, Object... content ) {
		this.prefix = prefix;
		this.content = content;
	}

	@Override
	public String toString() {
		return new XMLBuilder( prefix, ELEMENT_NAME ).content( content );
	}

}
