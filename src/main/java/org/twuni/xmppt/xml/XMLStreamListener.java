package org.twuni.xmppt.xml;

public interface XMLStreamListener {

	public void onStartTag( XMLElement element );

	public void onEndTag( XMLElement element );

	public void onText( XMLText text );

}
