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

package com.ericbt.rpncalc.javascript;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.ericbt.rpncalc.MiscUtils;
import com.ericbt.rpncalc.R;
import com.ericbt.rpncalc.StringLiterals;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener.SourceCodeStatus;

public class SourceCode {
	private static List<SourceCodeParseListener> sourceCodeChangeListeners = new ArrayList<>();
	
	public static int getListenerCount() {
		return sourceCodeChangeListeners.size();
	}
	
	public static void listen(SourceCodeParseListener listener) {
		sourceCodeChangeListeners.add(listener);
	}
	
	public static void unListen(SourceCodeParseListener listener) {
		sourceCodeChangeListeners.remove(listener);
	}

	private static String sourceCode;

	public static String getSourceCode() 
	{ 
		return sourceCode; 
	}
	
	public static void setSourceCode(String sourceCode, Context context) {
		classMetadata = new ArrayList<>();
		
		SourceCode.sourceCode = sourceCode;
		saveSourceCode(sourceCode, context);
		
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
	
	public static List<MethodMetadata> getMethodMetadata(String className) {
		List<MethodMetadata> methodMetadata = new ArrayList<>();
		
		for (ClassMetadata item : classMetadata) {
			if (item.getClassName().equals(className)) {
				methodMetadata.addAll(item.getMethodMetadata());
			}
		}
		
		return methodMetadata;
	}
	
	private static void saveSourceCode(String sourceCode, Context context) {
		FileOutputStream fileOutputStream = null;
		
		try {
			fileOutputStream = context.openFileOutput(StringLiterals.SourceFileName, Context.MODE_PRIVATE);
			fileOutputStream.write(sourceCode.getBytes());
		}
		catch (Exception ex) {
			Log.e(StringLiterals.LogTag, "EditFunctionsActivity.saveSourceCode", ex);
		}
		finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				}
				catch (Exception ex) {
					Log.e(StringLiterals.LogTag, "EditFunctionsActivity.saveSourceCode", ex);
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
				Log.e(StringLiterals.LogTag, "EditFunctionsActivity.readFromInputStream", ex);
			}
		}
		
		return text.toString();
	}
	
	public static void loadSourceCode(Context context) {
		String loadedSourceCode = "";
		
		try {
			FileInputStream fileInputStream = context.openFileInput(StringLiterals.SourceFileName);
			
			loadedSourceCode = readFromInputStream(fileInputStream);
		}
		catch (FileNotFoundException ex) {
			loadedSourceCode = getInitialSourceCode(context);
			Log.i(StringLiterals.LogTag, "SourceCode.loadSourceCode: creating initial source code");
		}
		catch (Exception ex) {
			Log.e(StringLiterals.LogTag, "EditFunctionsActivity.loadSourceCode", ex);
		}
		
		SourceCode.setSourceCode(loadedSourceCode, context);
	}
		
	public static String getInitialSourceCode(Context context, String userCode) {
		String builtInsBegin = String.format("/* BUILT_IN_CODE_BEGIN (version %s) */", MiscUtils.getVersionName());
		String builtInsEnd   = "/* BUILT_IN_CODE_END */";
		String userCodeComment = "/* ***** PUT YOUR CODE HERE, BETWEEN THE USER_CODE_BEGIN AND USER_CODE_END markers ***** */";
		String userCodeBegin = String.format("/* USER_CODE_BEGIN */\n\n%s\n\n", userCodeComment);
		String userCodeEnd = "/* USER_CODE_END */";
		
		return String.format("%s\n\n%s\n\n%s\n\n%s%s%s", 
							 builtInsBegin,
							 getBuiltInSourceCode(context), 
							 builtInsEnd,
							 userCodeBegin,
							 userCode.replace(userCodeComment, ""),
							 userCodeEnd);
	}

	public static String getInitialSourceCode(Context context) {
		return getInitialSourceCode(context, "");
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
	
	private static String getBuiltInSourceCode(Context context) {
		String builtInSourceCode = null;
		
		try {
			builtInSourceCode = readFromInputStream(context.getResources().openRawResource(R.raw.custom));
	    } catch (Exception e) {
	    	Log.e(StringLiterals.LogTag, "SourceCode.getBuiltInSourceCode: cannot read raw file");
	    }
		
		return builtInSourceCode;
	}
}
