package org.twuni.xmppt.xml;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.twuni.xmppt.xml.validator.ValidateEmptyTag;
import org.twuni.xmppt.xml.validator.ValidateOnEndTagChildHasParent;
import org.twuni.xmppt.xml.validator.ValidateOnEndTagHasAttributes;
import org.twuni.xmppt.xml.validator.ValidateOnEndTagHasChildren;
import org.twuni.xmppt.xml.validator.ValidateOnStartTagChildHasParent;
import org.twuni.xmppt.xml.validator.ValidateOnStartTagHasAttributes;
import org.twuni.xmppt.xml.validator.ValidateOnStartTagHasNoChildren;
import org.twuni.xmppt.xml.validator.XMLStreamValidator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLStreamReaderTest extends Assert {

	@Test
	public void onStartTag_shouldNotHaveChildren() throws Exception {
		test( "<test>Hello</test>", new ValidateOnStartTagHasNoChildren() );
	}

	@Test
	public void onEndTag_shouldHaveChildren() throws Exception {
		test( "<test>Hello</test>", new ValidateOnEndTagHasChildren() );
	}

	@Test
	public void onStartTag_shouldHaveAttributes() throws Exception {
		test( "<test a=\"123\"/>", new ValidateOnStartTagHasAttributes() );
	}

	@Test
	public void onEndTag_shouldHaveAttributes() throws Exception {
		test( "<test a=\"123\"/>", new ValidateOnEndTagHasAttributes() );
	}

	@Test
	public void expandedEmptyTag_shouldTriggerAppropriateEvents() throws Exception {
		test( "<test></test>", new ValidateEmptyTag() );
	}

	@Test
	public void emptyTag_shouldTriggerAppropriateEvents() throws Exception {
		test( "<test/>", new ValidateEmptyTag() );
	}

	@Test
	public void onStartTag_shouldHaveParentWhenGivenChild() throws Exception {
		test( "<parent><child/></parent>", new ValidateOnStartTagChildHasParent() );
	}

	@Test
	public void onEndTag_shouldHaveParentWhenGivenChild() throws Exception {
		test( "<parent><child/></parent>", new ValidateOnEndTagChildHasParent() );
	}

	protected void test( String input, XMLStreamValidator validator ) throws Exception {

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware( true );
		XmlPullParser parser = factory.newPullParser();
		XMLStreamReader reader = new XMLStreamReader( parser );

		reader.setListener( validator );
		reader.setInput( new ByteArrayInputStream( input.getBytes( "UTF-8" ) ), "UTF-8" );

		reader.read();

		assertTrue( validator.isValid() );

	}

}
