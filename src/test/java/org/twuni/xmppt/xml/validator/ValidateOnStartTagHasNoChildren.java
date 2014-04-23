package org.twuni.xmppt.xml.validator;

import org.junit.Assert;
import org.twuni.xmppt.xml.XMLElement;

public class ValidateOnStartTagHasNoChildren extends BaseValidator {

	private boolean hasChildren;

	@Override
	public boolean isValid() {
		return !hasChildren;
	}

	@Override
	public void onStartTag( XMLElement element ) {
		hasChildren = !element.children.isEmpty();
		if( hasChildren ) {
			Assert.fail( "Children should not be accessible from within #onStartTag" );
		}
	}

}
