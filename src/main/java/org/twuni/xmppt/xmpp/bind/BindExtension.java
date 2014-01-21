package org.twuni.xmppt.xmpp.bind;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class BindExtension extends PacketTransformer implements Extension {

	@Override
	public PacketTransformer packet() {
		return null;
	}

	@Override
	public PacketTransformer iq() {
		return this;
	}

	@Override
	public PacketTransformer feature() {
		return this;
	}

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
