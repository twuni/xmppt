package org.twuni.xmppt.xmpp.iq.bind;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class BindPacketTransformer extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Bind.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		if( Bind.is( element ) ) {
			return Bind.from( element );
		}
		return null;
	}

}