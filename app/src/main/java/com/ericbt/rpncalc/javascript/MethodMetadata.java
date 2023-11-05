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

public class MethodMetadata {
	private final String className;
	
	public String getClassName() {
		return className;
	}

	private final String methodName;
	
	public String getMethodName() {
		return methodName;
	}

	private List<String> arguments = new ArrayList<>();
	
	public List<String> getArguments() {
		return arguments;
	}
	
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	
	private int position;
	
	public int getPosition() {
		return position;
	}
	
	private boolean hidden;
	
	public boolean isHidden() {
		return hidden;
	}
	
	private String buttonText;
	
	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}
	
	public String getButtonText() {
		return buttonText != null ? buttonText : methodName;
	}
	
	public boolean isButtonTextCustom() {
		return buttonText != null;
	}
	
	@Override
	public String toString() {
		return methodName;
	}

	public enum MethodType { ClassMethod, InstanceMethod, GlobalFunction }
	
	private MethodType methodType;
	
	public MethodType getMethodType() {
		return methodType;
	}
	
	public void setMethodType(MethodType methodType) {
		this.methodType = methodType;
	}
	
	public MethodMetadata(String className, String methodName, List<String> arguments, MethodType methodType, int position, boolean hidden) {
		this.className = className;
		this.methodName = methodName;
		this.arguments = arguments;
		this.methodType = methodType;
		this.position = position;
		this.hidden = hidden;
	}
	
	public MethodMetadata(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}
	
	public String getArgumentList() {
    	StringBuilder argumentList = new StringBuilder();
    	
    	for (int i = 0; i < arguments.size(); i++) {
    		argumentList.append(arguments.get(i));
    		
    		if (i < arguments.size() - 1) {
    			argumentList.append(',');
    		}
    	}

    	return argumentList.toString();
	}
}
