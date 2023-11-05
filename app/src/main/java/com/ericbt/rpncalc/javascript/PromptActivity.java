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

package com.ericbt.rpncalc.javascript;

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ericbt.rpncalc.AndroidUtils;
import com.ericbt.rpncalc.Globals;
import com.ericbt.rpncalc.R;
import com.ericbt.rpncalc.StringLiterals;
import com.ericbt.rpncalc.javascript.PromptItem.ItemType;

public class PromptActivity extends Activity {
	private static boolean active;
	
	public static boolean isActive() {
		return active;
	}

	private Button okButton;
	
	private boolean isTextBoxValid(PromptItem promptItem) {
		boolean isTextBoxValid = true;
		
		String regexPattern = promptItem.getRegexOrSpinnerValues();
		
		if (promptItem.getValue() != null && promptItem.getItemType() == ItemType.Value && regexPattern != null && regexPattern.trim().length() > 0) {
			try {
				isTextBoxValid = Pattern.matches(promptItem.getRegexOrSpinnerValues(), promptItem.getValue());
			}
			catch (Exception ex) {
				Log.e(StringLiterals.LogTag, "PromptAdapter.isTextBoxValid", ex);
			}
		}
		
		return isTextBoxValid;
	}

	private void enableOKButton() {
		boolean enabled = true;
		
		for (PromptItem promptItem : Prompt.getPromptItems()) {
			if (!promptItem.isValid()) {
				enabled = false;
				break;
			}
		}
		
		okButton.setEnabled(enabled);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		active = true;
		
		setContentView(R.layout.prompt);
		setTitle(String.format("%s - %s", Globals.getApplication().getString(R.string.app_name), this.getIntent().getStringExtra("Title")));
		
		okButton = (Button) findViewById(R.id.OK);
		
		okButton.setOnClickListener(v -> {
			finish();

			Prompt.setResultsAreAvailable(true);
		});
		
		Button cancelButton = (Button) findViewById(R.id.Cancel);
		
		cancelButton.setOnClickListener(v -> cancel());
		
		TableLayout fieldsTableLayout = (TableLayout) findViewById(R.id.FieldsTableLayout);
		
		for (final PromptItem promptItem : Prompt.getPromptItems()) {
			TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.prompt_table_row, null);
			
			final TextView label = (TextView) tableRow.findViewById(R.id.Label);
			label.setText(String.format("%s:", promptItem.getLabelText()));

			final Spinner spinner = (Spinner) tableRow.findViewById(R.id.SpinnerField);
			
			final EditText editText = (EditText) tableRow.findViewById(R.id.TextField);
			
			ItemType itemType = promptItem.getItemType();
			
			editText.setVisibility(itemType == ItemType.Value ? View.VISIBLE : View.GONE);
			spinner.setVisibility(itemType == ItemType.Select ? View.VISIBLE : View.GONE);

			AndroidUtils.clickButtonWhenEnterPressed(editText, okButton);
			
			if (itemType == ItemType.Value) {
				editText.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable editable) {
						promptItem.setValue(editable.toString());
						promptItem.setValid(isTextBoxValid(promptItem));
						label.setTextColor(promptItem.isValid() ? Color.BLACK : Color.RED);
						
						enableOKButton();
					}
		
					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					}
		
					@Override
					public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					}
				});
				
				editText.setText(promptItem.getValue());
			}
			else if (itemType == ItemType.Select) {
				String[] spinnerValues = promptItem.getRegexOrSpinnerValues().split("\\|");
				String[] spinnerValuesWithSelect = new String[spinnerValues.length + 1];
		
				spinnerValuesWithSelect[0] = tableRow.getContext().getString(R.string.spinner_select_prompt);

                System.arraycopy(spinnerValues, 0, spinnerValuesWithSelect, 1, spinnerValues.length);

				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						String newValue = (String) parent.getItemAtPosition(position);
						
						promptItem.setSpinnerIndex(position);
						promptItem.setValue(newValue);
						promptItem.setValid(position != 0);
						
						label.setTextColor(promptItem.isValid() ? Color.BLACK : Color.RED);
						
						enableOKButton();
					}
		
					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
				
				ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(tableRow.getContext(), R.layout.prompt_spinner_item, R.id.Value, spinnerValuesWithSelect);
				spinnerAdapter.setDropDownViewResource(R.layout.prompt_spinner_dropdown_item);
				spinner.setAdapter(spinnerAdapter);
				
				spinner.setSelection(promptItem.getSpinnerIndex());
			}
			
			fieldsTableLayout.addView(tableRow);
		}
	}

	private void cancel() {
		finish();
		
		Prompt.setPromptItems(null);
		
		Prompt.setResultsAreAvailable(true);
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
	public void onBackPressed() {
		cancel();

		super.onBackPressed();
	}
	
	public void enableOKButton(boolean enabled) {
		okButton.setEnabled(enabled);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(StringLiterals.LogTag, "***** CONFIGURATION *****");
		cancel();
		
		super.onConfigurationChanged(newConfig);
	}
	
}
