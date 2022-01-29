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

import java.util.List;


public class ParseSourceCodeTaskResult {
	private final List<ClassMetadata> allClassMetadata;
	
	public List<ClassMetadata> getAllClassMetadata() {
		return allClassMetadata;
	}
	
	private final List<SyntaxIssue> syntaxErrors;

	public List<SyntaxIssue> getSyntaxErrors() {
		return syntaxErrors;
	}
	
	private final List<SyntaxIssue> syntaxWarnings;

	public List<SyntaxIssue> getSyntaxWarnings() {
		return syntaxWarnings;
	}
	
	public ParseSourceCodeTaskResult(List<ClassMetadata> allClassMetadata, List<SyntaxIssue> syntaxErrors, List<SyntaxIssue> syntaxWarnings) {
		this.allClassMetadata = allClassMetadata;
		this.syntaxErrors = syntaxErrors;
		this.syntaxWarnings = syntaxWarnings;
	}

}
