package org.twuni.xmppt.xmpp;

public interface PacketListener {

	public void onPacketReceived( Object packet );

	public void onPacketSent( Object packet );

	public void onPacketException( Throwable exception );

}
