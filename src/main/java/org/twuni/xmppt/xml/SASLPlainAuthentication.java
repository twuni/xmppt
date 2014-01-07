package org.twuni.xmppt.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

public class SASLPlainAuthentication extends SASLAuthentication {

	public static final String MECHANISM = "PLAIN";

	private final String content;

	public SASLPlainAuthentication( String identity, String password ) {
		this( identity, identity, password );
	}

	public SASLPlainAuthentication( String authz, String authc, String password ) {

		super( MECHANISM );

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
