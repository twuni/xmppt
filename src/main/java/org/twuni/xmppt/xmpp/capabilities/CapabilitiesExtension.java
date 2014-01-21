package org.twuni.xmppt.xmpp.capabilities;

import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class CapabilitiesExtension implements Extension {

	@Override
	public PacketTransformer packet() {
		return null;
	}

	@Override
	public PacketTransformer iq() {
		return null;
	}

	@Override
	public PacketTransformer feature() {
		return new CapabilitiesFeature();
	}

}
