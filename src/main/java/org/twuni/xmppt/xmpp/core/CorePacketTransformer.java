package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;
import org.twuni.xmppt.xmpp.PacketTransformerGroup;
import org.twuni.xmppt.xmpp.capabilities.CapabilitiesFeature;
import org.twuni.xmppt.xmpp.iq.bind.BindFeature;
import org.twuni.xmppt.xmpp.iq.bind.BindPacketTransformer;
import org.twuni.xmppt.xmpp.iq.session.SessionFeature;
import org.twuni.xmppt.xmpp.iq.session.SessionPacketTransformer;
import org.twuni.xmppt.xmpp.sasl.SASLFeature;

public class CorePacketTransformer extends PacketTransformer {

	public static PacketTransformer getDefault() {
		return new CorePacketTransformer( new PacketTransformerGroup( new BindPacketTransformer(), new SessionPacketTransformer() ), new PacketTransformerGroup( new BindFeature(), new SessionFeature(), new SASLFeature(), new CapabilitiesFeature() ) );
	}

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
