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

public class SyntaxIssue {
	public enum Severity { Warning, Error }
	
	private final Severity severity;
	
	public Severity getSeverity() {
		return severity;
	}
	
	private final String message;

	public String getMessage() {
		return message;
	}

	private final String sourceName;

	public String getSourceName() {
		return sourceName;
	}

	private final int line;

	public int getLine() {
		return line;
	}

	private final String lineSource;

	public String getLineSource() {
		return lineSource;
	}

	private final int lineOffset;

	public int getLineOffset() {
		return lineOffset;
	}

	@Override
	public String toString() {
		return String.format("%s %s", severity, message);
	}

	public SyntaxIssue(Severity category, String message, String sourceName, int line, String lineSource, int lineOffset) {
		this.severity = category;
		this.message = message;
		this.sourceName = sourceName;
		this.line = line;
		this.lineSource = lineSource;
		this.lineOffset = lineOffset;
	}
}
