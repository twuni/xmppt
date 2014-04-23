package org.twuni.xmppt.xmpp.sasl;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class SASLFeature extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( SASLAuthentication.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {

		if( SASLMechanisms.is( element ) ) {
			return SASLMechanisms.from( element );
		}

		return element;

	}

}
