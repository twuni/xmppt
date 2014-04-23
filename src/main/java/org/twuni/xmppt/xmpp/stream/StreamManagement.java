package org.twuni.xmppt.xmpp.stream;

import org.twuni.xmppt.xml.XMLBuilder;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xmpp.Extension;
import org.twuni.xmppt.xmpp.PacketTransformer;

public class StreamManagement implements Extension {

	static class Feature extends PacketTransformer {

		@Override
		public boolean matches( XMLElement element ) {
			return StreamManagement.is( element );
		}

		@Override
		public Object transform( XMLElement element ) {

			if( StreamManagement.is( element ) ) {
				return StreamManagement.from( element );
			}

			return element;

		}

	}

	static class Packet extends PacketTransformer {

		@Override
		public boolean matches( XMLElement element ) {
			return StreamManagement.is( element );
		}

		@Override
		public Object transform( XMLElement element ) {

			if( Acknowledgment.is( element ) ) {
				return Acknowledgment.from( element );
			}

			if( AcknowledgmentRequest.is( element ) ) {
				return AcknowledgmentRequest.from( element );
			}

			return element;

		}

	}

	public static final String NAMESPACE = "urn:xmpp:sm:3";
	public static final String ELEMENT_NAME = "sm";

	private static final PacketTransformer FEATURE = new Feature();
	private static final PacketTransformer PACKET = new Packet();
	private static final StreamManagement INSTANCE = new StreamManagement();

	public static boolean is( Object packet ) {
		return packet instanceof Acknowledgment || packet instanceof AcknowledgmentRequest;
	}

	public static boolean is( XMLElement element ) {
		return element.belongsTo( NAMESPACE );
	}

	public static StreamManagement from( XMLElement element ) {
		return INSTANCE;
	}

	@Override
	public PacketTransformer packet() {
		return PACKET;
	}

	@Override
	public PacketTransformer iq() {
		return null;
	}

	@Override
	public PacketTransformer feature() {
		return FEATURE;
	}

	@Override
	public String toString() {
		return new XMLBuilder( ELEMENT_NAME ).attribute( XMLElement.ATTRIBUTE_NAMESPACE, StreamManagement.NAMESPACE ).close();
	}

	@Override
	public PacketTransformer presence() {
		return null;
	}

}
