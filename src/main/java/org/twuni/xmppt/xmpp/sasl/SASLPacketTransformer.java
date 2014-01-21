package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SASLPacketTransformer extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( SASLAuthentication.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {

		if( SASLSuccess.is( element ) ) {
			return SASLSuccess.from( element );
		}

		if( SASLPlainAuthentication.is( element ) ) {
			return SASLPlainAuthentication.from( element );
		}

		return null;

	}

}
