package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.core.Error;

public class StreamError extends Error {

	public static StreamError from( XMLElement element ) {
		return new StreamError( element.content() );
	}

	public StreamError( Object content ) {
		super( Stream.NAMESPACE, null, 0, content );
	}

	public StreamError() {
		super( Stream.NAMESPACE );
	}

}
