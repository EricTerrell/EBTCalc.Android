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

package com.ericbt.rpncalc.validators;

import java.util.Stack;

import org.mozilla.javascript.NativeArray;

import com.ericbt.rpncalc.ResultWrapper;

public class TopItemIs1DArray implements Validator {
	public boolean isValid(Stack<ResultWrapper> stackData, CharSequence text) {
		boolean valid = false;
		
		if (text.length() == 0) {
			if (!stackData.isEmpty() && stackData.peek().getResult() instanceof NativeArray) {
				NativeArray array = (NativeArray) stackData.peek().getResult();
				
				boolean nestedArrayFound = false;
				
				for (long i = 0; i < array.getLength(); i++) {
					if (array.get(i) instanceof NativeArray) {
						nestedArrayFound = true;
						break;
					}
				}
				
				if (!nestedArrayFound) {
					valid = true;
				}
			}
		}
		
		return valid;
	}
}
