package org.twuni.xmppt.xml.util;

import java.io.CharArrayWriter;

public class XMLUtils {

	private static final String ENCODED_GREATER_THAN = "&gt;";
	private static final String ENCODED_LESS_THAN = "&lt;";
	private static final String ENCODED_DOUBLE_QUOTE = "&quot;";
	private static final String ENCODED_AMPERSAND = "&amp;";

	public static String encodeAttribute( String value ) {

		CharArrayWriter writer = new CharArrayWriter();

		int size = value.length();

		for( int i = 0; i < size; i++ ) {

			char c = value.charAt( i );

			switch( c ) {

				case '&':
					writer.append( ENCODED_AMPERSAND );
					break;

				case '"':
					writer.append( ENCODED_DOUBLE_QUOTE );
					break;

				case '<':
					writer.append( ENCODED_LESS_THAN );
					break;

				case '>':
					writer.append( ENCODED_GREATER_THAN );
					break;

				default:
					writer.append( c );
					break;

			}

		}

		return writer.toString();

	}

}
