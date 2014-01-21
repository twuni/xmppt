package org.twuni.xmppt.xmpp.bind;

import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class BindExtension implements Extension {

	@Override
	public PacketTransformer packet() {
		return null;
	}

	@Override
	public PacketTransformer iq() {
		return new BindPacketTransformer();
	}

	@Override
	public PacketTransformer feature() {
		return new BindFeature();
	}

}
