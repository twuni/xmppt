package org.twuni.xmppt.xmpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.twuni.xmppt.xml.XMLElement;

public class PacketTransformerGroup extends PacketTransformer {

	private final List<PacketTransformer> transformers;

	public PacketTransformerGroup( PacketTransformer... transformers ) {
		this.transformers = new ArrayList<PacketTransformer>( Arrays.asList( transformers ) );

	}

	public void add( PacketTransformer transformer ) {
		if( transformer != null ) {
			transformers.add( transformer );
		}
	}

	public void remove( PacketTransformer transformer ) {
		if( transformer != null ) {
			transformers.remove( transformer );
		}
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
				if( packet != null && !( packet instanceof XMLElement ) ) {
					return packet;
				}
			}
		}
		return element;
	}

}
