package org.twuni.xmppt.xml;

public class Push {

	public static class Identity {

		public static final String ATTRIBUTE_APPLICATION_ID = "app_id";
		public static final String ATTRIBUTE_TOKEN = "token";
		public static final String ELEMENT_NAME = "identity";

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

	public static Push register() {
		return new Push( ACTION_REGISTER );
	}

	public static Push register( String applicationID, String token ) {
		return new Push( ACTION_REGISTER, new Identity( applicationID, token ) );
	}

	private final String action;
	private final Object content;

	public Push( String action ) {
		this( action, null );
	}

	public Push( String action, Object content ) {
		this.action = action;
		this.content = content;
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( XMLBuilder.ATTRIBUTE_NAMESPACE, NAMESPACE );
		xml.attribute( ATTRIBUTE_ACTION, action );
		return xml.content( content );
	}

}
