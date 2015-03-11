package org.twuni.xmppt.server;

import java.util.List;

import org.twuni.Logger;
import org.twuni.nio.server.Connection;
import org.twuni.nio.server.EventHandler;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLElementParser;

public class XMLEventHandler extends EventHandler {

	private static Logger defaultLogger() {
		return new Logger( XMLEventHandler.class.getName() );
	}

	private static final XMLElementParser XML = new XMLElementParser();

	private final Logger log;

	public XMLEventHandler() {
		this( defaultLogger() );
	}

	public XMLEventHandler( Logger logger ) {
		log = logger;
	}

	@Override
	public void onData( Connection connection, byte [] data ) {
		log.info( "RECV C/%s [%d bytes] %s", connection.id(), Integer.valueOf( data.length ), new String( data, 0, data.length ) );
		List<XMLElement> xml = XML.parse( data );
		for( XMLElement element : xml ) {
			onXMLElement( connection, element );
		}
	}

	public void onXMLElement( Connection connection, XMLElement element ) {
		log.debug( "XML C/%s %s", connection.id(), element );
	}

}
