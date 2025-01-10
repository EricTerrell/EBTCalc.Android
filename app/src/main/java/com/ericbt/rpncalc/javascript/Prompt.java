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

package com.ericbt.rpncalc.javascript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.NativeArray;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ericbt.rpncalc.Globals;
import com.ericbt.rpncalc.StringLiterals;

public class Prompt implements Serializable {
	/**
	 * This class is Serializable to avoid causing the serialization of the javascriptGlobals object to fail.
	 * All fields are marked transient because we don't actually want to ever deserialize objects of this class.
	 */
	private static final long serialVersionUID = -7590657724029414803L;

	private transient static List<PromptItem> promptItems;
	
	private transient String title;

	public static List<PromptItem> getPromptItems() {
		return promptItems;
	}
	
	public static void setPromptItems(List<PromptItem> promptItems) {
		Prompt.promptItems = promptItems;
	}
	
	private transient static boolean resultsAreAvailable;
	
	private synchronized static boolean resultsAreAvailable() {
		return Prompt.resultsAreAvailable;
	}

	private boolean areResultsValid() {
		boolean areResultsValid = true;
		
		for (PromptItem promptItem : promptItems) {
			if (!promptItem.isValid()) {
				areResultsValid = false;
				break;
			}
		}
		
		return areResultsValid;
	}
	
	public synchronized static void setResultsAreAvailable(boolean resultsAreAvailable) {
		Prompt.resultsAreAvailable = resultsAreAvailable;
		
		if (resultsAreAvailable) {
			CustomContextFactory.setStartTime();
		}
	}

	// This method is really used - called from custom code (e.g. Computer Math / Enter Bin.
	public NativeArray prompt(NativeArray nativeArray, String title) {
		setResultsAreAvailable(false);
		
		this.title = title;
		
		List<PromptItem> promptItems = new ArrayList<>((int) nativeArray.getLength());
		
		for (int i = 0; i < nativeArray.getLength(); i++) {
			NativeArray row = (NativeArray) nativeArray.get(i);
			
			PromptItem promptItem = new PromptItem((String) row.get(0), (String) row.get(1), (String) row.get(2), (String) row.get(3));
			promptItems.add(promptItem);
		}
		
		Prompt.promptItems = promptItems;

		final Activity currentActivity = Globals.getApplication().getCurrentActivity();

		currentActivity.runOnUiThread(() -> {
			final Intent promptActivity =
					new Intent(currentActivity, PromptActivity.class);
			promptActivity.putExtra("Title", Prompt.this.title);

			currentActivity.startActivity(promptActivity);
		});
		
		while (!resultsAreAvailable()) {
			try {
				Thread.sleep(1000);
			}
			catch (Exception ex) {
				Log.e(StringLiterals.LogTag, "Prompt.prompt", ex);
			}
		}
		
		NativeArray result = null;
		
		if (Prompt.promptItems != null) {
			String[] resultArray = new String[Prompt.promptItems.size()];
			
			for (int i = 0; i < resultArray.length; i++) {
				resultArray[i] = Prompt.promptItems.get(i).getValue();
			}
			
			result = new NativeArray(resultArray);
			
			// Reset the timeout clock so that we don't time out because the dialog was displayed longer than the timeout.
			CustomContextFactory.setStartTime();
			
			result = areResultsValid() ? result : null;
		}
		
		return result;
	}
}
