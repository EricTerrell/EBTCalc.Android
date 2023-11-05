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

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.ericbt.rpncalc.javascript.SyntaxIssue.Severity;


public class ParseErrorReporter implements ErrorReporter {
	private final List<SyntaxIssue> syntaxErrors;
	
	public List<SyntaxIssue> getSyntaxErrors() {
		return syntaxErrors;
	}

	private final List<SyntaxIssue> syntaxWarnings;
	
	public List<SyntaxIssue> getSyntaxWarnings() {
		return syntaxWarnings;
	}
	
	public ParseErrorReporter() {
		syntaxErrors = new ArrayList<>();
		syntaxWarnings = new ArrayList<>();
	}

	@Override
	public void warning(String message, String sourceName, int line,
			String lineSource, int lineOffset) {
		syntaxWarnings.add(new SyntaxIssue(Severity.Warning, message, sourceName, line, lineSource, lineOffset));
	}

	@Override
	public void error(String message, String sourceName, int line,
			String lineSource, int lineOffset) {
		syntaxErrors.add(new SyntaxIssue(Severity.Error, message, sourceName, line, lineSource, lineOffset));
	}

	@Override
	public EvaluatorException runtimeError(String message, String sourceName,
			int line, String lineSource, int lineOffset) {
		return new EvaluatorException("runtime error", sourceName, line, lineSource, lineOffset);
	}
}
