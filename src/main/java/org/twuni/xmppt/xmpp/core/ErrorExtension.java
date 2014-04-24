package org.twuni.xmppt.xmpp.core;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class ErrorExtension extends PacketTransformer implements Extension {

	@Override
	public PacketTransformer feature() {
		return null;
	}

	@Override
	public PacketTransformer iq() {
		return this;
	}

	@Override
	public boolean matches( XMLElement element ) {
		return Error.is( element );
	}

	@Override
	public PacketTransformer packet() {
		return null;
	}

	@Override
	public PacketTransformer presence() {
		return null;
	}

	@Override
	public Object transform( XMLElement element ) {

		if( Error.is( element ) ) {
			return Error.from( element );
		}

		return element;

	}

}
