package org.twuni.xmppt.xml.validator;

import org.junit.Assert;

public class ValidatePacketSequence implements XMPPPacketValidator {

	private final Class<?> [] packetTypes;
	private int index = 0;

	public ValidatePacketSequence( Class<?>... packetTypes ) {
		this.packetTypes = packetTypes;
	}

	@Override
	public void onPacketReceived( Object packet ) {
		Assert.assertEquals( packetTypes[index], packet.getClass() );
		index++;
	}

	@Override
	public boolean isValid() {
		return index == packetTypes.length;
	}

}