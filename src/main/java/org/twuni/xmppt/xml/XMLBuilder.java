package org.twuni.xmppt.xml;

import org.twuni.xmppt.xml.util.XMLUtils;

public class XMLBuilder {

	private final StringBuilder xml = new StringBuilder();
	private String prefix;
	private String elementName;
	public static final String ATTRIBUTE_NAMESPACE = "xmlns";

	public XMLBuilder( String elementName ) {
		reset( elementName );
	}

	public XMLBuilder( String prefix, String elementName ) {
		reset( prefix, elementName );
	}

	public XMLBuilder attribute( String key, String value ) {
		return attribute( null, key, value );
	}

	public XMLBuilder attribute( String prefix, String key, String value ) {

		if( key == null || value == null ) {
			return this;
		}

		xml.append( ' ' );

		if( prefix != null ) {
			xml.append( prefix ).append( ':' );
		}

		xml.append( key );
		xml.append( '=' );
		xml.append( '"' ).append( XMLUtils.encodeAttribute( value ) ).append( '"' );

		return this;

	}

	public String close() {
		xml.append( '/' ).append( '>' );
		return toString();
	}

	public String content( Object... content ) {

		if( content.length <= 0 || content[0] == null ) {
			return close();
		}

		xml.append( '>' );
		for( int i = 0, count = content.length; i < count; i++ ) {
			xml.append( String.valueOf( content[i] ) );
		}

		xml.append( end() );

		return toString();

	}

	public String end() {

		StringBuilder xml = new StringBuilder();

		xml.append( '<' ).append( '/' );

		if( prefix != null ) {
			xml.append( prefix ).append( ':' );
		}

		xml.append( elementName ).append( '>' );

		return xml.toString();

	}

	public String leaveOpen() {
		xml.append( '>' );
		return toString();
	}

	public XMLBuilder reset() {

		if( elementName == null ) {
			throw new IllegalArgumentException();
		}

		xml.setLength( 0 );

		xml.append( '<' );

		if( prefix != null ) {
			xml.append( prefix ).append( ':' );
		}

		xml.append( elementName );

		return this;

	}

	public XMLBuilder reset( String elementName ) {
		return reset( null, elementName );
	}

	public XMLBuilder reset( String prefix, String elementName ) {

		if( elementName == null ) {
			throw new IllegalArgumentException();
		}

		this.prefix = prefix;
		this.elementName = elementName;

		return reset();

	}

	@Override
	public String toString() {
		return xml.toString();
	}

}
