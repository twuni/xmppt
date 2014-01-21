package org.twuni.xmppt.xmpp.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;
import org.twuni.xmppt.xmpp.PacketTransformerGroup;
import org.twuni.xmppt.xmpp.bind.BindExtension;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesExtension;
import org.twuni.xmppt.xmpp.sasl.SASLExtension;
import org.twuni.xmppt.xmpp.session.SessionExtension;

public class XMPPPacketConfiguration {

	public static PacketTransformer getDefault() {

		XMPPPacketConfiguration config = new XMPPPacketConfiguration();

		config.add( new SASLExtension() );
		config.add( new BindExtension() );
		config.add( new SessionExtension() );
		config.add( new CapabilitiesExtension() );

		return config.build();

	}

	private final List<Extension> extensions = new ArrayList<Extension>();

	public XMPPPacketConfiguration( Extension... extensions ) {
		add( extensions );
	}

	public XMPPPacketConfiguration add( Extension... extensions ) {
		this.extensions.addAll( Arrays.asList( extensions ) );
		return this;
	}

	public XMPPPacketConfiguration reset() {
		extensions.clear();
		return this;
	}

	public PacketTransformer build() {

		PacketTransformerGroup packets = new PacketTransformerGroup();
		PacketTransformerGroup iqs = new PacketTransformerGroup();
		PacketTransformerGroup features = new PacketTransformerGroup();

		for( Extension extension : extensions ) {
			packets.add( extension.packet() );
			iqs.add( extension.iq() );
			features.add( extension.feature() );
		}

		packets.add( new CorePacketTransformer( iqs, features ) );

		return packets;

	}

}
