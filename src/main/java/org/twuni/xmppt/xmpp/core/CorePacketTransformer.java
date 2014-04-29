package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;
import org.twuni.xmppt.xmpp.stream.Stream;

public class CorePacketTransformer extends PacketTransformer {

	private final PacketTransformer iqs;
	private final PacketTransformer presenceChildTransformer;

	public CorePacketTransformer( PacketTransformer iqs, PacketTransformer presenceChildTransformer ) {
		this.iqs = iqs;
		this.presenceChildTransformer = presenceChildTransformer;
	}

	@Override
	public boolean matches( XMLElement element ) {
		return element.getNamespace() == null || element.belongsTo( Stream.ROOT_NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {

		if( Presence.is( element ) ) {
			return Presence.from( element, presenceChildTransformer );
		}

		if( IQ.is( element ) ) {
			return IQ.from( element, iqs );
		}

		if( Message.is( element ) ) {
			return Message.from( element );
		}

		return element;

	}

}
