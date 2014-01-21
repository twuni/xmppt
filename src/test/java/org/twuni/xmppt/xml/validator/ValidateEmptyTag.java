package org.twuni.xmppt.xml.validator;

import org.junit.Assert;
import org.twuni.xmppt.xml.XMLElement;
import org.twuni.xmppt.xml.XMLText;

public class ValidateEmptyTag extends BaseValidator {

	private boolean started;
	private boolean ended;

	@Override
	public void onText( XMLText text ) {
		Assert.fail( "#onText should not have been called." );
	}

	@Override
	public void onStartTag( XMLElement element ) {

		if( started || ended ) {
			Assert.fail( "#onStartTag should only have been called once." );
			return;
		}

		started = true;

	}

	@Override
	public void onEndTag( XMLElement element ) {

		if( !started ) {
			Assert.fail( "#onEndTag should have been called after #onStartTag" );
			return;
		}

		if( ended ) {
			Assert.fail( "#onEndTag should only have been called once" );
		}

		ended = true;

	}

	@Override
	public boolean isValid() {
		return started && ended;
	}

}
