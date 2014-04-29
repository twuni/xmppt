package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Enable {

	public static final String ELEMENT_NAME = "enable";
	public static final String ATTRIBUTE_MAX = "max";
	public static final String ATTRIBUTE_RESUME = "resume";

	public static Enable from( XMLElement element ) {
		String max = element.attribute( ATTRIBUTE_MAX );
		String resume = element.attribute( ATTRIBUTE_RESUME );
		return new Enable( max != null ? Integer.parseInt( max ) : 0, resume != null && Boolean.parseBoolean( resume ) );
	}

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name ) && element.belongsTo( StreamManagement.NAMESPACE );
	}

	private final boolean supportsSessionResumption;
	private final int maximumResumptionTime;

	public Enable() {
		this( 0, false );
	}

	public Enable( int maximumResumptionTime, boolean supportsSessionResumption ) {
		this.maximumResumptionTime = maximumResumptionTime;
		this.supportsSessionResumption = supportsSessionResumption;
	}

	public int getMaximumResumptionTime() {
		return maximumResumptionTime;
	}

	public boolean supportsSessionResumption() {
		return supportsSessionResumption;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, StreamManagement.NAMESPACE );

		if( maximumResumptionTime > 0 ) {
			xml.attribute( ATTRIBUTE_MAX, maximumResumptionTime );
		}

		if( supportsSessionResumption ) {
			xml.attribute( ATTRIBUTE_RESUME, Boolean.toString( supportsSessionResumption ) );
		}

		return xml.close();

	}

}
