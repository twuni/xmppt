package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SASLExtension implements Extension {

	private static final PacketTransformer PACKET = new SASLPacketTransformer();
	private static final PacketTransformer FEATURE = new SASLFeature();

	@Override
	public PacketTransformer feature() {
		return FEATURE;
	}

	@Override
	public PacketTransformer iq() {
		return null;
	}

	@Override
	public PacketTransformer packet() {
		return PACKET;
	}

	@Override
	public PacketTransformer presence() {
		return null;
	}

}
