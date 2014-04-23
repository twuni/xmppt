package org.twuni.xmppt.xml.validator;

import org.junit.Assert;
import org.twuni.xmppt.xml.XMLElement;

public class ValidateOnStartTagHasAttributes extends BaseValidator {

	private boolean hasAttributes;

	@Override
	public boolean isValid() {
		return hasAttributes;
	}

	@Override
	public void onStartTag( XMLElement element ) {
		hasAttributes = !element.attributes.isEmpty();
		if( !hasAttributes ) {
			Assert.fail( "Attributes should be accessible from within #onStartTag" );
		}
	}

}
