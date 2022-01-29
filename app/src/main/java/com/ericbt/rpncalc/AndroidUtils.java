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

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AndroidUtils {
	/**
	 * Simulate a click on the specified Button when Enter is pressed in the specified EditText.
	 * @param editText EditText in which user presses Enter (or clicks DONE on the keyboard)
	 * @param button Button that will be clicked implicitly when Enter is pressed.
	 */
	public static void clickButtonWhenEnterPressed(EditText editText, final Button button) {
		editText.setOnKeyListener((v, keyCode, event) -> {
			boolean result = false;

			// If the event is a key-down event on the "enter" button
			if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && button.isEnabled() && button.getVisibility() == View.VISIBLE) {
				button.performClick();
				result = true;
			}

			return result;
		});
	}

}
