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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.about_dialog);
		
		setTitle(String.format(getString(R.string.about_title), getString(R.string.app_name)));
		
        boolean isFreeVersion = Globals.isFreeVersion();
        
        Button upgrade = findViewById(R.id.Upgrade);
        upgrade.setEnabled(isFreeVersion);
        upgrade.setVisibility(isFreeVersion ? View.VISIBLE : View.INVISIBLE);
        
        if (isFreeVersion) {
        	upgrade.setOnClickListener(v -> new UpgradeDialog(AboutActivity.this).display());
        }
		
		Button readLicenseTermsButton = findViewById(R.id.ReadLicenseTerms);
		
		readLicenseTermsButton.setOnClickListener(v -> {
			final Intent intent = new Intent(AboutActivity.this, LicenseTermsActivity.class);
			intent.putExtra(StringLiterals.AllowCancel, true);
startActivity(intent);
		});
	
		TextView version = findViewById(R.id.Version);
		
		String versionName = MiscUtils.getVersionName();
		
		version.setText(String.format("%s, Version %s", getString(R.string.full_app_name), versionName));
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
