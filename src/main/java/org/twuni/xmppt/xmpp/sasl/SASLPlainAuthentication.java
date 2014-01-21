package org.twuni.xmppt.xmpp.sasl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.twuni.xmppt.xml.XMLElement;

public class SASLPlainAuthentication extends SASLAuthentication {

	public static final String MECHANISM = "PLAIN";

	public static boolean is( XMLElement element ) {
		return SASLAuthentication.is( element ) && MECHANISM.equals( element.attributes.get( ATTRIBUTE_MECHANISM ) );
	}

	public static SASLPlainAuthentication from( XMLElement element ) {
		return new SASLPlainAuthentication( element.children.iterator().next().toString() );
	}

	private final String authz;
	private final String authc;
	private final String password;
	private final String content;

	public SASLPlainAuthentication( String base64content ) {

		super( MECHANISM );
		this.content = base64content;

		byte [] content = Base64.decodeBase64( base64content );

		int a = 0, b = 0;
		for( int i = 0, length = content.length; i < length; i++ ) {
			if( content[i] == 0 ) {
				if( a > 0 ) {
					b = i;
					break;
				} else {
					a = i;
				}
			}
		}

		authz = new String( content, 0, a );
		authc = new String( content, a, b - a );
		password = new String( content, b, content.length - b );

	}

	public SASLPlainAuthentication( String identity, String password ) {
		this( identity, identity, password );
	}

	public String getAuthenticationString() {
		return authz;
	}

	public String getAuthorizationString() {
		return authc;
	}

	public String getPassword() {
		return password;
	}

	public SASLPlainAuthentication( String authz, String authc, String password ) {

		super( MECHANISM );

		this.authz = authz;
		this.authc = authc;
		this.password = password;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			out.write( authz.getBytes() );
			out.write( 0 );
			out.write( authc.getBytes() );
			out.write( 0 );
			out.write( password.getBytes() );
		} catch( IOException exception ) {
			// Impossible.
		}

		content = Base64.encodeBase64URLSafeString( out.toByteArray() );

	}

	@Override
	protected Object getContent() {
		return content;
	}

}
