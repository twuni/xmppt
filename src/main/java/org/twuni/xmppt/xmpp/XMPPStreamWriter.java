package org.twuni.xmppt.xmpp;

import java.io.IOException;
import java.io.OutputStream;

public class XMPPStreamWriter {

	private final OutputStream out;
	private final String encoding;

	public XMPPStreamWriter( OutputStream out ) {
		this( out, "UTF-8" );
	}

	public XMPPStreamWriter( OutputStream out, String encoding ) {
		this.out = out;
		this.encoding = encoding;
	}

	public void write( Object packet ) throws IOException {
		out.write( String.valueOf( packet ).getBytes( encoding ) );
	}

}
