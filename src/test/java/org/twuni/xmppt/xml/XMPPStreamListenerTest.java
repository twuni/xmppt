package org.twuni.xmppt.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.twuni.xmppt.xml.validator.ValidatePacketSequence;
import org.twuni.xmppt.xml.validator.XMPPPacketValidator;
import org.twuni.xmppt.xmpp.XMPPStreamListener;
import org.twuni.xmppt.xmpp.core.XMPPPacketConfiguration;
import org.twuni.xmppt.xmpp.core.IQ;
import org.twuni.xmppt.xmpp.core.Presence;
import org.twuni.xmppt.xmpp.sasl.SASLPlainAuthentication;
import org.twuni.xmppt.xmpp.stream.Stream;

public class XMPPStreamListenerTest extends Assert {

	private static InputStream read( String... strings ) {
		ByteArrayOutputStream t = new ByteArrayOutputStream();
		for( String s : strings ) {
			try {
				t.write( s.getBytes( "UTF-8" ) );
			} catch( IOException ignore ) {
				// Ignore.
			}
		}
		return new ByteArrayInputStream( t.toByteArray() );
	}

	@Test
	public void happyPath() throws Exception {

		XMPPPacketValidator validator = new ValidatePacketSequence( new Class<?> [] {
			Stream.class,
			SASLPlainAuthentication.class,
			Stream.class,
			IQ.class,
			IQ.class,
			Presence.class,
			Presence.class
		} );

		XMLStreamReader reader = new XMLStreamReader();
		reader.setListener( new XMPPStreamListener( validator, XMPPPacketConfiguration.getDefault() ) );
		reader.setInput( read( new String [] {
			"<stream:stream xmlns=\"jabber:client\" xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\" to=\"twuni.org\">",
			"<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\">YWxpY2UAYWxpY2UAY2hhbmdlaXQ</auth>",
			"<stream:stream xmlns=\"jabber:client\" xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\" to=\"twuni.org\">",
			"<iq id=\"1000-1\" type=\"set\"><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><resource>test</resource></bind></iq>",
			"<iq id=\"1000-2\" type=\"set\"><session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></iq>",
			"<presence id=\"1000-3\"/>",
			"<presence id=\"1000-4\" type=\"unavailable\"/>",
			"</stream:stream>",
			"</stream:stream>"
		} ), "UTF-8" );

		reader.read();

		assertTrue( validator.isValid() );

	}

}
