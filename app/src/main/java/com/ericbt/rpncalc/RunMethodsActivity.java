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

import java.util.Stack;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ericbt.rpncalc.javascript.MethodMetadata;

public class RunMethodsActivity extends Activity {
	private ProgrammableKeypadFragment programmableKeypadFragment;
	private DisplayFragment displayFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (getString(R.string.run_methods_activity_force_portrait).equals(StringLiterals.True)) {
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);    		
    	}
    	
		setContentView(R.layout.run_methods);
		
		String stackDataString = savedInstanceState != null ? 
				savedInstanceState.getString(StringLiterals.StackData) :
				getIntent().getExtras().getString(StringLiterals.StackData);
		
		Object obj = SerializeDeserialize.deserialize(stackDataString);
		
		if (obj instanceof Stack<?>) {
			@SuppressWarnings("unchecked")
			Stack<ResultWrapper> stackData = (Stack<ResultWrapper>) obj;
	
			displayFragment.setStackData(stackData);
			displayFragment.updateStack();
		}
		
		setTitle(String.format(getString(R.string.run_methods_title), getString(R.string.app_name)));
		
		setResult(RESULT_CANCELED);
	}

	@Override
	protected void onStart() {
		Log.i(StringLiterals.LogTag, "RunMethodsActivity.onStart");
		
		programmableKeypadFragment.setDisplayFragment(displayFragment);
		programmableKeypadFragment.updateUI();

		getActionBar().setDisplayHomeAsUpEnabled(true);

		super.onStart();
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		
		if (fragment instanceof DisplayFragment) {
			displayFragment = (DisplayFragment) fragment;
		}
		else if (fragment instanceof ProgrammableKeypadFragment) {
			programmableKeypadFragment = (ProgrammableKeypadFragment) fragment;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(StringLiterals.StackData, SerializeDeserialize.serialize(displayFragment.getStackData()));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        Button button = (Button) view;
        
		Intent editMethods = new Intent(this, EditMethodsActivity.class);
		editMethods.putExtra(StringLiterals.SourcePosition, ((MethodMetadata) button.getTag()).getPosition());
		startActivity(editMethods);

		super.onCreateContextMenu(menu, view, menuInfo);
	}

	private void updateStackAndFinish() {
		BackgroundTasks.cancel(false);
		
		Intent returnData = new Intent();
		returnData.putExtra(StringLiterals.StackData, SerializeDeserialize.serialize(displayFragment.getStackData()));
		setResult(RESULT_OK, returnData);
		
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			updateStackAndFinish();
			
			result = true;
		}
		
		return result;
	}

	@Override
	public void onBackPressed() {
		updateStackAndFinish();
	}
	
}
