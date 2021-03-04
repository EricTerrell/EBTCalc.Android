/*
  EBTCalc
  (C) Copyright 2015, Eric Bergman-Terrell
  
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
	private SourceCodeEditText sourceCode;
	private Button saveButton, cancel, updown, advanced, fixIssues;
	private Spinner selectClass, selectMethod;
	private TextView classMethodSeparator;
	private static final Pattern builtInCodeBegin = Pattern.compile("/\\*[\\s]*BUILT_IN_CODE_BEGIN[^\\*^/]*\\*/", Pattern.CASE_INSENSITIVE);
	private static final Pattern builtInCodeEnd = Pattern.compile("/\\*[\\s]*BUILT_IN_CODE_END[^\\*^/]*\\*/", Pattern.CASE_INSENSITIVE);
	private static final Pattern userCodeBegin = Pattern.compile("/\\*[\\s]*USER_CODE_BEGIN[^\\*^/]*\\*/", Pattern.CASE_INSENSITIVE);
	private static final Pattern userCodeEnd = Pattern.compile("/\\*[\\s]*USER_CODE_END[^\\*^/]*\\*/", Pattern.CASE_INSENSITIVE);
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

		originalSourceCode = SourceCode.getSourceCode();

		sourceCode = (SourceCodeEditText) findViewById(R.id.SourceCode);
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
		
		selectClass = (Spinner) findViewById(R.id.SelectClass);
		selectMethod = (Spinner) findViewById(R.id.SelectMethod);
		classMethodSeparator = (TextView) findViewById(R.id.ClassMethodSeparator);

		updateUI();

		cancel = (Button) findViewById(R.id.Cancel);

		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		saveButton = (Button) findViewById(R.id.Save);

		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				saveChanges();
			}
		});

		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			int position = extras.getInt(StringLiterals.SourcePosition);

			if (position != 0 && position < sourceCode.getEditableText().toString().length()) {
				sourceCode.setSelection(position);
			}
		}

		advanced = (Button) findViewById(R.id.Advanced);

		advanced.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				PopupMenu popupMenu = new PopupMenu(EditMethodsActivity.this, view);
				popupMenu.getMenuInflater().inflate(R.menu.edit_methods_menu, popupMenu.getMenu());
				
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						final int menuId = item.getItemId();
						
						if (menuId == R.id.UpdateBuiltIns) {
							updateSourceCode();
						}
						else if (menuId == R.id.Import) {
							importSourceCode();
						}
						else if (menuId == R.id.Export) {
							exportSourceCode();
						}
						
						return true;
					}
				});

				popupMenu.show();
			}
		});
		
		updown = (Button) findViewById(R.id.UpDown);

		if (updown != null) {
			updown.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (up) {
						sourceCode.setSelection(0);
					}
					else {
						sourceCode.setSelection(sourceCode.getEditableText().length());
					}
					
					up = !up;
				}
			});
		}

		created = true;
		
		Log.i(StringLiterals.LogTag, "EditMethodsActivity.onCreate - end");
	}

	private void initIssuesUI(final List<SyntaxIssue> allIssues) {
		fixIssues = (Button) findViewById(R.id.FixIssues);
		
		fixIssues.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				PopupMenu menu = new PopupMenu(fixIssues.getContext(), view);
				
				int menuId = 0;
				int ordinalPosition = 0;
				
				for (SyntaxIssue syntaxIssue : allIssues) {
					final String formatString = view.getContext().getString(syntaxIssue.getSeverity() == Severity.Error ? R.string.issue_menu_error : R.string.issue_menu_warning);
					final String menuText = String.format(formatString, syntaxIssue.getMessage());
					
					menu.getMenu().add(Menu.NONE, menuId++, ordinalPosition++, menuText);
				}
				
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						goToIssue(allIssues.get(item.getItemId()));
						
						return false;
					}
				});
				
				menu.show();
			}
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

	private void updateSourceCode() {
		updateBuiltInCode();
	}

	private void importSourceCode() {
		{
			try {
				String text = FileUtils.readFile(getImportExportFilePath());

				Toast message = Toast.makeText(EditMethodsActivity.this, String.format("Imported methods from %s", getImportExportFilePath()), Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
				message.show();

				sourceCode.setText(text);
			} catch (Exception ex) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditMethodsActivity.this);
				alertDialogBuilder.setTitle("Cannot Import File");
				alertDialogBuilder.setMessage(String.format("Cannot import %s.", getImportExportFilePath()));
				alertDialogBuilder.setPositiveButton("OK", null);

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		}
	}
	
	private void exportSourceCode() {
		try {
			FileUtils.writeFile(getImportExportFilePath(), sourceCode.getEditableText().toString());

			Toast message = Toast.makeText(EditMethodsActivity.this, String.format("Exported methods to %s", getImportExportFilePath()), Toast.LENGTH_SHORT);
			message.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
			message.show();
		} catch (Exception ex) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditMethodsActivity.this);
			alertDialogBuilder.setTitle("Cannot Export File");
			alertDialogBuilder.setMessage(String.format("Cannot export %s.", getImportExportFilePath()));
			alertDialogBuilder.setPositiveButton("OK", null);

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
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
		int linePosition = MiscUtils.getLinePosition(syntaxIssue.getLine() - 1, sourceCode.getEditableText().toString()) + syntaxIssue.getLineOffset() - 1;
		sourceCode.setSelection(Math.min(linePosition, sourceCode.getEditableText().length()));
	}
	
	private void updateBuiltInCode() {
		String currentCode = sourceCode.getEditableText().toString();
		
		// Find beginning and end of built-in code.
		Matcher beginBuiltInCodeMatcher = builtInCodeBegin.matcher(currentCode);
		Matcher endBuiltInCodeMatcher = builtInCodeEnd.matcher(currentCode);
		Matcher beginUserCodeMatcher = userCodeBegin.matcher(currentCode);
		Matcher endUserCodeMatcher = userCodeEnd.matcher(currentCode);
		
		if (beginBuiltInCodeMatcher.find() && endBuiltInCodeMatcher.find() && beginBuiltInCodeMatcher.start() < endBuiltInCodeMatcher.start() &&
			beginUserCodeMatcher.find() && endUserCodeMatcher.find() && beginUserCodeMatcher.start() < endUserCodeMatcher.start()) {
			String before = currentCode.substring(0, beginBuiltInCodeMatcher.start()).trim();
			String userCode = currentCode.substring(beginUserCodeMatcher.end() + 1, endUserCodeMatcher.start() - 1).trim();

			String after = "";
			
			if (endUserCodeMatcher.end() < currentCode.length()) {
				after = currentCode.substring(endUserCodeMatcher.end()).trim();
			}
			
			if (before.length() > 0) {
				before += "\n\n";
			}
			
			if (after.length() > 0) {
				after = "\n\n" + after;
			}
			
			if (userCode.length() > 0) {
				userCode += "\n\n";
			}
			
			final String newSourceCode = String.format("%s%s%s", before, SourceCode.getInitialSourceCode(this, userCode), after);
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditMethodsActivity.this);
			alertDialogBuilder.setTitle("Update Built-In Code");
			alertDialogBuilder.setMessage("Replace built-in code with the latest version?");
			alertDialogBuilder.setPositiveButton(getString(R.string.ok_button_text), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sourceCode.setText(newSourceCode);

					Toast message = Toast.makeText(EditMethodsActivity.this, "Updated built-in code", Toast.LENGTH_SHORT);
					message.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
					message.show();
				}
			});
			
			alertDialogBuilder.setNegativeButton(getString(R.string.cancel_button_text), null);

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
		else {
			final String newSourceCode = String.format("%s\n\n%s", SourceCode.getInitialSourceCode(this), sourceCode.getEditableText());
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditMethodsActivity.this);
			alertDialogBuilder.setTitle("Update Built-In Code");
			alertDialogBuilder.setMessage("Could not find existing built-in code. Insert built-in code at top?");
			alertDialogBuilder.setPositiveButton(getString(R.string.ok_button_text), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sourceCode.setText(newSourceCode);

					Toast message = Toast.makeText(EditMethodsActivity.this, "Updated built-in code", Toast.LENGTH_SHORT);
					message.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
					message.show();
				}
			});
			
			alertDialogBuilder.setNegativeButton(getString(R.string.cancel_button_text), null);

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}
	
	private String getImportExportFilePath() {
		return String.format("%s/%s", Preferences.getImportExportFolder(), StringLiterals.SourceFileName);
	}
	
	private void enableDisableFields(boolean enabled) {
		View views[] = new View[] { 
				cancel, selectClass, selectMethod, sourceCode, fixIssues, classMethodSeparator, updown, saveButton, advanced };
		
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

        for (ClassMetadata aSortedClassMetadata : sortedClassMetadata) {
            ClassMetadata item = aSortedClassMetadata.shallowCopy();

            item.setUseDisplayClassName(false);
            sortedClassMetadataList.add(item);
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
						sourceCode.setUpdateSpinners(false);
						sourceCode.setSelection(methodMetadata.getPosition());
						sourceCode.setUpdateSpinners(true);
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
		SourceCode.setSourceCode(sourceCode.getEditableText().toString(), EditMethodsActivity.this);
	}

	private void promptToSaveChanges() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Save Changes");
		alertDialogBuilder.setMessage("Source code has changed.");
		
		alertDialogBuilder.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				promptDialog.dismiss();
				
				saveChanges();
			}
		});
		
		alertDialogBuilder.setNegativeButton("Discard Changes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				promptDialog.dismiss();
				
				EditMethodsActivity.this.finish();
			}
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
