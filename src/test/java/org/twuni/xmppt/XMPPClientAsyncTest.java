package org.twuni.xmppt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class XMPPClientAsyncTest extends Assert {

	private final Object guard = new Object();

	@Ignore
	@Test
	public void asyncSanityCheck() throws IOException {

		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking( false );
		channel.connect( new InetSocketAddress( "localhost", 5223 ) );

		boolean running = true;
		Selector selector = channel.provider().openSelector();
		ByteBuffer inputBuffer = ByteBuffer.allocate( 16 * 1024 );
		ByteBuffer outputBuffer = ByteBuffer.allocate( 16 * 1024 );

		synchronized( guard ) {
			selector.wakeup();
			channel.register( selector, SelectionKey.OP_READ, channel );
		}

		while( running ) {

			synchronized( guard ) {
				// Delay execution until the guard is freed.
			}

			selector.select();

			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

			while( iterator.hasNext() ) {

				SelectionKey key = iterator.next();
				iterator.remove();
				SocketChannel connection = (SocketChannel) key.attachment();

				if( !connection.isConnected() || !connection.isOpen() ) {
					key.cancel();
					continue;
				}

				if( key.isValid() && key.isReadable() ) {
					inputBuffer.clear();
					int bytesRead = connection.read( inputBuffer );
					if( bytesRead <= 0 ) {
						connection.close();
						return;
					}
					inputBuffer.flip();
					byte [] data = new byte [inputBuffer.limit()];
					inputBuffer.get( data );
					// TODO: Process this data.
				}

				if( key.isValid() && key.isWritable() ) {
					outputBuffer.flip();
					if( outputBuffer.hasRemaining() ) {
						int bytesWritten = connection.write( outputBuffer );
						outputBuffer.clear();
						if( bytesWritten <= 0 ) {
							connection.close();
						}
					}
				}

			}

		}

	}

}
