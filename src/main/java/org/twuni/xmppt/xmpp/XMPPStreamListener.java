package org.twuni.xmppt.xmpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLStreamListener;
import org.twuni.xmppt.xml.XMLText;
import org.twuni.xmppt.xmpp.stream.Stream;

public class XMPPStreamListener implements XMLStreamListener {

	private PacketListener packetListener;
	private final List<PacketTransformer> packetTransformers = new ArrayList<PacketTransformer>();

	public XMPPStreamListener( PacketListener packetListener, PacketTransformer... packetTransformers ) {
		this.packetListener = packetListener;
		this.packetTransformers.addAll( Arrays.asList( packetTransformers ) );
	}

	public XMPPStreamListener( PacketTransformer... packetTransformers ) {
		this( null, packetTransformers );
	}

	public void addPacketTransformer( PacketTransformer packetTransformer ) {
		packetTransformers.add( packetTransformer );
	}

	@Override
	public void onEndTag( XMLElement element ) {

		if( Stream.is( element ) ) {
			// Ignore this and just let the stream terminate.
			return;
		}

		for( PacketTransformer packetTransformer : packetTransformers ) {
			if( packetTransformer.matches( element ) ) {
				Object packet = packetTransformer.transform( element );
				if( packet != null && !( packet instanceof XMLElement ) ) {
					onPacketReceived( packet );
					return;
				}
			}
		}

	}

	protected void onPacketReceived( Object packet ) {
		if( packetListener != null ) {
			packetListener.onPacketReceived( packet );
		}
	}

	@Override
	public void onStartTag( XMLElement element ) {

		if( Stream.is( element ) ) {
			onPacketReceived( Stream.from( element ) );
			return;
		}

	}

	@Override
	public void onText( XMLText text ) {
		// TODO Auto-generated method stub
	}

	public void removePacketTransformer( PacketTransformer packetTransformer ) {
		packetTransformers.remove( packetTransformer );
	}

	public void setPacketListener( PacketListener packetListener ) {
		this.packetListener = packetListener;
	}

}
