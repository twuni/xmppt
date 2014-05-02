package org.twuni.xmppt;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.twuni.nio.server.Transporter;
import org.twuni.nio.server.Writable;

public class TransporterTest extends Assert {

	private static class NamedBuffer implements Writable {

		private final String id;
		private final ByteArrayOutputStream out = new ByteArrayOutputStream();

		public NamedBuffer( String id ) {
			this.id = id;
		}

		public byte [] getBytes() {
			return out.toByteArray();
		}

		public String id() {
			return id;
		}

		@Override
		public String toString() {
			return out.toString();
		}

		@Override
		public int write( byte [] buffer ) {
			return buffer != null ? write( buffer, 0, buffer.length ) : 0;
		}

		@Override
		public int write( byte [] buffer, int offset, int length ) {
			out.write( buffer, offset, length );
			return length;
		}

	}

	private Transporter transporter;
	private NamedBuffer target;

	@Test
	public void available_shouldTransportPendingPackets() {
		String expected = "This is a good test.";
		transporter.transport( expected, target.id() );
		assertArrayEquals( new byte [0], target.getBytes() );
		transporter.available( target, target.id() );
		assertEquals( expected, target.toString() );
	}

	@Before
	public void createTransporter() {
		transporter = new Transporter();
		target = new NamedBuffer( "alice" );
	}

	@Test
	public void transport_shouldNotTransportPacketsToUnknownTarget() {
		transporter.transport( "This message should not have been delivered.", target.id() );
		assertArrayEquals( new byte [0], target.getBytes() );
	}

	@Test
	public void transport_shouldTransportPacketsToAvailableTarget() {
		String expected = "This is a good message.";
		transporter.available( target, target.id() );
		transporter.transport( expected, target.id() );
		assertEquals( expected, target.toString() );
	}

	@Test
	public void unavailable_shouldPreventPacketsFromBeingTransported() {
		transporter.unavailable( target.id() );
		transporter.transport( "This message should not have been delivered.", target.id() );
		assertArrayEquals( new byte [0], target.getBytes() );
	}

}
