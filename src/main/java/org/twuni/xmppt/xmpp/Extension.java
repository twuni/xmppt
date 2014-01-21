package org.twuni.xmppt.xmpp;

public interface Extension {

	public PacketTransformer packet();

	public PacketTransformer iq();

	public PacketTransformer feature();

}
