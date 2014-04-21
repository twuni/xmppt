package org.twuni.xmppt.xmpp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.twuni.xmppt.xml.XMLStreamReader;
import org.xmlpull.v1.XmlPullParserException;

public class XMPPStreamReader implements Runnable {

	private final InputStream in;
	private final PacketTransformer transformer;
	private final PacketListener listener;
	private boolean closed;

	public XMPPStreamReader( InputStream in, PacketTransformer transformer, PacketListener listener ) {
		this.in = in;
		this.transformer = transformer;
		this.listener = listener;
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void run() {
		closed = false;
		try {
			new XMLStreamReader( in, new XMPPStreamListener( listener, transformer ) ).read();
		} catch( EOFException exception ) {
			// We're done.
		} catch( XmlPullParserException exception ) {
			listener.onPacketException( exception );
		} catch( IOException exception ) {
			listener.onPacketException( exception );
		} catch( XMLStreamResetException exception ) {
			// We're done.
		}
		closed = true;
	}

}
