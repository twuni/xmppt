package org.twuni.xmppt.xmpp;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.twuni.Logger;
import org.twuni.nio.server.Writable;
import org.twuni.xmppt.client.SocketFactory;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLElementParser;
import org.twuni.xmppt.xml.XMLEntity;
import org.twuni.xmppt.xmpp.core.XMPPPacketConfiguration;
import org.twuni.xmppt.xmpp.stream.Stream;

public class XMPPSocket implements Closeable, Flushable, Writable {

	static class Node {

		public final Object data;
		public Node next;

		public Node( Object data ) {
			this.data = data;
		}

	}

	private static Logger defaultLogger() {
		return new Logger( XMPPSocket.class.getName() );
	}

	public static final int DEFAULT_INPUT_BUFFER_SIZE = 32 * 1024;

	private static final XMLElementParser XML = new XMLElementParser();
	private static final PacketTransformer TRANSFORMER = XMPPPacketConfiguration.getDefault();

	private final Object guard = new Object();
	private final Socket socket;
	private final byte [] inputBuffer;

	private Logger log;
	private Node head;

	public XMPPSocket( Socket socket ) {
		this( socket, defaultLogger() );
	}

	public XMPPSocket( Socket socket, int inputBufferSize ) {
		this( socket, inputBufferSize, defaultLogger() );
	}

	public XMPPSocket( Socket socket, int inputBufferSize, Logger logger ) {
		this.socket = socket;
		inputBuffer = new byte [inputBufferSize];
		log = logger;
	}

	public XMPPSocket( Socket socket, Logger logger ) {
		this( socket, DEFAULT_INPUT_BUFFER_SIZE, logger );
	}

	public XMPPSocket( SocketFactory socketFactory, String host, int port ) throws IOException {
		this( socketFactory, host, port, defaultLogger() );
	}

	public XMPPSocket( SocketFactory socketFactory, String host, int port, boolean secure ) throws IOException {
		this( socketFactory, host, port, secure, defaultLogger() );
	}

	public XMPPSocket( SocketFactory socketFactory, String host, int port, boolean secure, Logger logger ) throws IOException {
		this( socketFactory.createSocket( host, port, secure ), logger );
	}

	public XMPPSocket( SocketFactory socketFactory, String host, int port, Logger logger ) throws IOException {
		this( socketFactory.createSocket( host, port ), logger );
	}

	public XMPPSocket( String host, int port ) throws IOException {
		this( host, port, defaultLogger() );
	}

	public XMPPSocket( String host, int port, boolean secure ) throws IOException {
		this( host, port, secure, defaultLogger() );
	}

	public XMPPSocket( String host, int port, boolean secure, Logger logger ) throws IOException {
		this( SocketFactory.getInstance(), host, port, secure, logger );
	}

	public XMPPSocket( String host, int port, Logger logger ) throws IOException {
		this( SocketFactory.getInstance(), host, port, logger );
	}

	@Override
	public void close() throws IOException {
		synchronized( guard ) {
			socket.close();
		}
	}

	private void consumeNextBytes() throws IOException {

		InputStream in = socket.getInputStream();

		byte [] buffer = inputBuffer;

		int size = in.read( buffer, 0, buffer.length );

		if( size <= 0 ) {
			return;
		}

		while( buffer[size - 1] != '>' ) {
			if( size >= buffer.length ) {
				buffer = Arrays.copyOf( buffer, buffer.length * 2 );
				if( log != null ) {
					log.debug( "BUFR(%d)", Integer.valueOf( buffer.length ) );
				}
			}
			size += in.read( buffer, size, buffer.length - size );
		}

		if( log != null ) {
			log.info( "RECV %s", new String( buffer, 0, size ) );
		}

		List<XMLElement> elements = XML.parse( buffer, 0, size );
		List<XMLElement> expanded = new ArrayList<XMLElement>();

		for( int i = 0; i < elements.size(); i++ ) {
			XMLElement element = elements.get( i );
			expanded.add( element );
			if( Stream.ELEMENT_NAME.equals( element.name ) ) {
				for( XMLEntity child : element.children ) {
					if( child instanceof XMLElement ) {
						expanded.add( (XMLElement) child );
					}
				}
			}
		}

		elements = expanded;

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
					if( tail == null ) {
						tail = head;
					}
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

	public boolean hasNext() {
		return isConnected() && !socket.isInputShutdown();
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	public boolean isWritable() {
		return isConnected() && !socket.isOutputShutdown();
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

	public <T> T nextPacket() throws IOException {
		return (T) next();
	}

	public <T> T nextPacket( Class<T> type ) throws IOException {
		return nextPacket( type, null );
	}

	public <T> T nextPacket( Class<T> type, PacketListener until ) throws IOException {
		for( Object packet = next(); packet != null; packet = next() ) {
			if( type.isInstance( packet ) ) {
				return (T) packet;
			} else if( until != null ) {
				until.onPacketReceived( packet );
			}
		}
		return null;
	}

	public void setLogger( Logger logger ) {
		log = logger;
	}

	@Override
	public int write( byte [] buffer ) {
		return write( buffer, 0, buffer.length );
	}

	@Override
	public int write( byte [] buffer, int offset, int length ) {
		if( socket.isClosed() || !socket.isConnected() || socket.isOutputShutdown() ) {
			return -1;
		}
		try {
			socket.getOutputStream().write( buffer, offset, length );
			return length;
		} catch( IOException exception ) {
			return -1;
		}
	}

	public void write( Object packet ) throws IOException {
		OutputStream out = socket.getOutputStream();
		String packetString = packet.toString();
		if( log != null ) {
			log.info( "SEND %s", packetString );
		}
		byte [] buffer = packetString.getBytes();
		out.write( buffer, 0, buffer.length );
	}

}
