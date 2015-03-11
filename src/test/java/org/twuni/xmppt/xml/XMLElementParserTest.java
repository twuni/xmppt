package org.twuni.xmppt.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class XMLElementParserTest extends Assert {

	private static XMLElement parse( String in ) {
		return new XMLElementParser().parse( in ).get( 0 );
	}

	@Test
	public void parse_shouldDiscardTopLevelText() {
		List<XMLElement> entities = new XMLElementParser().parse( "Ignore this." );
		assertTrue( entities.isEmpty() );
	}

	@Test
	public void parse_shouldParseElementWithChildElement() {
		Collection<XMLEntity> children = new ArrayList<XMLEntity>();
		children.add( new XMLElement( "child" ) );
		XMLElement expected = new XMLElement( "parent", null, children );
		XMLElement actual = parse( "<parent><child/></parent>" );
		assertEquals( expected.name, actual.name );
		assertEquals( expected.children.size(), actual.children.size() );
	}

	@Test
	public void parse_shouldParseMultipleTopLevelElements() {
		List<XMLElement> entities = new XMLElementParser().parse( "<a/><b/><c/>" );
		assertEquals( 3, entities.size() );
	}

	@Test
	public void parse_shouldParseSimpleElement() {
		XMLElement expected = new XMLElement( "test" );
		XMLElement actual = parse( "<test/>" );
		assertEquals( expected.name, actual.name );
	}

	@Test
	public void parse_shouldParseSimpleElementWithOneAttribute() {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put( "a", "1" );
		XMLElement expected = new XMLElement( "test", attributes );
		XMLElement actual = parse( "<test a=\"1\"/>" );
		assertEquals( expected.name, actual.name );
		assertEquals( "1", expected.attribute( "a" ) );
	}

	@Test
	public void parse_shouldParseSimpleElementWithTwoAttributes() {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put( "a", "1" );
		attributes.put( "z", "9" );
		XMLElement expected = new XMLElement( "test", attributes );
		XMLElement actual = parse( "<test z=\"9\" a=\"1\"/>" );
		assertEquals( expected.name, actual.name );
		assertEquals( expected.attribute( "a" ), actual.attribute( "a" ) );
		assertEquals( expected.attribute( "z" ), actual.attribute( "z" ) );
	}

	@Test
	public void parse_shouldParseTopLevelElementsWithIgnoredSiblings() {
		List<XMLElement> entities = new XMLElementParser().parse( "Ignore <test/>, please." );
		assertEquals( 1, entities.size() );
	}

	@Test
	public void parse_xmppStream() {
		List<XMLElement> entities = new XMLElementParser().parse( "<stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" to=\"example.com\" xmlns=\"jabber:client\" version=\"1.0\"/>" );
		assertEquals( 1, entities.size() );
		System.out.println( entities );
	}

}
