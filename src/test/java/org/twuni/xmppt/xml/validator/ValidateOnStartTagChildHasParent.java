package org.twuni.xmppt.xml.validator;

import org.junit.Assert;
import org.twuni.xmppt.xml.XMLElement;

public class ValidateOnStartTagChildHasParent extends BaseValidator {

	private boolean parentIsSet;

	@Override
	public boolean isValid() {
		return parentIsSet;
	}

	@Override
	public void onStartTag( XMLElement element ) {
		if( element.name.equals( "child" ) ) {
			if( element.parent == null || !element.parent.name.equals( "parent" ) ) {
				Assert.fail( "Child element should have its parent element set within #onStartTag" );
				return;
			}
			parentIsSet = true;
		}
	}

}
