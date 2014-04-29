package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;

public class Enabled {

	public static final String ELEMENT_NAME = "enabled";
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_RESUME = "resume";
	public static final String ATTRIBUTE_MAX = "max";
	public static final String ATTRIBUTE_LOCATION = "location";

	public static Enabled from( XMLElement element ) {

		String id = element.attribute( ATTRIBUTE_ID );
		String resume = element.attribute( ATTRIBUTE_RESUME );
		String max = element.attribute( ATTRIBUTE_MAX );
		String location = element.attribute( ATTRIBUTE_LOCATION );

		return new Enabled( id, location, max != null ? Integer.parseInt( max ) : 0, resume != null && Boolean.parseBoolean( resume ) );

	}

	public static boolean is( XMLElement element ) {
		return ELEMENT_NAME.equals( element.name ) && element.belongsTo( StreamManagement.NAMESPACE );
	}

	private final String id;
	private final boolean supportsSessionResumption;
	private final int maximumResumptionTime;
	private final String location;

	public Enabled() {
		this( null, null, 0, false );
	}

	public Enabled( String id, String location, int maximumResumptionTime, boolean supportsSessionResumption ) {
		this.id = id;
		this.location = location;
		this.maximumResumptionTime = maximumResumptionTime;
		this.supportsSessionResumption = supportsSessionResumption;
	}

	public String getLocation() {
		return location;
	}

	public int getMaximumResumptionTime() {
		return maximumResumptionTime;
	}

	public String id() {
		return id;
	}

	public boolean supportsSessionResumption() {
		return supportsSessionResumption;
	}

	@Override
	public String toString() {

		XMLBuilder xml = new XMLBuilder( ELEMENT_NAME );

		xml.attribute( XMLElement.ATTRIBUTE_NAMESPACE, StreamManagement.NAMESPACE );
		xml.attribute( ATTRIBUTE_ID, id );
		xml.attribute( ATTRIBUTE_LOCATION, location );

		if( maximumResumptionTime > 0 ) {
			xml.attribute( ATTRIBUTE_MAX, maximumResumptionTime );
		}

		if( supportsSessionResumption ) {
			xml.attribute( ATTRIBUTE_RESUME, Boolean.toString( supportsSessionResumption ) );
		}

		return xml.close();

	}

}
