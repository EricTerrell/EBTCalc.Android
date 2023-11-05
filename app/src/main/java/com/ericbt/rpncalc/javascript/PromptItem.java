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

package com.ericbt.rpncalc.javascript;

public class PromptItem {
	private String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	private final String labelText;

	public String getLabelText() {
		return labelText;
	}
	
	private final String itemTypeString;

	public enum ItemType { None, Value, Select }
	
	public ItemType getItemType() {
		ItemType itemType = ItemType.None;
		
		if (itemTypeString != null) {
			if (itemTypeString.compareToIgnoreCase("v") == 0) {
				itemType = ItemType.Value;
			}
			else if (itemTypeString.compareToIgnoreCase("s") == 0) {
				itemType = ItemType.Select;
			}
		}
		
		return itemType;
	}

	private final String regexOrSpinnerValues;

	public String getRegexOrSpinnerValues() {
		return regexOrSpinnerValues;
	}
	
	private final String defaultValue;

	public String getDefaultValue() {
		return defaultValue;
	}
	
	private int spinnerIndex;
	
	public int getSpinnerIndex() {
		return spinnerIndex;
	}

	public void setSpinnerIndex(int spinnerIndex) {
		this.spinnerIndex = spinnerIndex;
	}

	private boolean valid;
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public PromptItem(String labelText, String itemTypeString, String regexOrSpinnerValues, String defaultValue) {
		this.labelText = labelText;
		this.itemTypeString = itemTypeString;
		this.regexOrSpinnerValues = regexOrSpinnerValues;
		this.defaultValue = defaultValue;

		try {
			this.spinnerIndex = Integer.parseInt(defaultValue);
		}
		catch (Exception ex) {
			this.spinnerIndex = 0;
		}

		this.value = defaultValue != null ? defaultValue : "";
		this.valid = false;
	}
}
