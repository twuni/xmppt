package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SASLExtension implements Extension {

	@Override
	public PacketTransformer packet() {
		return new SASLPacketTransformer();
	}

	@Override
	public PacketTransformer iq() {
		return null;
	}

	@Override
	public PacketTransformer feature() {
		return new SASLFeature();
	}

}
