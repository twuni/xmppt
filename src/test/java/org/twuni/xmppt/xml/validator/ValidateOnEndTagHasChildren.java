package org.twuni.xmppt.xml.validator;

import org.junit.Assert;
import org.twuni.xmppt.xml.XMLElement;

public class ValidateOnEndTagHasChildren extends BaseValidator {

	private boolean hasChildren;

	@Override
	public void onEndTag( XMLElement element ) {
		hasChildren = !element.children.isEmpty();
		if( !hasChildren ) {
			Assert.fail( "Children should be accessible from within #onEndTag" );
		}
	}

	@Override
	public boolean isValid() {
		return hasChildren;
	}

}
