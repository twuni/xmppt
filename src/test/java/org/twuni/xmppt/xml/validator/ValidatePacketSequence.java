package org.twuni.xmppt.xml.validator;

import org.junit.Assert;

public class ValidatePacketSequence implements XMPPPacketValidator {

	private final Class<?> [] packetTypes;
	private int index = 0;

	public ValidatePacketSequence( Class<?>... packetTypes ) {
		this.packetTypes = packetTypes;
	}

	@Override
	public boolean isValid() {
		Assert.assertEquals( packetTypes.length, index );
		return index == packetTypes.length;
	}

	@Override
	public void onPacketException( Throwable exception ) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPacketReceived( Object packet ) {
		Assert.assertEquals( packetTypes[index], packet.getClass() );
		index++;
	}

	@Override
	public void onPacketSent( Object packet ) {
		// TODO Auto-generated method stub
	}

}
