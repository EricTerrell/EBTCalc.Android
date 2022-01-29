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

package com.ericbt.rpncalc;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Preferences {
	private static final String USER_ACCEPTED_TERMS_KEY = "UserAcceptedTerms";
	private static final String STACK_CONTENTS_KEY = "StackContents";
	private static final String TOP_LINE_KEY = "TopLine";
	private static final String JAVASCRIPT_GLOBALS_KEY = "JavascriptGlobals";
	private static final String DIGITS_PAST_DECIMAL_POINT_KEY = "DigitsPastDecimalPoint";
	private static final String IMPORT_EXPORT_FOLDER_KEY = "import_export_folder";
	public static final String FORCE_1_COLUMN_IN_PORTRAIT = "force_1_column_in_portrait";

	public static void setDefaultValues() {
		if (getImportExportFolder() == null || getImportExportFolder().trim().length() == 0) {
			putImportExportFolder(Environment.getExternalStorageDirectory().getPath());
		}
	}
	
	public static int getKeyclickVolume() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		String value = sharedPreferences.getString("keyclick_volume", "0");
		
		return Integer.parseInt(value);
	}
	
	public static boolean getThousandsSeparator() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		return sharedPreferences.getBoolean("thousands_separator", true);
	}
	
	public static boolean isUncaughtExceptionLoggingEnabled() { 
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		return sharedPreferences.getBoolean("IsUncaughtExceptionLoggingEnabled", false);
	}
	
	public static int getMethodTimeout() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		return Integer.parseInt(sharedPreferences.getString("method_timeout", "15"));
	}
	
	public static boolean getUserAcceptedTerms() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext()).getBoolean(USER_ACCEPTED_TERMS_KEY, false);
	}
	
	public static void putUserAcceptedTerms(boolean userAcceptedTerms) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(USER_ACCEPTED_TERMS_KEY, userAcceptedTerms);
		editor.apply();
	}
	
	public static String getTopLine() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		return sharedPreferences.getString(TOP_LINE_KEY, "");
	}
	
	public static void putTopLine(String stackContents) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(TOP_LINE_KEY, stackContents);
		editor.apply();
	}

	public static String getStackContents() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		return sharedPreferences.getString(STACK_CONTENTS_KEY, null);
	}
	
	public static void putStackContents(String stackContents) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(STACK_CONTENTS_KEY, stackContents);
		editor.apply();
	}

	public static String getJavascriptGlobals() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		return sharedPreferences.getString(JAVASCRIPT_GLOBALS_KEY, null);
	}
	
	public static void putJavascriptGlobals(String javascriptGlobals) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(JAVASCRIPT_GLOBALS_KEY, javascriptGlobals);
		editor.apply();
	}
	
	public static int getDigitsPastDecimalPoint() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());

		return sharedPreferences.getInt(DIGITS_PAST_DECIMAL_POINT_KEY, -1);
	}
	
	public static void putDigitsPastDecimalPoint(int digitsPastDecimalPoint) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt(DIGITS_PAST_DECIMAL_POINT_KEY, digitsPastDecimalPoint);
		editor.apply();
	}
	
	public static String getImportExportFolder() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		return sharedPreferences.getString(IMPORT_EXPORT_FOLDER_KEY, null);
	}
	
	private static void putImportExportFolder(String importExportFolder) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(IMPORT_EXPORT_FOLDER_KEY, importExportFolder);
		editor.apply();
	}
	
	/**
	 * Return the preferred decimal point character from the user's locale
	 * @return decimal point character
	 */
	public static char getDecimalPointCharacter() {
		char decimalPoint = '.';
		
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		if (numberFormat instanceof DecimalFormat) {
			DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			
			decimalPoint = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
		}
		
		return decimalPoint;
	}

	/**
	 * Return the preferred thousands separator character from the user's locale
	 * @return decimal point character
	 */
	public static char getThousandsSeparatorCharacter() {
		char decimalPoint = '.';
		
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		if (numberFormat instanceof DecimalFormat) {
			DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			
			decimalPoint = decimalFormat.getDecimalFormatSymbols().getGroupingSeparator();
		}
		
		return decimalPoint;
	}
	
	public static boolean getForce1ColumnValue() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext().getApplicationContext());

		return sharedPreferences.getBoolean(FORCE_1_COLUMN_IN_PORTRAIT, false);
	}

}
