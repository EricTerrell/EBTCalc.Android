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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;

public class ResultWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Object result;
	
	public Object getResult() {
		return result;
	}
	
	private String resultString;
	
	public void setResultString(String resultString) {
		this.resultString = resultString;
	}
	
	@Override
	public String toString() {
		return resultString;
	}

	public static ResultWrapper createUsingContext(Object result) {
		ResultWrapper resultWrapper = new ResultWrapper(result);
		resultWrapper.setResultString(Context.toString(result));
		
		if (result instanceof NativeArray) {
			resultWrapper.setResultString(arrayText((NativeArray) result));
		}
		
		return resultWrapper;
	}
	
	private static String arrayText(NativeArray array) {
		StringBuilder result = new StringBuilder("[");

		for (long i = 0; i < array.getLength(); i++) {
			Object obj = array.get(i);
			
			if (obj instanceof NativeArray) {
				result.append(arrayText((NativeArray) obj));
			}
			else {
				String text = Context.toString(obj);
				
				if (obj instanceof String) {
					text = String.format("\"%s\"", text);
				}
				
				result.append(text);
			}
			
			if (i < array.getLength() - 1) {
				result.append(", ");
			}
		}
		
		result.append("]");
		
		return result.toString();
	}
	
	public ResultWrapper(Object result) {
		this.result = result;
		this.resultString = null;
	}

	public static String getValueString(ResultWrapper value) {
		String valueString;
		
		Object valueObj = value.getResult();
		
		if (valueObj != null) {
			if (valueObj instanceof String) {
		        valueString = String.format("\"%s\"", valueObj);
			}
			else if (valueObj instanceof Double) {
		        valueString = formatDouble((Double) valueObj);
			}
			else {
	        	valueString = value != null ? value.toString() : "null";
			}
		}
		else {
			valueString = "null";
		}
		
        return valueString;
	}
	
	private static String formatDouble(double value) {
		String valueString;
		
		if (Math.abs(value) >= 1.0e10 || (Math.abs(value) > 0.0 && Math.abs(value) <= 1.0e-10)) {
			DecimalFormat formatter = new DecimalFormat("0.####################################################################################################E0");
		    valueString = formatter.format(value);
		}
		else if (Globals.getDigitsPastDecimalPoint() >= 0) {
			String formatString = String.format(Locale.ROOT, "%%.%df", Globals.getDigitsPastDecimalPoint());
			
	        valueString = String.format(Locale.ROOT, formatString, value);
		}
		else {
			String formatString = String.format(Locale.ROOT, "%%.%df", 100);
			
	        valueString = String.format(Locale.ROOT, formatString, value);
	        valueString = removeZerosToRightOfDecimal(valueString);
		}
        
		valueString = addThousandsSeperators(valueString);
		valueString = localizeValue(valueString);
		
        return valueString;
	}
	
	private static String addThousandsSeperators(String valueString) {
		String result;
		
		if (Preferences.getThousandsSeparator()) {
			StringBuilder stringBuilder = new StringBuilder();
	
			boolean foundDecimalPoint = false;
			int digitsToLeft = 0;
			
			for (int i = valueString.length() - 1; i >= 0; i--) {
				char currentChar = valueString.charAt(i);
				
				if (foundDecimalPoint) {
					digitsToLeft++;
				}
				
				if (currentChar == '.') {
					foundDecimalPoint = true;
				}
				
				stringBuilder.insert(0, currentChar);
	
				if (digitsToLeft > 0 && digitsToLeft % 3 == 0 && i > 0 && Character.isDigit(valueString.charAt(i - 1)) /* Don't want to put a comma right after a - sign. */) {
					stringBuilder.insert(0, ',');
				}
			}
			
			result = stringBuilder.toString();
		}
		else {
			result = valueString;
		}
		
		return result;
	}
	
	/**
	 * Convert floating point value in ROOT locale (1,000.23) to user's localed (e.g. German User would get 1.000,23).
	 * @param valueString floating point value string in ROOT locale
	 * @return valueString in user's locale.
	 */
	private static String localizeValue(String valueString) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		if (numberFormat instanceof DecimalFormat) {
			DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();

			StringBuilder stringBuilder = new StringBuilder(valueString);
			
			for (int i = 0; i < stringBuilder.length(); i++) {
				char originalChar = valueString.charAt(i);
				
				if (originalChar == '.') {
					stringBuilder.setCharAt(i, decimalFormatSymbols.getDecimalSeparator());
				}
				else if (originalChar == ',') {
					stringBuilder.setCharAt(i, decimalFormatSymbols.getGroupingSeparator());
				}
			}
			
			valueString = stringBuilder.toString();
		}
		
		return valueString;
	}
	
	private static String removeZerosToRightOfDecimal(String valueString) {
		StringBuilder result = new StringBuilder(valueString);
		
		int decimalPos = valueString.indexOf('.');
		
		if (decimalPos >= 0) {
			int i = result.length() - 1;
			
			while (i > decimalPos && result.charAt(result.length() - 1) == '0') {
				result.deleteCharAt(result.length() - 1);
			}
		}
		
		return result.toString();
	}
}
