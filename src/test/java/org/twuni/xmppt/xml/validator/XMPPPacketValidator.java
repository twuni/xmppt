package org.twuni.xmppt.xml.validator;

import org.twuni.xmppt.xmpp.PacketListener;

public interface XMPPPacketValidator extends PacketListener {

	public boolean isValid();

}
