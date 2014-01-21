package org.twuni.xmppt.xml.validator;

import org.twuni.xmppt.xml.XMLStreamListener;

public interface XMLStreamValidator extends XMLStreamListener {

	public boolean isValid();

}