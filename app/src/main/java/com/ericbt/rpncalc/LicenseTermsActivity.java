/*
  EBTCalc
  (C) Copyright 2023, Eric Bergman-Terrell
  
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
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;

public class LicenseTermsActivity extends Activity {
	private boolean allowCancel;
	private AlertDialog alertDialog;

	private static boolean active;
	
	public static boolean isActive() {
		return active;
	}

	@Override
	public void onBackPressed() {
		if (allowCancel) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		active = true;
		
		allowCancel = getIntent().getExtras().getBoolean(StringLiterals.AllowCancel);

		if (allowCancel) { 
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		setContentView(R.layout.license_terms_dialog);
		
		setTitle(String.format(getString(R.string.license_terms_title), getString(R.string.app_name)));

		final RadioButton acceptLicenseTerms = findViewById(R.id.AcceptLicenseTerms);
		acceptLicenseTerms.setChecked(Preferences.getUserAcceptedTerms());
		
		final RadioButton rejectLicenseTerms = findViewById(R.id.RejectLicenseTerms);
		rejectLicenseTerms.setChecked(!Preferences.getUserAcceptedTerms());
		
		Button okButton = findViewById(R.id.OKButton);

		okButton.setOnClickListener(v -> {
			final boolean userAcceptedTerms = acceptLicenseTerms.isChecked();

			Preferences.putUserAcceptedTerms(userAcceptedTerms);

			if (!userAcceptedTerms) {
				AlertDialog.Builder userRejectedTermsDialogBuilder = new AlertDialog.Builder(LicenseTermsActivity.this);
				userRejectedTermsDialogBuilder.setTitle(String.format("Rejected %s License Terms", getString(R.string.app_name)));
				userRejectedTermsDialogBuilder.setMessage(String.format("You rejected the %s license terms. Please uninstall %s immediately.", getString(R.string.app_name), getString(R.string.app_name)));
				userRejectedTermsDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
					alertDialog.dismiss();

					finish();

					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("EXIT", true);
					startActivity(intent);
				});

				userRejectedTermsDialogBuilder.setCancelable(false);

				alertDialog = userRejectedTermsDialogBuilder.create();
				alertDialog.show();
			}
			else {
				finish();
			}
		});
	}

	@Override
	protected void onStart() {
		active = true;
		
		super.onStart();
	}

	@Override
	protected void onStop() {
		active = false;
		
		super.onStop();
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
