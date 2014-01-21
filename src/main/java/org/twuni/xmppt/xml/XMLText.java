package org.twuni.xmppt.xml;

public class XMLText extends XMLEntity {

	public final String text;

	public XMLText( String text ) {
		this( null, text );
	}

	public XMLText( XMLElement parent, String text ) {
		super( parent );
		this.text = text;
	}

	@Override
	public String toString() {
		return text != null ? text : super.toString();
	}

}
