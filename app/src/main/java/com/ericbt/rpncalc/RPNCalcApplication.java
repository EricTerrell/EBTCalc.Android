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

package com.ericbt.rpncalc;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.ericbt.rpncalc.javascript.SourceCode;

public class RPNCalcApplication extends Application {
	private final CustomActivityLifecycleCallbacks customActivityLifecycleCallbacks =
			new CustomActivityLifecycleCallbacks();

	public Activity getCurrentActivity() {
		return customActivityLifecycleCallbacks.getCurrentActivity();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler());
		
		Log.i(StringLiterals.LogTag, "RPNCalcApplication.onCreate");
		Log.i(StringLiterals.LogTag, String.format("SourceCode.getListenerCount: %d", SourceCode.getListenerCount()));

		registerActivityLifecycleCallbacks(customActivityLifecycleCallbacks);

		Globals.setApplication(this);
		Globals.setInitialLaunch(true);
	}
}
