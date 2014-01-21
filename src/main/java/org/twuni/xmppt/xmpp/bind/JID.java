package org.twuni.xmppt.xmpp.bind;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.util.XMLUtils;

public class JID {

	public static final String ELEMENT_NAME = "jid";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static JID from( XMLElement element ) {
		return new JID( element.content() );
	}

	private final String jid;

	public JID( String jid ) {
		this.jid = jid;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).content( XMLUtils.encodeAttribute( jid ) );
	}

}