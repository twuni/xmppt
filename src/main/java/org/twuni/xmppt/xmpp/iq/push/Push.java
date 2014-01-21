package org.twuni.xmppt.xmpp.iq.push;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLEntity;

public class Push {

	public static class Identity {

		public static final String ATTRIBUTE_APPLICATION_ID = "app_id";
		public static final String ATTRIBUTE_TOKEN = "token";
		public static final String ELEMENT_NAME = "identity";

		public static boolean is( XMLElement element ) {
			return ELEMENT_NAME.equals( element.name );
		}

		public static Identity from( XMLElement element ) {
			return new Identity( element.attributes.get( ATTRIBUTE_APPLICATION_ID ), element.attributes.get( ATTRIBUTE_TOKEN ) );
		}

		private final String applicationID;
		private final String token;

		public Identity( String applicationID, String token ) {
			this.applicationID = applicationID;
			this.token = token;
		}

		@Override
		public String toString() {

			XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

			xml.attribute( ATTRIBUTE_APPLICATION_ID, applicationID );
			xml.attribute( ATTRIBUTE_TOKEN, token );

			return xml.close();

		}

	}

	public static final String ACTION_REGISTER = "register";
	public static final String ACTION_UNREGISTER = "unregister";
	public static final String ATTRIBUTE_ACTION = "action";
	public static final String NAMESPACE = "http://silentcircle.com/protocol/push/gcm";
	public static final String ELEMENT_NAME = "push";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static Push from( XMLElement element ) {
		String action = element.attribute( ATTRIBUTE_ACTION );
		for( XMLEntity entity : element.children ) {
			if( entity instanceof XMLElement ) {
				XMLElement child = (XMLElement) entity;
				if( Identity.is( child ) ) {
					return new Push( action, Identity.from( child ) );
				}
			}
		}
		return new Push( action, element.content() );
	}

	public static Push register() {
		return new Push( ACTION_REGISTER );
	}

	public static Push register( String applicationID, String token ) {
		return new Push( ACTION_REGISTER, new Identity( applicationID, token ) );
	}

	private final String action;
	private final Object [] content;

	public Push( String action ) {
		this( action, (Object []) null );
	}

	public Push( String action, Object... content ) {
		this.action = action;
		this.content = content;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, NAMESPACE );
		xml.attribute( ATTRIBUTE_ACTION, action );
		return xml.content( content );
	}

}
