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

package com.ericbt.rpncalc.javascript;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;

import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.rpncalc.BackgroundTasks;
import com.ericbt.rpncalc.StringLiterals;
import com.ericbt.rpncalc.javascript.MethodMetadata.MethodType;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener.SourceCodeStatus;

public class ParseSourceCodeTask extends AsyncTask<Void, Void, ParseSourceCodeTaskResult> {
	private static Pattern buttonPattern;
	private static Pattern categoryPattern;
	
	private static boolean sourceCodeChanging = false;
	
	public synchronized static boolean isSourceCodeChanging() {
		return ParseSourceCodeTask.sourceCodeChanging;
	}
	
	private synchronized static void setSourceCodeChanging(boolean sourceCodeChanging) {
		ParseSourceCodeTask.sourceCodeChanging = sourceCodeChanging;
	}
	
	private static int sourceCodeVersion = -1;
	
	public synchronized static int getSourceCodeVersion() {
		return ParseSourceCodeTask.sourceCodeVersion;
	}
	
	private synchronized static void incrementSourceCodeVersion() {
		sourceCodeVersion++;
	}
	
	@Override
	protected void onPreExecute() {
		SourceCode.broadcastStatus(SourceCodeStatus.ParsingStarted);
		
		BackgroundTasks.add(this);

		ParseSourceCodeTask.setSourceCodeChanging(true);
	}

	@Override
	protected ParseSourceCodeTaskResult doInBackground(Void... params) {
		return parseSourceCode();
	}

	@Override
	protected void onPostExecute(ParseSourceCodeTaskResult result) {
		Log.i(StringLiterals.LogTag, "ParseSourceCodeTask.onPostExecute");
		
		BackgroundTasks.remove(this);
		
		SourceCode.updateParseData(result);

		ParseSourceCodeTask.setSourceCodeChanging(false);
		ParseSourceCodeTask.incrementSourceCodeVersion();
	}
	
	@Override
	protected void onCancelled(ParseSourceCodeTaskResult result) {
		Log.i(StringLiterals.LogTag, "ParseSourceCodeTask.onCancelled");
		
		BackgroundTasks.remove(this);

		ParseSourceCodeTask.setSourceCodeChanging(false);
	}

	private static ParseSourceCodeTaskResult parseSourceCode() {
		long startTime = System.currentTimeMillis();

        final CompilerEnvirons compilerEnv = new CompilerEnvirons();
        compilerEnv.setLanguageVersion(CustomContext.JAVASCRIPT_VERSION);
        compilerEnv.setOptimizationLevel(-1);

        final ParseErrorReporter parseErrorReporter = new ParseErrorReporter();

        final CustomNodeVisitor customNodeVisitor = new CustomNodeVisitor();

		List<ClassMetadata> allClassMetadata = new ArrayList<>();

        try {
	        // Create an instance of the parser...
	        final Parser parser = new Parser(compilerEnv, parseErrorReporter);

	        final AstRoot astRoot = parser.parse(SourceCode.getMergedCode(), StringLiterals.Empty, 1);
	        
	        astRoot.visit(customNodeVisitor);

	        allClassMetadata = getClassMetadata(customNodeVisitor,
					getButtonConversions(SourceCode.getMergedCode()),
					getCategoryConversions(SourceCode.getMergedCode()));
        }
        catch (Exception ex) {
        	Log.i(StringLiterals.LogTag, "SourceCode.parseSourceCode: runtime exception");
        }

		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("SourceCode.parseSourceCode: %d ms", elapsedMilliseconds));
		
		return new ParseSourceCodeTaskResult(allClassMetadata, parseErrorReporter.getSyntaxErrors(), parseErrorReporter.getSyntaxWarnings());
	}
	
	private static List<ClassMetadata> getClassMetadata(CustomNodeVisitor customNodeVisitor, 
			                                            Map<String, String> buttonConversions,
			                                            Map<String, String> classNameConversions) {
		List<ClassMetadata> allClassMetadata = new ArrayList<>();
		
		Map<String, Boolean> niladicConstructors = new HashMap<>();
		
		for (String niladicConstructor : customNodeVisitor.getNiladicConstructors()) {
			niladicConstructors.put(niladicConstructor, Boolean.TRUE);
		}
		
		for (String className : customNodeVisitor.getConstructors()) {
			String displayClassName = classNameConversions.containsKey(className) ? classNameConversions.get(className) : className;
			ClassMetadata classMetadata = new ClassMetadata(className, displayClassName);
			
			for (MethodMetadata methodMetadata : customNodeVisitor.getMethods()) {
				if (methodMetadata.getClassName().equals(className)) {
					if ((methodMetadata.getMethodType() == MethodType.ClassMethod) ||
						(methodMetadata.getMethodType() == MethodType.InstanceMethod)) {
						String buttonText = buttonConversions.get(String.format("%s.%s", className, methodMetadata.getMethodName()));
						methodMetadata.setButtonText(buttonText);
						
						classMetadata.getMethodMetadata().add(methodMetadata);
					}
				}
			}
			
			boolean hidden = !niladicConstructors.containsKey(classMetadata.getClassName()) ||
                    classMetadata.getMethodMetadata().isEmpty();
			
			classMetadata.setHidden(hidden);
			
			allClassMetadata.add(classMetadata);
		}
		
		return allClassMetadata;
	}
	
	private static Map<String, String> getButtonConversions(String sourceCode) {
		Map<String, String> buttonConversions = new HashMap<>();
		
		if (buttonPattern == null) {
			buttonPattern = Pattern.compile("[\\s]*//[\\s]*button[\\s]+([\\S]+)[\\s]+\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
		}
		
		Matcher matcher = buttonPattern.matcher(sourceCode);
		
		while (matcher.find()) {
			buttonConversions.put(matcher.group(1), matcher.group(2));
		}
		
		return buttonConversions;
	}
	
	private static Map<String, String> getCategoryConversions(String sourceCode) {
		Map<String, String> categoryConversions = new HashMap<>();
		
		if (categoryPattern == null) {
			categoryPattern = Pattern.compile("[\\s]*//[\\s]*category[\\s]+([\\S]+)[\\s]+\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
		}
		
		Matcher matcher = categoryPattern.matcher(sourceCode);
		
		while (matcher.find()) {
			categoryConversions.put(matcher.group(1), matcher.group(2));
		}
		
		return categoryConversions;
	}
	
}
