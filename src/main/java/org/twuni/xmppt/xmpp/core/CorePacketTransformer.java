package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class CorePacketTransformer extends PacketTransformer {

	private final PacketTransformer iqs;
	private final PacketTransformer features;

	public CorePacketTransformer( PacketTransformer iqs, PacketTransformer features ) {
		this.iqs = iqs;
		this.features = features;
	}

	@Override
	public boolean matches( XMLElement element ) {
		return element.getNamespace() == null;
	}

	@Override
	public Object transform( XMLElement element ) {

		if( Presence.is( element ) ) {
			return Presence.from( element );
		}

		if( IQ.is( element ) ) {
			return IQ.from( element, iqs );
		}

		if( Features.is( element ) ) {
			return Features.from( element, features );
		}

		return null;

	}

}
