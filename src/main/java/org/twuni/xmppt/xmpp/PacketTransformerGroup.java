package org.twuni.xmppt.xmpp;

import java.util.Arrays;
import java.util.List;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.core.CorePacketTransformer;
import org.twuni.xmppt.xmpp.sasl.SASLPacketTransformer;

public class PacketTransformerGroup extends PacketTransformer {

	public static PacketTransformer getDefault() {
		return new PacketTransformerGroup( CorePacketTransformer.getDefault(), new SASLPacketTransformer() );
	}

	private final List<PacketTransformer> transformers;

	public PacketTransformerGroup( PacketTransformer... transformers ) {
		this.transformers = Arrays.asList( transformers );
	}

	@Override
	public boolean matches( XMLElement element ) {
		for( PacketTransformer transformer : transformers ) {
			if( transformer.matches( element ) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object transform( XMLElement element ) {
		for( PacketTransformer transformer : transformers ) {
			if( transformer.matches( element ) ) {
				Object packet = transformer.transform( element );
				if( packet != null ) {
					return packet;
				}
			}
		}
		return null;
	}

}
