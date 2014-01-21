package org.twuni.xmppt.xml;

public abstract class XMLEntity {

	public final XMLElement parent;

	public XMLEntity() {
		this( null );
	}

	protected XMLEntity( XMLElement parent ) {
		this.parent = parent;
	}

}