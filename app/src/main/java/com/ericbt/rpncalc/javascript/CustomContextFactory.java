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

import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import android.util.Log;

import com.ericbt.rpncalc.StringLiterals;

public class CustomContextFactory extends ContextFactory {
	private static long startTime;
	
	public synchronized static void setStartTime() {
		CustomContextFactory.startTime = System.currentTimeMillis();
	}
	
	private int timeoutSeconds;
	private ExecuteMethodTask executeMethodTask;
	
	public void start(int timeoutSeconds, ExecuteMethodTask executeMethodTask) {
		setStartTime();
		this.timeoutSeconds = timeoutSeconds;
		this.executeMethodTask = executeMethodTask;
		
		Log.i(StringLiterals.LogTag, String.format("CustomContextFactory.start startTime: %d %s", startTime, new Date(startTime)));
	}

	@Override
	protected void observeInstructionCount(Context cx, int instructionCount) {
		long now = System.currentTimeMillis();
		long elapsedTime = (now - startTime) / 1000;
		
		if (executeMethodTask.isCancelled()) {
			throw new Error("cancelled");
		}
		// Timeout if the the script run has gone for more than the specified number of seconds.
		else if (timeoutSeconds > 0 && elapsedTime > timeoutSeconds) {
        	throw new Error(String.format("This method was stopped because it took longer to execute than the %d second Method Timeout value specified in SETTINGS.", timeoutSeconds));
        }
	}

	@Override
	public Context enterContext() {
		final Context context = enterContext(new CustomContext(this));
		
		// Android requires optimization level -1.
		// https://github.com/mozilla/rhino/discussions/1162
		context.setOptimizationLevel(-1);

		context.setLanguageVersion(CustomContext.JAVASCRIPT_VERSION);

		// Enable instruction observer callbacks.
		context.setInstructionObserverThreshold(1000);
		
		return context;
	}
}
