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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class SourceCodeEditText extends EditText {
	private boolean updateSpinners = true;
	
	public void setUpdateSpinners(boolean updateSpinners) {
		this.updateSpinners = updateSpinners;
	}

	public SourceCodeEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SourceCodeEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SourceCodeEditText(Context context) {
		super(context);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);

		if (updateSpinners) {
			Context context = getContext();
			
			if (context instanceof EditMethodsActivity) {
				EditMethodsActivity editMethodsActivity = (EditMethodsActivity) context;
				
				editMethodsActivity.resetSpinners();
			}
		}
	}
}
