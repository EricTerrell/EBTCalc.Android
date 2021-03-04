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

import java.util.Stack;

import com.ericbt.rpncalc.ResultWrapper;

public class ExecuteMethodTaskResult {
	private Stack<ResultWrapper> results = new Stack<>();
	
	public Stack<ResultWrapper> getResults() {
		return results;
	}
	
	public void setResults(Stack<ResultWrapper> results) {
		this.results = results;
	}

	private Throwable exception;
	
	public Throwable getException() {
		return exception; 
	}
	
	public void setException(Throwable exception) {
		this.exception = exception;
	}
	
	private MethodMetadata methodMetadata;

	public MethodMetadata getMethodMetadata() {
		return methodMetadata;
	}
	
	public void setMethodMetadata(MethodMetadata methodMetadata) {
		this.methodMetadata = methodMetadata;
	}

}
