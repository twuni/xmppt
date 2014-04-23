package org.twuni.xmppt.xmpp;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import org.twuni.nio.server.Logger;
import org.twuni.xmppt.client.SocketFactory;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLElementParser;
import org.twuni.xmppt.xmpp.core.XMPPPacketConfiguration;

public class XMPPSocket implements Closeable, Flushable {

	static class Node {

		public final Object data;
		public Node next;

		public Node( Object data ) {
			this.data = data;
		}

	}

	public static final int DEFAULT_INPUT_BUFFER_SIZE = 32 * 1024;

	private static final Logger LOG = new Logger( XMPPSocket.class.getName() );
	private static final XMLElementParser XML = new XMLElementParser();
	private static final PacketTransformer TRANSFORMER = XMPPPacketConfiguration.getDefault();
	private final Object guard = new Object();
	private final Socket socket;
	private final byte [] inputBuffer;
	private Node head;

	public XMPPSocket( String host, int port, boolean secure ) throws IOException {
		this( SocketFactory.createSocket( host, port, secure ) );
	}

	public XMPPSocket( String host, int port ) throws IOException {
		this( SocketFactory.createSocket( host, port ) );
	}

	public XMPPSocket( Socket socket ) {
		this( socket, DEFAULT_INPUT_BUFFER_SIZE );
	}

	public XMPPSocket( Socket socket, int inputBufferSize ) {
		this.socket = socket;
		this.inputBuffer = new byte [inputBufferSize];
	}

	public void write( Object packet ) throws IOException {
		OutputStream out = socket.getOutputStream();
		String packetString = packet.toString();
		LOG.info( "SEND %s", packetString );
		byte [] buffer = packetString.getBytes();
		out.write( buffer, 0, buffer.length );
	}

	public <T> T nextPacket() throws IOException {
		return (T) next();
	}

	public Object next() throws IOException {

		if( head == null || head.data == null ) {
			consumeNextBytes();
		}

		if( head != null && head.data != null ) {
			Object packet = head.data;
			head = head.next;
			return packet;
		}

		return null;

	}

	private void consumeNextBytes() throws IOException {

		InputStream in = socket.getInputStream();
		int size = in.read( inputBuffer, 0, inputBuffer.length );

		if( size <= 0 ) {
			return;
		}
		
		LOG.info( "RECV %s", new String( inputBuffer, 0, size ) );

		List<XMLElement> elements = XML.parse( inputBuffer, 0, size );

		synchronized( guard ) {

			Node tail = head;

			if( tail != null ) {
				while( tail.next != null ) {
					tail = tail.next;
				}
			}

			int count = elements.size();

			for( int i = 0; i < count; i++ ) {

				Object packet = TRANSFORMER.transform( elements.get( i ) );

				if( head == null ) {
					head = new Node( packet );
					tail = head;
				} else {
					tail.next = new Node( packet );
					tail = tail.next;
				}

			}

		}

	}

	@Override
	public void flush() throws IOException {
		synchronized( guard ) {
			socket.getOutputStream().flush();
		}
	}

	@Override
	public void close() throws IOException {
		synchronized( guard ) {
			socket.close();
		}
	}

}
