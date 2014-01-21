package org.twuni.xmppt.xmpp;

import java.io.IOException;
import java.io.InputStream;

import org.twuni.xmppt.xml.XMLStreamReader;
import org.xmlpull.v1.XmlPullParserException;

public class XMPPStreamReaderThread extends Thread {

	private final InputStream in;
	private final PacketListener listener;

	public XMPPStreamReaderThread( InputStream in, PacketListener listener ) {
		super( "XMPP Reader" );
		this.in = in;
		this.listener = listener;
	}

	@Override
	public void run() {
		try {
			new XMLStreamReader( in, new XMPPStreamListener( listener, PacketTransformerGroup.getDefault() ) ).read();
		} catch( XmlPullParserException exception ) {
			// No worries.
		} catch( IOException exception ) {
			// No worries.
		}
	}

}
