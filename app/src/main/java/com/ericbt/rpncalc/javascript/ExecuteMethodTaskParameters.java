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

import com.ericbt.rpncalc.DisplayFragment;

public class ExecuteMethodTaskParameters {
	private MethodMetadata methodMetadata;
	
	private ExecuteMethodTask executeMethodTask;

	public void setExecuteMethodTask(ExecuteMethodTask executeMethodTask) {
		this.executeMethodTask = executeMethodTask;
	}

	public MethodMetadata getMethodMetadata() {
		return methodMetadata;
	}

	public ExecuteMethodTask getExecuteMethodTask() {
		return executeMethodTask;
	}
	
	private DisplayFragment displayFragment;
	
	public DisplayFragment getDisplayFragment() {
		return displayFragment;
	}

	public ExecuteMethodTaskParameters(MethodMetadata methodMetadata, DisplayFragment displayFragment) {
		this.methodMetadata = methodMetadata;
		this.displayFragment = displayFragment;
		this.executeMethodTask = null;
	}
}
