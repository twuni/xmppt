package org.twuni.xmppt.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLStreamReader {

	private XMLStreamListener listener;
	private InputStream input;
	private String encoding;
	private final XmlPullParser xml;
	private final Stack<XMLElement> trace = new Stack<XMLElement>();

	public XMLStreamReader( InputStream input, String encoding, XMLStreamListener listener ) throws XmlPullParserException {
		this( input, encoding );
		setListener( listener );
	}

	public XMLStreamReader( InputStream input, XMLStreamListener listener ) throws XmlPullParserException {
		this( input );
		setListener( listener );
	}

	public XMLStreamReader( InputStream input ) throws XmlPullParserException {
		this( input, "UTF-8" );
	}

	public XMLStreamReader( InputStream input, String encoding ) throws XmlPullParserException {
		this();
		setInput( input, encoding );
	}

	public XMLStreamReader() throws XmlPullParserException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware( true );
		this.xml = factory.newPullParser();
	}

	public XMLStreamReader( XmlPullParser xml, XMLStreamListener listener ) {
		this( xml );
		setListener( listener );
	}

	public XMLStreamReader( XmlPullParser xml ) {
		this.xml = xml;
	}

	protected void onEndTag( String name ) {
		if( trace.isEmpty() || !trace.peek().name.equals( name ) ) {
			throw new IllegalStateException();
		}
		XMLElement tag = trace.pop();
		if( listener != null ) {
			listener.onEndTag( tag );
		}
	}

	protected void onStartTag( String name, Map<String, String> attributes ) {
		XMLElement parent = trace.isEmpty() ? null : trace.peek();
		XMLElement child = new XMLElement( parent, name, attributes );
		if( parent != null ) {
			parent.children.add( child );
		}
		trace.push( child );
		if( listener != null ) {
			listener.onStartTag( child );
		}
	}

	protected void onText( String text ) {
		XMLElement parent = trace.isEmpty() ? null : trace.peek();
		XMLText child = new XMLText( parent, text );
		if( parent != null ) {
			parent.children.add( child );
		}
		if( listener != null ) {
			listener.onText( child );
		}
	}

	private String getCurrentNamespace() throws XmlPullParserException, IOException {
		String prefix = xml.getPrefix();
		int nsStart = xml.getNamespaceCount( xml.getDepth() - 1 );
		int nsEnd = xml.getNamespaceCount( xml.getDepth() );
		for( int i = nsStart; i < nsEnd; i++ ) {
			String nsPrefix = xml.getNamespacePrefix( i );
			String nsUri = xml.getNamespaceUri( i );
			if( prefix != null ? prefix.equals( nsPrefix ) : nsPrefix == null ) {
				return nsUri;
			}
		}
		return null;
	}

	public void read() throws XmlPullParserException, IOException {
		for( int event = xml.getEventType(); event != XmlPullParser.END_DOCUMENT; event = xml.next() ) {
			switch( event ) {
				case XmlPullParser.START_TAG:
					Map<String, String> attributes = new HashMap<String, String>();
					for( int i = 0; i < xml.getAttributeCount(); i++ ) {
						attributes.put( xml.getAttributeName( i ), xml.getAttributeValue( i ) );
					}
					String namespace = getCurrentNamespace();
					if( namespace != null ) {
						attributes.put( XMLElement.ATTRIBUTE_NAMESPACE, namespace );
					}
					onStartTag( xml.getName(), attributes );
					break;
				case XmlPullParser.END_TAG:
					onEndTag( xml.getName() );
					break;
				case XmlPullParser.TEXT:
					onText( xml.getText() );
					break;
			}
		}
	}

	public void reset() throws XmlPullParserException {
		setInput( input, encoding );
	}

	public void setInput( InputStream in, String encoding ) throws XmlPullParserException {
		this.input = in;
		this.encoding = encoding;
		xml.setInput( in, encoding );
	}

	public void setListener( XMLStreamListener listener ) {
		this.listener = listener;
	}

}
