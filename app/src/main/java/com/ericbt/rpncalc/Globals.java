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

import org.mozilla.javascript.NativeObject;

public class Globals {
	private static RPNCalcApplication rpnCalcApplication;
	
	public static RPNCalcApplication getApplication() { return rpnCalcApplication; }
	
	public static void setApplication(RPNCalcApplication rpnCalcApplication) {
		Globals.rpnCalcApplication = rpnCalcApplication;
	}

	private static int digitsPastDecimalPoint = -1;

	public static int getDigitsPastDecimalPoint() {
		return digitsPastDecimalPoint;
	}

	public static void setDigitsPastDecimalPoint(int digitsPastDecimalPoint) {
		Globals.digitsPastDecimalPoint = digitsPastDecimalPoint;
	}
	
	private static NativeObject javascriptGlobals = null;
	
	public static NativeObject getJavascriptGlobals() {
		return javascriptGlobals;
	}
	
	public static void setJavascriptGlobals(NativeObject javascriptGlobals) {
		Globals.javascriptGlobals = javascriptGlobals;
	}
	
	public static boolean isFreeVersion () {
		return rpnCalcApplication.getString(R.string.app_version).equals("free");
	}
	
	private static boolean initialLaunch;
	
	public static boolean getInitialLaunch() {
		return initialLaunch;
	}
	
	public static void setInitialLaunch(boolean initialLaunch) {
		Globals.initialLaunch = initialLaunch;
	}
}
