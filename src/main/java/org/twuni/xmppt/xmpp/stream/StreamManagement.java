package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class StreamManagement extends PacketTransformer implements Extension {

	public static final String NAMESPACE = "urn:xmpp:sm:3";

	public static boolean is( Object packet ) {
		return packet instanceof Acknowledgment || packet instanceof AcknowledgmentRequest;
	}

	public static boolean is( XMLElement element ) {
		return NAMESPACE.equals( element.attribute( XMLElement.ATTRIBUTE_NAMESPACE ) );
	}

	@Override
	public PacketTransformer packet() {
		return this;
	}

	@Override
	public PacketTransformer iq() {
		return null;
	}

	@Override
	public PacketTransformer feature() {
		return null;
	}

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		if( Acknowledgment.is( element ) ) {
			return Acknowledgment.from( element );
		}
		if( AcknowledgmentRequest.is( element ) ) {
			return AcknowledgmentRequest.from( element );
		}
		return null;
	}

}
