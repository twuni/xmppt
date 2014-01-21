package org.twuni.xmppt.xmpp.sasl;

import java.util.ArrayList;
import java.util.List;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLEntity;

public class SASLMechanisms {

	public static final String ELEMENT_NAME = "mechanisms";

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name );
	}

	public static SASLMechanisms from( XMLElement element ) {
		List<SASLMechanism> mechanisms = new ArrayList<SASLMechanism>();
		for( XMLEntity entity : element.children ) {
			if( entity instanceof XMLElement ) {
				XMLElement child = (XMLElement) entity;
				if( SASLMechanism.is( child ) ) {
					mechanisms.add( SASLMechanism.from( child ) );
				}
			}
		}
		int count = mechanisms.size();
		String [] m = new String [count];
		for( int i = 0; i < count; i++ ) {
			m[i] = mechanisms.get( i ).getMechanism();
		}
		return new SASLMechanisms( m );
	}

	private final Object [] mechanisms;

	public SASLMechanisms( String... mechanisms ) {
		int count = mechanisms.length;
		this.mechanisms = new Object [count];
		for( int i = 0; i < count; i++ ) {
			this.mechanisms[i] = new SASLMechanism( mechanisms[i] );
		}
	}

	@Override
	public String toString() {
		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );
		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, SASLAuthentication.NAMESPACE );
		return xml.content( mechanisms );
	}

}
