package org.twuni.xmppt.xmpp.sasl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.twuni.xmppt.util.Base64;
import org.twuni.xmppt.xml.XMLElement;

public class SASLPlainAuthentication extends SASLAuthentication {

	public static SASLPlainAuthentication from( XMLElement element ) {
		return new SASLPlainAuthentication( element.children.iterator().next().toString() );
	}

	public static boolean is( XMLElement element ) {
		return SASLAuthentication.is( element ) && MECHANISM.equals( element.attributes.get( ATTRIBUTE_MECHANISM ) );
	}

	public static final String MECHANISM = "PLAIN";

	private final String authz;
	private final String authc;
	private final String password;
	private final String content;

	public SASLPlainAuthentication( String base64content ) {

		super( MECHANISM );
		content = base64content;

		byte [] content = Base64.decodeBase64( base64content );

		int a = 0, b = 0;
		for( int i = 0, length = content.length; i < length; i++ ) {
			if( content[i] == 0 ) {
				if( a > 0 ) {
					b = i;
					break;
				}
				a = i;
			}
		}
		a++;
		b++;

		authz = new String( content, 0, a - 1 );
		authc = new String( content, a, b - a - 1 );
		password = new String( content, b, content.length - b );

	}

	public SASLPlainAuthentication( String identity, String password ) {
		this( identity, identity, password );
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

		String base64content = Base64.encodeBase64URLSafeString( out.toByteArray() );
		int modulus = base64content.length() % 4;
		content = modulus != 0 ? base64content.concat( modulus == 2 ? "==" : "=" ) : base64content;

	}

	public String getAuthenticationString() {
		return authz;
	}

	public String getAuthorizationString() {
		return authc;
	}

	@Override
	protected Object getContent() {
		return content;
	}

	public String getPassword() {
		return password;
	}

}
