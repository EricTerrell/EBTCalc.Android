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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.ericbt.rpncalc.javascript.ClassMetadata;
import com.ericbt.rpncalc.javascript.ClassMetadataArrayAdapter;
import com.ericbt.rpncalc.javascript.ClassMetadataComparitor;
import com.ericbt.rpncalc.javascript.MethodMetadata;
import com.ericbt.rpncalc.javascript.MethodMetadataArrayAdapter;
import com.ericbt.rpncalc.javascript.MethodMetadataComparitor;
import com.ericbt.rpncalc.javascript.SourceCode;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener;
import com.ericbt.rpncalc.javascript.SyntaxIssue;
import com.ericbt.rpncalc.javascript.SyntaxIssue.Severity;

public class EditMethodsActivity extends Activity implements SourceCodeParseListener {
	private String originalSourceCode;
	private EditText sourceCode;
	private Button saveButton, cancel, updown, fixIssues;
	private Spinner selectClass, selectMethod;
	private TextView classMethodSeparator;
	private boolean up = false;
	private boolean created;
	private AlertDialog promptDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(StringLiterals.LogTag, "EditMethodsActivity.onCreate - begin");

		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (getString(R.string.edit_methods_activity_force_portrait).equals(StringLiterals.True)) {
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);    		
    	}
    	
		setContentView(R.layout.edit_methods);

		setTitle(String.format(getString(R.string.edit_methods_title), getString(R.string.app_name)));

		originalSourceCode = SourceCode.getUserCode();

		sourceCode = findViewById(R.id.SourceCode);
		sourceCode.setText(originalSourceCode);

		sourceCode.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				resetSpinners();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
				saveButton.setEnabled(sourceCodeHasChanged());
			}
		});

		selectClass = findViewById(R.id.SelectClass);
		selectMethod = findViewById(R.id.SelectMethod);
		classMethodSeparator = findViewById(R.id.ClassMethodSeparator);

		updateUI();

		cancel = findViewById(R.id.Cancel);

		cancel.setOnClickListener(v -> finish());

		saveButton = findViewById(R.id.Save);

		saveButton.setOnClickListener(view -> saveChanges());

		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			int position = extras.getInt(StringLiterals.SourcePosition);

			if (position != 0 && position < sourceCode.getEditableText().toString().length()) {
				sourceCode.setSelection(position);
			}
		}

		updown = findViewById(R.id.UpDown);

		if (updown != null) {
			updown.setOnClickListener(v -> {
				if (up) {
					sourceCode.setSelection(0);
				}
				else {
					sourceCode.setSelection(sourceCode.getEditableText().length());
				}

				up = !up;
			});
		}

		sourceCode.requestFocus();

		created = true;
		
		Log.i(StringLiterals.LogTag, "EditMethodsActivity.onCreate - end");
	}

	private void initIssuesUI(final List<SyntaxIssue> allIssues) {
		fixIssues = findViewById(R.id.FixIssues);
		
		fixIssues.setOnClickListener(view -> {
			PopupMenu menu = new PopupMenu(fixIssues.getContext(), view);

			int menuId = 0;
			int ordinalPosition = 0;

			for (SyntaxIssue syntaxIssue : allIssues) {
				final String formatString = view.getContext().getString(syntaxIssue.getSeverity() == Severity.Error ? R.string.issue_menu_error : R.string.issue_menu_warning);
				final String menuText = String.format(formatString, syntaxIssue.getMessage());

				menu.getMenu().add(Menu.NONE, menuId++, ordinalPosition++, menuText);
			}

			menu.setOnMenuItemClickListener(item -> {
				goToIssue(allIssues.get(item.getItemId()));

				return false;
			});

			menu.show();
		});
	}
	
	public void resetSpinners() {
		if (selectClass != null) {
			selectClass.setSelection(0);
		}
		
		if (selectMethod != null) {
			selectMethod.setSelection(0);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (created) {
			created = false;
			
			if (Globals.isFreeVersion()) {
				new UpgradeDialog(EditMethodsActivity.this).display();
			}
		}
	}

	private void updateUI() {
		Log.i(StringLiterals.LogTag, "EditMethodsActivity.updateUI begin");
		
		List<SyntaxIssue> allIssues = SourceCode.getSyntaxErrors();
		allIssues.addAll(SourceCode.getSyntaxWarnings());
		
		boolean haveIssues = allIssues.size() > 0;

		List<MethodMetadata> methodMetadata = SourceCode.getClassMetadata().size() > 0 ? SourceCode.getClassMetadata().get(0).getMethodMetadata() : new ArrayList<MethodMetadata>();
		
		initMethodSpinner(methodMetadata);
		initClassSpinner(SourceCode.filterClassMetadata(true));
		initIssuesUI(allIssues);
		
		
		fixIssues.setVisibility(haveIssues ? View.VISIBLE : View.GONE);
		selectClass.setVisibility(haveIssues ? View.GONE : View.VISIBLE);
		selectMethod.setVisibility(haveIssues ? View.GONE : View.VISIBLE);
		classMethodSeparator.setVisibility(haveIssues ? View.GONE : View.VISIBLE);

		if (haveIssues) {
			goToIssue(allIssues.get(0));
		}

		Log.i(StringLiterals.LogTag, "EditMethodsActivity.updateUI end");
	}

	private void goToIssue(SyntaxIssue syntaxIssue) {
		Log.i(StringLiterals.LogTag,
				String.format("EditMethodsActivity.goToIssue: lineSource: \"%s\" lineOffset: %d",
						syntaxIssue.getLineSource(),
						syntaxIssue.getLineOffset()));

		final int start = MiscUtils.getLinePosition(syntaxIssue.getLine() - 1,
				sourceCode.getEditableText().toString());

		final int stop = start + syntaxIssue.getLineSource().length();

		sourceCode.requestFocus();

		sourceCode.setSelection(start, stop);
	}
	
	private void enableDisableFields(boolean enabled) {
		View[] views = new View[] {
				cancel, selectClass, selectMethod, sourceCode, fixIssues, classMethodSeparator, updown, saveButton };
		
		for (View view : views) {
			if (view != null) {
				view.setEnabled(enabled);
			}
		}
	}
	
	@Override
	protected void onStart() {
		Log.i(StringLiterals.LogTag, "EditMethodsActivity.onStart");
		
		SourceCode.listen(this);
		
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.i(StringLiterals.LogTag, "EditMethodsActivity.onStop");
		
		SourceCode.unListen(this);

		super.onStop();
	}

	private void initClassSpinner(List<ClassMetadata> classMetadata) {
		ClassMetadata[] sortedClassMetadata = classMetadata.toArray(new ClassMetadata[classMetadata.size()]);
		Arrays.sort(sortedClassMetadata, new ClassMetadataComparitor());
		
		List<ClassMetadata> sortedClassMetadataList = new ArrayList<>(sortedClassMetadata.length);

		final int userCodeLength = SourceCode.getUserCode().length();

        for (final ClassMetadata aSortedClassMetadata : sortedClassMetadata) {
            ClassMetadata item = aSortedClassMetadata.shallowCopy();

            boolean isBuiltin = false;

            for (final MethodMetadata method : item.getMethodMetadata()) {
            	if (method.getPosition() > userCodeLength) {
            		isBuiltin = true;
            		break;
				}
			}

            if (!isBuiltin) {
				item.setUseDisplayClassName(false);
				sortedClassMetadataList.add(item);
			}
        }

		sortedClassMetadataList.add(0, new ClassMetadata(getString(R.string.spinner_select_prompt)));
		
		ClassMetadataArrayAdapter classSpinnerAdapter = new ClassMetadataArrayAdapter(this, R.layout.class_spinner_item, R.id.ClassName, sortedClassMetadataList, false);
		classSpinnerAdapter.setDropDownViewResource(R.layout.class_spinner_dropdown_item);
		selectClass.setAdapter(classSpinnerAdapter);
		
		selectClass.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View arg1, int position, long id) {
				if (position > 0) {
					ClassMetadataArrayAdapter adapter = (ClassMetadataArrayAdapter) adapterView.getAdapter();
					ClassMetadata classMetadata = adapter.getItem(position);
					
					initMethodSpinner(classMetadata.getMethodMetadata());
					
					if (selectMethod.getCount() >= 2) {
						selectMethod.setSelection(1);
					}
				}
				else {
					initMethodSpinner(new ArrayList<MethodMetadata>());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	private void initMethodSpinner(List<MethodMetadata> methodMetadata) {
		MethodMetadata[] sortedMethodMetadata = methodMetadata.toArray(new MethodMetadata[methodMetadata.size()]);
		Arrays.sort(sortedMethodMetadata, new MethodMetadataComparitor());
		
		List<MethodMetadata> sortedMethodMetadataList = new ArrayList<>(sortedMethodMetadata.length);

        Collections.addAll(sortedMethodMetadataList, sortedMethodMetadata);
		
		sortedMethodMetadataList.add(0, new MethodMetadata("", getString(R.string.spinner_select_prompt)));

		MethodMetadataArrayAdapter methodSpinnerAdapter = new MethodMetadataArrayAdapter(this, R.layout.method_spinner_item, R.id.MethodName, sortedMethodMetadataList);
		methodSpinnerAdapter.setDropDownViewResource(R.layout.method_spinner_dropdown_item);
		selectMethod.setAdapter(methodSpinnerAdapter);

		selectMethod.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View arg1, int position, long id) {
				if (position > 0) {
					MethodMetadataArrayAdapter adapter = (MethodMetadataArrayAdapter) adapterView.getAdapter();
					MethodMetadata methodMetadata = adapter.getItem(position);
					
					if (methodMetadata.getPosition() > 0 && methodMetadata.getPosition() < sourceCode.getEditableText().toString().length()) {
						sourceCode.setSelection(methodMetadata.getPosition());
					}
				}
			}
			
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	@Override
	public void sourceCodeChanged(SourceCodeStatus sourceCodeStatus) {
		if (sourceCodeStatus == SourceCodeStatus.ParsingCompleted) {
			if (SourceCode.getSyntaxErrors().size() == 0) {
				finish();
			}
			else {
				updateUI();
				enableDisableFields(true);
			}
		}
	}
	
	private void saveChanges() {
		enableDisableFields(false);

		originalSourceCode = sourceCode.getEditableText().toString();
		SourceCode.setUserCode(sourceCode.getEditableText().toString(), EditMethodsActivity.this);
	}

	private void promptToSaveChanges() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Save Changes");
		alertDialogBuilder.setMessage("Source code has changed.");
		
		alertDialogBuilder.setPositiveButton("Save Changes", (dialog, which) -> {
			promptDialog.dismiss();

			saveChanges();
		});
		
		alertDialogBuilder.setNegativeButton("Discard Changes", (dialog, which) -> {
			promptDialog.dismiss();

			EditMethodsActivity.this.finish();
		});
		
		promptDialog = alertDialogBuilder.create();
		
		promptDialog.show();
	}

	private boolean sourceCodeHasChanged() {
		String currentText = sourceCode.getEditableText().toString();

		return (!currentText.equals(originalSourceCode));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			if (sourceCodeHasChanged()) {
				promptToSaveChanges();
			}
			else {
				finish();
			}
			
			result = true;
		}
		
		return result;
	}

	@Override
	public void onBackPressed() {
		if (sourceCodeHasChanged()) {
			promptToSaveChanges();
		}
		else {
			super.onBackPressed();
		}
	}
	
}
