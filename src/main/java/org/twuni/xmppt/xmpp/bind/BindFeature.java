package org.twuni.xmppt.xmpp.bind;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class BindFeature extends PacketTransformer {

	@Override
	public boolean matches( XMLElement element ) {
		return element.belongsTo( Bind.NAMESPACE );
	}

	@Override
	public Object transform( XMLElement element ) {
		return Bind.from( element );
	}

}
