/*
  EBTCalc
  (C) Copyright 2025, Eric Bergman-Terrell
  
  This file is part of EBTCalc.

    EBTCalc is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBTCalc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBTCalc.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.rpncalc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

public class EnterStringActivity extends Activity {
	private EditText stringValue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.enter_string);
		
		setTitle(String.format(getString(R.string.enter_string_title), getString(R.string.app_name)));

		stringValue = findViewById(R.id.StringValue);

		final Button okButton = findViewById(R.id.OK);
		
		okButton.setOnClickListener(v -> {
			final String stringText = stringValue.getEditableText().toString();

			final Intent returnData = new Intent();
			returnData.putExtra(StringLiterals.StringText, stringText);
			setResult(RESULT_OK, returnData);

			finish();
		});
		
		AndroidUtils.clickButtonWhenEnterPressed(stringValue, okButton);

		Button cancelButton = findViewById(R.id.Cancel);
		
		cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);

			finish();
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}
}
