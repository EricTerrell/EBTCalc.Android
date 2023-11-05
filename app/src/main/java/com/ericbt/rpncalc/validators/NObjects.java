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

package com.ericbt.rpncalc.validators;

import java.util.Stack;

import com.ericbt.rpncalc.Preferences;
import com.ericbt.rpncalc.ResultWrapper;

public class NObjects implements Validator {
	private final int n;
	
	public NObjects(int n) {
		this.n = n;
	}
	
	public boolean isValid(Stack<ResultWrapper> stackData, CharSequence text) {
		boolean valid = false;
		
		text = text.toString().replace(Preferences.getDecimalPointCharacter(), '.');

		if (text.length() > 0) {
			try {
				Double.parseDouble(text.toString());
				valid = 1 + stackData.size() >= n;
			}
			catch (NumberFormatException ex) {
				// empty
			}
		}
		else {
			valid = stackData.size() >= n;
		}
		
		return valid;
	}
}
