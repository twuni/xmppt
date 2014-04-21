package org.twuni.xmppt.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class XMLElementParser {

	static enum State {
		START_DOCUMENT,
		END_DOCUMENT,
		START_ELEMENT,
		ELEMENT_CONTENT,
		END_ELEMENT,
		START_ATTRIBUTE,
		END_ATTRIBUTE
	}

	public List<XMLElement> parse( byte [] in ) {
		return parse( in, 0, in.length );
	}

	public List<XMLElement> parse( byte [] in, int offset, int length ) {

		List<XMLElement> root = new ArrayList<XMLElement>();
		Stack<XMLElement> tree = new Stack<XMLElement>();
		State state = State.START_DOCUMENT;
		String prefix = null;

		for( int i = 0; i < length; i++ ) {

			byte b = in[offset + i];
			int start = offset + i;
			int end = start + 1;

			switch( b ) {

				case '<':

					boolean starting = in[start + 1] != '/';

					if( !starting ) {
						start++;
						state = State.END_ELEMENT;
					} else {
						state = State.START_ELEMENT;
						prefix = null;
					}

					start++;
					end = start + 1;
					while( end < offset + length && in[end] != ' ' && in[end] != '/' && in[end] != '>' ) {
						if( in[end] == ':' ) {
							prefix = new String( in, start, end - start );
							start = end + 1;
						}
						end++;
					}

					String name = new String( in, start, end - start );

					i = end - 1;

					XMLElement parent = tree.isEmpty() ? null : tree.peek();

					if( starting ) {
						XMLElement prototype = new XMLElement( parent, name, null );
						if( parent != null ) {
							parent.children.add( prototype );
						}
						tree.push( prototype );
					} else {
						if( parent != null && parent.name.equals( name ) ) {
							tree.pop();
							if( tree.isEmpty() ) {
								root.add( parent );
							}
						}
					}

					break;

				case '>':

					if( state != State.END_ELEMENT ) {
						state = State.ELEMENT_CONTENT;
					}

					break;

				case '/':

					state = State.END_ELEMENT;
					i++;
					XMLElement top = tree.pop();
					if( tree.isEmpty() ) {
						root.add( top );
					}

					break;

				case ' ':

					if( state != State.START_ELEMENT ) {
						break;
					}
					while( start < offset + length && in[start] == ' ' ) {
						start++;
					}
					if( in[start] == '>' || in[start] == '/' ) {
						break;
					}
					end = start + 1;
					while( end < offset + length && in[end] != '=' ) {
						end++;
					}
					String attributeName = new String( in, start, end - start );
					start = end + 2;
					end = start + 1;
					while( end < offset + length && in[end] != '"' && in[end] != '\'' ) {
						end++;
					}
					String attributeValue = new String( in, start, end - start );
					i = end;

					if( !tree.isEmpty() ) {

						XMLElement x = tree.peek();

						if( !XMLElement.ATTRIBUTE_NAMESPACE.equals( attributeName ) ) {
							x.attributes.put( attributeName, attributeValue );
						} else {
							if( prefix == null ) {
								x.attributes.put( attributeName, attributeValue );
							}
						}

						int attributePrefixIndex = attributeName.indexOf( ':' );

						if( attributePrefixIndex > -1 && prefix != null ) {
							String namespace = attributeName.substring( attributePrefixIndex + 1 );
							if( namespace.equals( prefix ) ) {
								x.attributes.put( XMLElement.ATTRIBUTE_NAMESPACE, attributeValue );
							}
						}

					}

					break;

				default:

					while( end < offset + length && in[end] != '<' ) {
						end++;
					}
					i = end - 1;
					String text = new String( in, start, end - start );
					if( !tree.isEmpty() ) {
						tree.peek().children.add( new XMLText( tree.peek(), text ) );
					}

					break;

			}

		}

		XMLElement top = null;
		while( !tree.isEmpty() ) {
			top = tree.pop();
		}
		if( top != null ) {
			root.add( top );
		}

		return root;

	}

	public List<XMLElement> parse( String in ) {
		return parse( in.getBytes() );
	}

}
