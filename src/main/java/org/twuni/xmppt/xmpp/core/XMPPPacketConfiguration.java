package org.twuni.xmppt.xmpp.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;
import org.twuni.xmppt.xmpp.PacketTransformerGroup;
import org.twuni.xmppt.xmpp.bind.BindExtension;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesExtension;
import org.twuni.xmppt.xmpp.ping.PingExtension;
import org.twuni.xmppt.xmpp.sasl.SASLExtension;
import org.twuni.xmppt.xmpp.session.SessionExtension;
import org.twuni.xmppt.xmpp.stream.StreamManagement;
import org.twuni.xmppt.xmpp.stream.StreamPacketTransformer;

public class XMPPPacketConfiguration {

	private static PacketTransformer defaultConfiguration;

	public static PacketTransformer getDefault() {

		if( defaultConfiguration == null ) {

			XMPPPacketConfiguration config = new XMPPPacketConfiguration();

			config.add( new SASLExtension() );
			config.add( new BindExtension() );
			config.add( new PingExtension() );
			config.add( new SessionExtension() );
			config.add( new CapabilitiesExtension() );
			config.add( new StreamManagement() );

			defaultConfiguration = config.build();

		}

		return defaultConfiguration;

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

		packets.add( new StreamPacketTransformer( features ) );
		packets.add( new CorePacketTransformer( iqs ) );

		return packets;

	}

}
