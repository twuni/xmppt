package org.twuni.xmppt.xml.validator;

import org.junit.Assert;
import org.twuni.xmppt.xml.XMLElement;

public class ValidateOnEndTagHasAttributes extends BaseValidator {

	private boolean hasAttributes;

	@Override
	public void onEndTag( XMLElement element ) {
		hasAttributes = !element.attributes.isEmpty();
		if( !hasAttributes ) {
			Assert.fail( "Attributes should be accessible from within #onEndTag" );
		}
	}

	@Override
	public boolean isValid() {
		return hasAttributes;
	}

}
