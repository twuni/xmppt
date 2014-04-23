package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;
import org.twuni.xmppt.xmpp.core.Features;

public class StreamPacketTransformer extends PacketTransformer {

	private final PacketTransformer features;

	public StreamPacketTransformer( PacketTransformer features ) {
		this.features = features;
	}

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Stream.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {

		if( Stream.is( element ) ) {
			return Stream.from( element );
		}

		if( StreamError.is( element ) ) {
			return StreamError.from( element );
		}

		if( Features.is( element ) ) {
			return Features.from( element, features );
		}

		return null;

	}

}
