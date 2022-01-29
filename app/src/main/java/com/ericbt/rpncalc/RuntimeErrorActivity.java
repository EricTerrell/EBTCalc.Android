/*
  EBTCalc
  (C) Copyright 2022, Eric Bergman-Terrell
  
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
import android.widget.TextView;

public class RuntimeErrorActivity extends Activity {
	private int sourcePosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.runtime_error);
		
		setTitle(String.format(getString(R.string.runtime_error_title), getString(R.string.app_name)));
		
		String text = getIntent().getExtras().getString(StringLiterals.Text);
		sourcePosition = getIntent().getExtras().getInt(StringLiterals.SourcePosition);

		TextView textView = (TextView) findViewById(R.id.Text);
		textView.setText(text);
		
		Button edit = (Button) findViewById(R.id.Edit);
		
		edit.setOnClickListener(view -> {
			finish();

			Intent editMethods = new Intent(RuntimeErrorActivity.this, EditMethodsActivity.class);
			editMethods.putExtra(StringLiterals.SourcePosition, sourcePosition);
			RuntimeErrorActivity.this.startActivity(editMethods);
		});

		Button settings = findViewById(R.id.Settings);
		
		settings.setOnClickListener(view -> {
			finish();

			RuntimeErrorActivity.this.startActivity(new Intent(RuntimeErrorActivity.this, SettingsActivity.class));
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
