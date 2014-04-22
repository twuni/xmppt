package org.twuni.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Connection implements Writable {

	public static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

	private final SocketChannel client;
	private final Dispatcher dispatcher;
	private final EventHandler eventHandler;
	private final ByteBuffer inputBuffer;
	private final ByteBuffer outputBuffer;

	public Connection( SocketChannel client, Dispatcher dispatcher, EventHandler eventHandler, int inputBufferSize, int outputBufferSize ) {
		this.client = client;
		this.dispatcher = dispatcher;
		this.eventHandler = eventHandler;
		inputBuffer = ByteBuffer.allocateDirect( inputBufferSize );
		outputBuffer = ByteBuffer.allocateDirect( outputBufferSize );
	}

	public Connection( SocketChannel client, Dispatcher dispatcher, EventHandler eventHandler ) {
		this( client, dispatcher, eventHandler, DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE );
	}

	public abstract Object state();

	public ByteBuffer getInputBuffer() {
		return inputBuffer;
	}

	public ByteBuffer getOutputBuffer() {
		return outputBuffer;
	}

	public SocketChannel getClient() {
		return client;
	}

	public EventHandler getEventHandler() {
		return eventHandler;
	}

	public void cleanup() {

		ByteBuffer in = getInputBuffer();
		ByteBuffer out = getOutputBuffer();
		int inSize = in.capacity();
		int outSize = out.capacity();
		int size = inSize < outSize ? outSize : inSize;
		byte [] zeroes = new byte [size];

		synchronized( in ) {
			in.clear();
			in.put( zeroes, 0, in.limit() );
		}

		synchronized( out ) {
			out.clear();
			out.put( zeroes, 0, out.limit() );
		}

	}

	public int flush() throws IOException {
		ByteBuffer buffer = getOutputBuffer();
		buffer.flip();
		if( !buffer.hasRemaining() ) {
			return 0;
		}
		byte [] b = new byte [buffer.remaining()];
		buffer.get( b, 0, b.length );
		LOG.info( "SEND C/%s [%d bytes] %s", Integer.toHexString( hashCode() ), Integer.valueOf( b.length ), new String( b, 0, b.length ) );
		buffer.flip();
		int bytesWritten = getClient().write( buffer );
		buffer.clear();
		if( bytesWritten <= 0 ) {
			getClient().close();
			eventHandler.onDisconnected( this );
		}
		return bytesWritten;
	}

	@Override
	public int write( byte [] buffer ) {
		return write( buffer, 0, buffer.length );
	}

	private static final Logger LOG = new Logger( Connection.class.getName() );

	@Override
	public int write( byte [] buffer, int offset, int length ) {
		// FIXME: This could potentially throw a buffer overflow exception. Should that be handled
		// here?
		ByteBuffer out = getOutputBuffer();
		if( out.limit() <= 0 ) {
			out.limit( out.capacity() );
		}
		synchronized( out ) {
			out.put( buffer, offset, length );
		}
		dispatcher.requestWrite( this );
		return length;
	}

}
