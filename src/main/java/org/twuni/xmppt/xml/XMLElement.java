package org.twuni.xmppt.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class XMLElement extends XMLEntity {

	public static final String ATTRIBUTE_NAMESPACE = "xmlns";

	public final String prefix;
	public final String name;
	public final Map<String, String> attributes;
	public final Collection<XMLEntity> children;

	public XMLElement( String name ) {
		this( null, name );
	}

	public XMLElement( String name, Map<String, String> attributes ) {
		this( (String) null, name, attributes );
	}

	public XMLElement( String name, Map<String, String> attributes, Collection<XMLEntity> children ) {
		this( (String) null, name, attributes, children );
	}

	public XMLElement( String prefix, String name ) {
		this( prefix, name, null );
	}

	public XMLElement( String prefix, String name, Map<String, String> attributes ) {
		this( prefix, name, attributes, null );
	}

	public XMLElement( String prefix, String name, Map<String, String> attributes, Collection<XMLEntity> children ) {
		this( null, prefix, name, attributes, children );
	}

	public XMLElement( XMLElement parent, String name, Map<String, String> attributes ) {
		this( parent, null, name, attributes );
	}

	public XMLElement( XMLElement parent, String name, Map<String, String> attributes, Collection<XMLEntity> children ) {
		this( parent, null, name, attributes, children );
	}

	public XMLElement( XMLElement parent, String prefix, String name ) {
		this( parent, prefix, name, null );
	}

	public XMLElement( XMLElement parent, String prefix, String name, Map<String, String> attributes ) {
		this( parent, prefix, name, attributes, null );
	}

	public XMLElement( XMLElement parent, String prefix, String name, Map<String, String> attributes, Collection<XMLEntity> children ) {
		super( parent );
		this.prefix = prefix;
		this.name = name;
		this.attributes = attributes != null ? attributes : new HashMap<String, String>();
		this.children = children != null ? children : new ArrayList<XMLEntity>();
	}

	public String attribute( String attributeName ) {
		return attributes.get( attributeName );
	}

	public boolean belongsTo( String namespace ) {
		return namespace != null && namespace.equals( getNamespace() );
	}

	public String content() {
		StringBuilder content = new StringBuilder();
		for( XMLEntity child : children ) {
			content.append( child );
		}
		return content.toString();
	}

	public String getNamespace() {
		return attribute( ATTRIBUTE_NAMESPACE );
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( prefix, name );
		for( String attribute : attributes.keySet() ) {
			xml.attribute( attribute, attributes.get( attribute ) );
		}
		xml.content( children.toArray() );
		return xml.toString();
	}

}
