package org.twuni.xmppt.xml;

public class CapabilitiesHash {

	public static final String ELEMENT_NAME = "c";
	public static final String NAMESPACE = "http://jabber.org/protocol/caps";
	public static final String ATTRIBUTE_HASH_TYPE = "hash";
	public static final String ATTRIBUTE_NODE = "node";
	public static final String ATTRIBUTE_HASH = "ver";
	public static final String HASH_SHA1 = "sha-1";

	private final String hashType;
	private final String node;
	private final String hash;

	public CapabilitiesHash( String node, String hashType, String hash ) {
		this.hashType = hashType;
		this.node = node;
		this.hash = hash;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, NAMESPACE );
		xml.attribute( ATTRIBUTE_NODE, node );
		xml.attribute( ATTRIBUTE_HASH_TYPE, hashType );
		xml.attribute( ATTRIBUTE_HASH, hash );

		return xml.close();

	}

}
