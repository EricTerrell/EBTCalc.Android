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

package com.ericbt.rpncalc.javascript;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ericbt.rpncalc.EditMethodsActivity;
import com.ericbt.rpncalc.R;
import com.ericbt.rpncalc.StringLiterals;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener.SourceCodeStatus;

public class SourceCode {
	public static final String USER_FILE_NAME = "custom.js";

	private static String builtIn;

	private static final List<SourceCodeParseListener> sourceCodeChangeListeners = new ArrayList<>();
	
	public static int getListenerCount() {
		return sourceCodeChangeListeners.size();
	}
	
	public static void listen(SourceCodeParseListener listener) {
		sourceCodeChangeListeners.add(listener);
	}
	
	public static void unListen(SourceCodeParseListener listener) {
		sourceCodeChangeListeners.remove(listener);
	}

	private static String userCode;

	public static String getUserCode()
	{ 
		return userCode;
	}

	public static String getMergedCode() {
		return String.format("%s\r\n\r\n%s", userCode, builtIn);
	}
	
	public static void setUserCode(String userCode, Context context) {
		classMetadata = new ArrayList<>();
		
		SourceCode.userCode = userCode;

		saveUserCode(context);

		new ParseSourceCodeTask().execute();
	}
	
	private static List<ClassMetadata> classMetadata;

	public static List<ClassMetadata> filterClassMetadata(boolean getHidden) 
	{ 
		List<ClassMetadata> results = new ArrayList<>();

		for (ClassMetadata currentClassMetadata : classMetadata) {
			if ((getHidden || !currentClassMetadata.isHidden()) && currentClassMetadata.getMethodMetadata().size() > 0) {
				results.add(currentClassMetadata);
			}
		}
		
		return results; 
	}
	
	public static List<ClassMetadata> getClassMetadata() 
	{ 
		return classMetadata;
	}
	
	private static List<SyntaxIssue> syntaxWarnings;

	public static List<SyntaxIssue> getSyntaxWarnings() {
		return syntaxWarnings;
	}

	private static List<SyntaxIssue> syntaxErrors;
	
	public static List<SyntaxIssue> getSyntaxErrors() {
		return syntaxErrors;
	}
	
	static {
		classMetadata = new ArrayList<>();
	}

	public static void updateParseData(ParseSourceCodeTaskResult parseSourceCodeTaskResult) {
		Log.i(StringLiterals.LogTag, String.format("SourceCode.updateParseData %d listeners", sourceCodeChangeListeners.size()));
		
		classMetadata = parseSourceCodeTaskResult.getAllClassMetadata();
		syntaxErrors = parseSourceCodeTaskResult.getSyntaxErrors();
		syntaxWarnings = parseSourceCodeTaskResult.getSyntaxWarnings();

		broadcastStatus(SourceCodeStatus.ParsingCompleted);
	}
	
	public static void broadcastStatus(SourceCodeStatus sourceCodeStatus) {
		for (SourceCodeParseListener listener : sourceCodeChangeListeners) {
			Log.i(StringLiterals.LogTag, "SourceCode.updateParseData - begin notify listener");
			
			listener.sourceCodeChanged(sourceCodeStatus);
			
			Log.i(StringLiterals.LogTag, "SourceCode.updateParseData - end notify listener");
		}
	}
	
	private static void saveUserCode(Context context) {
		FileOutputStream fileOutputStream = null;

		try {
			fileOutputStream = context.openFileOutput(USER_FILE_NAME, Context.MODE_PRIVATE);
			fileOutputStream.write(userCode.getBytes());
		} catch (Exception ex) {
			Log.e(StringLiterals.LogTag, "SourceCode.saveUserCode", ex);
		}
		finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				}
				catch (Exception ex) {
					Log.e(StringLiterals.LogTag, "SourceCode.saveUserCode", ex);
				}
			}
		}
	}

	private static String readFromInputStream(InputStream inputStream) throws Exception {
		StringBuilder text = new StringBuilder();
		
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		
		try {
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            boolean finished = false;
            
            do {
            	String line = bufferedReader.readLine();
            	
            	if (line == null) {
            		finished = true;
            	}
            	else {
            		text.append(line);
            		text.append(StringLiterals.NewLine);
            	}
            } while (!finished);
		}        
		finally {
			try {
				if (inputStreamReader != null) {
					inputStreamReader.close();
				}
				
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (Exception ex) {
				Log.e(StringLiterals.LogTag, "SourceCode.readFromInputStream", ex);
			}
		}
		
		return text.toString();
	}

	public static void loadUserCode(Context context) {
		String userCode = "";

		try (FileInputStream fileInputStream = context.openFileInput(USER_FILE_NAME)) {
			userCode = readFromInputStream(fileInputStream);
		}
		catch (Exception ex) {
			Log.e(StringLiterals.LogTag, "SourceCode.loadUserCode", ex);
		} finally {
			if (getUserCode() == null && userCode.contains("BUILT_IN_CODE")) {
				// User is running with source code from an older version of this app. User needs
				// to remove built-in code.
				warnUserToRemoveBuiltInCode(context);
			}

			setUserCode(userCode, context);
		}
	}

	private static void warnUserToRemoveBuiltInCode(Context context) {
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(context.getString(R.string.warn_user_update_code_title));
		alertDialogBuilder.setMessage(context.getString(R.string.warn_user_update_code_message));

		alertDialogBuilder.setPositiveButton(context.getText(R.string.ok_button_text),
				(dialog, which) -> {
					context.startActivity(new Intent(context, EditMethodsActivity.class));
				});

		alertDialogBuilder.create().show();
	}

	public static void loadBuiltInCode(Context context) {
		try {
			builtIn = readFromInputStream(context.getResources().openRawResource(R.raw.builtin));
		} catch (Exception ex) {
			Log.e(StringLiterals.LogTag, "EditFunctionsActivity.loadBuiltInCode", ex);
		}
	}

	public static List<String> getMethodNames() {
		List<String> methodNames = new ArrayList<>();

		for (ClassMetadata currentClassMetadata : classMetadata) {
			if (currentClassMetadata.getMethodMetadata().size() > 0) {
				for (MethodMetadata methodMetadata : currentClassMetadata.getMethodMetadata()) {
					methodNames.add(String.format("%s.%s", methodMetadata.getClassName(), methodMetadata.getMethodName()));
				}
			}
		}
		
		return methodNames;
	}
}
