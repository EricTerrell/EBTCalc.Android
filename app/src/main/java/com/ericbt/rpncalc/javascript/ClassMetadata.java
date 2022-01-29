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

import java.util.ArrayList;
import java.util.List;


public class ClassMetadata {
	private final String className;
	
	public String getClassName() {
		return this.className;
	}
	
	private final String displayClassName;
	
	public String getDisplayClassName() {
		return displayClassName;
	}
	
	private boolean useDisplayClassName = true;
	
	public void setUseDisplayClassName(boolean useDisplayClassName) {
		this.useDisplayClassName = useDisplayClassName;
	}

	private List<MethodMetadata> methodMetadata;
	
	public List<MethodMetadata> filterMethodMetadata(boolean getHidden) {
		List<MethodMetadata> results = new ArrayList<>();
		
		for (MethodMetadata currentMethodMetadata : methodMetadata) {
			if (getHidden || !currentMethodMetadata.isHidden()) {
				results.add(currentMethodMetadata);
			}
		}
		
		return results;
	}
	
	public List<MethodMetadata> getMethodMetadata() { 
		return methodMetadata;
	}
	
	private boolean hidden;
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	@Override
	public String toString() {
		return useDisplayClassName ? displayClassName : className;
	}
	
	public ClassMetadata(String className) {
		this.className = className;
		this.displayClassName = className;
		this.methodMetadata = new ArrayList<>();
		this.hidden = false;
	}
	
	public ClassMetadata(String className, String displayClassName) {
		this.className = className;
		this.displayClassName = displayClassName;
		this.methodMetadata = new ArrayList<>();
		this.hidden = false;
	}
	
	public ClassMetadata shallowCopy() {
		ClassMetadata shallowCopy = new ClassMetadata(className, displayClassName);

		shallowCopy.hidden = hidden;
		shallowCopy.methodMetadata = methodMetadata;
		shallowCopy.useDisplayClassName = useDisplayClassName;
		
		return shallowCopy;
	}
}
