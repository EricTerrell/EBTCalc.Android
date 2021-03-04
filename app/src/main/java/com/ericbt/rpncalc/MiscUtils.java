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

import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class MiscUtils {
	public static int getLinePosition(int lineNumber, String text) {
		int linePosition = 0;

		for (int i = 0, start = 0; i < lineNumber; i++) {
			int returnPos = text.indexOf('\n', start);

			if (returnPos >= 0) {
				int currentLineLength = returnPos - start + 1;
				linePosition += currentLineLength;
				start += currentLineLength;
			}
		}

		return linePosition;
	}

	public static void enableDisableView(View view, boolean enabled) {
		view.setEnabled(enabled);

		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;

			for (int idx = 0; idx < group.getChildCount(); idx++) {
				enableDisableView(group.getChildAt(idx), enabled);
			}
		}
	}

	public static void logViewDimensions(View view, String name) {
		DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
		
		int width = view.getWidth();
		int height = view.getHeight();
		
		String message = String.format("%s: width(px): %d height(px): %d width(dp): %d height(dp): %d", 
									   name, width, height, (int) (width / displayMetrics.density), (int) (height / displayMetrics.density));
		
		Log.i(StringLiterals.LogTag, message);
	}

	public static String getVersionName() {
		String versionName = null;
		
		try {
			versionName = Globals.getApplication().getApplicationContext().getPackageManager().getPackageInfo(Globals.getApplication().getApplicationContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			
			Log.e(StringLiterals.LogTag, "MiscUtils.getVersionName: cannot get version name");
		}
		
		return versionName;
	}

}
