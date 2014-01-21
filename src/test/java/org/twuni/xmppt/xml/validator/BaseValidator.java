package org.twuni.xmppt.xml.validator;

import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLText;

public class BaseValidator implements XMLStreamValidator {

	@Override
	public void onStartTag( XMLElement element ) {
		// By default, do nothing.
	}

	@Override
	public void onEndTag( XMLElement element ) {
		// By default, do nothing.
	}

	@Override
	public void onText( XMLText text ) {
		// By default, do nothing.
	}

	@Override
	public boolean isValid() {
		// By default, do not assume the stream is valid.
		return false;
	}

}