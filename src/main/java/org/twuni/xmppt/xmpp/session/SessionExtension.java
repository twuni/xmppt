package org.twuni.xmppt.xmpp.session;

import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SessionExtension implements Extension {

	@Override
	public PacketTransformer packet() {
		return null;
	}

	@Override
	public PacketTransformer iq() {
		return new SessionPacketTransformer();
	}

	@Override
	public PacketTransformer feature() {
		return new SessionFeature();
	}

}
