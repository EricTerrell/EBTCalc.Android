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

package com.ericbt.rpncalc;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class BackgroundTasks {
	@SuppressWarnings("rawtypes")
	private static List<AsyncTask> asyncTasks = new ArrayList<>();
	
	public static synchronized void add(@SuppressWarnings("rawtypes") AsyncTask asyncTask) {
		asyncTasks.add(asyncTask);
	}
	
	@SuppressWarnings("rawtypes")
	public static synchronized void remove(AsyncTask asyncTask) {
		asyncTasks.remove(asyncTask);
	}
	
	@SuppressWarnings("rawtypes")
	public static synchronized void cancel(boolean exceptParse) {
		Log.i(StringLiterals.LogTag, "BackgroundTasks.cancel");
		
		for (AsyncTask asyncTask : asyncTasks) {
			String className = asyncTask.getClass().toString();
			boolean parse = "class com.ericbt.rpncalc.javascript.ParseSourceCodeTask".equals(className);
			
			if (!parse || !exceptParse) {
				Log.i(StringLiterals.LogTag, String.format("BackgroundTasks.cancel: cancelling %s", asyncTask.getClass()));
				asyncTask.cancel(true);
			}
		}
	}
}
