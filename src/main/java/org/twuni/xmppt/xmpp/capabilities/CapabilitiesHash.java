package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class CapabilitiesHash {

	public static final String ELEMENT_NAME = "c";
	public static final String NAMESPACE = "http://jabber.org/protocol/caps";
	public static final String ATTRIBUTE_HASH_TYPE = "hash";
	public static final String ATTRIBUTE_NODE = "node";
	public static final String ATTRIBUTE_HASH = "ver";
	public static final String HASH_SHA1 = "sha-1";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static CapabilitiesHash from( XMLElement element ) {
		return new CapabilitiesHash( element.attribute( ATTRIBUTE_NODE ), element.attribute( ATTRIBUTE_HASH_TYPE ), element.attribute( ATTRIBUTE_HASH ) );
	}

	private final String hashType;
	private final String node;
	private final String hash;

	public CapabilitiesHash( String node, String hashType, String hash ) {
		this.hashType = hashType;
		this.node = node;
		this.hash = hash;
	}

	public Capabilities query() {
		return new Capabilities( node, hash );
	}

	public String getHash() {
		return hash;
	}

	public String getHashType() {
		return hashType;
	}

	public String getNode() {
		return node;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE );
		xml.attribute( ATTRIBUTE_NODE, node );
		xml.attribute( ATTRIBUTE_HASH_TYPE, hashType );
		xml.attribute( ATTRIBUTE_HASH, hash );

		return xml.close();

	}

}
